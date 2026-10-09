package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class MeterAmendmentRequestDTO {

    @JsonProperty("acc_nbr")
    private String accNbr;

    @JsonProperty("area_cd")
    private String areaCd;

    @JsonProperty("added_blcy")
    private String addedBlcy;

    @JsonProperty("effct_blcy")
    private String effctBlcy;

    @JsonProperty("effct_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effctDate;

    @JsonProperty("type_chg")
    private String typeChg;

    @JsonProperty("amd_type")
    private String amdType;

    @JsonProperty("mtr_seq")
    private Integer mtrSeq;

    @JsonProperty("mtrset_type")
    private Short mtrsetType;

    @JsonProperty("mtr_order")
    private Short mtrOrder;

    @JsonProperty("old_mtr_nbr")
    private String oldMtrNbr;

    @JsonProperty("new_mtr_nbr")
    private String newMtrNbr;

    @JsonProperty("old_mtr_ratio")
    private String oldMtrRatio;

    @JsonProperty("mtr_ratio")
    private String mtrRatio;

    @JsonProperty("ct_ratio")
    private String ctRatio;

    @JsonProperty("m_factor")
    private BigDecimal mFactor;

    @JsonProperty("br_code")
    private String brCode;

    @JsonProperty("rsn_code")
    private String rsnCode;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("meter_types")
    private List<MeterTypeDataDTO> meterTypes;
}
