package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DuplicateInvoiceRule implements InvoiceValidationRule {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public Optional<String> validate(InvoicePreviewDTO previewData) {
        System.out.println("[DuplicateInvoiceRule] Starting validation for account=" + previewData.getAccountNumber() + ", area=" + previewData.getAreaCode() + ", cycle=" + previewData.getBillCycle());
        if (previewData.getAccountNumber() == null || previewData.getAreaCode() == null || previewData.getBillCycle() == null) {
            System.err.println("[DuplicateInvoiceRule] ERROR: Required fields are missing");
            return Optional.of("Required invoice identifiers (Account Number, Area Code, Bill Cycle) are missing from the preview data.");
        }

        long start = System.currentTimeMillis();
        System.out.println("[DuplicateInvoiceRule] Querying invoiceRepository.findByAccountNumberAndAreaCodeAndBillCycle...");
        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByAccountNumberAndAreaCodeAndBillCycle(
                previewData.getAccountNumber().trim(),
                previewData.getAreaCode().trim(),
                previewData.getBillCycle()
        );
        System.out.println("[DuplicateInvoiceRule] Query completed in " + (System.currentTimeMillis() - start) + " ms. Invoice present = " + existingInvoiceOpt.isPresent());

        if (existingInvoiceOpt.isPresent()) {
            Invoice existing = existingInvoiceOpt.get();
            InvoiceStatus status = existing.getStatus();
            System.out.println("[DuplicateInvoiceRule] Existing invoice status is: " + status);
            if (status == InvoiceStatus.RECOMMEND || status == InvoiceStatus.APPROVE || status == InvoiceStatus.FINALIZE) {
                return Optional.of("An active invoice with status '" + getStatusDisplayName(status) 
                        + "' already exists for Account " + previewData.getAccountNumber()
                        + ", Area " + previewData.getAreaCode()
                        + ", Bill Cycle " + previewData.getBillCycle() + ". Please resolve or cancel that invoice before creating a new one.");
            }
        }

        System.out.println("[DuplicateInvoiceRule] Validation passed.");
        return Optional.empty();
    }

    private String getStatusDisplayName(InvoiceStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case RECOMMEND: return "Pending Recommendation";
            case APPROVE: return "Pending Approval";
            case FINALIZE: return "Finalized";
            case REJECTED: return "Rejected";
            default: return status.name();
        }
    }
}
