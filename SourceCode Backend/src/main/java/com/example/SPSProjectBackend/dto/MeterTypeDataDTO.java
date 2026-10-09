package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeterTypeDataDTO {

    @JsonProperty("mtr_type")
    private String mtrType;

    @JsonProperty("mtr_order")
    private Integer mtrOrder;

    @JsonProperty("prsnt_rdn")
    private Integer prsntRdn;

    @JsonProperty("prv_rdn")
    private Integer prvRdn;

    @JsonProperty("unts")
    private BigDecimal unts;

    @JsonProperty("rate")
    private BigDecimal rate;

    @JsonProperty("amt")
    private BigDecimal amt;

    @JsonProperty("old_prv_rdn")
    private Integer oldPrvRdn;

    @JsonProperty("old_prsnt_rdn")
    private Integer oldPrsntRdn;

    @JsonProperty("old_unts")
    private BigDecimal oldUnts;

    @JsonProperty("old_rate")
    private BigDecimal oldRate;

    @JsonProperty("old_amt")
    private BigDecimal oldAmt;

    @JsonProperty("new_prv_rdn")
    private Integer newPrvRdn;

    @JsonProperty("new_prsnt_rdn")
    private Integer newPrsntRdn;

    @JsonProperty("new_unts")
    private BigDecimal newUnts;

    @JsonProperty("new_rate")
    private BigDecimal newRate;

    @JsonProperty("new_amt")
    private BigDecimal newAmt;
}
