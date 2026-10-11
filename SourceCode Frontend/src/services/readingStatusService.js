import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/reading-status');

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

let inFlightUserStatusPromise = null;
let lastUserStatusCache = null;
let lastUserStatusCacheTime = 0;
const CACHE_TTL_MS = 3000; // 3 seconds cache/dedup for simultaneous widget mounts

// Get reading status for user based on their access level
export const getUserReadingStatus = async (includeCustomerDetails = true, includeReadingDetails = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const cacheKey = `${userId}_${includeCustomerDetails}_${includeReadingDetails}`;
        const now = Date.now();

        if (lastUserStatusCache && lastUserStatusCache.key === cacheKey && (now - lastUserStatusCacheTime < CACHE_TTL_MS)) {
            return lastUserStatusCache.data;
        }

        if (inFlightUserStatusPromise && inFlightUserStatusPromise.key === cacheKey) {
            return await inFlightUserStatusPromise.promise;
        }

        const fetchPromise = (async () => {
            const response = await fetch(`${API_BASE_URL}/user-reading-status`, {
                method: 'POST',
                headers: getAuthHeaders(),
                credentials: 'include',
                body: JSON.stringify({
                    session_id: sessionId,
                    user_id: userId,
                    include_customer_details: includeCustomerDetails,
                    include_reading_details: includeReadingDetails
                })
            });
            const data = await handleApiResponse(response);
            lastUserStatusCache = { key: cacheKey, data };
            lastUserStatusCacheTime = Date.now();
            return data;
        })();

        inFlightUserStatusPromise = { key: cacheKey, promise: fetchPromise };

        try {
            return await fetchPromise;
        } finally {
            if (inFlightUserStatusPromise && inFlightUserStatusPromise.key === cacheKey) {
                inFlightUserStatusPromise = null;
            }
        }
    } catch (error) {
        console.error('Error fetching user reading status:', error);
        throw new Error(`Failed to fetch reading status: ${error.message}`);
    }
};

// Get reading status for specific area
export const getAreaReadingStatus = async (areaCode, includeCustomerDetails = true, includeReadingDetails = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCode}?session_id=${sessionId}&user_id=${userId}&include_customer_details=${includeCustomerDetails}&include_reading_details=${includeReadingDetails}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching area reading status:', error);
        throw new Error(`Failed to fetch area reading status: ${error.message}`);
    }
};

// Get pending readings for area
export const getPendingReadingsForArea = async (areaCode) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCode}/pending?session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching pending readings:', error);
        throw new Error(`Failed to fetch pending readings: ${error.message}`);
    }
};

// Get completed readings for area
export const getCompletedReadingsForArea = async (areaCode) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCode}/completed?session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching completed readings:', error);
        throw new Error(`Failed to fetch completed readings: ${error.message}`);
    }
};

// Get reading status for province
export const getProvinceReadingStatus = async (provinceCode, includeCustomerDetails = false, includeReadingDetails = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/province/${provinceCode}?session_id=${sessionId}&user_id=${userId}&include_customer_details=${includeCustomerDetails}&include_reading_details=${includeReadingDetails}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching province reading status:', error);
        throw new Error(`Failed to fetch province reading status: ${error.message}`);
    }
};

// Get reading status for region
export const getRegionReadingStatus = async (regionCode, includeCustomerDetails = false, includeReadingDetails = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/region/${regionCode}?session_id=${sessionId}&user_id=${userId}&include_customer_details=${includeCustomerDetails}&include_reading_details=${includeReadingDetails}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching region reading status:', error);
        throw new Error(`Failed to fetch region reading status: ${error.message}`);
    }
};

// Get all reading status (Admin only)
export const getAllReadingStatus = async (includeCustomerDetails = false, includeReadingDetails = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(`${API_BASE_URL}/all?session_id=${sessionId}&user_id=${userId}&include_customer_details=${includeCustomerDetails}&include_reading_details=${includeReadingDetails}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching all reading status:', error);
        throw new Error(`Failed to fetch all reading status: ${error.message}`);
    }
};

// Helper function to determine row color based on reading status
export const getRowColor = (hasReading) => {
    if (hasReading === true) {
        return 'bg-success-50 hover:bg-success-100'; // Light green for readings received
    } else if (hasReading === false) {
        return 'bg-critical-50 hover:bg-critical-100'; // Light red for readings not received
    }
    return 'bg-white hover:bg-ink-50'; // Default
};

// Helper function to get reading status text
export const getReadingStatusText = (hasReading) => {
    if (hasReading === true) {
        return 'Received';
    } else if (hasReading === false) {
        return 'Not Received';
    }
    return 'Unknown';
};

// Helper function to get reading status badge color
export const getReadingStatusBadge = (hasReading) => {
    if (hasReading === true) {
        return 'bg-success-100 text-success-800 border-success-200';
    } else if (hasReading === false) {
        return 'bg-critical-100 text-critical-800 border-critical-200';
    }
    return 'bg-ink-100 text-ink-800 border-ink-200';
};