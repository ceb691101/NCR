import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import {
  getSelectedAreaCode,
  getBillCyclesFromStorage,
} from "services/AreaAndBillService";
const BulkCustomerAmendment = () => {
  const [amendments, setAmendments] = useState([]);
  const [filteredAmendments, setFilteredAmendments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [amendmentTypes, setAmendmentTypes] = useState([]);
  const baseUrl = process.env.REACT_APP_API_BASE_URL;
  // Fetch amendment types
  const fetchAmendmentTypes = async () => {
    try {
      const response = await fetch(`${baseUrl}/api/amndtypes`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        setAmendmentTypes(data);
        return data; // Return the data for chaining
      } else {
        console.error("Failed to fetch amendment types");
        return [];
      }
    } catch (error) {
      console.error("Error fetching amendment types:", error);
      return [];
    }
  };
  // Fetch amendments with old values and descriptions
  const fetchAmendments = async (
    amendmentTypesData,
    page = 1,
    limit = recordsPerPage
  ) => {
    setLoading(true);
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      let url = `${baseUrl}/api/v1/amendments?session_id=${sessionId}&user_id=${userId}`;
      const amendmentsResponse = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (amendmentsResponse.ok) {
        const data = await amendmentsResponse.json();
        const amendmentsData = data.amendments || [];
        // Filter to user's allowed areas
        const allowedAreas = getBillCyclesFromStorage().map((c) => c.area_code);
        const allowedAreasSet = new Set(allowedAreas);
        const filteredAmendmentsData = amendmentsData.filter((amendment) =>
          allowedAreasSet.has(amendment.areaCd)
        );
        // Create a map for quick lookup of amendment types
        const amendmentTypeMap = new Map();
        amendmentTypesData.forEach((type) => {
          amendmentTypeMap.set(type.amdType, type.amdDesc);
        });
        // Fetch old values for each amendment
        const amendmentsWithDetails = await Promise.all(
          filteredAmendmentsData.map(async (amendment) => {
            try {
              let oldValue = "N/A";
              // Fetch old value
              const oldValueResponse = await fetch(
                `${baseUrl}/api/v1/amendments/old-value?accNbr=${
                  amendment.accNbr
                }&amdType=${amendment.amdType}&billCycle=${
                  amendment.effctBlcy || ""
                }`,
                {
                  method: "GET",
                  headers: {
                    "Content-Type": "application/json",
                    Authorization: "Basic " + btoa("user:admin123"),
                  },
                }
              );
              if (oldValueResponse.ok) {
                const oldValueData = await oldValueResponse.json();
                oldValue = oldValueData.oldValue || "N/A";
              }
              // Get amendment description from the map
              const amendmentDescription =
                amendmentTypeMap.get(amendment.amdType) || "N/A";
              return {
                ...amendment,
                oldValue,
                amendmentDescription,
              };
            } catch (error) {
              console.error(
                `Error processing amendment ${amendment.accNbr}:`,
                error
              );
              return {
                ...amendment,
                oldValue: "Error",
                amendmentDescription:
                  amendmentTypeMap.get(amendment.amdType) || "N/A",
              };
            }
          })
        );
        setAmendments(amendmentsWithDetails);
        setFilteredAmendments(amendmentsWithDetails);
        setTotalRecords(amendmentsWithDetails.length);
        setTotalPages(Math.ceil(amendmentsWithDetails.length / limit));
      } else {
        toast.error("Failed to fetch amendments");
      }
    } catch (error) {
      console.error("Error fetching amendments:", error);
      toast.error("Failed to fetch amendments");
    } finally {
      setLoading(false);
    }
  };
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);
  // Initialize data - fetch amendment types first, then amendments
  useEffect(() => {
    const initializeData = async () => {
      try {
        setLoading(true);
        const amendmentTypesData = await fetchAmendmentTypes();
        await fetchAmendments(amendmentTypesData, currentPage, recordsPerPage);
      } catch (error) {
        console.error("Error initializing data:", error);
        setLoading(false);
      }
    };
    initializeData();
  }, [currentPage, recordsPerPage]);
  // Refresh function that can be called manually
  const refreshData = async () => {
    try {
      setLoading(true);
      const amendmentTypesData = await fetchAmendmentTypes();
      await fetchAmendments(amendmentTypesData, currentPage, recordsPerPage);
      toast.success("Data refreshed successfully");
    } catch (error) {
      console.error("Error refreshing data:", error);
      toast.error("Failed to refresh data");
    }
  };
  // Client-side search functionality
  useEffect(() => {
    if (searchTerm.trim() === "") {
      setFilteredAmendments(amendments);
      setTotalPages(Math.ceil(amendments.length / recordsPerPage));
      setCurrentPage(1);
    } else {
      const searchLower = searchTerm.toLowerCase();
      const filtered = amendments.filter((amendment) =>
        Object.values(amendment).some((value) =>
          value?.toString().toLowerCase().includes(searchLower)
        )
      );
      setFilteredAmendments(filtered);
      setTotalPages(Math.ceil(filtered.length / recordsPerPage));
      setCurrentPage(1);
    }
  }, [searchTerm, amendments, recordsPerPage]);
  // Updated handleReject
  const handleReject = async (amendment) => {
    const confirmReject = () => {
      toast.dismiss();
      performReject(amendment);
    };
    const cancelReject = () => {
      toast.dismiss();
    };
    toast(
      <div className="flex flex-col space-y-2">
        <p className="text-sm">
          Are you sure you want to reject the amendment for{" "}
          <strong>Folio {amendment.folioNo ?? amendment.folio_no ?? "N/A"}</strong>?
        </p>
        <div className="flex gap-2 justify-end">
          <button
            onClick={cancelReject}
            className="px-3 py-1 rounded-md text-white text-xs font-medium bg-ink-500 hover:bg-ink-600 border-none cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={confirmReject}
            className="px-3 py-1 rounded-md text-white text-xs font-medium bg-critical-500 hover:bg-critical-600 border-none cursor-pointer"
          >
            Reject
          </button>
        </div>
      </div>,
      {
        position: "top-center",
        autoClose: false,
        closeOnClick: false,
        closeButton: false,
        draggable: false,
      }
    );
  };
  const performReject = async (amendment) => {
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      const url = `${baseUrl}/api/v1/amendments/${amendment.accNbr}/${amendment.amdType}/${amendment.effctBlcy}/reject?session_id=${sessionId}&user_id=${userId}`;
      const response = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        toast.success(`Rejected amendment for Folio ${amendment.folioNo ?? amendment.folio_no ?? "N/A"}`);
        refreshData(); // Use the new refresh function
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to reject amendment");
      }
    } catch (error) {
      console.error("Error rejecting amendment:", error);
      toast.error("Failed to reject amendment");
    }
  };
  // Updated handlePost
  const handlePost = async (amendment) => {
    const confirmPost = () => {
      toast.dismiss();
      performPost(amendment);
    };
    const cancelPost = () => {
      toast.dismiss();
    };
    toast(
      <div className="flex flex-col space-y-2">
        <p className="text-sm">
          Are you sure you want to post the amendment for{" "}
          <strong>Folio {amendment.folioNo ?? amendment.folio_no ?? "N/A"}</strong>?
        </p>
        <div className="flex gap-2 justify-end">
          <button
            onClick={cancelPost}
            className="px-3 py-1 rounded-md text-white text-xs font-medium bg-ink-500 hover:bg-ink-600 border-none cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={confirmPost}
            className="ds-btn ds-btn-success ds-btn-sm"
          >
            Post
          </button>
        </div>
      </div>,
      {
        position: "top-center",
        autoClose: false,
        closeOnClick: false,
        closeButton: false,
        draggable: false,
      }
    );
  };
  const performPost = async (amendment) => {
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      const url = `${baseUrl}/api/v1/amendments/${amendment.accNbr}/${amendment.amdType}/${amendment.effctBlcy}/post?session_id=${sessionId}&user_id=${userId}`;
      const response = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        toast.success(`Posted amendment for Folio ${amendment.folioNo ?? amendment.folio_no ?? "N/A"}`);
        refreshData(); // Use the new refresh function
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to post amendment");
      }
    } catch (error) {
      console.error("Error posting amendment:", error);
      toast.error("Failed to post amendment");
    }
  };
  // Get display value (chrValue or nmrValue)
  const getNewValue = (amendment) => {
    return amendment.chrValue || amendment.nmrValue?.toString() || "N/A";
  };
  // Pagination helper
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
  // Filtered data for pagination
  const startIndex = (currentPage - 1) * recordsPerPage;
  const endIndex = startIndex + recordsPerPage;
  const paginatedAmendments = filteredAmendments.slice(startIndex, endIndex);
  return (
    <div className="flex flex-col min-h-screen">
      <div className="w-full max-w-[92rem] px-6 mx-auto">
        <div className="ds-card p-ds-5 mb-ds-6 mt-ds-6">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 py-6 border-b border-ink-200">
            <h3 className="ds-page-title">
              Account Amendments
            </h3>
            <button
              onClick={refreshData}
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
                placeholder="Search amendments..."
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
                    Amendment Type
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden lg:table-cell">
                    Amendment Description
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden lg:table-cell">
                    Area Code
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden xl:table-cell">
                    Effective Bill Cycle
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10">
                    Effective Date
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10">
                    Old Value
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10">
                    New Value
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 text-center">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="9" className="ds-table-state">
                      <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                      Loading amendments...
                    </td>
                  </tr>
                ) : paginatedAmendments.length === 0 ? (
                  <tr>
                    <td colSpan="9" className="ds-table-state">
                      {searchTerm
                        ? "No amendments found matching your search"
                        : "No amendments found"}
                    </td>
                  </tr>
                ) : (
                  paginatedAmendments.map((amendment) => (
                    <tr
                      key={amendment.accNbr}
                      className="border-b hover:bg-ink-50 transition"
                    >
                      <td className="p-4 font-mono font-medium text-ink-900">
                        {amendment.folioNo ?? amendment.folio_no ?? "N/A"}
                      </td>
                      <td className="p-4 text-ink-800">
                        {amendment.amdType || "N/A"}
                      </td>
                      <td className="p-4 hidden lg:table-cell text-ink-600">
                        {amendment.amendmentDescription || "N/A"}
                      </td>
                      <td className="p-4 hidden lg:table-cell text-ink-600">
                        {amendment.areaCd || "N/A"}
                      </td>
                      <td className="p-4 hidden xl:table-cell text-ink-600">
                        {amendment.effctBlcy || "N/A"}
                      </td>
                      <td className="p-4 text-ink-700">
                        {amendment.effctDate || "N/A"}
                      </td>
                      <td className="p-4 text-ink-700">
                        {amendment.oldValue || "N/A"}
                      </td>
                      <td className="p-4 text-ink-700">
                        {getNewValue(amendment)}
                      </td>
                      <td className="p-4 text-center">
                        <div className="flex gap-2 justify-center">
                          <button
                            onClick={() => handlePost(amendment)}
                            className="px-3 py-2 rounded-lg text-white font-medium text-sm transition bg-success-600 hover:bg-success-700"
                          >
                            <i className="fas fa-check mr-2"></i>
                            Post
                          </button>
                          <button
                            onClick={() => handleReject(amendment)}
                            className="px-3 py-2 rounded-lg text-white font-medium text-sm transition bg-critical-600 hover:bg-critical-700"
                          >
                            <i className="fas fa-times mr-2"></i>
                            Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          {totalPages > 1 && (
            <div className="flex flex-col sm:flex-row justify-between items-center px-6 py-4 border-t border-ink-200">
              <div className="text-sm text-ink-700 mb-3 sm:mb-0">
                Showing {startIndex + 1} to{" "}
                {Math.min(
                  startIndex + recordsPerPage,
                  filteredAmendments.length
                )}{" "}
                of {filteredAmendments.length} entries
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
export default BulkCustomerAmendment;
