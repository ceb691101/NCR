package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ncre_developers", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreDeveloper {

    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Column(name = "folio_no")
    private Short folioNo;

    @Column(name = "file_ref_no", length = 50)
    private String fileRefNo;

    @Column(name = "sr_no_upto_date")
    private Integer srNoUptoDate;

    @Column(name = "tariff_type", length = 20, nullable = false)
    private String tariffType;

    @Column(name = "file_no")
    private Short fileNo;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "developer_name", length = 150, nullable = false)
    private String developerName;

    @Column(name = "facility_name", length = 150)
    private String facilityName;

    @Column(name = "commissioned_capacity_mw", precision = 10, scale = 3)
    private BigDecimal commissionedCapacityMw;

    @Column(name = "loi_issued")
    @Temporal(TemporalType.DATE)
    private Date loiIssued;

    @Column(name = "sppa_signed")
    @Temporal(TemporalType.DATE)
    private Date sppaSigned;

    @Column(name = "grid_connection_date")
    @Temporal(TemporalType.DATE)
    private Date gridConnectionDate;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "region", length = 10)
    private String region;

    @Column(name = "sr_no", length = 10)
    private String srNo;

    @Column(name = "area", length = 50)
    private String area;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "grid_substation", length = 100)
    private String gridSubstation;

    @Column(name = "initial_tariff", length = 50)
    private String initialTariff;

    @Column(name = "expiration_date")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;

    @Column(name = "expiration_extension_date")
    @Temporal(TemporalType.DATE)
    private Date expirationExtensionDate;

    @Column(name = "sppa_capacity_mw", precision = 10, scale = 3)
    private BigDecimal sppaCapacityMw;

    @Column(name = "feeder_no")
    private Short feederNo;

    @Column(name = "commissioned_year")
    private Short commissionedYear;

    @Column(name = "ac_expiration_with_extension")
    @Temporal(TemporalType.DATE)
    private Date acExpirationWithExtension;

    @Column(name = "ex")
    @Temporal(TemporalType.DATE)
    private Date ex;

    @Column(name = "ac")
    @Temporal(TemporalType.DATE)
    private Date ac;

    @Column(name = "flat")
    @Temporal(TemporalType.DATE)
    private Date flat;

    @Column(name = "ttt")
    @Temporal(TemporalType.DATE)
    private Date ttt;

    @Column(name = "first_tier")
    @Temporal(TemporalType.DATE)
    private Date firstTier;

    @Column(name = "second_tier")
    @Temporal(TemporalType.DATE)
    private Date secondTier;

    @Column(name = "third_tier")
    @Temporal(TemporalType.DATE)
    private Date thirdTier;

    @Column(name = "new_sppa_signed")
    @Temporal(TemporalType.DATE)
    private Date newSppaSigned;

    @Column(name = "validity_start")
    @Temporal(TemporalType.DATE)
    private Date validityStart;

    @Column(name = "validity_expiry")
    @Temporal(TemporalType.DATE)
    private Date validityExpiry;

    @Column(name = "initial_tariff_revised", precision = 10, scale = 2)
    private BigDecimal initialTariffRevised;

    @Column(name = "recommissioned_on")
    @Temporal(TemporalType.DATE)
    private Date recommissionedOn;

    @Column(name = "ep_expired")
    @Temporal(TemporalType.DATE)
    private Date epExpired;

    @Column(name = "gl_expired")
    @Temporal(TemporalType.DATE)
    private Date glExpired;

    @Column(name = "project_status", length = 50)
    private String projectStatus;

    @Column(name = "voltage_level_kv", precision = 10, scale = 2)
    private BigDecimal voltageLevelKv;

    @Column(name = "address_line1", length = 200, nullable = true)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "address_line3", length = 200)
    private String addressLine3;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "telephone", length = 50)
    private String telephone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gps_coordinates", length = 100)
    private String gpsCoordinates;

    @Column(name = "company_group", length = 100)
    private String companyGroup;

    @Column(name = "tendered_or_not", length = 20)
    private String tenderedOrNot;

    @Column(name = "reductions", length = 50)
    private String reductions;

    @Column(name = "agreement_type", length = 100)
    private String agreementType;

    @Column(name = "longitude")
    private Double longitude;
    // private BigDecimal longitude;

    @Column(name = "latitude")
    private Double latitude;
    // private BigDecimal latitude;

    @Column(name = "generation_losses")
    private BigDecimal generationLosses;

    @Column(name = "payment_deductions")
    private BigDecimal paymentDeductions;

    @Column(name = "cus_cat", length = 1)
    private String cusCat;

    @Column(name = "mtr_nbr", length = 10)
    private String mtrNbr;

    @Column(name = "responsble_ee", length = 100)
    private String responsibleEe;

    @Column(name = "accept_ru")
    private Short acceptRu;

    @Column(name = "status", length = 1)
    private String status;
}
