package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "tariff")
@IdClass(Tariff.TariffId.class)  // ✅ COMPOSITE PRIMARY KEY
public class Tariff {

    // ================================================================
    // PRIMARY KEY FIELDS
    // ================================================================
    @Id
    @Column(name = "tariff")
    private Short tariff;

    @Id
    @Column(name = "frm_date")
    @Temporal(TemporalType.DATE)
    private Date frmDate;

    // ================================================================
    // BASIC FIELDS
    // ================================================================
    @Column(name = "rate_status", length = 1)
    private Character rateStatus;

    @Column(name = "record_status", length = 1)
    private Character recordStatus;

    @Column(name = "to_date")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column(name = "min_charg", precision = 5, scale = 2)
    private BigDecimal minCharge;

    @Column(name = "no_of_slbs")
    private Short numberOfSlabs;

    // ================================================================
    // SLAB 1
    // ================================================================
    @Column(name = "lmt1")
    private Short limit1;

    @Column(name = "rate1", precision = 5, scale = 2)
    private BigDecimal rate1;

    @Column(name = "fxdchg1", precision = 6, scale = 2)
    private BigDecimal fixedCharge1;

    @Column(name = "fu_chg1", precision = 5, scale = 2)
    private BigDecimal fuelCharge1;

    @Column(name = "st_chg1", precision = 5, scale = 2)
    private BigDecimal serviceCharge1;

    @Column(name = "fu_st1", length = 1)
    private Character fuelStatus1;

    @Column(name = "discnt1", precision = 5, scale = 2)
    private BigDecimal discount1;

    // ================================================================
    // SLAB 2
    // ================================================================
    @Column(name = "lmt2")
    private Short limit2;

    @Column(name = "rate2", precision = 5, scale = 2)
    private BigDecimal rate2;

    @Column(name = "fxdchg2", precision = 6, scale = 2)
    private BigDecimal fixedCharge2;

    @Column(name = "fu_chg2", precision = 5, scale = 2)
    private BigDecimal fuelCharge2;

    @Column(name = "st_chg2", precision = 5, scale = 2)
    private BigDecimal serviceCharge2;

    @Column(name = "fu_st2", length = 1)
    private Character fuelStatus2;

    @Column(name = "discnt2", precision = 5, scale = 2)
    private BigDecimal discount2;

    // ================================================================
    // SLAB 3
    // ================================================================
    @Column(name = "lmt3")
    private Short limit3;

    @Column(name = "rate3", precision = 5, scale = 2)
    private BigDecimal rate3;

    @Column(name = "fxdchg3", precision = 6, scale = 2)
    private BigDecimal fixedCharge3;

    @Column(name = "fu_chg3", precision = 5, scale = 2)
    private BigDecimal fuelCharge3;

    @Column(name = "st_chg3", precision = 5, scale = 2)
    private BigDecimal serviceCharge3;

    @Column(name = "fu_st3", length = 1)
    private Character fuelStatus3;

    @Column(name = "discnt3", precision = 5, scale = 2)
    private BigDecimal discount3;

    // ================================================================
    // SLAB 4
    // ================================================================
    @Column(name = "lmt4")
    private Short limit4;

    @Column(name = "rate4", precision = 5, scale = 2)
    private BigDecimal rate4;

    @Column(name = "fxdchg4", precision = 6, scale = 2)
    private BigDecimal fixedCharge4;

    @Column(name = "fu_chg4", precision = 5, scale = 2)
    private BigDecimal fuelCharge4;

    @Column(name = "st_chg4", precision = 5, scale = 2)
    private BigDecimal serviceCharge4;

    @Column(name = "fu_st4", length = 1)
    private Character fuelStatus4;

    @Column(name = "discnt4", precision = 5, scale = 2)
    private BigDecimal discount4;

    // ================================================================
    // SLAB 5
    // ================================================================
    @Column(name = "lmt5")
    private Short limit5;

    @Column(name = "rate5", precision = 5, scale = 2)
    private BigDecimal rate5;

    @Column(name = "fxdchg5", precision = 6, scale = 2)
    private BigDecimal fixedCharge5;

    @Column(name = "fu_chg5", precision = 5, scale = 2)
    private BigDecimal fuelCharge5;

    @Column(name = "st_chg5", precision = 5, scale = 2)
    private BigDecimal serviceCharge5;

    @Column(name = "fu_st5", length = 1)
    private Character fuelStatus5;

    @Column(name = "discnt5", precision = 5, scale = 2)
    private BigDecimal discount5;

    // ================================================================
    // SLAB 6
    // ================================================================
    @Column(name = "lmt6")
    private Short limit6;

    @Column(name = "rate6", precision = 5, scale = 2)
    private BigDecimal rate6;

    @Column(name = "fxdchg6", precision = 6, scale = 2)
    private BigDecimal fixedCharge6;

    @Column(name = "fu_chg6", precision = 5, scale = 2)
    private BigDecimal fuelCharge6;

    @Column(name = "st_chg6", precision = 5, scale = 2)
    private BigDecimal serviceCharge6;

    @Column(name = "fu_st6", length = 1)
    private Character fuelStatus6;

    @Column(name = "discnt6", precision = 5, scale = 2)
    private BigDecimal discount6;

    // ================================================================
    // SLAB 7
    // ================================================================
    @Column(name = "lmt7")
    private Short limit7;

    @Column(name = "rate7", precision = 5, scale = 2)
    private BigDecimal rate7;

    @Column(name = "fxdchg7", precision = 6, scale = 2)
    private BigDecimal fixedCharge7;

    @Column(name = "fu_chg7", precision = 5, scale = 2)
    private BigDecimal fuelCharge7;

    @Column(name = "st_chg7", precision = 5, scale = 2)
    private BigDecimal serviceCharge7;

    @Column(name = "fu_st7", length = 1)
    private Character fuelStatus7;

    @Column(name = "discnt7", precision = 5, scale = 2)
    private BigDecimal discount7;

    // ================================================================
    // SLAB 8
    // ================================================================
    @Column(name = "lmt8")
    private Short limit8;

    @Column(name = "rate8", precision = 5, scale = 2)
    private BigDecimal rate8;

    @Column(name = "fxdchg8", precision = 6, scale = 2)
    private BigDecimal fixedCharge8;

    @Column(name = "fu_chg8", precision = 5, scale = 2)
    private BigDecimal fuelCharge8;

    @Column(name = "st_chg8", precision = 5, scale = 2)
    private BigDecimal serviceCharge8;

    @Column(name = "fu_st8", length = 1)
    private Character fuelStatus8;

    @Column(name = "discnt8", precision = 5, scale = 2)
    private BigDecimal discount8;

    // ================================================================
    // SLAB 9
    // ================================================================
    @Column(name = "lmt9")
    private Short limit9;

    @Column(name = "rate9", precision = 5, scale = 2)
    private BigDecimal rate9;

    @Column(name = "fxdchg9", precision = 6, scale = 2)
    private BigDecimal fixedCharge9;

    @Column(name = "fu_chg9", precision = 5, scale = 2)
    private BigDecimal fuelCharge9;

    @Column(name = "st_chg9", precision = 5, scale = 2)
    private BigDecimal serviceCharge9;

    @Column(name = "fu_st9", length = 1)
    private Character fuelStatus9;

    @Column(name = "discnt9", precision = 5, scale = 2)
    private BigDecimal discount9;

    // ================================================================
    // COMPOSITE PRIMARY KEY CLASS
    // ================================================================
    public static class TariffId implements Serializable {
        private Short tariff;
        private Date frmDate;

        // Default constructor
        public TariffId() {}

        // Parameterized constructor
        public TariffId(Short tariff, Date frmDate) {
            this.tariff = tariff;
            this.frmDate = frmDate;
        }

        // Getters and setters
        public Short getTariff() { 
            return tariff; 
        }
        
        public void setTariff(Short tariff) { 
            this.tariff = tariff; 
        }
        
        public Date getFrmDate() { 
            return frmDate; 
        }
        
        public void setFrmDate(Date frmDate) { 
            this.frmDate = frmDate; 
        }

        // equals() - REQUIRED for composite key
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TariffId that = (TariffId) o;
            return Objects.equals(tariff, that.tariff) && 
                   Objects.equals(frmDate, that.frmDate);
        }

        // hashCode() - REQUIRED for composite key
        @Override
        public int hashCode() {
            return Objects.hash(tariff, frmDate);
        }
    }
}