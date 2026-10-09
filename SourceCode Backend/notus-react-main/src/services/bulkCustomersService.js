import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/bulk-customers');

// Helper function to handle API responses
const handleApiResponse = async (response) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
    }
    return response.json();
};

// Helper function to create authorization headers
const getAuthHeaders = () => {
    const sessionId = sessionStorage.getItem('session_id') || localStorage.getItem('session_id');
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa('user:admin123'),
    };
    if (sessionId) {
        headers['X-Session-Id'] = sessionId;
    }
    return headers;
};

// Get all bulk customers - DEPRECATED, use by area
export const getAllBulkCustomers = async () => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        const queryString = params.toString() ? `?${params.toString()}` : '';

        const response = await fetch(`${API_BASE_URL}${queryString}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching all bulk customers:', error);
        throw new Error(`Failed to fetch bulk customers: ${error.message}`);
    }
};

// Get customer by account number
export const getBulkCustomerByAccNbr = async (accNbr) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const response = await fetch(`${API_BASE_URL}/account/${accNbr}?session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.customer || result; // Handle both wrapped and direct responses
    } catch (error) {
        console.error('Error fetching bulk customer by account number:', error);
        throw new Error(`Failed to fetch bulk customer: ${error.message}`);
    }
};

// Get customers by area code - Add session params for validation
export const getBulkCustomersByAreaCd = async (areaCd) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const response = await fetch(`${API_BASE_URL}/area/${areaCd}?session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result; // Adjusted to return the full response
    } catch (error) {
        console.error('Error fetching bulk customers by area code:', error);
        throw new Error(`Failed to fetch bulk customers: ${error.message}`);
    }
};

// Search customers
export const searchBulkCustomers = async (searchTerm) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const response = await fetch(`${API_BASE_URL}/search?searchTerm=${encodeURIComponent(searchTerm)}&session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error searching bulk customers:', error);
        throw new Error(`Failed to search bulk customers: ${error.message}`);
    }
};

// Get customers by bill cycle
export const getBulkCustomersByBillCycle = async (billCycle) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const response = await fetch(`${API_BASE_URL}/bill-cycle/${billCycle}?session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching bulk customers by bill cycle:', error);
        throw new Error(`Failed to fetch bulk customers: ${error.message}`);
    }
};

// Get distinct area codes
export const getDistinctAreaCodes = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/distinct/area-codes`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching distinct area codes:', error);
        throw new Error(`Failed to fetch distinct area codes: ${error.message}`);
    }
};

// Get distinct customer categories
export const getDistinctCusCategories = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/distinct/categories`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching distinct customer categories:', error);
        throw new Error(`Failed to fetch distinct customer categories: ${error.message}`);
    }
};

// Update customer location
export const updateBulkCustomerLocation = async (accNbr, latitude, longitude) => {
    try {
        // Dummy endpoint, replace with real one later
        const response = await fetch(`${API_BASE_URL}/updateLocation`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({ acc_nbr: accNbr, latitude, longitude })
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error updating customer location:', error);
        throw new Error(`Failed to update location: ${error.message}`);
    }
};

// Update developer status (1 = Inactive, 2 = Active)
export const updateDeveloperStatus = async (accNbr, status) => {
    try {
        const response = await fetch(`${API_BASE_URL}/update-status`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({
                acc_nbr: accNbr,
                status: status,
            }),
        });
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error updating developer status:', error);
        throw new Error(`Failed to update status: ${error.message}`);
    }
};