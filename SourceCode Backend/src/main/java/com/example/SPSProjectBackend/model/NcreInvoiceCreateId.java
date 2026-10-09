package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcreInvoiceCreateId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "bill_cycle", nullable = false)
    private Integer billCycle;

    @Column(name = "folio_no", nullable = false)
    private Integer folioNo;

    public NcreInvoiceCreateId(Short billCycle, Integer folioNo) {
        this.billCycle = billCycle != null ? billCycle.intValue() : null;
        this.folioNo = folioNo;
    }

    public Short getBillCycleAsShort() {
        return billCycle != null ? billCycle.shortValue() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcreInvoiceCreateId that = (NcreInvoiceCreateId) o;
        return Objects.equals(billCycle, that.billCycle) &&
               Objects.equals(folioNo, that.folioNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billCycle, folioNo);
    }
}

