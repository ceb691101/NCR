package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.JournalDetailDTO;
import com.example.SPSProjectBackend.dto.JournalSummaryDTO;
import com.example.SPSProjectBackend.dto.JournalTypeDTO;
import com.example.SPSProjectBackend.dto.JournalUpdateDTO;
import com.example.SPSProjectBackend.dto.JournalCreateDTO;
import com.example.SPSProjectBackend.dto.JurnlAuthDTO;
import com.example.SPSProjectBackend.model.Journal;
import com.example.SPSProjectBackend.model.JournalId;
import com.example.SPSProjectBackend.model.JournalType;
import com.example.SPSProjectBackend.model.JurnlAuth;
import com.example.SPSProjectBackend.model.Bill_CycleLogFile;
import com.example.SPSProjectBackend.model.Process;
import com.example.SPSProjectBackend.repository.JournalRepository;
import com.example.SPSProjectBackend.repository.JournalTypeRepository;
import com.example.SPSProjectBackend.repository.JurnlAuthRepository;
import com.example.SPSProjectBackend.repository.Bill_CycleLogRepository;
import com.example.SPSProjectBackend.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private JournalTypeRepository journalTypeRepository;

    @Autowired
    private JurnlAuthRepository jurnlAuthRepository;

    @Autowired
    private BillCycleService billCycleService;

    @Autowired
    private Bill_CycleLogRepository billCycleLogRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private com.example.SPSProjectBackend.repository.NcreDeveloperRepository ncreDeveloperRepository;

    // ===================== GET ALL JOURNALS WITH SELECTED AREA AND BILL CYCLE =====================
    @Transactional(readOnly = true)
    public List<JournalSummaryDTO> getAllJournalsSummary(String areaCode, Integer billCycle) {
        try {
            System.out.println("Fetching journals for area: " + areaCode + ", bill cycle: " + billCycle);
            
            // Fetch journals filtered by selected area and bill cycle
            List<JournalSummaryDTO> journals = journalRepository.findFilteredJournalsByAreaAndBillCycle(areaCode, billCycle);
            
            System.out.println("Found " + journals.size() + " journals matching criteria");
            
            if (!journals.isEmpty()) {
                System.out.println("Sample of first 5 journals:");
                for (int i = 0; i < Math.min(5, journals.size()); i++) {
                    JournalSummaryDTO j = journals.get(i);
                    System.out.println("  " + (i+1) + ". jnl_no=" + j.getJnlNo() + 
                                     ", acc_nbr='" + j.getAccNbr() + "'" +
                                     ", adjust_amt=" + j.getAdjustAmt());
                }
            }
            
            if (!journals.isEmpty() && ncreDeveloperRepository != null) {
                List<String> accNbrs = journals.stream()
                        .map(JournalSummaryDTO::getAccNbr)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
                Map<String, Short> folioMap = new HashMap<>();
                for (String acc : accNbrs) {
                    ncreDeveloperRepository.findByAccNbrTrimmed(acc)
                            .ifPresent(d -> folioMap.put(acc, d.getFolioNo()));
                }
                for (JournalSummaryDTO j : journals) {
                    if (j.getAccNbr() != null) {
                        j.setFolioNo(folioMap.get(j.getAccNbr().trim()));
                    }
                }
            }

            return journals;
        } catch (Exception e) {
            System.err.println("Error in getAllJournalsSummary: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journals: " + e.getMessage(), e);
        }
    }

    // ===================== GET JOURNALS FOR USER REPORT (Acc Assistance) =====================
    @Transactional(readOnly = true)
    public List<JournalDetailDTO> getJournalsForUserReport(String sessionId, String userId, 
                                                            String areaCode, Integer billCycle) {
        try {
            System.out.println("Fetching journal report for user: " + userId 
                             + ", area: " + areaCode + ", bill cycle: " + billCycle);
            
            // Validate that user has access to the requested area
            var billCycleResponse = billCycleService.getBillCyclesForUser(sessionId, userId);
            
            if (!billCycleResponse.getSuccess()) {
                throw new RuntimeException("Failed to verify user access: " + billCycleResponse.getMessage());
            }
            
            // Check if user has access to the requested area
            boolean hasAccess = billCycleResponse.getBillCycles().stream()
                    .anyMatch(dto -> dto.getAreaCode().equals(areaCode) 
                                  && dto.getActiveBillCycle().equals(billCycle));
            
            if (!hasAccess) {
                System.out.println("User does not have access to area: " + areaCode);
                return Collections.emptyList();
            }
            
            System.out.println("User has access to area: " + areaCode + " with bill cycle: " + billCycle);
            
            // Fetch journals for the selected area and bill cycle only
            List<Journal> journals = journalRepository.findJournalsForUserReport(
                    Collections.singletonList(areaCode), 
                    Collections.singletonList(billCycle));
            
            System.out.println("Found " + journals.size() + " journals for report");
            
            return journals.stream()
                    .map(this::convertToDetailDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error in getJournalsForUserReport: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journal report: " + e.getMessage(), e);
        }
    }

    // ===================== GET JOURNAL DETAILS WITH SELECTED AREA AND BILL CYCLE =====================
    @Transactional(readOnly = true)
    public List<JournalDetailDTO> getJournalDetail(String accNbr, String jnlType, Integer jnlNo, BigDecimal adjustAmt, 
                                                    String areaCode, Integer billCycle) {
        try {
            if (accNbr == null || jnlType == null || jnlNo == null || adjustAmt == null) {
                System.out.println("Required parameters are null, returning empty list");
                return Collections.emptyList();
            }
            
            System.out.println("Fetching journal details for area: " + areaCode + ", bill cycle: " + billCycle);
            System.out.println("Composite key: accNbr=" + accNbr + ", jnlType=" + jnlType + 
                             ", jnlNo=" + jnlNo + ", adjustAmt=" + adjustAmt);
            
            // Fetch journal details with area and bill cycle filtering
            List<Journal> journals = journalRepository.findJournalDetailsByCompositeKeyAndArea(
                accNbr, jnlType, jnlNo, adjustAmt, areaCode, billCycle);
            
            System.out.println("Found " + journals.size() + " matching journals");
            
            if (!journals.isEmpty()) {
                Journal j = journals.get(0);
                System.out.println("Journal details found:");
                System.out.println("  jnl_no=" + j.getId().getJnlNo());
                System.out.println("  acc_nbr=" + j.getId().getAccNbr());
                System.out.println("  area_cd=" + j.getId().getAreaCd());
                System.out.println("  added_blcy=" + j.getId().getAddedBlcy());
                System.out.println("  jnl_type=" + j.getId().getJnlType());
                System.out.println("  adjust_amt=" + j.getAdjustAmt());
                System.out.println("  adjust_stat=" + j.getAdjustStat());
                System.out.println("  auth_code=" + j.getAuthCode());
                System.out.println("  doc_attch=" + j.getDocAttch());
                System.out.println("  confirmed=" + j.getConfirmed());
            }
            
            return journals.stream()
                    .map(this::convertToDetailDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getJournalDetail: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journal details: " + e.getMessage(), e);
        }
    }

    // ===================== CREATE JOURNAL =====================
    @Transactional
    public JournalDetailDTO createJournal(JournalCreateDTO createDTO, String userId) {
        try {
            System.out.println("Creating new journal with data: " + createDTO);
            
            // ====== CREATE FIRST JOURNAL ENTRY (Field 1) ======
            JournalId journalId1 = new JournalId();
            journalId1.setAccNbr(createDTO.getAccNbr());
            journalId1.setAreaCd(createDTO.getAreaCd());
            journalId1.setAddedBlcy(createDTO.getCurrentBillCycle());
            journalId1.setJnlType(createDTO.getJnlType());
            journalId1.setJnlNo(createDTO.getJnlNo());
            
            // Check if journal already exists
            if (journalRepository.existsById(journalId1)) {
                throw new RuntimeException("Journal already exists with jnl_no: " + createDTO.getJnlNo());
            }
            
            // Create first journal entity (for Field 1)
            Journal journal1 = new Journal();
            journal1.setId(journalId1);
            
            // Calculate adjust_amt from field1
            BigDecimal field1Amount = createDTO.getField1() != null ? createDTO.getField1() : BigDecimal.ZERO;
            int field1Sign = "CREDIT".equalsIgnoreCase(createDTO.getField1Type()) ? -1 : 1;
            BigDecimal adjustAmt1 = field1Amount.multiply(new BigDecimal(field1Sign));
            
            journal1.setAdjustAmt(adjustAmt1);
            
            // adjust_stat: "1" for DEBIT, "-1" for CREDIT
            journal1.setAdjustStat("CREDIT".equalsIgnoreCase(createDTO.getField1Type()) ? "-1" : "1");
            
            journal1.setAuthCode(createDTO.getApprovedBy());
            
            // Document Attached: "0" for No, actual value for Yes
            journal1.setDocAttch("N".equalsIgnoreCase(createDTO.getDocAttch()) ? "0" : createDTO.getDocAttch());
            
            // Parse and set journal date
            if (createDTO.getJnlDate() != null && !createDTO.getJnlDate().isEmpty()) {
                try {
                    journal1.setJnlDate(LocalDateTime.parse(createDTO.getJnlDate() + "T00:00:00"));
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                    journal1.setJnlDate(LocalDateTime.now());
                }
            } else {
                journal1.setJnlDate(LocalDateTime.now());
            }
            
            // Confirmed: null if not checked, "Y" if checked
            journal1.setConfirmed(createDTO.getIndividuallyConfirmed() != null && createDTO.getIndividuallyConfirmed() ? "Y" : null);
            
            journal1.setUserId(userId); // Set from current user session
            journal1.setEnteredDtime(LocalDateTime.now());
            
            // Save first journal to database
            Journal savedJournal1 = journalRepository.save(journal1);
            System.out.println("First journal entry created: " + savedJournal1.getId());
            
            // ====== CREATE SECOND JOURNAL ENTRY (Field 2 - if paired type exists) ======
            if (createDTO.getPairedJnlType() != null && !createDTO.getPairedJnlType().isEmpty() 
                && createDTO.getField2() != null && createDTO.getField2().compareTo(BigDecimal.ZERO) > 0) {
                
                System.out.println("Creating paired journal entry with type: " + createDTO.getPairedJnlType());
                
                // Use same jnl_no for second entry (only jnl_type differs)
                JournalId journalId2 = new JournalId();
                journalId2.setAccNbr(createDTO.getAccNbr());
                journalId2.setAreaCd(createDTO.getAreaCd());
                journalId2.setAddedBlcy(createDTO.getCurrentBillCycle());
                journalId2.setJnlType(createDTO.getPairedJnlType()); // Use paired journal type
                journalId2.setJnlNo(createDTO.getJnlNo()); // Same journal number
                
                // Check if second journal already exists
                if (journalRepository.existsById(journalId2)) {
                    throw new RuntimeException("Paired journal already exists with jnl_no: " + createDTO.getJnlNo());
                }
                
                // Create second journal entity (for Field 2)
                Journal journal2 = new Journal();
                journal2.setId(journalId2);
                
                // Calculate adjust_amt from field2
                BigDecimal field2Amount = createDTO.getField2();
                int field2Sign = "CREDIT".equalsIgnoreCase(createDTO.getField2Type()) ? -1 : 1;
                BigDecimal adjustAmt2 = field2Amount.multiply(new BigDecimal(field2Sign));
                
                journal2.setAdjustAmt(adjustAmt2);
                
                // adjust_stat: "1" for DEBIT, "-1" for CREDIT
                journal2.setAdjustStat("CREDIT".equalsIgnoreCase(createDTO.getField2Type()) ? "-1" : "1");
                
                journal2.setAuthCode(createDTO.getApprovedBy());
                journal2.setDocAttch("N".equalsIgnoreCase(createDTO.getDocAttch()) ? "0" : createDTO.getDocAttch());
                journal2.setJnlDate(journal1.getJnlDate()); // Same date as first entry
                journal2.setConfirmed(createDTO.getIndividuallyConfirmed() != null && createDTO.getIndividuallyConfirmed() ? "Y" : null);
                journal2.setUserId(userId);
                journal2.setEnteredDtime(LocalDateTime.now());
                
                // Save second journal to database
                Journal savedJournal2 = journalRepository.save(journal2);
                System.out.println("Second journal entry created: " + savedJournal2.getId());
            }
            
            System.out.println("Journal creation completed successfully");
            
            return convertToDetailDTO(savedJournal1);
            
        } catch (Exception e) {
            System.err.println("Error creating journal: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating journal: " + e.getMessage(), e);
        }
    }

    // ===================== DTO MAPPER =====================
    private JournalDetailDTO convertToDetailDTO(Journal journal) {
        if (journal == null) return null;

        JournalDetailDTO dto = new JournalDetailDTO(
                journal.getId().getJnlNo(),
                trimString(journal.getId().getAccNbr()),
                trimString(journal.getId().getAreaCd()),
                journal.getId().getAddedBlcy(),
                trimString(journal.getId().getJnlType()),
                journal.getAdjustAmt() != null ? journal.getAdjustAmt() : BigDecimal.ZERO,
                trimString(journal.getAdjustStat()),
                trimString(journal.getAuthCode()),
                trimString(journal.getDocAttch()),
                journal.getJnlDate(),
                trimString(journal.getConfirmed()),
                trimString(journal.getUserId()),
                journal.getEnteredDtime(),
                trimString(journal.getEditedUserId()),
                journal.getEditedDtime()
        );
        
        if (journal.getId().getAccNbr() != null && ncreDeveloperRepository != null) {
            ncreDeveloperRepository.findByAccNbrTrimmed(journal.getId().getAccNbr().trim())
                    .ifPresent(dev -> dto.setFolioNo(dev.getFolioNo()));
        }
        
        return dto;
    }

    // ===================== GET ALL JOURNAL TYPES =====================
    @Transactional(readOnly = true)
    public List<JournalTypeDTO> getAllJournalTypes() {
        try {
            System.out.println("Fetching all journal types from jnal_typs table where jnlt_status='A'");
            
            List<JournalType> journalTypes = journalTypeRepository.findAllOrderedByType();
            
            System.out.println("Found " + journalTypes.size() + " active journal types");
            
            return journalTypes.stream()
                    .map(jt -> new JournalTypeDTO(jt.getJnlType(), jt.getJnlDesc()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllJournalTypes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching journal types: " + e.getMessage(), e);
        }
    }

    // ===================== GET ALL JURNL AUTH =====================
    public List<JurnlAuthDTO> getAllJurnlAuth() {
        try {
            System.out.println("Fetching all jurnl_auth records where ac_status='A'");
            
            List<JurnlAuth> jurnlAuths = jurnlAuthRepository.findAllActive();
            
            System.out.println("Found " + jurnlAuths.size() + " active jurnl_auth records");
            
            return jurnlAuths.stream()
                    .map(ja -> new JurnlAuthDTO(ja.getAuthCode(), ja.getDesig(), ja.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllJurnlAuth: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching jurnl_auth: " + e.getMessage(), e);
        }
    }

    // ===================== UTIL =====================
    private String trimString(String value) {
        return value != null ? value.trim() : null;
    }
    
    private String safeString(String value) {
        return value != null ? value.trim() : "N/A";
    }

    // ===================== CONFIRM JOURNALS AND CREATE LOG ENTRY =====================
    @Transactional
    public Map<String, Object> confirmJournals(List<JournalDetailDTO> journals, String userId) {
        try {
            if (journals == null || journals.isEmpty()) {
                throw new RuntimeException("No journals provided for confirmation");
            }

            System.out.println("========== CONFIRMING JOURNALS ==========");
            System.out.println("Number of journals: " + journals.size());
            System.out.println("User ID: " + userId);

            // Record start time
            LocalDateTime startTime = LocalDateTime.now();
            
            // Get area and bill cycle from first journal
            String areaCode = journals.get(0).getAreaCd();
            Integer billCycle = journals.get(0).getAddedBlcy();
            
            System.out.println("Area Code: " + areaCode);
            System.out.println("Bill Cycle: " + billCycle);
            
            // Get pro_code from process table for "Confirm Editing of Journals"
            Optional<Process> processOpt = processRepository.findByProDescTrimmed("Confirm Editing of Journals");
            
            if (!processOpt.isPresent()) {
                throw new RuntimeException("Process 'Confirm Editing of Journals' not found in process table");
            }
            
            String proCode = processOpt.get().getProCode();
            System.out.println("Process Code: " + proCode);
            
            // Get the earliest entered_dtime from journals (for date_time field)
            LocalDateTime earliestDateTime = journals.stream()
                    .map(JournalDetailDTO::getEnteredDtime)
                    .filter(dt -> dt != null)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());
            
            System.out.println("Earliest Date Time: " + earliestDateTime);
            
            // Use the logged-in user's ID (who is confirming the journals)
            System.out.println("Logged-in User ID (confirming): " + userId);
            
            // Count number of records
            int noOfRecs = journals.size();
            
            // Record end time
            LocalDateTime endTime = LocalDateTime.now();
            
            // Format times as HH:mm:ss
            java.time.format.DateTimeFormatter timeFormatter = 
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
            String startTimeStr = startTime.format(timeFormatter);
            String endTimeStr = endTime.format(timeFormatter);
            
            // Calculate duration in minutes
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
            int durationMin = (int) (durationSeconds / 60);
            
            System.out.println("Start Time: " + startTimeStr);
            System.out.println("End Time: " + endTimeStr);
            System.out.println("Duration: " + durationMin + " minutes");
            
            // Check if a log_file entry already exists for this area_code, bill_cycle, and pro_code
            Optional<Bill_CycleLogFile> existingLogOpt = billCycleLogRepository
                .findByAreaCodeAndBillCycleAndProCodeTrimmed(areaCode, billCycle, proCode);
            
            Bill_CycleLogFile logFile;
            if (existingLogOpt.isPresent()) {
                // Update existing record
                logFile = existingLogOpt.get();
                System.out.println("Updating existing log_file entry with ID: " + logFile.getLogId());
            } else {
                // Create new record
                logFile = new Bill_CycleLogFile();
                logFile.setProCode(proCode);
                logFile.setAreaCode(areaCode);
                logFile.setBillCycle(billCycle);
                System.out.println("Creating new log_file entry");
            }
            
            // Set/Update fields
            logFile.setDateTime(earliestDateTime);
            logFile.setNoOfRecs(noOfRecs);
            logFile.setStartTime(startTimeStr);
            logFile.setEndTime(endTimeStr);
            logFile.setDurationMin(durationMin);
            logFile.setUserId(userId); // Use logged-in user's ID
            
            // Save log entry (create or update)
            Bill_CycleLogFile savedLog = billCycleLogRepository.save(logFile);
            
            System.out.println("Log file entry saved with ID: " + savedLog.getLogId());
            System.out.println("==========================================");
            
            // Calculate display log_id: count total records and subtract 1 to make it 0-based
            // First record: count=1 -> display 0, Second: count=2 -> display 1, etc.
            long totalRecords = billCycleLogRepository.count();
            String formattedLogId = String.valueOf(totalRecords - 1);
            
            // Return result with log details
            Map<String, Object> result = new HashMap<>();
            result.put("confirmedCount", noOfRecs);
            result.put("logId", formattedLogId);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error confirming journals: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error confirming journals: " + e.getMessage(), e);
        }
    }

    // ===================== CHECK IF AREA AND BILL CYCLE ALREADY CONFIRMED =====================
    @Transactional(readOnly = true)
    public boolean isAlreadyConfirmed(String areaCode, Integer billCycle) {
        try {
            System.out.println("Checking if confirmed - Area: " + areaCode + ", Bill Cycle: " + billCycle);
            
            // Get process code for "Confirm Editing of Journals"
            Optional<Process> processOpt = processRepository.findByProDescTrimmed("Confirm Editing of Journals");
            
            if (!processOpt.isPresent()) {
                System.out.println("Process 'Confirm Editing of Journals' not found");
                return false;
            }
            
            String proCode = processOpt.get().getProCode();
            
            // Check if log_file entry exists for this area, bill cycle, and process code
            Optional<Bill_CycleLogFile> existingLog = billCycleLogRepository
                .findByAreaCodeAndBillCycleAndProCodeTrimmed(areaCode, billCycle, proCode);
            
            boolean isConfirmed = existingLog.isPresent();
            System.out.println("Is confirmed: " + isConfirmed);
            
            return isConfirmed;
            
        } catch (Exception e) {
            System.err.println("Error checking confirmation status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===================== UPDATE JOURNAL RECORD =====================
    @Transactional
    public JournalDetailDTO updateJournal(JournalUpdateDTO updateDTO, String userId) {
        try {
            System.out.println("========== UPDATING JOURNAL ==========");
            System.out.println("Account Number: " + updateDTO.getAccNbr());
            System.out.println("Area Code: " + updateDTO.getAreaCd());
            System.out.println("Bill Cycle: " + updateDTO.getAddedBlcy());
            System.out.println("Journal Type: " + updateDTO.getJnlType());
            System.out.println("Journal No: " + updateDTO.getJnlNo());
            System.out.println("Updated by: " + userId);
            
            // Check if already confirmed - cannot edit after confirmation
            if (isAlreadyConfirmed(updateDTO.getAreaCd(), updateDTO.getAddedBlcy())) {
                throw new RuntimeException("Cannot edit journals that have already been confirmed");
            }
            
            // Create composite key from original values (from DTO - these are the original values sent from frontend)
            JournalId journalId = new JournalId(
                updateDTO.getAccNbr(),
                updateDTO.getAreaCd(),
                updateDTO.getAddedBlcy(),
                updateDTO.getJnlType(),
                updateDTO.getJnlNo()
            );
            
            // Find existing journal
            Optional<Journal> journalOpt = journalRepository.findById(journalId);
            
            if (!journalOpt.isPresent()) {
                throw new RuntimeException("Journal not found");
            }
            
            Journal journal = journalOpt.get();
            
            // Update only non-key fields (not area_cd and added_blcy as per requirements)
            // Key fields (acc_nbr, jnl_type, jnl_no) are already set in the ID
            if (updateDTO.getAdjustAmt() != null) {
                journal.setAdjustAmt(updateDTO.getAdjustAmt());
            }
            if (updateDTO.getAuthCode() != null) {
                journal.setAuthCode(updateDTO.getAuthCode());
            }
            if (updateDTO.getJnlDate() != null) {
                journal.setJnlDate(updateDTO.getJnlDate());
            }
            if (updateDTO.getUserId() != null) {
                journal.setUserId(updateDTO.getUserId());
            }
            if (updateDTO.getEnteredDtime() != null) {
                journal.setEnteredDtime(updateDTO.getEnteredDtime());
            }
            
            // Set the editor information
            journal.setEditedUserId(userId);
            journal.setEditedDtime(LocalDateTime.now());
            
            // Save updated journal
            Journal savedJournal = journalRepository.save(journal);
            
            System.out.println("Journal updated successfully");
            System.out.println("======================================");
            
            // Convert to DTO
            return convertToDetailDTO(savedJournal);
            
        } catch (Exception e) {
            System.err.println("Error updating journal: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating journal: " + e.getMessage(), e);
        }
    }
}
