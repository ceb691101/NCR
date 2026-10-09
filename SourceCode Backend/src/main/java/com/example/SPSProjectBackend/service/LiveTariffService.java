package com.example.SPSProjectBackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves the Live Tariff list shown under Tariff -> Live Tariff.
 *
 * Data is sourced from the same tables used by the Tariff Rate Report
 * (ncre_dev_year_tariff_rates / ncre_developers / ncre_tariff_desc) and the
 * live tariff rate is resolved centrally here based on the tariff_changed date:
 *
 *   tariff_changed < today  -> prv_tariff_rate
 *   tariff_changed >= today -> cur_tariff_rate
 */
@Service
public class LiveTariffService {

    private static final String LIVE_TARIFF_QUERY =
        "SELECT \n" +
        "    dyt.folio_no, \n" +
        "    nd.developer_name, \n" +
        "    nd.facility_name,\n" +
        "    trf.tariff_desc,\n" +
        "    dyt.tariff_changed, \n" +
        "    dyt.prv_tariff_rate, \n" +
        "    dyt.cur_tariff_rate\n" +
        "FROM ncre_dev_year_tariff_rates dyt\n" +
        "LEFT JOIN ncre_developers nd ON dyt.folio_no = nd.folio_no\n" +
        "LEFT JOIN ncre_tariff_desc trf ON dyt.tariff_code = trf.tariff_code\n" +
        "WHERE TRIM(dyt.status) = '2'\n" +
        "ORDER BY dyt.folio_no ASC";

    @Autowired
    private TariffRatesReportService tariffRatesReportService;

    public List<Map<String, Object>> getLiveTariffList() {
        // Reuses the schema-fallback and date-formatting logic from the Tariff Rate Report.
        List<Map<String, Object>> formattedRows = tariffRatesReportService.fetchReportData(LIVE_TARIFF_QUERY);
        return buildLiveTariffRows(formattedRows);
    }

    private List<Map<String, Object>> buildLiveTariffRows(List<Map<String, Object>> formattedRows) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> liveRows = new ArrayList<>();
        int seq = 1;
        for (Map<String, Object> row : formattedRows) {
            Map<String, Object> liveRow = new LinkedHashMap<>();
            liveRow.put("row_no", seq++);
            liveRow.put("folio_no", row.getOrDefault("folio_no", ""));
            liveRow.put("developer_name", row.getOrDefault("developer_name", ""));
            liveRow.put("facility_name", row.getOrDefault("facility_name", ""));
            liveRow.put("tariff_desc", row.getOrDefault("tariff_desc", ""));
            liveRow.put("live_tariff_rate", resolveLiveRate(row, today));
            liveRows.add(liveRow);
        }
        return liveRows;
    }

    private Object resolveLiveRate(Map<String, Object> row, LocalDate today) {
        LocalDate tariffChanged = parseDate(row.get("tariff_changed"));
        Object currentRate = row.get("cur_tariff_rate");
        Object previousRate = row.get("prv_tariff_rate");

        if (tariffChanged != null && tariffChanged.isBefore(today)) {
            return previousRate != null ? previousRate : "";
        }
        return currentRate != null ? currentRate : "";
    }

    /**
     * Parses date values that have been normalised to ISO-8601 (yyyy-MM-dd) strings
     * by the report formatting layer. Returns null when no usable date is present.
     */
    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        String str = String.valueOf(value).trim();
        if (str.isEmpty()) return null;
        try {
            return LocalDate.parse(str);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}