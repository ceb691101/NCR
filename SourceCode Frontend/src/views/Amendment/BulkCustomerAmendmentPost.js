import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import { getBillCyclesFromStorage } from "services/AreaAndBillService";
const BulkCustomerAmendmentPost = () => {
  const [amendments, setAmendments] = useState([]);
  const [filteredAmendments, setFilteredAmendments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalRecords, setTotalRecords] = useState(0);
  const baseUrl = process.env.REACT_APP_API_BASE_URL;
  // Fetch amendment types (for description)
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
        return data; // Array of { amdType, amdDesc, ... }
      }
      return [];
    } catch (error) {
      console.error("Error fetching amendment types:", error);
      return [];
    }
  };
  // CORRECT: Use the dedicated /posted endpoint
  const fetchPostedAmendments = async (amendmentTypesData) => {
    setLoading(true);
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      let url = `${baseUrl}/api/v1/amendments/posted?session_id=${sessionId}&user_id=${userId}`;
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const data = await response.json();
      const postedAmendments = data.amendments || [];
      // Filter to user's allowed areas
      const allowedAreas = getBillCyclesFromStorage().map((c) => c.area_code);
      const allowedAreasSet = new Set(allowedAreas);
      const filteredPosted = postedAmendments.filter((amendment) =>
        allowedAreasSet.has(amendment.areaCd)
      );
      // Map amdType → description
      const typeMap = new Map();
      amendmentTypesData.forEach((t) => typeMap.set(t.amdType, t.amdDesc));
      // Fetch old value for each amendment
      const enriched = await Promise.all(
        filteredPosted.map(async (amendment) => {
          let oldValue = "N/A";
          try {
            const oldResp = await fetch(
              `${baseUrl}/api/v1/amendments/old-value?accNbr=${
                amendment.accNbr
              }&amdType=${amendment.amdType}&billCycle=${
                amendment.effctBlcy || ""
              }`,
              {
                headers: {
                  Authorization: "Basic " + btoa("user:admin123"),
                },
              }
            );
            if (oldResp.ok) {
              const oldData = await oldResp.json();
              oldValue = oldData.oldValue ?? "N/A";
            }
          } catch (e) {
            console.warn("Old value fetch failed:", e);
          }
          return {
            ...amendment,
            oldValue,
            amendmentDescription:
              typeMap.get(amendment.amdType) || "Unknown Amendment",
          };
        })
      );
      setAmendments(enriched);
      setFilteredAmendments(enriched);
      setTotalRecords(enriched.length);
      setTotalPages(Math.ceil(enriched.length / recordsPerPage));
    } catch (err) {
      console.error("Error loading posted amendments:", err);
      toast.error("Failed to load posted amendments");
      setAmendments([]);
      setFilteredAmendments([]);
    } finally {
      setLoading(false);
    }
  };
  // Load data on mount
  useEffect(() => {
    const loadData = async () => {
      const types = await fetchAmendmentTypes();
      await fetchPostedAmendments(types);
    };
    loadData();
  }, []);
  // Refresh button
  const refreshData = async () => {
    setLoading(true);
    const types = await fetchAmendmentTypes();
    await fetchPostedAmendments(types);
    toast.success("Posted amendments refreshed");
  };
  // Client-side search
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredAmendments(amendments);
    } else {
      const lower = searchTerm.toLowerCase();
      const filtered = amendments.filter((a) =>
        Object.values(a).some(
          (v) => v && v.toString().toLowerCase().includes(lower)
        )
      );
      setFilteredAmendments(filtered);
    }
    setCurrentPage(1);
  }, [searchTerm, amendments]);
  // Update pagination when recordsPerPage changes
  useEffect(() => {
    setTotalPages(Math.ceil(filteredAmendments.length / recordsPerPage));
    setCurrentPage(1);
  }, [filteredAmendments, recordsPerPage]);
  const getNewValue = (amendment) => {
    return amendment.chrValue || amendment.nmrValue?.toString() || "N/A";
  };
  const getPageNumbers = () => {
    if (totalPages <= 5)
      return Array.from({ length: totalPages }, (_, i) => i + 1);
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
  const startIndex = (currentPage - 1) * recordsPerPage;
  const paginated = filteredAmendments.slice(
    startIndex,
    startIndex + recordsPerPage
  );

  // NEW: Handle final post confirmation
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
          Are you sure you want to archive the amendment for{" "}
          <strong>Folio {amendment.folioNo ?? amendment.folio_no ?? "N/A"}</strong> to history?
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
            Archive
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

  // NEW: Perform final post to history
  const performPost = async (amendment) => {
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      const url = `${baseUrl}/api/v1/amendments/${amendment.accNbr}/${amendment.amdType}/${amendment.effctBlcy}/final-post?session_id=${sessionId}&user_id=${userId}`;
      const response = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        toast.success(`Archived amendment for Folio ${amendment.folioNo ?? amendment.folio_no ?? "N/A"} to history`);
        refreshData(); // Refresh the list
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to archive amendment");
      }
    } catch (error) {
      console.error("Error archiving amendment:", error);
      toast.error("Failed to archive amendment");
    }
  };

  return (
    <div className="flex flex-col min-h-screen">
      <div className="w-full max-w-[92rem] px-6 mx-auto">
        <div className="ds-card p-ds-5 mb-ds-6 mt-ds-6">
          {/* Header */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 py-6 border-b border-ink-200">
            <h3 className="ds-page-title">
              Pending Amendments (Ready to Post)
            </h3>
            <button
              onClick={refreshData}
              className="mt-3 sm:mt-0 ds-btn ds-btn-primary"
            >
              <i className="fas fa-sync-alt"></i>
              Refresh List
            </button>
          </div>
          {/* Search & Entries */}
          <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center px-6 py-4 gap-4">
            <div className="relative w-full lg:w-96">
              <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-ink-400 text-sm"></i>
              <input
                type="text"
                placeholder="Search pending amendments..."
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
          {/* Table */}
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
                    Description
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden lg:table-cell">
                    Area Code
                  </th>
                  <th className="p-4 font-medium sticky top-0 z-10 hidden xl:table-cell">
                    Eff. Bill Cycle
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
                    Action
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="9" className="ds-table-state">
                      <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                      Loading pending amendments...
                    </td>
                  </tr>
                ) : paginated.length === 0 ? (
                  <tr>
                    <td colSpan="9" className="ds-table-state">
                      {searchTerm
                        ? "No matching amendments found"
                        : "No pending amendments ready to post"}
                    </td>
                  </tr>
                ) : (
                  paginated.map((amendment) => (
                    <tr
                      key={amendment.accNbr}
                      className="border-b hover:bg-ink-50"
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
                        <button
                          onClick={() => handlePost(amendment)}
                          className="px-4 py-2 bg-success-600 hover:bg-success-700 text-white text-xs font-medium rounded transition"
                        >
                          Post
                        </button>
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
                {getPageNumbers().map((page, i) => (
                  <button
                    key={i}
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
export default BulkCustomerAmendmentPost;
