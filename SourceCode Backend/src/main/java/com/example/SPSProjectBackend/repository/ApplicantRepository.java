package com.example.SPSProjectBackend.repository;


import com.example.SPSProjectBackend.model.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, String> {
    Optional<Applicant> findByIdNo(String idNo);
}