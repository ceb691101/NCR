package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "areas", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbArea {
    
    @Id
    @Column(name = "area_code", length = 2, nullable = false)
    private String areaCode;
    
    @Column(name = "prov_code", length = 2, nullable = false)
    private String provCode;
    
    @Column(name = "area_name", length = 20, nullable = false)
    private String areaName;
    
    @Column(name = "address_l1", length = 40)
    private String addressL1;
    
    @Column(name = "address_l2", length = 40)
    private String addressL2;
    
    @Column(name = "address_l3", length = 25)
    private String addressL3;
    
    @Column(name = "ar_tel_1", length = 12)
    private String arTel1;
    
    @Column(name = "ar_tel_2", length = 12)
    private String arTel2;
    
    @Column(name = "ar_tel_3", length = 12)
    private String arTel3;
    
    @Column(name = "ar_fax_1", length = 13)
    private String arFax1;
    
    @Column(name = "ar_fax_2", length = 13)
    private String arFax2;
    
    @Column(name = "ar_email", length = 75)
    private String arEmail;
    
    @Column(name = "region", length = 2)
    private String region;
    
    @Column(name = "ar_email_2", length = 75)
    private String arEmail2;
    
    // Many-to-one relationship with Province
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prov_code", referencedColumnName = "prov_code", insertable = false, updatable = false)
    private HsbProvince province;

    // Add trimming setters and getters for all CHAR fields
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode != null ? areaCode.trim() : null;
    }

    public void setProvCode(String provCode) {
        this.provCode = provCode != null ? provCode.trim() : null;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName != null ? areaName.trim() : null;
    }

    public void setAddressL1(String addressL1) {
        this.addressL1 = addressL1 != null ? addressL1.trim() : null;
    }

    public void setAddressL2(String addressL2) {
        this.addressL2 = addressL2 != null ? addressL2.trim() : null;
    }

    public void setAddressL3(String addressL3) {
        this.addressL3 = addressL3 != null ? addressL3.trim() : null;
    }

    public void setArTel1(String arTel1) {
        this.arTel1 = arTel1 != null ? arTel1.trim() : null;
    }

    public void setArTel2(String arTel2) {
        this.arTel2 = arTel2 != null ? arTel2.trim() : null;
    }

    public void setArTel3(String arTel3) {
        this.arTel3 = arTel3 != null ? arTel3.trim() : null;
    }

    public void setArFax1(String arFax1) {
        this.arFax1 = arFax1 != null ? arFax1.trim() : null;
    }

    public void setArFax2(String arFax2) {
        this.arFax2 = arFax2 != null ? arFax2.trim() : null;
    }

    public void setArEmail(String arEmail) {
        this.arEmail = arEmail != null ? arEmail.trim() : null;
    }

    public void setRegion(String region) {
        this.region = region != null ? region.trim() : null;
    }

    public void setArEmail2(String arEmail2) {
        this.arEmail2 = arEmail2 != null ? arEmail2.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getAreaCode() {
        return areaCode != null ? areaCode.trim() : null;
    }

    public String getProvCode() {
        return provCode != null ? provCode.trim() : null;
    }

    public String getAreaName() {
        return areaName != null ? areaName.trim() : null;
    }

    public String getAddressL1() {
        return addressL1 != null ? addressL1.trim() : null;
    }

    public String getAddressL2() {
        return addressL2 != null ? addressL2.trim() : null;
    }

    public String getAddressL3() {
        return addressL3 != null ? addressL3.trim() : null;
    }

    public String getArTel1() {
        return arTel1 != null ? arTel1.trim() : null;
    }

    public String getArTel2() {
        return arTel2 != null ? arTel2.trim() : null;
    }

    public String getArTel3() {
        return arTel3 != null ? arTel3.trim() : null;
    }

    public String getArFax1() {
        return arFax1 != null ? arFax1.trim() : null;
    }

    public String getArFax2() {
        return arFax2 != null ? arFax2.trim() : null;
    }

    public String getArEmail() {
        return arEmail != null ? arEmail.trim() : null;
    }

    public String getRegion() {
        return region != null ? region.trim() : null;
    }

    public String getArEmail2() {
        return arEmail2 != null ? arEmail2.trim() : null;
    }
}