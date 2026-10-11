import { apiPath } from "../config";

const API_BASE_URL = apiPath("/api/developer-reports");

const getAuthHeaders = () => {
  const sessionId = sessionStorage.getItem("session_id") || localStorage.getItem("session_id");
  const headers = {
    Authorization: "Basic " + btoa("user:admin123"),
  };
  if (sessionId) {
    headers["X-Session-Id"] = sessionId;
  }
  return headers;
};

const handleApiResponse = async (response) => {
  if (!response.ok) {
    const text = await response.text().catch(() => "");
    let errorMessage = `Request failed with status ${response.status}`;
    if (text) {
      try {
        const json = JSON.parse(text);
        errorMessage = json.error || json.message || text;
      } catch (e) {
        errorMessage = text;
      }
    }
    throw new Error(errorMessage);
  }
  return response.json();
};

/**
 * Fetch report preview data
 * @returns {Promise<Array<Object>>}
 */
const buildQueryString = (filters = {}) => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value);
  });
  const query = params.toString();
  return query ? `?${query}` : "";
};

export const getReportPreview = async (filters = {}) => {
  const response = await fetch(`${API_BASE_URL}/preview${buildQueryString(filters)}`, {
    method: "GET",
    headers: {
      ...getAuthHeaders(),
      "Accept": "application/json",
    },
    cache: "no-store",
    credentials: "include",
  });
  return handleApiResponse(response);
};

/**
 * Download file helper for PDF and CSV endpoints
 * @param {string} url 
 * @param {string} filename 
 */
const downloadFileBlob = async (url, filename) => {
  const response = await fetch(url, {
    method: "GET",
    headers: getAuthHeaders(),
    cache: "no-store",
    credentials: "include",
  });

  if (!response.ok) {
    const text = await response.text().catch(() => "");
    let errorMsg = `Download failed with status ${response.status}`;
    if (text) {
      try {
        const json = JSON.parse(text);
        errorMsg = json.error || json.message || text;
      } catch (e) {
        errorMsg = text;
      }
    }
    throw new Error(errorMsg);
  }

  const blob = await response.blob();
  const objectUrl = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = objectUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(objectUrl);
};

/**
 * Download report as PDF
 */
export const downloadReportPdf = async (filters = {}) => {
  await downloadFileBlob(`${API_BASE_URL}/export-pdf${buildQueryString(filters)}`, "developer_report.pdf");
};

/**
 * Download report as CSV
 */
export const downloadReportCsv = async (filters = {}) => {
  await downloadFileBlob(`${API_BASE_URL}/export-csv${buildQueryString(filters)}`, "developer_report.csv");
};
