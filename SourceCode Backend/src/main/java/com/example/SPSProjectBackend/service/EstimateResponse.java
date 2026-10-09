package com.example.SPSProjectBackend.service;

public class EstimateResponse {
    private boolean exists;
    private String etimateDt;
    private String prjAssDt;

    public EstimateResponse(boolean exists, String etimateDt,String prjAssDt) {
        this.exists = exists;
        this.etimateDt = etimateDt;
        this.prjAssDt = prjAssDt;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getEtimateDt() {
        return etimateDt;
    }

    public void setEtimateDt(String etimateDt) {
        this.etimateDt = etimateDt;
    }

    public String getPrjAssDt() {
        return prjAssDt;
    }

    public void setPrjAssDt(String prjAssDt) {
        this.prjAssDt = prjAssDt;
    }
}