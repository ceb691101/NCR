import React from "react";
import { useHistory } from "react-router-dom";

const PendReadingsTable = ({
  customers,
  loading,
  searchTerm,
  onSearch,
  recordsPerPage,
  onRecordsPerPageChange,
  currentPage,
  totalPages,
  totalRecords,
  onPageChange,
  onInsertReading,
  areaInfo,
}) => {
  const history = useHistory();

  const handleSearchChange = (e) => {
    onSearch(e.target.value);
  };

  const handleRecordsPerPageChange = (e) => {
    onRecordsPerPageChange(parseInt(e.target.value));
  };

  const handleInsertClick = (customer) => {
    const params = new URLSearchParams();
    params.append("account", customer.acc_nbr);
    params.append("area", customer.area_cd);

    const activeBillCycle = areaInfo?.active_bill_cycle;
    if (activeBillCycle) {
      params.append("billCycle", activeBillCycle);
    } else if (customer.bill_cycle) {
      params.append("billCycle", customer.bill_cycle);
    }

    // If a reading already exists for this cycle, open the received flow so the
    // existing readings are loaded and can be corrected/updated.
    if (customer.has_reading) {
      params.append("source", "tempReadings");
    }

    history.push(`/monthlyReadings/readingsEntry?${params.toString()}`);
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
              placeholder="Search by folio, name, project..."
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
              className="border border-ink-200 rounded-xl px-7 py-2 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800 max-w-32 bg-white text-ink-700 cursor-pointer "
            >
              <option value={10}>10 records</option>
              <option value={20}>20 records</option>
              <option value={30}>30 records</option>
              <option value={40}>40 records</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results Info */}
      {!loading && (
        <div className="px-4 sm:px-6 py-2.5 text-xs text-ink-400 border-b border-ink-200 font-semibold uppercase tracking-wider bg-ink-50/20">
          Showing{" "}
          {customers.length > 0 ? (currentPage - 1) * recordsPerPage + 1 : 0}{" "}
          to {Math.min(currentPage * recordsPerPage, totalRecords)} of{" "}
          {totalRecords} customers
        </div>
      )}

      {/* Table - Desktop View */}
      <div className="hidden md:block overflow-x-auto">
        <div className="min-w-full">
          <table className="w-full border-collapse">
            <thead className="bg-ink-50">
              <tr className="border-b border-ink-200">
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
                  Tariff Type
                </th>
                <th className="ds-th text-center">Status</th>
                <th className="ds-th text-center">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-ink-100 text-sm text-ink-700">
              {loading ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center">
                    <div className="flex justify-center items-center">
                      <div className="ds-spinner"></div>
                      <span className="ml-3 text-ink-500 font-medium">
                        Loading pending readings...
                      </span>
                    </div>
                  </td>
                </tr>
              ) : customers.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center">
                    <div className="text-ink-400 py-6">
                      <i className="fas fa-inbox mb-3 block text-2xl text-ink-300" aria-hidden="true"></i>
                      <p className="font-bold text-ink-700">
                        {searchTerm
                          ? "No customers found matching your search"
                          : "No developers found for this area"}
                      </p>
                      <p className="text-xs text-ink-400 mt-1">
                        {searchTerm
                          ? "Try adjusting your search criteria"
                          : "There are no developers registered in this area"}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                customers.map((customer, index) => (
                  <tr
                    key={customer.acc_nbr}
                    className={`transition-all duration-150 ${
                      customer.has_reading
                        ? "bg-success-50 hover:bg-success-50"
                        : index % 2 === 0
                        ? "bg-white hover:bg-ink-50"
                        : "bg-ink-50/50 hover:bg-ink-50"
                    }`}
                  >
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
                      <div className="text-xs font-semibold text-ink-700">
                        {customer.tariff_desc || customer.tariff_type || "N/A"}
                      </div>
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap text-center">
                      {customer.has_reading ? (
                        <span className="ds-badge ds-badge-success">
                          <i className="fas fa-check-circle"></i> Received
                        </span>
                      ) : (
                        <span className="ds-badge ds-badge-warning">
                          <i className="fas fa-clock"></i> Not Received
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => handleInsertClick(customer)}
                        className="ds-btn ds-btn-primary ds-btn-sm mx-auto"
                        title={customer.has_reading ? "Update meter reading" : "Insert meter reading"}
                      >
                        <i
                          className={`fas ${customer.has_reading ? "fa-edit" : "fa-plus"} mr-1 text-white/80`}
                        ></i>
                        <span>{customer.has_reading ? "Update Reading" : "Insert Reading"}</span>
                      </button>
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
              <span className="ml-3 text-ink-500 text-sm">
                Loading pending readings...
              </span>
            </div>
          </div>
        ) : customers.length === 0 ? (
          <div className="px-4 py-12 text-center">
            <div className="text-ink-400">
              <i className="fas fa-inbox mb-3 block text-2xl text-ink-300" aria-hidden="true"></i>
              <p className="font-bold text-ink-700">
                {searchTerm
                  ? "No customers found matching your search"
                  : "No developers found for this area"}
              </p>
            </div>
          </div>
        ) : (
          <div className="divide-y divide-ink-100 bg-white">
            {customers.map((customer, index) => (
              <div
                key={customer.acc_nbr}
                className={`p-4 transition-all ${
                  customer.has_reading
                    ? "bg-success-50 hover:bg-success-50"
                    : index % 2 === 0
                    ? "bg-white hover:bg-ink-50"
                    : "bg-ink-50/50 hover:bg-ink-50"
                }`}
              >
                <div className="space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1 space-y-1">
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Folio:
                        </span>
                        <span className="text-sm font-mono font-medium text-ink-800">
                          {customer.folio_no ?? "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Name:
                        </span>
                        <span className="text-sm font-normal text-ink-700">
                          {customer.name || "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Project:
                        </span>
                        <span className="text-sm font-medium text-ink-600">
                          {customer.facility_name || "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs text-ink-400 font-bold uppercase">
                          Tariff Type:
                        </span>
                        <span className="text-sm font-medium text-ink-700">
                          {customer.tariff_desc || customer.tariff_type || "N/A"}
                        </span>
                      </div>
                      <div className="flex items-center space-x-2 pt-1">
                        {customer.has_reading ? (
                          <span className="ds-badge ds-badge-success">
                            <i className="fas fa-check-circle"></i> Received
                          </span>
                        ) : (
                          <span className="ds-badge ds-badge-warning">
                            <i className="fas fa-clock"></i> Not Received
                          </span>
                        )}
                      </div>
                    </div>
                    <button
                      onClick={() => handleInsertClick(customer)}
                      className="ds-btn ds-btn-primary ds-btn-sm ml-2 flex-shrink-0"
                      title={customer.has_reading ? "Update meter reading" : "Insert meter reading"}
                    >
                      <i
                        className={`fas ${customer.has_reading ? "fa-edit" : "fa-plus"} mr-1`}
                      ></i>
                      <span>{customer.has_reading ? "Update" : "Insert"}</span>
                    </button>
                  </div>
                </div>
              </div>
            ))}
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
                className="px-3 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
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
                        className={`px-3 py-1 text-xs font-bold focus:outline-none select-none cursor-pointer rounded ${
                          currentPage === page
                            ? "bg-success-500 text-white"
                            : "border border-ink-200 bg-white text-ink-600 hover:bg-ink-50"
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
                className="px-3 py-1.5 text-xs font-bold text-ink-600 bg-white border border-ink-200 rounded hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none cursor-pointer"
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

export default PendReadingsTable;