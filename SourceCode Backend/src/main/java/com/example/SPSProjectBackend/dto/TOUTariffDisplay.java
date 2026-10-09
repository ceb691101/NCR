package com.example.SPSProjectBackend.dto;

public class TOUTariffDisplay {
    private String periodLabel;
    private String timeRange;
    private Short tariffId;
    private String energyRate;
    private String fixedCharge;

    public TOUTariffDisplay() {
    }

    public TOUTariffDisplay(String periodLabel, String timeRange, Short tariffId, String energyRate, String fixedCharge) {
        this.periodLabel = periodLabel;
        this.timeRange = timeRange;
        this.tariffId = tariffId;
        this.energyRate = energyRate;
        this.fixedCharge = fixedCharge;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public Short getTariffId() {
        return tariffId;
    }

    public void setTariffId(Short tariffId) {
        this.tariffId = tariffId;
    }

    public String getEnergyRate() {
        return energyRate;
    }

    public void setEnergyRate(String energyRate) {
        this.energyRate = energyRate;
    }

    public String getFixedCharge() {
        return fixedCharge;
    }

    public void setFixedCharge(String fixedCharge) {
        this.fixedCharge = fixedCharge;
    }
}
