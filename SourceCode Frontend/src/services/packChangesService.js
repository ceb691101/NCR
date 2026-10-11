import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/pack-changes');

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

// Update pack changes for a customer
export const updatePackChanges = async (accountNumber, areaCode, billCycle, packChangesData) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const requestBody = {
            session_id: sessionId,
            user_id: userId,
            account_number: accountNumber,
            area_code: areaCode,
            bill_cycle: billCycle,
            pack_changes: {
                new_reader_code: packChangesData.new_reader_code || '',
                new_daily_pack: packChangesData.new_daily_pack || '',
                new_walk_order: packChangesData.new_walk_order || ''
            }
        };

        const response = await fetch(`${API_BASE_URL}/update`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error updating pack changes:', error);
        throw new Error(`Failed to update pack changes: ${error.message}`);
    }
};

// View pack changes for a customer
export const viewPackChanges = async (accountNumber, areaCode, billCycle) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const requestBody = {
            session_id: sessionId,
            user_id: userId,
            account_number: accountNumber,
            area_code: areaCode,
            bill_cycle: billCycle
        };

        const response = await fetch(`${API_BASE_URL}/view`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error viewing pack changes:', error);
        throw new Error(`Failed to view pack changes: ${error.message}`);
    }
};

// Check if pack changes data is valid (at least one field filled)
export const hasValidPackChanges = (packChangesData) => {
    if (!packChangesData) return false;
    
    return (
        (packChangesData.new_reader_code && packChangesData.new_reader_code.trim() !== '') ||
        (packChangesData.new_daily_pack && packChangesData.new_daily_pack.trim() !== '') ||
        (packChangesData.new_walk_order && packChangesData.new_walk_order.trim() !== '')
    );
};

// Reset pack changes data
export const resetPackChangesData = () => {
    return {
        new_reader_code: '',
        new_daily_pack: '',
        new_walk_order: ''
    };
};