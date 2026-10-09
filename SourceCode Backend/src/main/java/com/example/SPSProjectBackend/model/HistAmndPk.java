package com.example.SPSProjectBackend.model;

import java.io.Serializable;
import java.util.Objects;

public class HistAmndPk implements Serializable {
    private String accNbr;
    private String amdType;
    private String effctBlcy; // char(3)

    public HistAmndPk() {}

    public HistAmndPk(String accNbr, String amdType, String effctBlcy) {
        this.accNbr = accNbr;
        this.amdType = amdType;
        this.effctBlcy = effctBlcy;
    }

    // Getters, Setters, equals, hashCode
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

    public String getEffctBlcy() {
        return effctBlcy;
    }

    public void setEffctBlcy(String effctBlcy) {
        this.effctBlcy = effctBlcy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistAmndPk that = (HistAmndPk) o;
        return Objects.equals(accNbr, that.accNbr) &&
                Objects.equals(amdType, that.amdType) &&
                Objects.equals(effctBlcy, that.effctBlcy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNbr, amdType, effctBlcy);
    }
}