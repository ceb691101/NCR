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
public class SpestedyConId implements java.io.Serializable {
    private static final long serialVersionUID = -1533084549466384949L;
    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Column(name = "APPOINTMENT_ID", nullable = false, length = 11)
    private String appointmentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpestedyConId entity = (SpestedyConId) o;
        return Objects.equals(this.appointmentId, entity.appointmentId) &&
                Objects.equals(this.deptId, entity.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId, deptId);
    }

}