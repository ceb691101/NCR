package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NcreDocumentRepository extends JpaRepository<NcreDocument, Long> {
    List<NcreDocument> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
