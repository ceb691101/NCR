package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreRuChange;
import com.example.SPSProjectBackend.model.NcreRuChangeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NcreRuChangeRepository extends JpaRepository<NcreRuChange, NcreRuChangeId> {
}
