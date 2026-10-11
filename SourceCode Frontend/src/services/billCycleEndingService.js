import { apiPath, getAuthHeaders } from "../config";

const BILL_CYCLE_BASE_URL = apiPath("/api/v1/billcycles");
const CURRENT_OPEN_URL = `${BILL_CYCLE_BASE_URL}/current-open`;
const INVOICE_CREATION_SUMMARY_URL = `${BILL_CYCLE_BASE_URL}/invoice-creation-summary`;
const INVOICE_CREATE_ENTRIES_URL = `${BILL_CYCLE_BASE_URL}/invoice-create-entries`;
const END_CYCLE_URL = `${BILL_CYCLE_BASE_URL}/end-cycle`;

const getRequestOptions = () => ({
  method: "GET",
  headers: getAuthHeaders(),
  credentials: "include",
});

const buildUrl = (baseUrl, billCycle) => {
  const hasCycle = billCycle !== undefined && billCycle !== null && billCycle !== "";
  return hasCycle ? `${baseUrl}?billCycle=${encodeURIComponent(billCycle)}` : baseUrl;
};

// Technical noise that must never be shown to a user in place of a real message.
const TECHNICAL_ERROR_PATTERN = /sql|jdbc|informix|exception|stack|trace|select |insert |update |column|table |syntax|nullpointer/i;

/**
 * Turn any failure into a message that is safe and useful on screen.
 * The server's own wording is preferred because it is written for users; anything that
 * looks like raw database/SQL output is replaced with a plain fallback.
 */
const toUserError = (serverMessage, fallback) => {
  const message = typeof serverMessage === "string" ? serverMessage.trim() : "";
  if (!message || TECHNICAL_ERROR_PATTERN.test(message)) {
    return new Error(fallback);
  }
  return new Error(message);
};

const requestJson = async (url, options) => {
  let response;
  try {
    response = await fetch(url, options);
  } catch (networkError) {
    // fetch rejects on connection problems; surface a plain message
    throw new Error("Could not reach the server. Please check your connection and try again.");
  }
  const result = await response.json().catch(() => ({}));
  return { response, result };
};

export async function getCurrentOpenBillCycle() {
  const { response, result } = await requestJson(CURRENT_OPEN_URL, getRequestOptions());

  if (!response.ok || !result.success || !result.current_bill_cycle) {
    throw toUserError(result.message, "Unable to load the current bill month. Please try again.");
  }

  return result.current_bill_cycle;
}

export async function getInvoiceCreationSummary(billCycle) {
  const { response, result } = await requestJson(
    buildUrl(INVOICE_CREATION_SUMMARY_URL, billCycle),
    getRequestOptions()
  );

  if (!response.ok || !result.success || !result.summary) {
    throw toUserError(result.message, "Unable to load invoice progress. Please try again.");
  }

  return result.summary;
}

export async function getInvoiceCreateEntries(billCycle) {
  const { response, result } = await requestJson(
    buildUrl(INVOICE_CREATE_ENTRIES_URL, billCycle),
    getRequestOptions()
  );

  if (!response.ok || !result.success) {
    throw toUserError(result.message, "Unable to load the invoice register. Please try again.");
  }

  return result.entries || [];
}

export async function endBillCycle(remarks = "") {
  const { response, result } = await requestJson(END_CYCLE_URL, {
    ...getRequestOptions(),
    method: "POST",
    body: JSON.stringify({ remarks: remarks.trim() || null }),
  });

  if (!response.ok || !result.success) {
    const error = toUserError(
      result.message,
      "The bill month could not be ended. No changes were made. Please try again."
    );
    // Lets the UI react differently to "the cycle state changed" (409) vs other faults
    error.status = response.status;
    throw error;
  }

  return result.result || {};
}
