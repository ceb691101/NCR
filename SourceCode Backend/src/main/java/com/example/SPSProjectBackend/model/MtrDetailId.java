package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MtrDetailId implements Serializable {
    private String instId;
    private String addedBlcy;
    private String mtrType;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MtrDetailId that = (MtrDetailId) o;
        return Objects.equals(instId, that.instId) &&
               Objects.equals(addedBlcy, that.addedBlcy) &&
               Objects.equals(mtrType, that.mtrType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instId, addedBlcy, mtrType);
    }
}