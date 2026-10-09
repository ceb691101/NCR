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
@Table(name = "SPESTCND")
public class Spestcnd {
    @EmbeddedId
    private SpestcndId id;

    @Column(name = "CONSUMER_NAME", length = 60)
    private String consumerName;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "SEAL_NO", length = 10)
    private String sealNo;

    @Column(name = "ALLOCATED_USER", length = 10)
    private String allocatedUser;

    @Column(name = "ALLOCATED_DATE")
    private LocalDate allocatedDate;

    @Column(name = "ALLOCATED_TIME", length = 11)
    private String allocatedTime;

    @Column(name = "FINISHED_USER", length = 10)
    private String finishedUser;

    @Column(name = "FINISHED_DATE")
    private LocalDate finishedDate;

    @Column(name = "FINISHED_TIME", length = 11)
    private String finishedTime;

    @Column(name = "STATUS", length = 2)
    private String status;

    @Column(name = "IS_EXPORTED")
    private Boolean isExported;

    @Column(name = "ERROR_MSG", length = 150)
    private String errorMsg;

    @Column(name = "ESTIMATE_NO", length = 20)
    private String estimateNo;

}