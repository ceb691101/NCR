// remove
//package com.example.SPSProjectBackend.service;
//
//
//import net.sf.jasperreports.engine.*;
//        import net.sf.jasperreports.export.*;
//        import net.sf.jasperreports.engine.export.JRPdfExporter;
//import org.apache.commons.io.IOUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//
//import javax.sql.DataSource;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.*;
//        import java.sql.Connection;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class CheckReport {
//
//    @Autowired
//    private DataSource dataSource;
//
//    public void generateApplicantReport(String applicationNo,String deptId, HttpServletResponse response) throws Exception {
//        String jrxmlPath = "allocation_Of_jobNumber.jrxml";
//        InputStream jrxmlInput = new ClassPathResource(jrxmlPath).getInputStream();
//
//        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlInput);
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("applicationNo", applicationNo); // 👈 Injecting the parameter
//        params.put("deptId", deptId);
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
//
//            response.setContentType("application/pdf");
//            response.setHeader("Content-Disposition", "inline; filename=applicant_report.pdf");
//            response.getOutputStream().write(pdfOutputStream.toByteArray());
//            response.getOutputStream().flush();
//        }
//    }
//
//}
