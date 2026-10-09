package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdmPlantsResponse {

    @JsonProperty("items")
    private List<MdmPlantItem> items;

    @JsonProperty("nextCursor")
    private String nextCursor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MdmPlantItem {
        @JsonProperty("plantId")
        private String plantId;

        @JsonProperty("folioNo")
        private String folioNo;

        @JsonProperty("status")
        private String status;

        @JsonProperty("statusReason")
        private String statusReason;

        @JsonProperty("sourceFresh")
        private Boolean sourceFresh;

        @JsonProperty("facilityName")
        private String facilityName;

        @JsonProperty("ncreTypeCode")
        private String ncreTypeCode;
    }
}
