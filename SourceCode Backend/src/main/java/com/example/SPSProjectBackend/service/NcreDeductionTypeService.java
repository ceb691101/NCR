package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreDeductionType;
import com.example.SPSProjectBackend.repository.NcreDeductionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NcreDeductionTypeService {

    private final NcreDeductionTypeRepository deductionTypeRepository;

    /**
     * Retrieve active NCRE payment deduction types from Informix database
     * (dbadmin.ncre_dedcution_type) where status = '2'.
     *
     * @return clean list of active payment deduction type entities.
     */
    public List<NcreDeductionType> getActiveDeductionTypes() {
        log.info("Fetching active payment deduction types from repository");
        List<NcreDeductionType> rawTypes = deductionTypeRepository.findActiveDeductionTypes();
        if (rawTypes == null || rawTypes.isEmpty()) {
            log.warn("No active payment deduction types found in database (status = '2')");
            return Collections.emptyList();
        }

        List<NcreDeductionType> cleaned = rawTypes.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getDedTypeNm() != null && !t.getDedTypeNm().trim().isEmpty())
                .collect(Collectors.toList());

        log.debug("Successfully retrieved {} active payment deduction types", cleaned.size());
        return cleaned;
    }
}