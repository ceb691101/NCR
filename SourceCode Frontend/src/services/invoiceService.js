import { apiPath } from "../config";

const API_BASE_URL = apiPath("/api/v1/invoice");

const handleApiResponse = async (response) => {
  const contentType = response.headers.get("content-type") || "";

  if (!response.ok) {
    const errorData = contentType.includes("application/json")
      ? await response.json().catch(() => ({}))
      : { message: await response.text().catch(() => "") };
    throw new Error(
      errorData.message || `HTTP error! Status: ${response.status}`
    );
  }

  if (contentType.includes("application/pdf")) {
    return {
      blob: await response.blob(),
      contentType,
    };
  }

  if (contentType.includes("application/json")) {
    return response.json();
  }

  return {
    text: await response.text(),
    contentType,
  };
};

const getAuthHeaders = () => {
  const sessionId = sessionStorage.getItem("session_id") || localStorage.getItem("session_id");
  const headers = {
    "Content-Type": "application/json",
    Authorization: "Basic " + btoa("user:admin123"),
  };
  if (sessionId) {
    headers["X-Session-Id"] = sessionId;
  }
  return headers;
};

/**
 * Generate invoice as PDF (existing endpoint).
 */
export const generateInvoice = async ({ accountNumber, areaCode, billCycle }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      account_number: accountNumber,
      area_code: areaCode,
      bill_cycle: billCycle,
    };

    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error generating invoice:", error);
    throw new Error(`Failed to generate invoice: ${error.message}`);
  }
};

/**
 * Prepare invoice data as JSON for the frontend modal preview.
 * Calls POST /api/v1/invoice/prepare which returns calculated invoice fields.
 */
export const prepareInvoice = async ({ accountNumber, areaCode, billCycle }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      account_number: accountNumber,
      area_code: areaCode,
      bill_cycle: parseInt(billCycle, 10),
    };

    const response = await fetch(`${API_BASE_URL}/prepare`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error preparing invoice:", error);
    throw error;
  }
};

/**
 * Validate invoice data.
 * Calls POST /api/v1/invoice/validate which returns { valid: boolean, errors: string[] }
 */
export const validateInvoice = async ({ accountNumber, areaCode, billCycle }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      account_number: accountNumber,
      area_code: areaCode,
      bill_cycle: parseInt(billCycle, 10),
    };

    const response = await fetch(`${API_BASE_URL}/validate`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error validating invoice:", error);
    throw new Error(`Failed to validate invoice: ${error.message}`);
  }
};

/**
 * Save invoice as DRAFT.
 * Calls POST /api/v1/invoice/save-draft which persists a draft invoice.
 */
export const saveDraftInvoice = async ({ accountNumber, areaCode, billCycle, bypassRu }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      account_number: accountNumber,
      area_code: areaCode,
      bill_cycle: parseInt(billCycle, 10),
      bypass_ru: bypassRu,
    };

    const response = await fetch(`${API_BASE_URL}/save-draft`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error saving draft invoice:", error);
    throw new Error(`Failed to save draft invoice: ${error.message}`);
  }
};

/**
 * Submit invoice for review.
 * Calls POST /api/v1/invoice/submit which validates and persists a submitted invoice.
 */
export const submitInvoice = async ({ accountNumber, areaCode, billCycle }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      account_number: accountNumber,
      area_code: areaCode,
      bill_cycle: parseInt(billCycle, 10),
    };

    const response = await fetch(`${API_BASE_URL}/submit`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error submitting invoice:", error);
    throw new Error(`Failed to submit invoice: ${error.message}`);
  }
};

/**
 * Fetch all invoices with optional month and status filters.
 */
export const getInvoices = async ({ month, status } = {}) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");
    const userCategory = sessionStorage.getItem("user_category");
    const params = new URLSearchParams();
    if (sessionId) params.append("session_id", sessionId);
    if (userId) params.append("user_id", userId);
    if (userCategory) params.append("user_category", userCategory);
    if (month) params.append("month", month);
    if (status) params.append("status", status);
    const queryString = params.toString() ? `?${params.toString()}` : "";

    const response = await fetch(`${API_BASE_URL}${queryString}`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching invoices:", error);
    throw new Error(`Failed to fetch invoices: ${error.message}`);
  }
};

/**
 * Fetch available finalized invoice months from the backend database.
 */
export const getInvoiceMonths = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/months`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching invoice months:", error);
    throw new Error(`Failed to fetch invoice months: ${error.message}`);
  }
};

/**
 * Approve an invoice.
 * Calls POST /api/v1/invoice/approve
 */
export const approveInvoice = async ({ invoiceId, remarks }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      invoice_id: parseInt(invoiceId, 10),
      remarks: remarks || "",
    };

    const response = await fetch(`${API_BASE_URL}/approve`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error approving invoice:", error);
    throw new Error(`Failed to approve invoice: ${error.message}`);
  }
};

/**
 * Reject an invoice.
 * Calls POST /api/v1/invoice/reject
 */
export const rejectInvoice = async ({ invoiceId, remarks }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      invoice_id: parseInt(invoiceId, 10),
      remarks: remarks || "",
    };

    const response = await fetch(`${API_BASE_URL}/reject`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error rejecting invoice:", error);
    throw new Error(`Failed to reject invoice: ${error.message}`);
  }
};

/**
 * Bulk approve/reject invoices.
 * Calls POST /api/v1/invoice/bulk-review
 */
export const bulkReviewInvoices = async ({ invoiceIds, action, remarks }) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      invoice_ids: invoiceIds.map(id => parseInt(id, 10)),
      action: action, // "APPROVE" or "REJECT"
      remarks: remarks || "",
    };

    const response = await fetch(`${API_BASE_URL}/bulk-review`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! Status: ${response.status}`
      );
    }

    return await response.json();
  } catch (error) {
    console.error("Error bulk reviewing invoices:", error);
    throw new Error(`Failed to bulk review invoices: ${error.message}`);
  }
};

/**
 * Fetch invoice history for a specific account number.
 */
export const getInvoiceHistory = async (accountNumber) => {
  try {
    const response = await fetch(`${API_BASE_URL}/history?accountNumber=${accountNumber}`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching invoice history:", error);
    throw new Error(`Failed to fetch invoice history: ${error.message}`);
  }
};