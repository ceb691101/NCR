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
public class SpstdestdmtId implements java.io.Serializable {
    private static final long serialVersionUID = 3768562425638060296L;
    @Column(name = "APP_NO", nullable = false, length = 25)
    private String appNo;

    @Column(name = "STD_NO", nullable = false, length = 25)
    private String stdNo;

    @Column(name = "LINE_TYPE", nullable = false, length = 50)
    private String lineType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpstdestdmtId entity = (SpstdestdmtId) o;
        return Objects.equals(this.lineType, entity.lineType) &&
                Objects.equals(this.appNo, entity.appNo) &&
                Objects.equals(this.stdNo, entity.stdNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineType, appNo, stdNo);
    }

}