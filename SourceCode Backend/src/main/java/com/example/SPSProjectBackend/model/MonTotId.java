package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonTotId implements Serializable {
    private String accNbr;
    private String billCycle;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonTotId monTotId = (MonTotId) o;
        return Objects.equals(accNbr, monTotId.accNbr) &&
               Objects.equals(billCycle, monTotId.billCycle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accNbr, billCycle);
    }
}