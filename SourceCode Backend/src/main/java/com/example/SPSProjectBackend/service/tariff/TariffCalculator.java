package com.example.SPSProjectBackend.service.tariff;

/**
 * Strategy interface for tariff calculation.
 * Implementations encapsulate the rate lookup and calculation logic for specific tariff types.
 */
public interface TariffCalculator {

    /**
     * Determines whether this calculator supports the given tariff type.
     *
     * @param tariffType the tariff type string from developer configuration
     * @return true if supported, false otherwise
     */
    boolean supports(String tariffType);

    /**
     * Executes the tariff rate selection and cost calculation.
     *
     * @param context the calculation context containing developer, readings, and period info
     * @return the calculated tariff result
     */
    TariffCalculationResult calculate(TariffCalculationContext context);
}
