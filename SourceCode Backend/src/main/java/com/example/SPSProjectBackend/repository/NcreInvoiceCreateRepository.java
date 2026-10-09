package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreInvoiceCreate;
import com.example.SPSProjectBackend.model.NcreInvoiceCreateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NcreInvoiceCreateRepository extends JpaRepository<NcreInvoiceCreate, NcreInvoiceCreateId> {

    List<NcreInvoiceCreate> findByBillCycle(Integer billCycle);

    Optional<NcreInvoiceCreate> findByBillCycleAndFolioNo(Integer billCycle, Integer folioNo);

    @Query("SELECT ic FROM NcreInvoiceCreate ic WHERE ic.billCycle = :billCycle AND ic.isCreate = 1")
    List<NcreInvoiceCreate> findActiveChecklistByBillCycle(@Param("billCycle") Integer billCycle);

    @Query("SELECT COUNT(DISTINCT c.folioNo) FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 1 " +
           "  AND UPPER(TRIM(d.responsibleEe)) = UPPER(TRIM(:role)) " +
           "  AND TRIM(d.status) = '2'")
    Long countCheckedFoliosByBillCycleAndRole(@Param("billCycle") Integer billCycle, @Param("role") String role);

    @Query("SELECT COUNT(DISTINCT c.folioNo) FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 1 " +
           "  AND TRIM(d.status) = '2'")
    Long countAllCheckedFoliosByBillCycle(@Param("billCycle") Integer billCycle);

    @Query("SELECT c FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 0 " +
           "  AND UPPER(TRIM(d.responsibleEe)) = UPPER(TRIM(:role)) " +
           "  AND TRIM(d.status) = '2' " +
           "ORDER BY c.folioNo ASC")
    List<NcreInvoiceCreate> findPendingChecklistRecordsByBillCycleAndRole(@Param("billCycle") Integer billCycle, @Param("role") String role);

    @Query("SELECT c FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 0 " +
           "  AND TRIM(d.status) = '2' " +
           "ORDER BY c.folioNo ASC")
    List<NcreInvoiceCreate> findAllPendingChecklistRecordsByBillCycle(@Param("billCycle") Integer billCycle);

    @Query("SELECT c FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 1 " +
           "  AND UPPER(TRIM(d.responsibleEe)) = UPPER(TRIM(:role)) " +
           "  AND TRIM(d.status) = '2' " +
           "ORDER BY c.folioNo ASC")
    List<NcreInvoiceCreate> findCheckedChecklistRecordsByBillCycleAndRole(@Param("billCycle") Integer billCycle, @Param("role") String role);

    @Query("SELECT c FROM NcreInvoiceCreate c, NcreDeveloper d " +
           "WHERE c.folioNo = d.folioNo " +
           "  AND c.billCycle = :billCycle " +
           "  AND c.isCreate = 1 " +
           "  AND TRIM(d.status) = '2' " +
           "ORDER BY c.folioNo ASC")
    List<NcreInvoiceCreate> findAllCheckedChecklistRecordsByBillCycle(@Param("billCycle") Integer billCycle);
}
