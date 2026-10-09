package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    List<Agreement> findByDeveloper_FolioNo(Short folioNo);
    List<Agreement> findByFolioNoOrderByAgreementIdDesc(Short folioNo);
}
