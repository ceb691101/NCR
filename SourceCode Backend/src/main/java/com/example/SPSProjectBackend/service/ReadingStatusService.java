package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.ReadingStatusDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import com.example.SPSProjectBackend.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReadingStatusService {

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    /**
     * Resolve the active bill cycle from ncre_bill_cycle (the single row with is_current = 1)
     */
    private Optional<Integer> resolveActiveBillCycle() {
        return ncreBillCycleRepository.findCurrentBillCycleNumber();
    }

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    /**
     * Get reading status for the areas the user may read.
     *
     * The area set comes from the sec_info region/province/area hierarchy, not from the
     * user category. When the user has not narrowed the scope from the header bar, data for
     * every permitted area is returned; otherwise only the selected area.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public ReadingStatusDTO.ReadingStatusResponse getReadingStatusForUser(String sessionId, String userId, 
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String userCategory = userInfo.getUserCategory();

            // EE users still get their own row-level filtering on top of the permitted areas.
            String eeFilterUserId = SessionUtils.isEEUserCategory(userCategory) ? userId : null;

            String selectedAreaCode = sessionUtils.getSessionData(sessionId)
                    .map(com.example.SPSProjectBackend.model.SecInfoSessionData::getSelectedAreaCode)
                    .orElse(null);

            List<String> effectiveAreaCodes = areaPermissionService
                    .resolveEffectiveAreaCodes(sessionId, userId, selectedAreaCode);

            List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus = getReadingStatusForAreaCodes(
                    effectiveAreaCodes, includeCustomerDetails, includeReadingDetails, eeFilterUserId);

            ReadingStatusDTO.ReadingStatusSummaryDTO summary = createSummary(areaReadingStatus);

            return createSuccessResponse(userCategory, areaReadingStatus, summary);

        } catch (Exception e) {
            System.err.println("ERROR in getReadingStatusForUser: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Failed to retrieve reading status: " + e.getMessage());
        }
    }

    /**
     * Bulk reading status for an explicit set of areas. Uses the same single-query path as
     * the all-areas view and simply restricts the area list.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getReadingStatusForAreaCodes(
            List<String> areaCodes, boolean includeCustomerDetails, boolean includeReadingDetails, String eeUserId) {
        List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
        if (areaCodes == null || areaCodes.isEmpty()) {
            return result;
        }

        Map<String, String> tariffByAcc = new HashMap<>();
        Map<Short, String> tariffByFolio = new HashMap<>();
        loadTariffMaps(tariffByAcc, tariffByFolio);

        List<Object[]> allDevelopers = ncreDeveloperRepository.findAllActiveDeveloperReadingRows();
        List<HsbAreaDTO> areas = locationService.getAreasWithProvinceNamesByCodes(areaCodes);

        Optional<Integer> currentCycleOpt = resolveActiveBillCycle();
        Integer currentCycle = currentCycleOpt.orElse(null);
        Map<String, Integer> activeBillCyclesByArea = new HashMap<>();
        if (currentCycle != null) {
            for (HsbAreaDTO area : areas) {
                if (area != null && area.getAreaCode() != null) {
                    activeBillCyclesByArea.put(area.getAreaCode().trim(), currentCycle);
                }
            }
        }

        List<Object[]> visibleDevelopers = filterDevelopersForEe(allDevelopers, eeUserId);
        Map<String, List<Object[]>> developersByArea = visibleDevelopers.stream()
                .collect(Collectors.groupingBy(developer -> valueAt(developer, 2)));

        List<Object[]> readingsTuples = ncreInvRdngsRepository.findAllActiveReadingsTuples();
        Set<String> areaBillCycleAccountsWithReadings = new HashSet<>();
        for (Object[] row : readingsTuples) {
            if (row[0] != null && row[1] != null && row[2] != null) {
                areaBillCycleAccountsWithReadings.add(row[0].toString().trim() + "_"
                        + row[1].toString().trim() + "_" + row[2].toString().trim());
            }
        }

        for (HsbAreaDTO area : areas) {
            if (area == null || area.getAreaCode() == null) continue;
            try {
                String cleanAreaCode = area.getAreaCode().trim();
                Integer activeBillCycle = activeBillCyclesByArea.get(cleanAreaCode);

                List<Object[]> areaDevelopers = developersByArea.getOrDefault(cleanAreaCode, Collections.emptyList());

                if (activeBillCycle == null) {
                    result.add(createAreaStatusWithoutActiveBillCycle(area, includeCustomerDetails,
                            areaDevelopers, tariffByAcc, tariffByFolio));
                    continue;
                }

                if (areaDevelopers.isEmpty()) {
                    result.add(createEmptyAreaStatus(area, activeBillCycle));
                    continue;
                }

                String activeBillCycleStr = activeBillCycle.toString();
                List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithReadings = new ArrayList<>();
                List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithoutReadings = new ArrayList<>();

                for (Object[] developer : areaDevelopers) {
                    String cleanAccNbr = valueAt(developer, 0);
                    boolean hasReading = !cleanAccNbr.isEmpty()
                            && areaBillCycleAccountsWithReadings.contains(cleanAreaCode + "_" + activeBillCycleStr + "_" + cleanAccNbr);

                    ReadingStatusDTO.CustomerReadingStatusDTO customerStatus = convertToDeveloperStatusDTO(
                            developer, hasReading, includeCustomerDetails, includeReadingDetails,
                            activeBillCycle, tariffByAcc, tariffByFolio);

                    if (includeReadingDetails && hasReading) {
                        customerStatus.setReadings(new ArrayList<>());
                        customerStatus.setReadingCount(0);
                    }

                    if (hasReading) {
                        customersWithReadings.add(customerStatus);
                    } else {
                        customersWithoutReadings.add(customerStatus);
                    }
                }

                ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
                areaStatus.setAreaCode(area.getAreaCode());
                areaStatus.setAreaName(area.getAreaName());
                areaStatus.setProvinceCode(area.getProvCode());
                areaStatus.setProvinceName(area.getProvinceName());
                areaStatus.setRegionCode(area.getRegion());
                areaStatus.setActiveBillCycle(activeBillCycle);
                areaStatus.setTotalCustomers(areaDevelopers.size());
                areaStatus.setCustomersWithReadings(customersWithReadings.size());
                areaStatus.setCustomersWithoutReadings(customersWithoutReadings.size());

                double percentage = areaDevelopers.size() > 0 ?
                        (double) customersWithReadings.size() / areaDevelopers.size() * 100.0 : 0.0;
                areaStatus.setReadingPercentage(Math.round(percentage * 100.0) / 100.0);

                if (includeCustomerDetails) {
                    areaStatus.setCustomersWithReadingsList(customersWithReadings);
                    areaStatus.setCustomersWithoutReadingsList(customersWithoutReadings);
                }

                result.add(areaStatus);
            } catch (Exception e) {
                System.err.println("ERROR processing area " + area.getAreaCode() + ": " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Get reading status for a specific area - FIXED VERSION with data type handling
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAreaReadingStatus(String areaCode, 
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        return getAreaReadingStatus(areaCode, includeCustomerDetails, includeReadingDetails, null);
    }

    private void loadTariffMaps(Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        try {
            List<Object[]> rows = ncreDeveloperRepository.findAllDeveloperTariffDescriptions();
            if (rows != null) {
                for (Object[] row : rows) {
                    if (row != null && row.length >= 3) {
                        String acc = row[0] != null ? row[0].toString().trim() : null;
                        Short folio = null;
                        if (row[1] instanceof Number) {
                            folio = ((Number) row[1]).shortValue();
                        } else if (row[1] != null) {
                            try {
                                folio = Short.valueOf(row[1].toString().trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        String tariffDesc = row[2] != null ? row[2].toString().trim() : null;
                        if (acc != null && tariffDesc != null) {
                            tariffByAcc.put(acc, tariffDesc);
                        }
                        if (folio != null && tariffDesc != null) {
                            tariffByFolio.put(folio, tariffDesc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to load developer tariff descriptions: " + e.getMessage());
        }
    }

    /**
     * Get reading status for a specific area with optional EE filtering
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAreaReadingStatus(String areaCode, 
            boolean includeCustomerDetails, boolean includeReadingDetails, String eeUserId) {
        Map<String, String> tariffByAcc = new HashMap<>();
        Map<Short, String> tariffByFolio = new HashMap<>();
        loadTariffMaps(tariffByAcc, tariffByFolio);
        List<Object[]> developers = ncreDeveloperRepository.findActiveDeveloperReadingRowsByArea(areaCode);
        return getAreaReadingStatus(areaCode, includeCustomerDetails, includeReadingDetails, eeUserId,
                developers, tariffByAcc, tariffByFolio);
    }

    private List<ReadingStatusDTO.AreaReadingStatusDTO> getAreaReadingStatus(String areaCode,
            boolean includeCustomerDetails, boolean includeReadingDetails, String eeUserId,
            List<Object[]> developers, Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
        
        try {
            // Add null check
            if (areaCode == null || areaCode.trim().isEmpty()) {
                throw new RuntimeException("Area code is required");
            }
            
            // Clean area code
            String cleanAreaCode = areaCode.trim();
            
            // Get area details
            Optional<HsbAreaDTO> areaOpt = locationService.getAreaByCode(cleanAreaCode);
            if (!areaOpt.isPresent()) {
                System.err.println("ERROR: Area not found: " + cleanAreaCode);
                throw new RuntimeException("Area not found: " + cleanAreaCode);
            }

            HsbAreaDTO area = areaOpt.get();
            
            // Get active bill cycle from ncre_bill_cycle (is_current = 1)
            Optional<Integer> activeBillCycleOpt = resolveActiveBillCycle();
            
            if (!activeBillCycleOpt.isPresent()) {
                System.err.println("ERROR: No active bill cycle found for area: " + cleanAreaCode);
                // Area has no active bill cycle - all customers are considered pending
                List<Object[]> visibleDevelopers = filterDevelopersForEe(developers, eeUserId);
                ReadingStatusDTO.AreaReadingStatusDTO areaStatus = createAreaStatusWithoutActiveBillCycle(
                    area, includeCustomerDetails, visibleDevelopers, tariffByAcc, tariffByFolio);
                result.add(areaStatus);
                return result;
            }

            Integer activeBillCycle = activeBillCycleOpt.get();
            // Convert integer bill cycle to string for comparison with added_blcy
            String activeBillCycleStr = activeBillCycle.toString();

            List<Object[]> areaDevelopers = filterDevelopersForEe(developers, eeUserId);
            
            if (areaDevelopers.isEmpty()) {
                ReadingStatusDTO.AreaReadingStatusDTO areaStatus = createEmptyAreaStatus(area, activeBillCycle);
                result.add(areaStatus);
                return result;
            }

            // Get all temp readings for the area's active bill cycle from ncre_inv_rdngs (including finalized)
            List<NcreInvRdngs> areaReadings = ncreInvRdngsRepository.findAllByAreaCdAndAddedBlcyTrimmed(cleanAreaCode, activeBillCycleStr);
            System.out.println("DEBUG: Found " + areaReadings.size() + " readings for area " + cleanAreaCode + " and bill cycle " + activeBillCycleStr);
            
            // Create a set of trimmed account numbers that have readings for the active bill cycle
            Set<String> accountsWithReadings = areaReadings.stream()
                    .filter(reading -> reading != null && reading.getAccNbr() != null)
                    .map(reading -> reading.getAccNbr().trim())
                    .collect(Collectors.toSet());

            // Separate customers into two groups
            List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithReadings = new ArrayList<>();
            List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithoutReadings = new ArrayList<>();

            for (Object[] developer : areaDevelopers) {
                String cleanAccNbr = valueAt(developer, 0);
                boolean hasReading = !cleanAccNbr.isEmpty() && accountsWithReadings.contains(cleanAccNbr);
                
                ReadingStatusDTO.CustomerReadingStatusDTO customerStatus = convertToDeveloperStatusDTO(
                        developer, hasReading, includeCustomerDetails, includeReadingDetails,
                        activeBillCycle, tariffByAcc, tariffByFolio);
                
                if (includeReadingDetails && hasReading) {
                    customerStatus.setReadings(new ArrayList<>());
                    customerStatus.setReadingCount(0);
                }

                if (hasReading) {
                    customersWithReadings.add(customerStatus);
                } else {
                    customersWithoutReadings.add(customerStatus);
                }
            }

            // Create area status
            ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
            areaStatus.setAreaCode(area.getAreaCode());
            areaStatus.setAreaName(area.getAreaName());
            areaStatus.setProvinceCode(area.getProvCode());
            areaStatus.setProvinceName(area.getProvinceName());
            areaStatus.setRegionCode(area.getRegion());
            areaStatus.setActiveBillCycle(activeBillCycle);
            areaStatus.setTotalCustomers(areaDevelopers.size());
            areaStatus.setCustomersWithReadings(customersWithReadings.size());
            areaStatus.setCustomersWithoutReadings(customersWithoutReadings.size());
            
            // Calculate percentage
            double percentage = areaDevelopers.size() > 0 ?
                (double) customersWithReadings.size() / areaDevelopers.size() * 100.0 : 0.0;
            areaStatus.setReadingPercentage(Math.round(percentage * 100.0) / 100.0);

            if (includeCustomerDetails) {
                areaStatus.setCustomersWithReadingsList(customersWithReadings);
                areaStatus.setCustomersWithoutReadingsList(customersWithoutReadings);
            }

            result.add(areaStatus);
            
        } catch (Exception e) {
            System.err.println("ERROR in getAreaReadingStatus for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for area " + areaCode + ": " + e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * Get reading status for all areas in a province
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getProvinceReadingStatus(String provinceCode, 
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            // Get areas by province
            List<HsbAreaDTO> areas = locationService.getAreasByProvince(provinceCode);
            
            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : areas) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(
                        area.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    result.addAll(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + " in province " + provinceCode + ": " + e.getMessage());
                    // Continue with other areas
                }
            }
            
            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getProvinceReadingStatus for province " + provinceCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for province " + provinceCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get reading status for all areas in a region
     */
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getRegionReadingStatus(String regionCode, 
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        try {
            // Get areas by region
            List<HsbAreaDTO> areas = locationService.getAreasByRegion(regionCode);
            
            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : areas) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(
                        area.getAreaCode(), includeCustomerDetails, includeReadingDetails);
                    result.addAll(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + " in region " + regionCode + ": " + e.getMessage());
                    // Continue with other areas
                }
            }
            
            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getRegionReadingStatus for region " + regionCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for region " + regionCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get reading status for all areas (Admin view)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAllAreasReadingStatus(
            boolean includeCustomerDetails, boolean includeReadingDetails) {
        return getAllAreasReadingStatus(includeCustomerDetails, includeReadingDetails, null);
    }

    /**
     * Get reading status for all areas with optional EE filtering (Bulk-optimized)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public List<ReadingStatusDTO.AreaReadingStatusDTO> getAllAreasReadingStatus(
            boolean includeCustomerDetails, boolean includeReadingDetails, String eeUserId) {
        try {
                // Bulk pre-fetch developer tariff descriptions.
            Map<String, String> tariffByAcc = new HashMap<>();
            Map<Short, String> tariffByFolio = new HashMap<>();
            loadTariffMaps(tariffByAcc, tariffByFolio);

                List<Object[]> allDevelopers = ncreDeveloperRepository.findAllActiveDeveloperReadingRows();

            // 2. Fetch all areas
            List<HsbAreaDTO> allAreas = locationService.getAllAreas();

            // 3. Resolve the single current bill cycle from ncre_bill_cycle (is_current = 1)
            Optional<Integer> currentCycleOpt = resolveActiveBillCycle();
            Integer currentCycle = currentCycleOpt.orElse(null);
            Map<String, Integer> activeBillCyclesByArea = new HashMap<>();
            if (currentCycle != null) {
                for (HsbAreaDTO a : allAreas) {
                    if (a != null && a.getAreaCode() != null) {
                        activeBillCyclesByArea.put(a.getAreaCode().trim(), currentCycle);
                    }
                }
            }

                List<Object[]> visibleDevelopers = filterDevelopersForEe(allDevelopers, eeUserId);
                Map<String, List<Object[]>> developersByArea = visibleDevelopers.stream()
                    .collect(Collectors.groupingBy(developer -> valueAt(developer, 2)));

            // 5. Bulk pre-fetch all active readings key tuples (1 query for all areas)
            List<Object[]> readingsTuples = ncreInvRdngsRepository.findAllActiveReadingsTuples();
            Set<String> areaBillCycleAccountsWithReadings = new HashSet<>();
            for (Object[] row : readingsTuples) {
                if (row[0] != null && row[1] != null && row[2] != null) {
                    String areaCd = row[0].toString().trim();
                    String addedBlcy = row[1].toString().trim();
                    String accNbr = row[2].toString().trim();
                    areaBillCycleAccountsWithReadings.add(areaCd + "_" + addedBlcy + "_" + accNbr);
                }
            }

            // 6. Build area statuses in memory without firing DB queries per area
            List<ReadingStatusDTO.AreaReadingStatusDTO> result = new ArrayList<>();
            for (HsbAreaDTO area : allAreas) {
                try {
                    if (area == null || area.getAreaCode() == null) continue;
                    String cleanAreaCode = area.getAreaCode().trim();
                    Integer activeBillCycle = activeBillCyclesByArea.get(cleanAreaCode);

                    if (activeBillCycle == null) {
                        List<Object[]> areaDevelopers = developersByArea.getOrDefault(cleanAreaCode, Collections.emptyList());
                        result.add(createAreaStatusWithoutActiveBillCycle(area, includeCustomerDetails,
                                areaDevelopers, tariffByAcc, tariffByFolio));
                        continue;
                    }

                    String activeBillCycleStr = activeBillCycle.toString();
                    List<Object[]> areaDevelopers = developersByArea.getOrDefault(cleanAreaCode, Collections.emptyList());

                    if (areaDevelopers.isEmpty()) {
                        result.add(createEmptyAreaStatus(area, activeBillCycle));
                        continue;
                    }

                    List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithReadings = new ArrayList<>();
                    List<ReadingStatusDTO.CustomerReadingStatusDTO> customersWithoutReadings = new ArrayList<>();

                    for (Object[] developer : areaDevelopers) {
                        String cleanAccNbr = valueAt(developer, 0);
                        String lookupKey = cleanAreaCode + "_" + activeBillCycleStr + "_" + cleanAccNbr;
                        boolean hasReading = !cleanAccNbr.isEmpty() && areaBillCycleAccountsWithReadings.contains(lookupKey);

                        ReadingStatusDTO.CustomerReadingStatusDTO customerStatus = convertToDeveloperStatusDTO(
                                developer, hasReading, includeCustomerDetails, includeReadingDetails,
                                activeBillCycle, tariffByAcc, tariffByFolio);

                        if (includeReadingDetails && hasReading) {
                            customerStatus.setReadings(new ArrayList<>());
                            customerStatus.setReadingCount(0);
                        }

                        if (hasReading) {
                            customersWithReadings.add(customerStatus);
                        } else {
                            customersWithoutReadings.add(customerStatus);
                        }
                    }

                    ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
                    areaStatus.setAreaCode(area.getAreaCode());
                    areaStatus.setAreaName(area.getAreaName());
                    areaStatus.setProvinceCode(area.getProvCode());
                    areaStatus.setProvinceName(area.getProvinceName());
                    areaStatus.setRegionCode(area.getRegion());
                    areaStatus.setActiveBillCycle(activeBillCycle);
                    areaStatus.setTotalCustomers(areaDevelopers.size());
                    areaStatus.setCustomersWithReadings(customersWithReadings.size());
                    areaStatus.setCustomersWithoutReadings(customersWithoutReadings.size());

                        double percentage = areaDevelopers.size() > 0 ?
                            (double) customersWithReadings.size() / areaDevelopers.size() * 100.0 : 0.0;
                    areaStatus.setReadingPercentage(Math.round(percentage * 100.0) / 100.0);

                    if (includeCustomerDetails) {
                        areaStatus.setCustomersWithReadingsList(customersWithReadings);
                        areaStatus.setCustomersWithoutReadingsList(customersWithoutReadings);
                    }

                    result.add(areaStatus);
                } catch (Exception e) {
                    System.err.println("ERROR processing area " + area.getAreaCode() + ": " + e.getMessage());
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("ERROR in getAllAreasReadingStatus: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get reading status for all areas: " + e.getMessage(), e);
        }
    }

    /**
     * Get pending readings (customers without readings) for an area
     */
    public ReadingStatusDTO.PendingReadingsDTO getPendingReadingsForArea(String areaCode) {
        return getPendingReadingsForArea(areaCode, null);
    }

    /**
     * Get pending readings for an area with optional EE filtering
     */
    public ReadingStatusDTO.PendingReadingsDTO getPendingReadingsForArea(String areaCode, String eeUserId) {
        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(areaCode, true, false, eeUserId);
            
            if (areaStatus.isEmpty()) {
                return new ReadingStatusDTO.PendingReadingsDTO();
            }

            ReadingStatusDTO.AreaReadingStatusDTO status = areaStatus.get(0);
            
            // Include ALL developers (with and without readings) so existing readings can be updated
            List<ReadingStatusDTO.CustomerReadingStatusDTO> allDevelopers = new ArrayList<>();
            if (status.getCustomersWithoutReadingsList() != null) {
                allDevelopers.addAll(status.getCustomersWithoutReadingsList());
            }
            if (status.getCustomersWithReadingsList() != null) {
                allDevelopers.addAll(status.getCustomersWithReadingsList());
            }

            ReadingStatusDTO.PendingReadingsDTO pendingReadings = new ReadingStatusDTO.PendingReadingsDTO();
            pendingReadings.setAreaCode(status.getAreaCode());
            pendingReadings.setAreaName(status.getAreaName());
            pendingReadings.setActiveBillCycle(status.getActiveBillCycle());
            pendingReadings.setPendingCustomers(allDevelopers);
            pendingReadings.setPendingCount(allDevelopers.size());

            return pendingReadings;

        } catch (Exception e) {
            System.err.println("ERROR in getPendingReadingsForArea for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get pending readings for area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get completed readings (customers with readings) for an area
     */
    public ReadingStatusDTO.CompletedReadingsDTO getCompletedReadingsForArea(String areaCode) {
        return getCompletedReadingsForArea(areaCode, null);
    }

    /**
     * Get completed readings for an area with optional EE filtering
     */
    public ReadingStatusDTO.CompletedReadingsDTO getCompletedReadingsForArea(String areaCode, String eeUserId) {
        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = getAreaReadingStatus(areaCode, true, true, eeUserId);
            
            if (areaStatus.isEmpty()) {
                return new ReadingStatusDTO.CompletedReadingsDTO();
            }

            ReadingStatusDTO.AreaReadingStatusDTO status = areaStatus.get(0);
            
            ReadingStatusDTO.CompletedReadingsDTO completedReadings = new ReadingStatusDTO.CompletedReadingsDTO();
            completedReadings.setAreaCode(status.getAreaCode());
            completedReadings.setAreaName(status.getAreaName());
            completedReadings.setActiveBillCycle(status.getActiveBillCycle());
            completedReadings.setCompletedCustomers(status.getCustomersWithReadingsList());
            completedReadings.setCompletedCount(status.getCustomersWithReadings());

            return completedReadings;

        } catch (Exception e) {
            System.err.println("ERROR in getCompletedReadingsForArea for area " + areaCode + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get completed readings for area " + areaCode + ": " + e.getMessage(), e);
        }
    }

    // Helper methods
    private List<Object[]> filterDevelopersForEe(List<Object[]> developers, String eeUserId) {
        if (eeUserId == null || eeUserId.trim().isEmpty()) return developers;
        return developers.stream()
                .filter(developer -> SessionUtils.matchEE(valueAt(developer, 7), eeUserId))
                .collect(Collectors.toList());
    }

    private String valueAt(Object[] row, int index) {
        return row != null && row.length > index && row[index] != null ? row[index].toString().trim() : "";
    }

    private Short shortAt(Object[] row, int index) {
        String value = valueAt(row, index);
        if (value.isEmpty()) return null;
        try {
            return Short.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String toCustomerNcreType(String ncreType) {
        if (ncreType == null) return null;
        switch (ncreType.trim().toUpperCase(Locale.ROOT)) {
            case "SPP": return "S";
            case "MHP": return "M";
            case "DPP": return "D";
            case "BMP": return "B";
            case "WPP":
            case "WHP": return "W";
            default: return ncreType.trim();
        }
    }

    private ReadingStatusDTO.CustomerReadingStatusDTO convertToDeveloperStatusDTO(Object[] developer,
            boolean hasReading, boolean includeDetails, boolean includeReadingDetails, Integer billCycle,
            Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        ReadingStatusDTO.CustomerReadingStatusDTO dto = new ReadingStatusDTO.CustomerReadingStatusDTO();
        String accNbr = valueAt(developer, 0);
        Short folioNo = shortAt(developer, 1);
        String areaCode = valueAt(developer, 2);
        String tariffType = valueAt(developer, 6);

        dto.setAccNbr(accNbr);
        dto.setFolioNo(folioNo);
        dto.setNcreType(toCustomerNcreType(valueAt(developer, 3)));
        dto.setFacilityName(valueAt(developer, 5));
        dto.setResponsibleEe(valueAt(developer, 7));
        dto.setHasReading(hasReading);
        dto.setTariffDesc(tariffByAcc.getOrDefault(accNbr, tariffByFolio.get(folioNo)));
        if (dto.getTariffDesc() == null || dto.getTariffDesc().isEmpty()) {
            dto.setTariffDesc(tariffType);
        }
        dto.setTariffType(dto.getTariffDesc());
        dto.setTariff(dto.getTariffDesc());

        if (includeDetails) {
            dto.setName(valueAt(developer, 4));
            dto.setAddressL1(valueAt(developer, 8));
            dto.setAreaCd(areaCode);
            dto.setBillCycle(billCycle);
            dto.setTelNbr(valueAt(developer, 9));
        }
        if (!includeReadingDetails) {
            dto.setReadings(new ArrayList<>());
            dto.setReadingCount(0);
        }
        return dto;
    }

    private ReadingStatusDTO.AreaReadingStatusDTO createAreaStatusWithoutActiveBillCycle(HsbAreaDTO area,
            boolean includeCustomerDetails, List<Object[]> developers,
            Map<String, String> tariffByAcc, Map<Short, String> tariffByFolio) {
        
        ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
        areaStatus.setAreaCode(area.getAreaCode());
        areaStatus.setAreaName(area.getAreaName());
        areaStatus.setProvinceCode(area.getProvCode());
        areaStatus.setProvinceName(area.getProvinceName());
        areaStatus.setRegionCode(area.getRegion());
        areaStatus.setActiveBillCycle(null);
        areaStatus.setTotalCustomers(developers.size());
        areaStatus.setCustomersWithReadings(0);
        areaStatus.setCustomersWithoutReadings(developers.size());
        areaStatus.setReadingPercentage(0.0);
        
        if (includeCustomerDetails) {
                List<ReadingStatusDTO.CustomerReadingStatusDTO> customerList = developers.stream()
                    .map(developer -> convertToDeveloperStatusDTO(developer, false, true, false, null,
                        tariffByAcc, tariffByFolio))
                    .collect(Collectors.toList());
            
            areaStatus.setCustomersWithReadingsList(new ArrayList<>());
            areaStatus.setCustomersWithoutReadingsList(customerList);
        }
        
        return areaStatus;
    }

    private ReadingStatusDTO.AreaReadingStatusDTO createEmptyAreaStatus(HsbAreaDTO area, Integer activeBillCycle) {
        ReadingStatusDTO.AreaReadingStatusDTO areaStatus = new ReadingStatusDTO.AreaReadingStatusDTO();
        areaStatus.setAreaCode(area.getAreaCode());
        areaStatus.setAreaName(area.getAreaName());
        areaStatus.setProvinceCode(area.getProvCode());
        areaStatus.setProvinceName(area.getProvinceName());
        areaStatus.setRegionCode(area.getRegion());
        areaStatus.setActiveBillCycle(activeBillCycle);
        areaStatus.setTotalCustomers(0);
        areaStatus.setCustomersWithReadings(0);
        areaStatus.setCustomersWithoutReadings(0);
        areaStatus.setReadingPercentage(0.0);
        areaStatus.setCustomersWithReadingsList(new ArrayList<>());
        areaStatus.setCustomersWithoutReadingsList(new ArrayList<>());
        
        return areaStatus;
    }

    private ReadingStatusDTO.ReadingStatusSummaryDTO createSummary(List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus) {
        int totalAreas = areaReadingStatus.size();
        int totalCustomers = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getTotalCustomers).sum();
        int totalCustomersWithReadings = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithReadings).sum();
        int totalCustomersWithoutReadings = areaReadingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithoutReadings).sum();
        
        double overallPercentage = totalCustomers > 0 ? (double) totalCustomersWithReadings / totalCustomers * 100.0 : 0.0;
        
        int areasWithActiveBillCycles = (int) areaReadingStatus.stream()
                .filter(area -> area.getActiveBillCycle() != null)
                .count();
        
        ReadingStatusDTO.ReadingStatusSummaryDTO summary = new ReadingStatusDTO.ReadingStatusSummaryDTO();
        summary.setTotalAreas(totalAreas);
        summary.setTotalCustomers(totalCustomers);
        summary.setTotalCustomersWithReadings(totalCustomersWithReadings);
        summary.setTotalCustomersWithoutReadings(totalCustomersWithoutReadings);
        summary.setOverallReadingPercentage(Math.round(overallPercentage * 100.0) / 100.0);
        summary.setAreasWithActiveBillCycles(areasWithActiveBillCycles);
        summary.setAreasWithoutActiveBillCycles(totalAreas - areasWithActiveBillCycles);
        
        return summary;
    }

    private ReadingStatusDTO.ReadingStatusResponse createSuccessResponse(String userCategory, 
            List<ReadingStatusDTO.AreaReadingStatusDTO> areaReadingStatus, 
            ReadingStatusDTO.ReadingStatusSummaryDTO summary) {
        ReadingStatusDTO.ReadingStatusResponse response = new ReadingStatusDTO.ReadingStatusResponse();
        response.setSuccess(true);
        response.setMessage("Reading status retrieved successfully");
        response.setUserCategory(userCategory);
        response.setAreaReadingStatus(areaReadingStatus);
        response.setSummary(summary);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private ReadingStatusDTO.ReadingStatusResponse createErrorResponse(String message) {
        ReadingStatusDTO.ReadingStatusResponse response = new ReadingStatusDTO.ReadingStatusResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setAreaReadingStatus(new ArrayList<>());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}