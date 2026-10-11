package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.config.InvoiceRenderingConfig;
import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfGenerationTest {

    private InvoiceService invoiceService;
    private SpringTemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        ReflectionTestUtils.setField(invoiceService, "invoiceTemplateEngine", templateEngine);
    }

    @Test
    @DisplayName("Should successfully render invoice HTML and PDF without SAXParseException")
    void testRenderInvoiceHtmlAndPdf() {
        InvoicePreviewDTO previewDTO = InvoicePreviewDTO.builder()
                .refNo("INV-54-5470100790-457-20261008122054")
                .accountNumber("5470100790")
                .companyName("Test Solar Power (Pvt) Ltd & Partners")
                .projectName("Test Solar Project")
                .invoiceMonth("September 2026")
                .capacityMw("1.000")
                .allowedGenerationMw("1.000")
                .presentReadingDate("2026-10-01")
                .previousReadingDate("2026-09-01")
                .presentR1("379044.000")
                .presentR2("123375.563")
                .presentR3("215205.953")
                .previousR1("359282.406")
                .previousR2("117512.242")
                .previousR3("204558.484")
                .multiplyFactor("1.0")
                .energyKwh("36272.38")
                .eligibleEnergyKwh("36272.38")
                .periodOfGeneration("30")
                .plantFactorPercent("50.38")
                .ratePerKwh("19.89")
                .costOfEnergy("721457.64")
                .finalAmountToBePaid("721457.64")
                .documentType("MAIN")
                .tariffChargeLines(new ArrayList<>())
                .paymentDeductions(new ArrayList<>())
                .chiefEngineer(InvoicePreviewDTO.SignerDetailsDTO.builder()
                        .name("Eng. (Mrs.) A.G.C.U Perera")
                        .designation("Chief Engineer")
                        .date("2026-10-08")
                        .build())
                .director(InvoicePreviewDTO.SignerDetailsDTO.builder()
                        .name("Eng. G. W. Vajira Priyantha")
                        .designation("Director")
                        .date("2026-10-08")
                        .build())
                .preparedBy(InvoicePreviewDTO.SignerDetailsDTO.builder()
                        .name("Eng. R.S. Ukwatta")
                        .designation("EE (REP3)")
                        .date("2026-10-08")
                        .build())
                .build();

        String html = invoiceService.renderInvoiceHtml(previewDTO);
        assertNotNull(html);
        assertTrue(html.contains("Director (BSTA &amp; TM)") || html.contains("Director (BSTA & TM)"));

        byte[] pdfBytes = invoiceService.renderPdf(html);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify PDF magic header "%PDF-"
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }

    @Test
    @DisplayName("Should sanitize unescaped ampersands in renderPdf to avoid SAXParseException")
    void testRenderPdfWithUnescapedAmpersand() {
        String rawHtml = "<!DOCTYPE html><html><head><title>Test</title></head><body>"
                + "<h4>Director (BSTA & TM)</h4>"
                + "<p>Energy & Power &amp; Gas</p>"
                + "</body></html>";

        byte[] pdfBytes = invoiceService.renderPdf(rawHtml);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }
}
