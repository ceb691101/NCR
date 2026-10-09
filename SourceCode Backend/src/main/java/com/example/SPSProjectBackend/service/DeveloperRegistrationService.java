package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.AddendumDTO;
import com.example.SPSProjectBackend.dto.AgreementDTO;
import com.example.SPSProjectBackend.dto.DeveloperRegistrationRequest;
import com.example.SPSProjectBackend.dto.PaymentDeductionDTO;
import com.example.SPSProjectBackend.model.*;
import com.example.SPSProjectBackend.repository.*;
import com.example.SPSProjectBackend.exception.DeveloperAlreadyExistsException;
import com.example.SPSProjectBackend.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperRegistrationService {

    private final NcreDeveloperRepository developerRepository;
    private final AgreementRepository agreementRepository;
    private final AddendumRepository addendumRepository;
    private final PaymentDeductionRepository paymentDeductionRepository;
    private final NcreDocumentRepository documentRepository;
    private final NcreDevTariffRateRepository tariffRateRepository;
    private final SessionUtils sessionUtils;

    @Transactional
    public NcreDeveloper registerDeveloper(DeveloperRegistrationRequest request, List<MultipartFile> files) {
        log.info("Starting registration process for Developer: {} / Facility: {}", 
                request.getDeveloperName(), request.getProjectName());
        validateRequest(request);

        try {
            // Index files by name
            java.util.Map<String, MultipartFile> fileMap = new java.util.HashMap<>();
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                        if (fileMap.put(file.getOriginalFilename(), file) != null) {
                            throw new IllegalArgumentException("Duplicate uploaded filename: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            // 1. Validation check for duplicates
            if (developerRepository.findByAccNbrTrimmed(request.getAccountNumber()).isPresent()) {
                throw new DeveloperAlreadyExistsException(request.getAccountNumber());
            }
            if (developerRepository.findByFolioNo(request.getFolioNumber()).isPresent()) {
                throw new IllegalStateException("Developer with Folio Number " + request.getFolioNumber() + " is already registered.");
            }

            // 2. Create and Save NcreDeveloper (Global Information)
            NcreDeveloper developer = mapToDeveloperEntity(request);
            developer.setStatus("2");
            
            developer = developerRepository.save(developer);
            String folioStr = String.valueOf(developer.getFolioNo());
            log.debug("Saved NcreDeveloper with Folio No: {}", developer.getFolioNo());

            // Check and create initial tariff rate record in ncre_dev_year_tariff_rates
            createInitialTariffRateRecord(developer, request);

            // 2. Process Agreements
            if (request.getAgreements() != null && !request.getAgreements().isEmpty()) {
                log.info("Processing {} agreements for Folio No: {}", request.getAgreements().size(), developer.getFolioNo());
                
                for (AgreementDTO agreementDTO : request.getAgreements()) {
                    Agreement agreement = new Agreement();
                    agreement.setDeveloper(developer);
                    agreement.setFolioNo(developer.getFolioNo());
                    agreement.setInitialTariff(agreementDTO.getInitialTariff());
                    
                    if (agreementDTO.getVoltageLevelKv() != null) {
                        agreement.setVoltageLevelKv(agreementDTO.getVoltageLevelKv().setScale(3, java.math.RoundingMode.HALF_UP));
                    }
                    if (agreementDTO.getGenerationLosses() != null) {
                        agreement.setGenerationLosses(agreementDTO.getGenerationLosses().setScale(2, java.math.RoundingMode.HALF_UP));
                    }
                    
                    agreement = agreementRepository.save(agreement);
                    log.debug("Saved Agreement ID: {}", agreement.getAgreementId());

                    // Save Agreement General Files
                    if (agreementDTO.getAgreementFileNames() != null) {
                        for (String fName : agreementDTO.getAgreementFileNames()) {
                            MultipartFile f = fileMap.get(fName);
                            if (f != null) {
                                saveDocument(f, agreement.getAgreementId(), "AGREEMENT", "GENERAL_AGREEMENT", folioStr);
                            }
                        }
                    }

                    // Save Agreement Tariff Files
                    if (agreementDTO.getTariffFileNames() != null) {
                        for (String fName : agreementDTO.getTariffFileNames()) {
                            MultipartFile f = fileMap.get(fName);
                            if (f != null) {
                                saveDocument(f, agreement.getAgreementId(), "AGREEMENT", "TARIFF_REVISION", folioStr);
                            }
                        }
                    }

                    // Process Payment Deductions
                    if (agreementDTO.getPaymentDeductions() != null) {
                        for (PaymentDeductionDTO pdDTO : agreementDTO.getPaymentDeductions()) {
                            PaymentDeduction deduction = new PaymentDeduction();
                            deduction.setAgreement(agreement);
                            deduction.setDeductionType(pdDTO.getType());
                            deduction.setPercentage(pdDTO.getPercentage());
                            paymentDeductionRepository.save(deduction);
                        }
                    }

                    // Process Addendums
                    if (agreementDTO.getAddendums() != null) {
                        for (AddendumDTO addendumDTO : agreementDTO.getAddendums()) {
                            Addendum addendum = new Addendum();
                            addendum.setAgreement(agreement);
                            addendum.setDeveloperName(addendumDTO.getDeveloperName());
                            addendum.setNewSppaSigned(addendumDTO.getNewSppaSignedDate());
                            addendum.setInitialTariffRevised(addendumDTO.getInitialTariffRevised());
                            addendum.setExpirationExtensionDate(addendumDTO.getExpirationExtensionDate());
                            addendum.setRecommissionedOn(addendumDTO.getRecommissionedOn());
                            addendum = addendumRepository.save(addendum);
                            log.debug("Saved Addendum ID: {}", addendum.getAddendumId());

                            // Save Addendum Files
                            if (addendumDTO.getAddendumFileNames() != null) {
                                for (String fName : addendumDTO.getAddendumFileNames()) {
                                    MultipartFile f = fileMap.get(fName);
                                    if (f != null) {
                                        saveDocument(f, addendum.getAddendumId(), "ADDENDUM", "ADDENDUM", folioStr);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.info("Successfully completed registration for Developer: {}", request.getDeveloperName());
            return developer;

        } catch (DeveloperAlreadyExistsException | IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while registering developer: {}", request.getDeveloperName(), e);
            throw new RuntimeException("Failed to register developer. Error: " + e.getMessage(), e);
        }
    }

    private void saveDocument(MultipartFile file, Long refId, String refType, String docCategory, String folioNo) throws IOException {
        if (file == null || file.isEmpty()) return;

        String uploadBasePath = System.getProperty("user.home") + "/NCRE_Uploads";
        File dir = new File(uploadBasePath + "/" + folioNo);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        File destFile = new File(dir, fileName);
        file.transferTo(destFile);

        NcreDocument doc = new NcreDocument();
        doc.setReferenceId(refId);
        doc.setReferenceType(refType);
        doc.setDocumentCategory(docCategory);
        doc.setFileName(fileName);
        doc.setFilePath(destFile.getAbsolutePath());
        doc.setUploadedAt(new Date());

        documentRepository.save(doc);
        log.info("Saved NcreDocument: {} under category: {}", fileName, docCategory);
    }

    private NcreDeveloper mapToDeveloperEntity(DeveloperRegistrationRequest request) {
        NcreDeveloper developer = new NcreDeveloper();
        // Base mapping
        developer.setFolioNo(request.getFolioNumber());
        developer.setAccNbr(request.getAccountNumber());
        developer.setFileRefNo(request.getFileReferenceNo());
        developer.setDeveloperName(request.getDeveloperName());
        developer.setCompanyGroup(request.getGroupOfCompany());
        developer.setContactPerson(request.getContactPerson());
        developer.setEmail(request.getEmail());
        developer.setTelephone(request.getPhone());
        developer.setMtrNbr(request.getMeterNo());
        developer.setReductions(request.getReductions());
        developer.setAddressLine1(request.getAddressLine1());
        developer.setAddressLine2(request.getAddressLine2());
        developer.setAddressLine3(request.getAddressLine3());
        developer.setFacilityName(request.getProjectName());
        developer.setProvince(request.getProvince());
        developer.setLoiIssued(request.getLoiIssued());
        developer.setSppaSigned(request.getSppaSignedDate());
        developer.setGridConnectionDate(request.getGridConnectionDate());
        developer.setExpirationDate(request.getExpirationDate());
        developer.setReferenceCode(request.getReferenceCode());
        developer.setGridSubstation(request.getGridSubstation());
        developer.setRegion(request.getRegion());
        developer.setSrNo(request.getSrNo());
        developer.setArea(request.getArea());
        developer.setLongitude(request.getLongitude());
        developer.setLatitude(request.getLatitude());
        developer.setResponsibleEe(request.getResponsibleEe());
        
        // Feeder No mapping
        if (request.getFeederNo() != null && !request.getFeederNo().isEmpty()) {
            try {
                developer.setFeederNo(Short.valueOf(request.getFeederNo()));
            } catch (Exception e) {
                log.warn("Failed to parse feeder_no: {}", request.getFeederNo());
            }
        }
        
        // NCRE Type missing from entity, let's map to 'type'
        developer.setType(request.getNcreType());
        
        // Global Tariff mapping
        developer.setAgreementType(request.getAgreementType());
        developer.setTariffType(request.getTariffType());
        developer.setCommissionedYear(request.getCommissionedYear());
        
        if (request.getCommissionedCapacityMw() != null) {
            developer.setCommissionedCapacityMw(request.getCommissionedCapacityMw().setScale(3, java.math.RoundingMode.HALF_UP));
        }
        if (request.getSppaSignedCapacityMw() != null) {
            developer.setSppaCapacityMw(request.getSppaSignedCapacityMw().setScale(3, java.math.RoundingMode.HALF_UP));
        }
        developer.setAcceptRu(request.getAcceptRu());
        
        if (request.getEpExpired() != null) {
            developer.setEpExpired(request.getEpExpired());
        }
        
        if (request.getGlExpired() != null) {
            developer.setGlExpired(request.getGlExpired());
        }

        // Conditionally populate tier dates only for TTT
        if (request.getTariffType() != null && request.getTariffType().startsWith("TTT")) {
            developer.setFirstTier(request.getFirstTierDate());
            developer.setSecondTier(request.getSecondTierDate());
            developer.setThirdTier(request.getThirdTierDate());
        } else {
            developer.setFirstTier(null);
            developer.setSecondTier(null);
            developer.setThirdTier(null);
        }
        
        // Populate legacy columns from the first Agreement and Addendum if available
        if (request.getAgreements() != null && !request.getAgreements().isEmpty()) {
            AgreementDTO firstAgreement = request.getAgreements().get(0);
            developer.setInitialTariff(firstAgreement.getInitialTariff());
            
            if (firstAgreement.getVoltageLevelKv() != null) {
                developer.setVoltageLevelKv(firstAgreement.getVoltageLevelKv().setScale(2, java.math.RoundingMode.HALF_UP));
            }
            if (firstAgreement.getGenerationLosses() != null) {
                developer.setGenerationLosses(firstAgreement.getGenerationLosses().setScale(2, java.math.RoundingMode.HALF_UP));
            }
            
            if (firstAgreement.getAddendums() != null && !firstAgreement.getAddendums().isEmpty()) {
                AddendumDTO firstAddendum = firstAgreement.getAddendums().get(0);
                developer.setExpirationExtensionDate(firstAddendum.getExpirationExtensionDate());
                developer.setNewSppaSigned(firstAddendum.getNewSppaSignedDate());
                developer.setRecommissionedOn(firstAddendum.getRecommissionedOn());
                
                if (firstAddendum.getInitialTariffRevised() != null && !firstAddendum.getInitialTariffRevised().isEmpty()) {
                    try {
                        developer.setInitialTariffRevised(new java.math.BigDecimal(firstAddendum.getInitialTariffRevised()).setScale(2, java.math.RoundingMode.HALF_UP));
                    } catch (Exception e) {
                        log.warn("Failed to parse initial_tariff_revised: {}", firstAddendum.getInitialTariffRevised());
                    }
                }
            }
        }
        
        developer.setCusCat("B");

        return developer;
    }

    @Transactional
    public NcreDeveloper updateDeveloper(DeveloperRegistrationRequest request, List<MultipartFile> files) {
        log.info("Starting update process for Developer: {} / Acc: {}", 
                request.getDeveloperName(), request.getAccountNumber());
        validateRequest(request);

        try {
            java.util.Map<String, MultipartFile> fileMap = new java.util.HashMap<>();
            if (files != null) {
                for (MultipartFile file : files) {
                    if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                        if (fileMap.put(file.getOriginalFilename(), file) != null) {
                            throw new IllegalArgumentException("Duplicate uploaded filename: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            NcreDeveloper developer = null;
            if (request.getAccountNumber() != null && !request.getAccountNumber().trim().isEmpty()) {
                developer = developerRepository.findByAccNbrTrimmed(request.getAccountNumber()).orElse(null);
            }
            if (developer == null && request.getFolioNumber() != null) {
                developer = developerRepository.findByFolioNo(request.getFolioNumber()).orElse(null);
            }
            if (developer == null) {
                throw new RuntimeException("Developer not found for Account Number " + request.getAccountNumber());
            }

            updateDeveloperFields(developer, request);
            developer = developerRepository.save(developer);
            String folioStr = String.valueOf(developer.getFolioNo());

            if (request.getAgreements() != null && !request.getAgreements().isEmpty()) {
                for (AgreementDTO agreementDTO : request.getAgreements()) {
                    List<Agreement> existingAgreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(developer.getFolioNo());
                    Agreement agreement = existingAgreements.isEmpty() ? new Agreement() : existingAgreements.get(0);
                    agreement.setDeveloper(developer);
                    agreement.setFolioNo(developer.getFolioNo());
                    agreement.setInitialTariff(agreementDTO.getInitialTariff());
                    
                    if (agreementDTO.getVoltageLevelKv() != null) {
                        agreement.setVoltageLevelKv(agreementDTO.getVoltageLevelKv().setScale(3, java.math.RoundingMode.HALF_UP));
                    }
                    if (agreementDTO.getGenerationLosses() != null) {
                        agreement.setGenerationLosses(agreementDTO.getGenerationLosses().setScale(2, java.math.RoundingMode.HALF_UP));
                    }
                    
                    agreement = agreementRepository.save(agreement);

                    if (agreementDTO.getAgreementFileNames() != null) {
                        for (String fName : agreementDTO.getAgreementFileNames()) {
                            MultipartFile f = fileMap.get(fName);
                            if (f != null) {
                                saveDocument(f, agreement.getAgreementId(), "AGREEMENT", "GENERAL_AGREEMENT", folioStr);
                            }
                        }
                    }

                    if (agreementDTO.getTariffFileNames() != null) {
                        for (String fName : agreementDTO.getTariffFileNames()) {
                            MultipartFile f = fileMap.get(fName);
                            if (f != null) {
                                saveDocument(f, agreement.getAgreementId(), "AGREEMENT", "TARIFF_REVISION", folioStr);
                            }
                        }
                    }

                    if (agreementDTO.getPaymentDeductions() != null) {
                        List<PaymentDeduction> existingDeductions = paymentDeductionRepository.findByAgreement_AgreementId(agreement.getAgreementId());
                        if (existingDeductions != null && !existingDeductions.isEmpty()) {
                            paymentDeductionRepository.deleteAll(existingDeductions);
                        }
                        for (PaymentDeductionDTO pdDTO : agreementDTO.getPaymentDeductions()) {
                            PaymentDeduction deduction = new PaymentDeduction();
                            deduction.setAgreement(agreement);
                            deduction.setDeductionType(pdDTO.getType());
                            deduction.setPercentage(pdDTO.getPercentage());
                            paymentDeductionRepository.save(deduction);
                        }
                    }

                    if (agreementDTO.getAddendums() != null) {
                        for (AddendumDTO addendumDTO : agreementDTO.getAddendums()) {
                            Addendum addendum = new Addendum();
                            addendum.setAgreement(agreement);
                            addendum.setDeveloperName(addendumDTO.getDeveloperName());
                            addendum.setNewSppaSigned(addendumDTO.getNewSppaSignedDate());
                            addendum.setInitialTariffRevised(addendumDTO.getInitialTariffRevised());
                            addendum.setExpirationExtensionDate(addendumDTO.getExpirationExtensionDate());
                            addendum.setRecommissionedOn(addendumDTO.getRecommissionedOn());
                            addendum = addendumRepository.save(addendum);

                            if (addendumDTO.getAddendumFileNames() != null) {
                                for (String fName : addendumDTO.getAddendumFileNames()) {
                                    MultipartFile f = fileMap.get(fName);
                                    if (f != null) {
                                        saveDocument(f, addendum.getAddendumId(), "ADDENDUM", "ADDENDUM", folioStr);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.info("Successfully updated Developer: {}", request.getDeveloperName());
            return developer;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating developer: {}", request.getDeveloperName(), e);
            throw new RuntimeException("Failed to update developer. Error: " + e.getMessage(), e);
        }
    }

    private void updateDeveloperFields(NcreDeveloper developer, DeveloperRegistrationRequest request) {
        if (request.getFileReferenceNo() != null) developer.setFileRefNo(request.getFileReferenceNo());
        if (request.getDeveloperName() != null) developer.setDeveloperName(request.getDeveloperName());
        if (request.getGroupOfCompany() != null) developer.setCompanyGroup(request.getGroupOfCompany());
        if (request.getContactPerson() != null) developer.setContactPerson(request.getContactPerson());
        if (request.getEmail() != null) developer.setEmail(request.getEmail());
        if (request.getPhone() != null) developer.setTelephone(request.getPhone());
        if (request.getMeterNo() != null) developer.setMtrNbr(request.getMeterNo());
        if (request.getReductions() != null) developer.setReductions(request.getReductions());
        if (request.getAddressLine1() != null) developer.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) developer.setAddressLine2(request.getAddressLine2());
        if (request.getAddressLine3() != null) developer.setAddressLine3(request.getAddressLine3());
        if (request.getProjectName() != null) developer.setFacilityName(request.getProjectName());
        if (request.getProvince() != null) developer.setProvince(request.getProvince());
        if (request.getLoiIssued() != null) developer.setLoiIssued(request.getLoiIssued());
        if (request.getSppaSignedDate() != null) developer.setSppaSigned(request.getSppaSignedDate());
        if (request.getGridConnectionDate() != null) developer.setGridConnectionDate(request.getGridConnectionDate());
        if (request.getExpirationDate() != null) developer.setExpirationDate(request.getExpirationDate());
        if (request.getReferenceCode() != null) developer.setReferenceCode(request.getReferenceCode());
        if (request.getGridSubstation() != null) developer.setGridSubstation(request.getGridSubstation());
        if (request.getRegion() != null) developer.setRegion(request.getRegion());
        if (request.getSrNo() != null) developer.setSrNo(request.getSrNo());
        if (request.getArea() != null) developer.setArea(request.getArea());
        if (request.getResponsibleEe() != null) developer.setResponsibleEe(request.getResponsibleEe());
        if (request.getLongitude() != null) developer.setLongitude(request.getLongitude());
        if (request.getLatitude() != null) developer.setLatitude(request.getLatitude());
        if (request.getStatus() != null) developer.setStatus(request.getStatus());
        if (request.getNcreType() != null) developer.setType(request.getNcreType());
        if (request.getAgreementType() != null) developer.setAgreementType(request.getAgreementType());
        if (request.getTariffType() != null) developer.setTariffType(request.getTariffType());
        if (request.getCommissionedYear() != null) developer.setCommissionedYear(request.getCommissionedYear());

        if (request.getFeederNo() != null && !request.getFeederNo().isEmpty()) {
            try {
                developer.setFeederNo(Short.valueOf(request.getFeederNo()));
            } catch (Exception e) {
                log.warn("Failed to parse feeder_no: {}", request.getFeederNo());
            }
        }
        if (request.getCommissionedCapacityMw() != null) {
            developer.setCommissionedCapacityMw(request.getCommissionedCapacityMw().setScale(3, java.math.RoundingMode.HALF_UP));
        }
        if (request.getSppaSignedCapacityMw() != null) {
            developer.setSppaCapacityMw(request.getSppaSignedCapacityMw().setScale(3, java.math.RoundingMode.HALF_UP));
        }
        if (request.getEpExpired() != null) developer.setEpExpired(request.getEpExpired());
        if (request.getGlExpired() != null) developer.setGlExpired(request.getGlExpired());
        developer.setAcceptRu(request.getAcceptRu());
    }

    @Transactional(readOnly = true)
    public DeveloperRegistrationRequest getDeveloperDetailsBySearch(String field, String value) {
        NcreDeveloper dev = null;
        switch (field) {
            case "acc_nbr":
                dev = developerRepository.findByAccNbrTrimmed(value).orElse(null);
                if (dev == null) dev = developerRepository.findByAccNbr(value).orElse(null);
                break;
            case "project_name":
                dev = developerRepository.findByFacilityName(value).orElse(null);
                break;
            case "file_no":
                try {
                    dev = developerRepository.findByFileNo(Short.valueOf(value)).orElse(null);
                } catch (Exception ignored) {}
                break;
            case "folio_no":
                try {
                    dev = developerRepository.findByFolioNo(Short.valueOf(value)).orElse(null);
                } catch (Exception ignored) {}
                break;
            default:
                throw new IllegalArgumentException("Invalid search field: " + field);
        }

        if (dev == null) {
            return null;
        }

        DeveloperRegistrationRequest req = new DeveloperRegistrationRequest();
        req.setDeveloperName(dev.getDeveloperName());
        req.setGroupOfCompany(dev.getCompanyGroup());
        req.setContactPerson(dev.getContactPerson());
        req.setEmail(dev.getEmail());
        req.setPhone(dev.getTelephone());
        req.setAddressLine1(dev.getAddressLine1());
        req.setAddressLine2(dev.getAddressLine2());
        req.setAddressLine3(dev.getAddressLine3());

        req.setFolioNumber(dev.getFolioNo());
        req.setAccountNumber(dev.getAccNbr());
        req.setFileReferenceNo(dev.getFileRefNo());
        req.setProjectName(dev.getFacilityName());
        req.setProvince(dev.getProvince());
        req.setLoiIssued(dev.getLoiIssued());
        req.setSppaSignedDate(dev.getSppaSigned());
        req.setGridConnectionDate(dev.getGridConnectionDate());
        req.setExpirationDate(dev.getExpirationDate());
        req.setReferenceCode(dev.getReferenceCode());
        req.setGridSubstation(dev.getGridSubstation());
        req.setNcreType(dev.getType());
        req.setFeederNo(dev.getFeederNo() != null ? String.valueOf(dev.getFeederNo()) : "");
        req.setMeterNo(dev.getMtrNbr());

        req.setArea(dev.getArea());
        req.setLongitude(dev.getLongitude());
        req.setLatitude(dev.getLatitude());
        req.setRegion(dev.getRegion());
        req.setSrNo(dev.getSrNo());
        req.setStatus(dev.getStatus());

        req.setAgreementType(dev.getAgreementType());
        req.setTariffType(dev.getTariffType());
        req.setCommissionedYear(dev.getCommissionedYear());
        req.setCommissionedCapacityMw(dev.getCommissionedCapacityMw());
        req.setSppaSignedCapacityMw(dev.getSppaCapacityMw());
        req.setAcceptRu(dev.getAcceptRu());
        req.setEpExpired(dev.getEpExpired());
        req.setGlExpired(dev.getGlExpired());
        req.setFirstTierDate(dev.getFirstTier());
        req.setSecondTierDate(dev.getSecondTier());
        req.setThirdTierDate(dev.getThirdTier());
        req.setResponsibleEe(dev.getResponsibleEe());
        req.setReductions(dev.getReductions());

        // Fetch associated Agreements
        List<AgreementDTO> agreementDTOs = new java.util.ArrayList<>();
        if (dev.getFolioNo() != null) {
            List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(dev.getFolioNo());
            for (Agreement ag : agreements) {
                AgreementDTO agDTO = new AgreementDTO();
                agDTO.setInitialTariff(ag.getInitialTariff());
                agDTO.setVoltageLevelKv(ag.getVoltageLevelKv());
                agDTO.setGenerationLosses(ag.getGenerationLosses());

                // Fetch Payment Deductions
                List<PaymentDeduction> deductions = paymentDeductionRepository.findByAgreement_AgreementId(ag.getAgreementId());
                List<PaymentDeductionDTO> pdDTOs = new java.util.ArrayList<>();
                for (PaymentDeduction pd : deductions) {
                    PaymentDeductionDTO pdDTO = new PaymentDeductionDTO();
                    pdDTO.setType(pd.getDeductionType());
                    pdDTO.setPercentage(pd.getPercentage());
                    pdDTOs.add(pdDTO);
                }
                agDTO.setPaymentDeductions(pdDTOs);

                // Fetch Addendums
                List<Addendum> addendums = addendumRepository.findByAgreement_AgreementId(ag.getAgreementId());
                List<AddendumDTO> addDTOs = new java.util.ArrayList<>();
                for (Addendum add : addendums) {
                    AddendumDTO addDTO = new AddendumDTO();
                    addDTO.setDeveloperName(add.getDeveloperName());
                    addDTO.setNewSppaSignedDate(add.getNewSppaSigned());
                    addDTO.setInitialTariffRevised(add.getInitialTariffRevised() != null ? String.valueOf(add.getInitialTariffRevised()) : "");
                    addDTO.setExpirationExtensionDate(add.getExpirationExtensionDate());
                    addDTO.setRecommissionedOn(add.getRecommissionedOn());
                    addDTOs.add(addDTO);
                }
                agDTO.setAddendums(addDTOs);

                // Fetch Documents
                List<NcreDocument> docs = documentRepository.findByReferenceIdAndReferenceType(ag.getAgreementId(), "AGREEMENT");
                List<String> tariffFiles = new java.util.ArrayList<>();
                List<String> agreementFiles = new java.util.ArrayList<>();
                for (NcreDocument doc : docs) {
                    if ("TARIFF_REVISION".equalsIgnoreCase(doc.getDocumentCategory())) {
                        tariffFiles.add(doc.getFileName());
                    } else {
                        agreementFiles.add(doc.getFileName());
                    }
                }
                agDTO.setTariffFileNames(tariffFiles);
                agDTO.setAgreementFileNames(agreementFiles);

                agreementDTOs.add(agDTO);
            }
        }

        // Fallback: If no Agreement entity records exist in relational table yet, build one from NcreDeveloper columns
        if (agreementDTOs.isEmpty()) {
            AgreementDTO agDTO = new AgreementDTO();
            agDTO.setInitialTariff(dev.getInitialTariff());
            agDTO.setVoltageLevelKv(dev.getVoltageLevelKv());
            agDTO.setGenerationLosses(dev.getGenerationLosses());
            if (dev.getPaymentDeductions() != null) {
                PaymentDeductionDTO pdDTO = new PaymentDeductionDTO();
                pdDTO.setType("Loyalty Deduction (%)");
                pdDTO.setPercentage(dev.getPaymentDeductions().multiply(java.math.BigDecimal.valueOf(100)));
                agDTO.setPaymentDeductions(java.util.Collections.singletonList(pdDTO));
            }
            agreementDTOs.add(agDTO);
        }

        req.setAgreements(agreementDTOs);
        return req;
    }

    private void validateRequest(DeveloperRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request data is required.");
        }
        if (isBlank(request.getDeveloperName())) {
            throw new IllegalArgumentException("Developer name is required.");
        }
        if (isBlank(request.getAccountNumber())) {
            throw new IllegalArgumentException("Account number is required.");
        }
        if (request.getFolioNumber() == null) {
            throw new IllegalArgumentException("Folio number is required.");
        }
        if (request.getFolioNumber() <= 0 || request.getFolioNumber() % 4 != 1) {
            throw new IllegalArgumentException("Folio number must match the pattern 1, 5, 9, 13, ...");
        }
        if (isBlank(request.getTariffType())) {
            throw new IllegalArgumentException("Tariff type is required.");
        }
        if ((request.getLongitude() == null) != (request.getLatitude() == null)) {
            throw new IllegalArgumentException("Longitude and latitude must be provided together.");
        }
        if (request.getLongitude() != null
                && (request.getLongitude() < 79.5 || request.getLongitude() > 81.9)) {
            throw new IllegalArgumentException("Longitude must be within Sri Lanka (79.5 to 81.9).");
        }
        if (request.getLatitude() != null
                && (request.getLatitude() < 5.9 || request.getLatitude() > 9.9)) {
            throw new IllegalArgumentException("Latitude must be within Sri Lanka (5.9 to 9.9).");
        }
        if (request.getStatus() != null && !"2".equals(request.getStatus()) && !"3".equals(request.getStatus())) {
            throw new IllegalArgumentException("Status must be 2 (active) or 3 (inactive).");
        }
        if (request.getAcceptRu() != null && request.getAcceptRu() < 0) {
            throw new IllegalArgumentException("Accepted RU cannot be negative.");
        }
        if (request.getAgreements() != null) {
            for (AgreementDTO agreement : request.getAgreements()) {
                if (agreement.getGenerationLosses() != null
                        && agreement.getGenerationLosses().signum() < 0) {
                    throw new IllegalArgumentException("Generation losses cannot be negative.");
                }
                if (agreement.getPaymentDeductions() != null) {
                    for (PaymentDeductionDTO deduction : agreement.getPaymentDeductions()) {
                        if (deduction.getPercentage() != null && deduction.getPercentage().signum() < 0) {
                            throw new IllegalArgumentException("Payment deduction percentage cannot be negative.");
                        }
                    }
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getDeveloperSuggestions(String field, String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String cleanQuery = query.trim();
        String lowerQuery = cleanQuery.toLowerCase();
        java.util.List<NcreDeveloper> allDevs = developerRepository.findAll();
        java.util.List<NcreDeveloper> devs;

        if ("folio_no".equalsIgnoreCase(field)) {
            devs = allDevs.stream()
                    .filter(d -> d.getFolioNo() != null && String.valueOf(d.getFolioNo()).contains(cleanQuery))
                    .sorted((d1, d2) -> {
                        boolean exact1 = cleanQuery.equals(String.valueOf(d1.getFolioNo()));
                        boolean exact2 = cleanQuery.equals(String.valueOf(d2.getFolioNo()));
                        if (exact1 && !exact2) return -1;
                        if (!exact1 && exact2) return 1;
                        return d1.getFolioNo().compareTo(d2.getFolioNo());
                    })
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
        } else if ("developer_name".equalsIgnoreCase(field)) {
            devs = allDevs.stream()
                    .filter(d -> d.getDeveloperName() != null && d.getDeveloperName().toLowerCase().contains(lowerQuery))
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
        } else if ("project_name".equalsIgnoreCase(field)) {
            devs = allDevs.stream()
                    .filter(d -> d.getFacilityName() != null && d.getFacilityName().toLowerCase().contains(lowerQuery))
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
        } else if ("acc_nbr".equalsIgnoreCase(field)) {
            devs = allDevs.stream()
                    .filter(d -> d.getAccNbr() != null && d.getAccNbr().toLowerCase().contains(lowerQuery))
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            devs = allDevs.stream()
                    .filter(d -> d.getFolioNo() != null && String.valueOf(d.getFolioNo()).contains(cleanQuery))
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
        }

        return devs.stream().map(d -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", d.getAccNbr());
            map.put("projectName", d.getFacilityName());
            map.put("accountNumber", d.getAccNbr());
            map.put("folioNumber", d.getFolioNo());
            map.put("developerName", d.getDeveloperName());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    private void createInitialTariffRateRecord(NcreDeveloper developer, DeveloperRegistrationRequest request) {
        Short folioNo = developer.getFolioNo();
        if (folioNo == null) {
            log.warn("Folio number is null, skipping tariff rate record creation");
            return;
        }

        Optional<NcreDevTariffRate> existingRate = tariffRateRepository.findByFolioNo(folioNo);
        if (existingRate.isPresent()) {
            log.info("Tariff rate record already exists for folio_no: {}", folioNo);
            return;
        }

        String sessionId = getSessionIdFromRequest();
        String entBy = sessionId != null ? getUserIdFromSession(sessionId) : "SYSTEM";

        NcreDevTariffRate tariffRate = new NcreDevTariffRate();
        tariffRate.setFolioNo(folioNo);
        tariffRate.setTechnoType(request.getNcreType());
        tariffRate.setTariffCode(request.getTariffType());
        String initialTariffStr = developer.getInitialTariff();
        tariffRate.setPrvTariffRate(initialTariffStr != null && !initialTariffStr.trim().isEmpty() 
            ? new BigDecimal(initialTariffStr.trim()) : null);
        tariffRate.setCurTariffRate(null);

        int currentYear = java.time.LocalDate.now().getYear();
        tariffRate.setTariffChanged(java.sql.Date.valueOf(java.time.LocalDate.of(currentYear, 1, 1)));
        tariffRate.setYear(currentYear);
        tariffRate.setResponsibleEe(request.getResponsibleEe());
        tariffRate.setStatus("0");
        tariffRate.setEntBy(entBy);
        tariffRate.setEntDt(java.sql.Date.valueOf(java.time.LocalDate.now()));
        tariffRate.setChangeTimesPerYear("0");

        tariffRateRepository.save(tariffRate);
        log.info("Created initial tariff rate record for folio_no: {}", folioNo);
    }

    private String getSessionIdFromRequest() {
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request =
                    ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
                return (String) request.getAttribute("session_id");
            }
        } catch (Exception e) {
            log.debug("Could not extract session_id from request: {}", e.getMessage());
        }
        return null;
    }

    private String getUserIdFromSession(String sessionId) {
        try {
            return sessionUtils.getSessionData(sessionId)
                .map(com.example.SPSProjectBackend.model.SecInfoSessionData::getUserId)
                .orElse("SYSTEM");
        } catch (Exception e) {
            log.debug("Could not extract user ID from session: {}", e.getMessage());
            return "SYSTEM";
        }
    }
}
