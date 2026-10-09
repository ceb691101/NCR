package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreRoleFuncId implements Serializable {
    private String userId;
    private String funcId;
    private String applId;
    private String subFuncId;

    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    public void setFuncId(String funcId) {
        this.funcId = funcId != null ? funcId.trim() : null;
    }

    public void setApplId(String applId) {
        this.applId = applId != null ? applId.trim() : null;
    }

    public void setSubFuncId(String subFuncId) {
        this.subFuncId = subFuncId != null ? subFuncId.trim() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcreRoleFuncId that = (NcreRoleFuncId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(funcId, that.funcId) &&
               Objects.equals(applId, that.applId) &&
               Objects.equals(subFuncId, that.subFuncId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, funcId, applId, subFuncId);
    }
}
