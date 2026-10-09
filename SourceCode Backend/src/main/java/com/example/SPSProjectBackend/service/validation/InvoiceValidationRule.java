package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;

import java.util.Optional;

public interface InvoiceValidationRule {
    Optional<String> validate(InvoicePreviewDTO previewData);
}
