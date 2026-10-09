import { apiPath, getAuthHeaders } from '../config';

// src/services/ncrePlantationService.js

const API_BASE_URL = apiPath('/api/v1/bulk-customers');

// Fetch all NCRE customers with better error handling
export const fetchNCRECustomers = async () => {
  try {
    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');
    const params = new URLSearchParams();
    if (sessionId) params.append('session_id', sessionId);
    if (userId) params.append('user_id', userId);
    const queryString = params.toString() ? `?${params.toString()}` : '';

    const response = await fetch(`${API_BASE_URL}/map-data${queryString}`, {
      method: 'GET',
      headers: getAuthHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    return data.data || [];

  } catch (error) {
    console.error('Error in fetchNCRECustomers:', error);
    throw error;
  }
};

// Fetch customers by area code
export const fetchCustomersByAreaCode = async (areaCode) => {
  try {
    const response = await fetch(`${API_BASE_URL}/area/${areaCode}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error('Failed to fetch customers');
    const data = await response.json();
    return data.data || data;
  } catch (error) {
    console.error('Error fetching customers by area:', error);
    throw error;
  }
};

// Fetch single customer by account number
export const fetchCustomerByAccountNumber = async (accountNumber) => {
  try {
    const response = await fetch(`${API_BASE_URL}/account/${accountNumber}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error('Failed to fetch customer');
    const data = await response.json();
    return data.data || data;
  } catch (error) {
    console.error('Error fetching customer:', error);
    throw error;
  }
};

const MONITORING_API_BASE_URL = apiPath('/api/v1/ncre-monitoring');

// Fetch live NCRE plant monitoring status from internal backend endpoint
export const fetchNCREPlantStatuses = async () => {
  try {
    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');
    const params = new URLSearchParams();
    if (sessionId) params.append('session_id', sessionId);
    if (userId) params.append('user_id', userId);
    const queryString = params.toString() ? `?${params.toString()}` : '';

    const response = await fetch(`${MONITORING_API_BASE_URL}/status${queryString}`, {
      method: 'GET',
      headers: getAuthHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error in fetchNCREPlantStatuses:', error);
    throw error;
  }
};