package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SpstdesthmtId implements Serializable {
    private static final long serialVersionUID = 5798491662987563222L;
    @Column(name = "APP_NO", nullable = false, length = 25)
    private String appNo;

    @Column(name = "STD_NO", nullable = false, length = 50)
    private String stdNo;

    @Column(name = "DEPT_ID", nullable = false, length = 20)
    private String deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpstdesthmtId entity = (SpstdesthmtId) o;
        return Objects.equals(this.deptId, entity.deptId) &&
                Objects.equals(this.appNo, entity.appNo) &&
                Objects.equals(this.stdNo, entity.stdNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId, appNo, stdNo);
    }

}