import React, { useEffect, useRef, useState } from "react";
import { toast } from "react-toastify";
import {
  getCurrentOpenBillCycle,
  getInvoiceCreationSummary,
  getInvoiceCreateEntries,
  endBillCycle,
} from "services/billCycleEndingService";
import { formatBillMonthName, formatBillPeriod } from "utils/billCycleUtils";
import EndBillCycleConfirmModal from "components/Modal/EndBillCycleConfirmModal";

const summaryCards = [
  {
    key: "active_developers",
    label: "Active Developers",
    detail: "Included in this bill month",
    icon: "fas fa-users",
    color: "bg-sky-600",
  },
  {
    key: "invoices_created",
    label: "Invoices Created",
    detail: "Ready for this cycle",
    icon: "fas fa-file-invoice",
    color: "bg-success-600",
  },
  {
    key: "invoices_pending",
    label: "Invoices Pending",
    detail: "Still to be created",
    icon: "fas fa-clock",
    color: "bg-warning-500",
  },
];

const REGISTER_PAGE_SIZE = 10;

export default function BillCycleEnding() {
  const [currentCycle, setCurrentCycle] = useState(null);
  const [cycleLoading, setCycleLoading] = useState(true);
  const [cycleError, setCycleError] = useState("");
  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(true);
  const [summaryError, setSummaryError] = useState("");
  const [invoiceEntries, setInvoiceEntries] = useState([]);
  const [entriesLoading, setEntriesLoading] = useState(true);
  const [entriesError, setEntriesError] = useState("");
  const [registerSearch, setRegisterSearch] = useState("");
  const [registerStatus, setRegisterStatus] = useState("pending");
  const [registerPage, setRegisterPage] = useState(0);
  const [retryCount, setRetryCount] = useState(0);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [remarks, setRemarks] = useState("");
  const [endSubmitting, setEndSubmitting] = useState(false);
  const [endError, setEndError] = useState("");
  // Guards a double click before React has re-rendered the disabled button
  const endInFlightRef = useRef(false);

  useEffect(() => {
    let isMounted = true;
    setCycleLoading(true);
    setCycleError("");
    setSummaryLoading(true);
    setSummaryError("");
    setSummary(null);
    setEntriesLoading(true);
    setEntriesError("");
    setInvoiceEntries([]);
    setRegisterPage(0);

    getCurrentOpenBillCycle()
      .then((cycle) => {
        if (!isMounted) return null;
        setCurrentCycle(cycle);
        const summaryRequest = getInvoiceCreationSummary(cycle.bill_cycle)
          .then((loadedSummary) => {
            if (isMounted) setSummary(loadedSummary);
          })
          .catch((error) => {
            if (isMounted) setSummaryError(error.message);
          });
        const entriesRequest = getInvoiceCreateEntries(cycle.bill_cycle)
          .then((entries) => {
            if (isMounted) setInvoiceEntries(entries);
          })
          .catch((error) => {
            if (isMounted) setEntriesError(error.message);
          });
        return Promise.all([summaryRequest, entriesRequest]);
      })
      .catch((error) => {
        if (!isMounted) return;
        setCycleError(error.message);
        setSummaryError(error.message);
        setEntriesError(error.message);
      })
      .finally(() => {
        if (!isMounted) return;
        setCycleLoading(false);
        setSummaryLoading(false);
        setEntriesLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [retryCount]);

  const billPeriod = formatBillPeriod(currentCycle?.bill_month, currentCycle?.bill_year);
  const billMonthName = formatBillMonthName(currentCycle?.bill_month);
  const cycleStatus = currentCycle?.cycle_status;

  const activeDevelopers = Number(summary?.active_developers ?? 0);
  const invoicesCreated = Number(summary?.invoices_created ?? 0);
  const invoicesPending = Number(summary?.invoices_pending ?? 0);
  const isSummaryFailed = Boolean(summaryError);
  const allInvoicesCreated = !summaryLoading && !isSummaryFailed && invoicesPending === 0;

  // The closing action is only offered for an open cycle with nothing outstanding
  const canCloseCycle = allInvoicesCreated && cycleStatus !== "Closed";

  // Progress reflects the real figures; nothing outstanding always reads as complete
  const progress = allInvoicesCreated
    ? 100
    : activeDevelopers > 0
      ? Math.round((invoicesCreated / activeDevelopers) * 100)
      : 0;

  // Cycle status is always derived from the live invoice completion data
  const cycleClosed = cycleStatus === "Closed";
  const statusView = cycleClosed
    ? {
      label: "Cycle Closed",
      pill: "bg-ink-100 text-ink-600",
      icon: "fas fa-lock",
      iconWrap: "bg-ink-100 text-ink-600",
      message: "This bill month has already been closed.",
    }
    : isSummaryFailed
      ? {
        label: "Unavailable",
        pill: "bg-critical-50 text-critical-600",
        icon: "fas fa-exclamation-triangle",
        iconWrap: "bg-critical-50 text-critical-600",
        message: "Invoice completion status could not be loaded. Please try again.",
      }
      : allInvoicesCreated
        ? {
          label: "Ready to Close",
          pill: "bg-success-50 text-success-700",
          icon: "fas fa-check-circle",
          iconWrap: "bg-success-50 text-success-700",
          message: activeDevelopers > 0
            ? `All ${activeDevelopers} active developer${activeDevelopers === 1 ? "" : "s"} have created invoices for this cycle, so it is ready to be closed.`
            : "There are no active developers requiring invoices for this cycle, so it is ready to be closed.",
        }
        : {
          label: "Invoices Pending",
          pill: "bg-warning-50 text-warning-700",
          icon: "fas fa-hourglass-half",
          iconWrap: "bg-warning-50 text-warning-700",
          message: `${invoicesPending} developer${invoicesPending === 1 ? "" : "s"} still need to create an invoice. This bill month cannot be ended until all required invoices are created.`,
        };

  const cardValue = (key) => {
    if (summaryLoading) return "Loading...";
    if (isSummaryFailed) return "Unavailable";
    return summary ? String(summary[key] ?? 0) : "—";
  };

  // Visual treatment for the readiness panel, driven only by the real cycle state
  const readinessTone = cycleClosed
    ? {
      tile: "bg-ink-500",
      callout: "border-ink-200 bg-ink-50",
      calloutText: "text-ink-600",
      calloutIcon: "fas fa-lock",
    }
    : isSummaryFailed
      ? {
        tile: "bg-critical-500",
        callout: "border-critical-200 bg-critical-50",
        calloutText: "text-critical-700",
        calloutIcon: "fas fa-exclamation-circle",
      }
      : allInvoicesCreated
        ? {
          tile: "bg-success-600",
          callout: "border-success-200 bg-success-50",
          calloutText: "text-success-800",
          calloutIcon: "fas fa-check-circle",
        }
        : {
          tile: "bg-warning-500",
          callout: "border-warning-200 bg-warning-50",
          calloutText: "text-warning-800",
          calloutIcon: "fas fa-hourglass-half",
        };

  const createdEntries = invoiceEntries.filter((entry) => Number(entry.is_create) === 1);
  const pendingEntries = invoiceEntries.filter((entry) => Number(entry.is_create) !== 1);
  const statusEntries = registerStatus === "created" ? createdEntries : pendingEntries;
  const filteredEntries = statusEntries.filter((entry) => {
    const query = registerSearch.trim().toLowerCase();
    if (!query) return true;
    return [entry.folio_no, entry.developer_name, entry.remarks]
      .some((value) => String(value ?? "").toLowerCase().includes(query));
  });
  const registerPageCount = Math.ceil(filteredEntries.length / REGISTER_PAGE_SIZE);
  const visibleRegisterEntries = filteredEntries.slice(
    registerPage * REGISTER_PAGE_SIZE,
    (registerPage + 1) * REGISTER_PAGE_SIZE
  );

  const handleOpenConfirm = () => {
    if (endInFlightRef.current) return;
    setEndError("");
    setRemarks("");
    setConfirmOpen(true);
  };

  const handleCancelConfirm = () => {
    if (endInFlightRef.current) return;
    setConfirmOpen(false);
    setEndError("");
  };

  // The backend re-validates everything; this only reacts to what it decides.
  const handleConfirmEndCycle = () => {
    if (endInFlightRef.current) return;
    endInFlightRef.current = true;
    setEndSubmitting(true);
    setEndError("");

    endBillCycle(remarks)
      .then((result) => {
        setConfirmOpen(false);
        setRemarks("");
        setRegisterStatus("pending");
        setRegisterSearch("");
        setRegisterPage(0);
        const closedPeriod = formatBillPeriod(result.closed_bill_month, result.closed_bill_year);
        const newPeriod = formatBillPeriod(result.new_bill_month, result.new_bill_year);
        toast.success(
          `Bill cycle ${result.closed_bill_cycle}${closedPeriod ? ` (${closedPeriod})` : ""} was ended. `
          + `Bill cycle ${result.new_bill_cycle}${newPeriod ? ` (${newPeriod})` : ""} is now the current bill month.`
        );
        // Re-read the cycle and the completion figures for the new current cycle
        setRetryCount((count) => count + 1);
      })
      .catch((error) => {
        // Refresh the completion data either way, since the server's view is authoritative
        setRetryCount((count) => count + 1);

        if (error.status === 409) {
          // The cycle state changed underneath us (already closed / invoices pending).
          // Close the dialog so the user sees the refreshed, accurate page.
          setConfirmOpen(false);
          toast.error(error.message);
        } else {
          // Transient failure: keep the dialog open so the user can simply retry.
          setEndError(error.message);
        }
      })
      .finally(() => {
        endInFlightRef.current = false;
        setEndSubmitting(false);
      });
  };

  return (
    <div className="flex flex-col gap-ds-6">
      <section>
        <div className="mx-auto w-full">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h1 className="ds-page-title">
                Bill Month Ending
              </h1>
              <p className="ds-page-subtitle mt-1">
                Review invoice progress for the current bill month.
              </p>
            </div>
            <span
              className={`inline-flex w-fit items-center rounded-full border px-3 py-1 text-xs font-semibold ${
                isSummaryFailed
                  ? "border-critical-200/30 bg-critical-500/10 text-critical-200"
                  : "border-navy-200/20 bg-white/10 text-navy-100"
              }`}
            >
              {summaryLoading
                ? "Loading progress..."
                : isSummaryFailed
                  ? "Progress unavailable"
                  : cycleStatus || "Current cycle"}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-ds-3 xl:grid-cols-4">
            <div className="ds-stat-card">
              <div className="ds-stat-icon ds-stat-icon-info">
                <i className="fas fa-calendar-day" aria-hidden="true"></i>
              </div>
              <div className="min-w-0">
                <p className="ds-label">Bill Month</p>
                <p className="mt-0.5 text-base font-extrabold text-ink-800 sm:mt-1 sm:text-lg">
                  {cycleLoading ? "Loading..." : billMonthName || "Unavailable"}
                </p>
                <p className="text-[11px] font-medium text-ink-500 sm:text-xs">
                  {cycleLoading
                    ? ""
                    : currentCycle
                      ? [
                        currentCycle.bill_year,
                        currentCycle.bill_cycle != null ? `Cycle ${currentCycle.bill_cycle}` : null,
                      ].filter(Boolean).join(" · ") || "Bill month not recorded"
                      : ""}
                </p>
              </div>
            </div>

            {summaryCards.map((card) => (
              <div key={card.key} className="ds-stat-card">
                <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-base text-white sm:h-12 sm:w-12 sm:rounded-2xl sm:text-xl ${card.color}`}>
                  <i className={card.icon} aria-hidden="true"></i>
                </div>
                <div className="min-w-0">
                  <p className="ds-label">{card.label}</p>
                  <p className="my-0.5 text-xl font-extrabold text-ink-800 sm:text-2xl">{cardValue(card.key)}</p>
                  <p className="hidden text-xs font-medium text-ink-500 sm:block">{card.detail}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section>
        <div className="ds-card overflow-hidden">
          {/* Panel header */}
          <div className="flex flex-col gap-3 border-b border-ink-200 bg-ink-50 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <div className="flex items-center gap-3">
              <div className={`ds-stat-icon ${readinessTone.tile}`}>
                <i className={`${statusView.icon} text-base`} aria-hidden="true"></i>
              </div>
              <div className="min-w-0">
                <h2 className="text-base font-bold text-ink-800">Bill Month Closing Readiness</h2>
                <p className="mt-0.5 text-xs text-ink-500">
                  Check whether the current bill month can be closed.
                </p>
              </div>
            </div>
            <span
              className={`inline-flex w-fit shrink-0 items-center gap-2 rounded-full px-3 py-1.5 text-sm font-semibold ${statusView.pill}`}
            >
              <span className="h-2 w-2 rounded-full bg-current" aria-hidden="true"></span>
              {summaryLoading ? "Loading" : statusView.label}
            </span>
          </div>

          {/* Panel body */}
          <div className="px-4 py-4 sm:px-6 sm:py-5">
            {/* Invoice creation progress */}
            <div className="flex items-end justify-between gap-4">
              <p className="ds-stat-label">
                Invoice Creation Progress
              </p>
              <span className="text-2xl font-extrabold text-ink-800">
                {summaryLoading || isSummaryFailed ? "—" : `${progress}%`}
              </span>
            </div>
            <div
              className="mt-2.5 h-4 w-full overflow-hidden rounded-full bg-ink-100"
              role="progressbar"
              aria-label="Invoice creation progress"
              aria-valuemin="0"
              aria-valuemax="100"
              aria-valuenow={summaryLoading || isSummaryFailed ? 0 : progress}
            >
              <div
                className={`h-full rounded-full transition-all duration-500 ${canCloseCycle ? "bg-success-600" : "bg-warning-500"}`}
                style={{ width: `${summaryLoading || isSummaryFailed ? 0 : progress}%` }}
              ></div>
            </div>
            <p className="mt-2.5 text-sm text-ink-500">
              {summaryLoading
                ? "Loading invoice progress..."
                : isSummaryFailed
                  ? summaryError
                  : activeDevelopers > 0
                    ? `${invoicesCreated} of ${activeDevelopers} required invoices created${
                      billPeriod ? ` for ${billPeriod}` : ""
                    }`
                    : "No active developers for this bill month"}
            </p>

            {/* Readiness message */}
            <div
              className={`mt-5 flex items-start gap-3 rounded-xl border p-4 ${readinessTone.callout}`}
            >
              <i className={`${readinessTone.calloutIcon} mt-0.5 shrink-0`} aria-hidden="true"></i>
              <p className={`text-sm leading-6 ${readinessTone.calloutText}`}>{statusView.message}</p>
            </div>

            {cycleError && (
              <button
                type="button"
                onClick={() => setRetryCount((count) => count + 1)}
                className="mt-4 text-sm font-semibold text-navy-800 underline underline-offset-2"
              >
                Try again
              </button>
            )}

            {/* Actions */}
            <div className="mt-6 flex flex-col gap-4 border-t border-ink-100 pt-5 sm:flex-row sm:items-center sm:justify-between">
              <a
                href="#invoice-register"
                className="inline-flex w-fit items-center gap-2 rounded-xl border border-ink-200 bg-white px-3.5 py-2 text-xs font-bold text-ink-700 shadow-xs transition-colors hover:bg-ink-50 focus:outline-none focus:ring-2 focus:ring-navy-800/20"
              >
                <i className="fas fa-list-alt text-ink-500" aria-hidden="true"></i>
                View invoice register
              </a>

              <div className="text-left sm:text-right">
                {canCloseCycle ? (
                  <>
                    <button
                      type="button"
                      onClick={handleOpenConfirm}
                      disabled={endSubmitting}
                      className="inline-flex min-h-11 w-full items-center justify-center gap-2 px-5 py-3 bg-navy-800 hover:bg-navy-900 text-white text-sm font-bold rounded-xl shadow-sm transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-navy-800/30 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
                    >
                      <i
                        className={`fas ${endSubmitting ? "fa-spinner fa-spin" : "fa-flag-checkered"}`}
                        aria-hidden="true"
                      ></i>
                      {endSubmitting ? "Ending Bill Month..." : "End Bill Month"}
                    </button>
                    {currentCycle?.next_bill_cycle != null && (
                      <p className="mt-2 text-xs text-ink-400">
                        Ending this closes the current bill month and opens cycle{" "}
                        {currentCycle.next_bill_cycle}
                        {formatBillPeriod(currentCycle.next_bill_month, currentCycle.next_bill_year)
                          ? ` for ${formatBillPeriod(currentCycle.next_bill_month, currentCycle.next_bill_year)}`
                          : ""}
                        .
                      </p>
                    )}
                  </>
                ) : (
                  <>
                    <button
                      type="button"
                      disabled
                      className="inline-flex min-h-11 w-full items-center justify-center gap-2 px-5 py-3 bg-navy-800 text-white text-sm font-bold rounded-xl shadow-sm opacity-50 sm:w-auto"
                      title={summaryLoading || isSummaryFailed
                        ? "Invoice progress is not available yet"
                        : "The bill month cannot be ended until all required invoices are created"}
                    >
                      <i className="fas fa-lock" aria-hidden="true"></i>
                      End Bill Month
                    </button>
                    <p className="mt-2 text-xs text-ink-400">
                      {summaryLoading
                        ? "Checking invoice progress..."
                        : isSummaryFailed
                          ? "Invoice progress is unavailable, so this cycle cannot be ended."
                          : "Waiting for all required invoices."}
                    </p>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="invoice-register" className="scroll-mt-24">
        <div className="ds-card overflow-hidden">
          <header className="border-b border-ink-200 bg-ink-50 px-4 py-4 sm:px-5">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex min-w-0 items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-navy-50 text-navy-800">
                  <i className="fas fa-file-invoice" aria-hidden="true"></i>
                </div>
                <div className="min-w-0">
                  <h2 className="text-sm font-bold text-ink-800">Invoice Register</h2>
                  <p className="mt-0.5 text-xs text-ink-500">
                    {billPeriod || "Current bill month"} · {entriesLoading ? "Loading entries..." : entriesError ? "Register unavailable" : `${filteredEntries.length} shown`}
                  </p>
                </div>
              </div>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <div className="inline-flex w-fit items-center rounded-xl border border-ink-200 bg-white p-1" role="tablist" aria-label="Filter invoice register by status">
                  <button
                    type="button"
                    role="tab"
                    aria-selected={registerStatus === "pending"}
                    onClick={() => {
                      setRegisterStatus("pending");
                      setRegisterPage(0);
                    }}
                    className={`rounded-lg px-3 py-2 text-xs font-bold transition-colors ${registerStatus === "pending" ? "bg-navy-100 text-navy-800 shadow-xs" : "text-ink-600 hover:bg-ink-50"}`}
                  >
                    Pending <span className="ml-1 opacity-75">{entriesLoading ? "—" : pendingEntries.length}</span>
                  </button>
                  <button
                    type="button"
                    role="tab"
                    aria-selected={registerStatus === "created"}
                    onClick={() => {
                      setRegisterStatus("created");
                      setRegisterPage(0);
                    }}
                    className={`rounded-lg px-3 py-2 text-xs font-bold transition-colors ${registerStatus === "created" ? "bg-navy-100 text-navy-800 shadow-xs" : "text-ink-600 hover:bg-ink-50"}`}
                  >
                    Created <span className="ml-1 opacity-75">{entriesLoading ? "—" : createdEntries.length}</span>
                  </button>
                </div>
                <label className="relative block min-w-0 sm:w-72">
                  <span className="sr-only">Search invoice register</span>
                  <i className="fas fa-search pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-xs text-ink-400" aria-hidden="true"></i>
                  <input
                    type="search"
                    value={registerSearch}
                    onChange={(event) => {
                      setRegisterSearch(event.target.value);
                      setRegisterPage(0);
                    }}
                    placeholder="Search folio or developer"
                    className="w-full rounded-xl border border-ink-200 bg-white py-2.5 pl-9 pr-3 text-sm text-ink-700 placeholder:text-ink-400 focus:border-navy-800 focus:outline-none focus:ring-2 focus:ring-navy-800/15"
                  />
                </label>
                <button
                  type="button"
                  onClick={() => setRetryCount((count) => count + 1)}
                  disabled={entriesLoading}
                  className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-ink-200 bg-white text-ink-600 transition-colors hover:bg-ink-50 disabled:cursor-not-allowed disabled:opacity-50"
                  title="Refresh invoice register"
                  aria-label="Refresh invoice register"
                >
                  <i className={`fas ${entriesLoading ? "fa-spinner fa-spin" : "fa-sync-alt"}`} aria-hidden="true"></i>
                </button>
              </div>
            </div>
          </header>

          {entriesLoading ? (
            <div className="space-y-2 p-4" aria-label="Loading invoice register">
              {[0, 1, 2, 3].map((row) => <div key={row} className="h-12 animate-pulse rounded-lg bg-ink-100"></div>)}
            </div>
          ) : entriesError ? (
            <div className="px-4 py-10 text-center">
              <i className="fas fa-exclamation-circle text-base text-critical-600" aria-hidden="true"></i>
              <p className="mt-2 text-sm text-ink-600">{entriesError}</p>
              <button type="button" onClick={() => setRetryCount((count) => count + 1)} className="mt-2 text-sm font-semibold text-navy-800 underline underline-offset-2">Try again</button>
            </div>
          ) : filteredEntries.length === 0 ? (
            <div className="px-4 py-12 text-center">
              <i className={`fas ${registerSearch ? "fa-search" : registerStatus === "created" ? "fa-check-circle" : "fa-inbox"} text-3xl ${registerStatus === "created" && !registerSearch ? "text-success-500" : "text-ink-300"}`} aria-hidden="true"></i>
              <p className="mt-3 text-sm font-semibold text-ink-700">
                {registerSearch
                  ? "No invoice records match your search."
                  : registerStatus === "created"
                    ? "No invoices have been created for this cycle yet."
                    : "No developers are waiting to create an invoice."}
              </p>
              {registerSearch && <button type="button" onClick={() => setRegisterSearch("")} className="mt-2 text-sm font-semibold text-navy-800 underline underline-offset-2">Clear search</button>}
            </div>
          ) : (
            <>
              <div className="w-full overflow-x-auto">
                <table className="min-w-full text-left border-collapse">
                  <thead className="bg-ink-50 text-ink-500">
                    <tr className="border-b border-ink-200 text-[11px] uppercase tracking-wider">
                      <th scope="col" className="px-4 py-3 font-bold sm:px-5">Folio Number</th>
                      <th scope="col" className="px-4 py-3 font-bold sm:px-5">Developer Name</th>
                      <th scope="col" className="px-4 py-3 font-bold sm:px-5">Invoice Status</th>
                      <th scope="col" className="px-4 py-3 font-bold sm:px-5">Remarks</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-ink-100 bg-white text-sm text-ink-700">
                    {visibleRegisterEntries.map((entry, index) => (
                      <tr key={`${entry.folio_no}-${index}`} className={`${index % 2 ? "bg-ink-50/30" : "bg-white"} transition-colors hover:bg-ink-50/80`}>
                        <td className="ds-th whitespace-nowrap sm:px-5">{entry.folio_no ?? "—"}</td>
                        <td className="max-w-[20rem] truncate px-4 py-3 font-medium sm:px-5" title={entry.developer_name || ""}>{entry.developer_name || "—"}</td>
                        <td className="whitespace-nowrap px-4 py-3 sm:px-5">
                          <span className={`inline-block rounded-full border px-3 py-1 text-xs font-semibold ${registerStatus === "created" ? "border-success-200 bg-success-50 text-success-700" : "border-warning-200 bg-warning-50 text-warning-700"}`}>
                            {registerStatus === "created" ? "Invoice Created" : "Invoice Pending"}
                          </span>
                        </td>
                        <td className="max-w-[18rem] truncate px-4 py-3 text-ink-500 sm:px-5" title={entry.remarks || ""}>{entry.remarks || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {registerPageCount > 1 && (
                <footer className="flex flex-col gap-3 border-t border-ink-100 bg-ink-50 p-4 text-sm text-ink-500 sm:flex-row sm:items-center sm:justify-between">
                  <span>Showing {registerPage * REGISTER_PAGE_SIZE + 1} to {Math.min((registerPage + 1) * REGISTER_PAGE_SIZE, filteredEntries.length)} of {filteredEntries.length} entries</span>
                  <div className="flex items-center gap-2">
                    <button type="button" onClick={() => setRegisterPage((page) => Math.max(0, page - 1))} disabled={registerPage === 0} className="rounded-xl border border-ink-200 bg-white px-3.5 py-1.5 text-xs font-bold text-ink-600 hover:bg-ink-50 disabled:opacity-50">Previous</button>
                    <span className="px-2 text-xs font-bold text-ink-600">{registerPage + 1} / {registerPageCount}</span>
                    <button type="button" onClick={() => setRegisterPage((page) => Math.min(registerPageCount - 1, page + 1))} disabled={registerPage >= registerPageCount - 1} className="rounded-xl border border-ink-200 bg-white px-3.5 py-1.5 text-xs font-bold text-ink-600 hover:bg-ink-50 disabled:opacity-50">Next</button>
                  </div>
                </footer>
              )}
            </>
          )}
        </div>
      </section>

      <EndBillCycleConfirmModal
        isOpen={confirmOpen}
        cycle={currentCycle}
        isSubmitting={endSubmitting}
        error={endError}
        remarks={remarks}
        onRemarksChange={setRemarks}
        onConfirm={handleConfirmEndCycle}
        onCancel={handleCancelConfirm}
      />
    </div>
  );
}