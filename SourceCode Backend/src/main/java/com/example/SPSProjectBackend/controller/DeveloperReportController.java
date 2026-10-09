package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.DeveloperReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/developer-reports")
// @CrossOrigin(origins = "*")  // Keep this commented out
public class DeveloperReportController {

    @Autowired
    private DeveloperReportService developerReportService;

    // ADD THIS METHOD - Handle OPTIONS requests explicitly
    @RequestMapping(value = "/preview", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        // Return empty response with OK status for preflight
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/export-csv", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsCsv() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/export-pdf", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsPdf() {
        return ResponseEntity.ok().build();
    }

    // Or use a wildcard to handle all OPTIONS requests for this controller
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleAllOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preview")
    public ResponseEntity<?> getReportPreview(@RequestParam(required = false) String tariffType,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String region,
                                              @RequestParam(required = false) String province,
                                              @RequestParam(required = false) String area) {
        try {
            List<Map<String, Object>> data = developerReportService.getReportPreviewData(tariffType, status, region, province, area);
                return ResponseEntity.ok()
                    .cacheControl(org.springframework.http.CacheControl.noStore())
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .body(data);
        } catch (Exception e) {
            System.err.println("[DeveloperReportController] Error generating preview:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to generate report preview: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/export-csv")
    public ResponseEntity<?> exportCsv(@RequestParam(required = false) String tariffType,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String region,
                                       @RequestParam(required = false) String province,
                                       @RequestParam(required = false) String area) {
        try {
            byte[] csvBytes = developerReportService.generateCsvReport(tariffType, status, region, province, area);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "developer_report.csv");
            headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
            headers.setPragma("no-cache");
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[DeveloperReportController] Error exporting CSV:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to export CSV report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<?> exportPdf(@RequestParam(required = false) String tariffType,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String region,
                                       @RequestParam(required = false) String province,
                                       @RequestParam(required = false) String area) {
        try {
            byte[] pdfBytes = developerReportService.generatePdfReport(tariffType, status, region, province, area);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "developer_report.pdf");
            headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
            headers.setPragma("no-cache");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[DeveloperReportController] Error exporting PDF:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to export PDF report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}