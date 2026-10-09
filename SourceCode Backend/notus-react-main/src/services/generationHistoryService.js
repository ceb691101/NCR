import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/generation-history');

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
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa('user:admin123'),
    };
};

export const getGenerationHistory = async (accountNumber, areaCode, cycleCount = 3) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const response = await fetch(`${API_BASE_URL}/${accountNumber}/${areaCode}?cycleCount=${cycleCount}&session_id=${sessionId}&user_id=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching generation history:', error);
        throw new Error(`Failed to fetch generation history: ${error.message}`);
    }
};

export const transformGenerationDataForChart = (historyList) => {
    if (!historyList || historyList.length === 0) {
        return null;
    }

    // Backend returns oldest first, which is correct for charts
    return {
        labels: historyList.map(item => `Cycle ${item.billCycle}`),
        datasets: [
            {
                label: 'R1 (Day)',
                data: historyList.map(item => item.generationR1 || 0),
                backgroundColor: 'rgba(54, 162, 235, 0.1)', // Light blue (reduced opacity)
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6,
            },
            {
                label: 'R2 (Peak)',
                data: historyList.map(item => item.generationR2 || 0),
                backgroundColor: 'rgba(255, 159, 64, 0.1)', // Orange/Yellow (reduced opacity)
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6,
            },
            {
                label: 'R3 (Off Peak)',
                data: historyList.map(item => item.generationR3 || 0),
                backgroundColor: 'rgba(219, 112, 147, 0.1)', // Pinkish Red (reduced opacity)
                borderColor: 'rgba(219, 112, 147, 1)',
                borderWidth: 2,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6,
            }
        ],
    };
};
