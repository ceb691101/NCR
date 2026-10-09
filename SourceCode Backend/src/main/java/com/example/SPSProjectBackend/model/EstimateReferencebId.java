package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class EstimateReferencebId implements java.io.Serializable {
    private static final long serialVersionUID = -4241686017340723210L;
    @Column(name = "SESTIMATE_NO", nullable = false, length = 26)
    private String sestimateNo;

    @Column(name = "WESTIMATE_NO", nullable = false, length = 21)
    private String westimateNo;

    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EstimateReferencebId entity = (EstimateReferencebId) o;
        return Objects.equals(this.deptId, entity.deptId) &&
                Objects.equals(this.westimateNo, entity.westimateNo) &&
                Objects.equals(this.sestimateNo, entity.sestimateNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId, westimateNo, sestimateNo);
    }

}