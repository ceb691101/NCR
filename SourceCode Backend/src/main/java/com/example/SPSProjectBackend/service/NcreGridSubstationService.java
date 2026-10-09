package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreGridSubstation;
import com.example.SPSProjectBackend.repository.NcreGridSubstationRepository;
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
public class NcreGridSubstationService {

    private final NcreGridSubstationRepository gridSubstationRepository;

    /**
     * Retrieve active NCRE grid substations from Informix database (dbadmin.ncre_grid_substation)
     * where status = 2. When a license code is supplied, only substations whose license_code
     * matches are returned (license_code corresponds to the selected region).
     *
     * @param licenseCode region license code filter, or null/blank for all active substations
     * @return clean list of active grid substation entities.
     */
    public List<NcreGridSubstation> getActiveGridSubstations(String licenseCode) {
        log.debug("Fetching active NCRE grid substations from repository (licenseCode = {})", licenseCode);
        List<NcreGridSubstation> rawSubstations = (licenseCode == null || licenseCode.trim().isEmpty())
                ? gridSubstationRepository.findActiveGridSubstations()
                : gridSubstationRepository.findActiveGridSubstationsByLicenceCode(licenseCode.trim());
        if (rawSubstations == null || rawSubstations.isEmpty()) {
            log.warn("No active grid substations found in database (status = 2)");
            return Collections.emptyList();
        }

        List<NcreGridSubstation> cleaned = rawSubstations.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getGssCode() != null && !t.getGssCode().trim().isEmpty())
                .filter(t -> t.getGssName() != null && !t.getGssName().trim().isEmpty())
                .collect(Collectors.toList());

        log.debug("Successfully retrieved {} active grid substations", cleaned.size());
        return cleaned;
    }
}