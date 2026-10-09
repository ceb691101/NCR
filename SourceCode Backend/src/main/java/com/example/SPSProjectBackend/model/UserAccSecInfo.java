package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sec_info", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccSecInfo {
    
    @Id
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "user_name", length = 20, nullable = false)
    private String userName;

    @Column(name = "passwd", length = 50, nullable = false)
    private String passwd;

    @Column(name = "user_cat", length = 20)
    private String userCat;
    
    @Column(name = "region_code", length = 2)
    private String regionCode;
    
    @Column(name = "province_code", length = 2)
    private String provinceCode;
    
    @Column(name = "area_code", length = 2)
    private String areaCode;
    
    @Column(name = "epf_num", length = 10, nullable = false)
    private String epfNum;
    
    @Column(name = "status", nullable = false, columnDefinition = "SMALLINT DEFAULT 1")
    private Integer status = 1; // Default to active (1)

    // Add class field with default value 1
    @Column(name = "class", nullable = false, columnDefinition = "SMALLINT DEFAULT 1")
    private Integer classField = 1; // Always set to 1 as default

    // Add trimming setters for all string fields
    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    public void setUserName(String userName) {
        this.userName = userName != null ? userName.trim() : null;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd != null ? passwd.trim() : null;
    }

    public void setUserCat(String userCat) {
        this.userCat = userCat != null ? userCat.trim() : null;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode != null ? regionCode.trim() : null;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode != null ? provinceCode.trim() : null;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode != null ? areaCode.trim() : null;
    }

    public void setEpfNum(String epfNum) {
        this.epfNum = epfNum != null ? epfNum.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getUserId() {
        return userId != null ? userId.trim() : null;
    }

    public String getUserName() {
        return userName != null ? userName.trim() : null;
    }

    public String getPasswd() {
        return passwd != null ? passwd.trim() : null;
    }

    public String getUserCat() {
        return userCat != null ? userCat.trim() : null;
    }

    public String getRegionCode() {
        return regionCode != null ? regionCode.trim() : null;
    }

    public String getProvinceCode() {
        return provinceCode != null ? provinceCode.trim() : null;
    }

    public String getAreaCode() {
        return areaCode != null ? areaCode.trim() : null;
    }

    public String getEpfNum() {
        return epfNum != null ? epfNum.trim() : null;
    }

    // Getter and setter for classField
    public Integer getClassField() {
        return classField != null ? classField : 1; // Always return 1 if null
    }

    public void setClassField(Integer classField) {
        this.classField = classField != null ? classField : 1; // Always set to 1, never null
    }
}