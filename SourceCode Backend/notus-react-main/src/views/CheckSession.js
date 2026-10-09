import React, { useCallback, useEffect } from "react";
import { useHistory } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../context/AuthContext";
import { useIdleTimer } from "../hooks/useIdleTimer";
import SessionTimeoutModal from "../components/Modal/SessionTimeoutModal";
import { apiPath } from "../config";

/**
 * SessionCheck Component
 *
 * Supervises user session activity:
 * 1. Tracks user inactivity with configurable idle timeout.
 * 2. Displays a countdown warning modal 2 minutes before auto-logout.
 * 3. Synchronizes session state and logout events across multiple browser tabs.
 * 4. Gracefully logs out and redirects to /auth/login on timeout.
 */
const SessionCheck = () => {
  const history = useHistory();
  const { isAuthenticated, logout } = useAuth();

  // Handle timeout expiry
  const handleTimeout = useCallback(async () => {
    try {
      // Broadcast logout event to other tabs
      localStorage.setItem("ncre_logout_event", Date.now().toString());
    } catch (e) {}

    try {
      await logout();
    } catch (err) {
      console.error("Error during auto-logout:", err);
    }

    sessionStorage.clear();
    localStorage.removeItem("ceb_area_bill");
    toast.warn("You have been logged out due to inactivity.", {
      position: "top-right",
      autoClose: 5000,
    });
    history.push("/auth/login");
  }, [history, logout]);

  // Configure idle timer: 15 minutes total, 2 minutes warning countdown
  const { showWarning, remainingSeconds, stayLoggedIn } = useIdleTimer({
    timeout: 15 * 60 * 1000, // 15 minutes
    warningDuration: 2 * 60 * 1000, // 2 minutes
    onTimeout: handleTimeout,
    enabled: Boolean(isAuthenticated || (typeof sessionStorage !== "undefined" && sessionStorage.getItem("session_id"))),
  });

  // Handle "Stay Logged In" click
  const handleStayLoggedIn = useCallback(async () => {
    stayLoggedIn();

    // Ping backend session validation endpoint to keep backend/Redis session alive
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");

    if (sessionId && userId) {
      try {
        await fetch(apiPath("/api/v1/secinfo/validate-session"), {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            session_id: sessionId,
            user_id: userId,
          }),
        });
      } catch (err) {
        console.warn("Could not refresh backend session:", err);
      }
    }
  }, [stayLoggedIn]);

  // Handle manual logout from modal
  const handleModalLogout = useCallback(async () => {
    try {
      localStorage.setItem("ncre_logout_event", Date.now().toString());
    } catch (e) {}

    try {
      await logout();
    } catch (err) {
      console.error("Logout error:", err);
    }

    sessionStorage.clear();
    localStorage.removeItem("ceb_area_bill");
    toast.info("Logged out successfully.");
    history.push("/auth/login");
  }, [history, logout]);

  // Listen for logout events dispatched by other tabs
  useEffect(() => {
    const handleStorage = (e) => {
      if (e.key === "ncre_logout_event" && e.newValue) {
        try {
          sessionStorage.clear();
          localStorage.removeItem("ceb_area_bill");
          logout();
        } catch (err) {}
        history.push("/auth/login");
      }
    };

    window.addEventListener("storage", handleStorage);
    return () => {
      window.removeEventListener("storage", handleStorage);
    };
  }, [history, logout]);

  return (
    <SessionTimeoutModal
      isOpen={showWarning}
      remainingSeconds={remainingSeconds}
      onStayLoggedIn={handleStayLoggedIn}
      onLogout={handleModalLogout}
    />
  );
};

export default SessionCheck;