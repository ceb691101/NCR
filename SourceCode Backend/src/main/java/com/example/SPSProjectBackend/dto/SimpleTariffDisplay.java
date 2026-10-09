package com.example.SPSProjectBackend.dto;

public class SimpleTariffDisplay {
    private String thresholdLabel;
    private String energyRate;
    private String fixedCharge;

    public SimpleTariffDisplay() {
    }

    public SimpleTariffDisplay(String thresholdLabel, String energyRate, String fixedCharge) {
        this.thresholdLabel = thresholdLabel;
        this.energyRate = energyRate;
        this.fixedCharge = fixedCharge;
    }

    public String getThresholdLabel() {
        return thresholdLabel;
    }

    public void setThresholdLabel(String thresholdLabel) {
        this.thresholdLabel = thresholdLabel;
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
