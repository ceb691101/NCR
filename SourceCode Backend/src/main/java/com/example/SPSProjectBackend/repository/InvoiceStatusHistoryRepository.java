package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.InvoiceStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface InvoiceStatusHistoryRepository extends JpaRepository<InvoiceStatusHistory, Short> {
    List<InvoiceStatusHistory> findByInvoiceIdOrderByChangedAtDesc(Short invoiceId);

    @Query("SELECT h FROM InvoiceStatusHistory h WHERE h.invoiceId IN :invoiceIds ORDER BY h.changedAt DESC")
    List<InvoiceStatusHistory> findByInvoiceIdInOrderByChangedAtDesc(@Param("invoiceIds") Collection<Short> invoiceIds);

    @Query("SELECT COALESCE(MAX(h.id), 0) FROM InvoiceStatusHistory h")
    Short findMaxId();
}
