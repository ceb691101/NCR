
//package com.example.SPSProjectBackend.service;
//
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.export.JRPdfExporter;
//import net.sf.jasperreports.export.SimpleExporterInput;
//import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
//import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
//import org.apache.commons.io.IOUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.util.HashMap;
//
//@Service
//public class UserReportService {
//
//    @Autowired
//    private DataSource dataSource;
//
//    public void exportLoggedUserReport(HttpServletResponse response, HttpSession session) {
//        try (Connection conn = dataSource.getConnection()) {
//
//            // 🟡 Get username (email) from session
//            String userEmail = (String) session.getAttribute("email");
//            if (userEmail == null) {
//                throw new RuntimeException("No user is logged in or session expired.");
//            }
//
//            // 🟡 JRXML file location
//            File file = new File("G:\\L3\\Intern\\12th week\\Report templates jrxml files\\Blank_A4.jrxml");
//            if (!file.exists()) {
//                throw new JRRuntimeException("File " + file + " not found. Compile the report first.");
//            }
//
//            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
//
//            // 🟡 Set email as parameter for report
//            HashMap<String, Object> params = new HashMap<>();
//            params.put("USERNAME", userEmail);  // Must match your JRXML parameter name
//
//            // 🟡 Fill report with user-specific data
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
//
//            String outputFileName = "User_Report_" + userEmail + ".pdf";
//            File pdfFile = new File("G:\\L3\\Intern\\12th week\\Reports", outputFileName);
//
//            // 🟡 Export to PDF
//            JRPdfExporter exporter = new JRPdfExporter();
//            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfFile));
//
//            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
//            exporter.setConfiguration(configuration);
//            exporter.exportReport();
//
//            // 🟡 Stream the PDF for download
//            if (pdfFile.exists()) {
//                try (InputStream inputStream = new FileInputStream(pdfFile)) {
//                    response.setContentType("application/pdf");
//                    response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
//                    IOUtils.copy(inputStream, response.getOutputStream());
//                    response.flushBuffer();
//                    System.out.println("Report downloaded for: " + userEmail);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to generate user report: " + e.getMessage());
//        }
//    }
//}

package com.example.SPSProjectBackend.service;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

@Service
public class UserReportService {

    @Autowired
    private DataSource dataSource;

    public void exportLoggedUserReport(HttpServletResponse response, HttpSession session) {
        try (Connection conn = dataSource.getConnection()) {

            // 🟡 Get username (email) from session
            String userEmail = (String) session.getAttribute("username");
            if (userEmail == null) {
                throw new RuntimeException("No user is logged in or session expired.");
            }

            // 🟡 JRXML file location
            File file = new File("G:\\L3\\Intern\\12th week\\Report templates jrxml files\\Blank_A4.jrxml");
            if (!file.exists()) {
                throw new JRRuntimeException("File " + file + " not found. Compile the report first.");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            // 🟡 Set email as parameter for report
            HashMap<String, Object> params = new HashMap<>();
            params.put("USERNAME", userEmail);  // Must match your JRXML parameter name

            // 🟡 Fill report with user-specific data
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

            String outputFileName = "User_Report_" + userEmail + ".pdf";
            File pdfFile = new File("G:\\L3\\Intern\\12th week\\Reports", outputFileName);

            // 🟡 Export to PDF
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfFile));

            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            // 🟡 Stream the PDF for download
            if (pdfFile.exists()) {
                try (InputStream inputStream = new FileInputStream(pdfFile)) {
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
                    IOUtils.copy(inputStream, response.getOutputStream());
                    response.flushBuffer();
                    System.out.println("Report downloaded for: " + userEmail);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate user report: " + e.getMessage());
        }
    }
}

