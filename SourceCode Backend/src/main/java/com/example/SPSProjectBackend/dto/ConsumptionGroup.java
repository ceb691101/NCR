package com.example.SPSProjectBackend.dto;

import java.util.List;

public class ConsumptionGroup {
    private String rangeName;
    private List<TariffBlockDisplay> blocks;

    public ConsumptionGroup() {
    }

    public ConsumptionGroup(String rangeName, List<TariffBlockDisplay> blocks) {
        this.rangeName = rangeName;
        this.blocks = blocks;
    }

    public String getRangeName() {
        return rangeName;
    }

    public void setRangeName(String rangeName) {
        this.rangeName = rangeName;
    }

    public List<TariffBlockDisplay> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<TariffBlockDisplay> blocks) {
        this.blocks = blocks;
    }
}
