package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.dto.CostCenterJobStatusDto;
import com.example.SPSProjectBackend.model.Pcesthmt;
import com.example.SPSProjectBackend.model.PcesthmtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CostCenterJobStatusRepository extends JpaRepository<Pcesthmt, PcesthmtId> {

    @Query("SELECT new com.example.SPSProjectBackend.dto.CostCenterJobStatusDto(" +
            ":deptId, " +
            "SUM(CASE WHEN h.status = 1 THEN 1 ELSE 0 END), " +   // Open
            "SUM(CASE WHEN h.status = 5 THEN 1 ELSE 0 END), " +   // Revised
            "SUM(CASE WHEN h.status = 3 THEN 1 ELSE 0 END)) " +   // Closed
            "FROM Pcesthmt h " +
            "WHERE h.id.deptId = :deptId")
    CostCenterJobStatusDto getJobStatusCountsByDeptId(@Param("deptId") String deptId);
}
