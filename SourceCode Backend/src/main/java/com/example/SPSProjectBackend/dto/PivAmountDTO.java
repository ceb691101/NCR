package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PivAmountDTO {
    private String deptId;
    private String pivNo;
    private String accountCode;
    private BigDecimal amount;
    private String addUser;
    private String updUser;
    private BigDecimal taxPercent;
    private BigDecimal sortKey;
    private LocalDateTime addDate;
    private LocalDateTime updDate;
    private BigDecimal displayAmount;
}