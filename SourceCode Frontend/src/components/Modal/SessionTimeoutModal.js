import React from "react";
import PropTypes from "prop-types";

/**
 * SessionTimeoutModal Component
 *
 * Displays a countdown warning modal before automatic session logout.
 */
const SessionTimeoutModal = ({
  isOpen,
  remainingSeconds,
  onStayLoggedIn,
  onLogout,
}) => {
  if (!isOpen) return null;

  // Format remaining seconds to MM:SS
  const formatTime = (totalSeconds) => {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto" role="dialog" aria-modal="true">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        {/* Backdrop overlay */}
        <div
          className="fixed inset-0 bg-ink-900 bg-opacity-60 transition-opacity"
          aria-hidden="true"
        />

        {/* Trick to center modal on desktop */}
        <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">
          &#8203;
        </span>

        {/* Modal content panel */}
        <div className="ds-modal sm:max-w-md animate-fade-in">
          <div className="bg-white px-6 pt-6 pb-4 sm:p-7">
            <div className="flex flex-col items-center text-center">
              {/* Animated Warning / Clock Icon */}
              <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-warning-100 text-warning-600 mb-4 animate-pulse">
                <i className="fas fa-hourglass-half text-base"></i>
              </div>

              {/* Title */}
              <h3 className="text-xl font-bold text-ink-800 mb-2">
                Session Timeout Warning
              </h3>

              {/* Description */}
              <p className="text-sm text-ink-500 mb-5 leading-relaxed">
                You have been inactive for a while. For security reasons, your session will automatically expire in:
              </p>

              {/* Countdown Badge */}
              <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-warning-50 border border-warning-200 text-warning-800 font-mono text-2xl font-bold tracking-wider shadow-inner mb-2">
                <i className="far fa-clock text-warning-600 text-lg"></i>
                <span>{formatTime(remainingSeconds)}</span>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="ds-modal-footer">
            <button
              type="button"
              onClick={onLogout}
              className="w-full sm:w-auto inline-flex justify-center items-center px-4 py-2.5 rounded-xl border border-ink-300 bg-white text-sm font-semibold text-ink-700 hover:bg-ink-100 focus:outline-none focus:ring-2 focus:ring-ink-400 transition-colors"
            >
              <i className="fas fa-sign-out-alt mr-2 text-ink-500"></i>
              Log Out Now
            </button>
            <button
              type="button"
              onClick={onStayLoggedIn}
              className="w-full sm:w-auto inline-flex justify-center items-center px-5 py-2.5 rounded-xl border border-transparent shadow-md bg-navy-600 text-sm font-semibold text-white hover:bg-navy-700 focus:outline-none focus:ring-2 focus:ring-navy-500 transition-all hover:shadow-lg"
            >
              <i className="fas fa-redo-alt mr-2 text-white"></i>
              Stay Logged In
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

SessionTimeoutModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  remainingSeconds: PropTypes.number.isRequired,
  onStayLoggedIn: PropTypes.func.isRequired,
  onLogout: PropTypes.func.isRequired,
};

export default SessionTimeoutModal;
