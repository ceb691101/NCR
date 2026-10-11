// FILE: src\services\meterReadingUpdateService.js
import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/meter-reading-update');

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

// Update meter readings API call
export const updateMeterReadings = async (updateData) => {
  try {
    const response = await fetch(`${API_BASE_URL}/update`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      credentials: 'include',
      body: JSON.stringify(updateData),
    });

    return await handleApiResponse(response);
  } catch (error) {
    console.error('Error updating meter readings:', error);
    throw new Error(`Failed to update meter readings: ${error.message}`);
  }
};