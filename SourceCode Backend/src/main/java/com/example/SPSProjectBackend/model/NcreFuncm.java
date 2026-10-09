package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ncre_funcm", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NcreFuncmId.class)
public class NcreFuncm {

    @Id
    @Column(name = "func_id", length = 8, nullable = false)
    private String funcId;

    @Id
    @Column(name = "appl_id", length = 3, nullable = false)
    private String applId;

    @Column(name = "func_nm", length = 60)
    private String funcNm;

    @Id
    @Column(name = "sub_func_id", length = 8, nullable = false)
    private String subFuncId;

    @Column(name = "sub_func_nm", length = 60)
    private String subFuncNm;

    @Column(name = "seq_no")
    private BigDecimal seqNo;

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

    public void setFuncId(String funcId) {
        this.funcId = funcId != null ? funcId.trim() : null;
    }

    public void setApplId(String applId) {
        this.applId = applId != null ? applId.trim() : null;
    }

    public void setSubFuncId(String subFuncId) {
        this.subFuncId = subFuncId != null ? subFuncId.trim() : null;
    }

    public void setFuncNm(String funcNm) {
        this.funcNm = funcNm != null ? funcNm.trim() : null;
    }

    public void setSubFuncNm(String subFuncNm) {
        this.subFuncNm = subFuncNm != null ? subFuncNm.trim() : null;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : null;
    }
}
