package com.example.SPSProjectBackend.service.tariff;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Strategy implementation for the PENDING tariff state.
 *
 * A developer with tariff_type = PENDING has not yet been assigned/approved a tariff rate.
 * Therefore, invoice preparation, calculation, draft saving, and submission must be stopped
 * immediately with a clear business error, and no financial calculation or rate lookup is performed.
 */
@Component("pendingTariffCalculator")
@Order(1)
public class PendingTariffCalculator implements TariffCalculator {

    private static final String TARIFF_TYPE = "PENDING";

    public static final String ERROR_MESSAGE =
            "Tariff Rate Pending: A tariff rate has not yet been assigned to this developer. " +
            "The invoice cannot be prepared until the tariff rate is updated.";

    @Override
    public boolean supports(String tariffType) {
        if (tariffType == null) {
            return false;
        }
        return TARIFF_TYPE.equalsIgnoreCase(tariffType.trim());
    }

    @Override
    public TariffCalculationResult calculate(TariffCalculationContext context) {
        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ERROR_MESSAGE
        );
    }
}
