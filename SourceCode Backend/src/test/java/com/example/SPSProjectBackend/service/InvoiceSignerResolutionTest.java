package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.model.InvoiceStatusHistory;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.InvoiceStatusHistoryRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceSignerResolutionTest {

    @Mock
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Mock
    private InvoiceStatusHistoryRepository invoiceStatusHistoryRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private UserAccSecInfo rep3User;
    private UserAccSecInfo cengUser;
    private UserAccSecInfo dgmUser;

    @BeforeEach
    void setUp() {
        rep3User = new UserAccSecInfo();
        rep3User.setUserId("REP3");
        rep3User.setUserName("Eng. R.S. Ukwatta");
        rep3User.setUserCat("Electrical Engineer");

        cengUser = new UserAccSecInfo();
        cengUser.setUserId("CEng");
        cengUser.setUserName("Eng. (Mrs.) A.G.C.U Perera");
        cengUser.setUserCat("Chief Engineer (Renewable Energy Projects)");

        dgmUser = new UserAccSecInfo();
        dgmUser.setUserId("DGM");
        dgmUser.setUserName("Eng. G. W. Vajira Priyantha");
        dgmUser.setUserCat("Director (ET-GL)");
    }

    @Test
    @DisplayName("Prepared By resolved from database and date formatted correctly")
    void testResolvePreparedByFromDatabase() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));

        Invoice invoice = new Invoice();
        invoice.setId(101L);
        invoice.setStatus(InvoiceStatus.RECOMMEND);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 7, 10, 15));

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 101))
                .thenReturn(Collections.emptyList());

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertNotNull(signers.getPreparedBy());
        assertEquals("REP3", signers.getPreparedBy().getUserId());
        assertEquals("Eng. R.S. Ukwatta", signers.getPreparedBy().getName());
        assertEquals("EE (REP3)", signers.getPreparedBy().getDesignation());
        assertEquals("2026-09-07", signers.getPreparedBy().getDate());

        // CE and Director should be blank for RECOMMEND status
        assertEquals("", signers.getChiefEngineer().getName());
        assertEquals("", signers.getChiefEngineer().getDate());
        assertEquals("", signers.getDirector().getName());
        assertEquals("", signers.getDirector().getDate());
    }

    @Test
    @DisplayName("Chief Engineer resolved from RECOMMEND -> APPROVE transition")
    void testResolveChiefEngineerFromStatusHistory() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("CEng"))).thenReturn(Optional.of(cengUser));

        Invoice invoice = new Invoice();
        invoice.setId(102L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 7, 10, 15));

        InvoiceStatusHistory h1 = InvoiceStatusHistory.builder()
                .id((short) 1)
                .invoiceId((short) 102)
                .statusFrom("DRAFT")
                .statusTo("RECOMMEND")
                .changedBy("REP3")
                .changedAt(LocalDateTime.of(2026, 9, 7, 10, 15))
                .build();

        InvoiceStatusHistory h2 = InvoiceStatusHistory.builder()
                .id((short) 2)
                .invoiceId((short) 102)
                .statusFrom("RECOMMEND")
                .statusTo("APPROVE")
                .changedBy("CEng")
                .changedAt(LocalDateTime.of(2026, 9, 8, 14, 30))
                .build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 102))
                .thenReturn(Arrays.asList(h2, h1));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertEquals("Eng. (Mrs.) A.G.C.U Perera", signers.getChiefEngineer().getName());
        assertEquals("Chief Engineer (Renewable Energy Projects)", signers.getChiefEngineer().getDesignation());
        assertEquals("2026-09-08", signers.getChiefEngineer().getDate());
        assertEquals("CEng", signers.getChiefEngineer().getUserId());

        // Director not yet finalized
        assertEquals("", signers.getDirector().getName());
        assertEquals("", signers.getDirector().getDate());
    }

    @Test
    @DisplayName("Director resolved from APPROVE -> FINALIZE transition")
    void testResolveDirectorFromStatusHistory() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("CEng"))).thenReturn(Optional.of(cengUser));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("DGM"))).thenReturn(Optional.of(dgmUser));

        Invoice invoice = new Invoice();
        invoice.setId(103L);
        invoice.setStatus(InvoiceStatus.FINALIZE);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 7, 10, 15));

        InvoiceStatusHistory h1 = InvoiceStatusHistory.builder()
                .id((short) 1)
                .invoiceId((short) 103)
                .statusFrom("DRAFT")
                .statusTo("RECOMMEND")
                .changedBy("REP3")
                .changedAt(LocalDateTime.of(2026, 9, 7, 10, 15))
                .build();

        InvoiceStatusHistory h2 = InvoiceStatusHistory.builder()
                .id((short) 2)
                .invoiceId((short) 103)
                .statusFrom("RECOMMEND")
                .statusTo("APPROVE")
                .changedBy("CEng")
                .changedAt(LocalDateTime.of(2026, 9, 8, 14, 30))
                .build();

        InvoiceStatusHistory h3 = InvoiceStatusHistory.builder()
                .id((short) 3)
                .invoiceId((short) 103)
                .statusFrom("APPROVE")
                .statusTo("FINALIZE")
                .changedBy("DGM")
                .changedAt(LocalDateTime.of(2026, 9, 9, 9, 45))
                .build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 103))
                .thenReturn(Arrays.asList(h3, h2, h1));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        // All 3 signers resolved from DB
        assertEquals("Eng. R.S. Ukwatta", signers.getPreparedBy().getName());
        assertEquals("2026-09-07", signers.getPreparedBy().getDate());

        assertEquals("Eng. (Mrs.) A.G.C.U Perera", signers.getChiefEngineer().getName());
        assertEquals("Chief Engineer (Renewable Energy Projects)", signers.getChiefEngineer().getDesignation());
        assertEquals("2026-09-08", signers.getChiefEngineer().getDate());

        assertEquals("Eng. G. W. Vajira Priyantha", signers.getDirector().getName());
        assertEquals("Director (ET-GL)", signers.getDirector().getDesignation());
        assertEquals("2026-09-09", signers.getDirector().getDate());
        assertEquals("DGM", signers.getDirector().getUserId());
    }

    @Test
    @DisplayName("Rejection and resubmission resets active CE and Director signatures")
    void testRejectionAndResubmissionResetsSigners() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));

        Invoice invoice = new Invoice();
        invoice.setId(104L);
        invoice.setStatus(InvoiceStatus.RECOMMEND);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 1, 9, 0));

        // h1: RECOMMEND -> APPROVE by old CE
        InvoiceStatusHistory h1 = InvoiceStatusHistory.builder()
                .id((short) 1)
                .invoiceId((short) 104)
                .statusTo("APPROVE")
                .changedBy("OldCE")
                .changedAt(LocalDateTime.of(2026, 9, 2, 10, 0))
                .build();

        // h2: APPROVE -> REJECTED by DGM
        InvoiceStatusHistory h2 = InvoiceStatusHistory.builder()
                .id((short) 2)
                .invoiceId((short) 104)
                .statusTo("REJECTED")
                .changedBy("DGM")
                .changedAt(LocalDateTime.of(2026, 9, 3, 11, 0))
                .build();

        // h3: REJECTED -> RECOMMEND by REP3 (resubmission)
        InvoiceStatusHistory h3 = InvoiceStatusHistory.builder()
                .id((short) 3)
                .invoiceId((short) 104)
                .statusTo("RECOMMEND")
                .changedBy("REP3")
                .changedAt(LocalDateTime.of(2026, 9, 4, 12, 0))
                .build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 104))
                .thenReturn(Arrays.asList(h3, h2, h1));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        // CE and Director must be reset after rejection/resubmission
        assertEquals("", signers.getChiefEngineer().getName());
        assertEquals("", signers.getChiefEngineer().getDate());
        assertEquals("", signers.getDirector().getName());
        assertEquals("", signers.getDirector().getDate());
    }

    @Test
    @DisplayName("Re-approval after rejection captures new CE approval")
    void testReapprovalAfterRejectionCapturesNewApproval() {
        UserAccSecInfo newCE = new UserAccSecInfo();
        newCE.setUserId("NewCE");
        newCE.setUserName("Eng. New Approver");
        newCE.setUserCat("Chief Engineer");

        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("NewCE"))).thenReturn(Optional.of(newCE));

        Invoice invoice = new Invoice();
        invoice.setId(105L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 1, 9, 0));

        InvoiceStatusHistory h1 = InvoiceStatusHistory.builder()
                .id((short) 1).invoiceId((short) 105).statusTo("APPROVE").changedBy("OldCE")
                .changedAt(LocalDateTime.of(2026, 9, 2, 10, 0)).build();
        InvoiceStatusHistory h2 = InvoiceStatusHistory.builder()
                .id((short) 2).invoiceId((short) 105).statusTo("REJECTED").changedBy("DGM")
                .changedAt(LocalDateTime.of(2026, 9, 3, 11, 0)).build();
        InvoiceStatusHistory h3 = InvoiceStatusHistory.builder()
                .id((short) 3).invoiceId((short) 105).statusTo("RECOMMEND").changedBy("REP3")
                .changedAt(LocalDateTime.of(2026, 9, 4, 12, 0)).build();
        InvoiceStatusHistory h4 = InvoiceStatusHistory.builder()
                .id((short) 4).invoiceId((short) 105).statusTo("APPROVE").changedBy("NewCE")
                .changedAt(LocalDateTime.of(2026, 9, 5, 15, 0)).build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 105))
                .thenReturn(Arrays.asList(h4, h3, h2, h1));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertEquals("Eng. New Approver", signers.getChiefEngineer().getName());
        assertEquals("Chief Engineer (REP)", signers.getChiefEngineer().getDesignation());
        assertEquals("2026-09-05", signers.getChiefEngineer().getDate());
        assertEquals("NewCE", signers.getChiefEngineer().getUserId());

        // Director is still blank
        assertEquals("", signers.getDirector().getName());
    }

    @Test
    @DisplayName("Prepared date recovered from history if preparedAt is null on invoice")
    void testPreparedDateRecoveredFromHistoryIfNull() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));

        Invoice invoice = new Invoice();
        invoice.setId(106L);
        invoice.setStatus(InvoiceStatus.RECOMMEND);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(null); // null preparedAt

        InvoiceStatusHistory h1 = InvoiceStatusHistory.builder()
                .id((short) 1)
                .invoiceId((short) 106)
                .statusFrom("DRAFT")
                .statusTo("RECOMMEND")
                .changedBy("REP3")
                .changedAt(LocalDateTime.of(2026, 8, 25, 11, 20))
                .build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 106))
                .thenReturn(Collections.singletonList(h1));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertEquals("Eng. R.S. Ukwatta", signers.getPreparedBy().getName());
        assertEquals("2026-08-25", signers.getPreparedBy().getDate());
    }

    @Test
    @DisplayName("Legacy fallback to invoice entity fields when history is empty")
    void testLegacyFallbackToInvoiceEntity() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("CEng"))).thenReturn(Optional.of(cengUser));

        Invoice invoice = new Invoice();
        invoice.setId(107L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 1, 8, 0));
        invoice.setApprovedBy("CEng");
        invoice.setApprovedAt(LocalDateTime.of(2026, 9, 2, 14, 0));

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 107))
                .thenReturn(Collections.emptyList());

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertEquals("Eng. (Mrs.) A.G.C.U Perera", signers.getChiefEngineer().getName());
        assertEquals("2026-09-02", signers.getChiefEngineer().getDate());
    }

    @Test
    @DisplayName("Unsaved draft preview handles null invoice gracefully")
    void testNullInvoiceReturnsBlankSigners() {
        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(null);

        assertNotNull(signers.getPreparedBy());
        assertEquals("", signers.getPreparedBy().getName());
        assertEquals("", signers.getPreparedBy().getDate());

        assertNotNull(signers.getChiefEngineer());
        assertEquals("", signers.getChiefEngineer().getName());
        assertEquals("", signers.getChiefEngineer().getDate());

        assertNotNull(signers.getDirector());
        assertEquals("", signers.getDirector().getName());
        assertEquals("", signers.getDirector().getDate());
    }

    @Test
    @DisplayName("resolveUserSigner with unknown user gracefully falls back to userId and default designation")
    void testResolveUserSignerUnknownUser() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("UNKNOWN_EE"))).thenReturn(Optional.empty());

        InvoicePreviewDTO.SignerDetailsDTO signer = invoiceService.resolveUserSigner(
                "UNKNOWN_EE", LocalDateTime.of(2026, 9, 10, 8, 0), "Electrical Engineer");

        assertEquals("UNKNOWN_EE", signer.getUserId());
        assertEquals("UNKNOWN_EE", signer.getName());
        assertEquals("EE (UNKNOWN_EE)", signer.getDesignation());
        assertEquals("2026-09-10", signer.getDate());
    }

    @Test
    @DisplayName("Director with DB userCat DIRECTOR formats as DIRECTOR (ET-GL)")
    void testResolveDirectorWithDirectorCategory() {
        UserAccSecInfo directorUser = new UserAccSecInfo();
        directorUser.setUserId("DIR1");
        directorUser.setUserName("S.H.Ediriweera");
        directorUser.setUserCat("DIRECTOR");

        when(userAccSecInfoRepository.findByIdTrimmed(eq("DIR1"))).thenReturn(Optional.of(directorUser));

        InvoicePreviewDTO.SignerDetailsDTO signer = invoiceService.resolveUserSigner(
                "DIR1", LocalDateTime.of(2026, 9, 10, 8, 0), "Director");

        assertEquals("DIR1", signer.getUserId());
        assertEquals("S.H.Ediriweera", signer.getName());
        assertEquals("DIRECTOR (ET-GL)", signer.getDesignation());
        assertEquals("2026-09-10", signer.getDate());
    }

    @Test
    @DisplayName("Chief Engineer with DB userCat Chief Engineer formats as Chief Engineer (REP)")
    void testResolveChiefEngineerWithChiefEngineerCategory() {
        UserAccSecInfo ceUser = new UserAccSecInfo();
        ceUser.setUserId("CE01");
        ceUser.setUserName("Eng. Sample Approver");
        ceUser.setUserCat("Chief Engineer");

        when(userAccSecInfoRepository.findByIdTrimmed(eq("CE01"))).thenReturn(Optional.of(ceUser));

        InvoicePreviewDTO.SignerDetailsDTO signer = invoiceService.resolveUserSigner(
                "CE01", LocalDateTime.of(2026, 9, 10, 8, 0), "Chief Engineer");

        assertEquals("CE01", signer.getUserId());
        assertEquals("Eng. Sample Approver", signer.getName());
        assertEquals("Chief Engineer (REP)", signer.getDesignation());
        assertEquals("2026-09-10", signer.getDate());
    }

    @Test
    @DisplayName("Default roles format with (REP) and (ET-GL) when user not found in DB")
    void testResolveDefaultRolesFallbackFormatting() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("UNKNOWN_CE"))).thenReturn(Optional.empty());
        when(userAccSecInfoRepository.findByIdTrimmed(eq("UNKNOWN_DIR"))).thenReturn(Optional.empty());

        InvoicePreviewDTO.SignerDetailsDTO ceSigner = invoiceService.resolveUserSigner(
                "UNKNOWN_CE", LocalDateTime.of(2026, 9, 10, 8, 0), "Chief Engineer");
        assertEquals("Chief Engineer (REP)", ceSigner.getDesignation());

        InvoicePreviewDTO.SignerDetailsDTO dirSigner = invoiceService.resolveUserSigner(
                "UNKNOWN_DIR", LocalDateTime.of(2026, 9, 10, 8, 0), "Director");
        assertEquals("Director (ET-GL)", dirSigner.getDesignation());
    }

    @Test
    @DisplayName("WorkflowSigners correctly resolves created on and approved on dates")
    void testWorkflowSignersCreatedAndApprovedDates() {
        when(userAccSecInfoRepository.findByIdTrimmed(eq("REP3"))).thenReturn(Optional.of(rep3User));
        when(userAccSecInfoRepository.findByIdTrimmed(eq("DGM"))).thenReturn(Optional.of(dgmUser));

        Invoice invoice = new Invoice();
        invoice.setId(105L);
        invoice.setStatus(InvoiceStatus.FINALIZE);
        invoice.setPreparedBy("REP3");
        invoice.setPreparedAt(LocalDateTime.of(2026, 9, 7, 10, 15));

        InvoiceStatusHistory hFinal = InvoiceStatusHistory.builder()
                .id((short) 1)
                .invoiceId((short) 105)
                .statusFrom("APPROVE")
                .statusTo("FINALIZE")
                .changedBy("DGM")
                .changedAt(LocalDateTime.of(2026, 9, 9, 9, 45))
                .build();

        when(invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc((short) 105))
                .thenReturn(Collections.singletonList(hFinal));

        InvoiceService.WorkflowSigners signers = invoiceService.resolveWorkflowSigners(invoice);

        assertEquals("2026-09-07", signers.getPreparedBy().getDate());
        assertEquals("2026-09-09", signers.getDirector().getDate());

        InvoicePreviewDTO previewDTO = InvoicePreviewDTO.builder()
                .invoiceCreatedOn(signers.getPreparedBy().getDate())
                .invoiceApprovedOn(signers.getDirector().getDate())
                .build();

        assertEquals("2026-09-07", previewDTO.getInvoiceCreatedOn());
        assertEquals("2026-09-09", previewDTO.getInvoiceApprovedOn());
    }
}
