package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YearTariffSetupService {
    private final JdbcTemplate jdbcTemplate;
    private final NcreDevTariffRateRepository rateRepository;
    private final NcreDeveloperRepository developerRepository;
    private final UserAccSecInfoRepository userRepository;
    private final SessionUtils sessionUtils;

    @Transactional
    public void processBillCycleStarted(int billYear, int billMonth) {
        if (billMonth == 1) {
            rolloverForYear(billYear);
        } else if (billMonth == 2 || billMonth == 9) {
            changeAcTariffs(billYear, billMonth);
        }
    }

    private void rolloverForYear(int targetYear) {
        Integer latestYear = jdbcTemplate.queryForObject(
                "SELECT MAX(year) FROM dbadmin.ncre_dev_year_tariff_rates", Integer.class);
        if (latestYear == null || latestYear >= targetYear) return;

        List<NcreDevTariffRate> rows = rateRepository.findAll();
        for (NcreDevTariffRate row : rows) {
            archive(row, latestYear);

            String tariffCode = normalizedTariffCode(row);
            if ("15_2019".equals(tariffCode) || "15_2022".equals(tariffCode)) {
                row.setPrvTariffRate(row.getCurTariffRate());
                row.setCurTariffRate(row.getCurTariffRate() == null ? null :
                    row.getCurTariffRate().multiply(new BigDecimal("1.03")).setScale(2, RoundingMode.DOWN));
            } else if ("TTT3".equals(tariffCode) || "TTT5".equals(tariffCode)) {
                NcreDeveloper developer = developerRepository.findByFolioNo(row.getFolioNo()).orElse(null);
                Date tierDate = currentTttTierDate(developer, targetYear);
                if (tierDate != null) {
                    row.setTariffChanged(tierDate);
                    row.setCurTariffRate(null);
                } else {
                    row.setPrvTariffRate(row.getCurTariffRate());
                    if (row.getTariffChanged() != null) {
                        LocalDate oldDate = new Date(row.getTariffChanged().getTime()).toLocalDate();
                        row.setTariffChanged(Date.valueOf(oldDate.plusYears(1)));
                    }
                }
            } else if (!"FLAT".equals(tariffCode) && !"AC".equals(tariffCode)) {
                row.setPrvTariffRate(row.getCurTariffRate());
                row.setCurTariffRate(null);
            }

            if (!"AC".equals(tariffCode) && !"TTT3".equals(tariffCode) && !"TTT5".equals(tariffCode)
                    && row.getTariffChanged() != null) {
                LocalDate oldDate = new Date(row.getTariffChanged().getTime()).toLocalDate();
                row.setTariffChanged(Date.valueOf(oldDate.plusYears(1)));
            }
            row.setYear(targetYear);
            resetWorkflow(row);
            rateRepository.save(row);
        }
    }

    private void changeAcTariffs(int billYear, int billMonth) {
        Date changeDate = Date.valueOf(LocalDate.of(billYear, billMonth, 1));
        for (NcreDevTariffRate row : rateRepository.findAll()) {
            if (!"AC".equals(normalizedTariffCode(row))) continue;

            archive(row, row.getYear() == null ? billYear : row.getYear());
            BigDecimal previousRate = row.getPrvTariffRate();
            row.setPrvTariffRate(row.getCurTariffRate());
            row.setCurTariffRate(previousRate);
            row.setTariffChanged(changeDate);
            row.setYear(billYear);
            resetWorkflow(row);
            rateRepository.save(row);
        }
    }

    private void archive(NcreDevTariffRate row, int historyYear) {
        Integer nextSequence = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(seq_no), 0) + 1 FROM dbadmin.ncre_dev_year_tariff_rates_history WHERE folio_no = ? AND year = ?",
                Integer.class, row.getFolioNo(), historyYear);
        jdbcTemplate.update("INSERT INTO dbadmin.ncre_dev_year_tariff_rates_history " +
                        "(folio_no, techno_type, tariff_code, prv_tariff_rate, cur_tariff_rate, tariff_changed, year, responsble_ee, status, ent_by, ent_dt, validate_by, validate_dt, approve_by, approve_dt, change_time_per_year, seq_no) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                row.getFolioNo(), row.getTechnoType(), row.getTariffCode(), row.getPrvTariffRate(),
                row.getCurTariffRate(), row.getTariffChanged(), historyYear, row.getResponsibleEe(), row.getStatus(),
                row.getEntBy(), row.getEntDt(), row.getValidateBy(), row.getValidateDt(), row.getApproveBy(),
                row.getApproveDt(), row.getChangeTimesPerYear(), nextSequence);
    }

    private String normalizedTariffCode(NcreDevTariffRate row) {
        return row.getTariffCode() == null ? "" : row.getTariffCode().trim().toUpperCase();
    }

    private Date currentTttTierDate(NcreDeveloper developer, int currentYear) {
        if (developer == null) return null;
        java.util.Date[] tierDates = {developer.getFirstTier(), developer.getSecondTier(), developer.getThirdTier()};
        for (java.util.Date tierDate : tierDates) {
            if (tierDate == null) continue;
            LocalDate date = new Date(tierDate.getTime()).toLocalDate();
            if (date.getYear() == currentYear) return Date.valueOf(date);
            if (date.getYear() > currentYear) return null;
        }
        return null;
    }

    private void resetWorkflow(NcreDevTariffRate row) {
        row.setStatus("0");
        row.setEntBy(null);
        row.setEntDt(null);
        row.setValidateBy(null);
        row.setValidateDt(null);
        row.setApproveBy(null);
        row.setApproveDt(null);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRows(String sessionId) {
        UserContext context = context(sessionId);
        String sql = "SELECT d.folio_no, nd.developer_name, nd.facility_name, d.techno_type, " +
                "COALESCE(td.tariff_desc, d.tariff_code) tariff_type, d.prv_tariff_rate current_tariff, " +
                "d.cur_tariff_rate next_tariff, d.tariff_changed, d.change_time_per_year, d.status, d.responsble_ee " +
                "FROM dbadmin.ncre_dev_year_tariff_rates d " +
                "LEFT JOIN dbadmin.ncre_developers nd ON d.folio_no = nd.folio_no " +
                "LEFT JOIN dbadmin.ncre_tariff_desc td ON d.tariff_code = td.tariff_code " +
                whereClause(context) + " ORDER BY d.folio_no";
        return jdbcTemplate.query(sql, new Object[0], (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("folioNo", rs.getShort("folio_no"));
            row.put("developerName", rs.getString("developer_name"));
            row.put("facilityName", rs.getString("facility_name"));
            row.put("tariffType", rs.getString("tariff_type"));
            row.put("currentTariff", rs.getBigDecimal("current_tariff"));
            row.put("nextTariff", rs.getBigDecimal("next_tariff"));
            row.put("tariffChanged", rs.getDate("tariff_changed"));
            row.put("changeTimesPerYear", rs.getString("change_time_per_year"));
            row.put("status", rs.getString("status"));
            row.put("responsibleEe", rs.getString("responsble_ee"));
            return row;
        });
    }

    @Transactional
    public void update(Short folioNo, BigDecimal nextTariff, LocalDate changed, String sessionId) {
        UserContext context = context(sessionId);
        require(context.category.equals("Electrical Engineer"));
        require(nextTariff != null && changed != null);
        int count = jdbcTemplate.update("UPDATE dbadmin.ncre_dev_year_tariff_rates SET cur_tariff_rate = ?, tariff_changed = ?, ent_by = ?, ent_dt = ? WHERE folio_no = ? AND TRIM(responsble_ee) = TRIM(?) AND TRIM(status) = '0'",
                nextTariff, Date.valueOf(changed), context.epf, Date.valueOf(LocalDate.now()), folioNo, context.userId);
        require(count == 1);
    }

    @Transactional
    public void submit(Short folioNo, String sessionId) {
        UserContext context = context(sessionId);
        require(context.category.equals("Electrical Engineer"));
        require(jdbcTemplate.update("UPDATE dbadmin.ncre_dev_year_tariff_rates SET status = '1' WHERE folio_no = ? AND TRIM(responsble_ee) = TRIM(?) AND TRIM(status) = '0'", folioNo, context.userId) == 1);
    }

    @Transactional
    public void decide(Short folioNo, boolean approved, String sessionId) {
        UserContext context = context(sessionId);
        String fromStatus;
        String toStatus;
        String auditSql;
        if (context.category.equals("Chief Engineer")) {
            fromStatus = "1"; toStatus = approved ? "4" : "0";
            auditSql = approved ? ", validate_by = ?, validate_dt = ?" : ", validate_by = NULL, validate_dt = NULL";
        } else if (context.category.equals("DGM") || context.category.equalsIgnoreCase("Director")) {
            fromStatus = "4"; toStatus = approved ? "2" : "0";
            auditSql = approved ? ", approve_by = ?, approve_dt = ?" : ", approve_by = NULL, approve_dt = NULL";
        } else throw new IllegalArgumentException("User is not authorized to decide tariff setup");

        String sql = "UPDATE dbadmin.ncre_dev_year_tariff_rates SET status = ?" + auditSql + " WHERE folio_no = ? AND TRIM(status) = ?";
        int count = approved
                ? jdbcTemplate.update(sql, toStatus, context.epf, Date.valueOf(LocalDate.now()), folioNo, fromStatus)
                : jdbcTemplate.update(sql, toStatus, folioNo, fromStatus);
        require(count == 1);
    }

    private String whereClause(UserContext context) {
        if (context.category.equals("Admin") || context.category.equals("Electrical Engineer")
            || context.category.equals("Chief Engineer") || context.category.equals("DGM")
            || context.category.equalsIgnoreCase("Director")) return "WHERE 1 = 1";
        throw new IllegalArgumentException("User is not authorized to view tariff setup");
    }

    private UserContext context(String sessionId) {
        SecInfoSessionData session = sessionUtils.getSessionData(sessionId).orElseThrow(() -> new IllegalArgumentException("Invalid session"));
        UserAccSecInfo user = userRepository.findActiveUserByIdTrimmed(session.getUserId()).orElseThrow(() -> new IllegalArgumentException("Inactive user"));
        return new UserContext(session.getUserId(), user.getEpfNum(), user.getUserCat());
    }

    private void require(boolean condition) { if (!condition) throw new IllegalArgumentException("Tariff setup operation was not allowed"); }

    private record UserContext(String userId, String epf, String category) { }
}
