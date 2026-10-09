package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.dto.ValidationResultDTO;
import com.example.SPSProjectBackend.service.validation.InvoiceValidationRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceValidationService {

    @Autowired
    private List<InvoiceValidationRule> validationRules;

    /**
     * Run all configured invoice validation rules against the calculated preview data.
     *
     * @param previewData the calculated preview DTO
     * @return the validation result containing errors if any
     */
    public ValidationResultDTO validate(InvoicePreviewDTO previewData) {
        System.out.println("[InvoiceValidationService] validate - START validation for account: " + previewData.getAccountNumber());
        List<String> errors = new ArrayList<>();

        if (validationRules != null) {
            System.out.println("[InvoiceValidationService] validate - configured rules count: " + validationRules.size());
            for (InvoiceValidationRule rule : validationRules) {
                String ruleName = rule.getClass().getSimpleName();
                try {
                    System.out.println("[InvoiceValidationService] validate - Executing rule: " + ruleName);
                    long ruleStart = System.currentTimeMillis();
                    
                    rule.validate(previewData).ifPresent(err -> {
                        System.out.println("[InvoiceValidationService] validate - Rule " + ruleName + " failed validation: " + err);
                        errors.add(err);
                    });
                    
                    System.out.println("[InvoiceValidationService] validate - Completed rule: " + ruleName + " in " + (System.currentTimeMillis() - ruleStart) + " ms");
                } catch (Exception e) {
                    System.err.println("[InvoiceValidationService] validate - ERROR executing validation rule " + ruleName + ": " + e.getMessage());
                    e.printStackTrace();
                    errors.add("Error executing validation rule " + ruleName + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("[InvoiceValidationService] validate - WARNING: No validation rules are configured (validationRules list is null)");
        }

        System.out.println("[InvoiceValidationService] validate - END validation. Errors found: " + errors.size());
        return ValidationResultDTO.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .build();
    }
}
