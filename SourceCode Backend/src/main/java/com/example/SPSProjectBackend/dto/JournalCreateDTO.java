package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalCreateDTO {
    private String accNbr;
    private String areaCd;
    private Integer currentBillCycle;
    private String jnlType;
    private String pairedJnlType; // For A/G pairing - the paired journal type code
    private Integer jnlNo;
    private String jnlDate;
    
    // Field 1 and Field 2 amounts
    private BigDecimal field1;
    private String field1Type; // "DEBIT" or "CREDIT"
    private BigDecimal field2;
    private String field2Type; // "DEBIT" or "CREDIT"
    
    private BigDecimal totalAmt;
    private String totalAmtType; // "DEBIT" or "CREDIT"
    
    private String approvedBy;
    private String name;
    private String docAttch;
    private Boolean individuallyConfirmed;
}
