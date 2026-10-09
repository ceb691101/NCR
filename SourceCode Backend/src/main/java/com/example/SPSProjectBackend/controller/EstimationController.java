//package com.example.SPSProjectBackend.controller;
//
//import com.example.SPSProjectBackend.model.Estimation;
//import com.example.SPSProjectBackend.service.EstimationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/estimations")
//@CrossOrigin(origins = "http://localhost:3000")
//public class EstimationController {
//
//    @Autowired
//    private EstimationService estimationService;
//
//    @GetMapping
//    public List<Estimation> getAllEstimations() {
//        return estimationService.getAllEstimations();
//    }
//
//    @GetMapping("/{appNo}")
//    public Optional<Estimation> getEstimationById(@PathVariable String appNo) {
//        return estimationService.getEstimationById(appNo);
//    }
//
//    @PostMapping
//    public Estimation createEstimation(@RequestBody Estimation estimation) {
//        return estimationService.saveEstimation(estimation);
//    }
//
//    @PutMapping
//    public Estimation updateEstimation(@RequestBody Estimation estimation) {
//        return estimationService.saveEstimation(estimation);
//    }
//
//    @DeleteMapping("/{appNo}")
//    public void deleteEstimation(@PathVariable String appNo) {
//        estimationService.deleteEstimation(appNo);
//    }
//}
//
