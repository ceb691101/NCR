package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MtrReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MtrReasonRepository extends JpaRepository<MtrReason, String> {

    @Query("SELECT r FROM MtrReason r WHERE (:status IS NULL OR TRIM(r.rsnStatus) = :status) ORDER BY r.rsnCode")
    List<MtrReason> findByStatus(@Param("status") String status);
}
