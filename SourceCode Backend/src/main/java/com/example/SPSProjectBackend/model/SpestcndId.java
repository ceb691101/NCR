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
public class SpestcndId implements java.io.Serializable {
    private static final long serialVersionUID = 672024032697963844L;
    @Column(name = "CONTRACTOR_ID", nullable = false, length = 4)
    private String contractorId;

    @Column(name = "PROJECT_NO", nullable = false, length = 20)
    private String projectNo;

    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpestcndId entity = (SpestcndId) o;
        return Objects.equals(this.projectNo, entity.projectNo) &&
                Objects.equals(this.contractorId, entity.contractorId) &&
                Objects.equals(this.deptId, entity.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectNo, contractorId, deptId);
    }

}