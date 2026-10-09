package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.StatusCountDTO;
import com.example.SPSProjectBackend.model.Applicant;
import com.example.SPSProjectBackend.model.Spstdesthmt;
import com.example.SPSProjectBackend.model.SpstdesthmtId;
import com.example.SPSProjectBackend.repository.SpstdesthmtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpstdesthmtService {

    private final SpstdesthmtRepository repository;

    public Spstdesthmt create(Spstdesthmt entity) {
        // Ensure EmbeddedId is constructed before save
        if (entity.getId() == null) {
            SpstdesthmtId id = new SpstdesthmtId();
            id.setAppNo(entity.getAppNo());
            id.setStdNo(entity.getStdNo());
            id.setDeptId(entity.getDeptId());
            entity.setId(id);
        }
        return repository.save(entity);
    }

    public Spstdesthmt update(Spstdesthmt entity) {
        // Ensure EmbeddedId is constructed before save
        if (entity.getId() == null) {
            SpstdesthmtId id = new SpstdesthmtId();
            id.setAppNo(entity.getAppNo());
            id.setStdNo(entity.getStdNo());
            id.setDeptId(entity.getDeptId());
            entity.setId(id);
        }
        return repository.save(entity);
    }

    public List<StatusCountDTO> getStatusCounts() {
        List<Object[]> results = repository.findStatusCountsNative();

        return results.stream()
                .map(record -> new StatusCountDTO(
                        record[0] != null ? new BigDecimal(record[0].toString()) : null,
                        ((Number) record[1]).longValue()
                ))
                .toList();
    }

    public Optional<Spstdesthmt> getById(SpstdesthmtId id) {
        return repository.findById(id);
    }

    public List<Spstdesthmt> getAll() {
        return repository.findAll();
    }

    public void delete(SpstdesthmtId id) {
        repository.deleteById(id);
    }

    public Optional<Spstdesthmt> getByAppNo(String appNo) {
        return repository.findFirstByAppNo(appNo);
    }

    public Optional<Applicant> getApplicantByAppNo(String appNo) {
        return repository.findApplicantByAppNo(appNo);
    }
}
