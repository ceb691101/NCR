package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_status_history", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceStatusHistory {

    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "invoice_id", nullable = false)
    private Short invoiceId;

    @Column(name = "status_from", length = 20)
    private String statusFrom;

    @Column(name = "status_to", length = 20, nullable = false)
    private String statusTo;

    @Column(name = "changed_by", length = 50, nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "remarks", length = 1000)
    private String remarks;
}
