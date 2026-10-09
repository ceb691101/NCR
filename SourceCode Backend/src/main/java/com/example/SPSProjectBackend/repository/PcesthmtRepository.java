package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Pcesthmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PcesthmtRepository extends JpaRepository<Pcesthmt, String> {

    @Query("SELECT p.id.estimateNo FROM Pcesthmt p")
    List<String> findAllEstimateNumbers();

    @Query(value = "SELECT TO_CHAR(ETIMATE_DT, 'YYYY-MM-DD') AS ETIMATE_DT, " +
            "TO_CHAR(PRJ_ASS_DT, 'YYYY-MM-DD') AS PRJ_ASS_DT " +
            "FROM SPSNEW.PCESTHMT " +
            "WHERE ESTIMATE_NO = :estimateNo",
            nativeQuery = true)
    Object[] findEstimateAndProjectDates(@Param("estimateNo") String estimateNo);
}