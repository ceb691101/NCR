package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.InvoiceReviewRequestDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.*;
import com.example.SPSProjectBackend.repository.*;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NcreInvoiceCreateFinalizationTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceStatusHistoryRepository invoiceStatusHistoryRepository;

    @Mock
    private NcreInvoiceCreateRepository ncreInvoiceCreateRepository;

    @Mock
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvoiceService invoiceService;

    private SecInfoLoginDTO.UserInfo directorUserInfo;
    private SecInfoLoginDTO.UserInfo ceUserInfo;
    private Invoice approvedInvoice;

    @BeforeEach
    void setUp() {
        directorUserInfo = new SecInfoLoginDTO.UserInfo();
        directorUserInfo.setUserId("DIR1");
        directorUserInfo.setUserName("Mr. Director");
        directorUserInfo.setUserCategory("Director");

        ceUserInfo = new SecInfoLoginDTO.UserInfo();
        ceUserInfo.setUserId("CE01");
        ceUserInfo.setUserName("Eng. Chief");
        ceUserInfo.setUserCategory("Chief Engineer");

        approvedInvoice = new Invoice();
        approvedInvoice.setId(200L);
        approvedInvoice.setStatus(InvoiceStatus.APPROVE);
        approvedInvoice.setBillCycle(456);
        approvedInvoice.setFolioNo(12345);
        approvedInvoice.setPreparedBy("REP3");
        approvedInvoice.setAccountNumber("0123456789");
    }

    @Test
    @DisplayName("1-9: Director finalization updates status to FINALIZE and inserts ncre_invoice_create with correct fields")
    void testDirectorFinalizationCreatesNcreInvoiceCreate() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceStatusHistoryRepository.findMaxId()).thenReturn((short) 10);
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks("Invoice verified");

        Invoice result = invoiceService.approveInvoice(request);

        // 1. Invoice status becomes FINALIZE
        assertEquals(InvoiceStatus.FINALIZE, result.getStatus());
        assertEquals("DIR1", result.getApprovedBy());
        assertNotNull(result.getApprovedAt());

        // 2. ncre_invoice_create row created exactly once
        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository, times(1)).save(captor.capture());

        NcreInvoiceCreate row = captor.getValue();
        assertNotNull(row);

        // 3. bill_cycle matches finalized invoice bill cycle
        assertEquals(456, row.getBillCycle());

        // 4. folio_no matches finalized invoice folio
        assertEquals(12345, row.getFolioNo());

        // 5. is_create is exactly 1
        assertEquals(1, row.getIsCreate());

        // 6. created_by is original Prepared By EE / REP
        assertEquals("REP3", row.getCreatedBy());

        // 7. created_by is NOT the Director
        assertNotEquals("DIR1", row.getCreatedBy());

        // 8. created_by is NOT the Chief Engineer
        assertNotEquals("CE01", row.getCreatedBy());

        // 9. created_date equals the Director FINALIZE action date
        assertEquals(LocalDate.now(), row.getCreatedDateAsLocalDate());

        // 10. remarks are copied from the finalization request
        assertEquals("Invoice verified", row.getRemarks());

        // 24. Existing status history preserved
        verify(invoiceStatusHistoryRepository, times(1)).save(any(InvoiceStatusHistory.class));
    }

    @Test
    @DisplayName("11: Null remarks in request are handled safely (stored as null)")
    void testNullRemarksHandledSafely() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceStatusHistoryRepository.findMaxId()).thenReturn(null);
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks(null);

        invoiceService.approveInvoice(request);

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertNull(captor.getValue().getRemarks());
    }

    @Test
    @DisplayName("12: Remarks up to 100 characters work without truncation")
    void testRemarksUpTo100Characters() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        String remarks100 = "A".repeat(100);
        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks(remarks100);

        invoiceService.approveInvoice(request);

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertEquals(100, captor.getValue().getRemarks().length());
        assertEquals(remarks100, captor.getValue().getRemarks());
    }

    @Test
    @DisplayName("13: Remarks over 100 characters are safely truncated to 100 characters")
    void testRemarksOver100CharactersTruncated() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        String remarks150 = "B".repeat(150);
        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks(remarks150);

        invoiceService.approveInvoice(request);

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertEquals(100, captor.getValue().getRemarks().length());
        assertEquals("B".repeat(100), captor.getValue().getRemarks());
    }

    @Test
    @DisplayName("14: CE approval (RECOMMEND -> APPROVE) does NOT create ncre_invoice_create")
    void testCeApprovalDoesNotCreateNcreInvoiceCreate() {
        Invoice recommendInvoice = new Invoice();
        recommendInvoice.setId(201L);
        recommendInvoice.setStatus(InvoiceStatus.RECOMMEND);
        recommendInvoice.setBillCycle(456);
        recommendInvoice.setFolioNo(12345);
        recommendInvoice.setPreparedBy("REP3");

        when(sessionUtils.isSessionValid("sess-ce", "CE01")).thenReturn(true);
        when(invoiceRepository.findById(201L)).thenReturn(Optional.of(recommendInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-ce", "CE01")).thenReturn(Optional.of(ceUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-ce");
        request.setUserId("CE01");
        request.setInvoiceId(201L);
        request.setRemarks("CE Recommended");

        Invoice result = invoiceService.approveInvoice(request);

        assertEquals(InvoiceStatus.APPROVE, result.getStatus());
        verify(ncreInvoiceCreateRepository, never()).save(any(NcreInvoiceCreate.class));
    }

    @Test
    @DisplayName("15: Director rejection does NOT create ncre_invoice_create")
    void testDirectorRejectionDoesNotCreateNcreInvoiceCreate() {
        when(sessionUtils.isSessionValid("sess-dir", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-dir", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-dir");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks("Needs amendment");

        Invoice result = invoiceService.rejectInvoice(request);

        assertEquals(InvoiceStatus.REJECTED, result.getStatus());
        verify(ncreInvoiceCreateRepository, never()).save(any(NcreInvoiceCreate.class));
    }

    @Test
    @DisplayName("20-21: Idempotency: Director finalization updates existing record with suitable values")
    void testDuplicateFinalizationPreservesExistingRecord() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NcreInvoiceCreate existingRecord = NcreInvoiceCreate.builder()
            .billCycle(456)
            .folioNo(12345)
            .isCreate(0)
                .createdBy("OLD_REP")
            .createdDate(Date.from(LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .remarks("Initial draft")
                .build();

        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class)))
                .thenReturn(Optional.of(existingRecord));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks("Finalized by Director");

        invoiceService.approveInvoice(request);

        // Verifies existing record was updated and saved
        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository, times(1)).save(captor.capture());
        NcreInvoiceCreate saved = captor.getValue();
        assertEquals(1, saved.getIsCreate());
        assertEquals("Finalized by Director", saved.getRemarks());
        assertEquals("REP3", saved.getCreatedBy());
        assertEquals(LocalDate.now(), saved.getCreatedDateAsLocalDate());
    }

    @Test
    @DisplayName("22: Failure to insert ncre_invoice_create throws exception and propagates out of @Transactional method")
    void testFailureToInsertPropagatesException() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());
        when(ncreInvoiceCreateRepository.save(any(NcreInvoiceCreate.class)))
                .thenThrow(new RuntimeException("Database constraint violation on ncre_invoice_create"));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);

        assertThrows(RuntimeException.class, () -> invoiceService.approveInvoice(request));
    }

    @Test
    @DisplayName("Folio number resolved from developer if invoice folio_no is null")
    void testFolioNumberResolvedFromDeveloperWhenInvoiceFolioNull() {
        Invoice invoiceWithoutFolio = new Invoice();
        invoiceWithoutFolio.setId(205L);
        invoiceWithoutFolio.setStatus(InvoiceStatus.APPROVE);
        invoiceWithoutFolio.setBillCycle(456);
        invoiceWithoutFolio.setFolioNo(null);
        invoiceWithoutFolio.setPreparedBy("REP3");
        invoiceWithoutFolio.setAccountNumber("0987654321");

        NcreDeveloper dev = new NcreDeveloper();
        dev.setAccNbr("0987654321");
        dev.setFolioNo((short) 9988);

        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(205L)).thenReturn(Optional.of(invoiceWithoutFolio));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreDeveloperRepository.findByAccNbrTrimmed("0987654321")).thenReturn(Optional.of(dev));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(205L);

        invoiceService.approveInvoice(request);

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertEquals(9988, captor.getValue().getFolioNo());
    }

    @Test
    @DisplayName("Attempting to finalize already FINALIZE invoice throws CONFLICT")
    void testFinalizeAlreadyFinalizedThrowsConflict() {
        Invoice finalizedInvoice = new Invoice();
        finalizedInvoice.setId(206L);
        finalizedInvoice.setStatus(InvoiceStatus.FINALIZE);

        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(206L)).thenReturn(Optional.of(finalizedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(206L);

        assertThrows(ResponseStatusException.class, () -> invoiceService.approveInvoice(request));
        verify(ncreInvoiceCreateRepository, never()).save(any(NcreInvoiceCreate.class));
    }

    @Test
    @DisplayName("Existing record is updated with suitable values for finalized invoice")
    void testConflictingExistingRecordThrowsConflict() {
        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NcreInvoiceCreate existingConflictingRecord = NcreInvoiceCreate.builder()
            .billCycle(456)
            .folioNo(12345)
            .isCreate(0)
                .createdBy("DIFFERENT_EE")
            .createdDate(Date.from(LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .remarks("Unrelated invoice")
                .build();

        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class)))
                .thenReturn(Optional.of(existingConflictingRecord));

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);
        request.setRemarks("Final Approval");

        Invoice result = invoiceService.approveInvoice(request);
        assertEquals(InvoiceStatus.FINALIZE, result.getStatus());

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository, times(1)).save(captor.capture());
        assertEquals((short) 1, captor.getValue().getIsCreate());
        assertEquals("Final Approval", captor.getValue().getRemarks());
        assertEquals("REP3", captor.getValue().getCreatedBy());
    }

    @Test
    @DisplayName("DGM user category can also finalize and creates ncre_invoice_create")
    void testDgmCategoryFinalizes() {
        SecInfoLoginDTO.UserInfo dgmUserInfo = new SecInfoLoginDTO.UserInfo();
        dgmUserInfo.setUserId("DGM01");
        dgmUserInfo.setUserName("Mr. DGM");
        dgmUserInfo.setUserCategory("DGM");

        when(sessionUtils.isSessionValid("sess-dgm", "DGM01")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-dgm", "DGM01")).thenReturn(Optional.of(dgmUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-dgm");
        request.setUserId("DGM01");
        request.setInvoiceId(200L);
        request.setRemarks("DGM Approved");

        Invoice result = invoiceService.approveInvoice(request);

        assertEquals(InvoiceStatus.FINALIZE, result.getStatus());
        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertEquals("REP3", captor.getValue().getCreatedBy());
        assertNotEquals("DGM01", captor.getValue().getCreatedBy());
    }

    @Test
    @DisplayName("Created_by is safely truncated to 12 characters if longer than 12")
    void testCreatedByTruncatedTo12Chars() {
        approvedInvoice.setPreparedBy("VERY_LONG_EE_USER_ID");

        when(sessionUtils.isSessionValid("sess-1", "DIR1")).thenReturn(true);
        when(invoiceRepository.findById(200L)).thenReturn(Optional.of(approvedInvoice));
        when(sessionUtils.getUserLocationFromSession("sess-1", "DIR1")).thenReturn(Optional.of(directorUserInfo));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ncreInvoiceCreateRepository.findById(any(NcreInvoiceCreateId.class))).thenReturn(Optional.empty());

        InvoiceReviewRequestDTO request = new InvoiceReviewRequestDTO();
        request.setSessionId("sess-1");
        request.setUserId("DIR1");
        request.setInvoiceId(200L);

        invoiceService.approveInvoice(request);

        ArgumentCaptor<NcreInvoiceCreate> captor = ArgumentCaptor.forClass(NcreInvoiceCreate.class);
        verify(ncreInvoiceCreateRepository).save(captor.capture());
        assertEquals(12, captor.getValue().getCreatedBy().length());
        assertEquals("VERY_LONG_EE", captor.getValue().getCreatedBy());
    }
}
