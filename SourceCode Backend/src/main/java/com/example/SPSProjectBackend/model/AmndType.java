package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "amnd_types")
public class AmndType {

    @Id
    @Column(name = "amd_type", length = 4, nullable = false)
    private String amdType;

    @Column(name = "amd_desc", length = 45)
    private String amdDesc;

    @Column(name = "uptbl_name", length = 25)
    private String uptblName;

    @Column(name = "field_name", length = 15)
    private String fieldName;

    @Column(name = "nval_tblnm", length = 25)
    private String nvalTblnm;

    @Column(name = "nval_fldnm", length = 15)
    private String nvalFldnm;

    @Column(name = "dt_type", length = 1)
    private String dtType;

    @Column(name = "val_jrnl", length = 1)
    private String valJrnl;

    @Column(name = "amdt_status", length = 1)
    private String amdtStatus;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private Timestamp enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private Timestamp editedDtime;
}