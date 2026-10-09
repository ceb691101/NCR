package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.BillCycleConfig;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.dto.HsbProvinceDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillCycleService {

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private NcreAuthorizationService ncreAuthorizationService;

    @Autowired
    private YearTariffSetupService yearTariffSetupService;

    private static final Integer DEFAULT_BILL_CYCLE = 0; // Default value when no bill cycle exists

    public Optional<Integer> getCurrentBillCycleNumber() {
        Optional<Integer> numOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
        if (numOpt.isPresent()) {
            return numOpt;
        }
        return getCurrentOpenBillCycle().map(BillCycleDTO.CurrentBillCycleDTO::getBillCycle);
    }
    private static final String CURRENT_OPEN_BILL_CYCLE_QUERY = """
            SELECT bill_cycle, bill_year, bill_month, is_current, is_closed
            FROM dbadmin.ncre_bill_cycle
            WHERE is_current = 1
            ORDER BY bill_cycle DESC
            """;

        /** Active developers are identified by status = 2 in ncre_developers. */
    private static final String ACTIVE_DEVELOPERS_FOR_CYCLE_QUERY = """
            SELECT COUNT(*) AS active_developers
            FROM dbadmin.ncre_invoice_create ic
            WHERE ic.bill_cycle = ?
            """;

    /**
     * Invoice creation records for a bill cycle, counted straight from the table:
     * created is is_create = 1 and pending is is_create = 0. They are independent
     * counts from the database, not a subtraction of one from the other.
     */
    private static final String INVOICE_CREATE_COUNTS_QUERY = """
            SELECT COUNT(CASE WHEN ic.is_create = 1 THEN 1 END) AS invoices_created,
                   COUNT(CASE WHEN ic.is_create IS NULL OR ic.is_create <> 1 THEN 1 END) AS invoices_pending
            FROM dbadmin.ncre_invoice_create ic
            WHERE ic.bill_cycle = ?
            """;

            private static final String INVOICE_CREATE_ENTRIES_QUERY = """
                 SELECT ic.folio_no,
                     MAX(d.developer_name) AS developer_name,
                     ic.is_create,
                     ic.remarks
                 FROM dbadmin.ncre_invoice_create ic
                 LEFT JOIN dbadmin.ncre_developers d ON d.folio_no = ic.folio_no
                 WHERE ic.bill_cycle = ?
                 GROUP BY ic.folio_no, ic.is_create, ic.remarks
                 ORDER BY ic.folio_no
                 """;

    /**
     * Active developers that do NOT have a successfully created invoice (is_create = 1)
     * for the given bill cycle. The LEFT JOIN + IS NULL check keeps only unmatched
     * active developers, matched on folio_no.
     */
    private static final String PENDING_DEVELOPERS_QUERY = """
            SELECT d.folio_no,
                   d.developer_name,
                   d.facility_name,
                   d.region
            FROM dbadmin.ncre_developers d
            LEFT JOIN dbadmin.ncre_invoice_create ic
                   ON ic.folio_no = d.folio_no
                  AND ic.bill_cycle = ?
                  AND ic.is_create = 1
                        WHERE d.status = '2'
                            AND d.folio_no IS NOT NULL
              AND ic.folio_no IS NULL
            ORDER BY d.folio_no
            """;

    /**
     * Close the current cycle. The WHERE clause repeats the "still current and still
     * open" conditions so the update is a safe compare-and-set: if another request
     * closed the cycle first, 0 rows are affected and the transaction is rolled back.
     */
    private static final String CLOSE_BILL_CYCLE_QUERY = """
            UPDATE dbadmin.ncre_bill_cycle
               SET is_current = 0,
                   is_closed = 1,
                   closed_by = ?,
                   closed_date = ?,
                   remarks = ?
             WHERE bill_cycle = ?
               AND is_current = 1
                             AND is_closed = 0
            """;

    /**
     * Open the next cycle. Audit columns follow the same convention as
     * ncre_invoice_create (created_by / created_date).
     */
    private static final String INSERT_BILL_CYCLE_QUERY = """
            INSERT INTO dbadmin.ncre_bill_cycle
                (bill_cycle, bill_year, bill_month, is_current, is_closed, closed_by, closed_date, remarks, created_by, created_date)
            VALUES (?, ?, ?, 1, 0, NULL, NULL, NULL, ?, ?)
            """;

    private static final String COUNT_BILL_CYCLE_QUERY = """
            SELECT COUNT(*) AS cycle_count
            FROM dbadmin.ncre_bill_cycle
            WHERE bill_cycle = ?
            """;

        /** Select valid folios first; Informix rejects INSERT ... SELECT with a bind value. */
        private static final String ACTIVE_DEVELOPER_FOLIOS_QUERY = """
            SELECT DISTINCT d.folio_no
            FROM dbadmin.ncre_developers d
            WHERE d.status = '2'
              AND d.folio_no IS NOT NULL
            """;

            private static final String EXISTING_CYCLE_FOLIOS_QUERY = """
                SELECT folio_no
                FROM dbadmin.ncre_invoice_create
                WHERE bill_cycle = ?
                """;

        private static final String INSERT_INVOICE_CREATE_QUERY = """
            INSERT INTO dbadmin.ncre_invoice_create (bill_cycle, folio_no, is_create)
            VALUES (?, ?, 0)
            """;

            /** Matches the VARCHAR(12) audit columns in dbadmin.ncre_bill_cycle. */
            private static final int MAX_CYCLE_AUDIT_EPF_LENGTH = 12;

            /** Matches dbadmin.ncre_bill_cycle.remarks VARCHAR(100). */
            private static final int MAX_CYCLE_REMARKS_LENGTH = 100;

    /** Last month of the year - the only point where the year rolls over. */
    private static final int DECEMBER = 12;

    public Optional<BillCycleDTO.CurrentBillCycleDTO> getCurrentOpenBillCycle() {
        List<BillCycleDTO.CurrentBillCycleDTO> cycles = jdbcTemplate.query(
                CURRENT_OPEN_BILL_CYCLE_QUERY,
                (resultSet, rowNumber) -> mapCurrentBillCycle(resultSet));

        if (cycles.isEmpty()) {
            return Optional.empty();
        }

        BillCycleDTO.CurrentBillCycleDTO cycle = cycles.get(0);
        NextCycle next = calculateNextCycle(cycle.getBillCycle(), cycle.getBillYear(), cycle.getBillMonth());
        cycle.setNextBillCycle(next.billCycle);
        cycle.setNextBillYear(next.billYear);
        cycle.setNextBillMonth(next.billMonth);
        return Optional.of(cycle);
    }

    private BillCycleDTO.CurrentBillCycleDTO mapCurrentBillCycle(ResultSet resultSet) throws SQLException {
        Integer isCurrent = getNullableInteger(resultSet, "is_current");
        Integer isClosed = getNullableInteger(resultSet, "is_closed");
        String cycleStatus = Integer.valueOf(1).equals(isClosed)
                ? "Closed"
                : Integer.valueOf(1).equals(isCurrent) ? "Open" : "Inactive";

        Integer billCycle = getNullableInteger(resultSet, "bill_cycle");
        Integer billYear = getNullableInteger(resultSet, "bill_year");
        Integer billMonth = getNullableInteger(resultSet, "bill_month");

        return new BillCycleDTO.CurrentBillCycleDTO(
                billCycle,
                billYear,
                billMonth,
                isCurrent,
                isClosed,
                cycleStatus,
                null,
                null,
                null);
    }

    /** Holder for the derived next bill cycle. */
    private static final class NextCycle {
        private final Integer billCycle;
        private final Integer billYear;
        private final Integer billMonth;

        private NextCycle(Integer billCycle, Integer billYear, Integer billMonth) {
            this.billCycle = billCycle;
            this.billYear = billYear;
            this.billMonth = billMonth;
        }
    }

    /**
     * Derive the next bill cycle from the current record. December rolls over to
     * January of the following year. Used both for the preview shown in the UI and for
     * the actual close, so the two can never disagree.
     */
    private NextCycle calculateNextCycle(Integer billCycle, Integer billYear, Integer billMonth) {
        if (billCycle == null || billYear == null || billMonth == null) {
            return new NextCycle(null, null, null);
        }
        if (billCycle >= Short.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No higher bill-cycle number is available in the database.");
        }
        int nextBillCycle = billCycle + 1;
        int nextMonth = billMonth == DECEMBER ? 1 : billMonth + 1;
        int nextYear = billMonth == DECEMBER ? billYear + 1 : billYear;
        return new NextCycle(nextBillCycle, nextYear, nextMonth);
    }

    private Integer getNullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }

    /**
     * Build the invoice creation summary for a bill cycle. When no cycle is supplied the
     * current open bill cycle is resolved through the existing lookup.
     */
    public BillCycleDTO.InvoiceCreationSummaryDTO getInvoiceCreationSummary(Integer billCycle) {
        return buildInvoiceCreationSummary(resolveBillCycle(billCycle));
    }

    public List<BillCycleDTO.InvoiceCreateEntryDTO> getInvoiceCreateEntries(Integer billCycle) {
        Integer targetCycle = resolveBillCycle(billCycle);
        return jdbcTemplate.query(INVOICE_CREATE_ENTRIES_QUERY, (resultSet, rowNumber) ->
                new BillCycleDTO.InvoiceCreateEntryDTO(
                        resultSet.getInt("folio_no"),
                        resultSet.getString("developer_name"),
                        getNullableInteger(resultSet, "is_create"),
                        resultSet.getString("remarks")),
                targetCycle);
    }

    @Transactional(rollbackFor = Exception.class)
    public BillCycleDTO.ActiveDeveloperReconciliationDTO reconcileActiveDevelopers(
            String sessionId, Integer billCycle) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session ID is required");
        }
        SecInfoLoginDTO.UserInfo userInfo = sessionUtils
                .getUserLocationFromSession(sessionId.trim(), null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid or expired session"));
        if (userInfo.getUserId() == null || userInfo.getUserId().trim().isEmpty()
                || userAccSecInfoRepository.findActiveUserByIdTrimmed(userInfo.getUserId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Active user account was not found for this session.");
        }
        if (!hasBillCycleEndingPermission(userInfo.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorised to reconcile the bill-cycle invoice register.");
        }

        Integer currentCycle = getCurrentOpenBillCycle()
                .map(BillCycleDTO.CurrentBillCycleDTO::getBillCycle)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "There is no current open bill cycle to reconcile."));
        Integer targetCycle = billCycle == null ? currentCycle : billCycle;
        if (!targetCycle.equals(currentCycle)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only the current open bill cycle can be reconciled.");
        }

        return new BillCycleDTO.ActiveDeveloperReconciliationDTO(
                targetCycle, insertMissingActiveFolios(targetCycle));
    }

    private List<Integer> insertMissingActiveFolios(Integer targetCycle) {
        List<Integer> activeFolios = jdbcTemplate.query(
                ACTIVE_DEVELOPER_FOLIOS_QUERY,
                (resultSet, rowNumber) -> resultSet.getInt("folio_no"));
        List<Map<String, Object>> existingFolioRows = jdbcTemplate.queryForList(
                EXISTING_CYCLE_FOLIOS_QUERY, targetCycle);
        Set<Integer> existingFolios = existingFolioRows.stream()
                .map(row -> row.values().iterator().next())
                .map(value -> (int) toLong(value))
                .collect(Collectors.toSet());
        List<Integer> missingActiveFolios = activeFolios.stream()
                .filter(folioNo -> !existingFolios.contains(folioNo))
                .distinct()
                .collect(Collectors.toList());

        if (!missingActiveFolios.isEmpty()) {
            List<Object[]> seedParameters = missingActiveFolios.stream()
                    .map(folioNo -> new Object[]{targetCycle, folioNo})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(INSERT_INVOICE_CREATE_QUERY, seedParameters);
        }
        return missingActiveFolios;
    }

    /**
     * Single source of truth for the invoice completion figures of a cycle. Both the
     * read-only summary endpoint and the End Bill Cycle validation use this, so the
     * displayed numbers and the close-time validation can never disagree.
     */
    private BillCycleDTO.InvoiceCreationSummaryDTO buildInvoiceCreationSummary(Integer targetCycle) {
        // The invoice register is the source of truth for required invoices in a cycle.
        long activeDevelopers = 0L;
        List<Map<String, Object>> activeRows = jdbcTemplate.queryForList(ACTIVE_DEVELOPERS_FOR_CYCLE_QUERY, targetCycle);
        if (!activeRows.isEmpty()) {
            activeDevelopers = toLong(activeRows.get(0).get("active_developers"));
        }

        // Created and pending are counted independently from ncre_invoice_create.
        long invoicesCreated = 0L;
        long invoicesPending = 0L;
        List<Map<String, Object>> createRows = jdbcTemplate.queryForList(INVOICE_CREATE_COUNTS_QUERY, targetCycle);
        if (!createRows.isEmpty()) {
            Map<String, Object> row = createRows.get(0);
            invoicesCreated = toLong(row.get("invoices_created"));
            invoicesPending = toLong(row.get("invoices_pending"));
        }

        // Guard the edge case of zero active developers (no division by zero)
        double progressPercent = activeDevelopers > 0
                ? (invoicesCreated * 100.0d) / activeDevelopers
                : 0.0d;

        BillCycleDTO.InvoiceCreationSummaryDTO summary = new BillCycleDTO.InvoiceCreationSummaryDTO();
        summary.setBillCycle(targetCycle);
        summary.setActiveDevelopers(activeDevelopers);
        summary.setInvoicesCreated(invoicesCreated);
        summary.setInvoicesPending(invoicesPending);
        summary.setProgressPercent(Math.round(progressPercent * 10.0d) / 10.0d);
        return summary;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Active developers that still need to create an invoice for the bill cycle.
     */
    public List<BillCycleDTO.PendingDeveloperDTO> getPendingDevelopers(Integer billCycle) {
        Integer targetCycle = resolveBillCycle(billCycle);

        return jdbcTemplate.query(
                PENDING_DEVELOPERS_QUERY,
                (resultSet, rowNumber) -> {
                    BillCycleDTO.PendingDeveloperDTO developer = new BillCycleDTO.PendingDeveloperDTO();
                    developer.setFolioNo(getTrimmedString(resultSet, "folio_no"));
                    developer.setDeveloperName(getTrimmedString(resultSet, "developer_name"));
                    developer.setFacilityName(getTrimmedString(resultSet, "facility_name"));
                    developer.setRegion(getTrimmedString(resultSet, "region"));
                    return developer;
                },
                targetCycle);
    }

    /**
     * Use the supplied bill cycle, otherwise fall back to the current open bill cycle.
     */
    private Integer resolveBillCycle(Integer billCycle) {
        if (billCycle != null) {
            return billCycle;
        }
        return getCurrentOpenBillCycle()
                .map(BillCycleDTO.CurrentBillCycleDTO::getBillCycle)
                .orElseThrow(() -> new IllegalStateException("No current open bill cycle was found"));
    }

    private String getTrimmedString(ResultSet resultSet, String columnName) throws SQLException {
        String value = resultSet.getString(columnName);
        return value == null ? "" : value.trim();
    }

    /**
     * Close the current open bill cycle and open the next one as a single atomic
     * operation.
     *
     * Both statements run inside one transaction, so a failure anywhere rolls the whole
     * thing back and the system can never end up with a closed cycle and no new current
     * cycle, or with two current cycles.
     *
     * The frontend state is never trusted: the cycle is re-read and the invoice
     * completion is re-counted inside the transaction before anything is written.
     */
    @Transactional(rollbackFor = Exception.class)
    public BillCycleDTO.EndBillCycleResultDTO endCurrentBillCycle(String sessionId) {
        return endCurrentBillCycle(sessionId, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public BillCycleDTO.EndBillCycleResultDTO endCurrentBillCycle(String sessionId, String remarks) {
        // ---- 1. Resolve and authorise the acting user from the session itself ----
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session ID is required");
        }
        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId.trim(), null);
        if (!userInfoOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }
        String actingUserId = userInfoOpt.get().getUserId();
        if (actingUserId == null || actingUserId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User information not found in session");
        }
        UserAccSecInfo actingAccount = userAccSecInfoRepository.findActiveUserByIdTrimmed(actingUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Active user account was not found for this session."));
        String actingEpf = actingAccount.getEpfNum();
        if (actingEpf == null || actingEpf.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "The logged-in user's EPF number is not available.");
        }

        // Authorization is checked against the active BM/BLMEND database grant.
        if (!hasBillCycleEndingPermission(userInfoOpt.get().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not authorised to end the bill cycle.");
        }

        // ---- 2. Re-read the current open cycle; refuse if gone or already closed ----
        Optional<BillCycleDTO.CurrentBillCycleDTO> currentCycleOpt = getCurrentOpenBillCycle();
        if (!currentCycleOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "There is no current open bill cycle to end. It may already have been closed.");
        }
        BillCycleDTO.CurrentBillCycleDTO currentCycle = currentCycleOpt.get();

        Integer currentBillCycle = currentCycle.getBillCycle();
        Integer currentBillYear = currentCycle.getBillYear();
        Integer currentBillMonth = currentCycle.getBillMonth();
        if (currentBillCycle == null || currentBillYear == null || currentBillMonth == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The current bill cycle is missing its cycle, year or month and cannot be closed.");
        }
        String cycleRemarks = normalizeCycleRemarks(remarks);

        // ---- 3. Re-validate invoice completion (never trust the frontend button) ----
        BillCycleDTO.InvoiceCreationSummaryDTO summary = buildInvoiceCreationSummary(currentBillCycle);
        if (summary.getInvoicesPending() != null && summary.getInvoicesPending() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot end bill cycle " + currentBillCycle + ": " + summary.getInvoicesPending()
                            + " active developer(s) have not created their invoice yet.");
        }

        // ---- 4. Derive the next cycle from the current record (no hardcoding) ----
        NextCycle next = calculateNextCycle(currentBillCycle, currentBillYear, currentBillMonth);
        int nextBillCycle = next.billCycle;
        int nextBillYear = next.billYear;
        int nextBillMonth = next.billMonth;

        // ---- 5. Refuse if the next cycle already exists (no duplicate / double submit) ----
        Integer existingNext = countBillCycle(nextBillCycle);
        if (existingNext != null && existingNext > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bill cycle " + nextBillCycle + " already exists. No changes were made.");
        }

        LocalDateTime closedAt = LocalDateTime.now();
        java.sql.Date cycleDate = java.sql.Date.valueOf(closedAt.toLocalDate());

        // ---- 6. Close the current cycle (guarded WHERE = optimistic re-check) ----
        int updated = jdbcTemplate.update(CLOSE_BILL_CYCLE_QUERY,
                cycleAuditEpf(actingEpf),
                cycleDate,
                cycleRemarks,
                currentBillCycle);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bill cycle " + currentBillCycle + " is no longer the current open cycle. No changes were made.");
        }

        // ---- 7. Open the next cycle ----
        jdbcTemplate.update(INSERT_BILL_CYCLE_QUERY,
                nextBillCycle,
                nextBillYear,
                nextBillMonth,
                cycleAuditEpf(actingEpf),
                cycleDate);

        // ---- 8. Seed the invoice-creation register for the new cycle ----
        // Same transaction: if this fails, everything above is rolled back and the
        // database is never left with a closed cycle and an unseeded new one.
        int seededRecords = insertMissingActiveFolios(nextBillCycle).size();
        yearTariffSetupService.processBillCycleStarted(nextBillYear, nextBillMonth);

        return new BillCycleDTO.EndBillCycleResultDTO(
                currentBillCycle,
                currentBillYear,
                currentBillMonth,
                nextBillCycle,
                nextBillYear,
                nextBillMonth,
                actingEpf,
                closedAt,
                (long) seededRecords);
    }

    /** How many rows exist for a given bill cycle number. */
    private Integer countBillCycle(Integer billCycle) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(COUNT_BILL_CYCLE_QUERY, billCycle);
        if (rows.isEmpty()) {
            return 0;
        }
        return (int) toLong(rows.get(0).values().iterator().next());
    }

    /** Keep the stored user id within the column width used across the schema. */
    private String cycleAuditEpf(String actingEpf) {
        String value = actingEpf.trim();
        if (value.length() > MAX_CYCLE_AUDIT_EPF_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The logged-in user's EPF number exceeds the bill-cycle audit field limit.");
        }
        return value;
    }

    private String normalizeCycleRemarks(String remarks) {
        if (remarks == null || remarks.trim().isEmpty()) {
            return null;
        }
        String normalized = remarks.trim();
        if (normalized.length() > MAX_CYCLE_REMARKS_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Remarks must be 100 characters or fewer.");
        }
        return normalized;
    }

    private boolean hasBillCycleEndingPermission(String userId) {
        return ncreAuthorizationService.hasAccess(userId, "BM", "BLMEND", "NCR");
    }

    /**
     * Get bill cycles based on user's access level - USING TRIMMED VERSIONS
     */
    public BillCycleDTO.BillCycleResponse getBillCyclesForUser(String sessionId, String userId) {
        try {
            // Get user info from session
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();
            List<BillCycleDTO.AreaBillCycleDTO> billCycles = new ArrayList<>();

            switch (userCategory) {
                case "Admin":
                case "Electrical Engineer":
                case "Chief Engineer":
                case "DGM":
                case "Director":
                case "DIRECTOR":
                    billCycles = getAllAreasBillCycles();
                    break;
                case "Region User":
                    billCycles = getRegionBillCycles(userInfo.getRegionCode());
                    break;
                case "Province User":
                case "Accountant Revenue":
                case "Acc Assistance":
                case "Accountant Clark":
                    billCycles = getProvinceBillCycles(userInfo.getProvinceCode());
                    break;
                case "Area User":
                    billCycles = getAreaBillCycles(userInfo.getAreaCode());
                    break;
                default:
                    return createErrorResponse("Unknown user category: " + userCategory);
            }

            return createSuccessResponse(userCategory, billCycles);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve bill cycles: " + e.getMessage());
        }
    }

    /**
     * Bill cycles for an explicit set of area codes. Areas with no active cycle are still
     * returned so the client can show them as "No Cycle".
     *
     * The active cycle is the single global one from dbadmin.ncre_bill_cycle (is_current = 1),
     * so every area in the set reports the same cycle.
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getBillCyclesForAreaCodes(List<String> areaCodes) {
        if (areaCodes == null || areaCodes.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<HsbArea> areas = areaRepository.findByAreaCodeIn(areaCodes);
            if (areas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
            boolean hasCycle = activeBillCycle.isPresent();

            Map<String, String> provinceNameMap = new HashMap<>();

            return areas.stream()
                    .map(area -> {
                        BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                        dto.setAreaCode(area.getAreaCode());
                        dto.setAreaName(area.getAreaName());
                        dto.setProvinceCode(area.getProvCode());
                        dto.setRegionCode(area.getRegion());
                        dto.setActiveBillCycle(currentCycle);
                        dto.setHasBillCycle(hasCycle);

                        String provName = provinceNameMap.computeIfAbsent(
                                area.getProvCode() != null ? area.getProvCode().trim() : "",
                                code -> locationService.getProvinceByCode(code).map(HsbProvinceDTO::getProvName).orElse(""));
                        dto.setProvinceName(provName);

                        return dto;
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get bill cycles for areas: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycles for a specific area - USING TRIMMED VERSION
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getAreaBillCycles(String areaCode) {
        List<BillCycleDTO.AreaBillCycleDTO> result = new ArrayList<>();
        
        try {
            // Get area details - USING TRIMMED VERSION
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(areaCode);
            if (!areaOpt.isPresent()) {
                return result;
            }

            HsbArea area = areaOpt.get();
            
            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            
            BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
            dto.setAreaCode(area.getAreaCode());
            dto.setAreaName(area.getAreaName());
            dto.setProvinceCode(area.getProvCode());
            dto.setRegionCode(area.getRegion());
            dto.setActiveBillCycle(activeBillCycle.orElse(DEFAULT_BILL_CYCLE));
            dto.setHasBillCycle(activeBillCycle.isPresent());
            
            // Get province name - USING TRIMMED VERSION
            locationService.getProvinceByCode(area.getProvCode())
                .ifPresent(province -> dto.setProvinceName(province.getProvName()));
            
            result.add(dto);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get area bill cycles: " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Get bill cycles for all areas in a province - USING TRIMMED VERSIONS
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getProvinceBillCycles(String provinceCode) {
        try {
            // Get all areas in the province - USING TRIMMED VERSION
            List<HsbArea> areas = areaRepository.findByProvCodeTrimmed(provinceCode);
            
            if (areas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
            boolean hasCycle = activeBillCycle.isPresent();

            // Get province name - USING TRIMMED VERSION
            String provinceName = locationService.getProvinceByCode(provinceCode)
                    .map(HsbProvinceDTO::getProvName)
                    .orElse("");

            // Build result
            return areas.stream()
                    .map(area -> {
                        BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                        dto.setAreaCode(area.getAreaCode());
                        dto.setAreaName(area.getAreaName());
                        dto.setProvinceCode(area.getProvCode());
                        dto.setProvinceName(provinceName);
                        dto.setRegionCode(area.getRegion());
                        dto.setActiveBillCycle(currentCycle);
                        dto.setHasBillCycle(hasCycle);
                        return dto;
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get province bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycles for all areas in a region - USING TRIMMED VERSIONS
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getRegionBillCycles(String regionCode) {
        try {
            // Get all areas in the region - USING TRIMMED VERSION
            List<HsbArea> areas = areaRepository.findByRegionCodeTrimmed(regionCode);
            
            if (areas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
            boolean hasCycle = activeBillCycle.isPresent();

            Map<String, String> provinceNameMap = new HashMap<>();

            return areas.stream()
                    .map(area -> {
                        BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                        dto.setAreaCode(area.getAreaCode());
                        dto.setAreaName(area.getAreaName());
                        dto.setProvinceCode(area.getProvCode());
                        dto.setRegionCode(area.getRegion());
                        dto.setActiveBillCycle(currentCycle);
                        dto.setHasBillCycle(hasCycle);

                        String provName = provinceNameMap.computeIfAbsent(
                                area.getProvCode() != null ? area.getProvCode().trim() : "",
                                code -> locationService.getProvinceByCode(code).map(HsbProvinceDTO::getProvName).orElse(""));
                        dto.setProvinceName(provName);

                        return dto;
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get region bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycles for all areas (Admin view) - USING TRIMMED VERSIONS
     */
    public List<BillCycleDTO.AreaBillCycleDTO> getAllAreasBillCycles() {
        try {
            // Get all areas - USING TRIMMED VERSION
            List<HsbArea> allAreas = areaRepository.findAllOrderByAreaCode();
            
            if (allAreas.isEmpty()) {
                return new ArrayList<>();
            }

            // Get active bill cycle from dbadmin.ncre_bill_cycle
            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
            boolean hasCycle = activeBillCycle.isPresent();

            Map<String, String> provinceNameMap = new HashMap<>();

            return allAreas.stream()
                    .map(area -> {
                        BillCycleDTO.AreaBillCycleDTO dto = new BillCycleDTO.AreaBillCycleDTO();
                        dto.setAreaCode(area.getAreaCode());
                        dto.setAreaName(area.getAreaName());
                        dto.setProvinceCode(area.getProvCode());
                        dto.setRegionCode(area.getRegion());
                        dto.setActiveBillCycle(currentCycle);
                        dto.setHasBillCycle(hasCycle);

                        String provName = provinceNameMap.computeIfAbsent(
                                area.getProvCode() != null ? area.getProvCode().trim() : "",
                                code -> locationService.getProvinceByCode(code).map(HsbProvinceDTO::getProvName).orElse(""));
                        dto.setProvinceName(provName);

                        return dto;
                    })
                    .sorted(Comparator.comparing(BillCycleDTO.AreaBillCycleDTO::getRegionCode, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(BillCycleDTO.AreaBillCycleDTO::getProvinceCode, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(BillCycleDTO.AreaBillCycleDTO::getAreaCode, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all areas bill cycles: " + e.getMessage(), e);
        }
    }

    /**
     * Get bill cycle summary for areas - USING TRIMMED VERSIONS
     */
    public List<BillCycleDTO.BillCycleSummaryDTO> getBillCycleSummary(List<String> areaCodes) {
        try {
            Map<String, HsbArea> areaMap = areaRepository.findByAreaCodeIn(areaCodes).stream()
                    .collect(Collectors.toMap(HsbArea::getAreaCode, area -> area));

            Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
            Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
            boolean hasCycle = activeBillCycle.isPresent();

            return areaCodes.stream()
                    .map(areaCode -> {
                        BillCycleDTO.BillCycleSummaryDTO summary = new BillCycleDTO.BillCycleSummaryDTO();
                        summary.setAreaCode(areaCode);
                        summary.setAreaName(areaMap.get(areaCode) != null ? areaMap.get(areaCode).getAreaName() : "");
                        summary.setTotalCycles(hasCycle ? 1 : 0);
                        summary.setActiveCycles(hasCycle ? 1 : 0);
                        summary.setInactiveCycles(0);
                        summary.setActiveBillCycle(currentCycle);

                        return summary;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to get bill cycle summary: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user has access to view bill cycles for an area - USING TRIMMED VERSIONS
     */
    public boolean hasAccessToArea(String sessionId, String userId, String targetAreaCode) {
        try {
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(targetAreaCode);
            if (!areaOpt.isPresent()) {
                return false;
            }

            HsbArea area = areaOpt.get();
            return sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);

        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods
    private Map<String, Integer> getActiveBillCyclesForAreas(List<String> areaCodes) {
        Optional<Integer> activeBillCycle = getCurrentBillCycleNumber();
        Integer currentCycle = activeBillCycle.orElse(DEFAULT_BILL_CYCLE);
        Map<String, Integer> billCycleMap = new HashMap<>();
        for (String areaCode : areaCodes) {
            billCycleMap.put(areaCode, currentCycle);
        }
        return billCycleMap;
    }

    private BillCycleDTO.BillCycleResponse createSuccessResponse(String userCategory, List<BillCycleDTO.AreaBillCycleDTO> billCycles) {
        BillCycleDTO.BillCycleResponse response = new BillCycleDTO.BillCycleResponse();
        response.setSuccess(true);
        response.setMessage("Bill cycles retrieved successfully");
        response.setUserCategory(userCategory);
        response.setBillCycles(billCycles);
        response.setTimestamp(java.time.LocalDateTime.now());
        return response;
    }

    private BillCycleDTO.BillCycleResponse createErrorResponse(String message) {
        BillCycleDTO.BillCycleResponse response = new BillCycleDTO.BillCycleResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setBillCycles(new ArrayList<>());
        response.setTimestamp(java.time.LocalDateTime.now());
        return response;
    }

    /**
     * Get all bill cycle configurations for an area - USING TRIMMED VERSION
     */
    public List<BillCycleDTO.BillCycleConfigDTO> getAllBillCyclesForArea(String areaCode) {
        try {
            List<BillCycleConfig> configs = billCycleConfigRepository.findByAreaCodeTrimmed(areaCode);
            
            return configs.stream()
                    .map(this::convertToConfigDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bill cycle configurations: " + e.getMessage(), e);
        }
    }

    /**
     * Convert BillCycleConfig entity to DTO
     */
    private BillCycleDTO.BillCycleConfigDTO convertToConfigDTO(BillCycleConfig config) {
        BillCycleDTO.BillCycleConfigDTO dto = new BillCycleDTO.BillCycleConfigDTO();
        dto.setBillCycle(config.getBillCycle());
        dto.setAreaCode(config.getAreaCode());
        dto.setUserId(config.getUserId());
        dto.setEnteredDate(config.getEnteredDate());
        dto.setCycleStat(config.getCycleStat());
        dto.setIsActive(config.getCycleStat() != null && config.getCycleStat() == 1);
        return dto;
    }

    /**
     * Check if area has active bill cycle - USING dbadmin.ncre_bill_cycle
     */
    public boolean hasActiveBillCycle(String areaCode) {
        try {
            return getCurrentBillCycleNumber().isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get active bill cycle for area - USING dbadmin.ncre_bill_cycle
     */
    public Optional<Integer> getActiveBillCycle(String areaCode) {
        try {
            return getCurrentBillCycleNumber();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}