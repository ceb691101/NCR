import { useState, useEffect, useRef, useCallback } from "react";

/**
 * useIdleTimer Hook
 *
 * @param {Object} options
 * @param {number} options.timeout - Total inactivity time before logout in milliseconds (default: 15 mins)
 * @param {number} options.warningDuration - Time before timeout to show warning in milliseconds (default: 2 mins)
 * @param {Function} options.onTimeout - Function invoked when the countdown expires
 * @param {boolean} options.enabled - Whether idle tracking is currently active (e.g. when user is authenticated)
 */
export const useIdleTimer = ({
  timeout = 1 * 60 * 1000, //1 minute
  warningDuration = 0.25 * 60 * 1000, // 0.25 minutes
  onTimeout,
  enabled = false,
}) => {
  const [showWarning, setShowWarning] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState(Math.floor(warningDuration / 1000));

  const lastActiveRef = useRef(Date.now());
  const timerCheckIntervalRef = useRef(null);
  const countdownIntervalRef = useRef(null);
  const onTimeoutRef = useRef(onTimeout);

  // Keep ref up to date with latest onTimeout callback
  useEffect(() => {
    onTimeoutRef.current = onTimeout;
  }, [onTimeout]);

  // Reset the active timestamp & broadcast to other tabs
  const recordActivity = useCallback(() => {
    const now = Date.now();
    // Throttle writing to localStorage to at most once every 2 seconds
    if (now - lastActiveRef.current > 2000) {
      lastActiveRef.current = now;
      try {
        localStorage.setItem("ncre_last_active", now.toString());
      } catch (e) {
        // Ignore localStorage errors (e.g. private mode quota)
      }
    } else {
      lastActiveRef.current = now;
    }
  }, []);

  // "Stay Logged In" handler
  const stayLoggedIn = useCallback(() => {
    const now = Date.now();
    lastActiveRef.current = now;
    try {
      localStorage.setItem("ncre_last_active", now.toString());
    } catch (e) {}

    setShowWarning(false);
    setRemainingSeconds(Math.floor(warningDuration / 1000));

    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }
  }, [warningDuration]);

  // Track user events
  useEffect(() => {
    if (!enabled) {
      setShowWarning(false);
      return;
    }

    const events = ["mousedown", "mousemove", "keydown", "scroll", "touchstart", "click"];

    const handleUserActivity = () => {
      // If warning modal is showing, activity alone does not auto-dismiss without clicking "Stay Logged In"
      // to ensure user is deliberately aware of their session state
      if (!showWarning) {
        recordActivity();
      }
    };

    events.forEach((event) => {
      window.addEventListener(event, handleUserActivity, { passive: true });
    });

    // Cross-tab synchronization via StorageEvent
    const handleStorageChange = (e) => {
      if (e.key === "ncre_last_active" && e.newValue) {
        const remoteTime = Number(e.newValue);
        if (!isNaN(remoteTime)) {
          lastActiveRef.current = remoteTime;
          // If another tab was active, dismiss warning in this tab
          setShowWarning(false);
          if (countdownIntervalRef.current) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
          }
        }
      }
    };

    window.addEventListener("storage", handleStorageChange);

    return () => {
      events.forEach((event) => {
        window.removeEventListener(event, handleUserActivity);
      });
      window.removeEventListener("storage", handleStorageChange);
    };
  }, [enabled, recordActivity, showWarning]);

  // Main background interval to evaluate idle duration
  useEffect(() => {
    if (!enabled) {
      if (timerCheckIntervalRef.current) clearInterval(timerCheckIntervalRef.current);
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
      return;
    }

    // Initialize last active time
    lastActiveRef.current = Date.now();

    timerCheckIntervalRef.current = setInterval(() => {
      const now = Date.now();
      const elapsed = now - lastActiveRef.current;
      const warningThreshold = timeout - warningDuration;

      // Check if we entered the warning window
      if (elapsed >= warningThreshold && elapsed < timeout) {
        const remainingMs = timeout - elapsed;
        const secondsLeft = Math.max(0, Math.ceil(remainingMs / 1000));

        setShowWarning(true);
        setRemainingSeconds(secondsLeft);
      } else if (elapsed >= timeout) {
        // Timed out!
        setShowWarning(false);
        if (timerCheckIntervalRef.current) clearInterval(timerCheckIntervalRef.current);
        if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);

        if (onTimeoutRef.current) {
          onTimeoutRef.current();
        }
      } else {
        // Still active
        setShowWarning(false);
      }
    }, 1000);

    return () => {
      if (timerCheckIntervalRef.current) clearInterval(timerCheckIntervalRef.current);
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    };
  }, [enabled, timeout, warningDuration]);

  return {
    showWarning,
    remainingSeconds,
    stayLoggedIn,
  };
};

export default useIdleTimer;
