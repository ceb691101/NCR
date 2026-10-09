package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "jurnl_auth", schema = "dbadmin")
public class JurnlAuth {

    @Id
    @Column(name = "auth_code", length = 3, nullable = false)
    private String authCode;

    @Column(name = "desig", length = 20)
    private String desig;

    @Column(name = "app_lmt", precision = 13, scale = 2)
    private BigDecimal appLmt;

    @Column(name = "name", length = 25)
    private String name;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "ac_status", length = 1)
    private String acStatus;

    @Column(name = "user_id", length = 9)
    private String userId;

    @Column(name = "entered_dtime")
    private LocalDateTime enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private LocalDateTime editedDtime;
}
