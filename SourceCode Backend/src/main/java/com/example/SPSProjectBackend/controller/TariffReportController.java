package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.TariffReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/tariff-reports")
public class TariffReportController {

    @Autowired
    private TariffReportService tariffReportService;

    /**
     * Export all active tariffs to PDF
     * GET /api/tariff-reports/export-pdf
     */
    @GetMapping("/export-pdf")
    public void exportTariffPDF(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== Export Tariff PDF Endpoint Called ===");
        System.out.println("Request Origin: " + request.getHeader("Origin"));
        System.out.println("Request Method: " + request.getMethod());

        try {
            // Generate PDF FIRST (before setting response headers)
            // This way if there's an error, we can still send an error response
            byte[] pdfBytes = tariffReportService.generateTariffPDFBytes();
            System.out.println("PDF generated successfully, size: " + pdfBytes.length + " bytes");

            // Add CORS headers
            String origin = request.getHeader("Origin");
            if (origin != null && (origin.equals("http://localhost:3000") ||
                                   origin.equals("http://localhost:8095") ||
                                   origin.equals("http://127.0.0.1:3000") ||
                                   origin.equals("http://127.0.0.1:8095"))) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Content-Type, Content-Length");
            }

            // Set response headers for PDF download
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=tariff_setup.pdf");
            response.setContentLength(pdfBytes.length);

            // Write PDF to response
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
            System.out.println("=== PDF Export Successful ===");

        } catch (Exception e) {
            System.err.println("=== ERROR: PDF Export Failed ===");
            System.err.println("Error class: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();

            // Only try to send error response if response not committed
            if (!response.isCommitted()) {
                try {
                    // Add CORS headers to error response
                    String origin = request.getHeader("Origin");
                    if (origin != null && (origin.equals("http://localhost:3000") ||
                                           origin.equals("http://localhost:8095") ||
                                           origin.equals("http://127.0.0.1:3000") ||
                                           origin.equals("http://127.0.0.1:8095"))) {
                        response.setHeader("Access-Control-Allow-Origin", origin);
                        response.setHeader("Access-Control-Allow-Credentials", "true");
                    }

                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("application/json");
                    String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                    response.getWriter().write("{\"error\": \"Failed to generate PDF: " + errorMsg + "\"}");
                    response.getWriter().flush();
                } catch (Exception writeError) {
                    System.err.println("Failed to write error response:");
                    writeError.printStackTrace();
                }
            } else {
                System.err.println("Response already committed, cannot send error response");
            }
        }
    }

    /**
     * Handle OPTIONS preflight request for CORS
     */
    @RequestMapping(value = "/export-pdf", method = RequestMethod.OPTIONS)
    public void handlePreflight(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== OPTIONS Preflight Request Received ===");
        String origin = request.getHeader("Origin");
        System.out.println("DEBUG OPTIONS: Origin = " + origin);

        if (origin != null && (origin.equals("http://localhost:3000") ||
                               origin.equals("http://localhost:8095") ||
                               origin.equals("http://127.0.0.1:3000"))) {
            System.out.println("DEBUG OPTIONS: Setting CORS headers for origin: " + origin);
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Max-Age", "3600");
            System.out.println("DEBUG OPTIONS: CORS headers set successfully");
        } else {
            System.out.println("DEBUG OPTIONS: Origin not matched or null");
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public String test() {
        return "Tariff Report API is working";
    }
}
