package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/appli")
    public void exportApplicantsReport(
            @RequestParam String applicationNo,
            @RequestParam String deptId,
            HttpServletResponse response) throws Exception {
        try {
            System.out.println("Received applicationNo: " + applicationNo);
            System.out.println("Received deptId: " + deptId);

            reportService.generateApplicantReport(applicationNo, deptId, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}