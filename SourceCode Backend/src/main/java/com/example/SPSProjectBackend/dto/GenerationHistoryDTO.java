package com.example.SPSProjectBackend.dto;

import java.math.BigDecimal;

public class GenerationHistoryDTO {
    private String billCycle;
    private BigDecimal generationR1;
    private BigDecimal generationR2;
    private BigDecimal generationR3;
    private BigDecimal totalGeneration;

    public GenerationHistoryDTO() {}

    public GenerationHistoryDTO(String billCycle, BigDecimal generationR1, BigDecimal generationR2, BigDecimal generationR3, BigDecimal totalGeneration) {
        this.billCycle = billCycle;
        this.generationR1 = generationR1;
        this.generationR2 = generationR2;
        this.generationR3 = generationR3;
        this.totalGeneration = totalGeneration;
    }

    public String getBillCycle() {
        return billCycle;
    }

    public void setBillCycle(String billCycle) {
        this.billCycle = billCycle;
    }

    public BigDecimal getGenerationR1() {
        return generationR1;
    }

    public void setGenerationR1(BigDecimal generationR1) {
        this.generationR1 = generationR1;
    }

    public BigDecimal getGenerationR2() {
        return generationR2;
    }

    public void setGenerationR2(BigDecimal generationR2) {
        this.generationR2 = generationR2;
    }

    public BigDecimal getGenerationR3() {
        return generationR3;
    }

    public void setGenerationR3(BigDecimal generationR3) {
        this.generationR3 = generationR3;
    }

    public BigDecimal getTotalGeneration() {
        return totalGeneration;
    }

    public void setTotalGeneration(BigDecimal totalGeneration) {
        this.totalGeneration = totalGeneration;
    }
}
