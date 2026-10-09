//package com.example.SPSProjectBackend.controller;
//
//
//import com.example.SPSProjectBackend.service.CheckReport;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//        import jakarta.servlet.http.HttpServletResponse;
//
//@RestController
//@RequestMapping("/report")
//public class CheckReportController {
//
//
//    @Autowired
//    private CheckReport checkReport;
//
////    @GetMapping("/applicants")
////    public void exportApplicantsReport(HttpServletResponse response) throws Exception {
////        reportService.generateApplicantReport(response);
////    }
//
//    @GetMapping("/appli")
//    public void exportApplicantsReport(
//            @RequestParam String applicationNo,
//            @RequestParam String deptId,
//            HttpServletResponse response) throws Exception {
//        checkReport.generateApplicantReport(applicationNo, deptId, response);
//    }
//}