import React from "react";
import PropTypes from "prop-types";
import { useHistory } from "react-router-dom";

const ValidationStatusModal = ({
  isOpen,
  isValid,
  errors = [],
  onSubmit,
  onClose,
  isActionLoading = false,
  accountNumber,
  areaCode,
  billCycle,
}) => {
  const history = useHistory();

  if (!isOpen) return null;

  // Define checklist rules mapped to target error signatures
  const checks = [
    {
      id: "duplicate",
      name: "Duplicate Invoice Check",
      description: "Validates that no other active or approved invoice exists for the same billing cycle.",
      failed: errors.some(err => 
        err.toLowerCase().includes("already exists") || 
        err.toLowerCase().includes("duplicate") || 
        err.toLowerCase().includes("required invoice identifiers")
      ),
      getError: () => errors.find(err => 
        err.toLowerCase().includes("already exists") || 
        err.toLowerCase().includes("duplicate") || 
        err.toLowerCase().includes("required invoice identifiers")
      )
    },
    {
      id: "plant_factor",
      name: "Plant Factor Constraints Check",
      description: "Ensures the calculated plant factor is greater than 0% and less than or equal to 100%.",
      failed: errors.some(err => 
        err.toLowerCase().includes("plant factor")
      ),
      getError: () => errors.find(err => 
        err.toLowerCase().includes("plant factor")
      )
    },
    {
      id: "ru_validation",
      name: "RU Validation Check",
      description: "Verifies that the difference between total export and the sum of interval export readings is within the developer's allowed limit.",
      failed: errors.some(err => 
        err.startsWith("RU Limit Exceeded") ||
        err.toLowerCase().includes("ru validation")
      ),
      getError: () => errors.find(err => 
        err.startsWith("RU Limit Exceeded") ||
        err.toLowerCase().includes("ru validation")
      )
    }
  ];

  // Track errors that are matched to our checklist to display unmatched errors in a fallback card
  const mappedErrors = new Set();
  checks.forEach(check => {
    const err = check.getError();
    if (err) mappedErrors.add(err);
  });
  const effectiveIsValid = isValid;
  const unmappedErrors = errors.filter(err => !mappedErrors.has(err));

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 overflow-y-auto">
      {/* Background overlay */}
      <div className="fixed inset-0 transition-opacity" aria-hidden="true" onClick={isActionLoading ? null : onClose}>
        <div className="absolute inset-0 bg-ink-900 opacity-60 "></div>
      </div>

      {/* Modal panel */}
      <div className="ds-modal max-w-lg max-h-[85vh] animate-fade-in">
        {/* Close Button */}
        {!isActionLoading && (
          <button
            type="button"
            onClick={onClose}
            className="absolute top-4 right-4 text-ink-400 hover:text-ink-600 transition-colors focus:outline-none"
            aria-label="Close"
          >
            <i className="fas fa-times text-lg"></i>
          </button>
        )}

        {/* Content Section */}
        <div className="px-6 pt-8 pb-6 flex-grow overflow-y-auto">
          <div className="text-center sm:text-left flex flex-col sm:flex-row items-center sm:items-start gap-4">
            {/* Status Icon */}
            <div className={`flex-shrink-0 flex items-center justify-center h-14 w-14 rounded-full ${
              effectiveIsValid ? "bg-success-50 text-success-500" : "bg-critical-50 text-critical-500"
            }`}>
              <i className={`fas ${effectiveIsValid ? "fa-check-circle text-2xl" : "fa-exclamation-triangle text-2xl"}`}></i>
            </div>

            <div className="flex-grow mt-3 sm:mt-0 text-center sm:text-left">
              <h3 className="text-xl leading-6 font-semibold text-ink-900 mb-2">
                {effectiveIsValid ? "All Checks Passed" : "Validation Check Failed"}
              </h3>
              
              <p className="text-sm text-ink-500 mb-4">
                {effectiveIsValid 
                  ? "The invoice data has passed all verification constraints. You can now save it as a draft or submit it directly for review."
                  : "Please review the checklist below and resolve the failing validation issues."}
              </p>

              {/* Validation Checklist */}
              <div className="mt-4 space-y-3">
                {checks.map(check => {
                  const errorMsg = check.getError();
                  return (
                    <div 
                      key={check.id} 
                      className={`p-3.5 rounded-lg border transition-all duration-200 text-left ${
                        check.failed 
                          ? "bg-critical-50/40 border-critical-100" 
                          : "bg-success-50/20 border-success-100/50"
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex items-start gap-2.5">
                          <span className={`mt-0.5 text-base flex-shrink-0 ${
                            check.failed ? "text-critical-500" : "text-success-500"
                          }`}>
                            <i className={check.failed ? "fas fa-times-circle" : "fas fa-check-circle"}></i>
                          </span>
                          <div>
                            <p className={`text-sm font-semibold ${
                              check.failed ? "text-ink-900" : "text-ink-800"
                            }`}>
                              {check.name}
                            </p>
                            <p className="text-xs text-ink-400 mt-0.5 leading-relaxed">
                              {check.description}
                            </p>
                          </div>
                        </div>
                        <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full border flex-shrink-0 ${
                          check.failed 
                            ? "bg-critical-50 text-critical-700 border-critical-200" 
                            : "bg-success-50 text-success-700 border-success-200"
                        }`}>
                          {check.failed ? "Failed" : "Passed"}
                        </span>
                      </div>
                      
                      {check.failed && errorMsg && (
                        <div className="mt-2.5 ml-7 bg-white border border-critical-100 rounded-md p-2.5 text-xs text-critical-700 font-medium leading-relaxed">
                          <div>{errorMsg}</div>
                          {check.id === "ru_validation" && (
                            <div className="mt-3 pt-3 border-t border-critical-50 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                              <span className="text-xs text-critical-600 font-medium italic">
                                This error cannot be bypassed. Developer configuration and reading data are required.
                              </span>
                              <button
                                type="button"
                                onClick={() => {
                                  history.push(`/monthlyReadings/readingsEntry?account=${accountNumber}&area=${areaCode}&billCycle=${billCycle}&source=tempReadings&resolveInvoice=true`);
                                }}
                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold bg-critical-600 hover:bg-critical-700 text-white rounded shadow-sm transition-colors focus:outline-none"
                              >
                                <i className="fas fa-edit"></i>
                                Resolve Readings
                              </button>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}

                {/* Unmapped System Errors Fallback */}
                {unmappedErrors.length > 0 && (
                  <div className="p-3.5 rounded-lg border bg-warning-50/40 border-warning-100 text-left">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-2.5">
                        <span className="mt-0.5 text-base flex-shrink-0 text-warning-500">
                          <i className="fas fa-exclamation-circle"></i>
                        </span>
                        <div>
                          <p className="text-sm font-semibold text-ink-900">
                            General System Check
                          </p>
                          <p className="text-xs text-ink-400 mt-0.5 leading-relaxed">
                            Additional metadata and calculation constraints failed validation checks.
                          </p>
                        </div>
                      </div>
                      <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-warning-50 text-warning-700 border border-warning-200 flex-shrink-0">
                        Failed
                      </span>
                    </div>
                    <div className="mt-2.5 ml-7 bg-white border border-warning-100 rounded-md p-2.5 text-xs text-warning-700 space-y-1.5 leading-relaxed">
                      {unmappedErrors.map((err, idx) => (
                        <div key={idx} className="flex items-start gap-1">
                          <span className="text-warning-500 font-bold flex-shrink-0">•</span>
                          <span>{err}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Action Footer */}
        <div className="ds-modal-footer">
          {isActionLoading ? (
            <div className="w-full flex justify-center py-2 text-sm text-ink-500 font-medium items-center gap-2">
              <i className="fas fa-spinner fa-spin"></i> Processing request...
            </div>
          ) : (
            <>
              {effectiveIsValid && onSubmit && (
                <button
                  type="button"
                  onClick={() => onSubmit()}
                  className="w-full sm:w-auto inline-flex justify-center items-center rounded-lg shadow-sm px-5 py-2.5 bg-success-600 hover:bg-success-700 text-sm font-semibold text-white transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-success-500"
                >
                  Submit For Review
                </button>
              )}

              <button
                type="button"
                onClick={onClose}
                className="w-full sm:w-auto inline-flex justify-center items-center rounded-lg border border-ink-300 shadow-sm px-5 py-2.5 bg-white text-sm font-semibold text-ink-700 hover:bg-ink-50 transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500"
              >
                {effectiveIsValid ? "Cancel" : "Close"}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

ValidationStatusModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  isValid: PropTypes.bool.isRequired,
  errors: PropTypes.arrayOf(PropTypes.string),
  onSubmit: PropTypes.func,
  onClose: PropTypes.func.isRequired,
  isActionLoading: PropTypes.bool,
  accountNumber: PropTypes.string,
  areaCode: PropTypes.string,
  billCycle: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
};

export default ValidationStatusModal;
