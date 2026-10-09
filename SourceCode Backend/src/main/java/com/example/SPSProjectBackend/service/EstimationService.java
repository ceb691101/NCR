//package com.example.SPSProjectBackend.service;
//
//import com.example.SPSProjectBackend.model.Estimation;
//import com.example.SPSProjectBackend.repository.EstimationRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class EstimationService {
//
//    @Autowired
//    private EstimationRepository estimationRepository;
//
//    public List<Estimation> getAllEstimations() {
//        return estimationRepository.findAll();
//    }
//
//    public Optional<Estimation> getEstimationById(String appNo) {
//        return estimationRepository.findById(appNo);
//    }
//
//    public Estimation saveEstimation(Estimation estimation) {
//        return estimationRepository.save(estimation);
//    }
//
//    public void deleteEstimation(String appNo) {
//        estimationRepository.deleteById(appNo);
//    }
//}
