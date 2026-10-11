import React from "react";
import PropTypes from "prop-types";

const UnsavedChangesModal = ({
  isOpen,
  onClose,
  onLeave,
  onStay,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 transition-opacity" aria-hidden="true">
          <div className="absolute inset-0 bg-ink-500 opacity-75"></div>
        </div>

        {/* Modal panel */}
        <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">
          &#8203;
        </span>
        
        <div className="ds-modal sm:max-w-lg animate-fade-in">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div className="sm:flex sm:items-start">
              <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-warning-100 sm:mx-0 sm:h-10 sm:w-10">
                <i className="fas fa-exclamation-triangle text-warning-600"></i>
              </div>
              <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                <h3 className="text-lg leading-6 font-medium text-ink-900">
                  Unsaved Changes
                </h3>
                <div className="mt-2">
                  <p className="text-sm text-ink-500">
                    You're about to leave this page. Would you like to save your work before leaving?
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div className="ds-modal-footer">
            <button
              type="button"
              onClick={onLeave}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-critical-600 text-base font-medium text-white hover:bg-critical-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-critical-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Leave this Page
            </button>
            <button
              type="button"
              onClick={onStay}
              className="mt-3 w-full inline-flex justify-center rounded-md border border-ink-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-ink-700 hover:bg-ink-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Stay on this Page
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

UnsavedChangesModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func,
  onLeave: PropTypes.func.isRequired,
  onStay: PropTypes.func.isRequired,
};

export default UnsavedChangesModal;