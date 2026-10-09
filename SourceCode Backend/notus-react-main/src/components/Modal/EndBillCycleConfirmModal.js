import React from "react";
import PropTypes from "prop-types";
import { formatBillPeriod } from "utils/billCycleUtils";

/**
 * EndBillCycleConfirmModal Component
 *
 * Compact "are you sure?" warning shown before the current bill month is closed.
 * Every figure comes from the live bill-cycle record - nothing is hardcoded.
 */
const EndBillCycleConfirmModal = ({
  isOpen,
  cycle,
  isSubmitting,
  error,
  remarks,
  onRemarksChange,
  onConfirm,
  onCancel,
}) => {
  if (!isOpen || !cycle) return null;

  const currentPeriod = formatBillPeriod(cycle.bill_month, cycle.bill_year);
  const nextPeriod = formatBillPeriod(cycle.next_bill_month, cycle.next_bill_year);

  return (
    <div
      className="fixed inset-0 z-50 overflow-y-auto bg-ink-900/60 px-4 py-8"
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="end-bill-cycle-title"
      aria-describedby="end-bill-cycle-message"
    >
      <div className="flex min-h-full items-center justify-center">
        <div className="ds-modal sm:max-w-md animate-fade-in">
          <div className="px-6 pt-6 pb-4">
            <div className="flex items-start gap-4">
              <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-warning-100 text-warning-600">
                <i
                  className={`fas ${isSubmitting ? "fa-spinner fa-spin" : "fa-exclamation-triangle"} text-xl`}
                  aria-hidden="true"
                ></i>
              </div>
              <div className="min-w-0 pt-0.5">
                <h3 id="end-bill-cycle-title" className="ds-section-title">
                  {isSubmitting ? "Ending bill month..." : "End this bill month?"}
                </h3>
                <p id="end-bill-cycle-message" className="mt-1.5 text-sm leading-6 text-ink-500">
                  {isSubmitting ? (
                    "Please wait while the bill month is closed and the next one is created."
                  ) : (
                    <>
                      You are about to end bill month{" "}
                      <span className="font-semibold text-ink-700">
                        {cycle.bill_cycle ?? "—"}
                        {currentPeriod ? ` (${currentPeriod})` : ""}
                      </span>
                      . A new bill month{" "}
                      <span className="font-semibold text-ink-700">
                        {cycle.next_bill_cycle ?? "—"}
                        {nextPeriod ? ` (${nextPeriod})` : ""}
                      </span>{" "}
                      will be created and become the current bill month.
                    </>
                  )}
                </p>
              </div>
            </div>

            {!isSubmitting && (
              <div className="mt-4 flex items-start gap-2 rounded-xl border border-warning-200 bg-warning-50 p-3">
                <i className="fas fa-info-circle text-warning-500 mt-0.5" aria-hidden="true"></i>
                <p className="text-xs leading-5 text-warning-800">
                  This action cannot be undone. Please make sure all invoices for this bill
                  month are correct before continuing.
                </p>
              </div>
            )}

            <div className="mt-4">
              <label htmlFor="bill-cycle-remarks" className="mb-1.5 block text-sm font-semibold text-ink-700">
                Remarks <span className="font-normal text-ink-400">(optional)</span>
              </label>
              <textarea
                id="bill-cycle-remarks"
                value={remarks}
                onChange={(event) => onRemarksChange(event.target.value)}
                maxLength={100}
                rows={3}
                disabled={isSubmitting}
                placeholder="Add a note for the closed bill month"
                className="w-full resize-y rounded-xl border border-ink-300 px-3 py-2.5 text-sm text-ink-700 placeholder:text-ink-400 focus:border-navy-800 focus:outline-none focus:ring-2 focus:ring-navy-800/15 disabled:bg-ink-50"
              />
              <p className="mt-1 text-right text-xs text-ink-400">{remarks.length}/100</p>
            </div>

            {error && (
              <div className="mt-4 flex items-start gap-2 rounded-xl border border-critical-200 bg-critical-50 p-3">
                <i className="fas fa-exclamation-circle text-critical-500 mt-0.5" aria-hidden="true"></i>
                <p className="text-sm text-critical-700">{error}</p>
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="ds-modal-footer">
            <button
              type="button"
              onClick={onCancel}
              disabled={isSubmitting}
              className="w-full sm:w-auto inline-flex justify-center items-center px-4 py-2.5 rounded-xl border border-ink-300 bg-white text-sm font-semibold text-ink-700 hover:bg-ink-100 focus:outline-none focus:ring-2 focus:ring-ink-400 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
            >
              No, Cancel
            </button>
            <button
              type="button"
              onClick={onConfirm}
              disabled={isSubmitting}
              className="w-full sm:w-auto inline-flex justify-center items-center px-5 py-2.5 rounded-xl border border-transparent shadow-md bg-critical-600 text-sm font-semibold text-white hover:bg-critical-700 focus:outline-none focus:ring-2 focus:ring-critical-500 focus:ring-offset-2 transition-all hover:shadow-lg disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {isSubmitting ? (
                <>
                  <span className="ds-spinner ds-spinner-sm ds-spinner-invert mr-2 align-middle" aria-hidden="true"></span>
                  Ending...
                </>
              ) : (
                <>
                  <i className="fas fa-flag-checkered mr-2" aria-hidden="true"></i>
                  Yes, End Bill Month
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

EndBillCycleConfirmModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  cycle: PropTypes.shape({
    bill_cycle: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    bill_year: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    bill_month: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    next_bill_cycle: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    next_bill_year: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    next_bill_month: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  }),
  isSubmitting: PropTypes.bool.isRequired,
  error: PropTypes.string,
  remarks: PropTypes.string.isRequired,
  onRemarksChange: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

export default EndBillCycleConfirmModal;
