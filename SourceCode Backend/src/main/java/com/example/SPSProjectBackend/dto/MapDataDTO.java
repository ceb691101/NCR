package com.example.SPSProjectBackend.dto;

public interface MapDataDTO {
    String getAccNbr();
    String getAreaCd();
    String getCity();
    String getName();
    Double getLatitude();
    Double getLongitude();
    String getNcreCategory();
    String getFacilityName();   // from NcreDeveloper
    String getDeveloperName();  // from NcreDeveloper
    String getArea();           // from NcreDeveloper (province or area)
    String getAddressLine1();   // from NcreDeveloper
    String getAddressLine2();   // from NcreDeveloper
    String getAddressLine3();   
    Short getFolioNo();         // from NcreDeveloper
    String getResponsibleEe();  // from NcreDeveloper
}