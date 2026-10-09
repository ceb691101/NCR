package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ApplicationDTO;
import com.example.SPSProjectBackend.dto.ApplicationTypeDto;
import com.example.SPSProjectBackend.dto.CostCenterJobStatusDto;
import com.example.SPSProjectBackend.model.ApplicationModel;
import com.example.SPSProjectBackend.service.ApplicationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000" , allowCredentials = "true")
@RequestMapping("/api/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/api/v1/application-details")
    public List<Object[]> getApplicationDetails(@RequestParam("applicationNo") String applicationNo) {
        return applicationService.getApplicationDetailsByApplicationNo(applicationNo);
    }

    @GetMapping("/all")
    public List<String> getAllApplicationNos() {
        return applicationService.getAllApplicationNos();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateApplicationNo(@RequestParam String number) {
        boolean exists = applicationService.validateApplicationNo(number);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/save")
    public ResponseEntity<ApplicationModel> saveApplication(@RequestBody ApplicationDTO applicationDTO, HttpSession session) {

        // Retrieve user details from the session
        String sessionUsername = (String) session.getAttribute("email");
        System.out.println("Session username: " + sessionUsername);
        if (sessionUsername == null) {
            return ResponseEntity.status(401).build(); // Unauthorized if session expired
        }

        ApplicationModel savedApplication = applicationService.saveApplication(applicationDTO, sessionUsername);
        return ResponseEntity.ok(savedApplication);
    }

//    @PutMapping("/update")
//    public ResponseEntity<?> updateApplication(
//            @RequestParam String applicationId,
//            @RequestBody ApplicationDTO applicationDTO,
//            HttpSession session) {
//
//        String sessionUsername = (String) session.getAttribute("username");
//        if (sessionUsername == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
//        }
//
//        try {
//            ApplicationModel updatedApplication = applicationService.updateApplication(applicationId, applicationDTO, sessionUsername);
//            return ResponseEntity.ok(updatedApplication);
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateApplication(
            @RequestParam String applicationId,
            @RequestBody ApplicationDTO applicationDTO,
            HttpSession session) {

        String sessionUsername = (String) session.getAttribute("email");
        System.out.println("Session username: " + sessionUsername);
        if (sessionUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in.");
        }

        try {
            ApplicationModel updatedApplication = applicationService.updateApplication(applicationId, applicationDTO, sessionUsername);
            return ResponseEntity.ok(updatedApplication);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> getApplicationById(@RequestParam String applicationId) {
        Optional<ApplicationModel> application = applicationService.getApplicationById(applicationId);
        return application.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{deptId}")
    public List<Map<String, Object>> getStatusSummaryByDept(@PathVariable String deptId) {
        return applicationService.getStatusCounts(deptId);
    }
}