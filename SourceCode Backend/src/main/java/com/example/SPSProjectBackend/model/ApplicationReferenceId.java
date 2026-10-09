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
public class ApplicationReferenceId implements java.io.Serializable {
    private static final long serialVersionUID = 5700278957584289076L;
    @Column(name = "APPLICATION_ID", nullable = false, length = 21)
    private String applicationId;

    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ApplicationReferenceId entity = (ApplicationReferenceId) o;
        return Objects.equals(this.deptId, entity.deptId) &&
                Objects.equals(this.applicationId, entity.applicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId, applicationId);
    }

}