package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(
        name = "inst_info",
        indexes = {

                // Non-unique indexes for filtering/searching
                @Index(name = "idx_inst_info_cust_num", columnList = "cust_num"),
                @Index(name = "idx_inst_info_dp_code", columnList = "dp_code"),

                // Composite index for faster queries on both together (not unique)
                @Index(name = "idx_inst_info_cust_dp", columnList = "cust_num, dp_code")
        }
)
public class InstInfo {

    @Id
    @Column(name = "inst_id", length = 8, nullable = false)
    private String instId;

    @Column(name = "date_cnnct")
    private Date dateCnnct;

    @Column(name = "mtr_set")
    private Short mtrSet;

    @Column(name = "nbr_met")
    private Short nbrMet;

    @Column(name = "cust_num", length = 2)
    private String custNum;

    @Column(name = "type_met", length = 2)
    private String typeMet;

    @Column(name = "dp_code", length = 2)
    private String dpCode;

    @Column(name = "tr_cb", length = 2)
    private String trCb;

    @Column(name = "cnnct_trpnl", length = 10)
    private String cnnctTrpnl;

    @Column(name = "trpnl_volt", precision = 7, scale = 2)
    private BigDecimal trpnlVolt;

    @Column(name = "trpnl_amps", precision = 7, scale = 2)
    private BigDecimal trpnlAmps;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private Timestamp enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private Timestamp editedDtime;
}
