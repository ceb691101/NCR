package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provinces", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbProvince {
    
    @Id
    @Column(name = "prov_code", length = 2, nullable = false)
    private String provCode;
    
    @Column(name = "prov_name", length = 20, nullable = false)
    private String provName;

    // Add trimming setters and getters
    public void setProvCode(String provCode) {
        this.provCode = provCode != null ? provCode.trim() : null;
    }

    public void setProvName(String provName) {
        this.provName = provName != null ? provName.trim() : null;
    }

    // Override getters to ensure trimmed values
    public String getProvCode() {
        return provCode != null ? provCode.trim() : null;
    }

    public String getProvName() {
        return provName != null ? provName.trim() : null;
    }
}