package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
public class HistAmndDTO {
    private String accNbr;
    private String amdType;
    private String addedBlcy;      // char(3) - e.g., "182"
    private String effctBlcy;      // char(3) - padded like "182"
    private String effctDate;      // yyyy-MM-dd (string for JSON)

    private BigDecimal oldNmrValue;
    private BigDecimal newNmrValue;
    private String oldChrValue;
    private String newChrValue;

    private String amauthCode;
    private String userId;
    private Timestamp enteredDtime;
    private String editedUserId;
    private Timestamp editedDtime;
}