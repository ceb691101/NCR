package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MtrReasonDTO {

    @JsonProperty("rsn_code")
    private String rsnCode;

    @JsonProperty("rsn_desc")
    private String rsnDesc;

    @JsonProperty("rsn_status")
    private String rsnStatus;
}
