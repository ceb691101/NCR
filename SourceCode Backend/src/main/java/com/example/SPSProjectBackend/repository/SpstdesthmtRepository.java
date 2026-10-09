package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Applicant;
import com.example.SPSProjectBackend.model.ApplicationModel;
import com.example.SPSProjectBackend.model.Spstdesthmt;
import com.example.SPSProjectBackend.model.SpstdesthmtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpstdesthmtRepository extends JpaRepository<Spstdesthmt, SpstdesthmtId> {

    @Query("select a from Applicant a " +
            "join ApplicationModel ap on ap.idNo = a.idNo " +
            "join Spstdesthmt s on s.id.appNo = ap.applicationNo " +
            "where s.id.appNo = :appNo")
    Optional<Applicant> findApplicantByAppNo(@Param("appNo") String appNo);

    @Query("select s from Spstdesthmt s where s.id.appNo = :appNo")
    Optional<Spstdesthmt> findFirstByAppNo(@Param("appNo") String appNo);

    // Add this native query method to get status counts:
    @Query(value = "SELECT status, COUNT(*) AS count FROM SPS.SPSTDESTHMT GROUP BY status", nativeQuery = true)
    List<Object[]> findStatusCountsNative();
}

