package com.example.SPSProjectBackend.dto;

import com.example.SPSProjectBackend.model.Pcestdmt;

public class PcestdmtDto {
        private String estimateNo;
        private String resCd;
        private String resType;
        private Integer resCat;
        private String uom;
        private Double estimateQty;
        private Double estimateCost;
        private Double unitPrice;

        // Constructors
        public PcestdmtDto(Pcestdmt dmt) {
            this.estimateNo = dmt.getId().getEstimateNo();
            this.resCd = dmt.getId().getResCd();
            this.resType = dmt.getResType();
            this.resCat = Math.toIntExact(dmt.getResCat());
            this.uom = dmt.getUom();
            this.estimateQty = Double.valueOf(dmt.getEstimateQty());
            this.estimateCost = Double.valueOf(dmt.getEstimateCost());
            this.unitPrice = Double.valueOf(dmt.getUnitPrice());
        }

        // Getters and Setters

    public String getEstimateNo() {
        return estimateNo;
    }

    public void setEstimateNo(String estimateNo) {
        this.estimateNo = estimateNo;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public Integer getResCat() {
        return resCat;
    }

    public void setResCat(Integer resCat) {
        this.resCat = resCat;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Double getEstimateQty() {
        return estimateQty;
    }

    public void setEstimateQty(Double estimateQty) {
        this.estimateQty = estimateQty;
    }

    public Double getEstimateCost() {
        return estimateCost;
    }

    public void setEstimateCost(Double estimateCost) {
        this.estimateCost = estimateCost;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}

