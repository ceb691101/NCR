package com.example.SPSProjectBackend.service.validation;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class RuValidationRule implements InvoiceValidationRule {

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Override
    public Optional<String> validate(InvoicePreviewDTO previewData) {
        if (previewData.getAccountNumber() == null || previewData.getAreaCode() == null
                || previewData.getBillCycle() == null) {
            return Optional.empty();
        }

        String accNbr = previewData.getAccountNumber().trim();
        String areaCd = previewData.getAreaCode().trim();
        int currentCycleInt = previewData.getBillCycle();

        // 1. Load developer and check accept_ru
        Optional<NcreDeveloper> developerOpt = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr);
        if (developerOpt.isEmpty()) {
            return Optional.of("RU validation unavailable. Developer not found for account " + accNbr + ".");
        }

        NcreDeveloper developer = developerOpt.get();
        Short acceptRu = developer.getAcceptRu();
        if (acceptRu == null) {
            return Optional.of("RU validation unavailable. Acceptable RU limit is not configured for this developer.");
        }

        // 2. Load current bill-cycle readings
        List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                accNbr,
                areaCd,
                String.valueOf(currentCycleInt));

        if (currentReadings == null || currentReadings.isEmpty()) {
            return Optional.of(
                    "RU validation unavailable. Current meter readings are required to calculate Energy Sent to Grid.");
        }

        NcreInvRdngs current = currentReadings.get(0);
        List<String> missingCurrent = new ArrayList<>();
        if (current.getKwhR1() == null)
            missingCurrent.add("KWD (Day)");
        if (current.getKwhR2() == null)
            missingCurrent.add("KWP (Peak)");
        if (current.getKwhR3() == null)
            missingCurrent.add("KWO (Off-Peak)");
        if (current.getKwhTot() == null)
            missingCurrent.add("KWT (Total Export)");
        if (!missingCurrent.isEmpty()) {
            return Optional.of("RU validation unavailable. Required current meter reading data is missing: "
                    + String.join(", ", missingCurrent) + ".");
        }

        // 3. Load previous bill-cycle readings (cycle - 1)
        String previousCycleStr = String.valueOf(currentCycleInt - 1);
        List<NcreInvRdngs> previousReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                accNbr,
                areaCd,
                previousCycleStr);

        if (previousReadings == null || previousReadings.isEmpty()) {
            return Optional.of(
                    "RU validation unavailable. Previous meter readings are required to calculate Energy Sent to Grid.");
        }

        NcreInvRdngs previous = previousReadings.get(0);
        List<String> missingPrev = new ArrayList<>();
        if (previous.getKwhR1() == null)
            missingPrev.add("KWD (Day)");
        if (previous.getKwhR2() == null)
            missingPrev.add("KWP (Peak)");
        if (previous.getKwhR3() == null)
            missingPrev.add("KWO (Off-Peak)");
        if (previous.getKwhTot() == null)
            missingPrev.add("KWT (Total Export)");
        if (!missingPrev.isEmpty()) {
            return Optional.of("RU validation unavailable. Required previous meter reading data is missing: "
                    + String.join(", ", missingPrev) + ".");
        }

        // 4. Calculate Energy Sent to Grid (units)
        BigDecimal kwdUnits = current.getKwhR1().subtract(previous.getKwhR1());
        if (kwdUnits.compareTo(BigDecimal.ZERO) < 0)
            kwdUnits = BigDecimal.ZERO;

        BigDecimal kwpUnits = current.getKwhR2().subtract(previous.getKwhR2());
        if (kwpUnits.compareTo(BigDecimal.ZERO) < 0)
            kwpUnits = BigDecimal.ZERO;

        BigDecimal kwoUnits = current.getKwhR3().subtract(previous.getKwhR3());
        if (kwoUnits.compareTo(BigDecimal.ZERO) < 0)
            kwoUnits = BigDecimal.ZERO;

        BigDecimal kwtUnits = current.getKwhTot().subtract(previous.getKwhTot());
        if (kwtUnits.compareTo(BigDecimal.ZERO) < 0)
            kwtUnits = BigDecimal.ZERO;

        // 5. Calculate RU difference: KWT_units - (KWD_units + KWP_units + KWO_units)
        BigDecimal sumOfIntervals = kwdUnits.add(kwpUnits).add(kwoUnits);
        BigDecimal signedDifference = kwtUnits.subtract(sumOfIntervals);
        BigDecimal absoluteDifference = signedDifference.abs();

        // 6. Compare with developer accept_ru
        if (absoluteDifference.compareTo(BigDecimal.valueOf(acceptRu)) > 0) {
            return Optional.of("RU Limit Exceeded: The difference between total export (" + kwtUnits
                    + ") and the sum of interval export readings (" + sumOfIntervals
                    + ") is " + signedDifference + " (absolute: " + absoluteDifference
                    + "), which exceeds the developer's allowed limit of " + acceptRu + ".");
        }

        return Optional.empty();
    }
}
