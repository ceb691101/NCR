package com.example.SPSProjectBackend.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TmpAmndPk implements Serializable {
    private String accNbr; // char(10)
    private String amdType; // char(4)
    private Short effctBlcy; // smallint

    // Default constructor
    public TmpAmndPk() {}

    // Constructor for convenience
    public TmpAmndPk(String accNbr, String amdType, Short effctBlcy) {
        this.accNbr = accNbr;
        this.amdType = amdType;
        this.effctBlcy = effctBlcy;
    }

    // Getters and setters
    public String getAccNbr() {
        return accNbr;
    }

    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr;
    }

    public String getAmdType() {
        return amdType;
    }

    public void setAmdType(String amdType) {
        this.amdType = amdType;
    }

    public Short getEffctBlcy() {
        return effctBlcy;
    }

    public void setEffctBlcy(Short effctBlcy) {
        this.effctBlcy = effctBlcy;
    }

    // Required for composite keys: equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TmpAmndPk that = (TmpAmndPk) o;
        return Objects.equals(accNbr, that.accNbr) &&
                Objects.equals(amdType, that.amdType) &&
                Objects.equals(effctBlcy, that.effctBlcy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNbr, amdType, effctBlcy);
    }
}