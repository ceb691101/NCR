package com.example.SPSProjectBackend.dto;

import java.util.List;

public class SubGroupSection {
    private String subGroupName;
    private List<SimpleTariffDisplay> tariffs;

    public SubGroupSection() {
    }

    public SubGroupSection(String subGroupName, List<SimpleTariffDisplay> tariffs) {
        this.subGroupName = subGroupName;
        this.tariffs = tariffs;
    }

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }

    public List<SimpleTariffDisplay> getTariffs() {
        return tariffs;
    }

    public void setTariffs(List<SimpleTariffDisplay> tariffs) {
        this.tariffs = tariffs;
    }
}
