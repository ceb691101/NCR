package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class AmndTypeDTO {
    private String amdType;
    private String amdDesc;
    private String uptblName;
    private String fieldName;
    private String nvalTblnm;
    private String nvalFldnm;
    private String dtType;
    private String valJrnl;
    private String amdtStatus;
    private String userId;
    private Timestamp enteredDtime;
    private String editedUserId;
    private Timestamp editedDtime;
}