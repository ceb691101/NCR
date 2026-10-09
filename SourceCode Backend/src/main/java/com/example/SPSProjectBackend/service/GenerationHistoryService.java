package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.GenerationHistoryDTO;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenerationHistoryService {

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private InvoiceCalculationService invoiceCalculationService;

    public List<GenerationHistoryDTO> getLatestGenerationHistory(String accNbr, String areaCd, int cycleCount) {
        String cleanAccNbr = accNbr != null ? accNbr.trim() : "";
        String cleanAreaCd = areaCd != null ? areaCd.trim() : "";

        // Fetch all readings for this account and area
        List<NcreInvRdngs> allReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdTrimmed(cleanAccNbr, cleanAreaCd);

        // Group by bill cycle (addedBlcy)
        Map<String, List<NcreInvRdngs>> readingsByCycle = allReadings.stream()
                .filter(r -> r.getAddedBlcy() != null)
                .collect(Collectors.groupingBy(r -> r.getAddedBlcy().trim()));

        // Sort cycles descending and take requested number (or all if -1)
        List<String> latestCycles = readingsByCycle.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .limit(cycleCount > 0 ? cycleCount : Integer.MAX_VALUE)
                .collect(Collectors.toList());

        List<GenerationHistoryDTO> historyList = new ArrayList<>();

        for (String cycle : latestCycles) {
            List<NcreInvRdngs> cycleReadings = readingsByCycle.get(cycle);
            
            // Deduplicate by meter to avoid double-counting if there are multiple records for the same cycle
            Map<String, NcreInvRdngs> uniqueMeters = new HashMap<>();
            for (NcreInvRdngs r : cycleReadings) {
                String mtr = r.getMtrNbr() != null ? r.getMtrNbr().trim() : "default";
                uniqueMeters.putIfAbsent(mtr, r);
            }
            
            BigDecimal cycleTotalR1 = BigDecimal.ZERO;
            BigDecimal cycleTotalR2 = BigDecimal.ZERO;
            BigDecimal cycleTotalR3 = BigDecimal.ZERO;

            for (NcreInvRdngs currentReading : uniqueMeters.values()) {
                // Find previous chronological reading for this meter dynamically from allReadings
                // allReadings is ordered by addedBlcy DESC, so the first one we find < currentCycle is the immediate previous.
                int currentCycleInt = Integer.parseInt(cycle);
                NcreInvRdngs previousReading = null;
                
                for (NcreInvRdngs r : allReadings) {
                    if (r.getAddedBlcy() != null) {
                        try {
                            int rCycleInt = Integer.parseInt(r.getAddedBlcy().trim());
                            if (rCycleInt < currentCycleInt) {
                                // Since allReadings is already filtered by accNbr, just take the first older reading
                                previousReading = r;
                                break;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                BigDecimal prsntR1 = currentReading.getKwhR1() != null ? currentReading.getKwhR1() : BigDecimal.ZERO;
                BigDecimal prsntR2 = currentReading.getKwhR2() != null ? currentReading.getKwhR2() : BigDecimal.ZERO;
                BigDecimal prsntR3 = currentReading.getKwhR3() != null ? currentReading.getKwhR3() : BigDecimal.ZERO;

                BigDecimal prvR1 = (previousReading != null && previousReading.getKwhR1() != null) ? previousReading.getKwhR1() : prsntR1;
                BigDecimal prvR2 = (previousReading != null && previousReading.getKwhR2() != null) ? previousReading.getKwhR2() : prsntR2;
                BigDecimal prvR3 = (previousReading != null && previousReading.getKwhR3() != null) ? previousReading.getKwhR3() : prsntR3;

                // Calculate using InvoiceCalculationService to apply the exact same rules (max(0, diff))
                BigDecimal meterTotalEnergy = invoiceCalculationService.calculateEnergyFromIntervals(prsntR1, prvR1, prsntR2, prvR2, prsntR3, prvR3);
                
                // InvoiceCalculationService calculates the total, but we need individual R1, R2, R3 for the graph.
                // We replicate the exact subtraction logic to avoid negative values
                BigDecimal r1 = prsntR1.subtract(prvR1);
                if (r1.compareTo(BigDecimal.ZERO) < 0) r1 = BigDecimal.ZERO;
                
                BigDecimal r2 = prsntR2.subtract(prvR2);
                if (r2.compareTo(BigDecimal.ZERO) < 0) r2 = BigDecimal.ZERO;
                
                BigDecimal r3 = prsntR3.subtract(prvR3);
                if (r3.compareTo(BigDecimal.ZERO) < 0) r3 = BigDecimal.ZERO;

                cycleTotalR1 = cycleTotalR1.add(r1);
                cycleTotalR2 = cycleTotalR2.add(r2);
                cycleTotalR3 = cycleTotalR3.add(r3);
            }

            BigDecimal totalGeneration = cycleTotalR1.add(cycleTotalR2).add(cycleTotalR3);
            historyList.add(new GenerationHistoryDTO(cycle, cycleTotalR1, cycleTotalR2, cycleTotalR3, totalGeneration));
        }
        
        // Return in chronological order (oldest first) so the chart displays left-to-right correctly
        Collections.reverse(historyList);
        return historyList;
    }
}
