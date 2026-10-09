package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandDTO {

    @JsonProperty("br_code")
    private String brCode;

    @JsonProperty("br_desc")
    private String brDesc;

    @JsonProperty("br_status")
    private String brStatus;
}
