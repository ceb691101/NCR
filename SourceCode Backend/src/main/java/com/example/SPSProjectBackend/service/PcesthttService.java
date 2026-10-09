package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.repository.PcesthttRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PcesthttService {

    @Autowired
    private PcesthttRepository pcesthttRepository;

    public Map<Short, Long> getRowCountByStatus() {
        List<Object[]> results = pcesthttRepository.getRowCountByStatus();
        Map<Short, Long> rowCountByStatus = new HashMap<>();
        for (Object[] result : results) {
            Short status = (Short) result[0];
            Long totalRows = (Long) result[1];
            rowCountByStatus.put(status, totalRows);
        }
        return rowCountByStatus;
    }
}