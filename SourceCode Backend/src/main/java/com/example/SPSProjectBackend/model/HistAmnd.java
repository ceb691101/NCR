package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "hist_amnds")
public class HistAmnd {
    @EmbeddedId
    private HistAmndPk id; // Composite key: acc_nbr + amd_type + effct_blcy (as String)

    @Column(name = "added_blcy", length = 3)
    private String addedBlcy;

    @Column(name = "effct_date")
    @Temporal(TemporalType.DATE)
    private Date effctDate;

    @Column(name = "old_nmr_value", precision = 11, scale = 2)
    private BigDecimal oldNmrValue;

    @Column(name = "new_nmr_value", precision = 11, scale = 2)
    private BigDecimal newNmrValue;

    @Column(name = "old_chr_value", length = 20)
    private String oldChrValue;

    @Column(name = "new_chr_value", length = 20)
    private String newChrValue;

    @Column(name = "amauth_code", length = 3)
    private String amauthCode;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private Timestamp enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private Timestamp editedDtime;
}