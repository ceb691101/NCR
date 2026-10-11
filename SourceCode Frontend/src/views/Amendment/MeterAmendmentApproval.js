import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import {
  getBillCyclesFromStorage,
} from "services/AreaAndBillService";

const MeterAmendmentApproval = () => {
  const [amendments, setAmendments] = useState([]);
  const [filteredAmendments, setFilteredAmendments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  
  const baseUrl = process.env.REACT_APP_API_BASE_URL;

  // Fetch pending meter amendments
  const fetchPendingAmendments = async () => {
    setLoading(true);
    try {
      const url = `${baseUrl}/api/v1/meter-amendments/pending`;
      
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });

      if (response.ok) {
        const data = await response.json();
        const amendmentsData = data.amendments || [];

        // Filter to user's allowed areas
        const allowedAreas = getBillCyclesFromStorage().map((c) => String(c.area_code).trim());
        const allowedAreasSet = new Set(allowedAreas);
        const filteredAmendmentsData = allowedAreasSet.size > 0
          ? amendmentsData.filter((amendment) =>
              allowedAreasSet.has(String(amendment.areaCd || "").trim())
            )
          : amendmentsData;

        setAmendments(filteredAmendmentsData);
        setFilteredAmendments(filteredAmendmentsData);
        setTotalPages(Math.ceil(filteredAmendmentsData.length / recordsPerPage));
        return true;
      } else {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to fetch pending meter amendments");
      }
    } catch (error) {
      console.error("Error fetching amendments:", error);
      toast.error("Failed to fetch pending meter amendments");
      return false;
    } finally {
      setLoading(false);
    }
  };

  // Initialize data
  useEffect(() => {
    fetchPendingAmendments();
  }, [recordsPerPage]);

  // Refresh function
  const refreshData = async () => {
    try {
      const ok = await fetchPendingAmendments();
      if (ok) {
        toast.success("Data refreshed successfully");
      }
    } catch (error) {
      console.error("Error refreshing data:", error);
      toast.error("Failed to refresh data");
    }
  };

  const toEpochMillis = (value) => {
    const parsed = new Date(value).getTime();
    return Number.isNaN(parsed) ? 0 : parsed;
  };

  const buildActionQuery = (amendment, userId) => {
    const params = new URLSearchParams({
      acc_nbr: amendment.accNbr || "",
      amd_type: amendment.amdType || "",
      mtr_nbr: amendment.mtrNbr || "",
      effct_blcy: amendment.effctBlcy || "",
      entered_dtime: String(toEpochMillis(amendment.enteredDtime)),
      user_id: userId || "SYSTEM",
    });
    return params.toString();
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

  // Handle Reject
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
          Are you sure you want to reject the meter amendment for{" "}
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
      const userId = sessionStorage.getItem("user_id");
      const url = `${baseUrl}/api/v1/meter-amendments/reject?${buildActionQuery(amendment, userId)}`;

      const response = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });

      if (response.ok) {
        toast.success(`Rejected meter amendment for Folio ${amendment.folioNo ?? amendment.folio_no ?? "N/A"}`);
        refreshData();
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to reject amendment");
      }
    } catch (error) {
      console.error("Error rejecting amendment:", error);
      toast.error("Failed to reject amendment");
    }
  };

  // Handle Post (Approve)
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
          Are you sure you want to approve the meter amendment for{" "}
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
      const userId = sessionStorage.getItem("user_id");
      const url = `${baseUrl}/api/v1/meter-amendments/approve?${buildActionQuery(amendment, userId)}`;

      const response = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });

      if (response.ok) {
        toast.success(`Approved meter amendment for Folio ${amendment.folioNo ?? amendment.folio_no ?? "N/A"}`);
        refreshData();
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to approve amendment");
      }
    } catch (error) {
      console.error("Error approving amendment:", error);
      toast.error("Failed to approve amendment");
    }
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

  // Format date
  const formatDate = (timestamp) => {
    if (!timestamp) return "N/A";
    const date = new Date(timestamp);
    return date.toLocaleDateString();
  };

  return (
    <div className="flex flex-col min-h-screen">
      <div className="w-full max-w-[92rem] px-6 mx-auto">
        <div className="ds-card p-ds-5 mb-ds-6 mt-ds-6">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 py-6 border-b border-ink-200">
            <h3 className="ds-page-title">
              Meter Amendments Approval
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
                className="pl-10 pr-4 py-2 w-full border border-ink-300 rounded-lg text-sm focus:ring-2 focus:ring-navy-500 focus:border-transparent"
              />
            </div>

            <div className="flex items-center gap-2">
              <label className="text-sm text-ink-700 whitespace-nowrap">
                Show:
              </label>
              <select
                value={recordsPerPage}
                onChange={(e) => {
                  setRecordsPerPage(Number(e.target.value));
                  setCurrentPage(1);
                }}
                className="px-3 py-2 border border-ink-300 rounded-lg text-sm focus:ring-2 focus:ring-navy-500 focus:border-transparent"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </div>
          </div>

          {loading ? (
            <div className="flex justify-center items-center py-20">
              <div className="ds-spinner"></div>
            </div>
          ) : paginatedAmendments.length === 0 ? (
          <div className="ds-empty">
            <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-inbox"></i>
            </span>
            <p className="ds-section-title mt-1">No pending amendments found</p>
            <p className="ds-caption">
              Meter amendment requests awaiting approval will appear here.
            </p>
          </div>
          ) : (
            <>
              <div className="overflow-x-auto px-6 pb-6">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-ink-100">
                      <th className="ds-th">Folio No</th>
                      <th className="ds-th">Area Code</th>
                      <th className="ds-th">Amendment Type</th>
                      <th className="ds-th">Meter Type</th>
                      <th className="ds-th">Meter No</th>
                      <th className="ds-th">Effective Bill Cycle</th>
                      <th className="ds-th">Effective Date</th>
                      <th className="ds-th">Reason</th>
                      <th className="ds-th">Entered By</th>
                      <th className="border border-ink-300 px-4 py-3 text-center text-xs font-semibold text-ink-700 uppercase">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedAmendments.map((amendment, index) => (
                      <tr key={`${amendment.accNbr}-${amendment.amdType}-${amendment.mtrNbr}-${amendment.enteredDtime || index}`} className="hover:bg-ink-50">
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.folioNo ?? amendment.folio_no ?? "N/A"}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.areaCd}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">
                          <span className={`px-2 py-1 rounded text-xs font-semibold ${
                            amendment.amdType === 'A' ? 'bg-success-100 text-success-800' : 'bg-critical-100 text-critical-800'
                          }`}>
                            {amendment.amdType === 'A' ? 'Add Meter' : 'Remove Meter'}
                          </span>
                        </td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.mtrType || 'N/A'}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.mtrNbr || 'N/A'}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.effctBlcy}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{formatDate(amendment.effctDate)}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.rsnDesc || amendment.rsnCode || 'N/A'}</td>
                        <td className="border border-ink-300 px-4 py-3 text-sm">{amendment.userId}</td>
                        <td className="border border-ink-300 px-4 py-3 text-center">
                          <div className="flex gap-2 justify-center">
                            <button
                              onClick={() => handlePost(amendment)}
                              className="bg-success-500 hover:bg-success-600 text-white px-3 py-1 rounded text-xs font-medium transition"
                              title="Approve"
                            >
                              <i className="fas fa-check mr-1"></i>
                              Post
                            </button>
                            <button
                              onClick={() => handleReject(amendment)}
                              className="bg-critical-500 hover:bg-critical-600 text-white px-3 py-1 rounded text-xs font-medium transition"
                              title="Reject"
                            >
                              <i className="fas fa-times mr-1"></i>
                              Reject
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="flex flex-col sm:flex-row justify-between items-center px-6 pb-6 gap-4">
                <div className="text-sm text-ink-600">
                  Showing {startIndex + 1} to {Math.min(endIndex, filteredAmendments.length)} of{" "}
                  {filteredAmendments.length} entries
                </div>

                <div className="flex gap-1">
                  <button
                    onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                    disabled={currentPage === 1}
                    className="px-3 py-1 border border-ink-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ink-100"
                  >
                    Previous
                  </button>

                  {getPageNumbers().map((page, index) =>
                    page === "..." ? (
                      <span key={index} className="px-3 py-1">
                        ...
                      </span>
                    ) : (
                      <button
                        key={index}
                        onClick={() => setCurrentPage(page)}
                        className={`px-3 py-1 border rounded-md text-sm ${
                          currentPage === page
                            ? "bg-navy-600 text-white border-navy-600"
                            : "border-ink-300 hover:bg-ink-100"
                        }`}
                      >
                        {page}
                      </button>
                    )
                  )}

                  <button
                    onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                    disabled={currentPage === totalPages}
                    className="px-3 py-1 border border-ink-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ink-100"
                  >
                    Next
                  </button>
                </div>
              </div>

            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default MeterAmendmentApproval;
