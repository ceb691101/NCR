import { apiPath } from "../config";

const API_URL = apiPath("/api/v1/function-management");

const getHeaders = () => {
  const sessionId = sessionStorage.getItem("session_id") || localStorage.getItem("session_id");
  if (!sessionId) throw new Error("Your login session has expired. Please sign in again.");
  return {
    "Content-Type": "application/json",
    "X-Session-Id": sessionId,
  };
};

const handleResponse = async (response) => {
  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(body.message || `Request failed (${response.status})`);
  }
  return body;
};

export const getFunctions = async () => {
  const response = await fetch(`${API_URL}/functions`, {
    headers: getHeaders(),
    credentials: "include",
  });
  return handleResponse(response);
};

export const createFunction = async (functionData) => {
  const response = await fetch(`${API_URL}/functions`, {
    method: "POST",
    headers: getHeaders(),
    credentials: "include",
    body: JSON.stringify(functionData),
  });
  return handleResponse(response);
};

export const updateFunction = async (functionData) => {
  const { applId, funcId, subFuncId } = functionData;
  const path = [applId, funcId, subFuncId].map(encodeURIComponent).join("/");
  const response = await fetch(`${API_URL}/functions/${path}`, {
    method: "PUT",
    headers: getHeaders(),
    credentials: "include",
    body: JSON.stringify(functionData),
  });
  return handleResponse(response);
};

export const getUserPermissions = async (userId, applId) => {
  const query = new URLSearchParams({ applId });
  const response = await fetch(
    `${API_URL}/users/${encodeURIComponent(userId)}/permissions?${query.toString()}`,
    { headers: getHeaders(), credentials: "include" }
  );
  return handleResponse(response);
};

export const saveUserPermissions = async (userId, applId, permissions) => {
  const response = await fetch(`${API_URL}/users/${encodeURIComponent(userId)}/permissions`, {
    method: "PUT",
    headers: getHeaders(),
    credentials: "include",
    body: JSON.stringify({ applId, permissions }),
  });
  return handleResponse(response);
};