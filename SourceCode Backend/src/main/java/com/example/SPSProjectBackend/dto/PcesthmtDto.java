// PcesthmtDto.java
package com.example.SPSProjectBackend.dto;

import java.time.LocalDate;

public class PcesthmtDto {
    private String pcesthmtId;
    private String estimateNo;
    private Short revNo;
    private String deptId;
    private String projectNo;
    private String clientNm;
    private String descr;
    private LocalDate etimateDt;
    private Long stdCost;
    private Long paidAmt;
    private String status;
    private String revReason;

    public PcesthmtDto(String estimateNo, Short revNo, String deptId,
                       String projectNo, String clientNm, String descr,
                       LocalDate etimateDt, Long stdCost, Long paidAmt,
                       Short statusCode, String revReason) {
        this.estimateNo = estimateNo;
        this.revNo = revNo;
        this.deptId = deptId;
        this.projectNo = projectNo;
        this.clientNm = clientNm;
        this.descr = descr;
        this.etimateDt = etimateDt;
        this.stdCost = stdCost;
        this.paidAmt = paidAmt;
        this.status = statusCode != null ? String.valueOf(statusCode) : null;
        this.revReason = revReason;
        this.pcesthmtId = estimateNo + "-" + revNo + "-" + deptId;
    }

    // Getters and setters

    public String getPcesthmtId() { return pcesthmtId; }

    public String getEstimateNo() { return estimateNo; }
    public void setEstimateNo(String estimateNo) { this.estimateNo = estimateNo; }

    public Short getRevNo() { return revNo; }
    public void setRevNo(Short revNo) { this.revNo = revNo; }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }

    public String getProjectNo() { return projectNo; }
    public void setProjectNo(String projectNo) { this.projectNo = projectNo; }

    public String getClientNm() { return clientNm; }
    public void setClientNm(String clientNm) { this.clientNm = clientNm; }

    public String getDescr() { return descr; }
    public void setDescr(String descr) { this.descr = descr; }

    public LocalDate getEtimateDt() { return etimateDt; }
    public void setEtimateDt(LocalDate etimateDt) { this.etimateDt = etimateDt; }

    public Long getStdCost() { return stdCost; }
    public void setStdCost(Long stdCost) { this.stdCost = stdCost; }

    public Long getPaidAmt() { return paidAmt; }
    public void setPaidAmt(Long paidAmt) { this.paidAmt = paidAmt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRevReason() { return revReason; }
    public void setRevReason(String revReason) { this.revReason = revReason; }
}
