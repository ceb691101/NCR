import { apiPath } from '../config';

const BASE_URL = '/api/v1/change-ru';

export const searchDevelopers = async (searchType, query) => {
    const sessionId = sessionStorage.getItem("session_id");
    const response = await fetch(apiPath(`${BASE_URL}/search?type=${searchType}&query=${query}`), {
        headers: {
            'X-Session-Id': sessionId
        }
    });
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || 'Failed to search developers');
    }
    return data;
};

export const getDeveloperDetails = async (folioNo) => {
    const sessionId = sessionStorage.getItem("session_id");
    const response = await fetch(apiPath(`${BASE_URL}/${folioNo}`), {
        headers: {
            'X-Session-Id': sessionId
        }
    });
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || 'Failed to fetch developer details');
    }
    return data;
};

export const updateRuDifference = async (folioNo, newRu) => {
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");
    
    if (!sessionId || !userId) {
        throw new Error("Session not found. Please login again.");
    }
    
    const response = await fetch(apiPath(`${BASE_URL}/update`), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Session-Id': sessionId
        },
        body: JSON.stringify({
            folioNo: parseInt(folioNo, 10),
            newRu: parseInt(newRu, 10),
            sessionId,
            userId
        })
    });
    
    const data = await response.json();
    if (!response.ok || !data.success) {
        throw new Error(data.message || 'Failed to update RU Difference');
    }
    return data;
};
