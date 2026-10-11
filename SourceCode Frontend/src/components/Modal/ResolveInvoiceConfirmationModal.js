import React from "react";
import PropTypes from "prop-types";

const ResolveInvoiceConfirmationModal = ({
  isOpen,
  onClose,
  onSubmitForReview,
  onSaveAsDraft,
  isActionLoading = false,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 transition-opacity" aria-hidden="true" onClick={isActionLoading ? null : onClose}>
          <div className="absolute inset-0 bg-ink-900/60"></div>
        </div>

        {/* Center alignment spacer */}
        <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">
          &#8203;
        </span>

        {/* Modal panel */}
        <div className="ds-modal sm:max-w-lg animate-fade-in">
          <div className="bg-white px-6 pt-6 pb-4 sm:p-6">
            <div className="sm:flex sm:items-start gap-4">
              <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-warning-50 border border-warning-200 sm:mx-0 sm:h-10 sm:w-10">
                <i className="fas fa-file-signature text-warning-600 text-lg"></i>
              </div>
              <div className="mt-3 text-center sm:mt-0 sm:text-left flex-grow">
                <h3 className="text-lg leading-6 font-bold text-ink-800">
                  Resolve Rejected Invoice
                </h3>
                <div className="mt-2.5">
                  <p className="text-sm text-ink-500 leading-relaxed">
                    You are saving reading edits for an account with a rejected invoice. 
                    Please choose how you would like to proceed:
                  </p>
                  <ul className="mt-3 space-y-2 text-xs text-ink-400">
                    <li className="flex items-start gap-1.5">
                      <span className="text-warning-500 font-bold">•</span>
                      <span><strong>Submit for Review</strong> will save your edits and directly queue the invoice for Chief Engineer review.</span>
                    </li>
                    <li className="flex items-start gap-1.5">
                      <span className="text-warning-500 font-bold">•</span>
                      <span><strong>Save as Draft</strong> will save your edits but keep the invoice in draft status so you can update it later.</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
          <div className="ds-modal-footer">
            {isActionLoading ? (
              <div className="w-full flex justify-center py-2 text-sm text-ink-500 font-semibold items-center gap-2">
                <i className="fas fa-spinner fa-spin text-warning-500"></i> Processing...
              </div>
            ) : (
              <>
                <button
                  type="button"
                  onClick={onSubmitForReview}
                  className="w-full sm:w-auto inline-flex justify-center items-center rounded-lg shadow-sm px-5 py-2.5 bg-success-600 hover:bg-success-700 text-sm font-semibold text-white transition-colors focus:outline-none"
                >
                  <i className="fas fa-paper-plane mr-2"></i>
                  Submit for Review
                </button>
                <button
                  type="button"
                  onClick={onSaveAsDraft}
                  className="w-full sm:w-auto inline-flex justify-center items-center rounded-lg shadow-sm px-5 py-2.5 bg-navy-600 hover:bg-navy-700 text-sm font-semibold text-white transition-colors focus:outline-none"
                >
                  <i className="fas fa-save mr-2"></i>
                  Save as Draft
                </button>
                <button
                  type="button"
                  onClick={onClose}
                  className="w-full sm:w-auto inline-flex justify-center items-center rounded-lg border border-ink-300 shadow-sm px-5 py-2.5 bg-white text-sm font-semibold text-ink-700 hover:bg-ink-50 transition-colors focus:outline-none"
                >
                  Cancel
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

ResolveInvoiceConfirmationModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSubmitForReview: PropTypes.func.isRequired,
  onSaveAsDraft: PropTypes.func.isRequired,
  isActionLoading: PropTypes.bool,
};

export default ResolveInvoiceConfirmationModal;
