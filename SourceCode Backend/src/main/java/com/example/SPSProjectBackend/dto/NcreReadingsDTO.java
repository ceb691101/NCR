package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreReadingsDTO {
    
    @JsonProperty("acc_nbr")
    private String accNbr;

    @JsonProperty("folio_no")
    private Short folioNo;
    
    @JsonProperty("area_cd")
    private String areaCd;
    
    @JsonProperty("added_blcy")
    private String addedBlcy;
    
    @JsonProperty("mtr_type")
    private String mtrType;
    
    @JsonProperty("prv_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date prvDate;
    
    @JsonProperty("rdng_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date rdngDate;
    
    @JsonProperty("prsnt_rdn")
    private BigDecimal prsntRdn;
    
    @JsonProperty("prv_rdn")
    private BigDecimal prvRdn;
    
    @JsonProperty("mtr_nbr")
    private String mtrNbr;
    
    @JsonProperty("units")
    private BigDecimal units;
    
    @JsonProperty("mtr_stat")
    private String mtrStat;
    
    @JsonProperty("rdn_stat")
    private String rdnStat;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("entered_dtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date enteredDtime;
    
    @JsonProperty("edited_user_id")
    private String editedUserId;
    
    @JsonProperty("edited_dtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date editedDtime;

    @JsonProperty("responsible_ee")
    private String responsibleEe;

    @JsonProperty("accept_ru")
    private Short acceptRu;

    @JsonProperty("tariff_type")
    private String tariffType;

    @JsonProperty("tariff_desc")
    private String tariffDesc;
}
