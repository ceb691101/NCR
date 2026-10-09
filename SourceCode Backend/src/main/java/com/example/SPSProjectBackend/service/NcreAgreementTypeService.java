package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreAgreementType;
import com.example.SPSProjectBackend.repository.NcreAgreementTypeRepository;
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
public class NcreAgreementTypeService {

    private final NcreAgreementTypeRepository agreementTypeRepository;

    /**
     * Retrieve active NCRE agreement types from Informix database (dbadmin.ncre_agree_type) where status = '2'.
     *
     * @return clean list of active NCRE agreement type entities.
     */
    public List<NcreAgreementType> getActiveAgreementTypes() {
        log.debug("Fetching active NCRE agreement types from repository");
        List<NcreAgreementType> rawTypes = agreementTypeRepository.findActiveAgreementTypes();
        if (rawTypes == null || rawTypes.isEmpty()) {
            log.warn("No active agreement types found in database (status = 2)");
            return Collections.emptyList();
        }

        List<NcreAgreementType> cleaned = rawTypes.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getAggTypeId() != null && !t.getAggTypeId().trim().isEmpty())
                .filter(t -> t.getAggTypeName() != null && !t.getAggTypeName().trim().isEmpty())
                .collect(Collectors.toList());

        log.debug("Successfully retrieved {} active agreement types", cleaned.size());
        return cleaned;
    }
}