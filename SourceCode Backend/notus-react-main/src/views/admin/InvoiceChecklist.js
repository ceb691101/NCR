import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import SearchInput from "../../components/SearchInput";
import {
  fetchCurrentBillCycle,
  fetchChecklistInvoices,
  fetchCheckedInvoices,
  addChecklistInvoices,
  validateChecklist
} from "../../services/invoiceChecklistService";

export default function InvoiceChecklist() {
  const [activeCycle, setActiveCycle] = useState("");
  const [cycleError, setCycleError] = useState(null);

  const [userRole, setUserRole] = useState("");
  const [invoices, setInvoices] = useState([]);
  const [activeTab, setActiveTab] = useState("REMAINING"); // "REMAINING" or "CHECKED"
  const [selectedMap, setSelectedMap] = useState({});
  const [remarksMap, setRemarksMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [validation, setValidation] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const isIT = userRole && (userRole.toUpperCase().includes("IT") || userRole.toUpperCase().includes("ADMIN"));

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setPageIndex(0);
  };

  const handleSearchClear = () => {
    setSearchTerm("");
    setPageIndex(0);
  };

  // Fetch active current bill cycle from DB on mount and load checklist data
  useEffect(() => {
    let isMounted = true;
    const initCurrentBillCycle = async () => {
      setLoading(true);
      setCycleError(null);
      try {
        const cycle = await fetchCurrentBillCycle();
        if (!isMounted) return;
        setActiveCycle(String(cycle));
        await loadData(cycle, activeTab);
      } catch (err) {
        if (!isMounted) return;
        console.error("Failed to load current bill cycle:", err);
        const errMsg = err.message || "No current bill month is available.";
        setCycleError(errMsg);
        setInvoices([]);
        setLoading(false);
      }
    };

    initCurrentBillCycle();
    return () => {
      isMounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getFilteredInvoices = () => {
    return invoices.filter((inv) => {
      if (!searchTerm || !searchTerm.trim()) return true;
      const term = searchTerm.trim().toLowerCase();
      return inv.folioNo && String(inv.folioNo).toLowerCase().includes(term);
    });
  };

  const filteredInvoices = getFilteredInvoices();
  const totalPages = Math.ceil(filteredInvoices.length / pageSize) || 1;
  const currentPage = Math.min(pageIndex, totalPages - 1);
  const startIdx = currentPage * pageSize;
  const paginatedInvoices = filteredInvoices.slice(startIdx, startIdx + pageSize);

  const handleSelectAll = () => {
    const nextMap = { ...selectedMap };
    filteredInvoices.forEach((inv) => {
      nextMap[inv.folioNo] = true;
    });
    setSelectedMap(nextMap);
  };

  const handlePageSizeChange = (e) => {
    setPageSize(Number(e.target.value));
    setPageIndex(0);
  };

  const loadData = async (targetCycle, mode = activeTab) => {
    if (!targetCycle || targetCycle === "0") {
      setLoading(false);
      return;
    }
    setLoading(true);
    const startTime = performance.now();
    console.log(`[InvoiceChecklist] LOAD started for billCycle: ${targetCycle}, mode: ${mode}`);
    try {
      const fetchInvoicesFn = mode === "CHECKED" ? fetchCheckedInvoices : fetchChecklistInvoices;
      const [data, val] = await Promise.all([
        fetchInvoicesFn(targetCycle),
        validateChecklist(targetCycle)
      ]);
      setInvoices(data.invoices || []);
      setUserRole(data.userRole || "");
      setPageIndex(0);

      const initRemarks = {};
      const initSelected = {};
      (data.invoices || []).forEach((inv) => {
        if (inv.remarks) {
          initRemarks[inv.folioNo] = inv.remarks;
        }
        if (inv.isAdded) {
          initSelected[inv.folioNo] = true;
        }
      });
      setRemarksMap(initRemarks);
      setValidation(val);
      const duration = (performance.now() - startTime).toFixed(2);
      console.log(`[InvoiceChecklist] LOAD completed in ${duration} ms`);
    } catch (err) {
      console.error("Invoice Checklist load error:", err);
      toast.error(err.message || "Unable to load Invoice Checklist. Please try again.");
      setInvoices([]);
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (mode) => {
    if (mode === activeTab) return;
    setActiveTab(mode);
    setPageIndex(0);
    if (activeCycle) {
      loadData(activeCycle, mode);
    }
  };

  const handleSelectNone = () => {
    setSelectedMap({});
  };

  const handleToggleSelect = (folioNo) => {
    setSelectedMap((prev) => ({
      ...prev,
      [folioNo]: !prev[folioNo]
    }));
  };

  const handleRemarkChange = (folioNo, value) => {
    setRemarksMap((prev) => ({
      ...prev,
      [folioNo]: value
    }));
  };

  const handleAdd = async () => {
    const selectedFolios = Object.keys(selectedMap).filter((k) => selectedMap[k]);
    if (selectedFolios.length === 0) {
      toast.warning("Please select at least one invoice.");
      return;
    }

    const payload = selectedFolios.map((folioNoStr) => {
      const folioNo = parseInt(folioNoStr, 10);
      return {
        folioNo,
        remarks: remarksMap[folioNo] || ""
      };
    });

    setSubmitting(true);
    try {
      const res = await addChecklistInvoices(activeCycle, payload);
      toast.success("Successfully added");
      if (res.validation) {
        setValidation(res.validation);
      }
      await loadData(activeCycle, activeTab);
    } catch (err) {
      console.error("Add invoices error:", err);
      toast.error(err.message || "Failed to add invoices to checklist.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col w-full min-h-screen bg-ink-50">
      {/* Top Blue Hero Section - Styled exactly like Invoice Management */}
      <div>
        <div className="w-full mx-auto">
          {/* Title and bill month */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2">
            <div>
              <div className="flex items-center gap-2 text-navy-200 text-xs font-semibold uppercase tracking-wider mb-1">
                <span>BILL MONTH</span>
                <i className="fas fa-chevron-right text-[9px] text-navy-400"></i>
                <span className="text-white font-extrabold">INVOICE CHECKLIST</span>
              </div>
              <h1 className="ds-page-title">
                Invoice Checklist
              </h1>
              <p className="ds-page-subtitle mt-1">
                Review, validate and manage pending renewable energy invoice creation checklist
              </p>
            </div>

            {/* Bill Month Read-Only Display */}
            <div className="flex items-center gap-2.5 bg-white/10 p-2 rounded-xl border border-white/10 shadow-inner">
              <label className="text-xs font-extrabold text-navy-100 uppercase tracking-wider px-2">
                BILL MONTH:
              </label>
              <input
                type="text"
                readOnly
                disabled
                value={loading && !activeCycle ? "..." : activeCycle || "N/A"}
                className="w-28 px-3 py-1.5 text-sm font-bold text-ink-900 bg-white/90 border border-ink-200 rounded-lg cursor-not-allowed text-center focus:outline-none shadow-xs"
              />
            </div>
          </div>

          {/* Stats Cards Section inside Stripe - Styled matching Invoice Management */}
          {validation && (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-ds-4">
              {/* 1. TOTAL INVOICES */}
              <div className="ds-stat-card">
                <div className="ds-stat-icon ds-stat-icon-info">
                  <i className="fas fa-file-invoice"></i>
                </div>
                <div>
                  <p className="ds-stat-label">Total Invoices</p>
                  <p className="ds-stat-value">{validation.totalInvoices ?? 0}</p>
                  <p className="ds-stat-note">Active role developers</p>
                </div>
              </div>

              {/* 2. CHECKED (Clickable) */}
              <div
                onClick={() => handleTabChange("CHECKED")}
                className={`ds-stat-card ds-stat-card-action ${
                  activeTab === "CHECKED"
                    ? "border-success-500 ring-2 ring-success-500/40 shadow-lg scale-[1.02]"
                    : "border-ink-100 shadow-sm hover:shadow-md hover:border-success-200"
                }`}
              >
                <div className="ds-stat-icon ds-stat-icon-success">
                  <i className="fas fa-check-circle"></i>
                </div>
                <div>
                  <p className="ds-stat-label flex items-center gap-1.5">
                    Checked
                    {activeTab === "CHECKED" && (
                      <span className="w-2 h-2 rounded-full bg-success-500 animate-pulse"></span>
                    )}
                  </p>
                  <p className="text-2xl font-extrabold text-success-600 my-0.5">
                    {validation.checkedCount ?? validation.assignedCount ?? 0}
                  </p>
                  <p className="ds-stat-note">Invoices created</p>
                </div>
              </div>

              {/* 3. REMAINING (Clickable) */}
              <div
                onClick={() => handleTabChange("REMAINING")}
                className={`ds-stat-card ds-stat-card-action ${
                  activeTab === "REMAINING"
                    ? "ds-stat-card-active"
                    : "border-ink-100 shadow-sm hover:shadow-md hover:border-warning-200"
                }`}
              >
                <div className="ds-stat-icon ds-stat-icon-warning">
                  <i className="fas fa-clock"></i>
                </div>
                <div>
                  <p className="ds-stat-label flex items-center gap-1.5">
                    Remaining
                    {activeTab === "REMAINING" && (
                      <span className="w-2 h-2 rounded-full bg-warning-500 animate-pulse"></span>
                    )}
                  </p>
                  <p className="text-2xl font-extrabold text-warning-600 my-0.5">
                    {Math.max(0, validation.remainingCount ?? 0)}
                  </p>
                  <p className="ds-stat-note">Pending creation</p>
                </div>
              </div>

              {/* 4. STATUS */}
              <div className="ds-stat-card">
                <div className={`ds-stat-icon ${
                  validation.complete ? "bg-success-600" : "bg-warning-500"
                }`}>
                  <i className={`fas ${validation.complete ? "fa-tasks" : "fa-exclamation-circle"}`}></i>
                </div>
                <div>
                  <p className="ds-stat-label">Status</p>
                  <div className="my-1">
                    <span className={`inline-block px-3 py-0.5 text-xs font-black tracking-wider rounded-full ${
                      validation.complete
                        ? "bg-success-50 text-success-700 border border-success-200"
                        : "bg-warning-50 text-warning-700 border border-warning-200"
                    }`}>
                      {validation.complete ? "COMPLETE" : "INCOMPLETE"}
                    </span>
                  </div>
                  <p className="ds-stat-note">Cycle status</p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="relative px-0 pb-10 w-full mx-auto flex-1 flex flex-col">
        <div className="ds-card overflow-hidden flex flex-col flex-1">
          {/* Table Actions & Search Header */}
          <div className="p-4 sm:px-6 bg-ink-50/70 border-b border-ink-200 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <SearchInput
              value={searchTerm}
              onChange={handleSearchChange}
              onClear={handleSearchClear}
              placeholder="Search by Folio No..."
              className="w-full sm:max-w-xs"
            />

            <div className="flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto">
              {activeTab === "REMAINING" && (
                <>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={handleSelectAll}
                      className="px-3.5 py-1.5 text-xs font-semibold text-ink-700 bg-white border border-ink-300 rounded-lg shadow-xs hover:bg-ink-100 active:scale-95 transition-all"
                    >
                      Select All
                    </button>
                    <button
                      type="button"
                      onClick={handleSelectNone}
                      className="px-3.5 py-1.5 text-xs font-semibold text-ink-700 bg-white border border-ink-300 rounded-lg shadow-xs hover:bg-ink-100 active:scale-95 transition-all"
                    >
                      Select None
                    </button>
                  </div>

                  <button
                    type="button"
                    disabled={submitting}
                    onClick={handleAdd}
                    className="px-6 py-2 text-xs font-bold uppercase tracking-wider text-white bg-success-500 hover:bg-success-600 active:bg-success-700 rounded-xl shadow-md hover:shadow-lg active:scale-95 transition-all disabled:opacity-50 flex items-center gap-2"
                  >
                    {submitting ? (
                      <>
                        <i className="fas fa-spinner fa-spin"></i>
                        Processing...
                      </>
                    ) : (
                      <>
                        <i className="fas fa-plus-circle"></i>
                        ADD
                      </>
                    )}
                  </button>
                </>
              )}
            </div>
          </div>

        {/* Table View */}
        <div className="overflow-x-auto flex-1">
          {loading ? (
            <div className="p-12 text-center text-ink-500 font-medium">
              <span className="ds-spinner" aria-hidden="true"></span>
              <p>Loading invoice checklist...</p>
            </div>
          ) : invoices.length === 0 ? (
            <div className="p-12 md:p-16 text-center text-ink-500">
              <div className="w-16 h-16 rounded-2xl bg-ink-100 border border-ink-200 flex items-center justify-center text-ink-400 text-2xl mx-auto mb-3 shadow-xs">
                <i className="fas fa-folder-open"></i>
              </div>
              <p className="font-bold text-ink-800 text-base mb-1">
                {cycleError
                  ? cycleError
                  : activeTab === "CHECKED"
                  ? `No checked invoices found for Bill Month ${activeCycle}`
                  : validation && validation.complete
                  ? `No remaining invoices for Bill Month ${activeCycle}`
                  : `No invoices found for Bill Month ${activeCycle}`}
              </p>
              <p className="text-xs text-ink-500 max-w-sm mx-auto">
                {cycleError
                  ? "Ensure a bill cycle has is_current = 1 in the database."
                  : activeTab === "CHECKED"
                  ? "No invoices have been added/checked for this bill month yet."
                  : validation && validation.complete
                  ? "All assigned invoices have been checked and added for this bill month."
                  : "Ensure bill month number is correct and invoices/developers are assigned."}
              </p>
            </div>
          ) : filteredInvoices.length === 0 ? (
            <div className="p-12 text-center text-ink-500">
              <div className="w-12 h-12 rounded-2xl bg-ink-100 border border-ink-200 flex items-center justify-center text-ink-400 text-xl mx-auto mb-3 shadow-xs">
                <i className="fas fa-search"></i>
              </div>
              <p className="font-bold text-ink-800 text-sm mb-1">
                No matching invoices found
              </p>
              <p className="text-xs text-ink-500 max-w-xs mx-auto">
                No records matched Folio No "{searchTerm}". Try clearing your search filter.
              </p>
            </div>
          ) : (
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-ink-100/80 text-[11px] font-bold text-ink-600 uppercase tracking-wider border-b border-ink-200">
                  {activeTab === "REMAINING" && (
                    <th className="py-3 px-4 w-12 text-center">Select</th>
                  )}
                  <th className="py-3 px-4 w-24">Folio No</th>
                  <th className="py-3 px-4">Company / Developer</th>
                  <th className="py-3 px-4">Project / Facility</th>
                  <th className="py-3 px-4 w-28 text-center">Status</th>
                  <th className="py-3 px-6">Remarks</th>
                  {isIT && <th className="py-3 px-4">ASSIGNED TO</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 text-xs">
                {paginatedInvoices.map((inv) => {
                  const isChecked = !!selectedMap[inv.folioNo];
                  return (
                    <tr
                      key={inv.folioNo}
                      className={`hover:bg-ink-50/80 transition-colors ${isChecked && activeTab === "REMAINING" ? "bg-navy-50/40" : ""}`}
                    >
                      {activeTab === "REMAINING" && (
                        <td className="py-3 px-4 text-center">
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={() => handleToggleSelect(inv.folioNo)}
                            className="w-4 h-4 rounded border-ink-300 accent-navy-800 cursor-pointer"
                          />
                        </td>
                      )}
                      <td className="py-3 px-4 font-bold text-navy-800">
                        {inv.folioNo}
                      </td>
                      <td className="py-3 px-4 font-semibold text-ink-800">
                        {inv.companyName || "N/A"}
                      </td>
                      <td className="py-3 px-4 text-ink-600">
                        {inv.projectName || "N/A"}
                      </td>

                      <td className="py-3 px-4 text-center">
                        {inv.isAdded ? (
                          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-success-100 text-success-700 border border-success-500">
                            ADDED
                          </span>
                        ) : (
                          <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-ink-100 text-ink-500 border border-ink-200">
                            PENDING
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-6">
                        {activeTab === "REMAINING" ? (
                          <input
                            type="text"
                            value={remarksMap[inv.folioNo] || ""}
                            onChange={(e) => handleRemarkChange(inv.folioNo, e.target.value)}
                            placeholder="Enter remarks..."
                            className="w-full px-3 py-1.5 text-xs bg-ink-50 border border-ink-200 rounded-lg focus:outline-none focus:bg-white focus:ring-2 focus:ring-navy-800"
                          />
                        ) : (
                          <span className="text-ink-700 font-medium">
                            {inv.remarks && inv.remarks.trim() ? inv.remarks : "-"}
                          </span>
                        )}
                      </td>
                      {isIT && (
                        <td className="py-3 px-4 font-semibold text-navy-800">
                          {inv.assignedTo || inv.preparedBy || "N/A"}
                        </td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>

        {/* Pagination Footer - Styled exactly like Invoice Management */}
        {!loading && filteredInvoices.length > 0 && (
          <div className="p-4 bg-ink-50/50 border-t border-ink-200 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs font-semibold text-ink-600">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-1.5">
                <span>Show</span>
                <select
                  value={pageSize}
                  onChange={handlePageSizeChange}
                  className="border border-ink-200 rounded-xl px-2 py-1 bg-white text-ink-700 focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800"
                >
                  {[5, 10, 20, 50].map((size) => (
                    <option key={size} value={size}>
                      {size}
                    </option>
                  ))}
                </select>
                <span>entries</span>
              </div>
              <span>
                Showing {filteredInvoices.length === 0 ? 0 : startIdx + 1} to{" "}
                {Math.min(startIdx + pageSize, filteredInvoices.length)} of {filteredInvoices.length} entries
              </span>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPageIndex((prev) => Math.max(0, prev - 1))}
                disabled={currentPage === 0}
                className="px-3.5 py-1.5 rounded-xl border border-ink-200 bg-white text-ink-600 hover:bg-ink-50 disabled:opacity-50 disabled:hover:bg-white text-xs font-bold flex items-center gap-1 focus:outline-none select-none cursor-pointer"
              >
                ◀ Previous
              </button>
              <div className="flex items-center gap-1">
                <button
                  type="button"
                  className="px-3 py-1 rounded-lg text-xs font-extrabold focus:outline-none select-none bg-navy-800 text-white"
                >
                  {currentPage + 1}
                </button>
              </div>
              <button
                type="button"
                onClick={() => setPageIndex((prev) => Math.min(totalPages - 1, prev + 1))}
                disabled={currentPage >= totalPages - 1}
                className="px-3.5 py-1.5 rounded-xl border border-ink-200 bg-white text-ink-600 hover:bg-ink-50 disabled:opacity-50 disabled:hover:bg-white text-xs font-bold flex items-center gap-1 focus:outline-none select-none cursor-pointer"
              >
                Next ▶
              </button>
            </div>
          </div>
        )}
        </div>
      </div>
    </div>
  );
}
