package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ChangeRuRequestDTO;
import com.example.SPSProjectBackend.dto.DeveloperRuDetailsDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreRuChange;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreRuChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChangeRuDifferenceService {

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreRuChangeRepository ncreRuChangeRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreTariffDescriptionRepository ncreTariffDescriptionRepository;

    private List<NcreDeveloper> developerCache = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile long lastDevCacheTime = 0;
    private static final long DEV_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private Map<String, String> tariffDescCache = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile long lastTariffCacheTime = 0;
    private static final long TARIFF_CACHE_TTL_MS = 15 * 60 * 1000; // 15 minutes

    public void invalidateDeveloperCache() {
        lastDevCacheTime = 0;
    }

    private List<NcreDeveloper> getDevelopers() {
        long now = System.currentTimeMillis();
        if (developerCache.isEmpty() || (now - lastDevCacheTime > DEV_CACHE_TTL_MS)) {
            refreshDeveloperCache();
        }
        return developerCache;
    }

    private synchronized void refreshDeveloperCache() {
        long now = System.currentTimeMillis();
        if (!developerCache.isEmpty() && (now - lastDevCacheTime <= DEV_CACHE_TTL_MS)) {
            return;
        }
        try {
            List<NcreDeveloper> all = ncreDeveloperRepository.findAll();
            if (all != null) {
                developerCache = new java.util.concurrent.CopyOnWriteArrayList<>(all);
                lastDevCacheTime = now;
            }
        } catch (Exception e) {
            // Keep existing cache on transient failure
        }
    }

    public List<DeveloperRuDetailsDTO> searchDevelopers(String searchType, String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String cleanQuery = query.trim();
        String lowerQuery = cleanQuery.toLowerCase();
        List<NcreDeveloper> allDevs = getDevelopers();
        Map<String, String> tariffMap = getTariffDescriptionMap();

        if ("FOLIO".equalsIgnoreCase(searchType)) {
            return allDevs.stream()
                    .filter(dev -> dev.getFolioNo() != null && String.valueOf(dev.getFolioNo()).contains(cleanQuery))
                    .sorted((d1, d2) -> {
                        String f1 = String.valueOf(d1.getFolioNo());
                        String f2 = String.valueOf(d2.getFolioNo());

                        boolean exact1 = f1.equals(cleanQuery);
                        boolean exact2 = f2.equals(cleanQuery);
                        if (exact1 && !exact2) return -1;
                        if (!exact1 && exact2) return 1;

                        boolean start1 = f1.startsWith(cleanQuery);
                        boolean start2 = f2.startsWith(cleanQuery);
                        if (start1 && !start2) return -1;
                        if (!start1 && start2) return 1;

                        return d1.getFolioNo().compareTo(d2.getFolioNo());
                    })
                    .limit(25)
                    .map(dev -> mapToDTO(dev, tariffMap))
                    .collect(Collectors.toList());
        } else if ("NAME".equalsIgnoreCase(searchType)) {
            return allDevs.stream()
                    .filter(dev -> dev.getDeveloperName() != null && dev.getDeveloperName().toLowerCase().contains(lowerQuery))
                    .sorted((d1, d2) -> {
                        String n1 = d1.getDeveloperName().toLowerCase();
                        String n2 = d2.getDeveloperName().toLowerCase();

                        boolean exact1 = n1.equals(lowerQuery);
                        boolean exact2 = n2.equals(lowerQuery);
                        if (exact1 && !exact2) return -1;
                        if (!exact1 && exact2) return 1;

                        boolean start1 = n1.startsWith(lowerQuery);
                        boolean start2 = n2.startsWith(lowerQuery);
                        if (start1 && !start2) return -1;
                        if (!start1 && start2) return 1;

                        return n1.compareTo(n2);
                    })
                    .limit(25)
                    .map(dev -> mapToDTO(dev, tariffMap))
                    .collect(Collectors.toList());
        }

        return java.util.Collections.emptyList();
    }

    public DeveloperRuDetailsDTO getDeveloperByFolio(Short folioNo) {
        Map<String, String> tariffMap = getTariffDescriptionMap();
        return ncreDeveloperRepository.findByFolioNo(folioNo)
                .map(dev -> mapToDTO(dev, tariffMap))
                .orElseThrow(() -> new IllegalArgumentException("Developer not found for Folio Number: " + folioNo));
    }

    @Transactional
    public void changeRuDifference(ChangeRuRequestDTO request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID (EPF) is missing from the request.");
        }

        NcreDeveloper developer = ncreDeveloperRepository.findByFolioNo(request.getFolioNo())
                .orElseThrow(() -> new IllegalArgumentException("Developer not found for Folio Number: " + request.getFolioNo()));

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date today = cal.getTime();
        
        // Check if changed today
        com.example.SPSProjectBackend.model.NcreRuChangeId id = new com.example.SPSProjectBackend.model.NcreRuChangeId(request.getFolioNo(), today);
        if (ncreRuChangeRepository.existsById(id)) {
            throw new IllegalStateException("RU Difference has already been changed for this Folio Number today.");
        }

        Short currentRu = developer.getAcceptRu();
        if (currentRu != null && currentRu.equals(request.getNewRu())) {
            throw new IllegalArgumentException("New RU Difference must be different from the current RU Difference.");
        }

        // Get EPF Number
        String epfNumber = request.getUserId();
        if (userAccSecInfoRepository != null) {
            com.example.SPSProjectBackend.model.UserAccSecInfo user = userAccSecInfoRepository.findById(request.getUserId()).orElse(null);
            if (user != null && user.getEpfNum() != null && !user.getEpfNum().trim().isEmpty()) {
                epfNumber = user.getEpfNum();
            }
        }

        // Audit Record
        NcreRuChange audit = new NcreRuChange();
        audit.setFolioNo(request.getFolioNo());
        audit.setCurentRu(currentRu);
        audit.setChangeRu(request.getNewRu());
        audit.setEntBy(epfNumber);
        audit.setEntDt(today);

        ncreRuChangeRepository.save(audit);

        // Update Developer
        developer.setAcceptRu(request.getNewRu());
        ncreDeveloperRepository.save(developer);
        invalidateDeveloperCache();
    }

    private Map<String, String> getTariffDescriptionMap() {
        long now = System.currentTimeMillis();
        if (tariffDescCache.isEmpty() || (now - lastTariffCacheTime > TARIFF_CACHE_TTL_MS)) {
            refreshTariffCache();
        }
        return tariffDescCache;
    }

    private synchronized void refreshTariffCache() {
        long now = System.currentTimeMillis();
        if (!tariffDescCache.isEmpty() && (now - lastTariffCacheTime <= TARIFF_CACHE_TTL_MS)) {
            return;
        }
        try {
            List<com.example.SPSProjectBackend.model.NcreTariffDescription> allTariffs = ncreTariffDescriptionRepository.findAll();
            Map<String, String> newMap = new java.util.concurrent.ConcurrentHashMap<>();
            for (com.example.SPSProjectBackend.model.NcreTariffDescription t : allTariffs) {
                if (t.getTariffCode() != null && t.getTariffDesc() != null) {
                    newMap.put(t.getTariffCode().trim().toUpperCase(), t.getTariffDesc().trim());
                    newMap.put(t.getTariffDesc().trim().toUpperCase(), t.getTariffDesc().trim());
                }
            }
            tariffDescCache = newMap;
            lastTariffCacheTime = now;
        } catch (Exception ignored) {
            // Keep existing cache on transient failure
        }
    }

    private DeveloperRuDetailsDTO mapToDTO(NcreDeveloper dev, Map<String, String> tariffMap) {
        String tariffDesc = "N/A";
        if (dev.getTariffType() != null && !dev.getTariffType().trim().isEmpty()) {
            String rawType = dev.getTariffType().trim();
            tariffDesc = tariffMap.getOrDefault(rawType.toUpperCase(), rawType);
        }
        
        return DeveloperRuDetailsDTO.builder()
                .folioNo(dev.getFolioNo())
                .developerName(dev.getDeveloperName())
                .facilityName(dev.getFacilityName())
                .acceptRu(dev.getAcceptRu())
                .ncreType(dev.getType())
                .tariffType(dev.getTariffType())
                .tariffDesc(tariffDesc)
                .build();
    }
}
