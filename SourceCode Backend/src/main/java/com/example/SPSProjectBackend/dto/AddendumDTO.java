package com.example.SPSProjectBackend.dto;

import lombok.Data;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class AddendumDTO {
    private String developerName;
    private Date newSppaSignedDate;
    private String initialTariffRevised;
    private Date expirationExtensionDate;
    private Date recommissionedOn;
    private java.util.List<String> addendumFileNames;
}
