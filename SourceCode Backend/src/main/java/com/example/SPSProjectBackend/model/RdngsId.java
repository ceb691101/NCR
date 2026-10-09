package com.example.SPSProjectBackend.model;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RdngsId implements Serializable {
    private String accNbr;
    private String addedBlcy;
    private Short mtrSeq;
    private String mtrType;
}
