package com.example.SPSProjectBackend.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite Primary Key for TmpPayment entity
 * Based on: agent_code + cent_code + acc_nbr + stub_no
 */
public class TmpPaymentId implements Serializable {
    
    private String agentCode;
    private String centCode;
    private String accNbr;
    private Short stubNo;
    
    // Default constructor
    public TmpPaymentId() {
    }
    
    public TmpPaymentId(String agentCode, String centCode, String accNbr, Short stubNo) {
        this.agentCode = agentCode;
        this.centCode = centCode;
        this.accNbr = accNbr;
        this.stubNo = stubNo;
    }
    
    // Getters and Setters
    public String getAgentCode() {
        return agentCode;
    }
    
    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }
    
    public String getCentCode() {
        return centCode;
    }
    
    public void setCentCode(String centCode) {
        this.centCode = centCode;
    }
    
    public String getAccNbr() {
        return accNbr;
    }
    
    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr;
    }
    
    public Short getStubNo() {
        return stubNo;
    }
    
    public void setStubNo(Short stubNo) {
        this.stubNo = stubNo;
    }
    
    // equals and hashCode are REQUIRED for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TmpPaymentId that = (TmpPaymentId) o;
        return Objects.equals(agentCode, that.agentCode) &&
               Objects.equals(centCode, that.centCode) &&
               Objects.equals(accNbr, that.accNbr) &&
               Objects.equals(stubNo, that.stubNo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(agentCode, centCode, accNbr, stubNo);
    }
}
