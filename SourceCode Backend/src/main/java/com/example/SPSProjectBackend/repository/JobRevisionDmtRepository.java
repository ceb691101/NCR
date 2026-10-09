package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Pcestdmt;
import com.example.SPSProjectBackend.model.PcestdmtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRevisionDmtRepository extends JpaRepository<Pcestdmt, PcestdmtId> {
    @Query("SELECT d FROM Pcestdmt d WHERE d.pcesthmt.projectNo = :projectNo")
    List<Pcestdmt> findAllByProjectNo(@Param("projectNo") String projectNo);
}

