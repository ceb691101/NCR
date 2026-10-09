package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MdmPlantStatusDTO {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("sourceFresh")
    private Boolean sourceFresh;

    @JsonProperty("checkedAt")
    private String checkedAt;

    @JsonProperty("onlineFolios")
    private List<String> onlineFolios;

    @JsonProperty("message")
    private String message;
}
