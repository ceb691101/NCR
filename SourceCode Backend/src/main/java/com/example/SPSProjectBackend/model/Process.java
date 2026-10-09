package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "process", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Process {
    
    @Id
    @Column(name = "pro_code", length = 10)
    private String proCode;
    
    @Column(name = "pro_desc", length = 100)
    private String proDesc;
    
    // Trimming setter for CHAR fields
    public void setProCode(String proCode) {
        this.proCode = proCode != null ? proCode.trim() : null;
    }
    
    public String getProCode() {
        return proCode != null ? proCode.trim() : proCode;
    }
}
