package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDocumentDTO {

    private String invoiceNumber;
    private String issueDate;
    private String dueDate;
    private String billingPeriod;
    private String billCycle;
    private String accountNumber;
    private String areaCode;
    private String areaName;
    private String customerName;
    private String customerAddressLine1;
    private String customerAddressLine2;
    private String customerCity;
    private String customerPhone;
    private String customerTariff;
    private String taxNumber;
    private String meterNumber;
    private List<InvoiceLineItemDTO> lineItems;
    private InvoiceSummaryDTO summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineItemDTO {
        private String meterType;
        private String label;
        private boolean available;
        private Integer previousReading;
        private Integer presentReading;
        private Integer units;
        private String rate;
        private String amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceSummaryDTO {
        private String subtotal;
        private String maintenanceFees;
        private String taxAmount;
        private String totalAmount;
    }
}