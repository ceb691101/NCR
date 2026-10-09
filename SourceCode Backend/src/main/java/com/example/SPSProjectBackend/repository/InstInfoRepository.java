package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.InstInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstInfoRepository extends JpaRepository<InstInfo, String> {

    // Native query to fix potential JPQL/Informix string ID matching issues
    @Query(value = "SELECT * FROM inst_info WHERE inst_id = :instId", nativeQuery = true)
    Optional<InstInfo> findByInstIdNative(@Param("instId") String instId);
}