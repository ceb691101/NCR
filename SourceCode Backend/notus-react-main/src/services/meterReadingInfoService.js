// ReadingsEntry meterReadingInfoService
import { apiPath } from '../config';
import { formatBillMonth } from './pendingReadingsService';

const API_BASE_URL = apiPath('/api/v1/meter-reading-info');

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

// Get meter reading information for a single customer
export const getMeterReadingInfo = async (accountNumber, areaCode, billCycle) => {
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

        const response = await fetch(`${API_BASE_URL}/customer`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching meter reading info:', error);
        throw new Error(`Failed to fetch meter reading info: ${error.message}`);
    }
};

// Edit meter readings (for received readings)
export const editMeterReadings = async (editData) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const response = await fetch(apiPath('/api/v1/meter-reading-info/customer/edit'), {
            method: 'PUT',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({
                session_id: sessionId,
                user_id: userId,
                account_number: editData.account_number,
                area_code: editData.area_code,
                bill_cycle: editData.bill_cycle,
                reading_date: editData.reading_date,
                meter_readings: editData.meter_readings || []
            })
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error editing meter readings:', error);
        throw new Error(`Failed to edit meter readings: ${error.message}`);
    }
};

// Helper function to prepare edit data - FIXED VERSION
export const prepareMeterReadingEditData = (formData, editedFields) => {
    const meterReadings = [];
    
    // Check which meter reading fields were edited
    const meterTypes = ['kwh_offp', 'kwh_day', 'kwh_peak', 'kva', 'kvah'];
    
    // In prepareMeterReadingEditData function, update KVAH handling
meterTypes.forEach(type => {
    const presentReadingKey = `${type}_presentread`;
    if (editedFields[presentReadingKey] !== undefined) {
        // Fix meter type mapping
        let meterType;
        let presentReadingValue;
        
        switch(type) {
            case 'kwh_offp':
                meterType = 'KWO';
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
                break;
            case 'kwh_day':
                meterType = 'KWD';
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
                break;
            case 'kwh_peak':
                meterType = 'KWP';
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
                break;
            case 'kva':
                meterType = 'KVA';
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
                break;
            case 'kvah':
                meterType = 'KVAH';
                // For KVAH, the "present reading" field actually contains the units value
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
                break;
            default:
                meterType = type.toUpperCase();
                presentReadingValue = parseFloat(editedFields[presentReadingKey]) || 0;
        }
        
        meterReadings.push({
            meter_type: meterType,
            present_reading: presentReadingValue
        });
    }
});
    
    // Convert reading_date from DD/MM/YYYY to YYYY-MM-DD format
    let formattedReadingDate = formData.reading_date;
    if (formData.reading_date && formData.reading_date.includes('/')) {
        const parts = formData.reading_date.split('/');
        if (parts.length === 3) {
            formattedReadingDate = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
    }
    
    return {
        account_number: formData.account_number,
        area_code: formData.area_code_number,
        bill_cycle: formData.current_billcycle_number,
        reading_date: formattedReadingDate,
        meter_readings: meterReadings
    };
};

// Update the existing getMeterReadingInfo function to include edit capabilities
export const getMeterReadingInfoWithEditSupport = async (accountNumber, areaCode, billCycle = null) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const response = await fetch(apiPath('/api/v1/meter-reading-info/customer'), {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({
                session_id: sessionId,
                user_id: userId,
                account_number: accountNumber,
                area_code: areaCode,
                bill_cycle: billCycle
            })
        });
        
        const result = await handleApiResponse(response);
        
        if (result.success && result.meter_reading_info) {
            return {
                success: true,
                meterReadingInfo: result.meter_reading_info,
                canEdit: result.meter_reading_info.reading_status === 'RECEIVED'
            };
        } else {
            return {
                success: false,
                message: result.message || 'Failed to fetch meter reading info',
                canEdit: false
            };
        }
    } catch (error) {
        console.error('Error fetching meter reading info:', error);
        throw new Error(`Failed to fetch meter reading information: ${error.message}`);
    }
};

// Get bulk meter reading information for multiple customers
export const getBulkMeterReadingInfo = async (areaCode, accountNumbers = null, billCycle = null) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const requestBody = {
            session_id: sessionId,
            user_id: userId,
            area_code: areaCode,
            account_numbers: accountNumbers,
            bill_cycle: billCycle
        };

        const response = await fetch(`${API_BASE_URL}/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching bulk meter reading info:', error);
        throw new Error(`Failed to fetch bulk meter reading info: ${error.message}`);
    }
};

// Get meter reading information for all customers in an area
export const getMeterReadingInfoForArea = async (areaCode, billCycle = null) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const params = new URLSearchParams();
        params.append('session_id', sessionId);
        params.append('user_id', userId);
        if (billCycle) params.append('bill_cycle', billCycle);

        const response = await fetch(`${API_BASE_URL}/area/${areaCode}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching area meter reading info:', error);
        throw new Error(`Failed to fetch area meter reading info: ${error.message}`);
    }
};

// Transform backend meter reading data to frontend format
export const transformMeterReadingToFrontend = (backendData) => {
    if (!backendData || !backendData.meter_reading_info) {
        return null;
    }

    const info = backendData.meter_reading_info;
    
    // Initialize frontend form data structure
    const frontendData = {
        account_number: info.account_number || "",
        folio_no: info.folio_no || "",
        tariff_type: info.tariff_type || "",
        tariff_value: info.tariff_value || "",
        meter_number: info.meter_number || "",
        customer_category: info.customer_category || "",
        area_code_number: info.area_code || "",
        area_code_name: info.area_name || "",
        current_billcycle_number: info.current_bill_cycle || "",
        current_billcycle_date: formatBillCycleDate(info.bill_cycle_date),
        bill_month: formatBillMonth(
            info.current_bill_cycle,
            info.bill_month,
            info.bill_year,
            info.bill_cycle_date
        ),
        reading_date: formatDate(info.reading_date),
        previous_reading_date: formatDate(info.previous_reading_date),
        has_reading: info.has_reading || false,
        reading_status: info.reading_status || "PENDING",
        accept_ru: info.accept_ru !== undefined ? info.accept_ru : (info.acceptRu !== undefined ? info.acceptRu : null)
    };

    // Initialize meter type fields with defaults
    const meterTypeDefaults = {
        kwh_offp: { type: 'KWO', prefix: 'kwh_offp' },
        kwh_day: { type: 'KWD', prefix: 'kwh_day' },
        kwh_peak: { type: 'KWP', prefix: 'kwh_peak' },
        kwh_tot: { type: 'KWT', prefix: 'kwh_tot' }
    };

    // Initialize all meter fields to empty
    Object.values(meterTypeDefaults).forEach(({ prefix }) => {
        frontendData[`${prefix}_meternum`] = "";
        frontendData[`${prefix}_presentread`] = "";
        frontendData[`${prefix}_previousread`] = "";
        frontendData[`${prefix}_units`] = "";
    });

    // Populate meter type data from backend
    if (info.meter_types && Array.isArray(info.meter_types)) {
        info.meter_types.forEach(meter => {
            const meterType = meter.meter_type?.trim();
            const meterConfig = Object.values(meterTypeDefaults).find(config => config.type === meterType);
            
            if (meterConfig) {
                const prefix = meterConfig.prefix;
                frontendData[`${prefix}_meternum`] = meter.meter_number || "";
                frontendData[`${prefix}_presentread`] = meter.present_reading?.toString() || "";
                frontendData[`${prefix}_previousread`] = meter.previous_reading?.toString() || "";
                frontendData[`${prefix}_units`] = meter.units?.toString() || "";
            }
        });
    }

    return frontendData;
};

// Helper function to format dates for display
const formatDate = (dateString) => {
    if (!dateString) return "";
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB');
    } catch (error) {
        return dateString;
    }
};

// Helper function to format bill cycle date
const formatBillCycleDate = (dateString) => {
    if (!dateString) return "";
    try {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.toLocaleDateString('en-US', { month: 'short' });
        return `${year} ${month}`;
    } catch (error) {
        return dateString;
    }
};

// Get meter types mapping
export const getMeterTypesMapping = () => {
    return {
        'KWO': { label: 'KWH (Off Peak)', prefix: 'kwh_offp' },
        'KWD': { label: 'KWH (Day)', prefix: 'kwh_day' },
        'KWP': { label: 'KWH (Peak)', prefix: 'kwh_peak' },
        'KWT': { label: 'KWH (Total Export)', prefix: 'kwh_tot' }
    };
};

// Validate meter reading info data
export const validateMeterReadingInfo = (data) => {
    const errors = [];
    
    if (!data.account_number) {
        errors.push("Account number is required");
    }
    
    if (!data.area_code_number) {
        errors.push("Area code is required");
    }
    
    if (!data.current_billcycle_number) {
        errors.push("Bill cycle is required");
    }
    
    return {
        isValid: errors.length === 0,
        errors: errors
    };
};

// Health check for the service
export const checkServiceHealth = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/health`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Service health check failed:', error);
        throw new Error(`Service health check failed: ${error.message}`);
    }
};