package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
public class TmpAmndDTO {
    private String accNbr;
    @com.fasterxml.jackson.annotation.JsonProperty("folio_no")
    private Short folioNo;
    private String amdType;
    private String areaCd;
    private Short addedBlcy;
    private Short effctBlcy;

    // FIXED: Changed to String to avoid Jackson Date deserialization issues; parse manually in service
    private String effctDate;

    private BigDecimal nmrValue;
    private String chrValue;
    private String amauthCode;
    private String userId;
    private Timestamp enteredDtime;
    private String editedUserId;
    private Timestamp editedDtime;
    private String status;
}