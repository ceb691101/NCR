import {apiPath} from '../config';

//API endpoint
const API_BASE_URL = apiPath('/api/v1/tmp-payments');

//handle API response
const handleApiResponse = async(response) => {
if (!response.ok){
    const errorData = await response.json().catch(() =>({}));
    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
}
return response.json();
}

//Helper function to handle authorization

const getAuthHeaders = () => {
    const sessionId = sessionStorage.getItem('session_id') || localStorage.getItem('session_id');
    const headers = {
        'Content-Type':'application/json',
        'Authorization': 'Basic ' + btoa('user:admin123'),
    };
    if (sessionId) {
        headers['X-Session-Id'] = sessionId;
    }
    return headers;
};

//Get all tmp_payments with pagination
export const getAllTempPayments = async (page = 0, size = 25, sortBy = 'creditDate', sortDir = 'desc') => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        
        const response = await fetch(`${API_BASE_URL}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result;
    } catch (error) {
        console.error('Error fetching all temp payments:', error);
        throw new Error(`Failed to fetch temp payments: ${error.message}`);
    }
};

// Get payments by area code with pagination
export const getTempPaymentsByAreaCd = async (areaCd, page = 0, size = 25, sortBy = 'creditDate', sortDir = 'desc') => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCd}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result;
    } catch (error) {
        console.error('Error fetching temp payments by area code:', error);
        throw new Error(`Failed to fetch temp payments: ${error.message}`);
    }
};

// Get payments by account number with pagination
export const getTempPaymentsByAccNbr = async (accNbr, page = 0, size = 25, sortBy = 'creditDate', sortDir = 'desc') => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        
        const response = await fetch(`${API_BASE_URL}/account/${accNbr}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result;
    } catch (error) {
        console.error('Error fetching temp payments by account number:', error);
        throw new Error(`Failed to fetch temp payments: ${error.message}`);
    }
};

// Get dropdown options for form fields
export const getDropdownOptions = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/options`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.data || { agentCodes: [], centerCodes: [], counters: [], payModes: [] };
    } catch (error) {
        console.error('Error fetching dropdown options:', error);
        throw new Error(`Failed to fetch dropdown options: ${error.message}`);
    }
};

// Create new temporary payment
export const createTempPayment = async (paymentData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(paymentData),
        });
        
        const result = await handleApiResponse(response);
        return result.payment || result;
    } catch (error) {
        console.error('Error creating temp payment:', error);
        throw new Error(`Failed to create temp payment: ${error.message}`);
    }
};

// Update temporary payment
export const updateTempPayment = async (paymentData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'PUT',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(paymentData),
        });
        
        const result = await handleApiResponse(response);
        return result.payment || result;
    } catch (error) {
        console.error('Error updating temp payment:', error);
        throw new Error(`Failed to update temp payment: ${error.message}`);
    }
};
