package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.repository.NcreTariffDescriptionRepository;
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
public class NcreTariffDescriptionService {

    private final NcreTariffDescriptionRepository tariffDescriptionRepository;

    /**
     * Retrieve active NCRE tariff descriptions from Informix database (dbadmin.ncre_tariff_desc) where status = '2'.
     *
     * @return clean, trimmed, non-empty, distinct, and sorted list of active tariff descriptions.
     */
    public List<String> getTariffDescriptions() {
        log.debug("Fetching active NCRE tariff descriptions from repository");
        List<String> rawDescriptions = tariffDescriptionRepository.findActiveTariffDescriptions();
        if (rawDescriptions == null || rawDescriptions.isEmpty()) {
            log.warn("No active tariff descriptions found in database (status = 2)");
            return Collections.emptyList();
        }

        List<String> cleaned = rawDescriptions.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.debug("Successfully retrieved {} active tariff descriptions", cleaned.size());
        return cleaned;
    }
}
