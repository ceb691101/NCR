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
public class PcesthttId implements java.io.Serializable {
    private static final long serialVersionUID = -1808297752775705524L;
    @Column(name = "ESTIMATE_NO", nullable = false, length = 20)
    private String estimateNo;

    @Column(name = "REV_NO", nullable = false)
    private Short revNo;

    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PcesthttId entity = (PcesthttId) o;
        return Objects.equals(this.estimateNo, entity.estimateNo) &&
                Objects.equals(this.revNo, entity.revNo) &&
                Objects.equals(this.deptId, entity.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estimateNo, revNo, deptId);
    }

}