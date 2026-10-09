// E:\Projects\CEB_MYPROJECTS\HSB PROJECT\FRONTEND\HSBFrontend\notus-react-main\src\views\Amendment\BulkCustomerAmendmentView.js
import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { getSelectedAreaCode } from "services/AreaAndBillService";
import BulkCustomerAmendmentForm from "components/BulkCustomersComp/BulkCustomerAmendmentForm";

const BulkCustomerAmendmentView = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [areaInfo, setAreaInfo] = useState({
    area_code: null,
    area_name: null,
    total_customers: 0,
  });
  const [expandedAmendments, setExpandedAmendments] = useState(new Set());
  const baseUrl = process.env.REACT_APP_API_BASE_URL;

  useEffect(() => {
    loadBulkCustomersWithAmendmentStatus();
  }, []);

  const loadBulkCustomersWithAmendmentStatus = async () => {
    const selectedAreaCode = getSelectedAreaCode();
    if (!selectedAreaCode) {
      toast.warning("Please select an area first.");
      setCustomers([]);
      setAreaInfo({
        area_code: null,
        area_name: null,
        total_customers: 0,
      });
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(
        `${baseUrl}/api/v1/bulk-customers/area/${selectedAreaCode}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
        }
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const customerResponse = await response.json();
      const customerData = customerResponse.customers || [];
      // Fetch amendment status for each customer
      const amendmentPromises = customerData.map(async (customer) => {
        try {
          const response = await fetch(
            `${baseUrl}/api/v1/amendments/${customer.acc_nbr}`,
            {
              method: "GET",
              headers: {
                "Content-Type": "application/json",
                Authorization: "Basic " + btoa("user:admin123"),
              },
            }
          );
          if (response.ok) {
            const data = await response.json();
            return {
              ...customer,
              has_amendment: true,
              amendment: data.amendment,
            };
          } else if (response.status === 404) {
            return { ...customer, has_amendment: false, amendment: null };
          } else {
            throw new Error("Failed to fetch amendment");
          }
        } catch (err) {
          return { ...customer, has_amendment: false, amendment: null };
        }
      });
      const customersWithAmendments = await Promise.all(amendmentPromises);
      setCustomers(customersWithAmendments);
      setAreaInfo({
        area_code: selectedAreaCode,
        area_name: selectedAreaCode,
        total_customers: customersWithAmendments.length,
      });
    } catch (err) {
      console.error("Error loading ncre developers:", err);
      toast.error("Failed to fetch ncre developers.");
      setCustomers([]);
      setAreaInfo({
        area_code: selectedAreaCode,
        area_name: null,
        total_customers: 0,
      });
    } finally {
      setLoading(false);
    }
  };
  const toggleAmendment = (accNbr) => {
    const newExpandedAmendments = new Set(expandedAmendments);
    if (newExpandedAmendments.has(accNbr)) {
      newExpandedAmendments.delete(accNbr);
    } else {
      newExpandedAmendments.add(accNbr);
    }
    setExpandedAmendments(newExpandedAmendments);
  };
  const closeAmendment = (accNbr) => {
    const newExpandedAmendments = new Set(expandedAmendments);
    newExpandedAmendments.delete(accNbr);
    setExpandedAmendments(newExpandedAmendments);
    loadBulkCustomersWithAmendmentStatus();
  };
  const filteredCustomers = customers.filter((customer) => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();
    return (
      String(customer.folio_no ?? "").toLowerCase().includes(searchLower) ||
      customer.acc_nbr?.toLowerCase().includes(searchLower) ||
      customer.job_nbr?.toLowerCase().includes(searchLower) ||
      customer.area_cd?.toLowerCase().includes(searchLower) ||
      customer.name?.toLowerCase().includes(searchLower)
    );
  });
  const totalRecords = filteredCustomers.length;
  const totalPages = Math.ceil(totalRecords / recordsPerPage);
  const startIndex = (currentPage - 1) * recordsPerPage;
  const currentRecords = filteredCustomers.slice(
    startIndex,
    startIndex + recordsPerPage
  );
  const getPageNumbers = () => {
    if (totalPages <= 5) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }
    if (currentPage <= 3) return [1, 2, 3, "...", totalPages];
    if (currentPage >= totalPages - 2)
      return [1, "...", totalPages - 2, totalPages - 1, totalPages];
    return [
      1,
      "...",
      currentPage - 1,
      currentPage,
      currentPage + 1,
      "...",
      totalPages,
    ];
  };
  return (
    <div className="flex flex-col min-h-screen">
      <div className="w-full max-w-[92rem] px-6 mx-auto">
        <div className="ds-card p-ds-5 mb-ds-6 mt-ds-6">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 py-6 border-b border-ink-200">
            <h3 className="ds-page-title">
              NCRE Developer Amendments
            </h3>
            <button
              onClick={loadBulkCustomersWithAmendmentStatus}
              className="mt-3 sm:mt-0 ds-btn ds-btn-primary"
            >
              <i className="fas fa-sync-alt"></i>
              Refresh List
            </button>
          </div>
          {areaInfo.area_code && (
            <div className="bg-navy-50 border border-navy-200 rounded-lg mx-6 mt-4 p-4">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                <div className="mb-2 md:mb-0">
                  <h3 className="ds-section-title">
                    NCRE Developers for Area {areaInfo.area_code}
                  </h3>
                  <div className="text-sm text-navy-700">
                    Total: {areaInfo.total_customers} developers
                  </div>
                </div>
              </div>
            </div>
          )}
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center px-6 py-4 gap-4">
            <div className="relative w-full lg:w-96">
              <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-ink-400 text-sm"></i>
              <input
                type="text"
                placeholder="Search by folio number, name, job..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-ink-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 text-sm"
              />
            </div>
            <div className="flex items-center gap-3">
              <span className="text-sm text-ink-600">Show:</span>
              <select
                value={recordsPerPage}
                onChange={(e) => {
                  setRecordsPerPage(Number(e.target.value));
                  setCurrentPage(1);
                }}
                className="ds-input w-auto"
              >
                {[10, 20, 30, 50].map((n) => (
                  <option key={n} value={n}>
                    {n} entries
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm border-collapse">
              <thead>
                <tr className="ds-btn ds-btn-primary">
                  <th className="p-4 font-medium sticky top-0 z-10">
                    Folio Number
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10">
                    Customer Name
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden lg:table-cell">
                    Job Number
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden xl:table-cell">
                    Area Code
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 text-center">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="5" className="ds-table-state">
                      <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                      Loading customers...
                    </td>
                  </tr>
                ) : currentRecords.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="ds-table-state">
                      {searchTerm
                        ? "No customers found matching your search"
                        : "No customers found"}
                    </td>
                  </tr>
                ) : (
                  currentRecords.map((customer) => {
                    const isAmendmentExpanded = expandedAmendments.has(
                      customer.acc_nbr
                    );
                    return (
                      <React.Fragment key={customer.acc_nbr}>
                        <tr className="border-b hover:bg-ink-50 transition">
                          <td className="p-4 font-mono font-medium text-ink-900">
                            {customer.folio_no ?? "N/A"}
                          </td>
                          <td className="p-4 text-ink-800">
                            {customer.name || "N/A"}
                          </td>
                          <td className="p-4 hidden lg:table-cell text-ink-600">
                            {customer.job_nbr || "N/A"}
                          </td>
                          <td className="p-4 hidden xl:table-cell text-ink-600">
                            {customer.area_cd || "N/A"}
                          </td>
                          <td className="p-4 text-center">
                            <button
                              onClick={() => toggleAmendment(customer.acc_nbr)}
                              className={`px-5 py-2 rounded-lg text-white font-medium text-sm transition ${
                                isAmendmentExpanded
                                  ? "bg-warning-600 hover:bg-warning-700"
                                  : "bg-navy-600 hover:bg-navy-700"
                              }`}
                            >
                              <i
                                className={`fas ${
                                  isAmendmentExpanded
                                    ? "fa-chevron-up"
                                    : "fa-edit"
                                } mr-2`}
                              ></i>
                              {isAmendmentExpanded ? "Close" : "Amend"}
                            </button>
                          </td>
                        </tr>
                        {isAmendmentExpanded && (
                          <tr>
                            <td colSpan="5" className="p-6 bg-ink-50">
                              <BulkCustomerAmendmentForm
                                customer={customer}
                                onClose={() => closeAmendment(customer.acc_nbr)}
                              />
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
          {totalPages > 1 && (
            <div className="flex flex-col sm:flex-row justify-between items-center px-6 py-4 border-t border-ink-200">
              <div className="text-sm text-ink-700 mb-3 sm:mb-0">
                Showing {startIndex + 1} to{" "}
                {Math.min(startIndex + recordsPerPage, totalRecords)} of{" "}
                {totalRecords} entries
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                  disabled={currentPage === 1}
                  className="ds-btn ds-btn-secondary ds-btn-sm"
                >
                  Previous
                </button>
                {getPageNumbers().map((page, idx) => (
                  <button
                    key={idx}
                    onClick={() =>
                      typeof page === "number" && setCurrentPage(page)
                    }
                    disabled={page === "..."}
                    className={`px-4 py-2 rounded transition ${
                      page === currentPage
                        ? "bg-brandred text-white"
                        : page === "..."
                        ? "cursor-default text-ink-500"
                        : "bg-ink-200 hover:bg-ink-300"
                    }`}
                  >
                    {page}
                  </button>
                ))}
                <button
                  onClick={() =>
                    setCurrentPage((p) => Math.min(totalPages, p + 1))
                  }
                  disabled={currentPage === totalPages}
                  className="ds-btn ds-btn-secondary ds-btn-sm"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BulkCustomerAmendmentView;
