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
public class BulkCustomerDTO {
    @JsonProperty("ncre_type")
    private String ncreType;

    @JsonProperty("acc_nbr")
    private String accNbr;

    @JsonProperty("job_nbr")
    private String jobNbr;

    @JsonProperty("area_cd")
    private String areaCd;

    @JsonProperty("bill_cycle")
    private Integer billCycle;

    @JsonProperty("folio_no")
    private Short folioNo;

    @JsonProperty("facility_name")
    private String facilityName;

    @JsonProperty("cus_cat")
    private String cusCat;

    @JsonProperty("red_code")
    private String redCode;

    @JsonProperty("dly_pack")
    private String dlyPack;

    @JsonProperty("wlk_ord")
    private String wlkOrd;

    @JsonProperty("nat_sup")
    private String natSup;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address_l1")
    private String addressL1;

    @JsonProperty("address_l2")
    private String addressL2;

    @JsonProperty("city")
    private String city;

    @JsonProperty("p_code")
    private String pCode;

    @JsonProperty("tel_nbr")
    private String telNbr;

    @JsonProperty("id_nbr")
    private String idNbr;

    @JsonProperty("id_type")
    private String idType;

    @JsonProperty("issued_dt")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issuedDt;

    @JsonProperty("no_of_phases")
    private Integer noOfPhases;

    @JsonProperty("est_amnt")
    private BigDecimal estAmnt;

    @JsonProperty("espay_dt")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date espayDt;

    @JsonProperty("est_piv_nbr")
    private String estPivNbr;

    @JsonProperty("cd_prmses")
    private String cdPrmses;

    @JsonProperty("ind_type")
    private String indType;

    @JsonProperty("deposit_amt")
    private BigDecimal depositAmt;

    @JsonProperty("dep_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date depDate;

    @JsonProperty("dep_piv_nbr")
    private String depPivNbr;

    @JsonProperty("add_dep_amt")
    private BigDecimal addDepAmt;

    @JsonProperty("add_dep_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date addDepDate;

    @JsonProperty("add_dep_piv")
    private String addDepPiv;

    @JsonProperty("tot_sec_dep")
    private BigDecimal totSecDep;

    @JsonProperty("cntr_dmnd")
    private BigDecimal cntrDmnd;

    @JsonProperty("tariff")
    private String tariff;

    @JsonProperty("gst_apl")
    private String gstApl;

    @JsonProperty("tax_inv")
    private String taxInv;

    @JsonProperty("tax_num")
    private String taxNum;

    @JsonProperty("auth_letter")
    private String authLetter;

    @JsonProperty("agrmnt_no")
    private String agrmntNo;

    @JsonProperty("cnect_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date cnectDate;

    @JsonProperty("op_stat")
    private String opStat;

    @JsonProperty("ah_arh_stat")
    private String ahArhStat;

    @JsonProperty("alt_addr_stat")
    private String altAddrStat;

    @JsonProperty("adl_dpst_st")
    private String adlDpstSt;

    @JsonProperty("slf_gen_st")
    private String slfGenSt;

    @JsonProperty("supsd_acc")
    private Integer supsdAcc;

    @JsonProperty("refund_dep")
    private String refundDep;

    @JsonProperty("no_loans")
    private Integer noLoans;

    @JsonProperty("cst_st")
    private Integer cstSt;

    @JsonProperty("inst_id")
    private String instId;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("date_addt")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateAddt;

    @JsonProperty("cust_cd")
    private String custCd;

    @JsonProperty("entered_dtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date enteredDtime;

    @JsonProperty("edited_dtime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date editedDtime;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("edited_user_id")
    private String editedUserId;

    @JsonProperty("mobile_no")
    private String mobileNo;

    @JsonProperty("cust_type")
    private String custType;

    @JsonProperty("net_type")
    private String netType;

    @JsonProperty("cat_code")
    private String catCode;

    // ADD THESE TWO NEW FIELDS ↓
    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("responsible_ee")
    private String responsibleEe;

    @JsonProperty("status")
    private Integer status;
}
