import { apiPath, getAuthHeaders } from "../config";

const API_BASE_URL = apiPath("/api/live-tariffs");

/**
 * Fetch the live tariff list.
 * The backend resolves the live (currently applicable) tariff rate.
 * @returns {Promise<Array<Object>>}
 */
export const getLiveTariffs = async () => {
  const response = await fetch(`${API_BASE_URL}/list`, {
    method: "GET",
    headers: {
      ...getAuthHeaders(),
      "Accept": "application/json",
    },
    credentials: "include",
  });

  if (!response.ok) {
    const text = await response.text().catch(() => "");
    let errorMessage = `Request failed with status ${response.status}`;
    if (text) {
      try {
        const json = JSON.parse(text);
        errorMessage = json.error || json.message || text;
      } catch (e) {
        errorMessage = text;
      }
    }
    throw new Error(errorMessage);
  }

  return response.json();
};