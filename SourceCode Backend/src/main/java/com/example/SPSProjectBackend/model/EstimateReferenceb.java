package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "ESTIMATE_REFERENCEBS")
public class EstimateReferenceb {
    @EmbeddedId
    private EstimateReferencebId id;

    @Column(name = "STATUS", length = 2)
    private String status;

    @Column(name = "ID_NO", length = 10)
    private String idNo;

    @Column(name = "PROJECTNO", length = 21)
    private String projectno;

    @Column(name = "ENTRY_BY", length = 10)
    private String entryBy;

    @Column(name = "POSTED_BY", length = 8)
    private String postedBy;

    @Column(name = "POSTED_DATE")
    private LocalDate postedDate;

    @Column(name = "POSTED_TIME", length = 11)
    private String postedTime;

    @Column(name = "FILE_REF", length = 100)
    private String fileRef;

}