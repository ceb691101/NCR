package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.CostCenterJobStatusDto;
import com.example.SPSProjectBackend.dto.JobReviseDropDownDTO;
import com.example.SPSProjectBackend.dto.JobRevisionDTO;
import com.example.SPSProjectBackend.dto.PcestdmtDto;
import com.example.SPSProjectBackend.model.Pcestdmt;
import com.example.SPSProjectBackend.model.PcestdmtId;
import com.example.SPSProjectBackend.model.Pcesthmt;
import com.example.SPSProjectBackend.repository.CostCenterJobStatusRepository;
import com.example.SPSProjectBackend.repository.JobRevisionDmtRepository;
import com.example.SPSProjectBackend.repository.JobRevisionHmtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobRevisionService {

    private final JobRevisionHmtRepository pcesthmtRepository;

    public JobRevisionService(JobRevisionHmtRepository pcesthmtRepository) {
        this.pcesthmtRepository = pcesthmtRepository;
    }

    @Transactional
    public Pcesthmt saveEstimateWithDetails(JobRevisionDTO jobRevisionDTO) {
        Pcesthmt header = jobRevisionDTO.getHeader();
        header.getDetails().forEach(detail -> detail.setPcesthmt(header)); // Link details to header
        return pcesthmtRepository.save(header); // Save header and details (cascading)
    }

    public JobReviseDropDownDTO getDropdownData() {
        JobReviseDropDownDTO dto = new JobReviseDropDownDTO();
        dto.setProjectNos(pcesthmtRepository.findDistinctProNo());
        return dto;
    }

    @Autowired
    private JobRevisionDmtRepository pcestdmtRepository;

    public List<PcestdmtDto> getDmtRecordsByProjectNo(String projectNo) {
        List<Pcestdmt> entities = pcestdmtRepository.findAllByProjectNo(projectNo);
        return entities.stream()
                .map(PcestdmtDto::new)
                .collect(Collectors.toList());
    }

}

