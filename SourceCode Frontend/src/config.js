// Centralized API base helper for the frontend.
// Exports API_BASE (no trailing slash) and apiPath(path) helper.
const RAW_BASE = process.env.REACT_APP_API_BASE_URL || '';  // Fallback to '' for relative proxy paths
// const RAW_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';  // Use this for direct backend dev (bypasses proxy)
export const API_BASE = RAW_BASE.replace(/\/$/, '');

/* Build a full URL for API calls. */
export function apiPath(path = '') {
  if (!path) return API_BASE;
  if (path.startsWith('/')) return API_BASE + path;
  return API_BASE + '/' + path;
}

/**
 * Central helper for HTTP authorization headers with X-Session-Id
 */
export function getAuthHeaders(customHeaders = {}) {
  const sessionId = sessionStorage.getItem('session_id') || localStorage.getItem('session_id');
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Basic ' + btoa('user:admin123'),
    ...customHeaders,
  };
  if (sessionId) {
    headers['X-Session-Id'] = sessionId;
  }
  return headers;
}

const config = {
  API_BASE,
  apiPath,
  getAuthHeaders,
};

export default config;