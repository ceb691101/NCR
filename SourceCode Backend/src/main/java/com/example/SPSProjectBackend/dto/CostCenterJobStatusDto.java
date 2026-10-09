package com.example.SPSProjectBackend.dto;

public class CostCenterJobStatusDto {
    private String costCenter;
    private Long openJobs;
    private Long revisedJobs;
    private Long closedJobs;

    public CostCenterJobStatusDto(String costCenter, Long openJobs, Long revisedJobs, Long closedJobs) {
        this.costCenter = costCenter;
        this.openJobs = openJobs;
        this.revisedJobs = revisedJobs;
        this.closedJobs = closedJobs;
    }

    // Getters and setters

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public Long getOpenJobs() {
        return openJobs;
    }

    public void setOpenJobs(Long openJobs) {
        this.openJobs = openJobs;
    }

    public Long getRevisedJobs() {
        return revisedJobs;
    }

    public void setRevisedJobs(Long revisedJobs) {
        this.revisedJobs = revisedJobs;
    }

    public Long getClosedJobs() {
        return closedJobs;
    }

    public void setClosedJobs(Long closedJobs) {
        this.closedJobs = closedJobs;
    }
}
