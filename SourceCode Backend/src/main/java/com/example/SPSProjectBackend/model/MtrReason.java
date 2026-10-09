package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "mtr_reason")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MtrReason {

    @Id
    @Column(name = "rsn_code", length = 2)
    private String rsnCode;

    @Column(name = "rsn_desc", length = 20)
    private String rsnDesc;

    @Column(name = "rsn_status", length = 1)
    private String rsnStatus;

    @Column(name = "user_id", length = 8)
    private String userId;

    @Column(name = "entered_dtime")
    private Date enteredDtime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    private Date editedDtime;
}
