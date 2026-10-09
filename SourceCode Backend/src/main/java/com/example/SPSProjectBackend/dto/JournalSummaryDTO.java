package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalSummaryDTO {
    private String accNbr;
    @JsonProperty("folio_no")
    private Short folioNo;
    private String jnlType;
    private Integer jnlNo;
    private BigDecimal adjustAmt;

    public JournalSummaryDTO(String accNbr, String jnlType, Integer jnlNo, BigDecimal adjustAmt) {
        this.accNbr = accNbr;
        this.jnlType = jnlType;
        this.jnlNo = jnlNo;
        this.adjustAmt = adjustAmt;
    }
}