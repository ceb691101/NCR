package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "WIRING_LAND_DETAIL_CON")
public class WiringLandDetailCon {
    @EmbeddedId
    private WiringLandDetailConId id;

    @Column(name = "CON_REF_NO", length = 100)
    private String conRefNo;

    @Column(name = "AREA_CODE", length = 60)
    private String areaCode;

    @Column(name = "FUND_SOURCE", length = 10)
    private String fundSource;

    @Column(name = "REPRESENTATIVE", length = 200)
    private String representative;

    @Column(name = "SCHEME_NO", length = 400)
    private String schemeNo;

    @Column(name = "SCHEME_NAME", length = 500)
    private String schemeName;

    @Column(name = "IS_ELECTRICITY_HAVING")
    private Boolean isElectricityHaving;

    @Column(name = "DES_OF_PREMISES", length = 50)
    private String desOfPremises;

    @Column(name = "ELECTORATE", length = 50)
    private String electorate;

    @Column(name = "DEV_SEC", length = 50)
    private String devSec;

    @Column(name = "GS_DIVISION", length = 50)
    private String gsDivision;

    @Column(name = "AGA_DIVISION", length = 50)
    private String agaDivision;

    @Column(name = "NO_OF_METER_POINT")
    private Short noOfMeterPoint;

    @Column(name = "CAP_OF_SERVICE")
    private Short capOfService;

    @Column(name = "REPRESENTATIVE2", length = 50)
    private String representative2;

    @Column(name = "REPCONTACT", length = 100)
    private String repcontact;

    @Column(name = "REP2CONTACT", length = 200)
    private String rep2contact;

    @Column(name = "SERVICEDEPONAME", length = 50)
    private String servicedeponame;

    @Column(name = "DISTRICT", length = 50)
    private String district;

    @Column(name = "ACCOUNT_NOS", length = 100)
    private String accountNos;

}