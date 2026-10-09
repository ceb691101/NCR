import { apiPath, getAuthHeaders } from "../config";

// Backend resolves the permitted areas from sec_info region/province/area codes.
const API_BASE = apiPath("/api/v1/secinfo");

const getSession = () => ({
  session_id: sessionStorage.getItem("session_id") || "",
  user_id: sessionStorage.getItem("user_id") || "",
});

// The session id also travels in the X-Session-Id header: the backend's
// AuthorizationInterceptor reads the header, not the JSON body, and rejects the
// request with 403 when it cannot find one.
const postJson = async (path, body) => {
  const response = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: getAuthHeaders(),
    credentials: "include",
    body: JSON.stringify(body),
  });
  const data = await response.json().catch(() => ({}));
  return { ok: response.ok, data };
};

/**
 * Areas the logged in user is permitted to read.
 * Nothing is selected - the caller decides how to scope the data.
 */
export async function fetchPermittedAreas() {
  const session = getSession();
  if (!session.session_id || !session.user_id) {
    throw new Error("Session not found. Please login again.");
  }

  const { data } = await postJson("/permitted-areas", session);
  if (!data || !data.success) {
    throw new Error((data && data.message) || "Failed to load permitted areas");
  }

  return {
    permittedAreas: data.permitted_areas || [],
    accessScope: data.access_scope || "",
    selectedAreaCode: data.selected_area_code || null,
  };
}

/**
 * Narrow the session to a single permitted area.
 * Pass null / "" / "ALL" to go back to every permitted area.
 */
export async function selectArea(areaCode) {
  const session = getSession();
  if (!session.session_id || !session.user_id) {
    throw new Error("Session not found. Please login again.");
  }

  const { data } = await postJson("/select-area", {
    ...session,
    area_code: areaCode || null,
  });

  if (!data || !data.success) {
    throw new Error((data && data.message) || "Failed to update area selection");
  }

  return {
    selectedAreaCode: data.selected_area_code || null,
    permittedAreas: data.permitted_areas || [],
    scope: data.scope || "",
  };
}
