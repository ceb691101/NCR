package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuValidationRuleTest {

    @Mock
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Mock
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @InjectMocks
    private RuValidationRule ruValidationRule;

    private InvoicePreviewDTO createPreview(String accNbr, String areaCd, Integer billCycle) {
        InvoicePreviewDTO preview = new InvoicePreviewDTO();
        preview.setAccountNumber(accNbr);
        preview.setAreaCode(areaCd);
        preview.setBillCycle(billCycle);
        return preview;
    }

    private NcreInvRdngs createReading(String accNbr, String areaCd, String cycle,
                                       BigDecimal tot, BigDecimal r1, BigDecimal r2, BigDecimal r3) {
        NcreInvRdngs r = new NcreInvRdngs();
        r.setAccNbr(accNbr);
        r.setAreaCd(areaCd);
        r.setAddedBlcy(cycle);
        r.setKwhTot(tot);
        r.setKwhR1(r1);
        r.setKwhR2(r2);
        r.setKwhR3(r3);
        return r;
    }

    private NcreDeveloper createDeveloper(String accNbr, Short acceptRu) {
        NcreDeveloper dev = new NcreDeveloper();
        dev.setAccNbr(accNbr);
        dev.setAcceptRu(acceptRu);
        return dev;
    }

    @Test
    void testValidate_AbsDiffLessThanAcceptRu_Passes() {
        // Prev: tot=1000, r1=300, r2=250, r3=400 (intervals sum = 950)
        // Curr: tot=1960, r1=600, r2=500, r3=800
        // Units: tot=960, r1=300, r2=250, r3=400 (intervals sum = 950) -> diff = 960 - 950 = 10
        // acceptRu = 24. abs(10) <= 24 -> PASS
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreInvRdngs curr = createReading("ACC01", "A1", "820",
                new BigDecimal("1960"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("ACC01", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("ACC01", (short) 24);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of(prev));

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isEmpty());
    }

    @Test
    void testValidate_AbsDiffExactBoundary_Passes() {
        // Positive boundary: tot units = 974, sum = 950 -> diff = +24, acceptRu = 24 -> PASS
        InvoicePreviewDTO previewPos = createPreview("ACC01", "A1", 820);
        NcreInvRdngs currPos = createReading("ACC01", "A1", "820",
                new BigDecimal("1974"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("ACC01", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("ACC01", (short) 24);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(currPos));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of(prev));

        Optional<String> errorPos = ruValidationRule.validate(previewPos);
        assertTrue(errorPos.isEmpty());

        // Negative boundary: tot units = 926, sum = 950 -> diff = -24, acceptRu = 24 -> PASS
        NcreInvRdngs currNeg = createReading("ACC01", "A1", "820",
                new BigDecimal("1926"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(currNeg));

        Optional<String> errorNeg = ruValidationRule.validate(previewPos);
        assertTrue(errorNeg.isEmpty());
    }

    @Test
    void testValidate_AbsDiffExceedsAcceptRu_FailsWithCorrectMessage() {
        // Units: tot=975, sum=950 -> diff = +25, acceptRu = 24 -> FAILS
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreInvRdngs curr = createReading("ACC01", "A1", "820",
                new BigDecimal("1975"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("ACC01", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("ACC01", (short) 24);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of(prev));

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isPresent());
        assertEquals("RU Limit Exceeded: The difference between total export (975) and the sum of interval export readings (950) is 25 (absolute: 25), which exceeds the developer's allowed limit of 24.", error.get());
    }

    @Test
    void testValidate_NegativeDiffExceedsAcceptRu_FailsWithCorrectMessage() {
        // Units: tot=900, sum=950 -> diff = -50, acceptRu = 24 -> FAILS
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreInvRdngs curr = createReading("ACC01", "A1", "820",
                new BigDecimal("1900"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("ACC01", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));
        NcreDeveloper dev = createDeveloper("ACC01", (short) 24);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of(prev));

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isPresent());
        assertEquals("RU Limit Exceeded: The difference between total export (900) and the sum of interval export readings (950) is -50 (absolute: 50), which exceeds the developer's allowed limit of 24.", error.get());
    }

    @Test
    void testValidate_AcceptRuNull_FailsWithNonBypassableMessage() {
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreDeveloper dev = createDeveloper("ACC01", null);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isPresent());
        assertEquals("RU validation unavailable. Acceptable RU limit is not configured for this developer.", error.get());
    }

    @Test
    void testValidate_DeveloperNotFound_FailsWithNonBypassableMessage() {
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.empty());

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isPresent());
        assertEquals("RU validation unavailable. Developer not found for account ACC01.", error.get());
    }

    @Test
    void testValidate_MissingPreviousReading_FailsWithNonBypassableMessage() {
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreDeveloper dev = createDeveloper("ACC01", (short) 24);
        NcreInvRdngs curr = createReading("ACC01", "A1", "820",
                new BigDecimal("2000"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));

        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of());

        Optional<String> error = ruValidationRule.validate(preview);

        assertTrue(error.isPresent());
        assertEquals("RU validation unavailable. Previous meter readings are required to calculate Energy Sent to Grid.", error.get());
    }

    @Test
    void testValidate_CustomAcceptRuPerDeveloper() {
        InvoicePreviewDTO preview = createPreview("ACC01", "A1", 820);
        NcreInvRdngs curr = createReading("ACC01", "A1", "820",
                new BigDecimal("1965"), new BigDecimal("600"), new BigDecimal("500"), new BigDecimal("800"));
        NcreInvRdngs prev = createReading("ACC01", "A1", "819",
                new BigDecimal("1000"), new BigDecimal("300"), new BigDecimal("250"), new BigDecimal("400"));

        // Case 1: Developer with acceptRu = 10 -> diff is 15 -> FAILS
        NcreDeveloper dev10 = createDeveloper("ACC01", (short) 10);
        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev10));
        when(ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "820")).thenReturn(List.of(curr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("ACC01", "A1", "819")).thenReturn(List.of(prev));

        Optional<String> error10 = ruValidationRule.validate(preview);
        assertTrue(error10.isPresent());
        assertEquals("RU Limit Exceeded: The difference between total export (965) and the sum of interval export readings (950) is 15 (absolute: 15), which exceeds the developer's allowed limit of 10.", error10.get());

        // Case 2: Developer with acceptRu = 50 -> diff is 15 -> PASSES
        NcreDeveloper dev50 = createDeveloper("ACC01", (short) 50);
        when(ncreDeveloperRepository.findByAccNbrTrimmed("ACC01")).thenReturn(Optional.of(dev50));

        Optional<String> error50 = ruValidationRule.validate(preview);
        assertTrue(error50.isEmpty());
    }
}
