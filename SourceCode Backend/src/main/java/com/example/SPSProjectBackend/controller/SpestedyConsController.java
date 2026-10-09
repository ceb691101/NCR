//package com.example.SPSProjectBackend.controller;
//
//import com.example.SPSProjectBackend.service.SpestedyConsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//public class SpestedyConsController {
//
//    @Autowired
//    private SpestedyConsService spestedyConsService;
//
//    @GetMapping("/api/v1/estimates")
//    public List<String> getAllEstimateNumbers() {
//        return spestedyConsService.getAllEstimateNumbers();
//    }
//}