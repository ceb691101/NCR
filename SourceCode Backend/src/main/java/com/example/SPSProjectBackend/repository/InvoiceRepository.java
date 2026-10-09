package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByAccountNumberAndAreaCodeAndBillCycle(String accountNumber, String areaCode, Integer billCycle);
    List<Invoice> findByAccountNumberOrderByIssueDateDesc(String accountNumber);

    List<Invoice> findByInvoiceMonth(String invoiceMonth);
    List<Invoice> findByInvoiceMonthAndStatus(String invoiceMonth, InvoiceStatus status);
    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("SELECT DISTINCT i.invoiceMonth FROM Invoice i WHERE i.status = :status AND i.invoiceMonth IS NOT NULL ORDER BY i.invoiceMonth DESC")
    List<String> findDistinctInvoiceMonthsByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT DISTINCT i.invoiceMonth FROM Invoice i WHERE i.invoiceMonth IS NOT NULL ORDER BY i.invoiceMonth DESC")
    List<String> findDistinctInvoiceMonths();
}
