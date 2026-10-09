package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.*;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.DifferenceStatisticsRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;

@Service
@Transactional(readOnly = true)
public class DifferenceStatisticsService {

    @Autowired
    private DifferenceStatisticsRepository differenceStatisticsRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    /**
     * Get difference statistics for all accounts in a single area and bill cycle.
     */
    public DifferenceResponse getAreaDifferences(String sessionId, String userId, String areaCode, String billCycle) {
        try {
            validateSessionAndAccess(sessionId, userId, areaCode);

            String targetBillCycle = billCycle;
            if (targetBillCycle == null || targetBillCycle.trim().isEmpty()) {
                Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
                if (activeBillCycleOpt.isEmpty()) {
                    return createErrorResponse("No active bill cycle found in dbadmin.ncre_bill_cycle (is_current = 1)",
                            areaCode, null);
                }
                targetBillCycle = activeBillCycleOpt.get().toString();
            }

            String cleanAreaCd = areaCode != null ? areaCode.trim() : "";
            String cleanBillCycle = targetBillCycle.trim();

            List<AccountDifferenceDTO> diffList = calculateAreaDifferences(
                    cleanAreaCd, cleanBillCycle, loadDevelopersByAccount(), resolveEeFilter(sessionId, userId));

            DifferenceResponse response = createSuccessResponse(
                    "Difference data retrieved successfully", areaCode, cleanBillCycle, diffList);
            response.setAreaCodes(List.of(cleanAreaCd));
            response.setBillCycles(List.of(cleanBillCycle));
            response.setSkippedAreaCodes(new ArrayList<>());
            return response;
        } catch (SecurityException se) {
            return createErrorResponse(se.getMessage(), areaCode, billCycle);
        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve difference data: " + e.getMessage(), areaCode, billCycle);
        }
    }

    /**
     * Difference statistics aggregated across the areas in scope.
     *
     * The active bill cycle is the single global one from dbadmin.ncre_bill_cycle
     * (is_current = 1), so every area in the set is read at the same cycle. Requested
     * areas are intersected with the permitted set.
     */
    public DifferenceResponse getDifferencesForAreas(String sessionId, String userId,
                                                    List<String> requestedAreaCodes, String billCycle) {
        if (sessionId == null || userId == null || sessionId.trim().isEmpty() || userId.trim().isEmpty()) {
            return createErrorResponse("Session ID and User ID are required", null, billCycle);
        }
        if (sessionUtils.getUserLocationFromSession(sessionId, userId).isEmpty()) {
            return createErrorResponse("Invalid session or user not found", null, billCycle);
        }

        List<String> permittedCodes = areaPermissionService.sanitizeRequestedAreaCodes(
                sessionId, userId, requestedAreaCodes);
        if (permittedCodes.isEmpty()) {
            return createErrorResponse("No permitted areas available", "ALL_PERMITTED", billCycle);
        }

        String targetBillCycle = billCycle;
        if (targetBillCycle == null || targetBillCycle.trim().isEmpty()) {
            Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
            if (activeBillCycleOpt.isEmpty()) {
                return createErrorResponse(
                        "No active bill cycle found in dbadmin.ncre_bill_cycle (is_current = 1)",
                        "ALL_PERMITTED", null);
            }
            targetBillCycle = activeBillCycleOpt.get().toString();
        }
        String sharedCycle = targetBillCycle.trim();

        String eeFilter = resolveEeFilter(sessionId, userId);
        Map<String, NcreDeveloper> devByAcc = loadDevelopersByAccount();

        List<AccountDifferenceDTO> aggregated = new ArrayList<>();
        List<String> coveredAreas = new ArrayList<>();
        List<String> resolvedCycles = new ArrayList<>();

        for (String areaCode : permittedCodes) {
            aggregated.addAll(calculateAreaDifferences(areaCode, sharedCycle, devByAcc, eeFilter));
            coveredAreas.add(areaCode);
            resolvedCycles.add(sharedCycle);
        }

        DifferenceResponse response = createSuccessResponse("Difference data retrieved successfully",
                coveredAreas.size() == 1 ? coveredAreas.get(0) : "ALL_PERMITTED",
                sharedCycle,
                aggregated);
        response.setAreaCodes(coveredAreas);
        response.setBillCycles(resolvedCycles);
        response.setSkippedAreaCodes(new ArrayList<>());
        return response;
    }

    /**
     * Energy Sent to Grid RU Difference per account: KWT_units - (KWD_units + KWP_units + KWO_units),
     * where each register's units are the current-cycle reading minus the previous-cycle reading
     * and are floored at zero.
     */
    private List<AccountDifferenceDTO> calculateAreaDifferences(String areaCode, String billCycle,
                                                                Map<String, NcreDeveloper> devByAcc,
                                                                String eeFilter) {
        List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository
                .findAllByAreaCdAndAddedBlcyTrimmed(areaCode, billCycle);

        int currentCycleInt = 0;
        try {
            currentCycleInt = Integer.parseInt(billCycle);
        } catch (Exception ignored) {}
        String previousCycle = String.valueOf(currentCycleInt - 1);

        List<NcreInvRdngs> prevList = ncreInvRdngsRepository
                .findAllByAreaCdAndAddedBlcyTrimmed(areaCode, previousCycle);
        Map<String, NcreInvRdngs> prevReadingsMap = prevList != null ? prevList.stream()
                .filter(r -> r.getAccNbr() != null)
                .collect(Collectors.toMap(r -> r.getAccNbr().trim(), r -> r, (r1, r2) -> r1)) : new HashMap<>();

        List<AccountDifferenceDTO> diffList = new ArrayList<>();
        if (currentReadings == null) {
            return diffList;
        }
        for (NcreInvRdngs current : currentReadings) {
            if (current == null || current.getAccNbr() == null) {
                continue;
            }

            String accNbr = current.getAccNbr().trim();
            NcreDeveloper dev = devByAcc.get(accNbr);
            if (eeFilter != null && (dev == null || !SessionUtils.matchEE(dev.getResponsibleEe(), eeFilter))) {
                continue;
            }

            String folioNo = (dev != null && dev.getFolioNo() != null) ? String.valueOf(dev.getFolioNo()) : "";
            String facilityName = (dev != null && dev.getFacilityName() != null && !dev.getFacilityName().trim().isEmpty())
                    ? dev.getFacilityName().trim() : accNbr;
            Short acceptRu = dev != null ? dev.getAcceptRu() : null;

            NcreInvRdngs previous = prevReadingsMap.get(accNbr);

            BigDecimal kwdUnits = nonNegativeDiff(current.getKwhR1(), previous == null ? null : previous.getKwhR1());
            BigDecimal kwpUnits = nonNegativeDiff(current.getKwhR2(), previous == null ? null : previous.getKwhR2());
            BigDecimal kwoUnits = nonNegativeDiff(current.getKwhR3(), previous == null ? null : previous.getKwhR3());
            BigDecimal kwtUnits = nonNegativeDiff(current.getKwhTot(), previous == null ? null : previous.getKwhTot());

            BigDecimal diff = kwtUnits.subtract(kwdUnits.add(kwpUnits).add(kwoUnits));

            diffList.add(new AccountDifferenceDTO(accNbr, folioNo, facilityName, diff, acceptRu));
        }

        diffList.sort((a, b) -> {
            int f1 = Integer.MAX_VALUE;
            int f2 = Integer.MAX_VALUE;
            try { f1 = Integer.parseInt(a.getFolioNo()); } catch (Exception ignored) {}
            try { f2 = Integer.parseInt(b.getFolioNo()); } catch (Exception ignored) {}
            if (f1 != f2) {
                return Integer.compare(f1, f2);
            }
            return a.getFacilityName().compareTo(b.getFacilityName());
        });

        return diffList;
    }

    private Map<String, NcreDeveloper> loadDevelopersByAccount() {
        Set<String> seen = new LinkedHashSet<>();
        Map<String, NcreDeveloper> devByAcc = new HashMap<>();
        for (NcreDeveloper dev : ncreDeveloperRepository.findAll()) {
            if (dev == null || dev.getAccNbr() == null) {
                continue;
            }
            String accNbr = dev.getAccNbr().trim();
            if (seen.add(accNbr)) {
                devByAcc.put(accNbr, dev);
            }
        }
        return devByAcc;
    }

    /** EE users only see the developers they are responsible for. */
    private String resolveEeFilter(String sessionId, String userId) {
        return sessionUtils.isEEUser(sessionId, userId) ? userId : null;
    }

    private BigDecimal nonNegativeDiff(BigDecimal present, BigDecimal previous) {
        BigDecimal prsnt = present != null ? present : BigDecimal.ZERO;
        BigDecimal prv = previous != null ? previous : BigDecimal.ZERO;
        BigDecimal units = prsnt.subtract(prv);
        return units.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : units;
    }

    /**
     * Validate session and user access rights for the specified area.
     */
    private void validateSessionAndAccess(String sessionId, String userId, String areaCode) {
        if (sessionId == null || userId == null || sessionId.trim().isEmpty() || userId.trim().isEmpty()) {
            throw new SecurityException("Session ID and User ID are required");
        }

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
        if (userInfoOpt.isEmpty()) {
            throw new SecurityException("Invalid session or user not found");
        }

        if (!hasAreaAccessBasedOnUserCategory(userInfoOpt.get(), areaCode)) {
            throw new SecurityException("Access denied to area: " + areaCode);
        }
    }

    /**
     * Access is derived from the sec_info region / province / area hierarchy.
     */
    public boolean hasAreaAccessBasedOnUserCategory(SecInfoLoginDTO.UserInfo userInfo, String targetAreaCode) {
        if (userInfo == null) {
            return false;
        }
        return areaPermissionService.isAreaPermitted(userInfo.getRegionCode(), userInfo.getProvinceCode(),
                userInfo.getAreaCode(), targetAreaCode);
    }

    private DifferenceResponse createSuccessResponse(String message, String areaCode, String activeBillCycle, List<AccountDifferenceDTO> differences) {
        DifferenceResponse response = new DifferenceResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setAreaCode(areaCode);
        response.setActiveBillCycle(activeBillCycle);
        response.setDifferences(differences);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private DifferenceResponse createErrorResponse(String message, String areaCode, String activeBillCycle) {
        DifferenceResponse response = new DifferenceResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setAreaCode(areaCode);
        response.setActiveBillCycle(activeBillCycle);
        response.setDifferences(new ArrayList<>());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
