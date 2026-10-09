package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JurnlAuthDTO {
    private String authCode;
    private String desig;
    private String name;
}
