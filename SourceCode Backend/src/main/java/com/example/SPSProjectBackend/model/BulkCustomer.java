package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

import org.checkerframework.checker.units.qual.C;

@Entity
@Table(name = "customer", indexes = {
        @Index(name = "idx_acc_nbr", columnList = "acc_nbr"),
        @Index(name = "idx_area_cd", columnList = "area_cd"),
        @Index(name = "idx_job_nbr", columnList = "job_nbr"),
        @Index(name = "idx_name", columnList = "name"),
        @Index(name = "idx_tariff", columnList = "tariff"),
        @Index(name = "idx_zone_area", columnList = "zone, area_cd")
}, schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCustomer {

    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Column(name = "job_nbr", length = 15)
    private String jobNbr;

    @Column(name = "area_cd", length = 2)
    private String areaCd;

    @Column(name = "bill_cycle")
    private Integer billCycle;

    @Column(name = "folio_no")
    private Short folioNo;

    @Column(name = "cus_cat", length = 1)
    private String cusCat;

    @Column(name = "red_code", length = 2)
    private String redCode;

    @Column(name = "dly_pack", length = 2)
    private String dlyPack;

    @Column(name = "wlk_ord", length = 6)
    private String wlkOrd;

    @Column(name = "nat_sup", length = 1)
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

    @Column(name = "id_type", length = 1)
    private String idType;

    @Column(name = "issued_dt")
    @Temporal(TemporalType.DATE)
    private Date issuedDt;

    @Column(name = "no_of_phases")
    private Integer noOfPhases;

    @Column(name = "est_amnt", precision = 10, scale = 2)
    private BigDecimal estAmnt;

    @Column(name = "espay_dt")
    @Temporal(TemporalType.DATE)
    private Date espayDt;

    @Column(name = "est_piv_nbr", length = 10)
    private String estPivNbr;

    @Column(name = "cd_prmses", length = 1)
    private String cdPrmses;

    @Column(name = "ind_type", length = 6)
    private String indType;

    @Column(name = "deposit_amt", precision = 13, scale = 2)
    private BigDecimal depositAmt;

    @Column(name = "dep_date")
    @Temporal(TemporalType.DATE)
    private Date depDate;

    @Column(name = "dep_piv_nbr", length = 10)
    private String depPivNbr;

    @Column(name = "add_dep_amt", precision = 13, scale = 2)
    private BigDecimal addDepAmt;

    @Column(name = "add_dep_date")
    @Temporal(TemporalType.DATE)
    private Date addDepDate;

    @Column(name = "add_dep_piv", length = 10)
    private String addDepPiv;

    @Column(name = "tot_sec_dep", precision = 13, scale = 2)
    private BigDecimal totSecDep;

    @Column(name = "cntr_dmnd", precision = 8, scale = 2)
    private BigDecimal cntrDmnd;

    @Column(name = "tariff", length = 3, nullable = false)
    private String tariff;

    @Column(name = "gst_apl", length = 1)
    private String gstApl;

    @Column(name = "tax_inv", length = 1)
    private String taxInv;

    @Column(name = "tax_num", length = 20)
    private String taxNum;

    @Column(name = "auth_letter", length = 1)
    private String authLetter;

    @Column(name = "agrmnt_no", length = 15)
    private String agrmntNo;

    @Column(name = "cnect_date")
    @Temporal(TemporalType.DATE)
    private Date cnectDate;

    @Column(name = "op_stat", length = 1)
    private String opStat;

    @Column(name = "ah_arh_stat", length = 1)
    private String ahArhStat;

    @Column(name = "alt_addr_stat", length = 1)
    private String altAddrStat;

    @Column(name = "adl_dpst_st", length = 1)
    private String adlDpstSt;

    @Column(name = "slf_gen_st", length = 1)
    private String slfGenSt;

    @Column(name = "supsd_acc")
    private Integer supsdAcc;

    @Column(name = "refund_dep", length = 1)
    private String refundDep;

    @Column(name = "no_loans")
    private Integer noLoans;

    @Column(name = "cst_st")
    private Integer cstSt;

    @Column(name = "inst_id", length = 8)
    private String instId;

    @Column(name = "zone", length = 2)
    private String zone;

    @Column(name = "date_addt")
    @Temporal(TemporalType.DATE)
    private Date dateAddt;

    @Column(name = "cust_cd", length = 4)
    private String custCd;

    @Column(name = "entered_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDtime;

    @Column(name = "edited_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedDtime;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "cust_type", length = 1)
    private String custType;

    @Column(name = "net_type", length = 1)
    private String netType;

    @Column(name = "cat_code", length = 4)
    private String catCode;

    @Column(name = "ncre", length = 1)
    private String ncre;


    @Column(name = "ncre_type", length = 1)
    private String ncre_type;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Add trimming setters for all string fields
    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr != null ? accNbr.trim() : null;
    }

    public void setJobNbr(String jobNbr) {
        this.jobNbr = jobNbr != null ? jobNbr.trim() : null;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd != null ? areaCd.trim() : null;
    }

    public void setCusCat(String cusCat) {
        this.cusCat = cusCat != null ? cusCat.trim() : null;
    }

    public void setRedCode(String redCode) {
        this.redCode = redCode != null ? redCode.trim() : null;
    }

    public void setDlyPack(String dlyPack) {
        this.dlyPack = dlyPack != null ? dlyPack.trim() : null;
    }

    public void setWlkOrd(String wlkOrd) {
        this.wlkOrd = wlkOrd != null ? wlkOrd.trim() : null;
    }

    public void setNatSup(String natSup) {
        this.natSup = natSup != null ? natSup.trim() : null;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setAddressL1(String addressL1) {
        this.addressL1 = addressL1 != null ? addressL1.trim() : null;
    }

    public void setAddressL2(String addressL2) {
        this.addressL2 = addressL2 != null ? addressL2.trim() : null;
    }

    public void setCity(String city) {
        this.city = city != null ? city.trim() : null;
    }

    public void setPCode(String pCode) {
        this.pCode = pCode != null ? pCode.trim() : null;
    }

    public void setTelNbr(String telNbr) {
        this.telNbr = telNbr != null ? telNbr.trim() : null;
    }

    public void setIdNbr(String idNbr) {
        this.idNbr = idNbr != null ? idNbr.trim() : null;
    }

    public void setIdType(String idType) {
        this.idType = idType != null ? idType.trim() : null;
    }

    public void setEstPivNbr(String estPivNbr) {
        this.estPivNbr = estPivNbr != null ? estPivNbr.trim() : null;
    }

    public void setCdPrmses(String cdPrmses) {
        this.cdPrmses = cdPrmses != null ? cdPrmses.trim() : null;
    }

    public void setIndType(String indType) {
        this.indType = indType != null ? indType.trim() : null;
    }

    public void setDepPivNbr(String depPivNbr) {
        this.depPivNbr = depPivNbr != null ? depPivNbr.trim() : null;
    }

    public void setAddDepPiv(String addDepPiv) {
        this.addDepPiv = addDepPiv != null ? addDepPiv.trim() : null;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff != null ? tariff.trim() : null;
    }

    public void setGstApl(String gstApl) {
        this.gstApl = gstApl != null ? gstApl.trim() : null;
    }

    public void setTaxInv(String taxInv) {
        this.taxInv = taxInv != null ? taxInv.trim() : null;
    }

    public void setTaxNum(String taxNum) {
        this.taxNum = taxNum != null ? taxNum.trim() : null;
    }

    public void setAuthLetter(String authLetter) {
        this.authLetter = authLetter != null ? authLetter.trim() : null;
    }

    public void setAgrmntNo(String agrmntNo) {
        this.agrmntNo = agrmntNo != null ? agrmntNo.trim() : null;
    }

    public void setOpStat(String opStat) {
        this.opStat = opStat != null ? opStat.trim() : null;
    }

    public void setAhArhStat(String ahArhStat) {
        this.ahArhStat = ahArhStat != null ? ahArhStat.trim() : null;
    }

    public void setAltAddrStat(String altAddrStat) {
        this.altAddrStat = altAddrStat != null ? altAddrStat.trim() : null;
    }

    public void setAdlDpstSt(String adlDpstSt) {
        this.adlDpstSt = adlDpstSt != null ? adlDpstSt.trim() : null;
    }

    public void setSlfGenSt(String slfGenSt) {
        this.slfGenSt = slfGenSt != null ? slfGenSt.trim() : null;
    }

    public void setRefundDep(String refundDep) {
        this.refundDep = refundDep != null ? refundDep.trim() : null;
    }

    public void setInstId(String instId) {
        this.instId = instId != null ? instId.trim() : null;
    }

    public void setZone(String zone) {
        this.zone = zone != null ? zone.trim() : null;
    }

    public void setCustCd(String custCd) {
        this.custCd = custCd != null ? custCd.trim() : null;
    }

    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    public void setEditedUserId(String editedUserId) {
        this.editedUserId = editedUserId != null ? editedUserId.trim() : null;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo != null ? mobileNo.trim() : null;
    }

    public void setCustType(String custType) {
        this.custType = custType != null ? custType.trim() : null;
    }

    public void setNetType(String netType) {
        this.netType = netType != null ? netType.trim() : null;
    }

    public void setCatCode(String catCode) {
        this.catCode = catCode != null ? catCode.trim() : null;
    }

    public void setNcre(String ncre) {
        this.ncre = ncre != null ? ncre.trim() : null;
    }

    public void setNcre_type(String ncre_type) {
        this.ncre_type = ncre_type != null ? ncre_type.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getAccNbr() {
        return accNbr != null ? accNbr.trim() : null;
    }

    public String getJobNbr() {
        return jobNbr != null ? jobNbr.trim() : null;
    }

    public String getAreaCd() {
        return areaCd != null ? areaCd.trim() : null;
    }

    public String getCusCat() {
        return cusCat != null ? cusCat.trim() : null;
    }

    public String getRedCode() {
        return redCode != null ? redCode.trim() : null;
    }

    public String getDlyPack() {
        return dlyPack != null ? dlyPack.trim() : null;
    }

    public String getWlkOrd() {
        return wlkOrd != null ? wlkOrd.trim() : null;
    }

    public String getNatSup() {
        return natSup != null ? natSup.trim() : null;
    }

    public String getName() {
        return name != null ? name.trim() : null;
    }

    public String getAddressL1() {
        return addressL1 != null ? addressL1.trim() : null;
    }

    public String getAddressL2() {
        return addressL2 != null ? addressL2.trim() : null;
    }

    public String getCity() {
        return city != null ? city.trim() : null;
    }

    public String getPCode() {
        return pCode != null ? pCode.trim() : null;
    }

    public String getTelNbr() {
        return telNbr != null ? telNbr.trim() : null;
    }

    public String getIdNbr() {
        return idNbr != null ? idNbr.trim() : null;
    }

    public String getIdType() {
        return idType != null ? idType.trim() : null;
    }

    public String getEstPivNbr() {
        return estPivNbr != null ? estPivNbr.trim() : null;
    }

    public String getCdPrmses() {
        return cdPrmses != null ? cdPrmses.trim() : null;
    }

    public String getIndType() {
        return indType != null ? indType.trim() : null;
    }

    public String getDepPivNbr() {
        return depPivNbr != null ? depPivNbr.trim() : null;
    }

    public String getAddDepPiv() {
        return addDepPiv != null ? addDepPiv.trim() : null;
    }

    public String getTariff() {
        return tariff != null ? tariff.trim() : null;
    }

    public String getGstApl() {
        return gstApl != null ? gstApl.trim() : null;
    }

    public String getTaxInv() {
        return taxInv != null ? taxInv.trim() : null;
    }

    public String getTaxNum() {
        return taxNum != null ? taxNum.trim() : null;
    }

    public String getAuthLetter() {
        return authLetter != null ? authLetter.trim() : null;
    }

    public String getAgrmntNo() {
        return agrmntNo != null ? agrmntNo.trim() : null;
    }

    public String getOpStat() {
        return opStat != null ? opStat.trim() : null;
    }

    public String getAhArhStat() {
        return ahArhStat != null ? ahArhStat.trim() : null;
    }

    public String getAltAddrStat() {
        return altAddrStat != null ? altAddrStat.trim() : null;
    }

    public String getAdlDpstSt() {
        return adlDpstSt != null ? adlDpstSt.trim() : null;
    }

    public String getSlfGenSt() {
        return slfGenSt != null ? slfGenSt.trim() : null;
    }

    public String getRefundDep() {
        return refundDep != null ? refundDep.trim() : null;
    }

    public String getInstId() {
        return instId != null ? instId.trim() : null;
    }

    public String getZone() {
        return zone != null ? zone.trim() : null;
    }

    public String getCustCd() {
        return custCd != null ? custCd.trim() : null;
    }

    public String getUserId() {
        return userId != null ? userId.trim() : null;
    }

    public String getEditedUserId() {
        return editedUserId != null ? editedUserId.trim() : null;
    }

    public String getMobileNo() {
        return mobileNo != null ? mobileNo.trim() : null;
    }

    public String getCustType() {
        return custType != null ? custType.trim() : null;
    }

    public String getNetType() {
        return netType != null ? netType.trim() : null;
    }

    public String getCatCode() {
        return catCode != null ? catCode.trim() : null;
    }
}