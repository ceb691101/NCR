package com.example.SPSProjectBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class UserAccSecInfoDTO {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("user_cat")
    private String userCat;
    
    @JsonProperty("region_code")
    private String regionCode;
    
    @JsonProperty("province_code")
    private String provinceCode;
    
    @JsonProperty("area_code")
    private String areaCode;
    
    @JsonProperty("epf_num")
    private String epfNum;
    
    @JsonProperty("status")
    private Integer status;
    
    public UserAccSecInfoDTO(String userId, String userName, String userCat,
                            String regionCode, String provinceCode, String areaCode,
                            String epfNum, Integer status) {
        this.userId = userId;
        this.userName = userName;
        this.userCat = userCat;
        this.regionCode = regionCode;
        this.provinceCode = provinceCode;
        this.areaCode = areaCode;
        this.epfNum = epfNum;
        this.status = status;
    }
}