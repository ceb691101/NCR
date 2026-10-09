package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity

@Table(name = "SPSUSERS")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SPSUSERS_SEQ")
    @SequenceGenerator(name = "SPSUSERS_SEQ", sequenceName = "SPSUSERS_SEQ", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "EACCOUNT_NO")
    private String eAccountNo;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "MOBILE")
    private Integer mobile;

    @Column(name = "ENABLED")
    private boolean enabled = false;

    @Column(name = "VERIFICATION_CODE", length = 50)
    private String verificationCode;

    @Column(name = "VERIFIED")
    private boolean verified = false;

    @Column(name = "USER_LEVEL")
    private String userlevel;

    @Column(name = "DEPTID")
    private String deptId;

    @Column(name = "NICNO")
    private String nicno;

    @Column(name = "FIRST_NAME")
    private String fname;

    @Column(name = "LAST_NAME")
    private String lname;

    // Getters and setters...
    public String getEAccountNo() {
        return eAccountNo;
    }

    public void setEAccountNo(String eAccountNo) {
        this.eAccountNo = eAccountNo;
    }
}