import { apiPath, getAuthHeaders } from '../config';

/**
 * Retrieve session_id and user_id from browser storage
 */
function getSessionParams() {
  const sessionId = sessionStorage.getItem('session_id') || localStorage.getItem('session_id') || '';
  const userId = sessionStorage.getItem('user_id') || localStorage.getItem('user_id') || '';
  return { sessionId, userId };
}

/**
 * Validate bill cycle input prior to issuing API requests
 */
function isValidBillCycle(billCycle) {
  if (billCycle === null || billCycle === undefined) return false;
  const str = String(billCycle).trim();
  if (!str || str === '0' || str === 'undefined' || str === 'null') return false;
  const num = parseInt(str, 10);
  return !isNaN(num) && num > 0;
}

/**
 * Fetch invoices for Invoice Checklist by bill cycle
 */
export async function fetchChecklistInvoices(billCycle) {
  if (!isValidBillCycle(billCycle)) {
    throw new Error('Valid bill cycle is required');
  }
  
  const cleanCycle = String(billCycle).trim();
  const { sessionId, userId } = getSessionParams();
  
  const params = new URLSearchParams();
  params.append('billCycle', cleanCycle);
  if (sessionId) params.append('session_id', sessionId);
  if (userId) params.append('user_id', userId);

  const url = apiPath(`/api/v1/invoice-checklist/invoices?${params.toString()}`);

  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    const errorMsg = data.errorMessage || data.message || `Server returned HTTP ${response.status}`;
    console.error('Invoice Checklist API Error:', { status: response.status, data });
    throw new Error(errorMsg);
  }

  return {
    success: true,
    billCycle: data.billCycle || parseInt(cleanCycle, 10),
    userRole: data.userRole || '',
    totalCount: data.totalCount || 0,
    invoices: data.data || data.invoices || [],
  };
}

/**
 * Fetch checked invoices for Invoice Checklist by bill cycle (is_create = 1)
 */
export async function fetchCheckedInvoices(billCycle) {
  if (!isValidBillCycle(billCycle)) {
    throw new Error('Valid bill cycle is required');
  }
  
  const cleanCycle = String(billCycle).trim();
  const { sessionId, userId } = getSessionParams();
  
  const params = new URLSearchParams();
  params.append('billCycle', cleanCycle);
  if (sessionId) params.append('session_id', sessionId);
  if (userId) params.append('user_id', userId);

  const url = apiPath(`/api/v1/invoice-checklist/checked-invoices?${params.toString()}`);

  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    const errorMsg = data.errorMessage || data.message || `Server returned HTTP ${response.status}`;
    console.error('Checked Invoices API Error:', { status: response.status, data });
    throw new Error(errorMsg);
  }

  return {
    success: true,
    billCycle: data.billCycle || parseInt(cleanCycle, 10),
    userRole: data.userRole || '',
    totalCount: data.totalCount || 0,
    invoices: data.data || data.invoices || [],
  };
}

/**
 * Save selected invoices to ncre_invoice_create
 */
export async function addChecklistInvoices(billCycle, selectedInvoices) {
  if (!isValidBillCycle(billCycle)) {
    throw new Error('Valid bill cycle is required');
  }
  if (!selectedInvoices || selectedInvoices.length === 0) {
    throw new Error('At least one invoice must be selected');
  }

  const cleanCycle = parseInt(String(billCycle).trim(), 10);
  const { sessionId, userId } = getSessionParams();

  const params = new URLSearchParams();
  if (sessionId) params.append('session_id', sessionId);
  if (userId) params.append('user_id', userId);
  const queryStr = params.toString() ? `?${params.toString()}` : '';

  const response = await fetch(apiPath(`/api/v1/invoice-checklist/add${queryStr}`), {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({
      billCycle: cleanCycle,
      session_id: sessionId,
      user_id: userId,
      selectedInvoices: selectedInvoices.map(item => ({
        folioNo: parseInt(item.folioNo, 10),
        remarks: item.remarks || '',
      })),
    }),
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    const errorMsg = data.errorMessage || data.message || `Server returned HTTP ${response.status}`;
    console.error('Add Invoices Checklist Error:', { status: response.status, data });
    throw new Error(errorMsg);
  }

  return data;
}

/**
 * Get Invoice Checklist Validation metrics
 */
export async function validateChecklist(billCycle) {
  if (!isValidBillCycle(billCycle)) {
    throw new Error('Valid bill cycle is required');
  }

  const cleanCycle = String(billCycle).trim();
  const { sessionId, userId } = getSessionParams();

  const params = new URLSearchParams();
  params.append('billCycle', cleanCycle);
  if (sessionId) params.append('session_id', sessionId);
  if (userId) params.append('user_id', userId);

  const response = await fetch(apiPath(`/api/v1/invoice-checklist/validate?${params.toString()}`), {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    const errorMsg = data.errorMessage || data.message || `Server returned HTTP ${response.status}`;
    console.error('Validate Checklist Error:', { status: response.status, data });
    throw new Error(errorMsg);
  }

  return data.data;
}

/**
 * Fetch current active bill cycle where is_current = 1 from ncre_bill_cycle
 */
export async function fetchCurrentBillCycle() {
  const { sessionId, userId } = getSessionParams();
  const params = new URLSearchParams();
  if (sessionId) params.append('session_id', sessionId);
  if (userId) params.append('user_id', userId);
  const queryStr = params.toString() ? `?${params.toString()}` : '';

  const response = await fetch(apiPath(`/api/v1/invoice-checklist/current-bill-cycle${queryStr}`), {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok || data.success === false) {
    const errorMsg = data.errorMessage || data.message || 'No current bill month is available.';
    throw new Error(errorMsg);
  }

  return data.billCycle;
}

const invoiceChecklistService = {
  fetchCurrentBillCycle,
  fetchChecklistInvoices,
  fetchCheckedInvoices,
  addChecklistInvoices,
  validateChecklist,
};

export default invoiceChecklistService;
