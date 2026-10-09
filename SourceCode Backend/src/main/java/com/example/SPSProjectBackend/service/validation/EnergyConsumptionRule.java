package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

//@Component
public class EnergyConsumptionRule implements InvoiceValidationRule {

    @Override
    public Optional<String> validate(InvoicePreviewDTO previewData) {
        if (previewData.getEnergyKwh() == null) {
            return Optional.of("Energy consumption value is missing.");
        }
        try {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            Number number = format.parse(previewData.getEnergyKwh().trim());
            BigDecimal energy = new BigDecimal(number.toString());
            if (energy.compareTo(BigDecimal.ZERO) < 0) {
                return Optional.of("Energy consumption cannot be negative (calculated: " 
                        + previewData.getEnergyKwh() + " kWh).");
            }
        } catch (Exception e) {
            return Optional.of("Could not parse energy consumption '" 
                    + previewData.getEnergyKwh() + "' as a valid decimal number.");
        }
        return Optional.empty();
    }
}
