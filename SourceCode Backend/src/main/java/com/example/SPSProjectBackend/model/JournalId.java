package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalId implements Serializable {

    @Column(name = "acc_nbr", length = 10)
    private String accNbr;

    @Column(name = "area_cd", length = 2)
    private String areaCd;

    @Column(name = "added_blcy", length = 5)
    private Integer addedBlcy;

    @Column(name = "jnl_type", length = 4)
    private String jnlType;

    @Column(name = "jnl_no", nullable = false)
    private Integer jnlNo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JournalId journalId = (JournalId) o;
        return Objects.equals(accNbr, journalId.accNbr) &&
               Objects.equals(areaCd, journalId.areaCd) &&
               Objects.equals(addedBlcy, journalId.addedBlcy) &&
               Objects.equals(jnlType, journalId.jnlType) &&
               Objects.equals(jnlNo, journalId.jnlNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNbr, areaCd, addedBlcy, jnlType, jnlNo);
    }
}
