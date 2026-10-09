package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreType;
import com.example.SPSProjectBackend.repository.NcreTypeRepository;
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
public class NcreTypeService {

    private final NcreTypeRepository ncreTypeRepository;

    /**
     * Retrieve active NCRE types from Informix database (dbadmin.ncre_type) where status = '2'.
     *
     * @return clean list of active NCRE type entities.
     */
    public List<NcreType> getActiveNcreTypes() {
        log.debug("Fetching active NCRE types from repository");
        List<NcreType> rawTypes = ncreTypeRepository.findActiveNcreTypes();
        if (rawTypes == null || rawTypes.isEmpty()) {
            log.warn("No active NCRE types found in database (status = 2)");
            return Collections.emptyList();
        }

        List<NcreType> cleaned = rawTypes.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTypeId() != null && !t.getTypeId().trim().isEmpty())
                .filter(t -> t.getTypeName() != null && !t.getTypeName().trim().isEmpty())
                .collect(Collectors.toList());

        log.debug("Successfully retrieved {} active NCRE types", cleaned.size());
        return cleaned;
    }
}
