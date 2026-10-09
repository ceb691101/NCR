package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "tmp_mtr_details", schema = "dbadmin")
public class MeterDetails {

    @Id
    @Column(name = "acc_nbr", length = 10)
    private String accountNumber; // Now this is the primary key

    @Column(name = "job_nbr", length = 10)
    private String jobNumber; // Now this is a regular column

    @Column(name = "area_cd", length = 2)
    private String areaCode;

    @Column(name = "added_blcy", length = 3)
    private String addedBillingCycle;

    @Column(name = "mtr_seq")
    private Integer meterSequence;

    @Column(name = "set_type")
    private Short setType;

    @Column(name = "mtr_order")
    private Short meterOrder;

    @Column(name = "mtr_type", length = 6)
    private String meterType;

    @Column(name = "no_of_phases", length = 1)
    private String numberOfPhases;

    @Column(name = "no_mtr_sets")
    private Integer numberOfMeterSets;

    @Column(name = "mtr_1set")
    private Short meter1Set;

    @Column(name = "mtr_2set")
    private Short meter2Set;

    @Column(name = "mtr_3set")
    private Short meter3Set;

    @Column(name = "mtr_nbr", length = 10)
    private String meterNumber;

    @Column(name = "prsnt_rdn")
    private Integer presentReading;

    @Column(name = "m_factor", precision = 8, scale = 3)
    private BigDecimal multiplicationFactor;

    @Column(name = "ct_ratio", length = 12)
    private String ctRatio;

    @Column(name = "mtr_ratio", length = 12)
    private String meterRatio;

    @Column(name = "effct_blcy", length = 3)
    private String effectiveBillingCycle;

    @Column(name = "effct_date")
    @Temporal(TemporalType.DATE)
    private Date effectiveDate;

    @Column(name = "tr_cb", length = 2)
    private String trCb;

    @Column(name = "dp_code", length = 2)
    private String dpCode;

    @Column(name = "br_code", length = 2)
    private String brCode;

    @Column(name = "cnnct_trnpan", length = 10)
    private String connectionTransformerPanel;

    @Column(name = "trnsf_volt", precision = 7, scale = 2)
    private BigDecimal transformerVoltage;

    @Column(name = "trnsf_amps", precision = 7, scale = 2)
    private BigDecimal transformerAmps;

    @Column(name = "user_id", length = 9)
    private String userId;

    @Column(name = "entered_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDateTime;

    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;

    @Column(name = "edited_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedDateTime;
}