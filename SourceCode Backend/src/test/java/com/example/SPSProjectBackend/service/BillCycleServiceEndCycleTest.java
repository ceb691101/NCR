package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import com.example.SPSProjectBackend.service.NcreAuthorizationService;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for closing the current bill cycle and database permission checks. */
@ExtendWith(MockitoExtension.class)
public class BillCycleServiceEndCycleTest {

    private static final String SESSION_ID = "session-123";
    private static final String ACTING_USER = "380EE2";
    private static final String ACTING_EPF = "EPF380EE2";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private com.example.SPSProjectBackend.repository.BillCycleConfigRepository billCycleConfigRepository;

    @Mock
    private com.example.SPSProjectBackend.repository.HsbAreaRepository areaRepository;

    @Mock
    private HsbLocationService locationService;

    @Mock
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Mock
    private NcreAuthorizationService ncreAuthorizationService;

    @Mock
    private YearTariffSetupService yearTariffSetupService;

    @InjectMocks
    private BillCycleService billCycleService;

    private SecInfoLoginDTO.UserInfo userInfo;
    private boolean billCycleFeatureGranted;

    @BeforeEach
    void setUp() {
        billCycleFeatureGranted = true;
        userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setUserId(ACTING_USER);
        userInfo.setUserCategory("Chief Engineer");
    }

    private void givenValidSession() {
        when(sessionUtils.getUserLocationFromSession(SESSION_ID, null))
                .thenReturn(Optional.of(userInfo));
        UserAccSecInfo account = new UserAccSecInfo();
        account.setEpfNum(ACTING_EPF);
        when(userAccSecInfoRepository.findActiveUserByIdTrimmed(anyString()))
                .thenAnswer(invocation -> {
                    account.setUserId(invocation.getArgument(0));
                    return Optional.of(account);
                });
        when(ncreAuthorizationService.hasAccess(anyString(), eq("BM"), eq("BLMEND"), eq("NCR")))
                .thenAnswer(invocation -> billCycleFeatureGranted);
    }

    private void givenCurrentOpenCycle(int cycle, int year, int month) {
        when(jdbcTemplate.query(contains("FROM dbadmin.ncre_bill_cycle"), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Collections.singletonList(
                        currentCycleDto(cycle, year, month, 1, 0)));
    }

    private BillCycleDTO.CurrentBillCycleDTO currentCycleDto(int cycle, int year, int month,
                                                              int isCurrent, int isClosed) {
        String status = isClosed == 1 ? "Closed" : (isCurrent == 1 ? "Open" : "Inactive");
        return new BillCycleDTO.CurrentBillCycleDTO(cycle, year, month, isCurrent, isClosed, status,
                cycle + 1, month == 12 ? year + 1 : year, month == 12 ? 1 : month + 1);
    }

    /**
     * Stubs both data sources behind the summary: the active-developer-for-cycle count
     * and the independent created / pending counts from ncre_invoice_create.
     */
    private void givenInvoiceCounts(int cycle, long activeDevelopers, long invoicesCreated, long invoicesPending) {
        Map<String, Object> activeRow = new HashMap<>();
        activeRow.put("active_developers", activeDevelopers);
        when(jdbcTemplate.queryForList(contains("AS active_developers"), eq(cycle)))
                .thenReturn(Collections.singletonList(activeRow));

        Map<String, Object> createRow = new HashMap<>();
        createRow.put("invoices_created", invoicesCreated);
        createRow.put("invoices_pending", invoicesPending);
        when(jdbcTemplate.queryForList(contains("AS invoices_created"), eq(cycle)))
                .thenReturn(Collections.singletonList(createRow));
    }

    private void givenNextCycleAbsent() {
        Map<String, Object> row = new HashMap<>();
        row.put("cycle_count", 0L);
        when(jdbcTemplate.queryForList(contains("COUNT(*) AS cycle_count"), anyInt()))
                .thenReturn(Collections.singletonList(row));
    }

    private void givenActiveFolios(int count) {
        List<Integer> folios = new ArrayList<>();
        for (int folio = 1; folio <= count; folio++) {
            folios.add(folio);
        }
        when(jdbcTemplate.query(contains("SELECT DISTINCT d.folio_no"), any(RowMapper.class)))
                .thenReturn(folios);
        when(jdbcTemplate.batchUpdate(contains("INSERT INTO dbadmin.ncre_invoice_create"), anyList()))
                .thenReturn(new int[count]);
    }

    // ------------------------------------------------------------------
    // 1. Invoices incomplete -> the cycle must NOT be closed
    // ------------------------------------------------------------------
    @Test
    void endCycle_rejectedWhenInvoicesStillPending() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 47, 3); // 3 pending

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("3 active developer"));

        // No writes at all
        verify(jdbcTemplate, never()).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
        verify(jdbcTemplate, never()).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    // ------------------------------------------------------------------
    // 2. Invoices complete -> close current + open next in one go
    // ------------------------------------------------------------------
    @Test
    void endCycle_closesCurrentAndOpensNext() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0); // nothing pending
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(444, result.getClosedBillCycle());
        assertEquals(2025, result.getClosedBillYear());
        assertEquals(8, result.getClosedBillMonth());
        assertEquals(445, result.getNewBillCycle());
        assertEquals(2025, result.getNewBillYear());
        assertEquals(9, result.getNewBillMonth());
        assertEquals(ACTING_EPF, result.getClosedBy());
        assertNotNull(result.getClosedDate());

        verify(jdbcTemplate).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
        verify(jdbcTemplate).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
        verify(yearTariffSetupService).processBillCycleStarted(2025, 9);
    }

    @Test
    void endCycle_bindsAuditValuesUsingBillCycleColumnTypes() {
        userInfo.setUserId("USER-IDENTIFIER-123456");
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        billCycleService.endCurrentBillCycle(SESSION_ID, "Month complete");

        ArgumentCaptor<Object[]> closeParameters = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(contains("UPDATE dbadmin.ncre_bill_cycle"), closeParameters.capture());
        assertTrue(((String) closeParameters.getValue()[0]).length() <= 12);
        assertTrue(closeParameters.getValue()[1] instanceof java.sql.Date);
        assertEquals("Month complete", closeParameters.getValue()[2]);

        ArgumentCaptor<Object[]> insertParameters = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), insertParameters.capture());
        assertTrue(((String) insertParameters.getValue()[3]).length() <= 12);
        assertTrue(insertParameters.getValue()[4] instanceof java.sql.Date);
    }

    // ------------------------------------------------------------------
    // 3. Month / year transitions (requirement 10)
    // ------------------------------------------------------------------
    @Test
    void endCycle_rollsDecemberOverToJanuaryNextYear() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 12);
        givenInvoiceCounts(444, 10, 10, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(2026, result.getNewBillYear());
        assertEquals(1, result.getNewBillMonth());
    }

    @Test
    void endCycle_movesJanuaryToFebruarySameYear() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2026, 1);
        givenInvoiceCounts(444, 10, 10, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(2026, result.getNewBillYear());
        assertEquals(2, result.getNewBillMonth());
    }

    @Test
    void endCycle_movesNovemberToDecemberSameYear() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 11);
        givenInvoiceCounts(444, 10, 10, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(2025, result.getNewBillYear());
        assertEquals(12, result.getNewBillMonth());
    }

    /**
     * The next cycle number is derived from the stored cycle, never from a constant,
     * so an arbitrary stored value carries through unchanged.
     */
    @Test
    void endCycle_derivesNextCycleNumberFromStoredValue() {
        givenValidSession();
        givenCurrentOpenCycle(1207, 2025, 5);
        givenInvoiceCounts(1207, 10, 10, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(1207, result.getClosedBillCycle());
        assertEquals(1208, result.getNewBillCycle());
        assertEquals(6, result.getNewBillMonth());
    }

    @Test
    void endCycle_usesTheImmediatelyFollowingCycleNumber() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class)))
                .thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
    }

    /**
     * The preview returned to the UI must agree with what the close actually writes.
     */
    @Test
    void currentOpenCycle_exposesNextCyclePreview() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("is_current")).thenReturn(1);
        when(resultSet.getInt("is_closed")).thenReturn(0);
        when(resultSet.getInt("bill_cycle")).thenReturn(444);
        when(resultSet.getInt("bill_year")).thenReturn(2025);
        when(resultSet.getInt("bill_month")).thenReturn(12);
        when(resultSet.wasNull()).thenReturn(false);

        when(jdbcTemplate.query(contains("FROM dbadmin.ncre_bill_cycle"), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<BillCycleDTO.CurrentBillCycleDTO> mapper = invocation.getArgument(1);
                    return Collections.singletonList(mapper.mapRow(resultSet, 0));
                });

        BillCycleDTO.CurrentBillCycleDTO cycle = billCycleService.getCurrentOpenBillCycle().orElseThrow();

        assertEquals(444, cycle.getBillCycle());
        assertEquals(2025, cycle.getBillYear());
        assertEquals(12, cycle.getBillMonth());
        assertEquals("Open", cycle.getCycleStatus());
        // Same rollover the close uses, so the confirmation dialog cannot mislead
        assertEquals(445, cycle.getNextBillCycle());
        assertEquals(2026, cycle.getNextBillYear());
        assertEquals(1, cycle.getNextBillMonth());
    }

    // ------------------------------------------------------------------
    // 4. Already closed / no longer current -> rejected safely
    // ------------------------------------------------------------------
    @Test
    void endCycle_rejectedWhenNoCurrentOpenCycleExists() {
        givenValidSession();
        when(jdbcTemplate.query(contains("FROM dbadmin.ncre_bill_cycle"), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Collections.emptyList());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(409, ex.getStatusCode().value());
        verify(jdbcTemplate, never()).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    @Test
    void endCycle_rejectedWhenAnotherRequestAlreadyClosedTheCycle() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        // UPDATE matched no rows -> the cycle was closed concurrently
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(409, ex.getStatusCode().value());
        // Critically: no new cycle is inserted, so no duplicate current cycle
        verify(jdbcTemplate, never()).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    @Test
    void endCycle_rejectedWhenNextCycleAlreadyExists() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);

        Map<String, Object> row = new HashMap<>();
        row.put("cycle_count", 1L); // cycle 445 already present
        when(jdbcTemplate.queryForList(contains("COUNT(*) AS cycle_count"), anyInt()))
                .thenReturn(Collections.singletonList(row));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(409, ex.getStatusCode().value());
        verify(jdbcTemplate, never()).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
        verify(jdbcTemplate, never()).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    // ------------------------------------------------------------------
    // 6. Summary counts come from the documented data sources
    // ------------------------------------------------------------------
    @Test
    void summary_pendingComesFromDatabaseAndIsNotDerivedFromActiveMinusCreated() {
        // 10 active developers for the cycle, but the table holds 2 created and 7
        // pending records - pending must be 7 from the table, not 10 - 2 = 8.
        givenInvoiceCounts(444, 10, 2, 7);

        BillCycleDTO.InvoiceCreationSummaryDTO summary =
                billCycleService.getInvoiceCreationSummary(444);

        assertEquals(444, summary.getBillCycle());
        assertEquals(10L, summary.getActiveDevelopers());
        assertEquals(2L, summary.getInvoicesCreated());
        assertEquals(7L, summary.getInvoicesPending());
        // Progress stays derived from created / active
        assertEquals(20.0d, summary.getProgressPercent());
    }

    @Test
    void summary_handlesCycleWithNoInvoiceRecordsAtAll() {
        givenInvoiceCounts(456, 0, 0, 0);

        BillCycleDTO.InvoiceCreationSummaryDTO summary =
                billCycleService.getInvoiceCreationSummary(456);

        assertEquals(0L, summary.getActiveDevelopers());
        assertEquals(0L, summary.getInvoicesCreated());
        assertEquals(0L, summary.getInvoicesPending());
        // No division by zero when there are no active developers
        assertEquals(0.0d, summary.getProgressPercent());
    }

    @Test
    void summary_scopesEveryCountToTheRequestedCycle() {
        givenInvoiceCounts(456, 12, 9, 3);

        BillCycleDTO.InvoiceCreationSummaryDTO summary =
                billCycleService.getInvoiceCreationSummary(456);

        assertEquals(456, summary.getBillCycle());
        // Both queries must be parameterised by the cycle, never by a literal
        verify(jdbcTemplate).queryForList(contains("AS active_developers"), eq(456));
        verify(jdbcTemplate).queryForList(contains("AS invoices_created"), eq(456));

        ArgumentCaptor<String> countQueries = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(2)).queryForList(countQueries.capture(), eq(456));
        assertTrue(countQueries.getAllValues().stream().allMatch(query ->
            query.contains("dbadmin.ncre_invoice_create")
                && !query.contains("dbadmin.ncre_developers")));
    }

    /**
     * The closing guard must use the same pending count the page displays, otherwise
     * the UI could offer End Bill Cycle and the backend would reject it.
     */
    @Test
    void endCycle_usesSamePendingCountAsThePage() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 47, 3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("3"));
    }

    // ------------------------------------------------------------------
    // 7. Seeding ncre_invoice_create for the newly opened cycle
    // ------------------------------------------------------------------
    @Test
    void endCycle_seedsInvoiceCreateRegisterForTheNewCycle() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        givenActiveFolios(12);
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(12L, result.getInvoiceCreateRecordsCreated());

        ArgumentCaptor<List<Object[]>> seedParameters = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(contains("VALUES (?, ?, 0)"), seedParameters.capture());
        assertEquals(12, seedParameters.getValue().size());
        assertEquals(445, seedParameters.getValue().get(0)[0]);
        assertEquals(1, seedParameters.getValue().get(0)[1]);
    }

    /** The seed must follow the December -> January rollover, not a fixed cycle. */
    @Test
    void endCycle_seedsRegisterWithRolledOverCycleNumber() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 12);
        givenInvoiceCounts(444, 10, 10, 0);
        givenNextCycleAbsent();
        givenActiveFolios(3);
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(2026, result.getNewBillYear());
        assertEquals(1, result.getNewBillMonth());
        assertEquals(445, result.getNewBillCycle());
        verify(jdbcTemplate).batchUpdate(contains("INSERT INTO dbadmin.ncre_invoice_create"), anyList());
    }

    /**
     * The seed filters on the active status and skips developers with no folio,
     * because folio_no is part of the primary key.
     */
    @Test
    void endCycle_seedQuerySelectsOnlyActiveDevelopersWithAFolio() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 5, 5, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        billCycleService.endCurrentBillCycle(SESSION_ID);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeastOnce()).query(sql.capture(), any(RowMapper.class));
        String seedSql = sql.getAllValues().stream()
            .filter(value -> value.contains("ncre_developers"))
                .findFirst()
            .orElseThrow(() -> new AssertionError("active folio query was not executed"));

        // Every selected active folio is inserted with is_create = 0.
        verify(jdbcTemplate, never()).batchUpdate(contains("INSERT INTO dbadmin.ncre_invoice_create"), anyList());
        assertTrue(seedSql.contains("SELECT DISTINCT d.folio_no"));
        // active developers only, and folio_no must be present
        assertTrue(seedSql.contains("d.status = '2'"));
        assertTrue(seedSql.contains("d.folio_no IS NOT NULL"));
        assertTrue(seedSql.contains("FROM dbadmin.ncre_developers d"));
    }

    @Test
    void reconcileActiveDevelopers_addsOnlyFoliosMissingFromCurrentCycle() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenActiveFolios(3);
        Map<String, Object> existingFolio = new HashMap<>();
        existingFolio.put("folio_no", 1);
        when(jdbcTemplate.queryForList(contains("SELECT folio_no"), eq(444)))
                .thenReturn(Collections.singletonList(existingFolio));

        BillCycleDTO.ActiveDeveloperReconciliationDTO result =
                billCycleService.reconcileActiveDevelopers(SESSION_ID, 444);

        assertEquals(444, result.getBillCycle());
        assertEquals(java.util.Arrays.asList(2, 3), result.getAddedFolios());
        ArgumentCaptor<List<Object[]>> seedParameters = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(contains("VALUES (?, ?, 0)"), seedParameters.capture());
        assertEquals(2, seedParameters.getValue().size());
        assertTrue(seedParameters.getValue().stream().allMatch(parameters -> parameters[0].equals(444)));
    }

    /**
     * A primary-key collision on (bill_cycle, folio_no) must surface and roll the whole
     * operation back - never be swallowed.
     */
    @Test
    void endCycle_rollsBackWhenSeedingFails() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.query(contains("SELECT DISTINCT d.folio_no"), any(RowMapper.class)))
            .thenReturn(Collections.singletonList(1));
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.batchUpdate(contains("INSERT INTO dbadmin.ncre_invoice_create"), anyList()))
                .thenThrow(new DataAccessException("duplicate (bill_cycle, folio_no)") {
                });

        assertThrows(DataAccessException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));
    }

    // ------------------------------------------------------------------
    // 5. Authorization + rollback behaviour
    // ------------------------------------------------------------------
    @Test
    void endCycle_rejectedWithoutValidSession() {
        when(sessionUtils.getUserLocationFromSession(SESSION_ID, null)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(401, ex.getStatusCode().value());
        verify(jdbcTemplate, never()).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    @Test
    void endCycle_rejectedForUsersWithoutDatabasePermissionRegardlessOfCategory() {
        billCycleFeatureGranted = false;
        for (String role : new String[]{"Chief Engineer", "DGM", "Admin", "Area User", "Accountant Revenue"}) {
            userInfo.setUserCategory(role);
            givenValidSession();

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> billCycleService.endCurrentBillCycle(SESSION_ID),
                    "role " + role + " must not be able to end a bill cycle");

            assertEquals(403, ex.getStatusCode().value());
        }

        verify(jdbcTemplate, never()).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
        verify(jdbcTemplate, never()).update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    @Test
    void endCycle_allowsDatabaseGrantedUserWithoutSpecialCategory() {
        userInfo.setUserCategory(null);
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 3);
        givenInvoiceCounts(444, 5, 5, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
    }

    /** The stored category may carry padding or different casing. */
    @Test
    void endCycle_allowedForChiefEngineer() {
        userInfo.setUserCategory(" chief engineer ");
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 3);
        givenInvoiceCounts(444, 5, 5, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(4, result.getNewBillMonth());
    }

    @Test
    void endCycle_allowedForExplicitITTESTUser() {
        userInfo.setUserId("ITTEST");
        userInfo.setUserCategory("Test User");
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 3);
        givenInvoiceCounts(444, 5, 5, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);

        BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(SESSION_ID);

        assertEquals(445, result.getNewBillCycle());
        assertEquals(4, result.getNewBillMonth());
    }

    @Test
    void endCycle_rejectsITTESTWhenDatabasePermissionIsMissing() {
        userInfo.setUserId("ITTEST");
        userInfo.setUserCategory("Test User");
        billCycleFeatureGranted = false;
        givenValidSession();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));

        assertEquals(403, ex.getStatusCode().value());
        verify(jdbcTemplate, never()).update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class));
    }

    /**
     * If the INSERT fails after the UPDATE succeeded, the RuntimeException must
     * propagate so Spring marks the transaction rollback-only - the system must not
     * be left with a closed cycle and no new current cycle.
     */
    @Test
    void endCycle_propagatesFailureSoTransactionRollsBack() {
        givenValidSession();
        givenCurrentOpenCycle(444, 2025, 8);
        givenInvoiceCounts(444, 50, 50, 0);
        givenNextCycleAbsent();
        when(jdbcTemplate.update(contains("UPDATE dbadmin.ncre_bill_cycle"), any(Object[].class))).thenReturn(1);
        when(jdbcTemplate.update(contains("INSERT INTO dbadmin.ncre_bill_cycle"), any(Object[].class)))
                .thenThrow(new DataAccessException("insert failed") {
                });

        assertThrows(DataAccessException.class,
                () -> billCycleService.endCurrentBillCycle(SESSION_ID));
    }
}
