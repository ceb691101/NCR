package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "ncre_role_func", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NcreRoleFuncId.class)
public class NcreRoleFunc {

    @Id
    @Column(name = "user_id", length = 10, nullable = false)
    private String userId;

    @Id
    @Column(name = "func_id", length = 8, nullable = false)
    private String funcId;

    @Id
    @Column(name = "appl_id", length = 3, nullable = false)
    private String applId;

    @Id
    @Column(name = "sub_func_id", length = 8, nullable = false)
    private String subFuncId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "ent_by", length = 12)
    private String entBy;

    @Column(name = "ent_dt")
    @Temporal(TemporalType.DATE)
    private Date entDt;

    @Column(name = "modi_by", length = 12)
    private String modiBy;

    @Column(name = "modi_dt")
    @Temporal(TemporalType.DATE)
    private Date modiDt;

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

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : null;
    }
}
