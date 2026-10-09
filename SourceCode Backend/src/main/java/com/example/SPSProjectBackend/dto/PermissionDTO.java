package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PermissionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPermissionItemDTO {
        private String funcId;
        private String applId;
        private String funcNm;
        private String subFuncId;
        private String subFuncNm;
        private BigDecimal seqNo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubFunctionDTO {
        private String subFuncId;
        private String subFuncNm;
        private String routePath;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SidebarFunctionDTO {
        private String funcId;
        private String funcNm;
        private BigDecimal seqNo;
        private List<SubFunctionDTO> subFunctions = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPermissionsResponseDTO {
        private String userId;
        private String applId;
        private List<UserPermissionItemDTO> permissions = new ArrayList<>();
        private List<SidebarFunctionDTO> sidebarFunctions = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckAccessRequestDTO {
        private String funcId;
        private String subFuncId;
        private String routePath;
        private String applId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckAccessResponseDTO {
        private boolean hasAccess;
        private String userId;
        private String applId;
        private String funcId;
        private String subFuncId;
        private String message;
    }
}
