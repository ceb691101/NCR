// E:\Projects\CEB_MYPROJECTS\HSB PROJECT\FRONTEND\HSBFrontend\notus-react-main\src\views\Amendment\UpdateAmendment.js
import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import {
  getSelectedAreaCode,
  getBillCyclesFromStorage,
} from "services/AreaAndBillService";
import BulkCustomerAmendmentForm from "components/BulkCustomersComp/BulkCustomerAmendmentForm";
const UpdateAmendment = () => {
  const [amendments, setAmendments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [expandedAmendments, setExpandedAmendments] = useState(new Set());
  const baseUrl = process.env.REACT_APP_API_BASE_URL;
  useEffect(() => {
    loadRejectedAmendments();
  }, []);
  const loadRejectedAmendments = async () => {
    setLoading(true);
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      let url = `${baseUrl}/api/v1/amendments/rejected?session_id=${sessionId}&user_id=${userId}`;
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      const rejectedAmendments = data.amendments || [];
      // Filter to user's allowed areas
      const allowedAreas = getBillCyclesFromStorage().map((c) => c.area_code);
      const allowedAreasSet = new Set(allowedAreas);
      const filteredRejected = rejectedAmendments.filter((amendment) =>
        allowedAreasSet.has(amendment.areaCd)
      );
      // Fetch customer details for each amendment
      const customerPromises = filteredRejected.map(async (amendment) => {
        try {
          const customerResponse = await fetch(
            `${baseUrl}/api/v1/bulk-customers/account/${amendment.accNbr}`,
            {
              method: "GET",
              headers: {
                "Content-Type": "application/json",
                Authorization: "Basic " + btoa("user:admin123"),
              },
            }
          );
          if (customerResponse.ok) {
            const customerData = await customerResponse.json();
            const customer = customerData.customer || {};
            return {
              ...amendment,
              name: customer.name || "Unknown",
              job_nbr: customer.jobNbr || "N/A",
              area_cd: customer.areaCd || amendment.areaCd || "N/A",
            };
          } else {
            return {
              ...amendment,
              name: "Unknown",
              job_nbr: "N/A",
              area_cd: amendment.areaCd || "N/A",
            };
          }
        } catch (err) {
          return {
            ...amendment,
            name: "Unknown",
            job_nbr: "N/A",
            area_cd: amendment.areaCd || "N/A",
          };
        }
      });
      const amendmentsWithCustomerDetails = await Promise.all(customerPromises);
      setAmendments(amendmentsWithCustomerDetails);
    } catch (err) {
      console.error("Error loading rejected amendments:", err);
      toast.error("Failed to fetch rejected amendments.");
      setAmendments([]);
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
    loadRejectedAmendments();
  };
  const filteredAmendments = amendments.filter((amendment) => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();
    return (
      amendment.accNbr?.toLowerCase().includes(searchLower) ||
      String(amendment.folioNo ?? amendment.folio_no ?? "").toLowerCase().includes(searchLower) ||
      amendment.job_nbr?.toLowerCase().includes(searchLower) ||
      amendment.areaCd?.toLowerCase().includes(searchLower) ||
      amendment.name?.toLowerCase().includes(searchLower) ||
      amendment.amdType?.toLowerCase().includes(searchLower)
    );
  });
  const totalRecords = filteredAmendments.length;
  const totalPages = Math.ceil(totalRecords / recordsPerPage);
  const startIndex = (currentPage - 1) * recordsPerPage;
  const currentRecords = filteredAmendments.slice(
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
              Update Rejected Amendments
            </h3>
            <button
              onClick={loadRejectedAmendments}
              className="mt-3 sm:mt-0 ds-btn ds-btn-primary"
            >
              <i className="fas fa-sync-alt"></i>
              Refresh List
            </button>
          </div>
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
                  <th className="p-4 font-medium sticky top-0 z-10">
                    Amendment Type
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 text-center">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="6" className="ds-table-state">
                      <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                      Loading amendments...
                    </td>
                  </tr>
                ) : currentRecords.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="ds-table-state">
                      {searchTerm
                        ? "No amendments found matching your search"
                        : "No rejected amendments found"}
                    </td>
                  </tr>
                ) : (
                  currentRecords.map((amendment) => {
                    const isAmendmentExpanded = expandedAmendments.has(
                      amendment.accNbr
                    );
                    return (
                      <React.Fragment key={amendment.accNbr}>
                        <tr className="border-b hover:bg-ink-50 transition">
                          <td className="p-4 font-mono font-medium text-ink-900">
                            {amendment.folioNo ?? amendment.folio_no ?? "N/A"}
                          </td>
                          <td className="p-4 text-ink-800">
                            {amendment.name || "Unknown"}
                          </td>
                          <td className="p-4 hidden lg:table-cell text-ink-600">
                            {amendment.job_nbr || "N/A"}
                          </td>
                          <td className="p-4 hidden xl:table-cell text-ink-600">
                            {amendment.area_cd || "N/A"}
                          </td>
                          <td className="p-4 text-ink-800">
                            {amendment.amdType || "N/A"}
                          </td>
                          <td className="p-4 text-center">
                            <button
                              onClick={() => toggleAmendment(amendment.accNbr)}
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
                              {isAmendmentExpanded ? "Close" : "Update"}
                            </button>
                          </td>
                        </tr>
                        {isAmendmentExpanded && (
                          <tr>
                            <td colSpan="6" className="p-6 bg-ink-50">
                              <BulkCustomerAmendmentForm
                                customer={amendment}
                                onClose={() => closeAmendment(amendment.accNbr)}
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
export default UpdateAmendment;
