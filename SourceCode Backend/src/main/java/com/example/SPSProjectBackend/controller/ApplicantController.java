package com.example.SPSProjectBackend.controller;


import com.example.SPSProjectBackend.dto.ApplicantDTO;
import com.example.SPSProjectBackend.repository.ApplicantRepository;
import com.example.SPSProjectBackend.model.Applicant;
import com.example.SPSProjectBackend.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/applicants")
public class ApplicantController {

    @Autowired
    private ApplicantService applicantService;


    @GetMapping
    public List<ApplicantDTO> getAllApplicants() {
        return applicantService.getAllApplicants();
    }



    @GetMapping("/search")
    public ResponseEntity<?> searchApplicantByIdNo(@RequestParam String idNo) {
        System.out.println("hello");

        Optional<ApplicantDTO> applicantDTO = applicantService.getApplicantById(idNo);

        if (applicantDTO.isPresent()) {
            return ResponseEntity.ok(applicantDTO.get());
        } else {
            return ResponseEntity.status(404)
                    .body("Applicant with ID " + idNo + " not found.");
        }
    }

    @GetMapping("/test")
    public String test() {
        try {
            System.out.println("hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    // Endpoint to update an applicant's data
    @PatchMapping("/{idNo}")
    public ResponseEntity<ApplicantDTO> updateApplicant(@PathVariable String idNo, @RequestBody ApplicantDTO updatedApplicantDTO) {
        try {
            ApplicantDTO updated = applicantService.updateApplicant(idNo, updatedApplicantDTO);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // Return 404 if not found
        }
    }


    @PostMapping("/save")
    public ApplicantDTO createApplicant(@RequestBody ApplicantDTO applicantDTO) {
        return applicantService.saveApplicant(applicantDTO);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplicant(@PathVariable String id) {
        if (applicantService.getApplicantById(id).isPresent()) {
            applicantService.deleteApplicant(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}