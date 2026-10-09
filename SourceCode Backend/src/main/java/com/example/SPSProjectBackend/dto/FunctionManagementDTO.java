package com.example.SPSProjectBackend.dto;

import com.example.SPSProjectBackend.model.NcreFuncm;
import lombok.Data;

import java.util.List;

public class FunctionManagementDTO {

    @Data
    public static class PermissionItemDTO {
        private NcreFuncm function;
        private boolean granted;
    }

    @Data
    public static class PermissionMatrixDTO {
        private String userId;
        private String applId;
        private List<PermissionItemDTO> permissions;
    }

    @Data
    public static class PermissionKeyDTO {
        private String funcId;
        private String subFuncId;
    }

    @Data
    public static class PermissionUpdateDTO {
        private String applId;
        private List<PermissionKeyDTO> permissions;
    }
}