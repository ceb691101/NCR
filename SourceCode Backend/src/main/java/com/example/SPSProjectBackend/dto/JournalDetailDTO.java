package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalDetailDTO {
    private Integer jnlNo;
    private String accNbr;
    @com.fasterxml.jackson.annotation.JsonProperty("folio_no")
    private Short folioNo;
    private String areaCd;
    private Integer addedBlcy;
    private String jnlType;
    private BigDecimal adjustAmt;
    private String adjustStat;
    private String authCode;
    private String docAttch;
    private LocalDateTime jnlDate;
    private String confirmed;
    private String userId;
    private LocalDateTime enteredDtime;
    private String editedUserId;
    private LocalDateTime editedDtime;

    public JournalDetailDTO(Integer jnlNo, String accNbr, String areaCd, Integer addedBlcy,
                            String jnlType, BigDecimal adjustAmt, String adjustStat,
                            String authCode, String docAttch, LocalDateTime jnlDate,
                            String confirmed, String userId, LocalDateTime enteredDtime,
                            String editedUserId, LocalDateTime editedDtime) {
        this.jnlNo = jnlNo;
        this.accNbr = accNbr;
        this.areaCd = areaCd;
        this.addedBlcy = addedBlcy;
        this.jnlType = jnlType;
        this.adjustAmt = adjustAmt;
        this.adjustStat = adjustStat;
        this.authCode = authCode;
        this.docAttch = docAttch;
        this.jnlDate = jnlDate;
        this.confirmed = confirmed;
        this.userId = userId;
        this.enteredDtime = enteredDtime;
        this.editedUserId = editedUserId;
        this.editedDtime = editedDtime;
    }
}