package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Addendum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddendumRepository extends JpaRepository<Addendum, Long> {
    List<Addendum> findByAgreement_AgreementId(Long agreementId);
}
