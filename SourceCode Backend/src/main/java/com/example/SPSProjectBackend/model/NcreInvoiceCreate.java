package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "ncre_invoice_create", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(NcreInvoiceCreateId.class)
public class NcreInvoiceCreate {

    @Id
    @Column(name = "bill_cycle", nullable = false)
    private Integer billCycle;

    @Id
    @Column(name = "folio_no", nullable = false)
    private Integer folioNo;

    @Column(name = "is_create", columnDefinition = "SMALLINT DEFAULT 1")
    private Integer isCreate;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public void setIsCreate(Short isCreate) {
        this.isCreate = isCreate != null ? isCreate.intValue() : null;
    }

    public void setIsCreate(Integer isCreate) {
        this.isCreate = isCreate;
    }

    public void setCreatedDate(LocalDate date) {
        this.createdDate = (date != null) ? java.sql.Date.valueOf(date) : null;
    }

    public void setCreatedDate(Date date) {
        this.createdDate = date;
    }

    public LocalDate getCreatedDateAsLocalDate() {
        if (createdDate == null) return null;
        if (createdDate instanceof java.sql.Date) {
            return ((java.sql.Date) createdDate).toLocalDate();
        }
        return createdDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    public NcreInvoiceCreateId getId() {
        if (billCycle == null && folioNo == null) return null;
        return new NcreInvoiceCreateId(billCycle, folioNo);
    }

    public void setId(NcreInvoiceCreateId id) {
        if (id != null) {
            this.billCycle = id.getBillCycle();
            this.folioNo = id.getFolioNo();
        }
    }

    public static class NcreInvoiceCreateBuilder {
        public NcreInvoiceCreateBuilder id(NcreInvoiceCreateId id) {
            if (id != null) {
                this.billCycle = id.getBillCycle();
                this.folioNo = id.getFolioNo();
            }
            return this;
        }

        public NcreInvoiceCreateBuilder isCreate(Short isCreate) {
            this.isCreate = isCreate != null ? isCreate.intValue() : null;
            return this;
        }

        public NcreInvoiceCreateBuilder isCreate(Integer isCreate) {
            this.isCreate = isCreate;
            return this;
        }

        public NcreInvoiceCreateBuilder createdDate(LocalDate date) {
            this.createdDate = (date != null) ? java.sql.Date.valueOf(date) : null;
            return this;
        }

        public NcreInvoiceCreateBuilder createdDate(Date date) {
            this.createdDate = date;
            return this;
        }
    }
}

