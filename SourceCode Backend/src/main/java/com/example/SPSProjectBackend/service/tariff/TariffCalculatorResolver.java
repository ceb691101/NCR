package com.example.SPSProjectBackend.service.tariff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Resolves the appropriate TariffCalculator strategy based on the developer's tariff type.
 * Automatically discovers all Spring-managed TariffCalculator implementations.
 */
@Component
public class TariffCalculatorResolver {

    private final List<TariffCalculator> calculators;

    @Autowired
    public TariffCalculatorResolver(List<TariffCalculator> calculators) {
        this.calculators = calculators;
    }

    /**
     * Resolves the matching TariffCalculator for the specified tariff type.
     *
     * @param tariffType the tariff type string from ncre_developers
     * @return the matching TariffCalculator implementation
     * @throws ResponseStatusException if tariffType is missing or unsupported
     */
    public TariffCalculator resolve(String tariffType) {
        if (tariffType == null || tariffType.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tariff type is required but missing for developer.");
        }

        String cleanTariffType = tariffType.trim();
        return calculators.stream()
                .filter(c -> c.supports(cleanTariffType))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported tariff type: " + cleanTariffType));
    }
}
