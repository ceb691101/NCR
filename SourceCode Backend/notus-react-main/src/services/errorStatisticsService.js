import { apiPath } from "../config";

const API_BASE_URL = apiPath("/api/v1/error-statistics");

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

// Get error details with complete meter readings for a specific account
export const getErrorDetailsWithMeterReadings = async (
  accountNumber,
  areaCode,
  billCycle,
  errorCode
) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      area_code: areaCode,
      bill_cycle: billCycle,
      error_code: errorCode,
    };

    const response = await fetch(
      `${API_BASE_URL}/error-details-with-readings`,
      {
        method: "POST",
        headers: getAuthHeaders(),
        credentials: "include",
        body: JSON.stringify(requestBody),
      }
    );

    const result = await handleApiResponse(response);
    return result;
  } catch (error) {
    console.error("Error fetching error details with readings:", error);
    throw new Error(`Failed to fetch error details: ${error.message}`);
  }
};

// Get error statistics for an area
export const getErrorStatistics = async (areaCode, billCycle) => {
  try {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (!sessionId || !userId) {
      throw new Error("Session not found. Please login again.");
    }

    const requestBody = {
      session_id: sessionId,
      user_id: userId,
      area_code: areaCode,
      bill_cycle: billCycle,
    };

    const response = await fetch(`${API_BASE_URL}/area-statistics`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(requestBody),
    });

    const result = await handleApiResponse(response);
    return result;
  } catch (error) {
    console.error("Error fetching error statistics:", error);
    throw new Error(`Failed to fetch error statistics: ${error.message}`);
  }
};
