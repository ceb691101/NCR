import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/locations');

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
        'Authorization': 'Basic ' + btoa('user:admin123'), // Use your actual credentials
    };
    if (sessionId) {
        headers['X-Session-Id'] = sessionId;
    }
    return headers;
};

// Get all regions in ascending order (R1, R2, R3, R4)
export const getAllRegions = async () => {
    try {
    const response = await fetch(`${API_BASE_URL}/regions`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.regions || [];
    } catch (error) {
        console.error('Error fetching regions:', error);
        throw new Error(`Failed to fetch regions: ${error.message}`);
    }
};

// Get all provinces with their names
export const getAllProvinces = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/provinces`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.provinces || [];
    } catch (error) {
        console.error('Error fetching provinces:', error);
        throw new Error(`Failed to fetch provinces: ${error.message}`);
    }
};

// Get provinces by region code
export const getProvincesByRegion = async (regionCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/provinces/region/${regionCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.provinces || [];
    } catch (error) {
        console.error('Error fetching provinces by region:', error);
        throw new Error(`Failed to fetch provinces for region ${regionCode}: ${error.message}`);
    }
};

// Get all areas with their names and province names
export const getAllAreas = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/areas`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.areas || [];
    } catch (error) {
        console.error('Error fetching areas:', error);
        throw new Error(`Failed to fetch areas: ${error.message}`);
    }
};

// Get areas by region code
export const getAreasByRegion = async (regionCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/areas/region/${regionCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.areas || [];
    } catch (error) {
        console.error('Error fetching areas by region:', error);
        throw new Error(`Failed to fetch areas for region ${regionCode}: ${error.message}`);
    }
};

// Get areas by province code
export const getAreasByProvince = async (provCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/areas/province/${provCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.areas || [];
    } catch (error) {
        console.error('Error fetching areas by province:', error);
        throw new Error(`Failed to fetch areas for province ${provCode}: ${error.message}`);
    }
};

// Get areas by region and province
export const getAreasByRegionAndProvince = async (regionCode, provCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/areas/region/${regionCode}/province/${provCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.areas || [];
    } catch (error) {
        console.error('Error fetching areas by region and province:', error);
        throw new Error(`Failed to fetch areas for region ${regionCode} and province ${provCode}: ${error.message}`);
    }
};

// Get province by code
export const getProvinceByCode = async (provCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/province/${provCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.province || null;
    } catch (error) {
        console.error('Error fetching province by code:', error);
        throw new Error(`Failed to fetch province ${provCode}: ${error.message}`);
    }
};

// Get area by code
export const getAreaByCode = async (areaCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/area/${areaCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.area || null;
    } catch (error) {
        console.error('Error fetching area by code:', error);
        throw new Error(`Failed to fetch area ${areaCode}: ${error.message}`);
    }
};

// Validation functions
export const validateRegion = async (regionCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/validate/region/${regionCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.is_valid || false;
    } catch (error) {
        console.error('Error validating region:', error);
        return false;
    }
};

export const validateProvince = async (provCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/validate/province/${provCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.is_valid || false;
    } catch (error) {
        console.error('Error validating province:', error);
        return false;
    }
};

export const validateArea = async (areaCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/validate/area/${areaCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.is_valid || false;
    } catch (error) {
        console.error('Error validating area:', error);
        return false;
    }
};

export const validateRegionProvince = async (regionCode, provCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/validate/region/${regionCode}/province/${provCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.is_valid || false;
    } catch (error) {
        console.error('Error validating region-province combination:', error);
        return false;
    }
};

export const validateRegionProvinceArea = async (regionCode, provCode, areaCode) => {
    try {
        const response = await fetch(`${API_BASE_URL}/validate/region/${regionCode}/province/${provCode}/area/${areaCode}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        return result.is_valid || false;
    } catch (error) {
        console.error('Error validating region-province-area combination:', error);
        return false;
    }
};

// Transform functions for frontend compatibility
export const transformRegionsToOptions = (regions) => {
    return regions.map(region => ({
        value: region,
        label: region
    }));
};

export const transformProvincesToOptions = (provinces) => {
    return provinces.map(province => ({
        value: province.prov_code,
        label: `${province.prov_name} (${province.prov_code})`
    }));
};

export const transformAreasToOptions = (areas) => {
    return areas.map(area => ({
        value: area.area_code,
        label: `${area.area_name} (${area.area_code})`
    }));
};