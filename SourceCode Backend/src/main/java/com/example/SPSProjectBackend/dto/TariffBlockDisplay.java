package com.example.SPSProjectBackend.dto;

public class TariffBlockDisplay {
    private String rangeLabel;
    private String energyRate;
    private String fixedCharge;

    public TariffBlockDisplay() {
    }

    public TariffBlockDisplay(String rangeLabel, String energyRate, String fixedCharge) {
        this.rangeLabel = rangeLabel;
        this.energyRate = energyRate;
        this.fixedCharge = fixedCharge;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
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
