package com.example.SPSProjectBackend.dto;

import java.math.BigDecimal;

public class StatusCountDTO {

    private BigDecimal status;
    private Long count;

    // Default constructor (needed by Jackson)
    public StatusCountDTO() {
    }

    // All-args constructor
    public StatusCountDTO(BigDecimal status, Long count) {
        this.status = status;
        this.count = count;
    }

    public BigDecimal getStatus() {
        return status;
    }

    public void setStatus(BigDecimal status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
