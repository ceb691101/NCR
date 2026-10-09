package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class PlantFactorRule implements InvoiceValidationRule {

    @Override
    public Optional<String> validate(InvoicePreviewDTO previewData) {
        if (previewData.getPlantFactorPercent() == null) {
            return Optional.of("Plant factor calculation value is missing.");
        }
        try {
            BigDecimal pf = new BigDecimal(previewData.getPlantFactorPercent().trim());
            if (pf.compareTo(BigDecimal.ZERO) <= 0 || pf.compareTo(new BigDecimal("100")) > 0) {
                return Optional.of("Plant factor must be greater than 0% and less than or equal to 100% (calculated: " 
                        + previewData.getPlantFactorPercent() + "%).");
            }
        } catch (NumberFormatException e) {
            return Optional.of("Could not parse plant factor '" 
                    + previewData.getPlantFactorPercent() + "' as a valid decimal number.");
        }
        return Optional.empty();
    }
}
