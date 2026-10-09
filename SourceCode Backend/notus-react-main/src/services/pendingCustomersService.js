// pendingCustomersService
import { apiPath } from '../config';
import { getPendingReadingsForArea } from './readingStatusService';

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

// Get pending readings for area (customers without readings)
export const getPendingReadingsByAreaCd = async (areaCode) => {
    try {
        const response = await getPendingReadingsForArea(areaCode);
        
        if (response.success && response.pending_readings) {
            // Transform to match expected format
const pendingCustomers = response.pending_readings.pending_customers || [];
            
            return {
                customers: pendingCustomers.map(customer => ({
                    acc_nbr: customer.acc_nbr,
                    name: customer.name,
                    facility_name: customer.facility_name,
                    folio_no: customer.folio_no,
                    area_cd: customer.area_cd,
                    bill_cycle: customer.bill_cycle,
                    address_l1: customer.address_l1,
                    mobile_no: customer.mobile_no,
                    tariff_desc: customer.tariff_desc || customer.tariff_type || null,
                    tariff_type: customer.tariff_desc || customer.tariff_type || null,
                    responsible_ee: customer.responsible_ee || customer.responsble_ee || null,
                    has_reading: customer.has_reading === true
                })),
                area_code: response.pending_readings.area_code,
                area_name: response.pending_readings.area_name,
                active_bill_cycle: response.pending_readings.active_bill_cycle,
                pending_count: response.pending_readings.pending_count || pendingCustomers.length
            };
        } else {
            return {
                customers: [],
                area_code: areaCode,
                area_name: null,
                active_bill_cycle: null,
                pending_count: 0
            };
        }
    } catch (error) {
        console.error('Error fetching pending readings:', error);
        throw new Error(`Failed to fetch pending readings: ${error.message}`);
    }
};

// Get all pending readings for user (based on access level)
export const getAllPendingReadings = async () => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const response = await fetch(`${API_BASE_URL}/user-reading-status`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({
                session_id: sessionId,
                user_id: userId,
                include_customer_details: true,
                include_reading_details: false
            })
        });
        
        const result = await handleApiResponse(response);
        
        if (result.success && result.area_reading_status) {
            // Extract all developers (both with and without readings) from all areas
            const allPendingCustomers = [];
            
            result.area_reading_status.forEach(area => {
                const withoutReadings = area.customers_without_readings_list || [];
                const withReadings = area.customers_with_readings_list || [];

                withoutReadings.forEach(customer => {
                    allPendingCustomers.push({
                        ...customer,
                        has_reading: customer.has_reading === true,
                        area_name: area.area_name
                    });
                });

                withReadings.forEach(customer => {
                    allPendingCustomers.push({
                        ...customer,
                        has_reading: true,
                        area_name: area.area_name
                    });
                });
            });
            
            return {
                customers: allPendingCustomers,
                total_pending: allPendingCustomers.length,
                areas: result.area_reading_status
            };
        }
        
        return {
            customers: [],
            total_pending: 0,
            areas: []
        };
    } catch (error) {
        console.error('Error fetching all pending readings:', error);
        throw new Error(`Failed to fetch pending readings: ${error.message}`);
    }
};

// Create a new meter reading (placeholder - would need actual endpoint)
export const createMeterReading = async (readingData) => {
    try {
        // This would be the actual endpoint for creating readings
        // For now, we'll simulate the API call
        const response = await fetch(apiPath('/api/v1/tmp-readings'), {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify({
                acc_nbr: readingData.acc_nbr,
                area_cd: readingData.area_cd,
                bill_cycle: readingData.bill_cycle,
                meter_reading: readingData.meter_reading,
                reading_date: readingData.reading_date,
                meter_type: readingData.meter_type || 'KWH',
                user_id: sessionStorage.getItem('user_id')
            }),
        });
        
        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error creating meter reading:', error);
        throw new Error(`Failed to create meter reading: ${error.message}`);
    }
};

// Get customer details for reading insertion
export const getCustomerForReading = async (accNbr) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        const response = await fetch(apiPath(`/api/v1/bulk-customers/account/${accNbr}?session_id=${sessionId}&user_id=${userId}`), {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.customer || result;
    } catch (error) {
        console.error('Error fetching customer details:', error);
        throw new Error(`Failed to fetch customer details: ${error.message}`);
    }
};

// Transform pending customer data for display
export const transformPendingCustomerData = (customers) => {
    if (!customers || !Array.isArray(customers)) return [];
    
    return customers.map(customer => ({
        acc_nbr: customer.acc_nbr,
        name: customer.name || 'N/A',
        facility_name: customer.facility_name || 'N/A',
        folio_no: customer.folio_no || 'N/A',
        area_cd: customer.area_cd,
        bill_cycle: customer.bill_cycle,
        address_l1: customer.address_l1,
        mobile_no: customer.mobile_no,
        tel_nbr: customer.tel_nbr,
        tariff: customer.tariff,
        has_reading: false
    }));
};