import { apiPath } from "../config";

const API_BASE_URL = apiPath("/api/v1/developers");

// Helper function to handle API responses
const handleApiResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(
      errorData.message || `HTTP error! Status: ${response.status}`
    );
  }
  return response.json();
};

// Helper function to create authorization headers
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
 * Register a new developer with project information
 * @param {Object} developerData - Form data containing developer and project information
 * @returns {Promise<Object>} Response from backend
 */
export const createDeveloper = async (developerData) => {
  try {
    console.log("Creating developer with data:", developerData);

    const response = await fetch(`${API_BASE_URL}/register-developer`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(developerData),
    });

    const result = await handleApiResponse(response);
    console.log("Developer created successfully:", result);
    return result;
  } catch (error) {
    console.error("Error creating developer:", error);
    throw new Error(`Failed to register developer: ${error.message}`);
  }
};

export const getActiveDeveloperCount = async (selectedAreaCode) => {
  const sessionId = sessionStorage.getItem("session_id");
  const userId = sessionStorage.getItem("user_id");
  if (!sessionId || !userId) {
    throw new Error("Session not found. Please login again.");
  }

  const response = await fetch(`${API_BASE_URL}/active-count`, {
    method: "POST",
    headers: getAuthHeaders(),
    credentials: "include",
    body: JSON.stringify({
      session_id: sessionId,
      user_id: userId,
      selected_area_code: selectedAreaCode,
    }),
  });

  const result = await handleApiResponse(response);
  return result.active_count || 0;
};

/**
 * Get all registered developers
 * @returns {Promise<Array>} List of developers
 */
export const getAllDevelopers = async () => {
  try {
    const response = await fetch(API_BASE_URL, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching developers:", error);
    throw new Error(`Failed to fetch developers: ${error.message}`);
  }
};

/**
 * Get developer by ID
 * @param {number} developerId - Developer ID
 * @returns {Promise<Object>} Developer details
 */
export const getDeveloperById = async (developerId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${developerId}`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching developer:", error);
    throw new Error(`Failed to fetch developer: ${error.message}`);
  }
};

/**
 * Update developer information
 * @param {number} developerId - Developer ID
 * @param {Object} developerData - Updated developer data
 * @returns {Promise<Object>} Updated developer details
 */
export const updateDeveloper = async (developerId, developerData) => {
  try {
    console.log("Updating developer:", developerId, developerData);

    const response = await fetch(`${API_BASE_URL}/${developerId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(developerData),
    });

    const result = await handleApiResponse(response);
    console.log("Developer updated successfully:", result);
    return result;
  } catch (error) {
    console.error("Error updating developer:", error);
    throw new Error(`Failed to update developer: ${error.message}`);
  }
};

/**
 * Delete developer
 * @param {number} developerId - Developer ID
 * @returns {Promise<Object>} Response from backend
 */
export const deleteDeveloper = async (developerId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${developerId}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error deleting developer:", error);
    throw new Error(`Failed to delete developer: ${error.message}`);
  }
};


/**
 * Search for a developer by field and value
 * @param {string} field - The field to search by ('acc_nbr', 'developer_name', 'file_no', 'folio_no')
 * @param {string} value - The value to search for
 * @returns {Promise<Object>} Developer details
 */
export const getDeveloperBySearch = async (field, value) => {
  try {
    const response = await fetch(`${API_BASE_URL}/search?field=${field}&value=${encodeURIComponent(value)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
      credentials: 'include',
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error('Error searching developer:', error);
    throw new Error(`Failed to search developer: ${error.message}`);
  }
};

/**
 * Get autocomplete suggestions for developer search
 * @param {string} field - Field to search by ('acc_nbr', 'project_name', 'folio_no', 'developer_name')
 * @param {string} query - Search query string
 * @returns {Promise<Array<Object>>} List of summary developer suggestions [{ id, projectName, accountNumber, folioNumber, developerName }]
 */
const toStr = (value) =>
  value === undefined || value === null ? '' : String(value);

export const getDeveloperSuggestions = async (field, query) => {
  if (!query || !query.trim()) return [];
  try {
    const response = await fetch(
      `${API_BASE_URL}/autocomplete?field=${field}&query=${encodeURIComponent(query.trim())}`,
      {
        method: 'GET',
        headers: getAuthHeaders(),
        credentials: 'include',
      }
    );

    if (response.status === 404) {
      // Endpoint fallback if dedicated autocomplete endpoint is not yet live on backend
      console.warn("Backend /autocomplete endpoint not found. Falling back to search API.");
      try {
        const searchResult = await getDeveloperBySearch(field, query.trim());
        if (!searchResult) return [];
        const items = Array.isArray(searchResult) ? searchResult : [searchResult];
        return items.map((item) => ({
          id: item.id,
          projectName: toStr(item.projectName || item.facilityName),
          accountNumber: toStr(item.accountNumber || item.accNbr),
          folioNumber: toStr(item.folioNumber || item.folioNo),
          developerName: toStr(item.developerName),
        }));
      } catch (fbErr) {
        console.warn("Fallback search error:", fbErr);
        return [];
      }
    }

    const data = await handleApiResponse(response);
    return Array.isArray(data)
      ? data.map((item) => ({
          ...item,
          projectName: toStr(item.projectName),
          accountNumber: toStr(item.accountNumber),
          folioNumber: toStr(item.folioNumber),
          developerName: toStr(item.developerName),
        }))
      : [];
  } catch (error) {
    console.error('Error fetching developer suggestions:', error);
    return [];
  }
};

/**
 * Get active NCRE tariff descriptions for dropdown
 * @returns {Promise<Array<string>>} List of active tariff descriptions
 */
export const getNcreTariffDescriptions = async () => {
  try {
    const url = apiPath("/api/v1/ncre-tariff-descriptions");
    const response = await fetch(url, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const data = await handleApiResponse(response);
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format: expected an array of tariff descriptions");
    }
    return data;
  } catch (error) {
    console.error("Error fetching NCRE tariff descriptions:", error);
    throw new Error(`Failed to fetch tariff descriptions: ${error.message}`);
  }
};

/**
 * Get active NCRE types for dropdowns
 * @returns {Promise<Array<{typeId: string, typeName: string}>>} List of active NCRE types
 */
export const getNcreTypes = async () => {
  try {
    const url = apiPath("/api/v1/ncre-types");
    const response = await fetch(url, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const data = await handleApiResponse(response);
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format: expected an array of NCRE types");
    }
    return data;
  } catch (error) {
    console.error("Error fetching NCRE types:", error);
    throw new Error(`Failed to fetch NCRE types: ${error.message}`);
  }
};

/**
 * Get active NCRE agreement types for dropdowns
 * @returns {Promise<Array<{aggTypeId: string, aggTypeName: string}>>} List of active agreement types
 */
export const getNcreAgreementTypes = async () => {
  try {
    const url = apiPath("/api/v1/ncre-agreement-types");
    const response = await fetch(url, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const data = await handleApiResponse(response);
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format: expected an array of agreement types");
    }
    return data;
  } catch (error) {
    console.error("Error fetching NCRE agreement types:", error);
    throw new Error(`Failed to fetch agreement types: ${error.message}`);
  }
};

/**
 * Get active grid substations, optionally filtered by region license code
 * @param {string} licenseCode - Region license code (e.g. "R1") to filter by
 * @returns {Promise<Array<{gssId, licenseCode, gssCode, gssName}>>} List of active grid substations
 */
export const getGridSubstations = async (licenseCode) => {
  try {
    const url = licenseCode && String(licenseCode).trim() !== ""
      ? apiPath(`/api/v1/grid-substations/region/${encodeURIComponent(String(licenseCode).trim())}`)
      : apiPath("/api/v1/grid-substations");
    const response = await fetch(url, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const data = await handleApiResponse(response);
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format: expected an array of grid substations");
    }
    return data;
  } catch (error) {
    console.error("Error fetching grid substations:", error);
    throw new Error(`Failed to fetch grid substations: ${error.message}`);
  }
};

/**
 * Get active payment deduction types (e.g. Loyalty, ESCROW, Treasury, Mahaveli)
 * @returns {Promise<Array<{dedTypeNm: string}>>} List of active payment deduction types
 */
export const getPaymentDeductionTypes = async () => {
  try {
    const url = apiPath("/api/v1/payment-deduction-types");
    const response = await fetch(url, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const data = await handleApiResponse(response);
    if (!Array.isArray(data)) {
      throw new Error("Invalid response format: expected an array of payment deduction types");
    }
    return data;
  } catch (error) {
    console.error("Error fetching payment deduction types:", error);
    throw new Error(`Failed to fetch payment deduction types: ${error.message}`);
  }
};

