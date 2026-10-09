package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreInvRdngsId implements Serializable {
    private String accNbr;
    private String areaCd;
    private String addedBlcy;
    private String mtrNbr;

    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr != null ? accNbr.trim() : null;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd != null ? areaCd.trim() : null;
    }

    public void setAddedBlcy(String addedBlcy) {
        this.addedBlcy = addedBlcy != null ? addedBlcy.trim() : null;
    }

    public void setMtrNbr(String mtrNbr) {
        this.mtrNbr = mtrNbr != null ? mtrNbr.trim() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NcreInvRdngsId that = (NcreInvRdngsId) o;
        return Objects.equals(accNbr, that.accNbr) &&
               Objects.equals(areaCd, that.areaCd) &&
               Objects.equals(addedBlcy, that.addedBlcy) &&
               Objects.equals(mtrNbr, that.mtrNbr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accNbr, areaCd, addedBlcy, mtrNbr);
    }
}
