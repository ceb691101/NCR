package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.CostCenterJobStatusDto;
import com.example.SPSProjectBackend.repository.CostCenterJobStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class CostCenterJobStatusService {

    private final CostCenterJobStatusRepository repository;

    public CostCenterJobStatusService(CostCenterJobStatusRepository repository) {
        this.repository = repository;
    }

    public CostCenterJobStatusDto getStatusSummaryForCostCenter(String deptId) {
        return repository.getJobStatusCountsByDeptId(deptId);
    }
}
