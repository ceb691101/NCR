package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MtrsetDfn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MtrsetDfnRepository extends JpaRepository<MtrsetDfn, Long> {
    Optional<MtrsetDfn> findByMtrTypeAndCusCat(String mtrType, String cusCat);
}