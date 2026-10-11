import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/insert-new-readings');

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

/**
 * Insert new meter readings into the database
 * @param {Object} data - The reading data to insert
 * @param {String} data.session_id - Session ID
 * @param {String} data.user_id - User ID
 * @param {String} data.account_number - Account number
 * @param {String} data.area_code - Area code
 * @param {String} data.bill_cycle - Bill cycle
 * @param {String} data.reading_date - Reading date (YYYY-MM-DD)
 * @param {Number} data.meter_sequence - Meter sequence
 * @param {Array} data.meter_readings - Array of meter readings
 * @param {Object} data.charges - Charge calculations
 * @returns {Promise} API response
 */
export const insertNewMeterReadings = async (data) => {
    try {
        const response = await fetch(`${API_BASE_URL}/insert`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(data),
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error inserting new meter readings:', error);
        throw new Error(`Failed to insert new meter readings: ${error.message}`);
    }
};

/**
 * Prepare data for the insert API from frontend form data
 * @param {Object} formData - Frontend form data
 * @param {Array} meterTypes - Array of meter types for this customer
 * @param {Object} charges - Calculated charges
 * @returns {Object} Data formatted for backend API
 */
export const prepareInsertData = (formData, meterTypes, charges) => {
    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');
    
    if (!sessionId || !userId) {
        throw new Error('Session not found. Please login again.');
    }

    // Format meter readings - only include meters with present readings
    const meterReadings = [];
    
    // Meter type mapping
    const meterTypeMapping = {
        'kwh_offp': 'KWO',
        'kwh_day': 'KWD',
        'kwh_peak': 'KWP',
        'kwh_tot': 'KWT'
    };

    // Process each meter type that has a present reading
    Object.entries(meterTypeMapping).forEach(([prefix, meterType]) => {
        const presentReading = formData[`${prefix}_presentread`];
        
        // Only include if present reading is provided
        if (presentReading && presentReading.trim() !== '') {
            const previousReading = parseFloat(formData[`${prefix}_previousread`]) || 0;
            const units = parseFloat(formData[`${prefix}_units`]) || 0;
            
            meterReadings.push({
                meter_type: meterType,
                meter_number: formData[`${prefix}_meternum`] || '',
                present_reading: parseFloat(presentReading),
                previous_reading: previousReading,
                units: units
            });
        }
    });

    // Convert reading date from DD/MM/YYYY to YYYY-MM-DD
    let formattedReadingDate = formData.reading_date;
    if (formData.reading_date && formData.reading_date.includes('/')) {
        const parts = formData.reading_date.split('/');
        if (parts.length === 3) {
            formattedReadingDate = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
    }

    return {
        session_id: sessionId,
        user_id: userId,
        account_number: formData.account_number,
        area_code: formData.area_code_number,
        bill_cycle: formData.current_billcycle_number,
        reading_date: formattedReadingDate,
        meter_sequence: 1,
        meter_readings: meterReadings
    };
};

/**
 * Validate if all required data is available for saving
 * @param {Object} formData - Frontend form data
 * @param {Object} charges - Calculated charges
 * @returns {Object} Validation result with isValid flag and message
 */
export const validateSaveData = (formData, charges) => {
    const errors = [];

    // Check required fields
    if (!formData.account_number || formData.account_number.trim() === '') {
        errors.push('Account number is required');
    }

    if (!formData.area_code_number || formData.area_code_number.trim() === '') {
        errors.push('Area code is required');
    }

    if (!formData.current_billcycle_number || formData.current_billcycle_number.trim() === '') {
        errors.push('Bill cycle is required');
    }

    if (!formData.reading_date || formData.reading_date.trim() === '') {
        errors.push('Reading date is required');
    }

    // Check if at least one meter reading is provided
    const hasPresentReading = [
        'kwh_offp_presentread',
        'kwh_day_presentread',
        'kwh_peak_presentread',
        'kwh_tot_presentread'
    ].some(field => {
        const value = formData[field];
        return value && value.trim() !== '' && parseFloat(value) >= 0;
    });

    if (!hasPresentReading) {
        errors.push('At least one meter reading is required');
    }

    return {
        isValid: errors.length === 0,
        errors: errors,
        message: errors.length > 0 ? errors.join(', ') : 'All validations passed'
    };
};