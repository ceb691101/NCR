import { apiPath } from "../config";

const API_BASE_URL = apiPath("/api/v1/accounts");

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

// Get all user accounts
export const getAllUserAccounts = async () => {
  try {
    const response = await fetch(API_BASE_URL, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching all user accounts:", error);
    throw new Error(`Failed to fetch user accounts: ${error.message}`);
  }
};

// Get all active users only
export const getActiveUserAccounts = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/active`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching active user accounts:", error);
    throw new Error(`Failed to fetch active user accounts: ${error.message}`);
  }
};

// Get all inactive users only
export const getInactiveUserAccounts = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/inactive`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching inactive user accounts:", error);
    throw new Error(`Failed to fetch inactive user accounts: ${error.message}`);
  }
};

// Get user account by ID
export const getUserAccountById = async (userId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${userId}`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    const result = await handleApiResponse(response);
    return result.user || result; // Handle both wrapped and direct responses
  } catch (error) {
    console.error("Error fetching user account by ID:", error);
    throw new Error(`Failed to fetch user account: ${error.message}`);
  }
};

// Create new user account (status automatically set to 1 - active)
export const createUserAccount = async (accountData) => {
  try {
    // Transform frontend data to match backend DTO structure
    const backendData = {
      user_id: accountData.user_id,
      user_name: accountData.user_name,
      user_cat: accountData.user_cat,
      epf_num: accountData.epf_num,
    };

    // Set location codes based on user category - PROPERLY HANDLE NULL VALUES
    if (accountData.user_cat === "Admin" ||
      accountData.user_cat === "Electrical Engineer" ||
      accountData.user_cat === "Chief Engineer" ||
      accountData.user_cat === "DGM" ||
      accountData.user_cat === "Director" ||
      accountData.user_cat === "DIRECTOR") {
      // These users have no location codes
      backendData.region_code = null;
      backendData.province_code = null;
      backendData.area_code = null;
    } else if (accountData.user_cat === "Region User") {
      // Region users only have region code
      backendData.region_code = accountData.region_code || null;
      backendData.province_code = null;
      backendData.area_code = null;
    } else if (
      accountData.user_cat === "Province User" ||
      accountData.user_cat === "Accountant Revenue" ||
      accountData.user_cat === "Acc Assistance" ||
      accountData.user_cat === "Accountant Clark"
    ) {
      // Province users have region and province codes
      backendData.region_code = accountData.region_code || null;
      backendData.province_code = accountData.province_code || null;
      backendData.area_code = null;
    } else if (accountData.user_cat === "Area User") {
      // Area users have all location codes
      backendData.region_code = accountData.region_code || null;
      backendData.province_code = accountData.province_code || null;
      backendData.area_code = accountData.area_code || null;
    }

    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(backendData),
    });

    const result = await handleApiResponse(response);
    return result.user || result; // Handle both wrapped and direct responses
  } catch (error) {
    console.error("Error creating user account:", error);
    throw new Error(`Failed to create user account: ${error.message}`);
  }
};

// Update user account - FIXED VERSION
export const updateUserAccount = async (userId, accountData) => {
  try {
    // Transform frontend data to match backend DTO structure
    const backendData = {
      user_name: accountData.user_name,
      user_cat: accountData.user_cat,
      region_code: accountData.region_code || null,
      province_code: accountData.province_code || null,
      area_code: accountData.area_code || null,
    };

    console.log("Sending update data to backend:", backendData);

    const response = await fetch(`${API_BASE_URL}/${userId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify(backendData),
    });

    const result = await handleApiResponse(response);
    return result.user || result; // Handle both wrapped and direct responses
  } catch (error) {
    console.error("Error updating user account:", error);
    throw new Error(`Failed to update user account: ${error.message}`);
  }
};

// NEW: Toggle user status (activate/deactivate)
export const toggleUserStatus = async (userId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${userId}/toggle-status`, {
      method: "PUT",
      headers: getAuthHeaders(),
      credentials: "include",
    });

    const result = await handleApiResponse(response);
    return result.user || result; // Handle both wrapped and direct responses
  } catch (error) {
    console.error("Error toggling user status:", error);
    throw new Error(`Failed to toggle user status: ${error.message}`);
  }
};

// NEW: Set user status explicitly
export const setUserStatus = async (userId, status) => {
  try {
    if (status !== 0 && status !== 1) {
      throw new Error("Status must be 0 (inactive) or 1 (active)");
    }

    const response = await fetch(`${API_BASE_URL}/${userId}/status/${status}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      credentials: "include",
    });

    const result = await handleApiResponse(response);
    return result.user || result; // Handle both wrapped and direct responses
  } catch (error) {
    console.error("Error setting user status:", error);
    throw new Error(`Failed to set user status: ${error.message}`);
  }
};

// NEW: Get user status
export const getUserStatus = async (userId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${userId}/status`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error getting user status:", error);
    throw new Error(`Failed to get user status: ${error.message}`);
  }
};

// Get users by category
export const getUsersByCategory = async (category) => {
  try {
    const response = await fetch(`${API_BASE_URL}/category/${category}`, {
      method: "GET",
      headers: getAuthHeaders(),
      credentials: "include",
    });
    return await handleApiResponse(response);
  } catch (error) {
    console.error("Error fetching users by category:", error);
    throw new Error(`Failed to fetch users by category: ${error.message}`);
  }
};

// Validate user login (only active users can login)
export const validateUserLogin = async (userId, password) => {
  try {
    const response = await fetch(`${API_BASE_URL}/validate`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: "include",
      body: JSON.stringify({
        userId: userId,
        password: password,
      }),
    });

    const result = await handleApiResponse(response);
    return result.valid || false;
  } catch (error) {
    console.error("Error validating user login:", error);
    throw new Error(`Failed to validate login: ${error.message}`);
  }
};

// Transform backend response to frontend format (UPDATED to include status)
export const transformBackendToFrontend = (backendUser) => {
  return {
    user_id: backendUser.user_id,
    user_name: backendUser.user_name,
    user_cat: backendUser.user_cat,
    epf_num: backendUser.epf_num,
    // Location codes
    region_code:
      backendUser.region_code !== undefined ? backendUser.region_code : null,
    province_code:
      backendUser.province_code !== undefined
        ? backendUser.province_code
        : null,
    area_code:
      backendUser.area_code !== undefined ? backendUser.area_code : null,
    // NEW: Include status field
    status: backendUser.status !== undefined ? backendUser.status : 1,
  };
};

// Transform frontend data to backend format (UPDATED to include status)
export const transformFrontendToBackend = (frontendUser) => {
  return {
    user_id: frontendUser.user_id,
    user_name: frontendUser.user_name,
    passwd: frontendUser.passwd,
    user_cat: frontendUser.user_cat,
    epf_num: frontendUser.epf_num,
    // Location codes
    region_code:
      frontendUser.region_code !== undefined ? frontendUser.region_code : null,
    province_code:
      frontendUser.province_code !== undefined
        ? frontendUser.province_code
        : null,
    area_code:
      frontendUser.area_code !== undefined ? frontendUser.area_code : null,
    // NEW: Include status field
    status: frontendUser.status !== undefined ? frontendUser.status : 1,
  };
};
