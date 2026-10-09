package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.TariffRatesReportService;
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
@RequestMapping("/api/tariff-rates-reports")
// @CrossOrigin(origins = "*")  // Keep this commented out
public class TariffRatesReportController {

    @Autowired
    private TariffRatesReportService tariffRatesReportService;

    // Handle OPTIONS requests explicitly
    @RequestMapping(value = "/preview", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
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

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleAllOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preview")
    public ResponseEntity<?> getReportPreview() {
        try {
            List<Map<String, Object>> data = tariffRatesReportService.getReportPreviewData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("[TariffRatesReportController] Error generating preview:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to generate report preview: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/export-csv")
    public ResponseEntity<?> exportCsv() {
        try {
            byte[] csvBytes = tariffRatesReportService.generateCsvReport();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "tariff_rates_report.csv");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[TariffRatesReportController] Error exporting CSV:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to export CSV report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<?> exportPdf() {
        try {
            byte[] pdfBytes = tariffRatesReportService.generatePdfReport();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tariff_rates_report.pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[TariffRatesReportController] Error exporting PDF:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to export PDF report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}