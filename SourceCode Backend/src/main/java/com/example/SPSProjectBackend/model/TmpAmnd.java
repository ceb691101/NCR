package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(
        name = "tmp_amnds",
        indexes = {
                @Index(name = "idx_tmp_amnds_status", columnList = "status"),
                @Index(name = "idx_tmp_amnds_status_area", columnList = "status, area_cd"),
                @Index(name = "idx_tmp_amnds_amd_type", columnList = "amd_type")
        }
)
@DynamicInsert
public class TmpAmnd {
    @EmbeddedId
    private TmpAmndPk id; // Composite key: acc_nbr + amd_type + effct_blcy

    @Column(name = "area_cd", length = 2)
    private String areaCd;

    @Column(name = "added_blcy")
    private Short addedBlcy;

    @Column(name = "effct_date")
    @Temporal(TemporalType.DATE)
    private Date effctDate;

    @Column(name = "nmr_value", precision = 11, scale = 2)
    private BigDecimal nmrValue;

    @Column(name = "chr_value", length = 20)
    private String chrValue;

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

    @Column(name = "status", nullable = false)
    private String status; // char(1) in DDL, but Short in code
}