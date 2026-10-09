package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmpMonTotId implements Serializable {
    private String accNbr;
    private String billCycle;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TmpMonTotId that = (TmpMonTotId) o;
        return Objects.equals(accNbr, that.accNbr) &&
               Objects.equals(billCycle, that.billCycle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accNbr, billCycle);
    }
}