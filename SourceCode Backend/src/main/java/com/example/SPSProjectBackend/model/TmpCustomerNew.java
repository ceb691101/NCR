
package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "tmp_customer")  // Renamed from tbl_tmp_customer
public class TmpCustomerNew {

    @Id
    @Column(name = "job_nbr", length = 15)
    private String jobNbr;

    @Column(name = "acc_nbr", length = 10)
    private String accNbr;

    @Column(name = "area_cd", columnDefinition = "CHAR(2)")
    private String areaCd;

    @Column(name = "bill_cycle")
    private Short billCycle;

    @Column(name = "cus_cat", columnDefinition = "CHAR(1)")
    private String cusCat;

    @Column(name = "red_code", columnDefinition = "CHAR(2)")
    private String redCode;

    @Column(name = "dly_pack", columnDefinition = "CHAR(2)")
    private String dlyPack;

    @Column(name = "wlk_ord", columnDefinition = "CHAR(6)")
    private String wlkOrd;

    @Column(name = "nat_sup", columnDefinition = "CHAR(1)")
    private String natSup;

    @Column(name = "name", length = 45, nullable = false)
    private String name;

    @Column(name = "address_l1", length = 45, nullable = false)
    private String addressL1;

    @Column(name = "address_l2", length = 30)
    private String addressL2;

    @Column(name = "city", length = 15)
    private String city;

    @Column(name = "p_code", length = 5)
    private String pCode;

    @Column(name = "tel_nbr", length = 16)
    private String telNbr;

    @Column(name = "id_nbr", length = 12)
    private String idNbr;

    @Column(name = "id_type", columnDefinition = "CHAR(1)")
    private String idType;

    @Temporal(TemporalType.DATE)
    @Column(name = "issued_dt")
    private Date issuedDt;

    @Column(name = "no_of_phases")
    private Short noOfPhases;

    @Column(name = "est_amnt", precision = 10, scale = 2)
    private BigDecimal estAmnt;

    @Temporal(TemporalType.DATE)
    @Column(name = "espay_dt")
    private Date espayDt;

    @Column(name = "est_piv_nbr", length = 10)
    private String estPivNbr;

    @Column(name = "cd_prmses", columnDefinition = "CHAR(1)")
    private String cdPrmses;

    @Column(name = "ind_type", length = 6)
    private String indType;

    @Column(name = "deposit_amt", precision = 13, scale = 2)
    private BigDecimal depositAmt;

    @Temporal(TemporalType.DATE)
    @Column(name = "dep_date")
    private Date depDate;

    @Column(name = "dep_piv_nbr", length = 10)
    private String depPivNbr;

    @Column(name = "add_dep_amt", precision = 13, scale = 2)
    private BigDecimal addDepAmt;

    @Temporal(TemporalType.DATE)
    @Column(name = "add_dep_date")
    private Date addDepDate;

    @Column(name = "add_dep_piv", length = 10)
    private String addDepPiv;

    @Column(name = "tot_sec_dep", precision = 13, scale = 2)
    private BigDecimal totSecDep;

    @Column(name = "cntr_dmnd", precision = 8, scale = 2)
    private BigDecimal cntrDmnd;

    @Column(name = "tariff", columnDefinition = "CHAR(3)", nullable = false)
    private String tariff;

    @Column(name = "gst_apl", columnDefinition = "CHAR(1)")
    private String gstApl;

    @Column(name = "tax_inv", columnDefinition = "CHAR(1)")
    private String taxInv;

    @Column(name = "tax_num", length = 20)
    private String taxNum;

    @Column(name = "auth_letter", columnDefinition = "CHAR(1)")
    private String authLetter;

    @Column(name = "agrmnt_no", length = 15)
    private String agrmntNo;

    @Temporal(TemporalType.DATE)
    @Column(name = "cnect_date")
    private Date cnectDate;

    @Column(name = "op_stat", columnDefinition = "CHAR(1)")
    private String opStat;

    @Column(name = "ah_arh_stat", columnDefinition = "CHAR(1)")
    private String ahArhStat;

    @Column(name = "alt_addr_stat", columnDefinition = "CHAR(1)")
    private String altAddrStat;

    @Column(name = "adl_dpst_st", columnDefinition = "CHAR(1)")
    private String adlDpstSt;

    @Column(name = "slf_gen_st", columnDefinition = "CHAR(1)")
    private String slfGenSt;

    @Column(name = "supsd_acc")
    private Short supsdAcc;

    @Column(name = "refund_dep", columnDefinition = "CHAR(1)")
    private String refundDep;

    @Column(name = "no_loans")
    private Short noLoans;

    @Column(name = "loan_amt", precision = 13, scale = 2)  // Renamed from loan_amount
    private BigDecimal loanAmt;

    @Column(name = "loan_type", length = 10)  // New field
    private String loanType;

    @Column(name = "ln_status", columnDefinition = "CHAR(1)")  // New field
    private String lnStatus;

    @Column(name = "cst_st")
    private Short cstSt;

    @Column(name = "inst_id", columnDefinition = "CHAR(10)")
    private String instId;

    @Column(name = "zone", columnDefinition = "CHAR(2)")
    private String zone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_addt")
    private Date dateAddt;

    @Column(name = "cust_cd", columnDefinition = "CHAR(4)")
    private String custCd;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entered_dtime")
    private Date enteredDtime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_dtime")
    private Date editedDtime;

    @Column(name = "user_id", columnDefinition = "CHAR(10)")
    private String userId;

    @Column(name = "edited_user_id", columnDefinition = "CHAR(10)")
    private String editedUserId;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "cust_type", columnDefinition = "CHAR(1)")
    private String custType;

    @Column(name = "net_type", columnDefinition = "CHAR(1)")
    private String netType;

    @Column(name = "cat_code", columnDefinition = "CHAR(4)")
    private String catCode;

    @Column(name = "cnnct_type")
    private Short cnnctType;

    @Column(name = "bess")
    private Short bess;

    @Column(name = "updt_flag", columnDefinition = "CHAR(1)")
    private String updtFlag;
}