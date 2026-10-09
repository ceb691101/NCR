import React, { useState } from "react";
import PropTypes from "prop-types";

const BulkActionModal = ({
  isOpen,
  actionType, // "APPROVE" or "REJECT"
  selectedCount,
  onConfirm,
  onClose,
  isLoading = false,
}) => {
  const [remarks, setRemarks] = useState("");

  if (!isOpen) return null;

  const handleConfirm = () => {
    onConfirm(remarks);
  };

  const isApprove = actionType === "APPROVE";

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 overflow-y-auto">
      {/* Background overlay */}
      <div 
        className="fixed inset-0 transition-opacity" 
        aria-hidden="true" 
        onClick={isLoading ? null : onClose}
      >
        <div className="absolute inset-0 bg-ink-900 opacity-60 "></div>
      </div>

      {/* Modal panel */}
      <div className="ds-modal max-w-md max-h-[85vh] animate-fade-in">
        {/* Close Button */}
        {!isLoading && (
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
        <div className="px-6 pt-8 pb-6 flex-grow">
          <div className="flex flex-col gap-4">
            {/* Top row with Icon and Title */}
            <div className="flex items-center gap-3">
              {/* Status Icon */}
              <div className={`flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full ${
                isApprove ? "bg-success-50 text-success-500" : "bg-critical-50 text-critical-500"
              }`}>
                <i className={`fas ${isApprove ? "fa-check-circle" : "fa-exclamation-triangle"} text-xl`}></i>
              </div>

              <div>
                <h3 className="text-lg font-semibold leading-6 text-ink-900">
                  {isApprove ? "Approve Invoices" : "Reject Invoices"}
                </h3>
              </div>
            </div>

            <div className="w-full text-left">
              <p className="text-sm text-ink-500 mb-4">
                You have selected <span className="font-bold text-ink-700">{selectedCount}</span> invoice{selectedCount > 1 ? "s" : ""} to {isApprove ? "approve and finalize" : "reject"}.
              </p>

              {/* Remarks Textarea */}
              <div className="w-full">
                <label className="block text-xs font-bold uppercase text-ink-500 mb-2">
                  Remarks / Comments {isApprove ? "(Optional)" : "(Required for rejection)"}
                </label>
                <textarea
                  className="w-full px-3 py-2 text-sm text-ink-600 placeholder-ink-400 bg-white rounded-lg border border-ink-200 focus:outline-none focus:ring-1 focus:ring-ink-400 focus:border-transparent transition-all duration-150 resize-none h-24"
                  placeholder={isApprove ? "Add bulk approval remarks here..." : "Provide rejection reasons here..."}
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="ds-modal-footer">
          <button
            type="button"
            onClick={onClose}
            disabled={isLoading}
            className="px-4 py-2 border border-ink-200 hover:bg-ink-100 text-ink-700 font-bold text-xs rounded-xl shadow-xs transition-all duration-150 cursor-pointer disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={isLoading || (!isApprove && !remarks.trim())}
            className={`ds-btn ds-btn-sm ${
              isApprove 
                ? "bg-success-600 hover:bg-success-700 active:bg-success-800" 
                : "bg-critical-700 hover:bg-critical-800 active:bg-critical-800"
            }`}
          >
            {isLoading ? (
              <>
                <i className="fas fa-spinner fa-spin text-white"></i> Processing...
              </>
            ) : (
              <>
                <i className={`fas ${isApprove ? "fa-paper-plane" : "fa-ban"} text-white/90`}></i> Confirm {isApprove ? "Approve" : "Reject"}
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

BulkActionModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  actionType: PropTypes.oneOf(["APPROVE", "REJECT"]).isRequired,
  selectedCount: PropTypes.number.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  isLoading: PropTypes.bool,
};

export default BulkActionModal;
