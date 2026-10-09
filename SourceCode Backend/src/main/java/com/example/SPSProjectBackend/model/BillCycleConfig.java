package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "config", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BillCycleConfigId.class)
public class BillCycleConfig {
    
    @Id
    @Column(name = "bill_cycle", nullable = false)
    private Integer billCycle;
    
    @Id
    @Column(name = "area_code", length = 2, nullable = false)
    private String areaCode;
    
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;
    
    @Column(name = "entered_date")
    private LocalDateTime enteredDate;
    
    @Column(name = "cycle_stat", columnDefinition = "SMALLINT DEFAULT 1")
    private Integer cycleStat;
    
    // Many-to-one relationship with Area
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    private HsbArea area;

    // Add trimming setters and getters for CHAR fields
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode != null ? areaCode.trim() : null;
    }

    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getAreaCode() {
        return areaCode != null ? areaCode.trim() : null;
    }

    public String getUserId() {
        return userId != null ? userId.trim() : null;
    }
}