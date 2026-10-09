package com.example.SPSProjectBackend.service;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private DataSource dataSource;

    public void generateApplicantReport(String applicationNo, String deptId, HttpServletResponse response) throws Exception {
        System.out.println("deptId: " + deptId);
        System.out.println("ApplicationNo: " + applicationNo);

        // Set the JRXML file path
        File file = new File("D:\\Intern Repo\\Report\\allocation_Of_constructionJob.jrxml");
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: Report template file not found.");
            return;
//    public void generateApplicantReport(String applicationNo,HttpSession session, HttpServletResponse response) throws Exception {
//        // 🟡 Get deptId from session
//
//        String deptId = (String) session.getAttribute("deptId");
//        if (deptId== null) {
//            throw new RuntimeException("No user is logged in or session expired.");
//        }
//        System.out.println("deptId: " + deptId);
//        System.out.println("ApplicationNo: " + applicationNo);
//
////        String jrxmlPath = "JobReport.jrxml";
////        InputStream jrxmlInput = new ClassPathResource(jrxmlPath).getInputStream();
//        // Set the JRXML file path
//        File file = new File("D:\\Intern Repo\\Report\\JobReport.jrxml");
//        if (!file.exists()) {
//            throw new JRRuntimeException("File " + file.getAbsolutePath() + " not found. Compile the report first.");
        }

        // Compile the JRXML file
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        // Set parameters
        Map<String, Object> params = new HashMap<>();
        params.put("@costctr", deptId);
        params.put("@appNo", applicationNo);

        // Get database connection
        try (Connection conn = dataSource.getConnection()) {
            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

            // Export to PDF
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));
            exporter.exportReport();

            // Write PDF to response

//        Map<String, Object> params = new HashMap<>();
//        params.put("deptId", deptId);
//        params.put("applicationNo", applicationNo); // 👈 Injecting the parameter
//
//
//        try (Connection conn = dataSource.getConnection()) {
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
//
//            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
//
//            JRPdfExporter exporter = new JRPdfExporter();
//            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));
//
//            exporter.exportReport();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=applicant_report.pdf");
            response.getOutputStream().write(pdfOutputStream.toByteArray());
            response.getOutputStream().flush();
        }
    }
}