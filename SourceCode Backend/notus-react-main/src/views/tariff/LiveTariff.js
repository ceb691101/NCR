// src/views/tariff/LiveTariff.js
// Live Tariff list under Tariff -> Live Tariff.
// Reuses the NCRE Developer List table/search/pagination theme and the Tariff
// Rate Report data source. The live rate is resolved by the backend.

import React, { useState, useEffect, useMemo } from "react";
import SearchInput from "components/SearchInput";
import { getLiveTariffs } from "../../services/liveTariffService";

const recordsPerPageOptions = [10, 20, 30, 40];

const searchFieldOptions = [
  { value: "developer_name", label: "Developer Name" },
  { value: "facility_name", label: "Project Name" },
  { value: "folio_no", label: "Folio Number" },
];

export default function LiveTariff() {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchField, setSearchField] = useState("developer_name");
  const [searchTerm, setSearchTerm] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("All Categories");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);

  const fetchLiveTariffs = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getLiveTariffs();
      setRows(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Live tariff load error:", err);
      setError(err.message || "Unable to load live tariffs.");
      setRows([]);
    } finally {
      setLoading(false);
      setCurrentPage(1);
    }
  };

  useEffect(() => {
    fetchLiveTariffs();
  }, []);

  // Distinct tariff categories from the live data (never hard-coded)
  const categories = useMemo(() => {
    const set = new Set();
    rows.forEach((row) => {
      const cat = row && row.tariff_desc ? String(row.tariff_desc).trim() : "";
      if (cat) set.add(cat);
    });
    return Array.from(set).sort((a, b) => a.localeCompare(b));
  }, [rows]);

  const selectedFieldLabel = searchFieldOptions.find((o) => o.value === searchField)?.label || "";

  const handleSearchFieldChange = (e) => {
    setSearchField(e.target.value);
    setSearchTerm("");
    setCurrentPage(1);
  };

  const handleSearch = (term) => {
    setSearchTerm(term);
    setCurrentPage(1);
  };

  const handleCategoryChange = (e) => {
    setCategoryFilter(e.target.value);
    setCurrentPage(1);
  };

  const handleRecordsPerPageChange = (e) => {
    setRecordsPerPage(parseInt(e.target.value, 10));
    setCurrentPage(1);
  };

  const handleRetry = () => {
    fetchLiveTariffs();
  };

  // Search by the selected field + Tariff Category filter together
  const filteredRows = useMemo(() => {
    const searchLower = searchTerm.trim().toLowerCase();
    return rows.filter((row) => {
      if (categoryFilter !== "All Categories" && (!row.tariff_desc || String(row.tariff_desc) !== categoryFilter)) {
        return false;
      }
      if (!searchLower) return true;
      const fieldValue = String(row && row[searchField] != null ? row[searchField] : "")
        .toLowerCase();
      return fieldValue.includes(searchLower);
    });
  }, [rows, searchTerm, searchField, categoryFilter]);

  const totalRecords = filteredRows.length;
  const totalPages = Math.max(1, Math.ceil(totalRecords / recordsPerPage));
  const safePage = Math.min(currentPage, totalPages);
  const startIndex = (safePage - 1) * recordsPerPage;
  const endIndex = startIndex + recordsPerPage;
  const currentRecords = filteredRows.slice(startIndex, endIndex);

  const isFiltered = searchTerm.trim() !== "" || categoryFilter !== "All Categories";

  const getPageNumbers = () => {
    const pages = [];
    const maxPagesToShow = 5;
    const maxPagesToShowMobile = 3;
    const isMobile = window.innerWidth < 768;
    const maxPages = isMobile ? maxPagesToShowMobile : maxPagesToShow;

    if (totalPages <= maxPages) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else if (safePage <= Math.ceil(maxPages / 2)) {
      for (let i = 1; i <= maxPages - 1; i++) pages.push(i);
      pages.push("...");
      pages.push(totalPages);
    } else if (safePage >= totalPages - Math.floor(maxPages / 2)) {
      pages.push(1);
      pages.push("...");
      for (let i = totalPages - (maxPages - 2); i <= totalPages; i++) pages.push(i);
    } else {
      pages.push(1);
      pages.push("...");
      if (isMobile) {
        pages.push(safePage);
      } else {
        for (let i = safePage - 1; i <= safePage + 1; i++) pages.push(i);
      }
      pages.push("...");
      pages.push(totalPages);
    }
    return pages;
  };

  const columnCount = 6;

  return (
    <div className="flex flex-col gap-ds-6">
      <div className="w-full">
        <div className="ds-card">
          {/* Card Header */}
          <div className="ds-page-header">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h6 className="text-ink-700 text-xl font-bold">Live Tariff</h6>
                <p className="text-xs text-ink-400 mt-1">
                  Currently applicable developer tariff rates
                </p>
              </div>
              <button
                type="button"
                onClick={handleRetry}
                disabled={loading}
                className={`ds-btn ds-btn-primary ${
                  loading ? "bg-ink-400 cursor-not-allowed" : "bg-navy-800 hover:bg-navy-900"
                }`}
              >
                <i className={`fas ${loading ? "fa-spinner fa-spin" : "fa-sync-alt"} mr-2`}></i>
                Refresh
              </button>
            </div>
          </div>

          {/* Card Content */}
          <div className="flex-auto px-4 lg:px-6 py-6">
            {error ? (
              <div className="px-4 py-8 text-center text-critical-600 bg-critical-50 rounded-lg border border-critical-200">
                <i className="fas fa-exclamation-circle mr-2"></i>
                Unable to load live tariffs: {error}
                <div className="mt-3">
                  <button
                    type="button"
                    onClick={handleRetry}
                    className="ds-btn ds-btn-danger ds-btn-sm"
                  >
                    Retry
                  </button>
                </div>
              </div>
            ) : (
              <div className="ds-card overflow-hidden">
                {/* Search + Filter + Records Per Page Controls */}
                <div className="flex flex-col xl:flex-row justify-between items-start xl:items-center px-4 sm:px-6 py-4 border-b border-ink-200 space-y-3 xl:space-y-0 bg-ink-50/55">
                  {/* Search: field selector + input */}
                  <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-3 w-full xl:w-auto">
                    <select
                      value={searchField}
                      onChange={handleSearchFieldChange}
                      className="border border-ink-200 rounded-xl px-2 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 w-full sm:w-auto min-w-[150px] bg-white text-ink-700 cursor-pointer"
                      title="Select search field"
                    >
                      {searchFieldOptions.map((opt) => (
                        <option key={opt.value} value={opt.value}>
                          {opt.label}
                        </option>
                      ))}
                    </select>

                    <SearchInput
                      value={searchTerm}
                      onChange={(e) => handleSearch(e.target.value)}
                      onClear={() => handleSearch("")}
                      placeholder={`Search ${selectedFieldLabel}...`}
                      className="max-w-md xl:w-72"
                    />
                  </div>

                  {/* Filter (Tariff Category) + Records Per Page dropdowns */}
                  <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-3 sm:space-y-0 sm:space-x-3 w-full xl:w-auto">
                    <div className="flex items-center space-x-2 flex-1 sm:flex-none">
                      <span className="text-xs sm:text-sm text-ink-500 whitespace-nowrap font-medium">
                        Filter:
                      </span>
                      <select
                        value={categoryFilter}
                        onChange={handleCategoryChange}
                        className="border border-ink-200 rounded-xl px-2 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 w-full sm:w-auto min-w-[160px] bg-white text-ink-700 cursor-pointer"
                        title="Filter by tariff category"
                      >
                        <option value="All Categories">All Categories</option>
                        {categories.map((cat) => (
                          <option key={cat} value={cat}>
                            {cat}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="flex items-center space-x-2 flex-1 sm:flex-none">
                      <span className="text-xs sm:text-sm text-ink-500 whitespace-nowrap font-medium">
                        Show:
                      </span>
                      <select
                        value={recordsPerPage}
                        onChange={handleRecordsPerPageChange}
                        className="border border-ink-200 rounded-xl px-2 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 w-full sm:w-auto min-w-[130px] bg-white text-ink-700 cursor-pointer"
                      >
                        {recordsPerPageOptions.map((opt) => (
                          <option key={opt} value={opt}>
                            {opt} records
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                {/* Active filter summary */}
                {isFiltered && !loading && (
                  <div className="flex flex-wrap items-center gap-2 px-4 sm:px-6 py-2.5 border-b border-ink-200 bg-ink-50/50">
                    {searchTerm.trim() && (
                      <span className="inline-flex items-center px-2.5 py-1 text-xs font-bold text-navy-800 bg-navy-100 border border-navy-200/60 rounded-lg">
                        {selectedFieldLabel}: "{searchTerm.trim()}"
                        <button
                          type="button"
                          onClick={() => handleSearch("")}
                          className="ml-1.5 text-navy-800/60 hover:text-navy-800 cursor-pointer"
                          title="Clear search"
                        >
                          <i className="fas fa-times text-[10px]"></i>
                        </button>
                      </span>
                    )}
                    {categoryFilter !== "All Categories" && (
                      <span className="inline-flex items-center px-2.5 py-1 text-xs font-bold text-navy-800 bg-navy-100 border border-navy-200/60 rounded-lg">
                        Category: {categoryFilter}
                        <button
                          type="button"
                          onClick={() => {
                            setCategoryFilter("All Categories");
                            setCurrentPage(1);
                          }}
                          className="ml-1.5 text-navy-800/60 hover:text-navy-800 cursor-pointer"
                          title="Clear category filter"
                        >
                          <i className="fas fa-times text-[10px]"></i>
                        </button>
                      </span>
                    )}
                  </div>
                )}

                {/* Results Info */}
                {!loading && (
                  <div className="px-4 sm:px-6 py-2.5 text-xs text-ink-400 border-b border-ink-200 font-semibold uppercase tracking-wider bg-ink-50/20">
                    Showing{" "}
                    {currentRecords.length > 0 ? startIndex + 1 : 0} to{" "}
                    {Math.min(safePage * recordsPerPage, totalRecords)} of{" "}
                    {totalRecords} live tariffs
                  </div>
                )}

                {/* Table - Desktop View */}
                <div className="hidden md:block overflow-x-auto">
                  <div className="min-w-full">
                    <table className="w-full border-collapse">
                      <thead className="bg-ink-50">
                        <tr className="border-b border-ink-200">
                          <th className="ds-th w-14">
                            No.
                          </th>
                          <th className="ds-th">
                            Folio Number
                          </th>
                          <th className="ds-th">
                            Developer Name
                          </th>
                          <th className="ds-th">
                            Project Name
                          </th>
                          <th className="ds-th">
                            Tariff Category
                          </th>
                          <th className="ds-th">
                            Tariff Rate (Rs/kWh)
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-ink-100 text-sm text-ink-700">
                        {loading ? (
                          <tr>
                            <td colSpan={columnCount} className="px-6 py-12 text-center">
                              <div className="flex justify-center items-center">
                                <div className="ds-spinner"></div>
                                <span className="ml-3 text-ink-500 font-medium">
                                  Loading live tariffs...
                                </span>
                              </div>
                            </td>
                          </tr>
                        ) : currentRecords.length === 0 ? (
                          <tr>
                            <td colSpan={columnCount} className="px-6 py-12 text-center">
                              <div className="text-ink-400 py-6">
                                <i className="fas fa-bolt text-lg text-ink-400"></i>
                                <p className="font-bold text-ink-700">
                                  {isFiltered
                                    ? "No live tariffs found matching search / filter"
                                    : "No live tariffs found"}
                                </p>
                              </div>
                            </td>
                          </tr>
                        ) : (
                          currentRecords.map((row) => (
                            <tr key={row.row_no} className="hover:bg-ink-50 transition-all duration-150">
                              <td className="px-4 py-3 whitespace-nowrap text-ink-500">
                                {row.row_no}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap font-medium text-ink-800">
                                {row.folio_no !== "" ? row.folio_no : "N/A"}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap text-ink-700">
                                {row.developer_name !== "" ? row.developer_name : "N/A"}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap text-ink-600">
                                {row.facility_name !== "" ? row.facility_name : "N/A"}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap">
                                {row.tariff_desc ? (
                                  <span className="inline-flex px-2.5 py-1 text-xs font-bold rounded-lg border border-navy-200/60 bg-navy-100 text-navy-800">
                                    {row.tariff_desc}
                                  </span>
                                ) : (
                                  <span className="text-ink-400">N/A</span>
                                )}
                              </td>
                              <td className="px-4 py-3 whitespace-nowrap font-bold text-ink-800">
                                {row.live_tariff_rate !== "" && row.live_tariff_rate != null
                                  ? row.live_tariff_rate
                                  : "N/A"}
                              </td>
                            </tr>
                          ))
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
                        <span className="ml-3 text-ink-500 text-sm">Loading live tariffs...</span>
                      </div>
                    </div>
                  ) : currentRecords.length === 0 ? (
                    <div className="px-4 py-12 text-center">
                      <div className="text-ink-400">
                        <i className="fas fa-bolt text-lg text-ink-400"></i>
                        <p className="font-bold text-ink-700">
                          {isFiltered
                            ? "No live tariffs found matching search / filter"
                            : "No live tariffs found"}
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="divide-y divide-ink-100 bg-white">
                      {currentRecords.map((row) => (
                        <div key={row.row_no} className="p-4 transition-all hover:bg-ink-50">
                          <div className="space-y-2">
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">No.:</span>
                              <span className="text-sm font-medium text-ink-600">{row.row_no}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">Folio Number:</span>
                              <span className="text-sm font-mono font-medium text-ink-800">
                                {row.folio_no !== "" ? row.folio_no : "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">Developer:</span>
                              <span className="text-sm font-normal text-ink-700">
                                {row.developer_name !== "" ? row.developer_name : "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">Project:</span>
                              <span className="text-sm font-medium text-ink-600">
                                {row.facility_name !== "" ? row.facility_name : "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">Tariff Category:</span>
                              <span className="text-sm text-navy-800 font-bold">
                                {row.tariff_desc || "N/A"}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-xs text-ink-400 uppercase">Tariff Rate (Rs/kWh):</span>
                              <span className="text-sm font-bold text-ink-800">
                                {row.live_tariff_rate !== "" && row.live_tariff_rate != null
                                  ? row.live_tariff_rate
                                  : "N/A"}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Pagination */}
                {!loading && currentRecords.length > 0 && totalPages > 1 && (
                  <div className="px-4 sm:px-6 py-4 border-t border-ink-200 bg-ink-50">
                    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 sm:gap-0 text-ink-500">
                      <div className="text-xs sm:text-sm font-medium order-2 sm:order-1">
                        Page {safePage} of {totalPages}
                      </div>
                      <div className="flex items-center space-x-2 order-1 sm:order-2">
                        <button
                          onClick={() => setCurrentPage(safePage - 1)}
                          disabled={safePage === 1}
                          className="px-3.5 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded-xl hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
                        >
                          ◀ Previous
                        </button>
                        <div className="flex items-center space-x-1">
                          {getPageNumbers().map((page, index) => (
                            <React.Fragment key={index}>
                              {page === "..." ? (
                                <span className="px-2 py-1.5 text-xs text-ink-400">...</span>
                              ) : (
                                <button
                                  onClick={() => setCurrentPage(page)}
                                  className={`px-3 py-1 text-xs font-extrabold rounded-lg focus:outline-none cursor-pointer ${
                                    safePage === page
                                      ? "bg-navy-800 text-white"
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
                          onClick={() => setCurrentPage(safePage + 1)}
                          disabled={safePage === totalPages}
                          className="px-3.5 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded-xl hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
                        >
                          Next ▶
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}