package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InvoiceChecklistDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvoiceCreate;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvoiceCreateRepository;
import com.example.SPSProjectBackend.repository.NcreRoleFuncRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceChecklistService {

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreInvoiceCreateRepository ncreInvoiceCreateRepository;

    @Autowired
    private NcreRoleFuncRepository ncreRoleFuncRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreBillCycleRepository ncreBillCycleRepository;

    /**
     * Get the active current bill cycle where is_current = 1 from ncre_bill_cycle table
     */
    @Transactional(readOnly = true)
    public Optional<Integer> getCurrentBillCycle() {
        return ncreBillCycleRepository.findCurrentBillCycleNumber();
    }

    /**
     * Resolve EPF Number for the logged-in user from SEC_INFO (sec_info) table
     */
    public String resolveEpfNum(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return userId;
        }
        Optional<com.example.SPSProjectBackend.model.UserAccSecInfo> secInfoOpt =
                userAccSecInfoRepository.findByIdTrimmed(userId.trim());
        if (secInfoOpt.isPresent() && secInfoOpt.get().getEpfNum() != null && !secInfoOpt.get().getEpfNum().trim().isEmpty()) {
            return secInfoOpt.get().getEpfNum().trim();
        }
        return userId.trim();
    }

    public boolean isITUser(String roleOrUserId) {
        if (roleOrUserId == null) return false;
        String u = roleOrUserId.trim().toUpperCase();
        return u.contains("ITTEST") || u.contains("IT_TEST") || u.equals("IT") || u.contains("ADMIN");
    }

    /**
     * Resolve the user's assigned role (REP2, REP3, or ITTest) from session user ID context
     */
    public String resolveUserRole(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "REP2";
        }
        String u = userId.trim().toUpperCase();
        if (isITUser(u)) {
            return "ITTest";
        }

        // Check user category or user name from SEC_INFO dynamically
        Optional<com.example.SPSProjectBackend.model.UserAccSecInfo> secInfoOpt =
                userAccSecInfoRepository.findByIdTrimmed(userId.trim());
        if (secInfoOpt.isPresent()) {
            com.example.SPSProjectBackend.model.UserAccSecInfo sec = secInfoOpt.get();
            String cat = sec.getUserCat() != null ? sec.getUserCat().trim().toUpperCase() : "";
            String name = sec.getUserName() != null ? sec.getUserName().trim().toUpperCase() : "";
            if (isITUser(cat) || isITUser(name) || isITUser(sec.getUserId())) {
                return "ITTest";
            }
        }

        if (u.contains("REP3")) {
            return "REP3";
        }
        return "REP2";
    }

    /**
     * Fetch available (un-checked) invoices/folios for Invoice Checklist for active developers (status = '2')
     */
    @Transactional(readOnly = true)
    public InvoiceChecklistDTO.InvoiceChecklistResponseDTO getChecklistInvoices(Integer billCycle, String userId) {
        String role = resolveUserRole(userId);
        boolean isIT = isITUser(role);

        // 1. Fetch pending records from NCRE_INVOICE_CREATE where is_create = 0 for this bill cycle
        List<NcreInvoiceCreate> pendingRecords;
        if (isIT) {
            pendingRecords = ncreInvoiceCreateRepository.findAllPendingChecklistRecordsByBillCycle(billCycle);
        } else {
            pendingRecords = ncreInvoiceCreateRepository.findPendingChecklistRecordsByBillCycleAndRole(billCycle, role);
        }

        List<InvoiceChecklistDTO.InvoiceChecklistItemDTO> items = new ArrayList<>();

        if (pendingRecords != null && !pendingRecords.isEmpty()) {
            List<Short> folioShorts = pendingRecords.stream()
                    .map(NcreInvoiceCreate::getFolioNo)
                    .filter(Objects::nonNull)
                    .map(Integer::shortValue)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Short, NcreDeveloper> devMap = new HashMap<>();
            if (!folioShorts.isEmpty()) {
                List<NcreDeveloper> devs = ncreDeveloperRepository.findByFolioNoIn(folioShorts);
                if (devs != null) {
                    for (NcreDeveloper d : devs) {
                        if (d.getFolioNo() != null) {
                            devMap.put(d.getFolioNo(), d);
                        }
                    }
                }
            }

            Set<Integer> processedFolios = new HashSet<>();
            for (NcreInvoiceCreate c : pendingRecords) {
                if (c.getFolioNo() == null || processedFolios.contains(c.getFolioNo())) continue;
                processedFolios.add(c.getFolioNo());

                NcreDeveloper dev = devMap.get(c.getFolioNo().shortValue());
                String accNbr = dev != null && dev.getAccNbr() != null ? dev.getAccNbr() : "N/A";
                String devName = dev != null && dev.getDeveloperName() != null ? dev.getDeveloperName() : "N/A";
                String facName = dev != null && dev.getFacilityName() != null ? dev.getFacilityName() : "N/A";
                String respEe = dev != null && dev.getResponsibleEe() != null ? dev.getResponsibleEe() : (isIT ? "N/A" : role);

                items.add(InvoiceChecklistDTO.InvoiceChecklistItemDTO.builder()
                        .folioNo(c.getFolioNo())
                        .accNbr(accNbr)
                        .companyName(devName)
                        .projectName(facName)
                        .preparedBy(respEe)
                        .assignedTo(respEe)
                        .isAdded(false)
                        .remarks(c.getRemarks() != null ? c.getRemarks() : "")
                        .build());
            }
        }

        items.sort(Comparator.comparing(InvoiceChecklistDTO.InvoiceChecklistItemDTO::getFolioNo));

        return InvoiceChecklistDTO.InvoiceChecklistResponseDTO.builder()
                .success(true)
                .billCycle(billCycle)
                .userRole(role)
                .totalCount(items.size())
                .invoices(items)
                .message("Invoices retrieved successfully")
                .build();
    }

    /**
     * Fetch checked invoices (is_create = 1) for Invoice Checklist for active developers (status = '2')
     */
    @Transactional(readOnly = true)
    public InvoiceChecklistDTO.InvoiceChecklistResponseDTO getCheckedInvoices(Integer billCycle, String userId) {
        String role = resolveUserRole(userId);
        boolean isIT = isITUser(role);

        List<NcreInvoiceCreate> checkedRecords;
        if (isIT) {
            checkedRecords = ncreInvoiceCreateRepository.findAllCheckedChecklistRecordsByBillCycle(billCycle);
        } else {
            checkedRecords = ncreInvoiceCreateRepository.findCheckedChecklistRecordsByBillCycleAndRole(billCycle, role);
        }

        List<InvoiceChecklistDTO.InvoiceChecklistItemDTO> items = new ArrayList<>();

        if (checkedRecords != null && !checkedRecords.isEmpty()) {
            List<Short> folioShorts = checkedRecords.stream()
                    .map(NcreInvoiceCreate::getFolioNo)
                    .filter(Objects::nonNull)
                    .map(Integer::shortValue)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Short, NcreDeveloper> devMap = new HashMap<>();
            if (!folioShorts.isEmpty()) {
                List<NcreDeveloper> devs = ncreDeveloperRepository.findByFolioNoIn(folioShorts);
                if (devs != null) {
                    for (NcreDeveloper d : devs) {
                        if (d.getFolioNo() != null) {
                            devMap.put(d.getFolioNo(), d);
                        }
                    }
                }
            }

            Set<Integer> processedFolios = new HashSet<>();
            for (NcreInvoiceCreate c : checkedRecords) {
                if (c.getFolioNo() == null || processedFolios.contains(c.getFolioNo())) continue;
                processedFolios.add(c.getFolioNo());

                NcreDeveloper dev = devMap.get(c.getFolioNo().shortValue());
                String accNbr = dev != null && dev.getAccNbr() != null ? dev.getAccNbr() : "N/A";
                String devName = dev != null && dev.getDeveloperName() != null ? dev.getDeveloperName() : "N/A";
                String facName = dev != null && dev.getFacilityName() != null ? dev.getFacilityName() : "N/A";
                String respEe = dev != null && dev.getResponsibleEe() != null ? dev.getResponsibleEe() : (isIT ? "N/A" : role);

                items.add(InvoiceChecklistDTO.InvoiceChecklistItemDTO.builder()
                        .folioNo(c.getFolioNo())
                        .accNbr(accNbr)
                        .companyName(devName)
                        .projectName(facName)
                        .preparedBy(respEe)
                        .assignedTo(respEe)
                        .isAdded(true)
                        .remarks(c.getRemarks() != null ? c.getRemarks() : "")
                        .build());
            }
        }

        items.sort(Comparator.comparing(InvoiceChecklistDTO.InvoiceChecklistItemDTO::getFolioNo));

        return InvoiceChecklistDTO.InvoiceChecklistResponseDTO.builder()
                .success(true)
                .billCycle(billCycle)
                .userRole(role)
                .totalCount(items.size())
                .invoices(items)
                .message("Checked invoices retrieved successfully")
                .build();
    }

    /**
     * Add or Update checklist items into ncre_invoice_create with transaction safety and duplicate prevention
     */
    public Map<String, Object> addChecklistInvoices(Integer billCycle, List<InvoiceChecklistDTO.AddChecklistItemRequestDTO> selectedInvoices, String userId) {
        if (billCycle == null || billCycle <= 0) {
            throw new IllegalArgumentException("Invalid bill cycle");
        }
        if (selectedInvoices == null || selectedInvoices.isEmpty()) {
            throw new IllegalArgumentException("At least one invoice must be selected");
        }

        String epfNum = resolveEpfNum(userId);
        Date now = new Date();
        int updatedCount = 0;

        for (InvoiceChecklistDTO.AddChecklistItemRequestDTO item : selectedInvoices) {
            if (item.getFolioNo() == null) continue;

            Optional<NcreInvoiceCreate> existingOpt = ncreInvoiceCreateRepository.findByBillCycleAndFolioNo(billCycle, item.getFolioNo());
            NcreInvoiceCreate record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
            } else {
                record = NcreInvoiceCreate.builder()
                        .billCycle(billCycle)
                        .folioNo(item.getFolioNo())
                        .build();
            }

            record.setIsCreate(1);
            if (item.getRemarks() != null && !item.getRemarks().trim().isEmpty()) {
                record.setRemarks(item.getRemarks().trim());
            }
            record.setCreatedBy(epfNum);
            record.setCreatedDate(now);

            ncreInvoiceCreateRepository.save(record);
            updatedCount++;
        }

        InvoiceChecklistDTO.ValidationResultDTO validation = validateChecklist(billCycle, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", updatedCount + " invoice(s) checklist status updated to IS_CREATE = 1 successfully.");
        response.put("validation", validation);
        return response;
    }

    /**
     * Calculate Invoice Checklist Validation metrics using DISTINCT folio sets and user role attribution
     */
    @Transactional(readOnly = true)
    public InvoiceChecklistDTO.ValidationResultDTO validateChecklist(Integer billCycle) {
        return validateChecklist(billCycle, null);
    }

    @Transactional(readOnly = true)
    public InvoiceChecklistDTO.ValidationResultDTO validateChecklist(Integer billCycle, String userId) {
        if (billCycle == null || billCycle <= 0) {
            return InvoiceChecklistDTO.ValidationResultDTO.builder()
                    .billCycle(billCycle)
                    .totalInvoices(0)
                    .assignedCount(0)
                    .checkedCount(0)
                    .remainingCount(0)
                    .complete(true)
                    .build();
        }

        String role = resolveUserRole(userId);
        boolean isIT = isITUser(role);

        Long totalCount;
        Long checkedCountLong;

        if (isIT) {
            // 1. Total Active Assigned Folios across all REP roles (status = 2)
            totalCount = ncreDeveloperRepository.countTotalActiveFolios();
            // 2. Already Checked Folios for this bill cycle across all REP roles (is_create = 1)
            checkedCountLong = ncreInvoiceCreateRepository.countAllCheckedFoliosByBillCycle(billCycle);
        } else {
            // 1. Total Active Assigned Folios for this REP role (status = 2)
            totalCount = ncreDeveloperRepository.countTotalActiveFoliosByRole(role);
            // 2. Already Checked Folios for this bill cycle and REP role (is_create = 1)
            checkedCountLong = ncreInvoiceCreateRepository.countCheckedFoliosByBillCycleAndRole(billCycle, role);
        }

        int totalInvoices = totalCount != null ? totalCount.intValue() : 0;
        int checkedCount = checkedCountLong != null ? checkedCountLong.intValue() : 0;
        int remainingCount = Math.max(0, totalInvoices - checkedCount);
        boolean complete = (totalInvoices > 0 && checkedCount == totalInvoices);

        return InvoiceChecklistDTO.ValidationResultDTO.builder()
                .billCycle(billCycle)
                .totalInvoices(totalInvoices)
                .rep2Count(checkedCount)
                .rep3Count(checkedCount)
                .assignedCount(checkedCount)
                .checkedCount(checkedCount)
                .remainingCount(remainingCount)
                .complete(complete)
                .build();
    }
}
