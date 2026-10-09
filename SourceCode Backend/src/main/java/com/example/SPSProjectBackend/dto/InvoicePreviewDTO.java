package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the invoice preview JSON response.
 * Maps to the physical CEB/NCRE Payment Invoice layout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePreviewDTO {

    private String documentType; // "MAIN", "ESCROW", "LOYALTY"

    // Header identifiers
    private String regionSrNo;           // e.g. "R3 - 146" (region + sr_no)
    private String folioMonthLabel;      // e.g. "1081  2026-3" (folio_no + invoice year-month)
    private String folioNo;

    // Invoice references
    private String refNo;                // file_ref_no e.g. "R0014"
    private String refCode;              // reference_code e.g. "S SP1 45 303"

    // Developer / Company info
    private String companyName;          // developer_name
    private String projectName;          // facility_name

    // Invoice period
    private String invoiceMonth;         // e.g. "February 2026"

    // Plant specifications
    private String capacityMw;           // sppa_capacity_mw e.g. "1.000"
    private String allowedGenerationMw;  // commissioned_capacity_mw e.g. "1.000"

    // Reading dates
    private String presentReadingDate;   // rdng_date formatted e.g. "2026-03-01"
    private String previousReadingDate;  // prv_date formatted e.g. "2026-02-01"

    // Aggregated readings (sum across KWO+KWD+KWP)
    private String totalPresentReading;  // sum of prsnt_rdn e.g. "228,046"
    private String totalPreviousReading; // sum of prv_rdn e.g. "110,509"

    // Interval readings (R1, R2, R3)
    private String presentR1;
    private String presentR2;
    private String presentR3;
    private String previousR1;
    private String previousR2;
    private String previousR3;

    // Energy sent to grid per interval (R1, R2, R3)
    private String engSendR1;
    private String engSendR2;
    private String engSendR3;

    // Calculation results
    private String multiplyFactor;       // m_factor e.g. "1.0"
    private String energyKwh;            // total present - total previous e.g. "117,537.0"
    private String eligibleEnergyKwh;    // energy after reduction of generation losses e.g. "117,537"
    private String eligibleEnergyLabel;  // e.g. "Energy after reduction of 0% Energy losses"
    private String generationLossPercent;// e.g. "0", "1.5", "2"
    private String periodOfGeneration;   // days in invoice month e.g. "28"
    private String plantFactorPercent;   // calculated percentage e.g. "17.49"
    private String energyPurchasedKwh;   // same as energyKwh
    private String ratePerKwh;           // initial_tariff e.g. "16.92"
    private String costOfEnergy;         // energy x rate e.g. "1,988,726.04"

    // Footer
    private String printDate;            // today's date e.g. "2026-06-22"
    private String preparedByName;       // e.g. "Eng. (Mr.) R. S. Ukwatta"
    private String preparedByTitle;      // e.g. "EE (REP3)"

    // Reference
    private String accountNumber;        // for reference
    private String areaCode;
    private Integer billCycle;

    // Payment Deductions
    private java.util.List<PaymentDeductionDTO> paymentDeductions;
    private String totalPaymentDeductions;
    private String finalAmountToBePaid;
    private String ded1Perc;             // 1% Deduction amount

    // Multi-rate Tariff Charge Lines (for transition/split invoices)
    private java.util.List<TariffChargeLineDTO> tariffChargeLines;

    // Signatures
    private SignerDetailsDTO preparedBy;
    private SignerDetailsDTO chiefEngineer;
    private SignerDetailsDTO director;

    // Created and Approved Dates
    private String invoiceCreatedOn;
    private String invoiceApprovedOn;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDeductionDTO {
        private String type;
        private String percentage;
        private String deductionAmount;
        private String amountToBePaid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TariffChargeLineDTO {
        private String label;
        private String periodStart;
        private String periodEnd;
        private String dateRange;
        private Integer numberOfDays;
        private String energyKwh;
        private String ratePerKwh;
        private String costOfEnergy;
        private String rateType;
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignerDetailsDTO {
        private String userId;
        private String name;
        private String designation;
        private String date;
    }
}
