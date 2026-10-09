package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.InvoiceDocumentDTO;
import com.example.SPSProjectBackend.dto.InvoiceGenerationRequestDTO;
import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.model.BillCycleConfig;
import com.example.SPSProjectBackend.model.BulkCustomer;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.model.InvoiceStatusHistory;
import com.example.SPSProjectBackend.model.Agreement;
import com.example.SPSProjectBackend.model.PaymentDeduction;
import com.example.SPSProjectBackend.repository.AgreementRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.BulkCustomerRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import com.example.SPSProjectBackend.repository.TmpReadingsRepository;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import com.example.SPSProjectBackend.repository.InvoiceRepository;
import com.example.SPSProjectBackend.repository.InvoiceStatusHistoryRepository;
import com.example.SPSProjectBackend.model.NcreInvoiceCreate;
import com.example.SPSProjectBackend.model.NcreInvoiceCreateId;
import com.example.SPSProjectBackend.repository.NcreInvoiceCreateRepository;
import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.model.YrMnth;
import com.example.SPSProjectBackend.repository.YrMnthRepository;
import com.example.SPSProjectBackend.dto.BulkInvoiceReviewRequestDTO;
import com.example.SPSProjectBackend.dto.InvoiceReviewRequestDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import com.example.SPSProjectBackend.dto.ValidationResultDTO;
import com.example.SPSProjectBackend.model.InvoiceTariffChargeLine;
import com.example.SPSProjectBackend.repository.InvoiceTariffChargeLineRepository;
import com.example.SPSProjectBackend.service.tariff.TariffCalculationContext;
import com.example.SPSProjectBackend.service.tariff.TariffCalculationResult;
import com.example.SPSProjectBackend.service.tariff.TariffCalculator;
import com.example.SPSProjectBackend.service.tariff.TariffCalculatorResolver;
import com.example.SPSProjectBackend.service.tariff.TariffChargeLine;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.example.SPSProjectBackend.dto.BillCycleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@Transactional(readOnly = true)
public class InvoiceService {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter INVOICE_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> METER_ORDER = List.of("KWO", "KWD", "KWP");

    @Autowired
    private BulkCustomerRepository bulkCustomerRepository;

    @Autowired
    private TmpReadingsRepository tmpReadingsRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private HsbLocationService hsbLocationService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private SpringTemplateEngine invoiceTemplateEngine;

    @Autowired
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Autowired
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    @Autowired
    private InvoiceCalculationService invoiceCalculationService;

    @Autowired
    private TariffCalculatorResolver tariffCalculatorResolver;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceStatusHistoryRepository invoiceStatusHistoryRepository;

    @Autowired
    private InvoiceTariffChargeLineRepository invoiceTariffChargeLineRepository;

    @Autowired
    private InvoiceValidationService invoiceValidationService;

    @Autowired
    private AgreementRepository agreementRepository;

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NcreInvoiceCreateRepository ncreInvoiceCreateRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private YrMnthRepository yrMnthRepository;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private BillCycleService billCycleService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private InvoiceService self;

    public static class PreparedByDetails {
        private final String name;
        private final String title;

        public PreparedByDetails(String name, String title) {
            this.name = name;
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class WorkflowSigners {
        private final InvoicePreviewDTO.SignerDetailsDTO preparedBy;
        private final InvoicePreviewDTO.SignerDetailsDTO chiefEngineer;
        private final InvoicePreviewDTO.SignerDetailsDTO director;

        public WorkflowSigners(InvoicePreviewDTO.SignerDetailsDTO preparedBy,
                               InvoicePreviewDTO.SignerDetailsDTO chiefEngineer,
                               InvoicePreviewDTO.SignerDetailsDTO director) {
            this.preparedBy = preparedBy;
            this.chiefEngineer = chiefEngineer;
            this.director = director;
        }

        public InvoicePreviewDTO.SignerDetailsDTO getPreparedBy() { return preparedBy; }
        public InvoicePreviewDTO.SignerDetailsDTO getChiefEngineer() { return chiefEngineer; }
        public InvoicePreviewDTO.SignerDetailsDTO getDirector() { return director; }
    }

    public InvoicePreviewDTO.SignerDetailsDTO resolveUserSigner(String userId, LocalDateTime actionTimestamp, String defaultRoleTitle) {
        if (userId == null || userId.trim().isEmpty()) {
            String dateStr = actionTimestamp != null
                    ? actionTimestamp.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : "";
            return InvoicePreviewDTO.SignerDetailsDTO.builder()
                    .userId("")
                    .name("")
                    .designation("")
                    .date(dateStr)
                    .build();
        }

        String cleanUserId = userId.trim();
        Optional<UserAccSecInfo> userOpt = userAccSecInfoRepository.findByIdTrimmed(cleanUserId);
        String name = cleanUserId;
        String designation = defaultRoleTitle != null ? defaultRoleTitle : "";

        if (userOpt.isPresent()) {
            UserAccSecInfo user = userOpt.get();
            if (user.getUserName() != null && !user.getUserName().trim().isEmpty()) {
                name = user.getUserName().trim();
            }
            if (user.getUserCat() != null && !user.getUserCat().trim().isEmpty()) {
                String cat = user.getUserCat().trim();
                if ("Electrical Engineer".equalsIgnoreCase(cat) || "EE".equalsIgnoreCase(cat)) {
                    designation = "EE (" + cleanUserId + ")";
                } else {
                    designation = cat;
                }
            }
        } else {
            if ("Electrical Engineer".equalsIgnoreCase(defaultRoleTitle) || "EE".equalsIgnoreCase(defaultRoleTitle)) {
                designation = "EE (" + cleanUserId + ")";
            }
        }

        if (designation != null && !designation.trim().isEmpty()) {
            String trimmedDesig = designation.trim();
            if ("Chief Engineer".equalsIgnoreCase(trimmedDesig)) {
                designation = trimmedDesig + " (REP)";
            } else if ("DIRECTOR".equalsIgnoreCase(trimmedDesig) || "Director".equalsIgnoreCase(trimmedDesig)) {
                designation = trimmedDesig + " (ET-GL)";
            }
        }

        String dateStr = actionTimestamp != null
                ? actionTimestamp.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "";

        return InvoicePreviewDTO.SignerDetailsDTO.builder()
                .userId(cleanUserId)
                .name(name)
                .designation(designation)
                .date(dateStr)
                .build();
    }

    public WorkflowSigners resolveWorkflowSigners(Invoice invoice) {
        if (invoice == null) {
            InvoicePreviewDTO.SignerDetailsDTO blank = InvoicePreviewDTO.SignerDetailsDTO.builder()
                    .userId("").name("").designation("").date("").build();
            return new WorkflowSigners(blank, blank, blank);
        }

        LocalDateTime prepAt = invoice.getPreparedAt();
        InvoicePreviewDTO.SignerDetailsDTO prepSigner = resolveUserSigner(
                invoice.getPreparedBy(), prepAt, "Electrical Engineer");

        List<InvoiceStatusHistory> histories = Collections.emptyList();
        if (invoice.getId() != null) {
            histories = invoiceStatusHistoryRepository.findByInvoiceIdOrderByChangedAtDesc(
                    (short) invoice.getId().longValue());
        }

        InvoiceStatusHistory activeCE = null;
        InvoiceStatusHistory activeFinal = null;

        if (histories != null && !histories.isEmpty()) {
            List<InvoiceStatusHistory> chronological = new ArrayList<>(histories);
            chronological.sort(Comparator.comparing(InvoiceStatusHistory::getId));

            for (InvoiceStatusHistory h : chronological) {
                String to = h.getStatusTo() != null ? h.getStatusTo().trim().toUpperCase() : "";
                if ("REJECTED".equals(to) || "RECOMMEND".equals(to) || "DRAFT".equals(to)) {
                    activeCE = null;
                    activeFinal = null;
                } else if ("APPROVE".equals(to)) {
                    activeCE = h;
                    activeFinal = null;
                } else if ("FINALIZE".equals(to)) {
                    activeFinal = h;
                }
            }

            if (prepAt == null) {
                for (InvoiceStatusHistory h : chronological) {
                    String to = h.getStatusTo() != null ? h.getStatusTo().trim().toUpperCase() : "";
                    if ("DRAFT".equals(to) || "RECOMMEND".equals(to)) {
                        prepSigner.setDate(h.getChangedAt() != null
                                ? h.getChangedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
                        break;
                    }
                }
            }
        }

        if ((prepSigner.getDate() == null || prepSigner.getDate().isEmpty()) && invoice.getIssueDate() != null) {
            prepSigner.setDate(invoice.getIssueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        InvoicePreviewDTO.SignerDetailsDTO ceSigner;
        if (activeCE != null) {
            ceSigner = resolveUserSigner(activeCE.getChangedBy(), activeCE.getChangedAt(), "Chief Engineer");
        } else if (invoice.getStatus() == InvoiceStatus.APPROVE && invoice.getApprovedBy() != null) {
            ceSigner = resolveUserSigner(invoice.getApprovedBy(), invoice.getApprovedAt(), "Chief Engineer");
        } else {
            ceSigner = InvoicePreviewDTO.SignerDetailsDTO.builder()
                    .userId("").name("").designation("").date("").build();
        }

        InvoicePreviewDTO.SignerDetailsDTO dirSigner;
        if (activeFinal != null) {
            dirSigner = resolveUserSigner(activeFinal.getChangedBy(), activeFinal.getChangedAt(), "Director");
        } else if (invoice.getStatus() == InvoiceStatus.FINALIZE) {
            dirSigner = resolveUserSigner(invoice.getApprovedBy(), invoice.getApprovedAt(), "Director");
        } else {
            dirSigner = InvoicePreviewDTO.SignerDetailsDTO.builder()
                    .userId("").name("").designation("").date("").build();
        }

        if (invoice.getStatus() == InvoiceStatus.FINALIZE && (dirSigner.getDate() == null || dirSigner.getDate().isEmpty()) && invoice.getApprovedAt() != null) {
            dirSigner.setDate(invoice.getApprovedAt().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        return new WorkflowSigners(prepSigner, ceSigner, dirSigner);
    }

    public PreparedByDetails resolvePreparedByDetails(String userId) {
        InvoicePreviewDTO.SignerDetailsDTO signer = resolveUserSigner(userId, null, "Electrical Engineer");
        return new PreparedByDetails(signer.getName(), signer.getDesignation());
    }

    public byte[] generateInvoicePdf(InvoiceGenerationRequestDTO request) {
        InvoicePreviewDTO previewDTO = prepareInvoiceData(request);
        String html = renderInvoiceHtml(previewDTO);
        return renderPdf(html);
    }

    private void validateSessionAndAccess(InvoiceGenerationRequestDTO request) {
        if (!sessionUtils.isSessionValid(request.getSessionId(), request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }

        HsbAreaDTO area = hsbLocationService.getAreaByCode(request.getAreaCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No area found for area code " + request.getAreaCode()));

        if (!sessionUtils.hasAreaAccess(request.getSessionId(), request.getUserId(), area.getRegion(), area.getProvCode(), area.getAreaCode())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
        }
    }

    private InvoiceDocumentDTO buildInvoiceDocument(
            InvoiceGenerationRequestDTO request,
            BulkCustomer customer,
            HsbAreaDTO area,
            List<TmpReadings> readings) {

        Map<String, TmpReadings> readingsByType = new LinkedHashMap<>();
        for (String meterType : METER_ORDER) {
            readingsByType.put(meterType, null);
        }

        readings.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TmpReadings::getMtrSeq, Comparator.nullsLast(Integer::compareTo)).reversed())
                .forEach(reading -> {
                    String meterType = reading.getMtrType();
                    if (meterType != null) {
                        String normalizedType = meterType.trim().toUpperCase(Locale.ENGLISH);
                        if (readingsByType.containsKey(normalizedType)) {
                            readingsByType.put(normalizedType, reading);
                        }
                    }
                });

        List<InvoiceDocumentDTO.InvoiceLineItemDTO> lineItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal maintenanceFees = BigDecimal.ZERO;

        for (String meterType : METER_ORDER) {
            TmpReadings reading = readingsByType.get(meterType);
            InvoiceDocumentDTO.InvoiceLineItemDTO lineItem = createLineItem(meterType, reading);
            lineItems.add(lineItem);

            if (reading != null) {
                subtotal = subtotal.add(toMoney(reading.getComputedChg()));
                maintenanceFees = maintenanceFees.add(toMoney(reading.getMntChg()));
            }
        }

        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(maintenanceFees).add(taxAmount);

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(14);
        LocalDateTime invoiceSequence = LocalDateTime.now();

        String invoiceNumber = "INV-"
                + request.getAreaCode().toUpperCase(Locale.ENGLISH)
                + "-"
                + request.getAccountNumber().trim().toUpperCase(Locale.ENGLISH)
                + "-"
                + String.format("%03d", request.getBillCycle())
                + "-"
                + invoiceSequence.format(INVOICE_NUMBER_FORMAT);

        String billingPeriod = "Bill Cycle " + request.getBillCycle();
        String customerAddressLine2 = customer.getAddressL2() == null ? "" : customer.getAddressL2();

        String meterNumber = lineItems.stream()
                .filter(InvoiceDocumentDTO.InvoiceLineItemDTO::isAvailable)
                .map(item -> readingsByType.get(item.getMeterType()))
                .filter(Objects::nonNull)
                .map(TmpReadings::getMtrNbr)
                .filter(value -> value != null && !value.trim().isEmpty())
                .findFirst()
                .orElse("");

        return new InvoiceDocumentDTO(
                invoiceNumber,
                issueDate.format(DISPLAY_DATE_FORMAT),
                dueDate.format(DISPLAY_DATE_FORMAT),
                billingPeriod,
                String.valueOf(request.getBillCycle()),
                customer.getAccNbr(),
                area.getAreaCode(),
                area.getAreaName(),
                customer.getName(),
                customer.getAddressL1(),
                customerAddressLine2,
                customer.getCity(),
                customer.getTelNbr(),
                customer.getTariff(),
                customer.getTaxNum(),
                meterNumber,
                lineItems,
                new InvoiceDocumentDTO.InvoiceSummaryDTO(
                        formatMoney(subtotal),
                        formatMoney(maintenanceFees),
                        formatMoney(taxAmount),
                        formatMoney(totalAmount)
                )
        );
    }

    private InvoiceDocumentDTO.InvoiceLineItemDTO createLineItem(String meterType, TmpReadings reading) {
        if (reading == null) {
            return new InvoiceDocumentDTO.InvoiceLineItemDTO(
                    meterType,
                    describeMeterType(meterType),
                    false,
                    null,
                    null,
                    null,
                    formatMoney(BigDecimal.ZERO),
                    formatMoney(BigDecimal.ZERO)
            );
        }

        return new InvoiceDocumentDTO.InvoiceLineItemDTO(
                meterType,
                describeMeterType(meterType),
                true,
                reading.getPrvRdn(),
                reading.getPrsntRdn(),
                reading.getUnits(),
                formatMoney(reading.getRate()),
                formatMoney(reading.getComputedChg())
        );
    }

    private String describeMeterType(String meterType) {
        if (meterType == null) {
            return "Meter Reading";
        }

        return switch (meterType.trim().toUpperCase(Locale.ENGLISH)) {
            case "KWO" -> "Off Peak";
            case "KWD" -> "Day";
            case "KWP" -> "Peak";
            default -> "Meter Reading";
        };
    }

    private String renderInvoiceHtml(InvoicePreviewDTO invoice) {
        if (invoice != null) {
            if (invoice.getChiefEngineer() != null && invoice.getChiefEngineer().getDesignation() != null) {
                String d = invoice.getChiefEngineer().getDesignation().trim();
                if ("Chief Engineer".equalsIgnoreCase(d)) {
                    invoice.getChiefEngineer().setDesignation(d + " (REP)");
                }
            }
            if (invoice.getDirector() != null && invoice.getDirector().getDesignation() != null) {
                String d = invoice.getDirector().getDesignation().trim();
                if ("DIRECTOR".equalsIgnoreCase(d) || "Director".equalsIgnoreCase(d)) {
                    invoice.getDirector().setDesignation(d + " (ET-GL)");
                }
            }
        }
        Context context = new Context();
        context.setVariable("invoice", invoice);
        context.setVariable("finalAmountInWords", convertNumberToWords(invoice != null ? invoice.getFinalAmountToBePaid() : null));
        context.setVariable("logoBase64", getLogoBase64());
        return invoiceTemplateEngine.process("invoice", context);
    }

    private String getLogoBase64() {
        try {
            ClassPathResource resource = new ClassPathResource("assets/nso_logo.png");
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            System.err.println("Failed to load logo: " + e.getMessage());
            return "";
        }
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(outputStream);
            builder.useFastMode();
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

    private BigDecimal toMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal amount) {
        return toMoney(amount).toPlainString();
    }

    // ============ INVOICE PREVIEW (JSON) ============

    /**
     * Prepare invoice data as JSON for the frontend modal preview.
     * Fetches data from ncre_developers and tmp_rdngs tables,
     * performs calculations, and returns a structured DTO.
     */
    public InvoicePreviewDTO prepareInvoiceData(InvoiceGenerationRequestDTO request) {
        System.out.println("[InvoiceService] prepareInvoiceData - START for account: " + request.getAccountNumber());
        long start = System.currentTimeMillis();
        
        System.out.println("[InvoiceService] prepareInvoiceData - Step 1: Validating session/access...");
        validateSessionAndAccess(request);
        System.out.println("[InvoiceService] prepareInvoiceData - Step 1 completed in " + (System.currentTimeMillis() - start) + " ms");

        // If invoice is already finalized, load preview directly from DB instead of recalculating
        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByAccountNumberAndAreaCodeAndBillCycle(
                request.getAccountNumber().trim(), request.getAreaCode(), request.getBillCycle());

        if (existingInvoiceOpt.isPresent()) {
            Invoice invoice = existingInvoiceOpt.get();
            if (InvoiceStatus.FINALIZE == invoice.getStatus()) {
                System.out.println("[InvoiceService] prepareInvoiceData - Invoice is finalized. Loading preview from database.");
                return mapInvoiceToPreview(invoice);
            }
        }

        // 1. Fetch NCRE developer info
        long queryStart = System.currentTimeMillis();
        System.out.println("[InvoiceService] prepareInvoiceData - Step 2: Fetching developer info...");
        NcreDeveloper developer = ncreDeveloperRepository.findByAccNbrTrimmed(request.getAccountNumber())
                .orElseThrow(() -> {
                    System.err.println("[InvoiceService] prepareInvoiceData - ERROR: Developer not found for account: " + request.getAccountNumber());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No NCRE developer found for account number " + request.getAccountNumber());
                });
        System.out.println("[InvoiceService] prepareInvoiceData - Step 2 completed in " + (System.currentTimeMillis() - queryStart) + " ms. Developer name: " + developer.getDeveloperName());

        // 2. Fetch readings for this account/area/bill cycle from ncre_inv_rdngs
        queryStart = System.currentTimeMillis();
        System.out.println("[InvoiceService] prepareInvoiceData - Step 3: Fetching ncre_inv_rdngs for account=" + request.getAccountNumber() + ", area=" + request.getAreaCode() + ", cycle=" + request.getBillCycle());
        
        String currentCycleStr = String.valueOf(request.getBillCycle()).trim();
        String cleanAccNbr = request.getAccountNumber() != null ? request.getAccountNumber().trim() : "";
        String cleanAreaCd = request.getAreaCode() != null ? request.getAreaCode().trim() : "";
        List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository.findByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                cleanAccNbr, cleanAreaCd, currentCycleStr);

        if (currentReadings == null || currentReadings.isEmpty()) {
            System.err.println("[InvoiceService] prepareInvoiceData - ERROR: No ncre_inv_rdngs found for account: " + request.getAccountNumber());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No readings found for account " + request.getAccountNumber()
                            + " in area " + request.getAreaCode()
                            + " and bill cycle " + request.getBillCycle());
        }

        NcreInvRdngs currentReading = currentReadings.get(0);

        // Fetch previous cycle reading (cycle - 1)
        int currentCycleInt = Integer.parseInt(currentCycleStr);
        String previousCycleStr = String.valueOf(currentCycleInt - 1);
        List<NcreInvRdngs> previousReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                cleanAccNbr, cleanAreaCd, previousCycleStr);
        NcreInvRdngs previousReading = null;
        if (previousReadings != null && !previousReadings.isEmpty()) {
            if (currentReading.getMtrNbr() != null) {
                previousReading = previousReadings.stream()
                        .filter(p -> p.getMtrNbr() != null && p.getMtrNbr().trim().equalsIgnoreCase(currentReading.getMtrNbr().trim()))
                        .findFirst()
                        .orElse(previousReadings.get(0));
            } else {
                previousReading = previousReadings.get(0);
            }
        }

        // 3. Perform calculations
        System.out.println("[InvoiceService] prepareInvoiceData - Step 4: Performing invoice calculations...");
        
        BigDecimal prsntR1 = currentReading.getKwhR1() != null ? currentReading.getKwhR1() : BigDecimal.ZERO;
        BigDecimal prsntR2 = currentReading.getKwhR2() != null ? currentReading.getKwhR2() : BigDecimal.ZERO;
        BigDecimal prsntR3 = currentReading.getKwhR3() != null ? currentReading.getKwhR3() : BigDecimal.ZERO;

        BigDecimal prvR1 = (previousReading != null && previousReading.getKwhR1() != null) ? previousReading.getKwhR1() : BigDecimal.ZERO;
        BigDecimal prvR2 = (previousReading != null && previousReading.getKwhR2() != null) ? previousReading.getKwhR2() : BigDecimal.ZERO;
        BigDecimal prvR3 = (previousReading != null && previousReading.getKwhR3() != null) ? previousReading.getKwhR3() : BigDecimal.ZERO;

        BigDecimal r1 = invoiceCalculationService.calculateIntervalEnergy(prsntR1, prvR1);
        BigDecimal r2 = invoiceCalculationService.calculateIntervalEnergy(prsntR2, prvR2);
        BigDecimal r3 = invoiceCalculationService.calculateIntervalEnergy(prsntR3, prvR3);
        BigDecimal energyKwh = r1.add(r2).add(r3).setScale(1, RoundingMode.HALF_UP);
        
        int totalPresent = prsntR1.add(prsntR2).add(prsntR3).intValue();
        int totalPrevious = prvR1.add(prvR2).add(prvR3).intValue();
        BigDecimal multiplyFactor = currentReading.getMFactor() != null ? currentReading.getMFactor() : BigDecimal.ONE;

        YearMonth invoiceMonth = resolveInvoiceMonth(request.getBillCycle());
        int periodDays = invoiceCalculationService.calculatePeriodOfGeneration(invoiceMonth);

        BigDecimal allowedGenerationMw = developer.getCommissionedCapacityMw() != null
                ? developer.getCommissionedCapacityMw()
                : BigDecimal.ZERO;

        BigDecimal plantFactor = invoiceCalculationService.calculatePlantFactor(energyKwh, periodDays, allowedGenerationMw);

        // Fetch latest agreement for generation losses and deductions from ncre_agreements
        List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(developer.getFolioNo());
        Agreement latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;

        BigDecimal generationLosses = resolveGenerationLosses(latestAgreement, developer);

        TariffCalculationContext tariffContext = TariffCalculationContext.builder()
                .developer(developer)
                .folioNo(developer.getFolioNo())
                .tariffType(developer.getTariffType())
                .totalEnergySentToGrid(energyKwh)
                .generationLosses(generationLosses)
                .billCycle(request.getBillCycle())
                .billingMonth(invoiceMonth)
                .presentReadingDate(currentReading.getRdngDate() != null
                        ? (currentReading.getRdngDate() instanceof java.sql.Date
                                ? ((java.sql.Date) currentReading.getRdngDate()).toLocalDate()
                                : currentReading.getRdngDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        : null)
                .previousReadingDate(previousReading != null && previousReading.getRdngDate() != null
                        ? (previousReading.getRdngDate() instanceof java.sql.Date
                                ? ((java.sql.Date) previousReading.getRdngDate()).toLocalDate()
                                : previousReading.getRdngDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        : null)
                .build();

        TariffCalculator tariffCalculator = tariffCalculatorResolver.resolve(developer.getTariffType());
        TariffCalculationResult tariffResult = tariffCalculator.calculate(tariffContext);

        BigDecimal ratePerKwh = tariffResult.getSelectedTariffRate();
        BigDecimal eligibleEnergy = tariffResult.getEligibleEnergy();
        BigDecimal costOfEnergy = tariffResult.getCostOfEnergy();
        System.out.println("[InvoiceService] prepareInvoiceData - Step 4 calculations finished: totalPresent=" + totalPresent 
                + ", totalPrevious=" + totalPrevious + ", energyKwh=" + energyKwh + ", eligibleEnergy=" + eligibleEnergy
                + ", pf=" + plantFactor + ", ratePerKwh=" + ratePerKwh + ", costOfEnergy=" + costOfEnergy);

        // 4. Extract reading dates
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String presentReadingDate = currentReading.getRdngDate() != null ? sdf.format(currentReading.getRdngDate()) : "";
        String previousReadingDate = (previousReading != null && previousReading.getRdngDate() != null) ? sdf.format(previousReading.getRdngDate()) : "";

        // 5. Build header identifiers
        String regionSrNo = buildRegionSrNo(developer);
        String folioMonthLabel = buildFolioMonthLabel(developer, invoiceMonth);

        // 6. Format numbers for display
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
        DecimalFormat readingFormat = new DecimalFormat("#,##0.###");

        // 7. Calculate deductions from latest agreement
        List<InvoicePreviewDTO.PaymentDeductionDTO> deductionDTOs = new ArrayList<>();
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal finalAmount = costOfEnergy;
        BigDecimal ded1PercVal = BigDecimal.ZERO;

        if (latestAgreement != null && latestAgreement.getPaymentDeductions() != null && !latestAgreement.getPaymentDeductions().isEmpty()) {
            List<PaymentDeduction> agreementDeductions = latestAgreement.getPaymentDeductions();
            
            BigDecimal loyaltyAmount = BigDecimal.ZERO;
            BigDecimal baseForOtherDeductions = costOfEnergy;

            // Search for Loyalty deduction
            Optional<PaymentDeduction> loyaltyOpt = agreementDeductions.stream()
                    .filter(d -> isLoyaltyDeduction(d.getDeductionType()))
                    .findFirst();

            if (loyaltyOpt.isPresent()) {
                PaymentDeduction loyalty = loyaltyOpt.get();
                BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                loyaltyAmount = costOfEnergy.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                baseForOtherDeductions = costOfEnergy.subtract(loyaltyAmount);
            }

            BigDecimal runningBalance = costOfEnergy;

            // 1. Add Loyalty row first if present
            if (loyaltyOpt.isPresent()) {
                PaymentDeduction loyalty = loyaltyOpt.get();
                BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                runningBalance = runningBalance.subtract(loyaltyAmount);
                totalDeductions = totalDeductions.add(loyaltyAmount);

                deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                        .type(loyalty.getDeductionType())
                        .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                        .deductionAmount(moneyFormat.format(loyaltyAmount))
                        .amountToBePaid(moneyFormat.format(runningBalance))
                        .build());
            }

            // 2. Add all other deductions (using baseForOtherDeductions)
            for (PaymentDeduction d : agreementDeductions) {
                String type = d.getDeductionType();
                if (!isLoyaltyDeduction(type)) {
                    BigDecimal pct = d.getPercentage() != null ? d.getPercentage() : BigDecimal.ZERO;
                    BigDecimal amount = baseForOtherDeductions.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    runningBalance = runningBalance.subtract(amount);
                    totalDeductions = totalDeductions.add(amount);

                    if (isDed1PercDeduction(type)) {
                        ded1PercVal = amount;
                    }

                    deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                            .type(type)
                            .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                            .deductionAmount(moneyFormat.format(amount))
                            .amountToBePaid(moneyFormat.format(runningBalance))
                            .build());
                }
            }
            finalAmount = runningBalance;
        }

        System.out.println("[InvoiceService] prepareInvoiceData - END. Total time: " + (System.currentTimeMillis() - start) + " ms");
        
        WorkflowSigners workflowSigners;
        if (existingInvoiceOpt.isPresent()) {
            workflowSigners = resolveWorkflowSigners(existingInvoiceOpt.get());
        } else {
            String reqUserId = request.getUserId() != null ? request.getUserId().trim() : "";
            Optional<UserAccSecInfo> userOpt = userAccSecInfoRepository.findByIdTrimmed(reqUserId);
            boolean isEE = false;
            if (userOpt.isPresent()) {
                String userCat = userOpt.get().getUserCat() != null ? userOpt.get().getUserCat().trim() : "";
                if ("Electrical Engineer".equalsIgnoreCase(userCat) || "EE".equalsIgnoreCase(userCat)) {
                    isEE = true;
                }
            }

            InvoicePreviewDTO.SignerDetailsDTO prepSigner;
            if (isEE) {
                prepSigner = resolveUserSigner(reqUserId, LocalDateTime.now(), "Electrical Engineer");
            } else {
                prepSigner = InvoicePreviewDTO.SignerDetailsDTO.builder()
                        .userId("")
                        .name("N/A")
                        .designation("")
                        .date(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build();
            }
            InvoicePreviewDTO.SignerDetailsDTO blankSigner = InvoicePreviewDTO.SignerDetailsDTO.builder()
                    .userId("").name("").designation("").date("").build();
            workflowSigners = new WorkflowSigners(prepSigner, blankSigner, blankSigner);
        }

        List<InvoicePreviewDTO.TariffChargeLineDTO> chargeLineDTOs = new ArrayList<>();
        if (tariffResult.getChargeLines() != null && !tariffResult.getChargeLines().isEmpty()) {
            List<TariffChargeLine> lines = tariffResult.getChargeLines();
            TariffChargeLine line1 = lines.size() > 0 ? lines.get(0) : null;
            TariffChargeLine line2 = lines.size() > 1 ? lines.get(1) : null;
            String changeDateStr = line2 != null && line2.getPeriodStart() != null ? line2.getPeriodStart().toString() : "";

            for (int i = 0; i < lines.size(); i++) {
                TariffChargeLine line = lines.get(i);
                String dateRange = "";
                String remarks = "";

                if (lines.size() >= 2) {
                    if (i == 0) {
                        String startStr = (previousReadingDate != null && !previousReadingDate.isEmpty())
                                ? previousReadingDate
                                : (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "");
                        String endStr = !changeDateStr.isEmpty()
                                ? changeDateStr
                                : (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
                        dateRange = startStr + " to " + endStr;
                        remarks = "";
                    } else if (i == 1) {
                        String startStr = !changeDateStr.isEmpty()
                                ? changeDateStr
                                : (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "");
                        String endStr = (presentReadingDate != null && !presentReadingDate.isEmpty())
                                ? presentReadingDate
                                : (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
                        dateRange = startStr + " to " + endStr;
                        String tt = developer.getTariffType() != null ? developer.getTariffType().trim() : "";
                        if (tt.contains("2022") || tt.contains("New")) {
                            remarks = "Tariff Change 15+ New";
                        } else if (tt.contains("TTT5") || tt.contains("5 years") || tt.contains("5 Years")) {
                            remarks = "Tariff Change TTT 5 Years";
                        } else if (tt.contains("TTT3") || tt.contains("3 years") || tt.contains("3 Years") || tt.contains("3 yrs") || tt.contains("3YRS") || tt.contains("3 YRS")) {
                            remarks = "Tariff Change TTT 3 Years";
                        } else if ("AC".equalsIgnoreCase(tt) || tt.contains("Avoided Cost")) {
                            remarks = "Tariff Change Avoided Cost";
                        } else if ("FLAT_VT".equalsIgnoreCase(tt) || "FLAT (V)".equalsIgnoreCase(tt) || tt.toUpperCase().contains("FLAT VARIABLE") || tt.toUpperCase().contains("FLAT  VARIABLE")) {
                            remarks = "Tariff Change Flat Variable Tariff";
                        } else if ("VT".equalsIgnoreCase(tt) || tt.equalsIgnoreCase("Variable Tariff") || tt.equalsIgnoreCase("Variable Traiff")) {
                            remarks = "Tariff Change Variable Tariff";
                        } else {
                            remarks = "Tariff Change 15+";
                        }
                    }
                } else {
                    dateRange = (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "")
                            + " to " + (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
                }

                chargeLineDTOs.add(InvoicePreviewDTO.TariffChargeLineDTO.builder()
                        .label(line.getLabel())
                        .periodStart(line.getPeriodStart() != null ? line.getPeriodStart().toString() : "")
                        .periodEnd(line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "")
                        .dateRange(dateRange)
                        .numberOfDays(line.getNumberOfDays())
                        .energyKwh(line.getEnergyKwh() != null ? decimalFormat.format(line.getEnergyKwh()) : "0.0")
                        .ratePerKwh(line.getRatePerKwh() != null ? line.getRatePerKwh().toPlainString() : "0.00")
                        .costOfEnergy(line.getCostOfEnergy() != null ? moneyFormat.format(line.getCostOfEnergy()) : "0.00")
                        .rateType(line.getRateType() != null ? line.getRateType().name() : "")
                        .remarks(remarks)
                        .build());
            }
        }

        String eligibleEnergyLabel = buildEligibleEnergyLabel(generationLosses);
        String generationLossPercent = formatLossPercent(generationLosses);

        // 8. Build and return DTO
        return InvoicePreviewDTO.builder()
                .documentType("MAIN")
                .regionSrNo(regionSrNo)
                .folioMonthLabel(folioMonthLabel)
                .folioNo(developer.getFolioNo() != null ? String.valueOf(developer.getFolioNo()) : "")
                .refNo(trimOrDefault(developer.getFileRefNo(), ""))
                .refCode(trimOrDefault(developer.getReferenceCode(), ""))
                .companyName(trimOrDefault(developer.getDeveloperName(), ""))
                .projectName(trimOrDefault(developer.getFacilityName(), ""))
                .invoiceMonth(invoiceMonth.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
                        + " " + invoiceMonth.getYear())
                .capacityMw(formatCapacity(developer.getSppaCapacityMw()))
                .allowedGenerationMw(formatCapacity(allowedGenerationMw))
                .presentReadingDate(presentReadingDate)
                .previousReadingDate(previousReadingDate)
                .totalPresentReading(numberFormat.format(totalPresent))
                .totalPreviousReading(numberFormat.format(totalPrevious))
                .presentR1(prsntR1 != null ? readingFormat.format(prsntR1) : "0")
                .presentR2(prsntR2 != null ? readingFormat.format(prsntR2) : "0")
                .presentR3(prsntR3 != null ? readingFormat.format(prsntR3) : "0")
                .previousR1(prvR1 != null ? readingFormat.format(prvR1) : "0")
                .previousR2(prvR2 != null ? readingFormat.format(prvR2) : "0")
                .previousR3(prvR3 != null ? readingFormat.format(prvR3) : "0")
                .multiplyFactor(multiplyFactor.stripTrailingZeros().toPlainString())
                .energyKwh(decimalFormat.format(energyKwh))
                .eligibleEnergyKwh(numberFormat.format(eligibleEnergy.intValue()))
                .eligibleEnergyLabel(eligibleEnergyLabel)
                .generationLossPercent(generationLossPercent)
                .periodOfGeneration(String.valueOf(periodDays))
                .plantFactorPercent(plantFactor.toPlainString())
                .energyPurchasedKwh(decimalFormat.format(eligibleEnergy))
                .ratePerKwh(ratePerKwh.toPlainString())
                .costOfEnergy(moneyFormat.format(costOfEnergy))
                .printDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .preparedBy(workflowSigners.getPreparedBy())
                .chiefEngineer(workflowSigners.getChiefEngineer())
                .director(workflowSigners.getDirector())
                .invoiceCreatedOn(workflowSigners.getPreparedBy() != null && workflowSigners.getPreparedBy().getDate() != null ? workflowSigners.getPreparedBy().getDate() : "")
                .invoiceApprovedOn(workflowSigners.getDirector() != null && workflowSigners.getDirector().getDate() != null ? workflowSigners.getDirector().getDate() : "")
                .preparedByName(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getName() : "N/A")
                .preparedByTitle(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getDesignation() : "")
                .accountNumber(request.getAccountNumber())
                .areaCode(request.getAreaCode())
                .billCycle(request.getBillCycle())
                .paymentDeductions(deductionDTOs)
                .totalPaymentDeductions(moneyFormat.format(totalDeductions))
                .finalAmountToBePaid(moneyFormat.format(finalAmount))
                .engSendR1(decimalFormat.format(r1))
                .engSendR2(decimalFormat.format(r2))
                .engSendR3(decimalFormat.format(r3))
                .ded1Perc(moneyFormat.format(ded1PercVal))
                .tariffChargeLines(chargeLineDTOs)
                .build();
    }

    // ============ HELPER METHODS FOR INVOICE PREVIEW ============

    private static class IntervalReadings {
        String presentR1 = "0";
        String presentR2 = "0";
        String presentR3 = "0";
        String previousR1 = "0";
        String previousR2 = "0";
        String previousR3 = "0";
    }

    private IntervalReadings resolveIntervalReadings(String accNbr, String areaCd, String billCycle) {
        IntervalReadings ir = new IntervalReadings();
        if (accNbr == null || areaCd == null || billCycle == null) {
            return ir;
        }
        try {
            List<NcreInvRdngs> currentReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                    accNbr.trim(), areaCd.trim(), billCycle.trim());
            if (currentReadings != null && !currentReadings.isEmpty()) {
                NcreInvRdngs cr = currentReadings.get(0);
                DecimalFormat rf = new DecimalFormat("#,##0.###");
                if (cr.getKwhR1() != null) ir.presentR1 = rf.format(cr.getKwhR1());
                if (cr.getKwhR2() != null) ir.presentR2 = rf.format(cr.getKwhR2());
                if (cr.getKwhR3() != null) ir.presentR3 = rf.format(cr.getKwhR3());

                int cycleInt = Integer.parseInt(billCycle.trim());
                List<NcreInvRdngs> prevReadings = ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed(
                        accNbr.trim(), areaCd.trim(), String.valueOf(cycleInt - 1));
                if (prevReadings != null && !prevReadings.isEmpty()) {
                    NcreInvRdngs pr = null;
                    if (cr.getMtrNbr() != null) {
                        pr = prevReadings.stream()
                                .filter(p -> p.getMtrNbr() != null && p.getMtrNbr().trim().equalsIgnoreCase(cr.getMtrNbr().trim()))
                                .findFirst().orElse(prevReadings.get(0));
                    } else {
                        pr = prevReadings.get(0);
                    }
                    if (pr != null) {
                        if (pr.getKwhR1() != null) ir.previousR1 = rf.format(pr.getKwhR1());
                        if (pr.getKwhR2() != null) ir.previousR2 = rf.format(pr.getKwhR2());
                        if (pr.getKwhR3() != null) ir.previousR3 = rf.format(pr.getKwhR3());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not resolve interval readings: " + e.getMessage());
        }
        return ir;
    }

    private IntervalReadings resolveIntervalReadings(String accNbr, String areaCd, Integer billCycle) {
        return resolveIntervalReadings(accNbr, areaCd, billCycle != null ? String.valueOf(billCycle) : null);
    }

    private BigDecimal parseInitialTariff(String initialTariff) {
        if (initialTariff == null || initialTariff.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(initialTariff.trim());
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse initial_tariff '" + initialTariff + "' as number. Defaulting to 0.");
            return BigDecimal.ZERO;
        }
    }

    private String extractReadingDate(List<TmpReadings> readings, boolean present) {
        return readings.stream()
                .filter(r -> present ? r.getRdngDate() != null : r.getPrvDate() != null)
                .map(r -> present ? r.getRdngDate() : r.getPrvDate())
                .findFirst()
                .map(this::formatDateToIso)
                .orElse("");
    }

    private String formatDateToIso(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String buildRegionSrNo(NcreDeveloper developer) {
        String region = trimOrDefault(developer.getRegion(), "");
        String srNo = trimOrDefault(developer.getSrNo(), "");
        if (region.isEmpty() && srNo.isEmpty()) return "";
        return region + " - " + srNo;
    }

    private String buildFolioMonthLabel(NcreDeveloper developer, YearMonth invoiceMonth) {
        String folio = developer.getFolioNo() != null ? String.valueOf(developer.getFolioNo()) : "";
        String monthLabel = invoiceMonth.getYear() + "-" + invoiceMonth.getMonthValue();
        if (folio.isEmpty()) return monthLabel;
        return folio + "  " + monthLabel;
    }

    private String formatCapacity(BigDecimal capacity) {
        if (capacity == null) return "0.000";
        return capacity.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private String trimOrDefault(String value, String defaultValue) {
        return value != null ? value.trim() : defaultValue;
    }

    /**
     * Resolves YearMonth from the active bill cycle in dbadmin.ncre_bill_cycle where is_current = 1.
     * Extracts the bill_month (1..12) and bill_year columns.
     */
    public Optional<YearMonth> resolveActiveBillCycleMonth() {
        // Strategy 1: Direct SQL query via JdbcTemplate from dbadmin.ncre_bill_cycle (is_current = 1)
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
                if (rows.isEmpty()) {
                    rows = jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC");
                }
                if (!rows.isEmpty()) {
                    Map<String, Object> row = rows.get(0);
                    Integer month = parseMonthValue(row.get("bill_month") != null ? row.get("bill_month") : row.get("BILL_MONTH"));
                    Integer year = parseYearValue(row.get("bill_year") != null ? row.get("bill_year") : row.get("BILL_YEAR"));
                    if (month != null) {
                        int finalYear = (year != null) ? year : LocalDate.now().getYear();
                        System.out.println("[InvoiceService] Successfully resolved active bill cycle from ncre_bill_cycle via JdbcTemplate: year=" + finalYear + ", month=" + month);
                        return Optional.of(YearMonth.of(finalYear, month));
                    }
                }
            } catch (Exception e) {
                System.err.println("[InvoiceService] Warning: Failed to query active bill cycle via JdbcTemplate: " + e.getMessage());
            }
        }

        // Strategy 2: BillCycleService.getCurrentOpenBillCycle()
        if (billCycleService != null) {
            try {
                Optional<BillCycleDTO.CurrentBillCycleDTO> cycleOpt = billCycleService.getCurrentOpenBillCycle();
                if (cycleOpt.isPresent()) {
                    BillCycleDTO.CurrentBillCycleDTO cycle = cycleOpt.get();
                    Integer month = cycle.getBillMonth();
                    Integer year = cycle.getBillYear();
                    if (month != null && month >= 1 && month <= 12) {
                        int finalYear = (year != null && year >= 1900 && year <= 2100) ? year : LocalDate.now().getYear();
                        System.out.println("[InvoiceService] Successfully resolved active bill cycle from ncre_bill_cycle via BillCycleService: year=" + finalYear + ", month=" + month);
                        return Optional.of(YearMonth.of(finalYear, month));
                    }
                }
            } catch (Exception e) {
                System.err.println("[InvoiceService] Warning: Failed to query active bill cycle via BillCycleService: " + e.getMessage());
            }
        }

        // Strategy 3: NcreBillCycleRepository.findCurrentBillCycle()
        if (ncreBillCycleRepository != null) {
            try {
                Optional<NcreBillCycle> bcOpt = ncreBillCycleRepository.findCurrentBillCycle();
                if (!bcOpt.isPresent()) {
                    List<NcreBillCycle> cycles = ncreBillCycleRepository.findCurrentBillCycles();
                    if (cycles != null && !cycles.isEmpty()) {
                        bcOpt = Optional.of(cycles.get(0));
                    }
                }
                if (bcOpt.isPresent()) {
                    NcreBillCycle bc = bcOpt.get();
                    if (bc.getBillMonth() != null) {
                        int month = bc.getBillMonth().intValue();
                        int year = bc.getBillYear() != null ? bc.getBillYear().intValue() : LocalDate.now().getYear();
                        if (month >= 1 && month <= 12) {
                            System.out.println("[InvoiceService] Successfully resolved active bill cycle from ncre_bill_cycle via NcreBillCycleRepository: year=" + year + ", month=" + month);
                            return Optional.of(YearMonth.of(year, month));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[InvoiceService] Warning: Failed to query active bill cycle via NcreBillCycleRepository: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    private Integer parseMonthValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            int val = ((Number) obj).intValue();
            if (val >= 1 && val <= 12) return val;
        }
        String str = obj.toString().trim();
        if (str.isEmpty()) return null;
        try {
            int val = Integer.parseInt(str);
            if (val >= 1 && val <= 12) return val;
        } catch (NumberFormatException ignored) {}

        return parseMonthToken(str);
    }

    private Integer parseYearValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            int val = ((Number) obj).intValue();
            if (val >= 1900 && val <= 2100) return val;
        }
        String str = obj.toString().trim();
        if (str.isEmpty()) return null;
        try {
            int val = Integer.parseInt(str);
            if (val >= 1900 && val <= 2100) return val;
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public YearMonth resolveInvoiceMonth(Integer billCycle) {
        // 1. PRIMARY: Resolve active bill cycle month from dbadmin.ncre_bill_cycle (where is_current = 1)
        Optional<YearMonth> activeCycleMonth = resolveActiveBillCycleMonth();
        if (activeCycleMonth.isPresent()) {
            return activeCycleMonth.get();
        }

        // 2. Fallback: If billCycle provided, try resolving from ncre_bill_cycle by id
        if (billCycle != null) {
            if (jdbcTemplate != null) {
                try {
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                            "SELECT bill_month, bill_year FROM dbadmin.ncre_bill_cycle WHERE bill_cycle = ?", billCycle);
                    if (rows.isEmpty()) {
                        rows = jdbcTemplate.queryForList(
                                "SELECT bill_month, bill_year FROM ncre_bill_cycle WHERE bill_cycle = ?", billCycle);
                    }
                    if (!rows.isEmpty()) {
                        Integer m = parseMonthValue(rows.get(0).get("bill_month") != null ? rows.get(0).get("bill_month") : rows.get(0).get("BILL_MONTH"));
                        Integer y = parseYearValue(rows.get(0).get("bill_year") != null ? rows.get(0).get("bill_year") : rows.get(0).get("BILL_YEAR"));
                        if (m != null) {
                            int finalYear = (y != null) ? y : LocalDate.now().getYear();
                            return YearMonth.of(finalYear, m);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (ncreBillCycleRepository != null && billCycle <= Short.MAX_VALUE && billCycle >= 0) {
                try {
                    Optional<NcreBillCycle> bcOpt = ncreBillCycleRepository.findById(billCycle.shortValue());
                    if (bcOpt.isPresent()) {
                        NcreBillCycle bc = bcOpt.get();
                        if (bc.getBillMonth() != null) {
                            int month = bc.getBillMonth().intValue();
                            int year = bc.getBillYear() != null ? bc.getBillYear().intValue() : LocalDate.now().getYear();
                            if (month >= 1 && month <= 12) {
                                return YearMonth.of(year, month);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[InvoiceService] Warning: Failed to resolve bill cycle from ncre_bill_cycle: " + e.getMessage());
                }
            }

            // 3. Fallback: Try resolving from yr_mnth table
            try {
                if (yrMnthRepository != null) {
                    Optional<YrMnth> yrMnthOpt = yrMnthRepository.findById(billCycle);
                    if (yrMnthOpt.isPresent() && yrMnthOpt.get().getBillMnth() != null) {
                        YearMonth ym = parseYearMonthString(yrMnthOpt.get().getBillMnth());
                        if (ym != null) {
                            return ym;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[InvoiceService] Warning: Failed to resolve bill cycle from yr_mnth: " + e.getMessage());
            }

            // 4. Fallback: Try interpreting billCycle as YYYYMM (e.g. 202608 or 202609)
            if (billCycle >= 190001 && billCycle <= 209912) {
                int year = billCycle / 100;
                int month = billCycle % 100;
                if (month >= 1 && month <= 12) {
                    return YearMonth.of(year, month);
                }
            }
        }

        // 5. Ultimate Fallback: Calculation service default (bill created month - 1)
        if (invoiceCalculationService != null) {
            YearMonth fallback = invoiceCalculationService.determineInvoiceMonth();
            if (fallback != null) {
                return fallback;
            }
        }
        return YearMonth.now().minusMonths(1);
    }

    private YearMonth parseYearMonthString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String cleaned = text.trim();
        try {
            java.util.regex.Matcher yearMatcher = java.util.regex.Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b").matcher(cleaned);
            if (yearMatcher.find()) {
                int year = Integer.parseInt(yearMatcher.group(1));
                String remainder = cleaned.substring(0, yearMatcher.start()) + cleaned.substring(yearMatcher.end());
                remainder = remainder.replaceAll("[^a-zA-Z0-9]", " ").trim();
                if (!remainder.isEmpty()) {
                    String[] tokens = remainder.split("\\s+");
                    for (String token : tokens) {
                        Integer m = parseMonthToken(token);
                        if (m != null) {
                            return YearMonth.of(year, m);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Integer parseMonthToken(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            int num = Integer.parseInt(token);
            if (num >= 1 && num <= 12) return num;
        } catch (NumberFormatException ignored) {}

        String lower = token.toLowerCase(Locale.ENGLISH);
        for (java.time.Month m : java.time.Month.values()) {
            if (m.name().toLowerCase(Locale.ENGLISH).startsWith(lower) ||
                    lower.startsWith(m.name().toLowerCase(Locale.ENGLISH).substring(0, 3))) {
                return m.getValue();
            }
        }
        return null;
    }

    public ValidationResultDTO validateInvoice(InvoiceGenerationRequestDTO request) {
        System.out.println("[InvoiceService] validateInvoice - START for account: " + request.getAccountNumber());
        try {
            System.out.println("[InvoiceService] validateInvoice - step 1: validating session and access");
            validateSessionAndAccess(request);
            
            System.out.println("[InvoiceService] validateInvoice - step 2: preparing invoice preview data");
            InvoicePreviewDTO previewData = prepareInvoiceData(request);
            
            System.out.println("[InvoiceService] validateInvoice - step 3: delegating to invoiceValidationService.validate");
            ValidationResultDTO result = invoiceValidationService.validate(previewData);
            
            System.out.println("[InvoiceService] validateInvoice - END. Result: valid=" + result.isValid() + ", errorsCount=" + (result.getErrors() != null ? result.getErrors().size() : 0));
            return result;
        } catch (Exception e) {
            System.err.println("[InvoiceService] ERROR during validateInvoice: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Save/overwrite the invoice as DRAFT state.
     */
    @Transactional
    public Invoice saveDraftInvoice(InvoiceGenerationRequestDTO request) {
        validateSessionAndAccess(request);
        InvoicePreviewDTO previewData = prepareInvoiceData(request);

        String accNbr = request.getAccountNumber().trim();
        String areaCode = request.getAreaCode();
        Integer billCycle = request.getBillCycle();
        String username = request.getUserId();

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByAccountNumberAndAreaCodeAndBillCycle(accNbr, areaCode, billCycle);
        Invoice invoice;
        InvoiceStatus oldStatus = null;

        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            oldStatus = invoice.getStatus();
            if (oldStatus == InvoiceStatus.RECOMMEND || oldStatus == InvoiceStatus.APPROVE || oldStatus == InvoiceStatus.FINALIZE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot save draft. An active invoice with status '" + getStatusDisplayName(oldStatus) + "' already exists.");
            }
        } else {
            invoice = new Invoice();
            invoice.setInvoiceNumber("INV-"
                    + areaCode.toUpperCase(Locale.ENGLISH)
                    + "-"
                    + accNbr.toUpperCase(Locale.ENGLISH)
                    + "-"
                    + String.format("%03d", billCycle)
                    + "-"
                    + LocalDateTime.now().format(INVOICE_NUMBER_FORMAT));
        }

        NcreDeveloper developer = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No NCRE developer found for account number " + accNbr));

        mapPreviewToInvoice(previewData, developer, invoice);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setPreparedBy(username);
        invoice.setPreparedAt(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);
        saveTariffChargeLines(savedInvoice.getId(), previewData.getTariffChargeLines());

        Short maxIdDraft = invoiceStatusHistoryRepository.findMaxId();
        short nextIdDraft = (short) (maxIdDraft == null ? 1 : maxIdDraft + 1);

        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                .id(nextIdDraft)
                .invoiceId((short) savedInvoice.getId().longValue())
                .statusFrom(oldStatus != null ? oldStatus.name() : "NONE")
                .statusTo(InvoiceStatus.DRAFT.name())
                .changedBy(username)
                .changedAt(LocalDateTime.now())
                .remarks("Saved as DRAFT")
                .build();
        invoiceStatusHistoryRepository.save(history);

        return savedInvoice;
    }

    /**
     * Submit/overwrite the invoice to RECOMMEND state (only if validation passes).
     */
    @Transactional
    public Invoice submitInvoice(InvoiceGenerationRequestDTO request) {
        validateSessionAndAccess(request);
        InvoicePreviewDTO previewData = prepareInvoiceData(request);

        // Run validation first!
        ValidationResultDTO validationResult = invoiceValidationService.validate(previewData);
        if (!validationResult.isValid()) {
            boolean hasNonBypassableErrors = validationResult.getErrors().stream()
                    .anyMatch(err -> !err.startsWith("RU Limit Exceeded"));
            
            if (Boolean.TRUE.equals(request.getBypassRu()) && !hasNonBypassableErrors) {
                System.out.println("[InvoiceService] submitInvoice - RU Limit Exceeded error bypassed by user request.");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Validation failed: " + String.join("; ", validationResult.getErrors()));
            }
        }

        String accNbr = request.getAccountNumber().trim();
        String areaCode = request.getAreaCode();
        Integer billCycle = request.getBillCycle();
        String username = request.getUserId();

        Optional<Invoice> existingInvoiceOpt = invoiceRepository.findByAccountNumberAndAreaCodeAndBillCycle(accNbr, areaCode, billCycle);
        Invoice invoice;
        InvoiceStatus oldStatus = null;

        if (existingInvoiceOpt.isPresent()) {
            invoice = existingInvoiceOpt.get();
            oldStatus = invoice.getStatus();
            if (oldStatus == InvoiceStatus.RECOMMEND || oldStatus == InvoiceStatus.APPROVE || oldStatus == InvoiceStatus.FINALIZE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot submit. An active invoice with status '" + getStatusDisplayName(oldStatus) + "' already exists.");
            }
        } else {
            invoice = new Invoice();
            invoice.setInvoiceNumber("INV-"
                    + areaCode.toUpperCase(Locale.ENGLISH)
                    + "-"
                    + accNbr.toUpperCase(Locale.ENGLISH)
                    + "-"
                    + String.format("%03d", billCycle)
                    + "-"
                    + LocalDateTime.now().format(INVOICE_NUMBER_FORMAT));
        }

        NcreDeveloper developer = ncreDeveloperRepository.findByAccNbrTrimmed(accNbr)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No NCRE developer found for account number " + accNbr));

        mapPreviewToInvoice(previewData, developer, invoice);
        invoice.setStatus(InvoiceStatus.RECOMMEND);
        invoice.setPreparedBy(username);
        invoice.setPreparedAt(LocalDateTime.now());

        Invoice savedInvoice = invoiceRepository.save(invoice);
        saveTariffChargeLines(savedInvoice.getId(), previewData.getTariffChargeLines());

        Short maxIdSubmit = invoiceStatusHistoryRepository.findMaxId();
        short nextIdSubmit = (short) (maxIdSubmit == null ? 1 : maxIdSubmit + 1);

        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                .id(nextIdSubmit)
                .invoiceId((short) savedInvoice.getId().longValue())
                .statusFrom(oldStatus != null ? oldStatus.name() : "NONE")
                .statusTo(InvoiceStatus.RECOMMEND.name())
                .changedBy(username)
                .changedAt(LocalDateTime.now())
                .remarks("Submitted for review")
                .build();
        invoiceStatusHistoryRepository.save(history);

        return savedInvoice;
    }

    private void saveTariffChargeLines(Long invoiceId, List<InvoicePreviewDTO.TariffChargeLineDTO> lineDtos) {
        if (invoiceId == null) return;
        invoiceTariffChargeLineRepository.deleteByInvoiceId(invoiceId);
        if (lineDtos == null || lineDtos.isEmpty()) {
            return;
        }
        List<InvoiceTariffChargeLine> entities = new ArrayList<>();
        int seq = 1;
        for (InvoicePreviewDTO.TariffChargeLineDTO dto : lineDtos) {
            entities.add(InvoiceTariffChargeLine.builder()
                    .invoiceId(invoiceId)
                    .lineSequence(seq++)
                    .label(dto.getLabel())
                    .periodStart(dto.getPeriodStart() != null && !dto.getPeriodStart().isEmpty() ? LocalDate.parse(dto.getPeriodStart()) : null)
                    .periodEnd(dto.getPeriodEnd() != null && !dto.getPeriodEnd().isEmpty() ? LocalDate.parse(dto.getPeriodEnd()) : null)
                    .numberOfDays(dto.getNumberOfDays())
                    .energyKwh(dto.getEnergyKwh() != null ? new BigDecimal(dto.getEnergyKwh().replace(",", "")) : null)
                    .ratePerKwh(dto.getRatePerKwh() != null ? new BigDecimal(dto.getRatePerKwh().replace(",", "")) : null)
                    .costOfEnergy(dto.getCostOfEnergy() != null ? new BigDecimal(dto.getCostOfEnergy().replace(",", "")) : null)
                    .rateType(dto.getRateType())
                    .build());
        }
        invoiceTariffChargeLineRepository.saveAll(entities);
    }

    private List<InvoicePreviewDTO.TariffChargeLineDTO> loadTariffChargeLineDTOs(Long invoiceId) {
        if (invoiceId == null) return Collections.emptyList();
        List<InvoiceTariffChargeLine> lines = invoiceTariffChargeLineRepository.findByInvoiceIdOrderByLineSequenceAsc(invoiceId);
        if (lines == null || lines.isEmpty()) return Collections.emptyList();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
        List<InvoicePreviewDTO.TariffChargeLineDTO> dtos = new ArrayList<>();

        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        String previousReadingDate = invoiceOpt.map(inv -> inv.getPreviousReadingDate() != null ? inv.getPreviousReadingDate().toString() : "").orElse("");
        String presentReadingDate = invoiceOpt.map(inv -> inv.getPresentReadingDate() != null ? inv.getPresentReadingDate().toString() : "").orElse("");

        InvoiceTariffChargeLine line1 = lines.size() > 0 ? lines.get(0) : null;
        InvoiceTariffChargeLine line2 = lines.size() > 1 ? lines.get(1) : null;
        String changeDateStr = line2 != null && line2.getPeriodStart() != null ? line2.getPeriodStart().toString() : "";

        for (int i = 0; i < lines.size(); i++) {
            InvoiceTariffChargeLine line = lines.get(i);
            String dateRange = "";
            String remarks = "";

            if (lines.size() >= 2) {
                if (i == 0) {
                    String startStr = !previousReadingDate.isEmpty()
                            ? previousReadingDate
                            : (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "");
                    String endStr = !changeDateStr.isEmpty()
                            ? changeDateStr
                            : (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
                    dateRange = startStr + " to " + endStr;
                    remarks = "";
                } else if (i == 1) {
                    String startStr = !changeDateStr.isEmpty()
                            ? changeDateStr
                            : (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "");
                    String endStr = !presentReadingDate.isEmpty()
                            ? presentReadingDate
                            : (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
                    dateRange = startStr + " to " + endStr;
                    String tariffType = "";
                    if (invoiceOpt.isPresent() && invoiceOpt.get().getAccountNumber() != null) {
                        Optional<NcreDeveloper> devOpt = ncreDeveloperRepository.findByAccNbrTrimmed(invoiceOpt.get().getAccountNumber());
                        if (devOpt.isPresent() && devOpt.get().getTariffType() != null) {
                            tariffType = devOpt.get().getTariffType().trim();
                        }
                    }
                    if (tariffType.contains("2022") || tariffType.contains("New")) {
                        remarks = "Tariff Change 15+ New";
                    } else if (tariffType.contains("TTT5") || tariffType.contains("5 years") || tariffType.contains("5 Years")) {
                        remarks = "Tariff Change TTT 5 Years";
                    } else if (tariffType.contains("TTT3") || tariffType.contains("3 years") || tariffType.contains("3 Years") || tariffType.contains("3 yrs") || tariffType.contains("3YRS") || tariffType.contains("3 YRS")) {
                        remarks = "Tariff Change TTT 3 Years";
                    } else if ("AC".equalsIgnoreCase(tariffType) || tariffType.contains("Avoided Cost")) {
                        remarks = "Tariff Change Avoided Cost";
                    } else if ("FLAT_VT".equalsIgnoreCase(tariffType) || "FLAT (V)".equalsIgnoreCase(tariffType) || tariffType.toUpperCase().contains("FLAT VARIABLE") || tariffType.toUpperCase().contains("FLAT  VARIABLE")) {
                        remarks = "Tariff Change Flat Variable Tariff";
                    } else if ("VT".equalsIgnoreCase(tariffType) || tariffType.equalsIgnoreCase("Variable Tariff") || tariffType.equalsIgnoreCase("Variable Traiff")) {
                        remarks = "Tariff Change Variable Tariff";
                    } else {
                        remarks = "Tariff Change 15+";
                    }
                }
            } else {
                dateRange = (line.getPeriodStart() != null ? line.getPeriodStart().toString() : "")
                        + " to " + (line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "");
            }

            dtos.add(InvoicePreviewDTO.TariffChargeLineDTO.builder()
                    .label(line.getLabel())
                    .periodStart(line.getPeriodStart() != null ? line.getPeriodStart().toString() : "")
                    .periodEnd(line.getPeriodEnd() != null ? line.getPeriodEnd().toString() : "")
                    .dateRange(dateRange)
                    .numberOfDays(line.getNumberOfDays())
                    .energyKwh(line.getEnergyKwh() != null ? decimalFormat.format(line.getEnergyKwh()) : "0.0")
                    .ratePerKwh(line.getRatePerKwh() != null ? line.getRatePerKwh().toPlainString() : "0.00")
                    .costOfEnergy(line.getCostOfEnergy() != null ? moneyFormat.format(line.getCostOfEnergy()) : "0.00")
                    .rateType(line.getRateType())
                    .remarks(remarks)
                    .build());
        }
        return dtos;
    }

    /**
     * Resolves the generation losses rate:
     * 1. First priority: from ncre_agreements table (generation_losses column of latest agreement).
     * 2. Fallback: from ncre_developers table (generation_losses column) if agreement or its generation_losses is missing/null.
     * 3. Default: BigDecimal.ZERO if neither source defines generation losses.
     */
    private BigDecimal resolveGenerationLosses(Agreement latestAgreement, NcreDeveloper developer) {
        if (latestAgreement != null && latestAgreement.getGenerationLosses() != null) {
            System.out.println("[InvoiceService] Using generation losses from ncre_agreements: " + latestAgreement.getGenerationLosses());
            return latestAgreement.getGenerationLosses();
        }
        if (developer != null && developer.getGenerationLosses() != null) {
            System.out.println("[InvoiceService] Generation losses not found in ncre_agreements, falling back to ncre_developers: " + developer.getGenerationLosses());
            return developer.getGenerationLosses();
        }
        System.out.println("[InvoiceService] No generation losses in ncre_agreements or ncre_developers; defaulting to 0.00");
        return BigDecimal.ZERO;
    }

    private String formatLossPercent(BigDecimal loss) {
        if (loss == null || loss.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        BigDecimal pct = (loss.compareTo(BigDecimal.ONE) < 0 && loss.compareTo(BigDecimal.ZERO) > 0)
                ? loss.multiply(BigDecimal.valueOf(100))
                : loss;
        return pct.stripTrailingZeros().toPlainString();
    }

    private String buildEligibleEnergyLabel(BigDecimal loss) {
        String pct = formatLossPercent(loss);
        return "Energy after reduction of " + pct + "% Energy losses";
    }

    private BigDecimal resolveGenerationLossesForInvoice(Invoice invoice) {
        if (invoice == null) return BigDecimal.ZERO;
        Short folio = invoice.getFolioNo() != null ? invoice.getFolioNo().shortValue() : null;
        NcreDeveloper developer = null;
        if (folio != null) {
            developer = ncreDeveloperRepository.findByFolioNo(folio).orElse(null);
        }
        if (developer == null && invoice.getAccountNumber() != null) {
            developer = ncreDeveloperRepository.findByAccNbrTrimmed(invoice.getAccountNumber()).orElse(null);
        }
        Agreement latestAgreement = null;
        if (developer != null && developer.getFolioNo() != null) {
            List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(developer.getFolioNo());
            latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;
        } else if (folio != null) {
            List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(folio);
            latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;
        }
        return resolveGenerationLosses(latestAgreement, developer);
    }

    private void mapPreviewToInvoice(InvoicePreviewDTO previewData, NcreDeveloper developer, Invoice invoice) {
        invoice.setAccountNumber(previewData.getAccountNumber());
        invoice.setAreaCode(previewData.getAreaCode());
        invoice.setBillCycle(previewData.getBillCycle());
        invoice.setInvoiceMonth(previewData.getInvoiceMonth());
        invoice.setIssueDate(LocalDate.now());

        invoice.setRegion(trimOrDefault(developer.getRegion(), ""));
        invoice.setFolioNo(developer.getFolioNo() != null ? developer.getFolioNo().intValue() : null);
        invoice.setSrNo(trimOrDefault(developer.getSrNo(), ""));

        invoice.setCompanyName(previewData.getCompanyName());
        invoice.setProjectName(previewData.getProjectName());
        invoice.setFileRefNo(previewData.getRefNo());
        invoice.setReferenceCode(previewData.getRefCode());

        invoice.setCapacityMw(parseFormattedNumber(previewData.getCapacityMw()));
        invoice.setAllowedGenerationMw(parseFormattedNumber(previewData.getAllowedGenerationMw()));

        if (previewData.getPresentReadingDate() != null && !previewData.getPresentReadingDate().isEmpty()) {
            invoice.setPresentReadingDate(LocalDate.parse(previewData.getPresentReadingDate()));
        }
        if (previewData.getPreviousReadingDate() != null && !previewData.getPreviousReadingDate().isEmpty()) {
            invoice.setPreviousReadingDate(LocalDate.parse(previewData.getPreviousReadingDate()));
        }

        invoice.setTotalPresentReading(parseFormattedInteger(previewData.getTotalPresentReading()));
        invoice.setTotalPreviousReading(parseFormattedInteger(previewData.getTotalPreviousReading()));
        invoice.setMultiplyFactor(parseFormattedNumber(previewData.getMultiplyFactor()));

        BigDecimal energyKwh = parseFormattedNumber(previewData.getEnergyKwh());
        invoice.setEnergyKwh(energyKwh);

        BigDecimal r1 = parseFormattedNumber(previewData.getEngSendR1());
        BigDecimal r2 = parseFormattedNumber(previewData.getEngSendR2());
        BigDecimal r3 = parseFormattedNumber(previewData.getEngSendR3());
        if ((r1 == null || r1.compareTo(BigDecimal.ZERO) == 0) && previewData.getPresentR1() != null) {
            r1 = invoiceCalculationService.calculateIntervalEnergy(
                    parseFormattedNumber(previewData.getPresentR1()),
                    parseFormattedNumber(previewData.getPreviousR1()));
        }
        if ((r2 == null || r2.compareTo(BigDecimal.ZERO) == 0) && previewData.getPresentR2() != null) {
            r2 = invoiceCalculationService.calculateIntervalEnergy(
                    parseFormattedNumber(previewData.getPresentR2()),
                    parseFormattedNumber(previewData.getPreviousR2()));
        }
        if ((r3 == null || r3.compareTo(BigDecimal.ZERO) == 0) && previewData.getPresentR3() != null) {
            r3 = invoiceCalculationService.calculateIntervalEnergy(
                    parseFormattedNumber(previewData.getPresentR3()),
                    parseFormattedNumber(previewData.getPreviousR3()));
        }
        invoice.setEngSendR1(r1 != null ? r1.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        invoice.setEngSendR2(r2 != null ? r2.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        invoice.setEngSendR3(r3 != null ? r3.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        invoice.setEligibleEnergyKwh(parseFormattedNumber(previewData.getEligibleEnergyKwh()));
        invoice.setPeriodOfGenerationDays(parseFormattedInteger(previewData.getPeriodOfGeneration()));
        invoice.setPlantFactorPercent(parseFormattedNumber(previewData.getPlantFactorPercent()));
        invoice.setEnergyPurchasedKwh(parseFormattedNumber(previewData.getEnergyPurchasedKwh()));
        invoice.setRatePerKwh(parseFormattedNumber(previewData.getRatePerKwh()));
        
        BigDecimal costOfEnergy = parseFormattedNumber(previewData.getCostOfEnergy());
        invoice.setCostOfEnergy(costOfEnergy);

        BigDecimal loyaltyVal = BigDecimal.ZERO;
        BigDecimal escrowVal = BigDecimal.ZERO;
        BigDecimal mahaweliVal = BigDecimal.ZERO;
        BigDecimal treasuryVal = BigDecimal.ZERO;
        BigDecimal ded1PercVal = BigDecimal.ZERO;
        BigDecimal developerPymntVal = costOfEnergy;

        List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(developer.getFolioNo());
        Agreement latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;

        if (latestAgreement != null && latestAgreement.getPaymentDeductions() != null && !latestAgreement.getPaymentDeductions().isEmpty()) {
            List<PaymentDeduction> agreementDeductions = latestAgreement.getPaymentDeductions();
            
            BigDecimal loyaltyAmount = BigDecimal.ZERO;
            BigDecimal baseForOtherDeductions = costOfEnergy;

            // Search for Loyalty deduction
            Optional<PaymentDeduction> loyaltyOpt = agreementDeductions.stream()
                    .filter(d -> isLoyaltyDeduction(d.getDeductionType()))
                    .findFirst();

            if (loyaltyOpt.isPresent()) {
                PaymentDeduction loyalty = loyaltyOpt.get();
                BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                loyaltyAmount = costOfEnergy.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                baseForOtherDeductions = costOfEnergy.subtract(loyaltyAmount);
                loyaltyVal = loyaltyAmount;
                developerPymntVal = developerPymntVal.subtract(loyaltyAmount);
            }

            for (PaymentDeduction d : agreementDeductions) {
                String type = d.getDeductionType();
                if (!isLoyaltyDeduction(type)) {
                    BigDecimal pct = d.getPercentage() != null ? d.getPercentage() : BigDecimal.ZERO;
                    BigDecimal amount = baseForOtherDeductions.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    developerPymntVal = developerPymntVal.subtract(amount);
                    
                    if (isEscrowDeduction(type)) {
                        escrowVal = amount;
                    } else if (isMahaweliDeduction(type)) {
                        mahaweliVal = amount;
                    } else if (isTreasuryDeduction(type)) {
                        treasuryVal = amount;
                    } else if (isDed1PercDeduction(type)) {
                        ded1PercVal = amount;
                    }
                }
            }
        }

        invoice.setLoyalty(loyaltyVal);
        invoice.setEscrow(escrowVal);
        invoice.setMahaweli(mahaweliVal);
        invoice.setTreasury(treasuryVal);
        invoice.setDed1Perc(ded1PercVal);
        invoice.setDeveloperPymnt(developerPymntVal);

        // Generation loss calculations (from latest agreement if available, fallback to ncre_developers):
        BigDecimal genLossVal = resolveGenerationLosses(latestAgreement, developer);
        BigDecimal lossFraction = BigDecimal.ZERO;
        if (genLossVal != null && genLossVal.compareTo(BigDecimal.ZERO) > 0) {
            lossFraction = genLossVal.compareTo(BigDecimal.ONE) >= 0
                    ? genLossVal.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    : genLossVal;
        }
        BigDecimal energyAfterGenLoss = energyKwh.multiply(BigDecimal.ONE.subtract(lossFraction)).setScale(2, RoundingMode.HALF_UP);
        invoice.setEnergyAfterGenLoss(energyAfterGenLoss);
    }

    private BigDecimal parseFormattedNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            Number number = format.parse(text.trim());
            return new BigDecimal(number.toString());
        } catch (Exception e) {
            try {
                return new BigDecimal(text.replace(",", "").trim());
            } catch (Exception ex) {
                return BigDecimal.ZERO;
            }
        }
    }

    private Integer parseFormattedInteger(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            Number number = format.parse(text.trim());
            return number.intValue();
        } catch (Exception e) {
            try {
                return Integer.parseInt(text.replace(",", "").trim());
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    public List<Invoice> getAllInvoices() {
        return getAllInvoices(null, null, null, null, null);
    }

    public List<Invoice> getAllInvoices(String eeUserId) {
        return getAllInvoices(eeUserId, null, null, null, null);
    }

    public List<Invoice> getAllInvoices(String userId, String sessionId, String userCategory) {
        return getAllInvoices(userId, sessionId, userCategory, null, null);
    }

    public List<Invoice> getAllInvoices(String invoiceMonth, String status) {
        return getAllInvoices(null, null, null, invoiceMonth, status);
    }

    public List<Invoice> getAllInvoices(String userId, String sessionId, String userCategory, String invoiceMonth, String status) {
        System.out.println("[InvoiceService] getAllInvoices - Querying invoices from repository (userId=" + userId 
                + ", category=" + userCategory + ", month=" + invoiceMonth + ", status=" + status + ")");

        // 1. Resolve target month
        String targetMonth = (invoiceMonth != null && !invoiceMonth.trim().isEmpty()) ? invoiceMonth.trim() : null;
        if (targetMonth == null) {
            YearMonth activeYm = resolveActiveBillCycleMonth().orElseGet(() -> resolveInvoiceMonth(null));
            targetMonth = activeYm.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + activeYm.getYear();
            System.out.println("[InvoiceService] getAllInvoices - Defaulting to active bill cycle month: " + targetMonth);
        }

        // 2. Resolve target status
        InvoiceStatus invoiceStatus = null;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            try {
                invoiceStatus = InvoiceStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("[InvoiceService] Warning: Unknown status filter: " + status);
            }
        }

        // 3. Query repository directly with database-level filtering
        List<Invoice> list;
        if (invoiceStatus != null) {
            if (!"ALL".equalsIgnoreCase(targetMonth)) {
                list = invoiceRepository.findByInvoiceMonthAndStatus(targetMonth, invoiceStatus);
            } else {
                list = invoiceRepository.findByStatus(invoiceStatus);
            }
        } else {
            if (!"ALL".equalsIgnoreCase(targetMonth)) {
                list = invoiceRepository.findByInvoiceMonth(targetMonth);
            } else {
                list = invoiceRepository.findAll();
            }
        }

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Batch load all developers once into memory maps to avoid N queries and full table scans
        List<NcreDeveloper> allDevs = ncreDeveloperRepository.findAll();
        Map<String, String> devEeByAcc = new HashMap<>();
        Map<Short, String> devEeByFolio = new HashMap<>();
        for (NcreDeveloper dev : allDevs) {
            if (dev.getResponsibleEe() != null && !dev.getResponsibleEe().trim().isEmpty()) {
                if (dev.getAccNbr() != null) {
                    devEeByAcc.put(dev.getAccNbr().trim(), dev.getResponsibleEe().trim());
                }
                if (dev.getFolioNo() != null) {
                    devEeByFolio.put(dev.getFolioNo(), dev.getResponsibleEe().trim());
                }
            }
        }

        // 2. Batch load all status histories for all invoices in a single query
        List<Short> invoiceIds = list.stream()
                .filter(i -> i.getId() != null)
                .map(i -> (short) i.getId().longValue())
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        Map<Short, List<InvoiceStatusHistory>> historyByInvoiceId = new HashMap<>();
        if (!invoiceIds.isEmpty()) {
            List<InvoiceStatusHistory> allHistories = invoiceStatusHistoryRepository
                    .findByInvoiceIdInOrderByChangedAtDesc(invoiceIds);
            if (allHistories != null) {
                historyByInvoiceId = allHistories.stream()
                        .filter(h -> h.getInvoiceId() != null)
                        .collect(java.util.stream.Collectors.groupingBy(InvoiceStatusHistory::getInvoiceId));
            }
        }

        // 3. Resolve responsibleEe and remarks in-memory with O(1) lookups
        for (Invoice invoice : list) {
            String respEe = null;
            if (invoice.getAccountNumber() != null) {
                respEe = devEeByAcc.get(invoice.getAccountNumber().trim());
            }
            if (respEe == null && invoice.getFolioNo() != null) {
                respEe = devEeByFolio.get(invoice.getFolioNo().shortValue());
            }
            if (respEe == null && invoice.getPreparedBy() != null) {
                respEe = invoice.getPreparedBy();
            }
            invoice.setResponsibleEe(respEe);

            if (invoice.getId() != null) {
                List<InvoiceStatusHistory> history = historyByInvoiceId.get((short) invoice.getId().longValue());
                if (history != null && !history.isEmpty()) {
                    String qualifyingRemarks = null;
                    for (InvoiceStatusHistory record : history) {
                        String from = record.getStatusFrom();
                        String to = record.getStatusTo();
                        // Priority 1: Exact match with current invoice status
                        if (invoice.getStatus() != null && invoice.getStatus().name().equalsIgnoreCase(to)) {
                            if (record.getRemarks() != null && !record.getRemarks().trim().isEmpty()) {
                                qualifyingRemarks = record.getRemarks();
                                break;
                            }
                        }
                        // Priority 2: Approval or rejection remarks
                        if ("REJECTED".equalsIgnoreCase(to) ||
                            ("RECOMMEND".equalsIgnoreCase(from) && "APPROVE".equalsIgnoreCase(to)) ||
                            ("APPROVE".equalsIgnoreCase(from) && "FINALIZE".equalsIgnoreCase(to))) {
                            if (qualifyingRemarks == null && record.getRemarks() != null && !record.getRemarks().trim().isEmpty()) {
                                qualifyingRemarks = record.getRemarks();
                            }
                        }
                    }
                    invoice.setRemarks(qualifyingRemarks);
                }
            }
        }

        // Determine user category from session if not directly supplied
        String effectiveCategory = userCategory;
        if ((effectiveCategory == null || effectiveCategory.trim().isEmpty()) && sessionId != null && userId != null) {
            try {
                Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
                if (userInfoOpt.isPresent()) {
                    effectiveCategory = userInfoOpt.get().getUserCategory();
                }
            } catch (Exception e) {
                System.out.println("[InvoiceService] Could not resolve user category from session: " + e.getMessage());
            }
        }

        boolean isReviewerOrAdmin = "Chief Engineer".equalsIgnoreCase(effectiveCategory) ||
                                    "DGM".equalsIgnoreCase(effectiveCategory) ||
                                    "Director".equalsIgnoreCase(effectiveCategory) ||
                                    "Admin".equalsIgnoreCase(effectiveCategory);

        // If user is an Electrical Engineer (or non-reviewer) and userId is provided, filter by assigned EE
        if (!isReviewerOrAdmin && userId != null && !userId.trim().isEmpty()) {
            list = list.stream()
                    .filter(i -> com.example.SPSProjectBackend.util.SessionUtils.matchEE(i.getResponsibleEe(), userId) ||
                                 com.example.SPSProjectBackend.util.SessionUtils.matchEE(i.getPreparedBy(), userId))
                    .collect(java.util.stream.Collectors.toList());
        }

        return list;
    }

    /**
     * Retrieve distinct available finalized invoice months for dropdown selection.
     * Ensures the current active bill cycle month is always included and returns
     * a chronologically sorted list (newest first).
     */
    public List<String> getAvailableFinalizedMonths() {
        Set<String> distinctMonths = new LinkedHashSet<>();

        // 1. Resolve current active bill cycle month and ensure it's always included
        YearMonth activeYm = resolveActiveBillCycleMonth().orElseGet(() -> resolveInvoiceMonth(null));
        String activeMonthStr = activeYm.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + activeYm.getYear();
        distinctMonths.add(activeMonthStr);

        // 2. Query distinct months with FINALIZE status from repository
        try {
            if (invoiceRepository != null) {
                List<String> dbMonths = invoiceRepository.findDistinctInvoiceMonthsByStatus(InvoiceStatus.FINALIZE);
                if (dbMonths != null) {
                    for (String m : dbMonths) {
                        if (m != null && !m.trim().isEmpty()) {
                            distinctMonths.add(m.trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[InvoiceService] Warning: Failed to query distinct finalized invoice months via repository: " + e.getMessage());
        }

        // Also query via JdbcTemplate if available as a safety layer
        if (jdbcTemplate != null) {
            try {
                List<String> nativeMonths = jdbcTemplate.queryForList(
                        "SELECT DISTINCT invoice_month FROM dbadmin.invoices WHERE status = 'FINALIZE' AND invoice_month IS NOT NULL", String.class);
                if (nativeMonths != null) {
                    for (String m : nativeMonths) {
                        if (m != null && !m.trim().isEmpty()) {
                            distinctMonths.add(m.trim());
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 3. Sort chronologically descending (newest first)
        List<String> sortedList = new ArrayList<>(distinctMonths);
        sortedList.sort((a, b) -> {
            YearMonth ymA = parseYearMonthString(a);
            YearMonth ymB = parseYearMonthString(b);
            if (ymA != null && ymB != null) {
                return ymB.compareTo(ymA);
            }
            if (ymA != null) return -1;
            if (ymB != null) return 1;
            return b.compareTo(a);
        });

        return sortedList;
    }

    @Transactional
    public Invoice approveInvoice(InvoiceReviewRequestDTO request) {
        if (!sessionUtils.isSessionValid(request.getSessionId(), request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(request.getSessionId(), request.getUserId());
        if (!userInfoOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User information not found in session");
        }
        String userCategory = userInfoOpt.get().getUserCategory();

        InvoiceStatus oldStatus = invoice.getStatus();
        InvoiceStatus newStatus;
        String defaultRemarks;

        if ("Chief Engineer".equals(userCategory)) {
            if (oldStatus != InvoiceStatus.RECOMMEND) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Chief Engineer can only approve invoices in RECOMMEND status");
            }
            newStatus = InvoiceStatus.APPROVE;
            defaultRemarks = "Approved by Chief Engineer";
        } else if ("DGM".equals(userCategory) || "Director".equalsIgnoreCase(userCategory)) {
            if (oldStatus != InvoiceStatus.APPROVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Director can only approve invoices in APPROVE status");
            }
            newStatus = InvoiceStatus.FINALIZE;
            defaultRemarks = "Approved by Director";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Chief Engineer or Director can approve invoices");
        }

        invoice.setStatus(newStatus);
        invoice.setApprovedBy(request.getUserId());
        invoice.setApprovedAt(LocalDateTime.now());

        if (newStatus == InvoiceStatus.FINALIZE) {
            populateFinalizedInvoiceFields(invoice);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        Short maxIdApprove = invoiceStatusHistoryRepository.findMaxId();
        short nextIdApprove = (short) (maxIdApprove == null ? 1 : maxIdApprove + 1);

        String remarks = request.getRemarks() != null && !request.getRemarks().isEmpty()
                ? request.getRemarks() : defaultRemarks;
        if (remarks.length() > 1000) {
            remarks = remarks.substring(0, 1000);
        }

        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                .id(nextIdApprove)
                .invoiceId((short) savedInvoice.getId().longValue())
                .statusFrom(oldStatus.name())
                .statusTo(newStatus.name())
                .changedBy(request.getUserId())
                .changedAt(LocalDateTime.now())
                .remarks(remarks)
                .build();
        invoiceStatusHistoryRepository.save(history);

        if (newStatus == InvoiceStatus.FINALIZE) {
            Integer billCycle = savedInvoice.getBillCycle();
            Integer folioNo = savedInvoice.getFolioNo();
            if (folioNo == null && savedInvoice.getAccountNumber() != null) {
                Optional<NcreDeveloper> devOpt = ncreDeveloperRepository.findByAccNbrTrimmed(savedInvoice.getAccountNumber());
                if (devOpt.isPresent() && devOpt.get().getFolioNo() != null) {
                    folioNo = devOpt.get().getFolioNo().intValue();
                }
            }

            if (billCycle == null || folioNo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invoice bill cycle and folio number must not be null for finalization");
            }

            NcreInvoiceCreateId createId = new NcreInvoiceCreateId(billCycle, folioNo);
            Optional<NcreInvoiceCreate> existingRecord = ncreInvoiceCreateRepository.findById(createId);

            String createdBy = (savedInvoice.getPreparedBy() != null && !savedInvoice.getPreparedBy().trim().isEmpty())
                    ? savedInvoice.getPreparedBy().trim()
                    : (request.getUserId() != null ? request.getUserId().trim() : "");
            if (createdBy.length() > 12) {
                createdBy = createdBy.substring(0, 12);
            }

            String invoiceCreateRemarks = null;
            if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
                invoiceCreateRemarks = request.getRemarks().trim();
            } else if (savedInvoice.getRemarks() != null && !savedInvoice.getRemarks().trim().isEmpty()) {
                invoiceCreateRemarks = savedInvoice.getRemarks().trim();
            }
            if (invoiceCreateRemarks != null && invoiceCreateRemarks.length() > 100) {
                invoiceCreateRemarks = invoiceCreateRemarks.substring(0, 100);
            }

            LocalDate finalizeDate = history.getChangedAt() != null
                    ? history.getChangedAt().toLocalDate()
                    : (savedInvoice.getApprovedAt() != null ? savedInvoice.getApprovedAt().toLocalDate() : LocalDate.now());
                Date finalizedDate = Date.from(finalizeDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            NcreInvoiceCreate invoiceCreate;
            if (existingRecord.isPresent()) {
                invoiceCreate = existingRecord.get();
                invoiceCreate.setIsCreate(1);
                invoiceCreate.setRemarks(invoiceCreateRemarks);
                if (!createdBy.isEmpty()) {
                    invoiceCreate.setCreatedBy(createdBy);
                }
                invoiceCreate.setCreatedDate(finalizeDate);
                System.out.println("[InvoiceService] Updating existing ncre_invoice_create record for billCycle=" + billCycle + ", folioNo=" + folioNo);
            } else {
                invoiceCreate = NcreInvoiceCreate.builder()
                        .billCycle(billCycle)
                        .folioNo(folioNo)
                        .isCreate(1)
                        .remarks(invoiceCreateRemarks)
                        .createdBy(createdBy)
                    .createdDate(finalizedDate)
                        .build();
                System.out.println("[InvoiceService] Creating new ncre_invoice_create record for billCycle=" + billCycle + ", folioNo=" + folioNo);
            }

            ncreInvoiceCreateRepository.save(invoiceCreate);
            ncreInvoiceCreateRepository.flush();
            System.out.println("[InvoiceService] Successfully recorded ncre_invoice_create for billCycle=" + billCycle + ", folioNo=" + folioNo);

            try {
                self.sendInvoiceDistributionAsync(savedInvoice.getId());
            } catch (Exception e) {
                System.err.println("Failed to trigger async invoice email distribution: " + e.getMessage());
            }
        }

        return savedInvoice;
    }

    @Transactional
    public Invoice rejectInvoice(InvoiceReviewRequestDTO request) {
        if (!sessionUtils.isSessionValid(request.getSessionId(), request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(request.getSessionId(), request.getUserId());
        if (!userInfoOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User information not found in session");
        }
        String userCategory = userInfoOpt.get().getUserCategory();

        InvoiceStatus oldStatus = invoice.getStatus();
        String defaultRemarks;

        if ("Chief Engineer".equals(userCategory)) {
            if (oldStatus != InvoiceStatus.RECOMMEND) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Chief Engineer can only reject invoices in RECOMMEND status");
            }
            defaultRemarks = "Rejected by Chief Engineer";
        } else if ("DGM".equals(userCategory) || "Director".equalsIgnoreCase(userCategory)) {
            if (oldStatus != InvoiceStatus.APPROVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Director can only reject invoices in APPROVE status");
            }
            defaultRemarks = "Rejected by Director";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Chief Engineer or Director can reject invoices");
        }

        invoice.setStatus(InvoiceStatus.REJECTED);
        invoice.setApprovedBy(null);
        invoice.setApprovedAt(null);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        Short maxIdReject = invoiceStatusHistoryRepository.findMaxId();
        short nextIdReject = (short) (maxIdReject == null ? 1 : maxIdReject + 1);

        String remarks = request.getRemarks() != null && !request.getRemarks().isEmpty()
                ? request.getRemarks() : defaultRemarks;
        if (remarks.length() > 1000) {
            remarks = remarks.substring(0, 1000);
        }

        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
                .id(nextIdReject)
                .invoiceId((short) savedInvoice.getId().longValue())
                .statusFrom(oldStatus.name())
                .statusTo(InvoiceStatus.REJECTED.name())
                .changedBy(request.getUserId())
                .changedAt(LocalDateTime.now())
                .remarks(remarks)
                .build();
        invoiceStatusHistoryRepository.save(history);

        return savedInvoice;
    }

    @Transactional
    public Map<String, Object> bulkReviewInvoices(BulkInvoiceReviewRequestDTO request) {
        if (!sessionUtils.isSessionValid(request.getSessionId(), request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
        }

        Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(request.getSessionId(), request.getUserId());
        if (!userInfoOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User information not found in session");
        }
        String userCategory = userInfoOpt.get().getUserCategory();

        // Allow DGM, Director and Chief Engineer to perform bulk approvals/rejections
        if (!"DGM".equalsIgnoreCase(userCategory) && !"Director".equalsIgnoreCase(userCategory) && !"Chief Engineer".equals(userCategory)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Chief Engineer or Director can perform bulk approvals or rejections");
        }

        int count = 0;
        for (Long invId : request.getInvoiceIds()) {
            InvoiceReviewRequestDTO reviewRequest = new InvoiceReviewRequestDTO();
            reviewRequest.setSessionId(request.getSessionId());
            reviewRequest.setUserId(request.getUserId());
            reviewRequest.setInvoiceId(invId);
            reviewRequest.setRemarks(request.getRemarks());

            if ("APPROVE".equalsIgnoreCase(request.getAction())) {
                approveInvoice(reviewRequest);
            } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
                rejectInvoice(reviewRequest);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action: " + request.getAction());
            }
            count++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("processed_count", count);
        return response;
    }

    public static String convertNumberToWords(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) return "";
        try {
            double num = Double.parseDouble(numStr.replace(",", ""));
            long mainPart = (long) num;
            long centsPart = Math.round((num - mainPart) * 100);

            String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

            java.util.function.BiFunction<Long, Boolean, String> convertLessThanThousand = new java.util.function.BiFunction<Long, Boolean, String>() {
                @Override
                public String apply(Long n, Boolean isCent) {
                    if (n == 0) return "";
                    StringBuilder sb = new StringBuilder();
                    if (n >= 100) {
                        sb.append(ones[(int)(n / 100)]).append(" Hundred ");
                        n %= 100;
                    }
                    if (n >= 20) {
                        sb.append(tens[(int)(n / 10)]).append(" ");
                        n %= 10;
                    }
                    if (n > 0) {
                        sb.append(ones[(int)(long)n]).append(" ");
                    }
                    return sb.toString().trim();
                }
            };

            java.util.function.Function<Long, String> convert = new java.util.function.Function<Long, String>() {
                @Override
                public String apply(Long n) {
                    if (n == 0) return "Zero";
                    StringBuilder sb = new StringBuilder();
                    if (n >= 1000000) {
                        sb.append(convertLessThanThousand.apply(n / 1000000, false)).append(" Million ");
                        n %= 1000000;
                    }
                    if (n >= 1000) {
                        sb.append(convertLessThanThousand.apply(n / 1000, false)).append(" Thousand ");
                        n %= 1000;
                    }
                    if (n > 0) {
                        sb.append(convertLessThanThousand.apply(n, false)).append(" ");
                    }
                    return sb.toString().trim();
                }
            };

            String words = convert.apply(mainPart) + " Rupees";
            if (centsPart > 0) {
                words += " and " + convertLessThanThousand.apply(centsPart, true) + " Cents";
            }
            words += " Only";
            return words;
        } catch (Exception e) {
            return "";
        }
    }

    public InvoicePreviewDTO buildPreviewFromInvoice(Invoice invoice, String documentType, List<InvoicePreviewDTO.PaymentDeductionDTO> deductionDTOs, BigDecimal finalAmount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

        String capacityStr = invoice.getCapacityMw() != null ? invoice.getCapacityMw().setScale(3, RoundingMode.HALF_UP).toPlainString() : "0.000";
        String allowedGenStr = invoice.getAllowedGenerationMw() != null ? invoice.getAllowedGenerationMw().setScale(3, RoundingMode.HALF_UP).toPlainString() : "0.000";
        String rateStr = invoice.getRatePerKwh() != null ? invoice.getRatePerKwh().toPlainString() : "0.00";
        String costStr = invoice.getCostOfEnergy() != null ? moneyFormat.format(invoice.getCostOfEnergy()) : "0.00";
        
        String multiplyFactorStr = "1.0";
        if (invoice.getMultiplyFactor() != null) {
            multiplyFactorStr = invoice.getMultiplyFactor().stripTrailingZeros().toPlainString();
        }

        String printDateStr = invoice.getIssueDate() != null ? invoice.getIssueDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        WorkflowSigners workflowSigners = resolveWorkflowSigners(invoice);
        IntervalReadings intervalReadings = resolveIntervalReadings(invoice.getAccountNumber(), invoice.getAreaCode(), invoice.getBillCycle());

        BigDecimal genLoss = resolveGenerationLossesForInvoice(invoice);
        String eligibleEnergyLabel = buildEligibleEnergyLabel(genLoss);
        String generationLossPercent = formatLossPercent(genLoss);

        String invMonth = invoice.getInvoiceMonth();
        if (invMonth == null || invMonth.trim().isEmpty()) {
            YearMonth ym = resolveInvoiceMonth(invoice.getBillCycle());
            invMonth = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear();
        }

        return InvoicePreviewDTO.builder()
                .documentType(documentType)
                .regionSrNo((invoice.getRegion() != null ? invoice.getRegion().trim() : "") + " - " + (invoice.getSrNo() != null ? invoice.getSrNo().trim() : ""))
                .folioMonthLabel((invoice.getFolioNo() != null ? String.valueOf(invoice.getFolioNo()) : "") + "  " + invMonth)
                .folioNo(invoice.getFolioNo() != null ? String.valueOf(invoice.getFolioNo()) : "")
                .refNo(invoice.getFileRefNo() != null ? invoice.getFileRefNo().trim() : "")
                .refCode(invoice.getReferenceCode() != null ? invoice.getReferenceCode().trim() : "")
                .companyName(invoice.getCompanyName() != null ? invoice.getCompanyName().trim() : "")
                .projectName(invoice.getProjectName() != null ? invoice.getProjectName().trim() : "")
                .invoiceMonth(invMonth)
                .capacityMw(capacityStr)
                .allowedGenerationMw(allowedGenStr)
                .presentReadingDate(invoice.getPresentReadingDate() != null ? invoice.getPresentReadingDate().toString() : "")
                .previousReadingDate(invoice.getPreviousReadingDate() != null ? invoice.getPreviousReadingDate().toString() : "")
                .totalPresentReading(invoice.getTotalPresentReading() != null ? numberFormat.format(invoice.getTotalPresentReading()) : "0")
                .totalPreviousReading(invoice.getTotalPreviousReading() != null ? numberFormat.format(invoice.getTotalPreviousReading()) : "0")
                .presentR1(intervalReadings.presentR1)
                .presentR2(intervalReadings.presentR2)
                .presentR3(intervalReadings.presentR3)
                .previousR1(intervalReadings.previousR1)
                .previousR2(intervalReadings.previousR2)
                .previousR3(intervalReadings.previousR3)
                .multiplyFactor(multiplyFactorStr)
                .energyKwh(invoice.getEnergyKwh() != null ? decimalFormat.format(invoice.getEnergyKwh()) : "0.0")
                .eligibleEnergyKwh(invoice.getEligibleEnergyKwh() != null ? numberFormat.format(invoice.getEligibleEnergyKwh().intValue()) : "0")
                .eligibleEnergyLabel(eligibleEnergyLabel)
                .generationLossPercent(generationLossPercent)
                .periodOfGeneration(invoice.getPeriodOfGenerationDays() != null ? String.valueOf(invoice.getPeriodOfGenerationDays()) : "0")
                .plantFactorPercent(invoice.getPlantFactorPercent() != null ? invoice.getPlantFactorPercent().toPlainString() : "0.00")
                .energyPurchasedKwh(invoice.getEnergyPurchasedKwh() != null ? decimalFormat.format(invoice.getEnergyPurchasedKwh()) : "0.0")
                .ratePerKwh(rateStr)
                .costOfEnergy(costStr)
                .printDate(printDateStr)
                .preparedBy(workflowSigners.getPreparedBy())
                .chiefEngineer(workflowSigners.getChiefEngineer())
                .director(workflowSigners.getDirector())
                .invoiceCreatedOn(workflowSigners.getPreparedBy() != null && workflowSigners.getPreparedBy().getDate() != null ? workflowSigners.getPreparedBy().getDate() : "")
                .invoiceApprovedOn(workflowSigners.getDirector() != null && workflowSigners.getDirector().getDate() != null ? workflowSigners.getDirector().getDate() : "")
                .preparedByName(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getName() : "N/A")
                .preparedByTitle(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getDesignation() : "")
                .accountNumber(invoice.getAccountNumber())
                .areaCode(invoice.getAreaCode())
                .billCycle(invoice.getBillCycle())
                .paymentDeductions(deductionDTOs)
                .totalPaymentDeductions("")
                .finalAmountToBePaid(finalAmount != null ? moneyFormat.format(finalAmount) : "0.00")
                .engSendR1(invoice.getEngSendR1() != null ? decimalFormat.format(invoice.getEngSendR1()) : "")
                .engSendR2(invoice.getEngSendR2() != null ? decimalFormat.format(invoice.getEngSendR2()) : "")
                .engSendR3(invoice.getEngSendR3() != null ? decimalFormat.format(invoice.getEngSendR3()) : "")
                .ded1Perc(invoice.getDed1Perc() != null ? moneyFormat.format(invoice.getDed1Perc()) : "")
                .tariffChargeLines(loadTariffChargeLineDTOs(invoice.getId()))
                .build();
    }

    @org.springframework.scheduling.annotation.Async
    public void sendInvoiceDistributionAsync(Long invoiceId) {
        System.out.println("[InvoiceService] Asynchronously distributing invoices for ID: " + invoiceId);
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

            NcreDeveloper developer = ncreDeveloperRepository.findByAccNbrTrimmed(invoice.getAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Developer not found: " + invoice.getAccountNumber()));

            String recipientEmail = developer.getEmail();
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                System.err.println("[InvoiceService] Skipped emailing. No email configured for developer: " + developer.getDeveloperName());
                return;
            }

            System.out.println("[InvoiceService] Found developer email: " + recipientEmail);

            Map<String, byte[]> attachments = new java.util.LinkedHashMap<>();

            List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(developer.getFolioNo());
            Agreement latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;

            List<InvoicePreviewDTO.PaymentDeductionDTO> deductionDTOs = new ArrayList<>();
            BigDecimal totalDeductions = BigDecimal.ZERO;
            BigDecimal finalAmount = invoice.getCostOfEnergy();

            DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

            if (latestAgreement != null && latestAgreement.getPaymentDeductions() != null && !latestAgreement.getPaymentDeductions().isEmpty()) {
                List<PaymentDeduction> agreementDeductions = latestAgreement.getPaymentDeductions();
                BigDecimal loyaltyAmount = BigDecimal.ZERO;
                BigDecimal baseForOtherDeductions = invoice.getCostOfEnergy();

                Optional<PaymentDeduction> loyaltyOpt = agreementDeductions.stream()
                        .filter(d -> "Loyalty Deduction (%)".equalsIgnoreCase(d.getDeductionType()))
                        .findFirst();

                if (loyaltyOpt.isPresent()) {
                    PaymentDeduction loyalty = loyaltyOpt.get();
                    BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                    loyaltyAmount = invoice.getCostOfEnergy().multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    baseForOtherDeductions = invoice.getCostOfEnergy().subtract(loyaltyAmount);
                }

                BigDecimal runningBalance = invoice.getCostOfEnergy();

                if (loyaltyOpt.isPresent()) {
                    PaymentDeduction loyalty = loyaltyOpt.get();
                    BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                    runningBalance = runningBalance.subtract(loyaltyAmount);
                    totalDeductions = totalDeductions.add(loyaltyAmount);

                    deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                            .type(loyalty.getDeductionType())
                            .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                            .deductionAmount(moneyFormat.format(loyaltyAmount))
                            .amountToBePaid(moneyFormat.format(runningBalance))
                            .build());
                }

                for (PaymentDeduction d : agreementDeductions) {
                    String type = d.getDeductionType();
                    if (!isLoyaltyDeduction(type)) {
                        BigDecimal pct = d.getPercentage() != null ? d.getPercentage() : BigDecimal.ZERO;
                        BigDecimal amount = baseForOtherDeductions.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        runningBalance = runningBalance.subtract(amount);
                        totalDeductions = totalDeductions.add(amount);

                        deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                                .type(type)
                                .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                                .deductionAmount(moneyFormat.format(amount))
                                .amountToBePaid(moneyFormat.format(runningBalance))
                                .build());
                    }
                }
                finalAmount = runningBalance;
            }

            InvoicePreviewDTO mainPreview = buildPreviewFromInvoice(invoice, "MAIN", deductionDTOs, finalAmount);
            mainPreview.setTotalPaymentDeductions(moneyFormat.format(totalDeductions));
            mainPreview.setDocumentType("MAIN");

            String mainHtml = renderInvoiceHtml(mainPreview);
            byte[] mainPdf = renderPdf(mainHtml);
            attachments.put("Invoice_" + invoice.getInvoiceNumber() + ".pdf", mainPdf);

            if (latestAgreement != null && latestAgreement.getPaymentDeductions() != null && !latestAgreement.getPaymentDeductions().isEmpty()) {
                List<PaymentDeduction> agreementDeductions = latestAgreement.getPaymentDeductions();
                BigDecimal baseForOtherDeductions = invoice.getCostOfEnergy();

                Optional<PaymentDeduction> loyaltyOpt = agreementDeductions.stream()
                        .filter(d -> isLoyaltyDeduction(d.getDeductionType()))
                        .findFirst();

                if (loyaltyOpt.isPresent()) {
                    PaymentDeduction loyalty = loyaltyOpt.get();
                    BigDecimal pct = loyalty.getPercentage() != null ? loyalty.getPercentage() : BigDecimal.ZERO;
                    BigDecimal loyaltyAmount = invoice.getCostOfEnergy().multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    baseForOtherDeductions = invoice.getCostOfEnergy().subtract(loyaltyAmount);

                    List<InvoicePreviewDTO.PaymentDeductionDTO> singleDeductionList = new ArrayList<>();
                    singleDeductionList.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                            .type("Loyalty to be paid to the MASL ( " + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "% of cost)")
                            .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                            .deductionAmount(moneyFormat.format(loyaltyAmount))
                            .amountToBePaid("")
                            .build());

                    InvoicePreviewDTO loyaltyPreview = buildPreviewFromInvoice(invoice, "LOYALTY", singleDeductionList, loyaltyAmount);
                    String loyaltyRefNo = "INV-LOYALTY-" + invoice.getAccountNumber().trim() + "+" + invoice.getBillCycle();
                    loyaltyPreview.setRefNo(loyaltyRefNo);
                    loyaltyPreview.setDocumentType("LOYALTY");

                    String loyaltyHtml = renderInvoiceHtml(loyaltyPreview);
                    byte[] loyaltyPdf = renderPdf(loyaltyHtml);
                    attachments.put("Invoice_Loyalty_" + loyaltyRefNo + ".pdf", loyaltyPdf);
                }

                for (PaymentDeduction d : agreementDeductions) {
                    String type = d.getDeductionType();
                    if (!isLoyaltyDeduction(type)) {
                        BigDecimal pct = d.getPercentage() != null ? d.getPercentage() : BigDecimal.ZERO;
                        BigDecimal amount = baseForOtherDeductions.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                        List<InvoicePreviewDTO.PaymentDeductionDTO> singleDeductionList = new ArrayList<>();
                        singleDeductionList.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                                .type(type)
                                .percentage(pct.setScale(2, RoundingMode.HALF_UP).toPlainString())
                                .deductionAmount(moneyFormat.format(amount))
                                .amountToBePaid("")
                                .build());

                        String docType = type.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                        InvoicePreviewDTO deductionPreview = buildPreviewFromInvoice(invoice, docType, singleDeductionList, amount);
                        String deductionRefNo = "INV-" + docType + "-" + invoice.getAccountNumber().trim() + "+" + invoice.getBillCycle();
                        deductionPreview.setRefNo(deductionRefNo);
                        deductionPreview.setDocumentType(docType);

                        String deductionHtml = renderInvoiceHtml(deductionPreview);
                        byte[] deductionPdf = renderPdf(deductionHtml);
                        attachments.put("Invoice_" + docType + "_" + deductionRefNo + ".pdf", deductionPdf);
                    }
                }
            }

            String subject = "Payment Invoice & Deductions Released: " + invoice.getInvoiceNumber();
            String htmlBody = "<h3>Dear " + developer.getDeveloperName() + ",</h3>"
                    + "<p>Please find attached the finalized Payment Invoice and associated Deduction invoices for the month of <strong>"
                    + invoice.getInvoiceMonth() + "</strong>.</p>"
                    + "<br/>"
                    + "<p>This is an automated message. Please do not reply.</p>";

            emailService.sendEmailWithAttachments(recipientEmail, subject, htmlBody, attachments);
            System.out.println("[InvoiceService] Invoices sent successfully to " + recipientEmail);

        } catch (Exception e) {
            System.err.println("[InvoiceService] Error distributing invoices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private InvoicePreviewDTO mapInvoiceToPreview(Invoice invoice) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

        List<InvoicePreviewDTO.PaymentDeductionDTO> deductionDTOs = new ArrayList<>();
        BigDecimal costOfEnergy = invoice.getCostOfEnergy() != null ? invoice.getCostOfEnergy() : BigDecimal.ZERO;
        BigDecimal runningBalance = costOfEnergy;
        BigDecimal totalDeductions = BigDecimal.ZERO;

        // 1. Loyalty
        if (invoice.getLoyalty() != null && invoice.getLoyalty().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal loyaltyVal = invoice.getLoyalty();
            runningBalance = runningBalance.subtract(loyaltyVal);
            totalDeductions = totalDeductions.add(loyaltyVal);
            BigDecimal pct = costOfEnergy.compareTo(BigDecimal.ZERO) > 0 
                ? loyaltyVal.multiply(BigDecimal.valueOf(100)).divide(costOfEnergy, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                    .type("Loyalty Deduction (%)")
                    .percentage(pct.stripTrailingZeros().toPlainString())
                    .deductionAmount(moneyFormat.format(loyaltyVal))
                    .amountToBePaid(moneyFormat.format(runningBalance))
                    .build());
        }

        BigDecimal baseForOtherDeductions = costOfEnergy.subtract(invoice.getLoyalty() != null ? invoice.getLoyalty() : BigDecimal.ZERO);

        // 2. Escrow
        if (invoice.getEscrow() != null && invoice.getEscrow().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal escrowVal = invoice.getEscrow();
            runningBalance = runningBalance.subtract(escrowVal);
            totalDeductions = totalDeductions.add(escrowVal);
            BigDecimal pct = baseForOtherDeductions.compareTo(BigDecimal.ZERO) > 0 
                ? escrowVal.multiply(BigDecimal.valueOf(100)).divide(baseForOtherDeductions, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                    .type("ESCROW Deduction (%)")
                    .percentage(pct.stripTrailingZeros().toPlainString())
                    .deductionAmount(moneyFormat.format(escrowVal))
                    .amountToBePaid(moneyFormat.format(runningBalance))
                    .build());
        }

        // 3. Mahaweli
        if (invoice.getMahaweli() != null && invoice.getMahaweli().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mahaweliVal = invoice.getMahaweli();
            runningBalance = runningBalance.subtract(mahaweliVal);
            totalDeductions = totalDeductions.add(mahaweliVal);
            BigDecimal pct = baseForOtherDeductions.compareTo(BigDecimal.ZERO) > 0 
                ? mahaweliVal.multiply(BigDecimal.valueOf(100)).divide(baseForOtherDeductions, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                    .type("Mahaveli Deduction (%)")
                    .percentage(pct.stripTrailingZeros().toPlainString())
                    .deductionAmount(moneyFormat.format(mahaweliVal))
                    .amountToBePaid(moneyFormat.format(runningBalance))
                    .build());
        }

        // 4. Treasury
        if (invoice.getTreasury() != null && invoice.getTreasury().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal treasuryVal = invoice.getTreasury();
            runningBalance = runningBalance.subtract(treasuryVal);
            totalDeductions = totalDeductions.add(treasuryVal);
            BigDecimal pct = baseForOtherDeductions.compareTo(BigDecimal.ZERO) > 0 
                ? treasuryVal.multiply(BigDecimal.valueOf(100)).divide(baseForOtherDeductions, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                    .type("Treasury")
                    .percentage(pct.stripTrailingZeros().toPlainString())
                    .deductionAmount(moneyFormat.format(treasuryVal))
                    .amountToBePaid(moneyFormat.format(runningBalance))
                    .build());
        }

        // 5. 1% Deduction
        if (invoice.getDed1Perc() != null && invoice.getDed1Perc().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ded1Val = invoice.getDed1Perc();
            runningBalance = runningBalance.subtract(ded1Val);
            totalDeductions = totalDeductions.add(ded1Val);
            BigDecimal pct = baseForOtherDeductions.compareTo(BigDecimal.ZERO) > 0 
                ? ded1Val.multiply(BigDecimal.valueOf(100)).divide(baseForOtherDeductions, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            deductionDTOs.add(InvoicePreviewDTO.PaymentDeductionDTO.builder()
                    .type("1% Deduction (%)")
                    .percentage(pct.stripTrailingZeros().toPlainString())
                    .deductionAmount(moneyFormat.format(ded1Val))
                    .amountToBePaid(moneyFormat.format(runningBalance))
                    .build());
        }

        WorkflowSigners workflowSigners = resolveWorkflowSigners(invoice);
        IntervalReadings intervalReadings = resolveIntervalReadings(invoice.getAccountNumber(), invoice.getAreaCode(), invoice.getBillCycle());

        BigDecimal genLoss = resolveGenerationLossesForInvoice(invoice);
        String eligibleEnergyLabel = buildEligibleEnergyLabel(genLoss);
        String generationLossPercent = formatLossPercent(genLoss);

        String invMonth = invoice.getInvoiceMonth();
        if (invMonth == null || invMonth.trim().isEmpty()) {
            YearMonth ym = resolveInvoiceMonth(invoice.getBillCycle());
            invMonth = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear();
        }

        return InvoicePreviewDTO.builder()
                .documentType("MAIN")
                .regionSrNo((invoice.getRegion() != null ? invoice.getRegion() : "") + " - " + (invoice.getSrNo() != null ? invoice.getSrNo() : ""))
                .folioMonthLabel((invoice.getFolioNo() != null ? String.valueOf(invoice.getFolioNo()) : "") + "  " + invMonth)
                .folioNo(invoice.getFolioNo() != null ? String.valueOf(invoice.getFolioNo()) : "")
                .refNo(invoice.getFileRefNo() != null ? invoice.getFileRefNo() : "")
                .refCode(invoice.getReferenceCode() != null ? invoice.getReferenceCode() : "")
                .companyName(invoice.getCompanyName() != null ? invoice.getCompanyName() : "")
                .projectName(invoice.getProjectName() != null ? invoice.getProjectName() : "")
                .invoiceMonth(invMonth)
                .capacityMw(invoice.getCapacityMw() != null ? invoice.getCapacityMw().stripTrailingZeros().toPlainString() : "0")
                .allowedGenerationMw(invoice.getAllowedGenerationMw() != null ? invoice.getAllowedGenerationMw().stripTrailingZeros().toPlainString() : "0")
                .presentReadingDate(invoice.getPresentReadingDate() != null ? invoice.getPresentReadingDate().toString() : "")
                .previousReadingDate(invoice.getPreviousReadingDate() != null ? invoice.getPreviousReadingDate().toString() : "")
                .totalPresentReading(numberFormat.format(invoice.getTotalPresentReading() != null ? invoice.getTotalPresentReading() : 0))
                .totalPreviousReading(numberFormat.format(invoice.getTotalPreviousReading() != null ? invoice.getTotalPreviousReading() : 0))
                .presentR1(intervalReadings.presentR1)
                .presentR2(intervalReadings.presentR2)
                .presentR3(intervalReadings.presentR3)
                .previousR1(intervalReadings.previousR1)
                .previousR2(intervalReadings.previousR2)
                .previousR3(intervalReadings.previousR3)
                .multiplyFactor(invoice.getMultiplyFactor() != null ? invoice.getMultiplyFactor().stripTrailingZeros().toPlainString() : "1")
                .energyKwh(decimalFormat.format(invoice.getEnergyKwh() != null ? invoice.getEnergyKwh() : BigDecimal.ZERO))
                .eligibleEnergyKwh(numberFormat.format(invoice.getEligibleEnergyKwh() != null ? invoice.getEligibleEnergyKwh().intValue() : 0))
                .eligibleEnergyLabel(eligibleEnergyLabel)
                .generationLossPercent(generationLossPercent)
                .periodOfGeneration(String.valueOf(invoice.getPeriodOfGenerationDays() != null ? invoice.getPeriodOfGenerationDays() : 0))
                .plantFactorPercent(invoice.getPlantFactorPercent() != null ? invoice.getPlantFactorPercent().stripTrailingZeros().toPlainString() : "0")
                .energyPurchasedKwh(decimalFormat.format(invoice.getEnergyPurchasedKwh() != null ? invoice.getEnergyPurchasedKwh() : BigDecimal.ZERO))
                .ratePerKwh(invoice.getRatePerKwh() != null ? invoice.getRatePerKwh().stripTrailingZeros().toPlainString() : "0")
                .costOfEnergy(moneyFormat.format(costOfEnergy))
                .printDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .preparedBy(workflowSigners.getPreparedBy())
                .chiefEngineer(workflowSigners.getChiefEngineer())
                .director(workflowSigners.getDirector())
                .invoiceCreatedOn(workflowSigners.getPreparedBy() != null && workflowSigners.getPreparedBy().getDate() != null ? workflowSigners.getPreparedBy().getDate() : "")
                .invoiceApprovedOn(workflowSigners.getDirector() != null && workflowSigners.getDirector().getDate() != null ? workflowSigners.getDirector().getDate() : "")
                .preparedByName(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getName() : "N/A")
                .preparedByTitle(workflowSigners.getPreparedBy() != null ? workflowSigners.getPreparedBy().getDesignation() : "")
                .accountNumber(invoice.getAccountNumber())
                .areaCode(invoice.getAreaCode())
                .billCycle(invoice.getBillCycle())
                .paymentDeductions(deductionDTOs)
                .totalPaymentDeductions(moneyFormat.format(totalDeductions))
                .finalAmountToBePaid(moneyFormat.format(invoice.getDeveloperPymnt() != null ? invoice.getDeveloperPymnt() : runningBalance))
                .engSendR1(invoice.getEngSendR1() != null ? decimalFormat.format(invoice.getEngSendR1()) : (intervalReadings != null && intervalReadings.presentR1 != null && invoiceCalculationService != null ? decimalFormat.format(invoiceCalculationService.calculateIntervalEnergy(parseFormattedNumber(intervalReadings.presentR1), parseFormattedNumber(intervalReadings.previousR1))) : "0.0"))
                .engSendR2(invoice.getEngSendR2() != null ? decimalFormat.format(invoice.getEngSendR2()) : (intervalReadings != null && intervalReadings.presentR2 != null && invoiceCalculationService != null ? decimalFormat.format(invoiceCalculationService.calculateIntervalEnergy(parseFormattedNumber(intervalReadings.presentR2), parseFormattedNumber(intervalReadings.previousR2))) : "0.0"))
                .engSendR3(invoice.getEngSendR3() != null ? decimalFormat.format(invoice.getEngSendR3()) : (intervalReadings != null && intervalReadings.presentR3 != null && invoiceCalculationService != null ? decimalFormat.format(invoiceCalculationService.calculateIntervalEnergy(parseFormattedNumber(intervalReadings.presentR3), parseFormattedNumber(intervalReadings.previousR3))) : "0.0"))
                .ded1Perc(invoice.getDed1Perc() != null ? moneyFormat.format(invoice.getDed1Perc()) : "0.00")
                .tariffChargeLines(loadTariffChargeLineDTOs(invoice.getId()))
                .build();
    }

    private String getStatusDisplayName(InvoiceStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case RECOMMEND: return "Pending Recommendation";
            case APPROVE: return "Pending Approval";
            case FINALIZE: return "Finalized";
            case REJECTED: return "Rejected";
            default: return status.name();
        }
    }

    public List<Invoice> getInvoiceHistory(String accountNumber) {
        List<Invoice> all = invoiceRepository.findByAccountNumberOrderByIssueDateDesc(accountNumber);
        List<Invoice> finalized = new ArrayList<>();
        for (Invoice inv : all) {
            if (inv.getStatus() == InvoiceStatus.FINALIZE) {
                finalized.add(inv);
            }
        }
        return finalized;
    }

    public boolean isLoyaltyDeduction(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase();
        return t.contains("loyalty");
    }

    public boolean isEscrowDeduction(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase();
        return t.contains("escrow");
    }

    public boolean isMahaweliDeduction(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase();
        return t.contains("mahaweli") || t.contains("mahaveli");
    }

    public boolean isTreasuryDeduction(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase();
        return t.contains("treasury");
    }

    public boolean isDed1PercDeduction(String type) {
        if (type == null) return false;
        String t = type.trim().toLowerCase();
        return t.equals("1% deduction (%)") || t.equals("1% deduction") || t.equals("1ded")
                || t.equals("ded_1_perc") || t.startsWith("1%") || t.contains("1% deduction");
    }

    public void populateFinalizedInvoiceFields(Invoice invoice) {
        if (invoice == null) return;
        try {
            // 1. Ensure interval energy sent to grid (R1, R2, R3) are populated
            boolean needR1 = invoice.getEngSendR1() == null || invoice.getEngSendR1().compareTo(BigDecimal.ZERO) == 0;
            boolean needR2 = invoice.getEngSendR2() == null || invoice.getEngSendR2().compareTo(BigDecimal.ZERO) == 0;
            boolean needR3 = invoice.getEngSendR3() == null || invoice.getEngSendR3().compareTo(BigDecimal.ZERO) == 0;

            if (needR1 || needR2 || needR3) {
                if (invoiceCalculationService != null && ncreInvRdngsRepository != null) {
                    IntervalReadings ir = resolveIntervalReadings(invoice.getAccountNumber(), invoice.getAreaCode(), invoice.getBillCycle());
                    if (needR1) {
                        BigDecimal r1 = (ir != null && ir.presentR1 != null)
                                ? invoiceCalculationService.calculateIntervalEnergy(
                                        parseFormattedNumber(ir.presentR1),
                                        parseFormattedNumber(ir.previousR1))
                                : BigDecimal.ZERO;
                        invoice.setEngSendR1(r1 != null ? r1.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    }
                    if (needR2) {
                        BigDecimal r2 = (ir != null && ir.presentR2 != null)
                                ? invoiceCalculationService.calculateIntervalEnergy(
                                        parseFormattedNumber(ir.presentR2),
                                        parseFormattedNumber(ir.previousR2))
                                : BigDecimal.ZERO;
                        invoice.setEngSendR2(r2 != null ? r2.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    }
                    if (needR3) {
                        BigDecimal r3 = (ir != null && ir.presentR3 != null)
                                ? invoiceCalculationService.calculateIntervalEnergy(
                                        parseFormattedNumber(ir.presentR3),
                                        parseFormattedNumber(ir.previousR3))
                                : BigDecimal.ZERO;
                        invoice.setEngSendR3(r3 != null ? r3.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    }
                }
            }

            // 2. Ensure ded_1_perc is populated
            if (invoice.getDed1Perc() == null || invoice.getDed1Perc().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal ded1PercVal = BigDecimal.ZERO;
                if (agreementRepository != null && (invoice.getFolioNo() != null || invoice.getAccountNumber() != null)) {
                    Short folio = invoice.getFolioNo() != null ? invoice.getFolioNo().shortValue() : null;
                    if (folio == null && ncreDeveloperRepository != null && invoice.getAccountNumber() != null) {
                        Optional<NcreDeveloper> devOpt = ncreDeveloperRepository.findByAccNbrTrimmed(invoice.getAccountNumber());
                        if (devOpt != null && devOpt.isPresent()) {
                            folio = devOpt.get().getFolioNo();
                        }
                    }

                    if (folio != null) {
                        List<Agreement> agreements = agreementRepository.findByFolioNoOrderByAgreementIdDesc(folio);
                        Agreement latestAgreement = (agreements != null && !agreements.isEmpty()) ? agreements.get(0) : null;
                        if (latestAgreement != null && latestAgreement.getPaymentDeductions() != null) {
                            BigDecimal costOfEnergy = invoice.getCostOfEnergy() != null ? invoice.getCostOfEnergy() : BigDecimal.ZERO;
                            BigDecimal baseForOtherDeductions = costOfEnergy;
                            Optional<PaymentDeduction> loyaltyOpt = latestAgreement.getPaymentDeductions().stream()
                                    .filter(d -> isLoyaltyDeduction(d.getDeductionType()))
                                    .findFirst();
                            if (loyaltyOpt.isPresent()) {
                                BigDecimal pct = loyaltyOpt.get().getPercentage() != null ? loyaltyOpt.get().getPercentage() : BigDecimal.ZERO;
                                BigDecimal loyaltyAmt = costOfEnergy.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                                baseForOtherDeductions = costOfEnergy.subtract(loyaltyAmt);
                            }

                            for (PaymentDeduction d : latestAgreement.getPaymentDeductions()) {
                                if (isDed1PercDeduction(d.getDeductionType())) {
                                    BigDecimal pct = d.getPercentage() != null ? d.getPercentage() : BigDecimal.ZERO;
                                    ded1PercVal = baseForOtherDeductions.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                                    break;
                                }
                            }
                        }
                    }
                }
                invoice.setDed1Perc(ded1PercVal);
            }
        } catch (Exception e) {
            System.err.println("[InvoiceService] Warning: Failed to populate finalized invoice fields: " + e.getMessage());
        } finally {
            if (invoice.getEngSendR1() == null) invoice.setEngSendR1(BigDecimal.ZERO);
            if (invoice.getEngSendR2() == null) invoice.setEngSendR2(BigDecimal.ZERO);
            if (invoice.getEngSendR3() == null) invoice.setEngSendR3(BigDecimal.ZERO);
            if (invoice.getDed1Perc() == null) invoice.setDed1Perc(BigDecimal.ZERO);
        }
    }
}