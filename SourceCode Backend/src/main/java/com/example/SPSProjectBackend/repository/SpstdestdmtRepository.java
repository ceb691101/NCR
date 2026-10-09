package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Spstdestdmt;
import com.example.SPSProjectBackend.model.SpstdestdmtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpstdestdmtRepository extends JpaRepository<Spstdestdmt, SpstdestdmtId> {
    List<Spstdestdmt> findAllByIdStdNo(String stdNo);
}