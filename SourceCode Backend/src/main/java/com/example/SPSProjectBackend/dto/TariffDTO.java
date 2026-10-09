package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TariffDTO {
    private Short tariff;
    private Character rateStatus;
    private Character recordStatus;
    private Date fromDate;
    private Date toDate;
    private BigDecimal minCharge;
    private Short numberOfSlabs;

    // Slab 1
    private Short limit1;
    private BigDecimal rate1;
    private BigDecimal fixedCharge1;
    private BigDecimal fuelCharge1;
    private BigDecimal serviceCharge1;
    private Character fuelStatus1;
    private BigDecimal discount1;

    // Slab 2
    private Short limit2;
    private BigDecimal rate2;
    private BigDecimal fixedCharge2;
    private BigDecimal fuelCharge2;
    private BigDecimal serviceCharge2;
    private Character fuelStatus2;
    private BigDecimal discount2;

    // Slab 3
    private Short limit3;
    private BigDecimal rate3;
    private BigDecimal fixedCharge3;
    private BigDecimal fuelCharge3;
    private BigDecimal serviceCharge3;
    private Character fuelStatus3;
    private BigDecimal discount3;

    // Slab 4
    private Short limit4;
    private BigDecimal rate4;
    private BigDecimal fixedCharge4;
    private BigDecimal fuelCharge4;
    private BigDecimal serviceCharge4;
    private Character fuelStatus4;
    private BigDecimal discount4;

    // Slab 5
    private Short limit5;
    private BigDecimal rate5;
    private BigDecimal fixedCharge5;
    private BigDecimal fuelCharge5;
    private BigDecimal serviceCharge5;
    private Character fuelStatus5;
    private BigDecimal discount5;

    // Slab 6
    private Short limit6;
    private BigDecimal rate6;
    private BigDecimal fixedCharge6;
    private BigDecimal fuelCharge6;
    private BigDecimal serviceCharge6;
    private Character fuelStatus6;
    private BigDecimal discount6;

    // Slab 7
    private Short limit7;
    private BigDecimal rate7;
    private BigDecimal fixedCharge7;
    private BigDecimal fuelCharge7;
    private BigDecimal serviceCharge7;
    private Character fuelStatus7;
    private BigDecimal discount7;

    // Slab 8
    private Short limit8;
    private BigDecimal rate8;
    private BigDecimal fixedCharge8;
    private BigDecimal fuelCharge8;
    private BigDecimal serviceCharge8;
    private Character fuelStatus8;
    private BigDecimal discount8;

    // Slab 9
    private Short limit9;
    private BigDecimal rate9;
    private BigDecimal fixedCharge9;
    private BigDecimal fuelCharge9;
    private BigDecimal serviceCharge9;
    private Character fuelStatus9;
    private BigDecimal discount9;
}