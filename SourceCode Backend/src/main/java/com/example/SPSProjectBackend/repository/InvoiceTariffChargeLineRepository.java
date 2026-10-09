package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.InvoiceTariffChargeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceTariffChargeLineRepository extends JpaRepository<InvoiceTariffChargeLine, Long> {

    List<InvoiceTariffChargeLine> findByInvoiceIdOrderByLineSequenceAsc(Long invoiceId);

    void deleteByInvoiceId(Long invoiceId);
}
