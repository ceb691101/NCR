package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class MeterDetailsDTO {
    private String jobNumber;  // Regular field (no longer primary key)
    private String accountNumber; // Now this will be used as primary key in entity
    private String areaCode;
    private String addedBillingCycle;
    private Integer meterSequence;
    private Short setType;
    private Short meterOrder;
    private String meterType;
    private String numberOfPhases;
    private Integer numberOfMeterSets;
    private Short meter1Set;
    private Short meter2Set;
    private Short meter3Set;
    private String meterNumber;
    private Integer presentReading;
    private BigDecimal multiplicationFactor;
    private String ctRatio;
    private String meterRatio;
    private String effectiveBillingCycle;
    private Date effectiveDate;
    private String trCb;
    private String dpCode;
    private String brCode;
    private String connectionTransformerPanel;
    private BigDecimal transformerVoltage;
    private BigDecimal transformerAmps;
    private String userId;
    private Date enteredDateTime;
    private String editedUserId;
    private Date editedDateTime;
}