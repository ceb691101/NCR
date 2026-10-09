package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "jnal_typs", schema = "dbadmin")
public class JournalType {

    @Id
    @Column(name = "jnl_type", length = 4, nullable = false)
    private String jnlType;

    @Column(name = "jnl_desc", length = 40)
    private String jnlDesc;

    @Column(name = "mltple_ent", length = 1)
    private String mltpleEnt;

    @Column(name = "nrmal_edit", length = 1)
    private String nrmalEdit;

    @Column(name = "aval_amnd", length = 1)
    private String avalAmnd;

    @Column(name = "amt_stat", length = 2)
    private String amtStat;

    @Column(name = "jnlt_status", length = 1)
    private String jnltStatus;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private LocalDateTime enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private LocalDateTime editedDtime;
}
