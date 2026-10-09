package com.example.SPSProjectBackend.model;



import java.io.Serializable;
import java.util.Objects;

public class ProgressMoniterId implements Serializable {

    private String id;
    private String deptId;

    public ProgressMoniterId() {}

    public ProgressMoniterId(String id, String deptId) {
        this.id = id;
        this.deptId = deptId;
    }

    // Getters, Setters, hashCode, and equals
    @Override
    public int hashCode() {
        return Objects.hash(id, deptId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProgressMoniterId that = (ProgressMoniterId) obj;
        return Objects.equals(id, that.id) && Objects.equals(deptId, that.deptId);
    }
}