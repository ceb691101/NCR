import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/consumption-history');

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

// Get consumption history for a single customer
export const getCustomerConsumptionHistory = async (accountNumber, cycleCount = 6, areaCode = null) => {
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
            cycle_count: cycleCount,
        };

        // Add area code if provided
        if (areaCode) {
            requestBody.area_code = areaCode;
        }

        const response = await fetch(`${API_BASE_URL}/customer`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching consumption history:', error);
        throw new Error(`Failed to fetch consumption history: ${error.message}`);
    }
};

// Get consumption history for multiple customers
export const getMultipleCustomersConsumptionHistory = async (accountNumbers, cycleCount = 6, areaCode = null) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const requestBody = {
            session_id: sessionId,
            user_id: userId,
            account_numbers: accountNumbers,
            cycle_count: cycleCount,
        };

        // Add area code if provided
        if (areaCode) {
            requestBody.area_code = areaCode;
        }

        const response = await fetch(`${API_BASE_URL}/bulk`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching multiple customers consumption history:', error);
        throw new Error(`Failed to fetch consumption history: ${error.message}`);
    }
};

// Get consumption history for an area
export const getAreaConsumptionHistory = async (areaCode, cycleCount = 6) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const response = await fetch(
            `${API_BASE_URL}/area/${areaCode}?session_id=${sessionId}&user_id=${userId}&cycle_count=${cycleCount}`,
            {
                method: 'GET',
                headers: getAuthHeaders(),
                credentials: 'include',
            }
        );

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching area consumption history:', error);
        throw new Error(`Failed to fetch area consumption history: ${error.message}`);
    }
};

// Transform consumption data for chart display
export const transformConsumptionDataForChart = (consumptionHistory) => {
    if (!consumptionHistory || !consumptionHistory.cycles) {
        return null;
    }

    // Sort cycles by bill cycle ascending for proper chart display
    const sortedCycles = [...consumptionHistory.cycles].sort((a, b) => {
        try {
            const cycleA = parseInt(a.bill_cycle) || 0;
            const cycleB = parseInt(b.bill_cycle) || 0;
            return cycleA - cycleB;
        } catch (error) {
            return a.bill_cycle.localeCompare(b.bill_cycle);
        }
    });

    return {
        labels: sortedCycles.map(cycle => `Cycle ${cycle.bill_cycle}`),
        billCycles: sortedCycles.map(cycle => cycle.bill_cycle),
        datasets: [
            {
                label: 'Off Peak',
                data: sortedCycles.map(cycle => cycle.off_peak_consumption || 0),
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 2,
                tension: 0.4,
                fill: true,
            },
            {
                label: 'Day',
                data: sortedCycles.map(cycle => cycle.day_consumption || 0),
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2,
                tension: 0.4,
                fill: true,
            },
            {
                label: 'Peak',
                data: sortedCycles.map(cycle => cycle.peak_consumption || 0),
                backgroundColor: 'rgba(255, 206, 86, 0.2)',
                borderColor: 'rgba(255, 206, 86, 1)',
                borderWidth: 2,
                tension: 0.4,
                fill: true,
            },
            {
                label: 'KVA',
                data: sortedCycles.map(cycle => cycle.kva_consumption || 0),
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 2,
                tension: 0.4,
                fill: true,
            },
        ],
        summary: {
            account_number: consumptionHistory.account_number,
            customer_name: consumptionHistory.customer_name,
            tariff: consumptionHistory.tariff,
            area_code: consumptionHistory.area_code,
            latest_bill_cycle: consumptionHistory.latest_bill_cycle,
            bill_cycle_count: consumptionHistory.bill_cycle_count,
            total_off_peak: consumptionHistory.total_off_peak,
            total_day: consumptionHistory.total_day,
            total_peak: consumptionHistory.total_peak,
            total_kva: consumptionHistory.total_kva,
            average_off_peak: consumptionHistory.average_off_peak,
            average_day: consumptionHistory.average_day,
            average_peak: consumptionHistory.average_peak,
            average_kva: consumptionHistory.average_kva,
        },
    };
};

// Format consumption value for display
export const formatConsumptionValue = (value) => {
    if (value === null || value === undefined) return '0.00';
    const numValue = typeof value === 'string' ? parseFloat(value) : value;
    return numValue.toFixed(2);
};