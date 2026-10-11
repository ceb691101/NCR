import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import { calculateRuDifference, isRuWithinLimit } from "utils/readingUtils";

const TempReadingsTable = ({
  groupedReadings,
  loading,
  searchTerm,
  onSearch,
  recordsPerPage,
  onRecordsPerPageChange,
  currentPage,
  totalPages,
  totalRecords,
  onPageChange,
  onViewReading,
  areaInfo,
  errorFilter,
  invoiceFilter = "pending",
  onInvoiceFilterChange,
  onPrepareInvoice,
  preparingInvoiceAccountNumber,
}) => {
  const [expandedAccounts, setExpandedAccounts] = useState(new Set());
  const history = useHistory();

  const handleSearchChange = (e) => {
    onSearch(e.target.value);
  };

  const handleRecordsPerPageChange = (e) => {
    onRecordsPerPageChange(parseInt(e.target.value));
  };

  const handleViewClick = (reading) => {
    onViewReading(reading);
  };

  const handleViewMeterReading = (accountNumber, areaCode, billCycle, errorFilter = null) => {
    const params = new URLSearchParams();
    params.append("account", accountNumber);
    params.append("area", areaCode);
    if (billCycle) {
      params.append("billCycle", billCycle);
    }
    params.append("source", "tempReadings");

    if (errorFilter) {
      params.append("errorCode", errorFilter);
    }

    const url = `/monthlyReadings/readingsEntry?${params.toString()}`;
    history.push(url);
  };

  const toggleExpand = (accNbr) => {
    const newExpanded = new Set(expandedAccounts);
    if (newExpanded.has(accNbr)) {
      newExpanded.delete(accNbr);
    } else {
      newExpanded.add(accNbr);
    }
    setExpandedAccounts(newExpanded);
  };

  // Generate page numbers for pagination
  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;
    const maxPagesToShowMobile = 3;
    const isMobile = window.innerWidth < 768;
    const maxPages = isMobile ? maxPagesToShowMobile : maxPagesToShow;

    if (totalPages <= maxPages) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= Math.ceil(maxPages / 2)) {
        for (let i = 1; i <= maxPages - 1; i++) {
          pages.push(i);
        }
        pages.push("...");
        pages.push(totalPages);
      } else if (currentPage >= totalPages - Math.floor(maxPages / 2)) {
        pages.push(1);
        pages.push("...");
        for (let i = totalPages - (maxPages - 2); i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push("...");
        if (isMobile) {
          pages.push(currentPage);
        } else {
          for (let i = currentPage - 1; i <= currentPage + 1; i++) {
            pages.push(i);
          }
        }
        pages.push("...");
        pages.push(totalPages);
      }
    }

    return pages;
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return new Date(dateString).toLocaleDateString("en-GB", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      });
    } catch (error) {
      return dateString;
    }
  };

  const formatVal = (reading, key) => {
    if (!reading) return "-";
    const val = reading[key];
    if (val === undefined || val === null) return "-";

    const hasError = reading.has_error;
    const errorName = reading.error_name || "Error";

    const parsed = parseFloat(val);
    const formatted = isNaN(parsed) ? val : parsed.toLocaleString("en-US");

    return (
      <div className="inline-flex items-center justify-end space-x-1 w-full font-mono text-xs">
        {hasError && (
          <span
            className="px-1 py-0.5 bg-critical-100 text-critical-800 text-[9px] font-extrabold rounded mr-1 leading-none shadow-3xs uppercase tracking-wider cursor-help"
            title={errorName}
          >
            !
          </span>
        )}
        <span className={hasError ? "text-critical-600 font-bold" : "text-ink-600"}>
          {formatted}
        </span>
      </div>
    );
  };

  const formatRuDiff = (group) => {
    const kwdReading = group.readings?.find(r => r.mtr_type === 'KWD');
    const kwpReading = group.readings?.find(r => r.mtr_type === 'KWP');
    const kwoReading = group.readings?.find(r => r.mtr_type === 'KWO');
    const ruReading = group.readings?.find(r => r.mtr_type === 'RU' || r.mtr_type === 'KWT');

    if (!ruReading) return <span className="text-ink-400 font-mono text-xs">-</span>;

    const r1 = kwdReading ? kwdReading.units : null;
    const r2 = kwpReading ? kwpReading.units : null;
    const r3 = kwoReading ? kwoReading.units : null;
    const total = ruReading.units;

    const diff = calculateRuDifference(r1, r2, r3, total);
    if (diff === null || diff === undefined) return <span className="text-ink-400 font-mono text-xs">-</span>;

    const limit = group.accept_ru ?? ruReading.accept_ru;
    const isWithin = isRuWithinLimit(diff, limit);
    const hasError = !isWithin;

    const formatted = Number(diff).toLocaleString("en-US", {
      minimumFractionDigits: 1,
      maximumFractionDigits: 2,
    });

    const tooltip = limit != null 
      ? (hasError ? `Difference ${formatted} exceeds limit ±${limit}` : `Within limit ±${limit}`)
      : `Limit unconfigured (Diff: ${formatted})`;

    return (
      <div className="inline-flex items-center justify-end space-x-1 w-full font-mono text-xs" title={tooltip}>
        {hasError && (
          <span
            className="px-1 py-0.5 bg-critical-100 text-critical-800 text-[9px] font-extrabold rounded mr-1 leading-none shadow-3xs uppercase tracking-wider cursor-help"
          >
            !
          </span>
        )}
        <span className={hasError ? "text-critical-600 font-bold" : "text-success-700 font-semibold"}>
          {formatted}
        </span>
      </div>
    );
  };

  const getCellClassName = (reading, groupType, isBoundary = false) => {
    const hasError = reading && reading.has_error;

    let bgClass = "bg-white";
    if (hasError) {
      bgClass = "bg-critical-50/50";
    } else {
      if (groupType === "previous") bgClass = "bg-critical-50/40";
      else if (groupType === "present") bgClass = "bg-navy-50/40";
      else if (groupType === "units") bgClass = "bg-success-50/30";
    }

    const borderClass = isBoundary
      ? "border-r-2 border-ink-300"
      : "border-r border-ink-200";

    return `px-4 py-2.5 whitespace-nowrap text-right font-mono ${bgClass} ${borderClass}`;
  };

  return (
    <div className="ds-card overflow-hidden">
      {/* Filter Tabs / Mode Selector */}
      {onInvoiceFilterChange && (
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center px-4 sm:px-6 py-3.5 border-b border-ink-200 bg-ink-50/80 gap-3">
          <div className="inline-flex p-1 bg-ink-200/70 rounded-xl border border-ink-200">
            {typeof sessionStorage !== "undefined" &&
              sessionStorage.getItem("user_category") === "Electrical Engineer" && (
                <button
                  type="button"
                  onClick={() => onInvoiceFilterChange("pending")}
                  className={`px-4 py-2 rounded-lg text-xs font-bold transition-all duration-200 flex items-center space-x-2 cursor-pointer focus:outline-none focus:ring-0 outline-none ${
                    invoiceFilter === "pending"
                      ? "bg-white text-navy-700 shadow-sm"
                      : "text-ink-600 hover:text-ink-900"
                  }`}
                >
                  <i className="fas fa-file-invoice text-xs"></i>
                  <span>Invoices to be Prepared</span>
                </button>
              )}
            <button
              type="button"
              onClick={() => onInvoiceFilterChange("all")}
              className={`px-4 py-2 rounded-lg text-xs font-bold transition-all duration-200 flex items-center space-x-2 cursor-pointer focus:outline-none focus:ring-0 outline-none ${invoiceFilter === "all"
                ? "bg-white text-success-700 shadow-sm"
                : "text-ink-600 hover:text-ink-900"
                }`}
            >
              <i className="fas fa-list-alt text-xs"></i>
              <span>All Received Readings</span>
            </button>
          </div>
        </div>
      )}

      {/* Search and Records Per Page Controls */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center px-4 sm:px-6 py-4 border-b border-ink-200 space-y-3 lg:space-y-0 bg-ink-50/55">
        {/* Search Input */}
        <div className="flex items-center space-x-2 w-full lg:w-auto">
          <div className="relative flex-1 lg:flex-initial min-w-0">
            <i className="fas fa-search absolute left-3.5 top-1/2 transform -translate-y-1/2 text-ink-400"></i>
            <input
              type="text"
              placeholder="Search received readings..."
              value={searchTerm}
              onChange={handleSearchChange}
              className="ds-filter-input h-10 font-medium w-full lg:w-64 xl:w-80"
            />
          </div>
          {searchTerm && (
            <button
              onClick={() => onSearch("")}
              className="text-ink-400 hover:text-ink-600 p-1 focus:outline-none flex-shrink-0 cursor-pointer"
              title="Clear search"
            >
              <i className="fas fa-times"></i>
            </button>
          )}
        </div>

        {/* Records Per Page Dropdown */}
        <div className="flex items-center space-x-2 w-full lg:w-auto justify-between lg:justify-end">
          <div className="flex items-center space-x-2">
            <span className="text-xs sm:text-sm text-ink-500 whitespace-nowrap font-medium">
              Show:
            </span>
            <select
              value={recordsPerPage}
              onChange={handleRecordsPerPageChange}
              className="border border-ink-200 rounded-xl px-6 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 max-w-32 bg-white text-ink-700 cursor-pointer"
            >
              <option value={10}>10 accounts</option>
              <option value={20}>20 accounts</option>
              <option value={30}>30 accounts</option>
              <option value={40}>40 accounts</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results Info */}
      {!loading && (
        <div className="px-4 sm:px-6 py-2.5 text-xs text-ink-400 border-b border-ink-200 font-semibold uppercase tracking-wider bg-ink-50/20">
          Showing{" "}
          {groupedReadings.length > 0
            ? (currentPage - 1) * recordsPerPage + 1
            : 0}{" "}
          to {Math.min(currentPage * recordsPerPage, totalRecords)} of{" "}
          {totalRecords} accounts
        </div>
      )}

      {/* Table - Desktop View */}
      <div className="hidden md:block overflow-x-auto">
        <div className="min-w-full">
          <table className="w-full border-collapse">
            <thead className="bg-ink-100 text-ink-700">
              <tr className="border-b border-ink-200">
                <th rowSpan="2" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider align-middle border-r border-ink-300">
                  Folio Number
                </th>
                <th rowSpan="2" className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider align-middle border-r border-ink-300">
                  Reading Date
                </th>
                <th rowSpan="2" className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider align-middle border-r border-ink-300">
                  Tariff Type
                </th>
                <th colSpan="4" className="px-4 py-2 text-center text-xs font-semibold uppercase tracking-wider border-b border-r border-ink-300 bg-ink-100/60 text-ink-700">
                  PREVIOUS READINGS
                </th>
                <th colSpan="4" className="px-4 py-2 text-center text-xs font-semibold uppercase tracking-wider border-b border-r border-ink-300 bg-navy-50/30 text-navy-800">
                  PRESENT READINGS
                </th>
                <th colSpan="5" className="px-4 py-2 text-center text-xs font-semibold uppercase tracking-wider border-b border-r border-ink-300 bg-success-50/20 text-success-800">
                  ENERGY SENT TO GRID (kWh)
                </th>
                {invoiceFilter === "pending" && (
                  <th rowSpan="2" className="px-4 py-3 text-center text-xs font-semibold uppercase tracking-wider align-middle">
                    Actions
                  </th>
                )}
              </tr>
              <tr className="text-center font-semibold text-[11px]">
                <th className="px-3 py-2 text-right text-xs uppercase bg-ink-50/80 text-ink-500 border-r border-ink-200 border-b-2 border-b-ink-400">R1 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-ink-50/80 text-ink-500 border-r border-ink-200 border-b-2 border-b-ink-400">R2 (KWP)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-ink-50/80 text-ink-500 border-r border-ink-200 border-b-2 border-b-ink-400">R3 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-ink-50/80 text-ink-700 border-r-2 border-ink-300 border-b-2 border-b-ink-500 font-bold">RU</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-navy-50/30 text-navy-700 border-r border-navy-100 border-b-2 border-b-navy-400">R1 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-navy-50/30 text-navy-700 border-r border-navy-100 border-b-2 border-b-navy-400">R2 (KWP)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-navy-50/30 text-navy-700 border-r border-navy-100 border-b-2 border-b-navy-400">R3 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-navy-50/30 text-navy-800 border-r-2 border-ink-300 border-b-2 border-b-navy-500 font-bold">RU</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-success-50/30 text-success-700 border-r border-success-100 border-b-2 border-b-success-500">R1 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-success-50/30 text-success-700 border-r border-success-100 border-b-2 border-b-success-500">R2 (KWP)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-success-50/30 text-success-700 border-r border-success-100 border-b-2 border-b-success-500">R3 (KWD)</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-success-50/30 text-success-800 border-r border-success-100 border-b-2 border-b-success-500 font-bold">RU</th>
                <th className="px-3 py-2 text-right text-xs uppercase bg-success-50/30 text-success-800 border-r border-ink-200 border-b-2 border-b-success-600 font-bold">RU Diff</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-ink-100 text-sm text-ink-700">
              {loading ? (
                <tr>
                  <td colSpan={invoiceFilter === "pending" ? 17 : 16} className="px-6 py-12 text-center">
                    <div className="flex justify-center items-center">
                      <div className="ds-spinner"></div>
                      <span className="ml-3 text-ink-500 font-medium">
                        Loading meter readings...
                      </span>
                    </div>
                  </td>
                </tr>
              ) : groupedReadings.length === 0 ? (
                <tr>
                  <td colSpan={invoiceFilter === "pending" ? 17 : 16} className="px-6 py-12 text-center">
                    <div className="text-ink-400 py-6">
                      <i className="fas fa-file-alt text-lg text-ink-400"></i>
                      <p className="font-bold text-ink-700">
                        {searchTerm
                          ? "No accounts found matching your search"
                          : "No meter readings found"}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                groupedReadings.map((group, index) => {
                  const latestReadingDate =
                    group.readings && group.readings.length > 0
                      ? group.readings.reduce((latest, reading) => {
                        const currentDate = new Date(reading.rdng_date);
                        return currentDate > latest ? currentDate : latest;
                      }, new Date(0))
                      : null;

                  const kwdReading = group.readings.find(r => r.mtr_type === 'KWD');
                  const kwpReading = group.readings.find(r => r.mtr_type === 'KWP');
                  const kwoReading = group.readings.find(r => r.mtr_type === 'KWO');
                  const ruReading = group.readings.find(r => r.mtr_type === 'RU' || r.mtr_type === 'KWT');

                  return (
                    <tr
                      key={group.acc_nbr}
                      className={`transition-all duration-150 border-b border-ink-200 ${index % 2 === 0
                        ? "bg-ink-50/20 hover:bg-ink-100/50"
                        : "bg-white hover:bg-ink-100/50"
                        }`}
                    >
                      <td className="px-4 py-3 whitespace-nowrap align-middle border-r border-ink-200">
                        <div className="text-sm font-mono font-normal text-ink-800">
                          {group.folio_no ?? "N/A"}
                        </div>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap align-middle border-r border-ink-200 text-sm font-medium text-ink-600">
                        {latestReadingDate ? formatDate(latestReadingDate) : "N/A"}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap align-middle border-r border-ink-200 text-center text-xs font-semibold text-ink-700">
                        {group.tariff_desc || group.tariff_type || "N/A"}
                      </td>

                      {/* Previous Readings (KWD, KWP, KWO, RU) */}
                      <td className={getCellClassName(kwdReading, 'previous', false)}>
                        {formatVal(kwdReading, 'prv_rdn')}
                      </td>
                      <td className={getCellClassName(kwpReading, 'previous', false)}>
                        {formatVal(kwpReading, 'prv_rdn')}
                      </td>
                      <td className={getCellClassName(kwoReading, 'previous', false)}>
                        {formatVal(kwoReading, 'prv_rdn')}
                      </td>
                      <td className={getCellClassName(ruReading, 'previous', true)}>
                        {formatVal(ruReading, 'prv_rdn')}
                      </td>

                      {/* Present Readings (KWD, KWP, KWO, RU) */}
                      <td className={getCellClassName(kwdReading, 'present', false)}>
                        {formatVal(kwdReading, 'prsnt_rdn')}
                      </td>
                      <td className={getCellClassName(kwpReading, 'present', false)}>
                        {formatVal(kwpReading, 'prsnt_rdn')}
                      </td>
                      <td className={getCellClassName(kwoReading, 'present', false)}>
                        {formatVal(kwoReading, 'prsnt_rdn')}
                      </td>
                      <td className={getCellClassName(ruReading, 'present', true)}>
                        {formatVal(ruReading, 'prsnt_rdn')}
                      </td>

                      {/* Energy Sent to Grid (KWD, KWP, KWO, RU, RU Diff) */}
                      <td className={getCellClassName(kwdReading, 'units', false)}>
                        {formatVal(kwdReading, 'units')}
                      </td>
                      <td className={getCellClassName(kwpReading, 'units', false)}>
                        {formatVal(kwpReading, 'units')}
                      </td>
                      <td className={getCellClassName(kwoReading, 'units', false)}>
                        {formatVal(kwoReading, 'units')}
                      </td>
                      <td className={getCellClassName(ruReading, 'units', false)}>
                        {formatVal(ruReading, 'units')}
                      </td>
                      <td className={getCellClassName(ruReading, 'units', true)}>
                        {formatRuDiff(group)}
                      </td>

                      {/* Actions */}
                      {invoiceFilter === "pending" && (
                        <td className="px-4 py-3 whitespace-nowrap align-middle text-center">
                          <div className="flex items-center justify-center gap-3">
                            <button
                              type="button"
                              onClick={() =>
                                handleViewMeterReading(
                                  group.acc_nbr,
                                  group.area_cd,
                                  group.added_blcy,
                                  errorFilter
                                )
                              }
                              className="w-8 h-8 rounded-full border border-ink-200 bg-white text-ink-500 hover:text-navy-600 hover:bg-navy-50 hover:border-navy-200 hover:shadow-xs flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                              title="Edit Readings"
                            >
                              <i className="fas fa-edit text-xs"></i>
                            </button>
                            <button
                              type="button"
                              onClick={() => onPrepareInvoice(group)}
                              disabled={preparingInvoiceAccountNumber === group.acc_nbr}
                              className="w-8 h-8 rounded-full border border-ink-200 bg-white text-ink-500 hover:text-warning-600 hover:bg-warning-50 hover:border-warning-200 hover:shadow-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                              title="Prepare Invoice"
                            >
                              {preparingInvoiceAccountNumber === group.acc_nbr ? (
                                <i className="fas fa-spinner fa-spin text-xs text-warning-500"></i>
                              ) : (
                                <i className="fas fa-file-invoice text-xs"></i>
                              )}
                            </button>
                          </div>
                        </td>
                      )}
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Mobile View - Card Layout */}
      <div className="md:hidden">
        {loading ? (
          <div className="px-4 py-12 text-center">
            <div className="flex justify-center items-center">
              <div className="ds-spinner"></div>
              <span className="ml-3 text-ink-500 text-sm">
                Loading meter readings...
              </span>
            </div>
          </div>
        ) : groupedReadings.length === 0 ? (
          <div className="px-4 py-12 text-center">
            <div className="text-ink-400">
              <i className="fas fa-file-alt text-lg text-ink-400"></i>
              <p className="font-bold text-ink-700">
                {searchTerm ? "No accounts found matching search" : "No meter readings found"}
              </p>
            </div>
          </div>
        ) : (
          <div className="divide-y divide-ink-100 bg-white">
            {groupedReadings.map((group, index) => {
              const isExpanded = expandedAccounts.has(group.acc_nbr);
              const latestReadingDate =
                group.readings && group.readings.length > 0
                  ? group.readings.reduce((latest, reading) => {
                    const currentDate = new Date(reading.rdng_date);
                    return currentDate > latest ? currentDate : latest;
                  }, new Date(0))
                  : null;

              return (
                <div
                  key={group.acc_nbr}
                  className={`p-4 transition-all ${index % 2 === 0 ? "bg-white hover:bg-success-100" : "bg-success-50 hover:bg-success-100"
                    }`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1 space-y-1">
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Folio:
                        </span>
                        <span className="text-sm font-mono font-medium text-ink-800">
                          {group.folio_no ?? "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Date:
                        </span>
                        <span className="text-sm font-medium text-ink-600">
                          {latestReadingDate ? formatDate(latestReadingDate) : "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Tariff Type:
                        </span>
                        <span className="text-sm font-medium text-ink-700">
                          {group.tariff_desc || group.tariff_type || "N/A"}
                        </span>
                      </div>
                      {(() => {
                        const kwd = group.readings.find(r => r.mtr_type === 'KWD');
                        const kwp = group.readings.find(r => r.mtr_type === 'KWP');
                        const kwo = group.readings.find(r => r.mtr_type === 'KWO');
                        const ru = group.readings.find(r => r.mtr_type === 'RU' || r.mtr_type === 'KWT');
                        const diff = calculateRuDifference(
                          kwd ? kwd.units : null,
                          kwp ? kwp.units : null,
                          kwo ? kwo.units : null,
                          ru ? ru.units : null
                        );
                        if (diff === null) return null;
                        const isFailed = !isRuWithinLimit(diff, group.accept_ru);
                        const limitLabel = group.accept_ru != null ? `±${group.accept_ru}` : "Unconfigured";
                        return (
                          <div className="flex items-center space-x-2">
                            <span className="text-xs text-ink-400 font-bold uppercase">
                              RU Diff:
                            </span>
                            <span className={`text-sm font-mono font-bold ${isFailed ? "text-critical-600" : "text-ink-700"}`}>
                              {diff.toLocaleString("en-US", { minimumFractionDigits: 1, maximumFractionDigits: 1 })}
                              <span className="text-xs font-normal text-ink-400 ml-1">
                                (Limit: {limitLabel})
                              </span>
                              {isFailed && (
                                <span className="ml-1 px-1 py-0.5 bg-critical-100 text-critical-800 text-[9px] font-extrabold rounded">
                                  {group.accept_ru == null ? "Unconfigured" : "Exceeds Limit"}
                                </span>
                              )}
                            </span>
                          </div>
                        );
                      })()}
                    </div>
                    <div className="flex flex-col space-y-2 ml-2">
                      {invoiceFilter === "pending" && (
                        <>
                          <button
                            onClick={() =>
                              handleViewMeterReading(
                                group.acc_nbr,
                                group.area_cd,
                                group.added_blcy,
                                errorFilter
                              )
                            }
                            className="bg-navy-500 hover:bg-navy-600 text-white px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer shadow-xs"
                          >
                            <i className="fas fa-edit mr-1"></i>
                            <span>Edit</span>
                          </button>
                          <button
                            onClick={() => onPrepareInvoice(group)}
                            disabled={preparingInvoiceAccountNumber === group.acc_nbr}
                            className="bg-warning-500 hover:bg-warning-600 disabled:opacity-60 disabled:cursor-not-allowed text-white px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer shadow-xs"
                          >
                            {preparingInvoiceAccountNumber === group.acc_nbr ? (
                              <span className="ds-spinner ds-spinner-sm mr-1 align-middle" aria-hidden="true"></span>
                            ) : (
                              <i className="fas fa-file-invoice mr-1"></i>
                            )}
                            <span>Invoice</span>
                          </button>
                        </>
                      )}
                      <button
                        onClick={() => toggleExpand(group.acc_nbr)}
                        className="bg-success-600 hover:bg-success-700 text-white px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer shadow-xs"
                      >
                        {isExpanded ? "Hide" : "Show"}
                      </button>
                    </div>
                  </div>

                  {isExpanded && (
                    <div className="mt-3 space-y-2">
                      {group.readings.map((reading, readingIndex) => (
                        <div
                          key={`${reading.acc_nbr}-${reading.mtr_type}-${reading.rdng_date}-${readingIndex}`}
                          className={`p-3 rounded-xl border ${reading.has_error ? "border-critical-200 bg-critical-50" : "border-ink-200 bg-white"
                            }`}
                        >
                          <div className="space-y-1 text-xs">
                            <div className="flex justify-between">
                              <span className="text-ink-400 font-medium">Meter Type:</span>
                              <span className="font-bold text-ink-700">
                                {reading.mtr_type || "N/A"}
                                {reading.has_error && (
                                  <span className="ml-1.5 px-1 py-0.5 bg-critical-100 text-critical-800 text-[10px] font-bold rounded">
                                    Error
                                  </span>
                                )}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-ink-400 font-medium">Previous:</span>
                              <span className={`font-mono ${reading.has_error ? "text-critical-600 font-bold" : "text-ink-700 font-semibold"}`}>
                                {reading.prv_rdn || "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-ink-400 font-medium">Present:</span>
                              <span className={`font-mono ${reading.has_error ? "text-critical-600 font-bold" : "text-ink-700 font-semibold"}`}>
                                {reading.prsnt_rdn || "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-ink-400 font-medium">Units:</span>
                              <span className={`font-mono ${reading.has_error ? "text-critical-600 font-bold" : "text-ink-700 font-semibold"}`}>
                                {reading.units || "N/A"}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Pagination */}
      {!loading && groupedReadings.length > 0 && totalPages > 1 && (
        <div className="px-4 sm:px-6 py-4 border-t border-ink-200 bg-ink-50">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 sm:gap-0 text-ink-500">
            <div className="text-xs sm:text-sm font-medium order-2 sm:order-1">
              Page {currentPage} of {totalPages}
            </div>

            <div className="flex items-center space-x-2 order-1 sm:order-2">
              <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-3.5 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded-xl hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
              >
                ◀ Previous
              </button>

              <div className="flex items-center space-x-1">
                {getPageNumbers().map((page, index) => (
                  <React.Fragment key={index}>
                    {page === "..." ? (
                      <span className="px-2 py-1.5 text-xs text-ink-400">
                        ...
                      </span>
                    ) : (
                      <button
                        onClick={() => onPageChange(page)}
                        className={`px-3 py-1 text-xs font-extrabold rounded-lg focus:outline-none cursor-pointer ${currentPage === page
                          ? "bg-success-600 text-white"
                          : "text-ink-600 bg-white border border-ink-200 hover:bg-ink-50"
                          }`}
                      >
                        {page}
                      </button>
                    )}
                  </React.Fragment>
                ))}
              </div>

              <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-3.5 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded-xl hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
              >
                Next ▶
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TempReadingsTable;
