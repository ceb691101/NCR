package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BulkInvoiceReviewRequestDTO;
import com.example.SPSProjectBackend.dto.InvoiceGenerationRequestDTO;
import com.example.SPSProjectBackend.dto.InvoiceReviewRequestDTO;
import com.example.SPSProjectBackend.dto.InvoicePreviewDTO;
import com.example.SPSProjectBackend.dto.ValidationResultDTO;
import java.util.Map;
import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoice")
@CrossOrigin(
		origins = {"http://localhost:3000", "http://localhost:8097", "http://127.0.0.1:3000", "http://127.0.0.1:8097"},
		allowCredentials = "true",
		allowedHeaders = "*",
		methods = {
				org.springframework.web.bind.annotation.RequestMethod.GET,
				org.springframework.web.bind.annotation.RequestMethod.POST,
				org.springframework.web.bind.annotation.RequestMethod.OPTIONS
		}
)
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	/**
	 * Generate invoice as PDF (existing endpoint).
	 */
	@PostMapping(produces = MediaType.APPLICATION_PDF_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> generateInvoice(@Valid @RequestBody InvoiceGenerationRequestDTO request) {
		byte[] pdfBytes = invoiceService.generateInvoicePdf(request);

		String safeAccountNumber = request.getAccountNumber() == null ? "invoice" : request.getAccountNumber().trim();
		String safeBillCycle = request.getBillCycle() == null ? "unknown" : String.valueOf(request.getBillCycle());
		String fileName = "invoice-" + safeAccountNumber + "-" + safeBillCycle + ".pdf";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(pdfBytes.length)
				.body(pdfBytes);
	}

	/**
	 * Prepare invoice data as JSON for frontend modal preview.
	 * Returns calculated invoice fields from ncre_developers + tmp_rdngs.
	 */
	@PostMapping(value = "/prepare", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<InvoicePreviewDTO> prepareInvoice(@Valid @RequestBody InvoiceGenerationRequestDTO request) {
		InvoicePreviewDTO invoiceData = invoiceService.prepareInvoiceData(request);
		return ResponseEntity.ok(invoiceData);
	}

	@PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ValidationResultDTO> validateInvoice(@Valid @RequestBody InvoiceGenerationRequestDTO request) {
		System.out.println("[InvoiceController] Entering /validate endpoint with request payload: "
				+ "accountNumber=" + request.getAccountNumber()
				+ ", areaCode=" + request.getAreaCode()
				+ ", billCycle=" + request.getBillCycle()
				+ ", userId=" + request.getUserId()
				+ ", sessionId=" + request.getSessionId());
		try {
			System.out.println("[InvoiceController] Delegating to invoiceService.validateInvoice...");
			ValidationResultDTO result = invoiceService.validateInvoice(request);
			System.out.println("[InvoiceController] Validation successfully completed. Result: valid=" + result.isValid() + ", errors=" + result.getErrors());
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			System.err.println("[InvoiceController] ERROR during /validate endpoint execution: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Save/overwrite the invoice as a DRAFT.
	 */
	@PostMapping(value = "/save-draft", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Invoice> saveDraft(@Valid @RequestBody InvoiceGenerationRequestDTO request) {
		Invoice invoice = invoiceService.saveDraftInvoice(request);
		return ResponseEntity.ok(invoice);
	}

	/**
	 * Submit/overwrite the invoice.
	 */
	@PostMapping(value = "/submit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Invoice> submitInvoice(@Valid @RequestBody InvoiceGenerationRequestDTO request) {
		Invoice invoice = invoiceService.submitInvoice(request);
		return ResponseEntity.ok(invoice);
	}

	/**
	 * Get all invoices.
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Invoice>> getAllInvoices(
			@RequestParam(required = false) String session_id,
			@RequestParam(required = false) String user_id,
			@RequestParam(required = false) String user_category,
			@RequestParam(required = false) String month,
			@RequestParam(required = false) String status) {
		System.out.println("[InvoiceController] GET /api/v1/invoice - Entering to fetch invoices for user=" + user_id
				+ ", category=" + user_category + ", month=" + month + ", status=" + status);
		List<Invoice> invoices = invoiceService.getAllInvoices(user_id, session_id, user_category, month, status);
		return ResponseEntity.ok(invoices);
	}

	/**
	 * Get available finalized invoice months for dropdown.
	 */
	@GetMapping(value = "/months", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getAvailableFinalizedMonths() {
		System.out.println("[InvoiceController] GET /api/v1/invoice/months - Fetching available finalized invoice months");
		List<String> months = invoiceService.getAvailableFinalizedMonths();
		return ResponseEntity.ok(months);
	}

	/**
	 * Approve an invoice.
	 */
	@PostMapping(value = "/approve", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Invoice> approveInvoice(@Valid @RequestBody InvoiceReviewRequestDTO request) {
		System.out.println("[InvoiceController] POST /approve - invoiceId=" + request.getInvoiceId() + " by user=" + request.getUserId());
		Invoice invoice = invoiceService.approveInvoice(request);
		return ResponseEntity.ok(invoice);
	}

	/**
	 * Reject an invoice.
	 */
	@PostMapping(value = "/reject", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Invoice> rejectInvoice(@Valid @RequestBody InvoiceReviewRequestDTO request) {
		System.out.println("[InvoiceController] POST /reject - invoiceId=" + request.getInvoiceId() + " by user=" + request.getUserId());
		Invoice invoice = invoiceService.rejectInvoice(request);
		return ResponseEntity.ok(invoice);
	}

	/**
	 * Bulk approve/reject invoices (DGM only).
	 */
	@PostMapping(value = "/bulk-review", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> bulkReviewInvoices(@Valid @RequestBody BulkInvoiceReviewRequestDTO request) {
		System.out.println("[InvoiceController] POST /bulk-review - action=" + request.getAction() 
				+ " for " + request.getInvoiceIds().size() + " invoices by user=" + request.getUserId());
		Map<String, Object> result = invoiceService.bulkReviewInvoices(request);
		return ResponseEntity.ok(result);
	}

	/**
	 * Get historical invoices for a specific account number.
	 */
	@GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Invoice>> getInvoiceHistory(@RequestParam String accountNumber) {
		System.out.println("[InvoiceController] GET /api/v1/invoice/history - accountNumber=" + accountNumber);
		List<Invoice> history = invoiceService.getInvoiceHistory(accountNumber);
		return ResponseEntity.ok(history);
	}
}
