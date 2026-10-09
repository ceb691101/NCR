package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreRuChangeId implements Serializable {
    private Short folioNo;
    private Date entDt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcreRuChangeId that = (NcreRuChangeId) o;
        return Objects.equals(folioNo, that.folioNo) &&
               Objects.equals(entDt, that.entDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folioNo, entDt);
    }
}
