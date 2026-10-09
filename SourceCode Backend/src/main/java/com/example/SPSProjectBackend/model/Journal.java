package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tmp_journals", schema = "dbadmin")
public class Journal {

    @EmbeddedId
    private JournalId id;

    @Column(name = "adjust_amt", precision = 13, scale = 2)
    private BigDecimal adjustAmt;

    @Column(name = "adjust_stat", length = 2)
    private String adjustStat;

    @Column(name = "auth_code", length = 3)
    private String authCode;

    @Column(name = "doc_attch", length = 1)
    private String docAttch;

    @Column(name = "jnl_date")
    private LocalDateTime jnlDate;

    @Column(name = "confirmed", length = 1)
    private String confirmed;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private LocalDateTime enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private LocalDateTime editedDtime;
}
