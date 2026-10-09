package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DifferenceStatisticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDifferenceDTO {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("folio_no")
        private String folioNo;

        @JsonProperty("facility_name")
        private String facilityName;

        @JsonProperty("difference")
        private BigDecimal difference;

        @JsonProperty("accept_ru")
        private Short acceptRu;

        public AccountDifferenceDTO(String accountNumber, String facilityName, BigDecimal difference) {
            this.accountNumber = accountNumber;
            this.facilityName = facilityName;
            this.difference = difference;
        }
    }

    @Data
    @NoArgsConstructor
    public static class DifferenceResponse {
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("message")
        private String message;

        @JsonProperty("area_code")
        private String areaCode;

        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;

        @JsonProperty("differences")
        private List<AccountDifferenceDTO> differences;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;

        /** Areas covered by this response. More than one when the user has not narrowed the selection. */
        @JsonProperty("area_codes")
        private List<String> areaCodes;

        /** Active bill cycle resolved per area; bill cycles can differ between areas. */
        @JsonProperty("bill_cycles")
        private List<String> billCycles;

        /** Permitted areas that had no active bill cycle and were therefore skipped. */
        @JsonProperty("skipped_area_codes")
        private List<String> skippedAreaCodes;

        public DifferenceResponse(Boolean success, String message, String areaCode, String activeBillCycle,
                                  List<AccountDifferenceDTO> differences, LocalDateTime timestamp) {
            this.success = success;
            this.message = message;
            this.areaCode = areaCode;
            this.activeBillCycle = activeBillCycle;
            this.differences = differences;
            this.timestamp = timestamp;
        }
    }
}
