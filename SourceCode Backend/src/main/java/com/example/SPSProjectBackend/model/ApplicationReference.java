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
@Table(name = "APPLICATION_REFERENCE")

public class ApplicationReference {
    @EmbeddedId
    private ApplicationReferenceId id;

    @Column(name = "APPLICATION_NO", nullable = false, length = 21)
    private String applicationNo;

    @Column(name = "STATUS", length = 2)
    private String status;

    @Column(name = "ID_NO", length = 12)
    private String idNo;

    @Column(name = "PROJECTNO", length = 21)
    private String projectno;

    @Column(name = "CONSRUCTOR_ID", length = 4)
    private String consructorId;

    @Column(name = "POSTED_BY", length = 8)
    private String postedBy;

    @Column(name = "POSTED_DATE")
    private LocalDate postedDate;

    @Column(name = "POSTED_TIME", length = 11)
    private String postedTime;

}