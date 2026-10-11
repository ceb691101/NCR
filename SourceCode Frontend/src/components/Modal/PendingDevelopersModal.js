import React from "react";
import PropTypes from "prop-types";

/**
 * PendingDevelopersModal Component
 *
 * Lists the active developers that have not yet created an invoice for the
 * bill cycle being reviewed. Shows friendly developer details only.
 */
const PendingDevelopersModal = ({
  isOpen,
  developers,
  isLoading,
  error,
  onRetry,
  onClose,
}) => {
  if (!isOpen) return null;

  const developerCount = developers.length;

  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto"
      role="dialog"
      aria-modal="true"
      aria-labelledby="pending-developers-title"
    >
      <div className="flex items-center justify-center min-h-screen px-4 py-8 text-center sm:block">
        {/* Backdrop overlay */}
        <div
          className="fixed inset-0 bg-ink-900 bg-opacity-60 transition-opacity"
          aria-hidden="true"
          onClick={onClose}
        />

        {/* Trick to center modal on desktop */}
        <span className="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">
          &#8203;
        </span>

        <div className="ds-modal sm:max-w-3xl animate-fade-in">
          {/* Header */}
          <div className="flex items-start justify-between gap-4 border-b border-ink-100 px-6 py-5">
            <div className="flex items-start gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-warning-50 text-warning-600">
                <i className="fas fa-user-clock text-lg" aria-hidden="true"></i>
              </div>
              <div>
                <h3 id="pending-developers-title" className="text-base font-bold text-ink-800">
                  Pending Developers
                </h3>
                <p className="mt-1 text-sm text-ink-500">
                  {isLoading
                    ? "Loading pending developers..."
                    : `${developerCount} developer${developerCount === 1 ? "" : "s"} still need to create an invoice.`}
                </p>
              </div>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg p-2 text-ink-400 hover:bg-ink-100 hover:text-ink-600 transition-colors focus:outline-none focus:ring-2 focus:ring-ink-300"
              aria-label="Close pending developers"
            >
              <i className="fas fa-times" aria-hidden="true"></i>
            </button>
          </div>

          {/* Body */}
          <div className="max-h-[60vh] overflow-y-auto px-6 py-5">
            {isLoading ? (
              <div className="space-y-3">
                {[0, 1, 2].map((row) => (
                  <div key={row} className="h-12 animate-pulse rounded-lg bg-ink-100"></div>
                ))}
              </div>
            ) : error ? (
              <div className="py-6 text-center">
                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-critical-50 text-critical-500">
                  <i className="fas fa-exclamation-triangle text-xl" aria-hidden="true"></i>
                </div>
                <p className="mt-3 text-sm text-ink-600">{error}</p>
                <button
                  type="button"
                  onClick={onRetry}
                  className="mt-3 text-sm font-semibold text-navy-800 underline underline-offset-2"
                >
                  Try again
                </button>
              </div>
            ) : developerCount === 0 ? (
              <div className="py-6 text-center">
                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-success-50 text-success-600">
                  <i className="fas fa-check text-xl" aria-hidden="true"></i>
                </div>
                <p className="mt-3 text-sm text-ink-600">
                  No pending developers for this bill month.
                </p>
              </div>
            ) : (
              <div className="overflow-auto rounded-xl border border-ink-200">
                <table className="min-w-full divide-y divide-ink-200 text-sm">
                  <thead className="bg-ink-50">
                    <tr>
                      <th scope="col" className="ds-page-header-title px-4 py-3">
                        Folio Number
                      </th>
                      <th scope="col" className="ds-page-header-title px-4 py-3">
                        Developer Name
                      </th>
                      <th scope="col" className="ds-page-header-title px-4 py-3">
                        Project Name
                      </th>
                      <th scope="col" className="ds-page-header-title px-4 py-3">
                        Region
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-ink-100 bg-white">
                    {developers.map((developer, index) => (
                      <tr key={`${developer.folio_no}-${index}`} className="hover:bg-ink-50">
                        <td className="ds-th whitespace-nowrap">
                          {developer.folio_no || "—"}
                        </td>
                        <td className="px-4 py-3 font-medium text-ink-700">
                          {developer.developer_name || "—"}
                        </td>
                        <td className="px-4 py-3 text-ink-600">
                          {developer.facility_name || "—"}
                        </td>
                        <td className="whitespace-nowrap px-4 py-3 text-ink-600">
                          {developer.region || "—"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="ds-modal-footer">
            <button
              type="button"
              onClick={onClose}
              className="inline-flex items-center justify-center px-5 py-2.5 rounded-xl border border-ink-300 bg-white text-sm font-semibold text-ink-700 hover:bg-ink-100 focus:outline-none focus:ring-2 focus:ring-ink-400 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

PendingDevelopersModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  developers: PropTypes.arrayOf(
    PropTypes.shape({
      folio_no: PropTypes.string,
      developer_name: PropTypes.string,
      facility_name: PropTypes.string,
      region: PropTypes.string,
    })
  ).isRequired,
  isLoading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  onRetry: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default PendingDevelopersModal;
