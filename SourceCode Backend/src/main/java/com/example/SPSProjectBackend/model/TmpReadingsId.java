package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmpReadingsId implements Serializable {
    private String accNbr;
    private String areaCd;
    private String addedBlcy;
    private Integer mtrSeq;
    private String mtrType;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TmpReadingsId that = (TmpReadingsId) o;
        return Objects.equals(accNbr, that.accNbr) &&
               Objects.equals(areaCd, that.areaCd) &&
               Objects.equals(addedBlcy, that.addedBlcy) &&
               Objects.equals(mtrSeq, that.mtrSeq) &&
               Objects.equals(mtrType, that.mtrType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accNbr, areaCd, addedBlcy, mtrSeq, mtrType);
    }
}