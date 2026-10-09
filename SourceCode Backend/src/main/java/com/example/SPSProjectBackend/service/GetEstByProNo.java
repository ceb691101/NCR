package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PcesthmtDto;
import com.example.SPSProjectBackend.model.Pcesthmt;

import java.util.List;

public interface GetEstByProNo {
    List<PcesthmtDto> getEstimatesByProjectNo(String projectNo);
}
