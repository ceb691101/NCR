package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class InvoiceChecklistDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceChecklistItemDTO {
        private Integer folioNo;
        private String accNbr;
        private String companyName;
        private String projectName;
        private String preparedBy; // REP2 or REP3 from ncre_developers.responsble_ee
        private String assignedTo; // Assigned user / role for ITTest view
        private Boolean isAdded;
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceChecklistResponseDTO {
        private Boolean success;
        private Integer billCycle;
        private String userRole;
        private Integer totalCount;
        private List<InvoiceChecklistItemDTO> invoices;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddChecklistItemRequestDTO {
        private Integer folioNo;
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddChecklistRequestDTO {
        private Integer billCycle;
        private List<AddChecklistItemRequestDTO> selectedInvoices;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationResultDTO {
        private Integer billCycle;
        private Integer totalInvoices;
        private Integer rep2Count;
        private Integer rep3Count;
        private Integer assignedCount;
        private Integer checkedCount;
        private Integer remainingCount;
        private Boolean complete;
    }
}
