package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BulkCustomerDTO;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.SPSProjectBackend.dto.BulkCustomerLocationUpdateRequest;
import com.example.SPSProjectBackend.dto.BulkCustomerStatusUpdateRequest;
import com.example.SPSProjectBackend.dto.MapDataDTO;

@Service
@Transactional
public class BulkCustomerService {

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    // Get all customers
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getAllCustomers() {
        return getAllCustomers(null);
    }

    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getAllCustomers(String eeUserId) {
        return getCustomersForAreas(null, eeUserId);
    }

    /**
     * Developers restricted to a set of areas. A null or empty areaCodes means every area.
     * The caller is responsible for supplying the areas the user is permitted to read.
     */
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersForAreas(List<String> areaCodes, String eeUserId) {
        try {
            List<NcreDeveloper> allDevelopers = ncreDeveloperRepository.findAllByOrderByFolioNoAsc();
            List<BulkCustomerDTO> dtos = allDevelopers.stream()
                    .map(this::convertDeveloperToDTO)
                    .collect(Collectors.toList());

            if (areaCodes != null && !areaCodes.isEmpty()) {
                java.util.Set<String> allowed = areaCodes.stream()
                        .map(UserAreaPermissionService::normalizeAreaCode)
                        .collect(Collectors.toSet());
                dtos = dtos.stream()
                        .filter(dto -> allowed.contains(
                                UserAreaPermissionService.normalizeAreaCode(dto.getAreaCd())))
                        .collect(Collectors.toList());
            }

            if (eeUserId != null && !eeUserId.trim().isEmpty()) {
                dtos = dtos.stream()
                        .filter(dto -> com.example.SPSProjectBackend.util.SessionUtils.matchEE(dto.getResponsibleEe(),
                                eeUserId))
                        .collect(Collectors.toList());
            }

            return dtos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all NCRE developers: " + e.getMessage(), e);
        }
    }

    // Get customer by account number
    @Transactional(readOnly = true)
    public Optional<BulkCustomerDTO> getCustomerByAccNbr(String accNbr) {
        try {
            if (accNbr == null) return Optional.empty();
            Optional<NcreDeveloper> devOpt = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr);
            if (devOpt.isPresent()) {
                return devOpt.map(this::convertDeveloperToDTO);
            }
            Optional<BulkCustomer> customer = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
            return customer.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to retrieve customer by account number: " + accNbr + " - " + e.getMessage(), e);
        }
    }

    // Get customers by area code - FROM NCRE_DEVELOPERS TABLE ONLY
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByAreaCd(String areaCd) {
        return getCustomersByAreaCd(areaCd, null);
    }

    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByAreaCd(String areaCd, String eeUserId) {
        try {
            List<NcreDeveloper> developers = ncreDeveloperRepository.findByAreaCodeTrimmed(areaCd);
            if (developers.isEmpty()) {
                String normTarget = normalizeAreaCode(areaCd);
                developers = ncreDeveloperRepository.findAll().stream()
                        .filter(d -> normalizeAreaCode(d.getArea()).equalsIgnoreCase(normTarget))
                        .collect(Collectors.toList());
            }

            List<BulkCustomerDTO> dtos = developers.stream()
                    .map(this::convertDeveloperToDTO)
                    .collect(Collectors.toList());

            if (eeUserId != null && !eeUserId.trim().isEmpty()) {
                dtos = dtos.stream()
                        .filter(dto -> com.example.SPSProjectBackend.util.SessionUtils.matchEE(dto.getResponsibleEe(),
                                eeUserId))
                        .collect(Collectors.toList());
            }

            return dtos;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve NCRE developers by area code: " + e.getMessage(), e);
        }
    }

    private String normalizeAreaCode(String areaCode) {
        return areaCode == null ? "" : areaCode.trim().replaceFirst("^0+(?!$)", "");
    }

    public BulkCustomerDTO convertDeveloperToDTO(NcreDeveloper dev) {
        if (dev == null) {
            return null;
        }
        BulkCustomerDTO dto = new BulkCustomerDTO();
        dto.setAccNbr(dev.getAccNbr() != null ? dev.getAccNbr().trim() : null);
        dto.setFolioNo(dev.getFolioNo());
        dto.setName(dev.getDeveloperName());
        dto.setFacilityName(dev.getFacilityName());
        dto.setResponsibleEe(dev.getResponsibleEe());
        dto.setCusCat(dev.getCusCat());
        dto.setNcreType(dev.getType());
        dto.setAreaCd(dev.getArea() != null ? dev.getArea().trim() : null);
        dto.setTariff(dev.getTariffType());
        dto.setAddressL1(dev.getAddressLine1());
        dto.setAddressL2(dev.getAddressLine2());
        dto.setCity(dev.getAddressLine3() != null ? dev.getAddressLine3() : dev.getProvince());
        dto.setTelNbr(dev.getTelephone());
        dto.setCnectDate(dev.getGridConnectionDate());
        dto.setAgrmntNo(dev.getAgreementType());
        dto.setLatitude(dev.getLatitude());
        dto.setLongitude(dev.getLongitude());
        if (dev.getStatus() != null && !dev.getStatus().trim().isEmpty()) {
            try {
                dto.setStatus(Integer.parseInt(dev.getStatus().trim()));
            } catch (Exception e) {
                dto.setStatus(null);
            }
        } else {
            dto.setStatus(2);
        }
        return dto;
    }


    // Get customers by zone
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByZone(String zone) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByZone(zone);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by zone: " + e.getMessage(), e);
        }
    }

    // Get customers by zone and area
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByZoneAndArea(String zone, String areaCd) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByZoneAndAreaCd(zone, areaCd);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by zone and area: " + e.getMessage(), e);
        }
    }

    // Search customers by name - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomersByName(String name) {
        try {
            String searchPattern = "%" + name + "%";
            List<BulkCustomer> customers = bulkCustomerRepository.findByNameContainingTrimmed(searchPattern);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customers by name: " + e.getMessage(), e);
        }
    }

    // Get customers by tariff - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByTariff(String tariff) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByTariffTrimmed(tariff);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by tariff: " + e.getMessage(), e);
        }
    }

    // Get customers by operational status - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByOpStat(String opStat) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByOpStatTrimmed(opStat);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by operational status: " + e.getMessage(), e);
        }
    }

    // Get customer by mobile number - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public Optional<BulkCustomerDTO> getCustomerByMobileNo(String mobileNo) {
        try {
            Optional<BulkCustomer> customer = bulkCustomerRepository.findByMobileNoTrimmed(mobileNo);
            return customer.map(this::convertToDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer by mobile number: " + e.getMessage(), e);
        }
    }

    // Get customers by city - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByCity(String city) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByCityTrimmed(city);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by city: " + e.getMessage(), e);
        }
    }

    // Get customers by bill cycle
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByBillCycle(Integer billCycle) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByBillCycle(billCycle);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by bill cycle: " + e.getMessage(), e);
        }
    }

    // Search customers by multiple criteria - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomersByMultipleCriteria(String areaCd, String zone, String tariff,
            String opStat, String cusCat) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByMultipleCriteriaTrimmed(areaCd, zone, tariff,
                    opStat, cusCat);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search customers by multiple criteria: " + e.getMessage(), e);
        }
    }

    // General search customers
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> searchCustomers(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllCustomers();
            }
            String term = searchTerm.trim().toLowerCase();
            List<NcreDeveloper> all = ncreDeveloperRepository.findAll();
            return all.stream()
                    .filter(d -> (d.getAccNbr() != null && d.getAccNbr().toLowerCase().contains(term)) ||
                            (d.getDeveloperName() != null && d.getDeveloperName().toLowerCase().contains(term)) ||
                            (d.getFacilityName() != null && d.getFacilityName().toLowerCase().contains(term)) ||
                            (d.getFolioNo() != null && String.valueOf(d.getFolioNo()).contains(term)) ||
                            (d.getArea() != null && d.getArea().toLowerCase().contains(term)))
                    .map(this::convertDeveloperToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search NCRE developers: " + e.getMessage(), e);
        }
    }

    // Create new customer - USING TRIMMED VERSION FOR VALIDATION
    @Transactional
    public BulkCustomerDTO createCustomer(BulkCustomerDTO customerDTO) {
        try {
            // Validate required fields
            validateCustomer(customerDTO);

            // Check if customer already exists - USING TRIMMED VERSION
            if (bulkCustomerRepository.existsByAccNbrTrimmed(customerDTO.getAccNbr())) {
                throw new RuntimeException(
                        "Customer with account number " + customerDTO.getAccNbr() + " already exists");
            }

            BulkCustomer customer = convertToEntity(customerDTO);

            // Set timestamps
            customer.setEnteredDtime(new Date());

            BulkCustomer savedCustomer = bulkCustomerRepository.save(customer);
            bulkCustomerRepository.flush();

            return convertToDTO(savedCustomer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer: " + e.getMessage(), e);
        }
    }

    // Update NCRE customer GPS location - ONLY update latitude and longitude
    @Transactional
    public void updateCustomerLocation(BulkCustomerLocationUpdateRequest request) {
        if (request == null || request.getAcc_nbr() == null || request.getLatitude() == null
                || request.getLongitude() == null) {
            throw new IllegalArgumentException("Account number, latitude, and longitude are required");
        }

        String accNbr = request.getAcc_nbr().trim();

        // Update in NcreDeveloper table instead of BulkCustomer table
        Optional<NcreDeveloper> ncreDeveloperOpt = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr);

        if (!ncreDeveloperOpt.isPresent()) {
            throw new IllegalArgumentException("NCRE Developer with account number " + accNbr + " not found");
        }

        NcreDeveloper ncreDeveloper = ncreDeveloperOpt.get();
        ncreDeveloper.setLatitude(request.getLatitude());
        ncreDeveloper.setLongitude(request.getLongitude());

        // Save and flush
        ncreDeveloperRepository.save(ncreDeveloper);
        ncreDeveloperRepository.flush();
    }

    // Update developer status (1 = Inactive, 2 = Active)
    @Transactional
    public void updateDeveloperStatus(BulkCustomerStatusUpdateRequest request) {
        if (request == null || request.getAcc_nbr() == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Account number and status are required");
        }

        if (request.getStatus() != 1 && request.getStatus() != 2) {
            throw new IllegalArgumentException("Status must be 1 (Inactive) or 2 (Active)");
        }

        String accNbr = request.getAcc_nbr().trim();

        Optional<NcreDeveloper> ncreDeveloperOpt = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr);

        if (!ncreDeveloperOpt.isPresent()) {
            throw new IllegalArgumentException("NCRE Developer with account number " + accNbr + " not found");
        }

        NcreDeveloper ncreDeveloper = ncreDeveloperOpt.get();
        ncreDeveloper.setStatus(String.valueOf(request.getStatus()));

        ncreDeveloperRepository.save(ncreDeveloper);
        ncreDeveloperRepository.flush();
    }

    // Update existing customer - USING TRIMMED VERSION FOR LOOKUP
    @Transactional
    public BulkCustomerDTO updateCustomer(String accNbr, BulkCustomerDTO customerDTO) {
        try {
            // Validate required fields
            validateCustomer(customerDTO);

            // Use trimmed version for lookup
            Optional<BulkCustomer> existingCustomer = bulkCustomerRepository.findByAccNbrTrimmed(accNbr);
            if (!existingCustomer.isPresent()) {
                throw new RuntimeException("Customer with account number " + accNbr + " not found");
            }

            BulkCustomer customer = existingCustomer.get();

            // Update fields
            updateCustomerFields(customer, customerDTO);

            // Set edit timestamp
            customer.setEditedDtime(new Date());
            if (customerDTO.getEditedUserId() != null) {
                customer.setEditedUserId(customerDTO.getEditedUserId());
            }

            BulkCustomer updatedCustomer = bulkCustomerRepository.save(customer);
            bulkCustomerRepository.flush();

            return convertToDTO(updatedCustomer);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    // Get distinct values for dropdowns
    @Transactional(readOnly = true)
    public List<String> getDistinctAreaCodes() {
        try {
            return bulkCustomerRepository.findDistinctAreaCodes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct area codes: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctZones() {
        try {
            return bulkCustomerRepository.findDistinctZones();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct zones: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctTariffs() {
        try {
            return bulkCustomerRepository.findDistinctTariffs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct tariffs: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCusCategories() {
        try {
            return bulkCustomerRepository.findDistinctCusCategories();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct customer categories: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        try {
            return bulkCustomerRepository.findDistinctCities();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve distinct cities: " + e.getMessage(), e);
        }
    }

    // Count methods - USING TRIMMED VERSIONS
    @Transactional(readOnly = true)
    public Long countCustomersByArea(String areaCd) {
        try {
            Long count = ncreDeveloperRepository.countByAreaCodeTrimmed(areaCd);
            if (count == null || count == 0) {
                String norm = normalizeAreaCode(areaCd);
                count = ncreDeveloperRepository.findAll().stream()
                        .filter(d -> normalizeAreaCode(d.getArea()).equalsIgnoreCase(norm))
                        .count();
            }
            return count;
        } catch (Exception e) {
            throw new RuntimeException("Failed to count NCRE developers by area: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Long countCustomersByZone(String zone) {
        try {
            return bulkCustomerRepository.countByZone(zone);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count customers by zone: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Long countCustomersByOpStat(String opStat) {
        try {
            return bulkCustomerRepository.countByOpStat(opStat);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count customers by operational status: " + e.getMessage(), e);
        }
    }

    // Get customers by multiple account numbers - USING TRIMMED VERSIONS
    @Transactional(readOnly = true)
    public List<BulkCustomerDTO> getCustomersByAccNbrs(List<String> accNbrs, String areaCd) {
        try {
            List<BulkCustomer> customers = bulkCustomerRepository.findByAccNbrInAndAreaCd(accNbrs, areaCd);
            return customers.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers by account numbers: " + e.getMessage(), e);
        }
    }

    // Check if customer exists - USING TRIMMED VERSION
    @Transactional(readOnly = true)
    public boolean customerExists(String accNbr) {
        try {
            return bulkCustomerRepository.existsByAccNbrTrimmed(accNbr);
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to validate customer
    private void validateCustomer(BulkCustomerDTO customerDTO) {
        if (customerDTO.getAccNbr() == null || customerDTO.getAccNbr().trim().isEmpty()) {
            throw new RuntimeException("Account number is required");
        }
        if (customerDTO.getName() == null || customerDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("Customer name is required");
        }
        if (customerDTO.getAddressL1() == null || customerDTO.getAddressL1().trim().isEmpty()) {
            throw new RuntimeException("Address line 1 is required");
        }
        if (customerDTO.getTariff() == null || customerDTO.getTariff().trim().isEmpty()) {
            throw new RuntimeException("Tariff is required");
        }

        // Validate account number length
        if (customerDTO.getAccNbr().length() > 10) {
            throw new RuntimeException("Account number cannot exceed 10 characters");
        }

        // Validate name length
        if (customerDTO.getName().length() > 45) {
            throw new RuntimeException("Customer name cannot exceed 45 characters");
        }

        // Validate address length
        if (customerDTO.getAddressL1().length() > 45) {
            throw new RuntimeException("Address line 1 cannot exceed 45 characters");
        }

        // Validate tariff length
        if (customerDTO.getTariff().length() > 3) {
            throw new RuntimeException("Tariff cannot exceed 3 characters");
        }
    }

    // Helper method to update customer fields
    private void updateCustomerFields(BulkCustomer customer, BulkCustomerDTO dto) {
        if (dto.getJobNbr() != null)
            customer.setJobNbr(dto.getJobNbr());
        if (dto.getAreaCd() != null)
            customer.setAreaCd(dto.getAreaCd());
        if (dto.getBillCycle() != null)
            customer.setBillCycle(dto.getBillCycle());
        if (dto.getFolioNo() != null)
            customer.setFolioNo(dto.getFolioNo());
        if (dto.getCusCat() != null)
            customer.setCusCat(dto.getCusCat());
        if (dto.getRedCode() != null)
            customer.setRedCode(dto.getRedCode());
        if (dto.getDlyPack() != null)
            customer.setDlyPack(dto.getDlyPack());
        if (dto.getWlkOrd() != null)
            customer.setWlkOrd(dto.getWlkOrd());
        if (dto.getNatSup() != null)
            customer.setNatSup(dto.getNatSup());
        if (dto.getName() != null)
            customer.setName(dto.getName());
        if (dto.getAddressL1() != null)
            customer.setAddressL1(dto.getAddressL1());
        if (dto.getAddressL2() != null)
            customer.setAddressL2(dto.getAddressL2());
        if (dto.getCity() != null)
            customer.setCity(dto.getCity());
        if (dto.getPCode() != null)
            customer.setPCode(dto.getPCode());
        if (dto.getTelNbr() != null)
            customer.setTelNbr(dto.getTelNbr());
        if (dto.getIdNbr() != null)
            customer.setIdNbr(dto.getIdNbr());
        if (dto.getIdType() != null)
            customer.setIdType(dto.getIdType());
        if (dto.getIssuedDt() != null)
            customer.setIssuedDt(dto.getIssuedDt());
        if (dto.getNoOfPhases() != null)
            customer.setNoOfPhases(dto.getNoOfPhases());
        if (dto.getEstAmnt() != null)
            customer.setEstAmnt(dto.getEstAmnt());
        if (dto.getEspayDt() != null)
            customer.setEspayDt(dto.getEspayDt());
        if (dto.getEstPivNbr() != null)
            customer.setEstPivNbr(dto.getEstPivNbr());
        if (dto.getCdPrmses() != null)
            customer.setCdPrmses(dto.getCdPrmses());
        if (dto.getIndType() != null)
            customer.setIndType(dto.getIndType());
        if (dto.getDepositAmt() != null)
            customer.setDepositAmt(dto.getDepositAmt());
        if (dto.getDepDate() != null)
            customer.setDepDate(dto.getDepDate());
        if (dto.getDepPivNbr() != null)
            customer.setDepPivNbr(dto.getDepPivNbr());
        if (dto.getAddDepAmt() != null)
            customer.setAddDepAmt(dto.getAddDepAmt());
        if (dto.getAddDepDate() != null)
            customer.setAddDepDate(dto.getAddDepDate());
        if (dto.getAddDepPiv() != null)
            customer.setAddDepPiv(dto.getAddDepPiv());
        if (dto.getTotSecDep() != null)
            customer.setTotSecDep(dto.getTotSecDep());
        if (dto.getCntrDmnd() != null)
            customer.setCntrDmnd(dto.getCntrDmnd());
        if (dto.getTariff() != null)
            customer.setTariff(dto.getTariff());
        if (dto.getGstApl() != null)
            customer.setGstApl(dto.getGstApl());
        if (dto.getTaxInv() != null)
            customer.setTaxInv(dto.getTaxInv());
        if (dto.getTaxNum() != null)
            customer.setTaxNum(dto.getTaxNum());
        if (dto.getAuthLetter() != null)
            customer.setAuthLetter(dto.getAuthLetter());
        if (dto.getAgrmntNo() != null)
            customer.setAgrmntNo(dto.getAgrmntNo());
        if (dto.getCnectDate() != null)
            customer.setCnectDate(dto.getCnectDate());
        if (dto.getOpStat() != null)
            customer.setOpStat(dto.getOpStat());
        if (dto.getAhArhStat() != null)
            customer.setAhArhStat(dto.getAhArhStat());
        if (dto.getAltAddrStat() != null)
            customer.setAltAddrStat(dto.getAltAddrStat());
        if (dto.getAdlDpstSt() != null)
            customer.setAdlDpstSt(dto.getAdlDpstSt());
        if (dto.getSlfGenSt() != null)
            customer.setSlfGenSt(dto.getSlfGenSt());
        if (dto.getSupsdAcc() != null)
            customer.setSupsdAcc(dto.getSupsdAcc());
        if (dto.getRefundDep() != null)
            customer.setRefundDep(dto.getRefundDep());
        if (dto.getNoLoans() != null)
            customer.setNoLoans(dto.getNoLoans());
        if (dto.getCstSt() != null)
            customer.setCstSt(dto.getCstSt());
        if (dto.getInstId() != null)
            customer.setInstId(dto.getInstId());
        if (dto.getZone() != null)
            customer.setZone(dto.getZone());
        if (dto.getDateAddt() != null)
            customer.setDateAddt(dto.getDateAddt());
        if (dto.getCustCd() != null)
            customer.setCustCd(dto.getCustCd());
        if (dto.getUserId() != null)
            customer.setUserId(dto.getUserId());
        if (dto.getMobileNo() != null)
            customer.setMobileNo(dto.getMobileNo());
        if (dto.getCustType() != null)
            customer.setCustType(dto.getCustType());
        if (dto.getNetType() != null)
            customer.setNetType(dto.getNetType());
        if (dto.getCatCode() != null)
            customer.setCatCode(dto.getCatCode());
    }

    // Convert Entity to DTO
    private BulkCustomerDTO convertToDTO(BulkCustomer customer) {
        return convertToDTO(customer, null, null);
    }

    private BulkCustomerDTO convertToDTO(BulkCustomer customer, Map<String, NcreDeveloper> devByAcc,
            Map<Short, NcreDeveloper> devByFolio) {
        BulkCustomerDTO dto = new BulkCustomerDTO();
        dto.setAccNbr(customer.getAccNbr());
        dto.setJobNbr(customer.getJobNbr());
        dto.setAreaCd(customer.getAreaCd());
        dto.setBillCycle(customer.getBillCycle());

        NcreDeveloper developer = null;
        if (customer.getAccNbr() != null && devByAcc != null) {
            developer = devByAcc.get(customer.getAccNbr().trim());
        }
        if (developer == null && customer.getFolioNo() != null && devByFolio != null) {
            developer = devByFolio.get(customer.getFolioNo());
        }

        // Fallback for single customer lookups if maps were not provided
        if (developer == null && devByAcc == null && customer.getAccNbr() != null) {
            Optional<NcreDeveloper> ncreDeveloper = ncreDeveloperRepository.findByAccNbrTrimmed(customer.getAccNbr());
            if (!ncreDeveloper.isPresent() && customer.getFolioNo() != null) {
                ncreDeveloper = ncreDeveloperRepository.findByFolioNo(customer.getFolioNo());
            }
            if (ncreDeveloper.isPresent()) {
                developer = ncreDeveloper.get();
            }
        }

        if (developer != null) {
            dto.setFacilityName(developer.getFacilityName());
            dto.setResponsibleEe(developer.getResponsibleEe());
            if (developer.getStatus() != null) {
                try {
                    dto.setStatus(Integer.parseInt(developer.getStatus().trim()));
                } catch (Exception e) {
                    dto.setStatus(null);
                }
            }
        }

        dto.setFolioNo(customer.getFolioNo());

        dto.setCusCat(customer.getCusCat());
        dto.setRedCode(customer.getRedCode());
        dto.setDlyPack(customer.getDlyPack());
        dto.setWlkOrd(customer.getWlkOrd());
        dto.setNatSup(customer.getNatSup());
        dto.setName(customer.getName());
        dto.setAddressL1(customer.getAddressL1());
        dto.setAddressL2(customer.getAddressL2());
        dto.setCity(customer.getCity());
        dto.setPCode(customer.getPCode());
        dto.setTelNbr(customer.getTelNbr());
        dto.setIdNbr(customer.getIdNbr());
        dto.setIdType(customer.getIdType());
        dto.setIssuedDt(customer.getIssuedDt());
        dto.setNoOfPhases(customer.getNoOfPhases());
        dto.setEstAmnt(customer.getEstAmnt());
        dto.setEspayDt(customer.getEspayDt());
        dto.setEstPivNbr(customer.getEstPivNbr());
        dto.setCdPrmses(customer.getCdPrmses());
        dto.setIndType(customer.getIndType());
        dto.setDepositAmt(customer.getDepositAmt());
        dto.setDepDate(customer.getDepDate());
        dto.setDepPivNbr(customer.getDepPivNbr());
        dto.setAddDepAmt(customer.getAddDepAmt());
        dto.setAddDepDate(customer.getAddDepDate());
        dto.setAddDepPiv(customer.getAddDepPiv());
        dto.setTotSecDep(customer.getTotSecDep());
        dto.setCntrDmnd(customer.getCntrDmnd());
        dto.setTariff(customer.getTariff());
        dto.setGstApl(customer.getGstApl());
        dto.setTaxInv(customer.getTaxInv());
        dto.setTaxNum(customer.getTaxNum());
        dto.setAuthLetter(customer.getAuthLetter());
        dto.setAgrmntNo(customer.getAgrmntNo());
        dto.setCnectDate(customer.getCnectDate());
        dto.setOpStat(customer.getOpStat());
        dto.setAhArhStat(customer.getAhArhStat());
        dto.setAltAddrStat(customer.getAltAddrStat());
        dto.setAdlDpstSt(customer.getAdlDpstSt());
        dto.setSlfGenSt(customer.getSlfGenSt());
        dto.setSupsdAcc(customer.getSupsdAcc());
        dto.setRefundDep(customer.getRefundDep());
        dto.setNoLoans(customer.getNoLoans());
        dto.setCstSt(customer.getCstSt());
        dto.setInstId(customer.getInstId());
        dto.setZone(customer.getZone());
        dto.setDateAddt(customer.getDateAddt());
        dto.setCustCd(customer.getCustCd());
        dto.setEnteredDtime(customer.getEnteredDtime());
        dto.setEditedDtime(customer.getEditedDtime());
        dto.setUserId(customer.getUserId());
        dto.setEditedUserId(customer.getEditedUserId());
        dto.setMobileNo(customer.getMobileNo());
        dto.setCustType(customer.getCustType());
        dto.setNetType(customer.getNetType());
        dto.setCatCode(customer.getCatCode());
        dto.setNcreType(customer.getNcre_type());

        return dto;
    }

    // Convert DTO to Entity
    private BulkCustomer convertToEntity(BulkCustomerDTO dto) {
        BulkCustomer customer = new BulkCustomer();
        customer.setAccNbr(dto.getAccNbr());
        customer.setJobNbr(dto.getJobNbr());
        customer.setAreaCd(dto.getAreaCd());
        customer.setBillCycle(dto.getBillCycle());
        customer.setFolioNo(dto.getFolioNo());
        customer.setCusCat(dto.getCusCat());
        customer.setRedCode(dto.getRedCode());
        customer.setDlyPack(dto.getDlyPack());
        customer.setWlkOrd(dto.getWlkOrd());
        customer.setNatSup(dto.getNatSup());
        customer.setName(dto.getName());
        customer.setAddressL1(dto.getAddressL1());
        customer.setAddressL2(dto.getAddressL2());
        customer.setCity(dto.getCity());
        customer.setPCode(dto.getPCode());
        customer.setTelNbr(dto.getTelNbr());
        customer.setIdNbr(dto.getIdNbr());
        customer.setIdType(dto.getIdType());
        customer.setIssuedDt(dto.getIssuedDt());
        customer.setNoOfPhases(dto.getNoOfPhases());
        customer.setEstAmnt(dto.getEstAmnt());
        customer.setEspayDt(dto.getEspayDt());
        customer.setEstPivNbr(dto.getEstPivNbr());
        customer.setCdPrmses(dto.getCdPrmses());
        customer.setIndType(dto.getIndType());
        customer.setDepositAmt(dto.getDepositAmt());
        customer.setDepDate(dto.getDepDate());
        customer.setDepPivNbr(dto.getDepPivNbr());
        customer.setAddDepAmt(dto.getAddDepAmt());
        customer.setAddDepDate(dto.getAddDepDate());
        customer.setAddDepPiv(dto.getAddDepPiv());
        customer.setTotSecDep(dto.getTotSecDep());
        customer.setCntrDmnd(dto.getCntrDmnd());
        customer.setTariff(dto.getTariff());
        customer.setGstApl(dto.getGstApl());
        customer.setTaxInv(dto.getTaxInv());
        customer.setTaxNum(dto.getTaxNum());
        customer.setAuthLetter(dto.getAuthLetter());
        customer.setAgrmntNo(dto.getAgrmntNo());
        customer.setCnectDate(dto.getCnectDate());
        customer.setOpStat(dto.getOpStat());
        customer.setAhArhStat(dto.getAhArhStat());
        customer.setAltAddrStat(dto.getAltAddrStat());
        customer.setAdlDpstSt(dto.getAdlDpstSt());
        customer.setSlfGenSt(dto.getSlfGenSt());
        customer.setSupsdAcc(dto.getSupsdAcc());
        customer.setRefundDep(dto.getRefundDep());
        customer.setNoLoans(dto.getNoLoans());
        customer.setCstSt(dto.getCstSt());
        customer.setInstId(dto.getInstId());
        customer.setZone(dto.getZone());
        customer.setDateAddt(dto.getDateAddt());
        customer.setCustCd(dto.getCustCd());
        customer.setEnteredDtime(dto.getEnteredDtime());
        customer.setEditedDtime(dto.getEditedDtime());
        customer.setUserId(dto.getUserId());
        customer.setEditedUserId(dto.getEditedUserId());
        customer.setMobileNo(dto.getMobileNo());
        customer.setCustType(dto.getCustType());
        customer.setNetType(dto.getNetType());
        customer.setCatCode(dto.getCatCode());
        return customer;
    }

    // ================= MAP DATA (FAST API) =================
    @Transactional(readOnly = true)
    public List<MapDataDTO> getMapData() {
        return getMapData(null);
    }

    @Transactional(readOnly = true)
    public List<MapDataDTO> getMapData(String eeUserId) {
        try {
            List<MapDataDTO> data = bulkCustomerRepository.findMapData();
            if (eeUserId != null && !eeUserId.trim().isEmpty()) {
                data = data.stream()
                        .filter(item -> com.example.SPSProjectBackend.util.SessionUtils.matchEE(item.getResponsibleEe(),
                                eeUserId))
                        .collect(Collectors.toList());
            }
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load map data: " + e.getMessage(), e);
        }
    }
}