package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PcesthmtDto;
import com.example.SPSProjectBackend.model.Pcesthmt;
import com.example.SPSProjectBackend.repository.JobRevisionHmtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetEstDataByProNoServiceImpl implements GetEstByProNo {
    private final JobRevisionHmtRepository estimateRepository;

    @Autowired
    public GetEstDataByProNoServiceImpl(JobRevisionHmtRepository estimateRepository) {
        this.estimateRepository = estimateRepository;
    }

    @Override
    public List<PcesthmtDto> getEstimatesByProjectNo(String projectNo) {
        List<Pcesthmt> entities = estimateRepository.findByProjectNo(projectNo);
        return entities.stream().map(entity -> new PcesthmtDto(
                entity.getId().getEstimateNo(),
                entity.getId().getRevNo(),
                entity.getId().getDeptId(),
                entity.getProjectNo(),
                entity.getClientNm(),
                entity.getDescr(),
                entity.getEtimateDt(),
                entity.getStdCost(),
                entity.getPaidAmt(),
                entity.getStatus(),
                entity.getRevReason()
        )).collect(Collectors.toList());
    }
}
