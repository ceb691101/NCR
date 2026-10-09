package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.NcreDeveloperDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SPSProjectBackend.exception.DeveloperAlreadyExistsException;

@Service
public class NcreDeveloperService {

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    /**
     * Counts active developers across the areas in scope.
     *
     * The scope is the set of areas resolved from the sec_info region/province/area
     * hierarchy, narrowed further when the user picked a single area in the header bar.
     */
    @Transactional(readOnly = true)
    public long getActiveDeveloperCount(String sessionId, String userId, String selectedAreaCode) {
        SecInfoLoginDTO.UserInfo userInfo = sessionUtils.getUserLocationFromSession(sessionId, userId)
                .orElseThrow(() -> new IllegalStateException("Invalid session or user"));

        String category = userInfo.getUserCategory();
        boolean eeUser = SessionUtils.isEEUserCategory(category);

        // Effective scope: selected area when present, otherwise every permitted area.
        List<String> effectiveAreaCodes = areaPermissionService
                .resolveEffectiveAreaCodes(sessionId, userId, selectedAreaCode);

        Set<String> scopedAreaCodes = new HashSet<>();
        for (String code : effectiveAreaCodes) {
            scopedAreaCodes.add(normalizeAreaCode(code));
        }

        long total = 0;
        for (Object[] row : ncreDeveloperRepository.countActiveDevelopersGroupedByAreaAndResponsibleEe()) {
            String areaCode = row[0] != null ? normalizeAreaCode(row[0].toString()) : null;
            String responsibleEe = row[1] != null ? row[1].toString().trim() : null;
            long count = ((Number) row[2]).longValue();

            if (eeUser && !SessionUtils.matchEE(responsibleEe, userId)) {
                continue;
            }
            if (areaCode != null && scopedAreaCodes.contains(areaCode)) {
                total += count;
            }
        }
        return total;
    }

    private String normalizeAreaCode(String areaCode) {
        return areaCode == null ? "" : areaCode.trim().replaceFirst("^0+(?!$)", "");
    }

    @Transactional
    public NcreDeveloper saveDeveloper(NcreDeveloperDTO dto) {
        NcreDeveloper entity = new NcreDeveloper();

        // set acc_nbr from incoming payload (accountNumber)
        String accNbr = dto.getAccountNumber() != null ? dto.getAccountNumber().trim() : null;
        if (accNbr == null || accNbr.isEmpty()) {
            throw new IllegalArgumentException("accountNumber is required");
        }
        if (ncreDeveloperRepository.existsById(accNbr) || bulkCustomerRepository.existsById(accNbr)) {
            throw new DeveloperAlreadyExistsException(accNbr);
        }
        entity.setAccNbr(accNbr);

        entity.setFolioNo(dto.getFolioNumber());
        entity.setFileRefNo(dto.getFileReferenceNo());
        entity.setSrNoUptoDate(dto.getSrNoUptoDate());
        entity.setTariffType(dto.getTariffType());
        entity.setFileNo(dto.getFileNo());
        entity.setProvince(dto.getProvince());
        entity.setDeveloperName(dto.getDeveloperName());
        entity.setFacilityName(dto.getProjectName());
        entity.setCommissionedCapacityMw(dto.getCommissionedCapacityMw());
        entity.setLoiIssued(toDate(dto.getLoiIssued()));
        entity.setSppaSigned(toDate(dto.getSppaSignedDate()));
        entity.setGridConnectionDate(toDate(dto.getGridConnectionDate()));
        entity.setReferenceCode(dto.getReferenceCode());
        entity.setRegion(dto.getRegion());
        entity.setSrNo(dto.getSrNo());
        entity.setArea(dto.getArea());
        entity.setType(dto.getNcreType());
        entity.setGridSubstation(dto.getGridSubstation());
        entity.setInitialTariff(dto.getInitialTariff());
        entity.setExpirationDate(toDate(dto.getExpirationDate()));
        entity.setExpirationExtensionDate(toDate(dto.getExpirationExtensionDate()));
        entity.setSppaCapacityMw(dto.getSppaSignedCapacityMw());
        entity.setAcceptRu(dto.getAcceptRu());
        entity.setFeederNo(dto.getFeederNo());
        entity.setCommissionedYear(dto.getCommissionedYear());
        entity.setAcExpirationWithExtension(toDate(dto.getAcExpirationWithExtension()));
        entity.setEx(toDate(dto.getExDate()));
        entity.setAc(toDate(dto.getAcDate()));
        entity.setFlat(toDate(dto.getFlatDate()));
        entity.setTtt(toDate(dto.getTttDate()));
        entity.setFirstTier(toDate(dto.getFirstTierDate()));
        entity.setSecondTier(toDate(dto.getSecondTierDate()));
        entity.setThirdTier(toDate(dto.getThirdTierDate()));
        entity.setNewSppaSigned(toDate(dto.getNewSppaSigned()));
        entity.setValidityStart(toDate(dto.getValidityStart()));
        entity.setValidityExpiry(toDate(dto.getValidityExpiry()));
        entity.setInitialTariffRevised(dto.getInitialTariffRevised());
        entity.setRecommissionedOn(toDate(dto.getRecommissionedOn()));
        entity.setEpExpired(toDate(dto.getEpExpired()));
        entity.setGlExpired(toDate(dto.getGlExpired()));
        entity.setProjectStatus(dto.getProjectStatus());
        entity.setVoltageLevelKv(dto.getVoltageLevelKv());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setAddressLine3(dto.getAddressLine3());
        entity.setContactPerson(dto.getContactPerson());
        entity.setTelephone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompanyGroup(dto.getCompanyGroup());
        entity.setTenderedOrNot(dto.getTenderOrNot());
        entity.setReductions(dto.getReductions());
        entity.setAgreementType(dto.getAgreementType());
        entity.setResponsibleEe(dto.getResponsibleEe());
        entity.setGenerationLosses(dto.getGenerationLosses() != null ? dto.getGenerationLosses().divide(BigDecimal.valueOf(100)) : null);
        entity.setPaymentDeductions(dto.getPaymentDeductions() != null ? dto.getPaymentDeductions().divide(BigDecimal.valueOf(100)) : null);
        if(dto.getLongitude() != null) {
            entity.setLongitude(dto.getLongitude().doubleValue());
        }
        // entity.setLongitude(dto.getLongitude());
        if(dto.getLatitude() != null) {
            entity.setLatitude(dto.getLatitude().doubleValue());
        }
        // entity.setLatitude(dto.getLatitude());

        NcreDeveloper saved = ncreDeveloperRepository.save(entity);

        // Also save into the customer table with selected fields
        BulkCustomer customer = new BulkCustomer();
        customer.setAccNbr(saved.getAccNbr());
        customer.setFolioNo(dto.getFolioNumber());
        customer.setName(dto.getDeveloperName());
        // customer.setAreaCd(dto.getArea());
        String ncreType = dto.getNcreType().trim();
        switch(ncreType){
            case "SPP": customer.setNcre_type("S"); break;
            case "MHP": customer.setNcre_type("M"); break;
            case "DPP": customer.setNcre_type("D"); break;
            case "BMP": customer.setNcre_type("B"); break;
            case "WPP": customer.setNcre_type("W"); break;
            case "WHP": customer.setNcre_type("W"); break;
        }
        customer.setNcre("1");
        // customer.setNcre_type(dto.getNcreType());
        if (dto.getLatitude() != null) {
            customer.setLatitude(dto.getLatitude().doubleValue());
        }
        if (dto.getLongitude() != null) {
            customer.setLongitude(dto.getLongitude().doubleValue());
        }
        customer.setCusCat("B");
        // address_l1 and tariff are NOT NULL in the customer table
        customer.setAddressL1("");
        customer.setTariff("");
        customer.setEnteredDtime(new Date());

        bulkCustomerRepository.save(customer);

        return saved;
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public NcreDeveloper findDeveloperByField(String field, String value) {
        switch (field) {
            case "acc_nbr":
                return ncreDeveloperRepository.findByAccNbr(value).orElse(null);
            case "project_name":
                return ncreDeveloperRepository.findByFacilityName(value).orElse(null); // Add to repository
            case "file_no":
                return ncreDeveloperRepository.findByFileNo(Short.valueOf(value)).orElse(null); // Add to repository
            case "folio_no":
                return ncreDeveloperRepository.findByFolioNo(Short.valueOf(value)).orElse(null); // Add to repository
            default:
                throw new IllegalArgumentException("Invalid search field");
        }
    }

    @Transactional
    public NcreDeveloper updateDeveloper(String accNbr, NcreDeveloperDTO dto) {
        NcreDeveloper existing = ncreDeveloperRepository.findByAccNbr(accNbr)
                .orElseThrow(() -> new RuntimeException("Developer not found"));

        // Update fields from DTO (similar to saveDeveloper, but update existing entity)
        existing.setFolioNo(dto.getFolioNumber());
        existing.setFileRefNo(dto.getFileReferenceNo());
        existing.setSrNoUptoDate(dto.getSrNoUptoDate());
        existing.setTariffType(dto.getTariffType());
        existing.setFileNo(dto.getFileNo());
        existing.setProvince(dto.getProvince());
        existing.setDeveloperName(dto.getDeveloperName());
        existing.setFacilityName(dto.getProjectName());
        existing.setCommissionedCapacityMw(dto.getCommissionedCapacityMw());
        existing.setLoiIssued(toDate(dto.getLoiIssued()));
        existing.setSppaSigned(toDate(dto.getSppaSignedDate()));
        existing.setGridConnectionDate(toDate(dto.getGridConnectionDate()));
        existing.setReferenceCode(dto.getReferenceCode());
        existing.setRegion(dto.getRegion());
        existing.setSrNo(dto.getSrNo());
        existing.setArea(dto.getArea());
        existing.setType(dto.getNcreType());
        existing.setGridSubstation(dto.getGridSubstation());
        existing.setInitialTariff(dto.getInitialTariff());
        existing.setExpirationDate(toDate(dto.getExpirationDate()));
        existing.setExpirationExtensionDate(toDate(dto.getExpirationExtensionDate()));
        existing.setSppaCapacityMw(dto.getSppaSignedCapacityMw());
        existing.setAcceptRu(dto.getAcceptRu());
        existing.setFeederNo(dto.getFeederNo());
        existing.setCommissionedYear(dto.getCommissionedYear());
        existing.setAcExpirationWithExtension(toDate(dto.getAcExpirationWithExtension()));
        existing.setEx(toDate(dto.getExDate()));
        existing.setAc(toDate(dto.getAcDate()));
        existing.setFlat(toDate(dto.getFlatDate()));
        existing.setTtt(toDate(dto.getTttDate()));
        existing.setFirstTier(toDate(dto.getFirstTierDate()));
        existing.setSecondTier(toDate(dto.getSecondTierDate()));
        existing.setThirdTier(toDate(dto.getThirdTierDate()));
        existing.setNewSppaSigned(toDate(dto.getNewSppaSigned()));
        existing.setValidityStart(toDate(dto.getValidityStart()));
        existing.setValidityExpiry(toDate(dto.getValidityExpiry()));
        existing.setInitialTariffRevised(dto.getInitialTariffRevised());
        existing.setRecommissionedOn(toDate(dto.getRecommissionedOn()));
        existing.setEpExpired(toDate(dto.getEpExpired()));
        existing.setGlExpired(toDate(dto.getGlExpired()));
        existing.setProjectStatus(dto.getProjectStatus());
        existing.setVoltageLevelKv(dto.getVoltageLevelKv());
        existing.setAddressLine1(dto.getAddressLine1());
        existing.setAddressLine2(dto.getAddressLine2());
        existing.setAddressLine3(dto.getAddressLine3());
        existing.setContactPerson(dto.getContactPerson());
        existing.setTelephone(dto.getPhone());
        existing.setEmail(dto.getEmail());
        existing.setCompanyGroup(dto.getCompanyGroup());
        existing.setTenderedOrNot(dto.getTenderOrNot());
        existing.setReductions(dto.getReductions());
        existing.setAgreementType(dto.getAgreementType());
        existing.setResponsibleEe(dto.getResponsibleEe());
        existing.setGenerationLosses(dto.getGenerationLosses() != null ? dto.getGenerationLosses().divide(BigDecimal.valueOf(100)) : null);
        existing.setPaymentDeductions(dto.getPaymentDeductions() != null ? dto.getPaymentDeductions().divide(BigDecimal.valueOf(100)) : null);
        if(dto.getLongitude() != null) {
            existing.setLongitude(dto.getLongitude().doubleValue());
        }
        // existing.setLongitude(dto.getLongitude());
        if(dto.getLatitude() != null) {
            existing.setLatitude(dto.getLatitude().doubleValue());
        }
        // existing.setLatitude(dto.getLatitude());

        NcreDeveloper saved = ncreDeveloperRepository.save(existing);

        // Also update the customer table to keep it in sync
        BulkCustomer customer = bulkCustomerRepository.findById(accNbr).orElse(null);
        if (customer != null) {
            customer.setFolioNo(dto.getFolioNumber());
            customer.setName(dto.getDeveloperName());
            String ncreType = dto.getNcreType() != null ? dto.getNcreType().trim() : "";
            switch(ncreType){
                case "SPP": customer.setNcre_type("S"); break;
                case "MHP": customer.setNcre_type("M"); break;
                case "DPP": customer.setNcre_type("D"); break;
                case "BMP": customer.setNcre_type("B"); break;
                case "WPP": customer.setNcre_type("W"); break;
                case "WHP": customer.setNcre_type("W"); break;
            }
            if (dto.getLatitude() != null) {
                customer.setLatitude(dto.getLatitude().doubleValue());
            }
            if (dto.getLongitude() != null) {
                customer.setLongitude(dto.getLongitude().doubleValue());
            }
            customer.setEditedDtime(new Date());
            bulkCustomerRepository.save(customer);
        }

        return saved;
    }

    
}

