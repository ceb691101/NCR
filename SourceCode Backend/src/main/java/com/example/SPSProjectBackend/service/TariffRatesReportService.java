package com.example.SPSProjectBackend.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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
public class TariffRatesReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String DEFAULT_SQL_QUERY =
        "SELECT \n" +
        "    dyt.folio_no, \n" +
        "    nd.developer_name, \n" +
        "    nd.facility_name,\n" +
        "    dyt.techno_type, \n" +
        "    trf.tariff_desc,\n" +
        "    dyt.prv_tariff_rate, \n" +
        "    dyt.tariff_changed, \n" +
        "    dyt.cur_tariff_rate\n" +
        "FROM ncre_dev_year_tariff_rates dyt\n" +
        "LEFT JOIN ncre_developers nd ON dyt.folio_no = nd.folio_no\n" +
        "LEFT JOIN ncre_tariff_desc trf ON dyt.tariff_code = trf.tariff_code\n" +
        "WHERE TRIM(dyt.status) = '2'\n" +
        "ORDER BY dyt.folio_no ASC";

    public List<Map<String, Object>> getReportPreviewData() {
        List<Map<String, Object>> result = fetchReportData(DEFAULT_SQL_QUERY);
        if (result.isEmpty()) {
            System.err.println("[TariffRatesReportService] No data returned for the specified query");
        }
        return result;
    }

    public List<Map<String, Object>> fetchReportData(String sqlQuery) {
        try {
            System.out.println("[TariffRatesReportService] Executing query: " + sqlQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(sqlQuery);
            System.out.println("[TariffRatesReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e1) {
            System.err.println("[TariffRatesReportService] Query failed: " + e1.getMessage());
        }

        try {
            String dbAdminQuery = prefixTables(sqlQuery, "dbadmin.");
            System.out.println("[TariffRatesReportService] Trying with dbadmin: " + dbAdminQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(dbAdminQuery);
            System.out.println("[TariffRatesReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e2) {
            System.err.println("[TariffRatesReportService] dbadmin query failed: " + e2.getMessage());
        }

        try {
            String appAdmQuery = prefixTables(sqlQuery, "appadm1.");
            System.out.println("[TariffRatesReportService] Trying with appadm1: " + appAdmQuery);
            List<Map<String, Object>> rawRows = jdbcTemplate.queryForList(appAdmQuery);
            System.out.println("[TariffRatesReportService] Query returned " + rawRows.size() + " rows");
            return formatRawRows(rawRows);
        } catch (Exception e3) {
            System.err.println("[TariffRatesReportService] appadm1 query failed: " + e3.getMessage());
        }

        System.err.println("[TariffRatesReportService] All SQL attempts failed. Returning empty list.");
        return Collections.emptyList();
    }

    private String prefixTables(String sql, String schema) {
        return sql
            .replace("ncre_dev_year_tariff_rates", schema + "ncre_dev_year_tariff_rates")
            .replace("ncre_developers", schema + "ncre_developers")
            .replace("ncre_tariff_desc", schema + "ncre_tariff_desc");
    }

    private List<Map<String, Object>> formatRawRows(List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> formattedRows = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        int seq = 1;
        for (Map<String, Object> row : rawRows) {
            Map<String, Object> formattedRow = new LinkedHashMap<>();
            
            // Add sequence number as first column
            formattedRow.put("row_no", seq++);

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

    public byte[] generateCsvReport() throws Exception {
        List<Map<String, Object>> data = getReportPreviewData();
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

    public byte[] generatePdfReport() throws Exception {
        List<Map<String, Object>> data = getReportPreviewData();
        String htmlContent = buildReportHtml(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(baos);
        builder.run();

        return addPageNumbers(baos.toByteArray());
    }

    private byte[] addPageNumbers(byte[] pdfBytes) throws Exception {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            int totalPages = doc.getNumberOfPages();
            if (totalPages == 0) {
                return pdfBytes;
            }
            for (int i = 0; i < totalPages; i++) {
                PDPage page = doc.getPage(i);
                try (PDPageContentStream cs = new PDPageContentStream(
                        doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    String text = "Page " + (i + 1) + " of " + totalPages;
                    float fontSize = 9f;
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 100f * fontSize;
                    float pageWidth = page.getMediaBox().getWidth();
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, fontSize);
                    cs.newLineAtOffset((pageWidth - textWidth) / 2f, 15f);
                    cs.showText(text);
                    cs.endText();
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
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

        switch (columnKey) {
            case "row_no":            return "No.";
            case "folio_no":          return "Folio Number";
            case "developer_name":    return "Developer Name";
            case "facility_name":     return "Project Name";
            case "techno_type":       return "Technology";
            case "tariff_desc":       return "Tariff Category";
            case "prv_tariff_rate":   return "Previous Tariff (Rs/kWh)";
            case "tariff_changed":    return "Tariff Changed On";
            case "cur_tariff_rate":   return "Current Tariff (Rs/kWh)";
            default:
                String[] words = columnKey.split("_");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (!w.isEmpty()) {
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
//-
    private String buildReportHtml(List<Map<String, Object>> data) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset=\"UTF-8\"/><style>");
        html.append("@page { size: A4 landscape; margin: 12mm 10mm; }");
        html.append("* { box-sizing: border-box; }");
        html.append("html, body { width: 100%; margin: 0; padding: 0; }");
        html.append("body { font-family: Arial, Helvetica, sans-serif; font-size: 10px; color: #1f2937; }");
        html.append("h1 { font-size: 18px; color: #111827; margin: 0 0 6px 0; border-bottom: 2px solid #334155; padding-bottom: 6px; }");
        html.append(".summary { font-size: 10px; color: #4b5563; margin: 8px 0 12px 0; }");
        html.append("table { width: 100%; border-collapse: collapse; table-layout: auto; }");
        html.append("thead { display: table-header-group; }");
        html.append("th { background-color: #334155; color: #ffffff; font-weight: bold; text-align: left; padding: 6px 8px; border: 1px solid #1f2937; }");
        html.append("td { padding: 5px 8px; border: 1px solid #cbd5e1; vertical-align: top; }");
        html.append(".avoid-break { page-break-inside: avoid; }");
        html.append("tr:nth-child(even) td { background-color: #f1f5f9; }");
        html.append("</style></head><body>");

        html.append("<h1>Tariff Rate Report</h1>");
        SimpleDateFormat stampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        html.append("<div class=\"summary\">Total Records: ").append(data.size())
            .append(" &#160;|&#160; Generated: ").append(stampFormat.format(new Date())).append("</div>");

        if (data.isEmpty()) {
            html.append("<p>No report data available.</p>");
        } else {
            List<String> headers = new ArrayList<>(data.get(0).keySet());

            html.append("<table>");

            html.append("<thead><tr>");
            for (String h : headers) {
                html.append("<th>").append(escapeHtml(formatHeaderLabel(h))).append("</th>");
            }
            html.append("</tr></thead><tbody>");

            for (Map<String, Object> row : data) {
                html.append("<tr class=\"avoid-break\">");
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