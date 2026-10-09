package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill_CycleLogFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @Column(name = "pro_code", length = 10)
    private String proCode;
    
    @Column(name = "area_code", length = 2)
    private String areaCode;
    
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    
    @Column(name = "bill_cycle")
    private Integer billCycle;
    
    @Column(name = "no_of_recs")
    private Integer noOfRecs;
    
    @Column(name = "start_time", length = 10)
    private String startTime;
    
    @Column(name = "end_time", length = 10)
    private String endTime;
    
    @Column(name = "duration_min")
    private Integer durationMin;
    
    @Column(name = "user_id", length = 20)
    private String userId;
    
    // Many-to-one relationship with Area
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "area_code", insertable = false, updatable = false)
    private HsbArea area;
    
    // Many-to-one relationship with BillCycleConfig
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "area_code", referencedColumnName = "area_code", insertable = false, updatable = false),
        @JoinColumn(name = "bill_cycle", referencedColumnName = "bill_cycle", insertable = false, updatable = false)
    })
    private BillCycleConfig billCycleConfig;
    
    // Pre-persist method to set date_time automatically
    @PrePersist
    protected void onCreate() {
        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }
    }

    // Add trimming setters and getters for CHAR fields
    public void setProCode(String proCode) {
        this.proCode = proCode != null ? proCode.trim() : null;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode != null ? areaCode.trim() : null;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime != null ? startTime.trim() : null;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime != null ? endTime.trim() : null;
    }

    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getProCode() {
        return proCode != null ? proCode.trim() : null;
    }

    public String getAreaCode() {
        return areaCode != null ? areaCode.trim() : null;
    }

    public String getStartTime() {
        return startTime != null ? startTime.trim() : null;
    }

    public String getEndTime() {
        return endTime != null ? endTime.trim() : null;
    }

    public String getUserId() {
        return userId != null ? userId.trim() : null;
    }
}