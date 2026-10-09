import React, { useState } from "react";
import { toast } from "react-toastify";
import {
  getRowColor,
  getReadingStatusText,
  getReadingStatusBadge,
} from "services/readingStatusService";
import { updateDeveloperStatus } from "services/bulkCustomersService";
import GenerationGraph from "./GenerationGraph";

const StatusToggle = ({ status, onToggle, disabled }) => {
  const isActive = status === 2;

  return (
    <button
      onClick={onToggle}
      disabled={disabled}
      className={`
        relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed
        ${
          isActive
            ? "bg-success-500 focus:ring-success-500"
            : "bg-ink-300 focus:ring-ink-400"
        }
      `}
      title={`Click to ${isActive ? "deactivate" : "activate"} developer`}
    >
      <span
        className={`
          inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ease-in-out
          ${isActive ? "translate-x-6" : "translate-x-1"}
        `}
      />
    </button>
  );
};

const BulkCustomersTable = ({
  customers,
  loading,
  searchTerm,
  onSearch,
  recordsPerPage,
  onRecordsPerPageChange,
  sortOption,
  onSortOptionChange,
  currentPage,
  totalPages,
  totalRecords,
  onPageChange,
  onViewCustomer,
  onUpdateLocation,
  areaInfo,
}) => {
  const [expandedCustomer, setExpandedCustomer] = useState(null);
  const [updatingMap, setUpdatingMap] = useState({});
  const [statusOverrides, setStatusOverrides] = useState({});

  const handleToggleDeveloperStatus = async (accNbr, currentStatus) => {
    if (!accNbr || updatingMap[accNbr]) return;

    const newStatus = currentStatus === 2 ? 1 : 2;

    setUpdatingMap((prev) => ({ ...prev, [accNbr]: true }));
    try {
      await updateDeveloperStatus(accNbr, newStatus);
      setStatusOverrides((prev) => ({ ...prev, [accNbr]: newStatus }));
      toast.success(
        `Developer status updated to ${newStatus === 2 ? "Active" : "Inactive"}`
      );
    } catch (err) {
      console.error("Error toggling developer status:", err);
      toast.error(err.message || "Failed to update developer status");
    } finally {
      setUpdatingMap((prev) => ({ ...prev, [accNbr]: false }));
    }
  };

  const handleSearchChange = (e) => {
    onSearch(e.target.value);
  };

  const handleRecordsPerPageChange = (e) => {
    onRecordsPerPageChange(parseInt(e.target.value));
  };

  const handleSortOptionChange = (e) => {
    onSortOptionChange(e.target.value);
  };

  const handleViewClick = (customer) => {
    onViewCustomer(customer);
  };

  const handleUpdateLocationClick = (customer) => {
    if (onUpdateLocation) {
      onUpdateLocation(customer);
    }
  };

  const toggleCustomerExpand = (customer) => {
    if (expandedCustomer && expandedCustomer.acc_nbr === customer.acc_nbr) {
      setExpandedCustomer(null);
    } else {
      setExpandedCustomer(customer);
    }
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

  return (
    <div className="ds-card overflow-hidden">
      {/* Search and Records Per Page Controls */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center px-4 sm:px-6 py-4 border-b border-ink-200 space-y-3 lg:space-y-0 bg-ink-50/55">
        {/* Search Input */}
        <div className="flex items-center space-x-2 w-full lg:w-auto">
          <div className="relative flex-1 lg:flex-initial min-w-0">
            <i className="fas fa-search absolute left-3.5 top-1/2 transform -translate-y-1/2 text-ink-400"></i>
            <input
              type="text"
              placeholder="Search developers or projects..."
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

        {/* Records Per Page and Sort Dropdowns */}
        <div className="flex flex-row items-center space-x-2 w-full lg:w-auto mt-3 lg:mt-0">
          <div className="flex items-center space-x-2 flex-1 sm:flex-none">
            <span className="text-xs sm:text-sm text-ink-500 whitespace-nowrap font-medium">
              Sort:
            </span>
            <select
              value={sortOption}
              onChange={handleSortOptionChange}
              className="border border-ink-200 rounded-xl px-2 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 w-full sm:w-auto min-w-[140px] bg-white text-ink-700 cursor-pointer"
            >
              <option value="acc_num_asc">Acc Num Ascending</option>
              <option value="status_received">Status: Received</option>
              <option value="status_not_received">Status: Not Received</option>
              <option value="name_asc">Name: A-Z</option>
              <option value="folio_num_asc">FOLIO Number Ascending</option>
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
              <option value={10}>10 records</option>
              <option value={20}>20 records</option>
              <option value={30}>30 records</option>
              <option value={40}>40 records</option>
            </select>
          </div>
        </div>
      </div>

      {/* Reading Status Legend */}
      <div className="px-4 sm:px-6 py-2.5 border-b border-ink-200 bg-ink-50/50">
        <div className="flex flex-wrap items-center gap-3 text-xs">
          <div className="flex items-center">
            <span className="px-2 py-0.5 bg-success-100 text-success-800 border border-success-200 rounded-lg font-bold mr-2">Received</span>
            <span className="text-ink-500">Reading Received</span>
          </div>
          <div className="flex items-center">
            <span className="px-2 py-0.5 bg-critical-100 text-critical-800 border border-critical-200 rounded-lg font-bold mr-2">Not Received</span>
            <span className="text-ink-500">Reading Not Received</span>
          </div>
          <div className="flex items-center">
            <span className="px-2 py-0.5 bg-navy-100 text-navy-800 border border-navy-200 rounded-lg font-bold mr-2">Generation</span>
            <span className="text-ink-500">Click chevron arrow for generation graph</span>
          </div>
        </div>
      </div>

      {/* Results Info */}
      {!loading && (
        <div className="px-4 sm:px-6 py-2.5 text-xs text-ink-400 border-b border-ink-200 font-semibold uppercase tracking-wider bg-ink-50/20">
          Showing{" "}
          {customers.length > 0 ? (currentPage - 1) * recordsPerPage + 1 : 0} to{" "}
          {Math.min(currentPage * recordsPerPage, totalRecords)} of{" "}
          {totalRecords} developers
        </div>
      )}

      {/* Table - Desktop View */}
      <div className="hidden md:block overflow-x-auto">
        <div className="min-w-full">
          <table className="w-full border-collapse">
            <thead className="bg-ink-50">
              <tr className="border-b border-ink-200">
                <th className="ds-th w-12">
                  {/* Expand */}
                </th>
                <th className="ds-th">
                  Folio Number
                </th>
                <th className="ds-th">
                  Developer Name
                </th>
                <th className="ds-th">
                  Project / Facility
                </th>
                <th className="ds-th">
                  Reading Status
                </th>
                <th className="px-4 py-3 text-center text-xs font-bold text-ink-500 uppercase tracking-wider">
                  Action
                </th>
                <th className="px-4 py-3 text-center text-xs font-bold text-ink-500 uppercase tracking-wider">
                  Status
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-ink-100 text-sm text-ink-700">
              {loading ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center">
                    <div className="flex justify-center items-center">
                      <div className="ds-spinner"></div>
                      <span className="ml-3 text-ink-500 font-medium">
                        Loading ncre developers...
                      </span>
                    </div>
                  </td>
                </tr>
              ) : customers.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center">
                    <div className="text-ink-400 py-6">
                      <i className="fas fa-users text-lg text-ink-400"></i>
                      <p className="font-bold text-ink-700">
                        {searchTerm ? "No developers found matching search" : "No NCRE developers found"}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                customers.map((customer, index) => {
                  const rowColor = getRowColor(customer.has_reading);
                  const isExpanded = expandedCustomer && expandedCustomer.acc_nbr === customer.acc_nbr;

                  return (
                    <React.Fragment key={customer.acc_nbr}>
                      <tr className={`${rowColor} transition-all duration-150`}>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <button
                            onClick={() => toggleCustomerExpand(customer)}
                            className={`w-7 h-7 rounded-lg flex items-center justify-center transition-all duration-200 focus:outline-none cursor-pointer ${
                              isExpanded
                                ? "bg-navy-100 text-navy-600 hover:bg-navy-200"
                                : "bg-ink-100 text-ink-600 hover:bg-ink-200"
                            }`}
                            title={isExpanded ? "Hide generation graph" : "Show generation graph"}
                          >
                            <i className={`fas ${isExpanded ? "fa-chevron-down" : "fa-chevron-right"} text-xs`}></i>
                          </button>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <div className="text-sm font-mono font-medium text-ink-800">
                            {customer.folio_no ?? "N/A"}
                          </div>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <div className="text-sm font-normal text-ink-700">
                            {customer.name || "N/A"}
                          </div>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-ink-600">
                            {customer.facility_name || "N/A"}
                          </div>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex px-2.5 py-1 text-xs font-bold rounded-lg border ${getReadingStatusBadge(
                              customer.has_reading
                            )}`}
                          >
                            {getReadingStatusText(customer.has_reading)}
                          </span>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap text-center">
                          <div className="flex flex-col sm:flex-row items-center justify-center gap-2">
                            <button
                              onClick={() => handleUpdateLocationClick(customer)}
                              className="bg-navy-500 hover:bg-navy-600 text-white px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center focus:outline-none whitespace-nowrap cursor-pointer shadow-xs"
                              title="Update location"
                            >
                              <i className="fas fa-map-marker-alt mr-1"></i>
                              <span>Location</span>
                            </button>
                            <button
                              onClick={() => handleViewClick(customer)}
                              className="bg-success-500 hover:bg-success-600 text-white px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center focus:outline-none whitespace-nowrap cursor-pointer shadow-xs"
                              title="View Details"
                            >
                              <i className="fas fa-eye mr-1"></i>
                              <span>View</span>
                            </button>
                          </div>
                        </td>
                        <td className="px-4 py-4 whitespace-nowrap text-center">
                          {(() => {
                            const currentStatus =
                              statusOverrides[customer.acc_nbr] !== undefined
                                ? statusOverrides[customer.acc_nbr]
                                : customer.status !== undefined &&
                                  customer.status !== null
                                ? Number(customer.status)
                                : 2;

                            return (
                              <div className="flex flex-col items-center space-y-1">
                                <StatusToggle
                                  status={currentStatus}
                                  onToggle={() =>
                                    handleToggleDeveloperStatus(
                                      customer.acc_nbr,
                                      currentStatus
                                    )
                                  }
                                  disabled={updatingMap[customer.acc_nbr]}
                                />
                                <span
                                  className={`text-xs font-medium ${
                                    currentStatus === 2
                                      ? "text-success-600"
                                      : "text-ink-500"
                                  }`}
                                >
                                  {currentStatus === 2 ? "Active" : "Inactive"}
                                </span>
                              </div>
                            );
                          })()}
                        </td>
                      </tr>

                      {/* Expanded Row - Generation Graph */}
                      {isExpanded && (
                        <tr>
                          <td colSpan="7" className="px-0 py-0 bg-ink-50/20">
                            <div className="bg-ink-50/30 border-l-4 border-navy-500 p-4">
                              <div className="overflow-hidden rounded-xl border border-ink-200 bg-white p-4">
                                <GenerationGraph
                                  accNbr={customer.acc_nbr}
                                  areaCd={customer.area_cd}
                                />
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
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
                Loading ncre developers...
              </span>
            </div>
          </div>
        ) : customers.length === 0 ? (
          <div className="px-4 py-12 text-center">
            <div className="text-ink-400">
              <i className="fas fa-users text-lg text-ink-400"></i>
              <p className="font-bold text-ink-700">
                {searchTerm ? "No developers found matching search" : "No NCRE developers found"}
              </p>
            </div>
          </div>
        ) : (
          <div className="divide-y divide-ink-100 bg-white">
            {customers.map((customer) => {
              const isExpanded = expandedCustomer && expandedCustomer.acc_nbr === customer.acc_nbr;
              const cardColor =
                customer.has_reading === true
                  ? "bg-success-50 border-success-200"
                  : customer.has_reading === false
                  ? "bg-critical-50 border-critical-200"
                  : "bg-white border-ink-200";

              return (
                <div key={customer.acc_nbr} className={`p-4 transition-all ${cardColor} hover:bg-ink-50`}>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-xs text-ink-500 uppercase">Folio Number:</span>
                      <span className="text-sm font-mono font-medium text-ink-800">
                        {customer.folio_no ?? "N/A"}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-xs text-ink-500 uppercase">Name:</span>
                      <span className="text-sm font-normal text-ink-700">
                        {customer.name || "N/A"}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-xs text-ink-500 uppercase">PROJECT:</span>
                      <span className="text-sm font-medium text-ink-600">
                        {customer.facility_name || "N/A"}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-xs text-ink-500 uppercase">Reading Status:</span>
                      <span
                        className={`text-xs font-bold px-2 py-0.5 rounded border ${getReadingStatusBadge(
                          customer.has_reading
                        )}`}
                      >
                        {getReadingStatusText(customer.has_reading)}
                      </span>
                    </div>
                    {(() => {
                      const currentStatus =
                        statusOverrides[customer.acc_nbr] !== undefined
                          ? statusOverrides[customer.acc_nbr]
                          : customer.status !== undefined &&
                            customer.status !== null
                          ? Number(customer.status)
                          : 2;

                      return (
                        <div className="flex justify-between items-center pt-1">
                          <span className="text-xs text-ink-500 uppercase font-semibold">Status:</span>
                          <div className="flex items-center space-x-2">
                            <StatusToggle
                              status={currentStatus}
                              onToggle={() =>
                                handleToggleDeveloperStatus(
                                  customer.acc_nbr,
                                  currentStatus
                                )
                              }
                              disabled={updatingMap[customer.acc_nbr]}
                            />
                            <span
                              className={`text-xs font-bold ${
                                currentStatus === 2
                                  ? "text-success-600"
                                  : "text-ink-500"
                              }`}
                            >
                              {currentStatus === 2 ? "Active" : "Inactive"}
                            </span>
                          </div>
                        </div>
                      );
                    })()}
                    
                    {/* Action Buttons */}
                    <div className="flex flex-col space-y-2 mt-3 pt-2 border-t border-ink-200/50">
                      <button
                        onClick={() => handleUpdateLocationClick(customer)}
                        className="w-full bg-navy-500 hover:bg-navy-600 text-white px-3.5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center focus:outline-none cursor-pointer shadow-xs"
                      >
                        <i className="fas fa-map-marker-alt mr-1"></i>
                        Update Location
                      </button>

                      <button
                        onClick={() => handleViewClick(customer)}
                        className="w-full bg-navy-800 hover:bg-navy-900 text-white px-3.5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center focus:outline-none cursor-pointer shadow-xs"
                      >
                        <i className="fas fa-eye mr-1"></i>
                        View Details
                      </button>
                      
                      <button
                        onClick={() => toggleCustomerExpand(customer)}
                        className={`w-full px-3.5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center focus:outline-none cursor-pointer shadow-xs ${
                          isExpanded
                            ? "bg-navy-600 hover:bg-navy-700 text-white"
                            : "bg-ink-200 hover:bg-ink-400 text-ink-700"
                        }`}
                      >
                        <i className={`fas ${isExpanded ? "fa-chevron-up" : "fa-chevron-down"} mr-1`}></i>
                        {isExpanded ? "Hide Graph" : "Show Graph"}
                      </button>
                    </div>
                    
                    {/* Expanded Section - Generation Graph for Mobile */}
                    {isExpanded && (
                      <div className="mt-4 pt-4 border-t border-ink-200">
                        <GenerationGraph
                          accNbr={customer.acc_nbr}
                          areaCd={customer.area_cd}
                        />
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Pagination */}
      {!loading && customers.length > 0 && totalPages > 1 && (
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
                        className={`px-3 py-1 text-xs font-extrabold rounded-lg focus:outline-none cursor-pointer ${
                          currentPage === page
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

export default BulkCustomersTable;