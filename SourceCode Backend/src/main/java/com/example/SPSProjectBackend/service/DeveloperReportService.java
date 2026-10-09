package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DeveloperReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    private static final String REPORT_SELECT =
        "SELECT ROW_NUMBER() OVER (ORDER BY d.folio_no) AS no, " +
        "d.folio_no AS folio_no, d.developer_name AS developer_name, d.file_ref_no AS file_ref_no, " +
        "d.sr_no_upto_date AS sr_no_upto_date, TRIM(t.tariff_desc) AS tariff_type, d.file_no AS file_no, " +
        "d.type AS type, d.facility_name AS facility_name, d.commissioned_capacity_mw AS commissioned_capacity_mw, " +
        "d.loi_issued AS loi_issued, d.sppa_signed AS sppa_signed, d.grid_connection_date AS grid_connection_date, " +
        "d.reference_code AS reference_code, d.region AS region, d.sr_no AS sr_no, TRIM(a.area_name) AS area, " +
        "TRIM(p.prov_name) AS province, TRIM(gs.gss_name) AS grid_substation, d.initial_tariff AS initial_tariff, " +
        "d.expiration_date AS expiration_date, d.expiration_extension_date AS expiration_extension_date, " +
        "d.sppa_capacity_mw AS sppa_capacity_mw, d.feeder_no AS feeder_no, d.commissioned_year AS commissioned_year, " +
        "d.ac_expiration_with_extension AS ac_expiration_with_extension, d.ex AS ex, d.ac AS ac, d.flat AS flat, " +
        "d.ttt AS ttt, d.first_tier AS first_tier, d.second_tier AS second_tier, d.third_tier AS third_tier, " +
        "d.new_sppa_signed AS new_sppa_signed, d.validity_start AS validity_start, d.validity_expiry AS validity_expiry, " +
        "d.initial_tariff_revised AS initial_tariff_revised, d.recommissioned_on AS recommissioned_on, " +
        "d.ep_expired AS ep_expired, d.gl_expired AS gl_expired, d.voltage_level_kv AS voltage_level_kv, " +
        "d.address_line1 AS address_line1, d.address_line2 AS address_line2, d.address_line3 AS address_line3, " +
        "d.contact_person AS contact_person, d.email AS email, d.gps_coordinates AS gps_coordinates, " +
        "d.company_group AS company_group, d.tendered_or_not AS tendered_or_not, d.reductions AS reductions, " +
        "d.agreement_type AS agreement_type, d.telephone AS telephone, " +
        "CASE WHEN TRIM(d.status) = '2' THEN 'Active' ELSE 'Inactive' END AS status " +
        "FROM ncre_developers d " +
        "LEFT JOIN provinces p ON TRIM(d.province) = TRIM(p.prov_code) " +
        "LEFT JOIN areas a ON TRIM(d.area) = TRIM(a.area_code) AND TRIM(d.province) = TRIM(a.prov_code) " +
        "LEFT JOIN ncre_tariff_desc t ON (TRIM(d.tariff_type) = TRIM(t.tariff_code) OR TRIM(d.tariff_type) = TRIM(t.tariff_desc)) " +
        "LEFT JOIN ncre_grid_substation gs ON TRIM(d.grid_substation) = TRIM(gs.gss_code) ";

    public List<Map<String, Object>> getReportPreviewData() {
        return getReportPreviewData(null, null, null, null, null);
    }

    public List<Map<String, Object>> getReportPreviewData(String tariffType, String status,
                                                            String region, String province, String area) {
        List<Object> parameters = new ArrayList<>();
        String sql = buildReportQuery(tariffType, status, region, province, area, parameters);
        List<Map<String, Object>> result = fetchReportData(sql, parameters.toArray());
        if (result.isEmpty()) {
            System.err.println("[DeveloperReportService] No data returned for the specified query");
        }
        return result;
    }

    public List<Map<String, Object>> fetchReportData(String sqlQuery) {
        return fetchReportData(sqlQuery, new Object[0]);
    }

    private List<Map<String, Object>> fetchReportData(String sqlQuery, Object... parameters) {
        try {
            System.out.println("[DeveloperReportService] Executing query: " + sqlQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sqlQuery, parameters);
            System.out.println("[DeveloperReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e1) {
            System.err.println("[DeveloperReportService] Query failed: " + e1.getMessage());
        }

        try {
            String dbAdminQuery = sqlQuery.replaceAll("(?i)FROM\\s+ncre_developers", "FROM dbadmin.ncre_developers");
            System.out.println("[DeveloperReportService] Trying with dbadmin: " + dbAdminQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(dbAdminQuery, parameters);
            System.out.println("[DeveloperReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e2) {
            System.err.println("[DeveloperReportService] dbadmin query failed: " + e2.getMessage());
        }

        try {
            String appAdmQuery = sqlQuery.replaceAll("(?i)FROM\\s+ncre_developers", "FROM appadm1.ncre_developers");
            System.out.println("[DeveloperReportService] Trying with appadm1: " + appAdmQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(appAdmQuery, parameters);
            System.out.println("[DeveloperReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e3) {
            System.err.println("[DeveloperReportService] appadm1 query failed: " + e3.getMessage());
        }

        System.err.println("[DeveloperReportService] All SQL attempts failed. Returning empty list.");
        return Collections.emptyList();
    }

    private String buildReportQuery(String tariffType, String status, String region,
                                    String province, String area, List<Object> parameters) {
        StringBuilder query = new StringBuilder(REPORT_SELECT).append(" WHERE 1 = 1 ");
        addFilter(query, parameters, "TRIM(t.tariff_desc)", tariffType);
        addFilter(query, parameters, "CASE WHEN TRIM(d.status) = '2' THEN 'Active' ELSE 'Inactive' END", status);
        addFilter(query, parameters, "TRIM(d.region)", region);
        addFilter(query, parameters, "TRIM(d.province)", province);
        addFilter(query, parameters, "TRIM(a.area_name)", area);
        query.append(" ORDER BY d.folio_no");
        return query.toString();
    }

    private void addFilter(StringBuilder query, List<Object> parameters, String column, String value) {
        if (value != null && !value.trim().isEmpty()) {
            query.append(" AND ").append(column).append(" = ?");
            parameters.add(value.trim());
        }
    }

    private List<Map<String, Object>> formatRawRows(List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> formattedRows = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Map<String, Object> row : rawRows) {
            Map<String, Object> formattedRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey().toLowerCase();
                Object val = entry.getValue();
                formattedRow.put(key, formatDateOrString(val, dateFormat));
            }
            formattedRows.add(formattedRow);
        }
        return formattedRows;
    }

    private String formatDateOrString(Object val, SimpleDateFormat dateFormat) {
        if (val == null) return "";
        if (val instanceof java.util.Date || val instanceof java.sql.Date || val instanceof java.sql.Timestamp) {
            return dateFormat.format((java.util.Date) val);
        }
        return val.toString().trim();
    }

    public byte[] generateCsvReport(String tariffType, String status, String region, String province, String area) throws Exception {
        List<Map<String, Object>> data = getReportPreviewData(tariffType, status, region, province, area);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            if (!data.isEmpty()) {
                Set<String> headers = data.get(0).keySet();
                List<String> headerList = new ArrayList<>(headers);

                for (int i = 0; i < headerList.size(); i++) {
                    writer.print(escapeCsv(formatHeaderLabel(headerList.get(i))));
                    if (i < headerList.size() - 1) writer.print(",");
                }
                writer.println();

                for (Map<String, Object> row : data) {
                    for (int i = 0; i < headerList.size(); i++) {
                        Object val = row.get(headerList.get(i));
                        writer.print(escapeCsv(val != null ? val.toString() : ""));
                        if (i < headerList.size() - 1) writer.print(",");
                    }
                    writer.println();
                }
            }
            writer.flush();
        }
        return baos.toByteArray();
    }

    public byte[] generateCsvReport() throws Exception {
        return generateCsvReport(null, null, null, null, null);
    }

    public byte[] generatePdfReport(String tariffType, String status, String region, String province, String area) throws Exception {
        List<Map<String, Object>> data = getReportPreviewData(tariffType, status, region, province, area);
        String htmlContent = buildReportHtml(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(baos);
        builder.run();

        return baos.toByteArray();
    }

    public byte[] generatePdfReport() throws Exception {
        return generatePdfReport(null, null, null, null, null);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String str = value.trim();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\"");
            return "\"" + str + "\"";
        }
        return str;
    }

    private String formatHeaderLabel(String columnKey) {
        if (columnKey == null) return "";
        String[] words = columnKey.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String buildReportHtml(List<Map<String, Object>> data) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset=\"UTF-8\"/><style>");
        html.append("@page { size: 1600mm 400mm; margin: 6mm; }");
        html.append("* { box-sizing: border-box; }");
        html.append("html, body { width: 100%; margin: 0; padding: 0; }");
        html.append("body { font-family: Arial, sans-serif; font-size: 6px; color: #334155; }");
        html.append("h1 { font-size: 14px; color: #1e293b; margin-bottom: 10px; font-weight: bold; }");
        html.append("table { width: 100%; max-width: 100%; border-collapse: collapse; margin-top: 8px; font-size: 6px; table-layout: fixed; }");
        html.append("th, td { width: auto; overflow: hidden; word-wrap: break-word; word-break: break-word; overflow-wrap: break-word; white-space: normal; }");
        html.append("th { background-color: #f1f5f9; color: #1e293b; font-weight: bold; text-align: left; padding: 2px 3px; border: 0.5px solid #cbd5e1; }");
        html.append("td { padding: 2px 3px; border: 0.5px solid #e2e8f0; }");
        html.append("tr:nth-child(even) { background-color: #f8fafc; }");
        html.append(".summary { font-size: 8px; color: #64748b; margin-bottom: 6px; }");
        html.append("</style></head><body>");

        html.append("<h1>Developer Report</h1>");
        html.append("<div class=\"summary\">Total Records: ").append(data.size()).append("</div>");

        if (data.isEmpty()) {
            html.append("<p>No report data available.</p>");
        } else {
            List<String> headers = new ArrayList<>(data.get(0).keySet());
            String colWidthPct = String.format(java.util.Locale.US, "%.4f", 100.0 / headers.size());

            html.append("<table>");

            html.append("<colgroup>");
            for (int i = 0; i < headers.size(); i++) {
                html.append("<col style=\"width: ").append(colWidthPct).append("%;\"/>");
            }
            html.append("</colgroup>");

            html.append("<thead><tr>");
            for (String h : headers) {
                html.append("<th>").append(escapeHtml(formatHeaderLabel(h))).append("</th>");
            }
            html.append("</tr></thead><tbody>");

            for (Map<String, Object> row : data) {
                html.append("<tr>");
                for (String h : headers) {
                    Object val = row.get(h);
                    html.append("<td>").append(escapeHtml(val != null ? val.toString() : "")).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody></table>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}