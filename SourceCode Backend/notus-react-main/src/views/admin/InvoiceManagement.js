import React, { useState, useEffect, useCallback, useRef } from "react";
import { getInvoices, getInvoiceMonths, generateInvoice, approveInvoice, rejectInvoice, submitInvoice, prepareInvoice, bulkReviewInvoices } from "services/invoiceService";
import { getCurrentOpenBillCycle } from "services/billCycleEndingService";
import { getAreaAndBill } from "services/AreaAndBillService";
import { toast } from "react-toastify";
import { useHistory } from "react-router-dom";
import InvoicePreviewModal from "components/Modal/InvoicePreviewModal";
import BulkActionModal from "components/Modal/BulkActionModal";
import MonthFilter from "components/MonthFilter";
import SearchInput from "components/SearchInput";
import {
  useReactTable,
  getCoreRowModel,
  getPaginationRowModel,
  flexRender,
} from "@tanstack/react-table";

export default function InvoiceManagement() {
  const history = useHistory();
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState(null);
  const [submittingDraftId, setSubmittingDraftId] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setPagination((prev) => ({ ...prev, pageIndex: 0 }));
  };

  const handleSearchClear = () => {
    setSearchTerm("");
    setPagination((prev) => ({ ...prev, pageIndex: 0 }));
  };
  
  // Preview modal states
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [previewInvoiceData, setPreviewInvoiceData] = useState(null);
  const [loadingPreviewId, setLoadingPreviewId] = useState(null);

  const [selectedInvoiceIds, setSelectedInvoiceIds] = useState([]);
  const [bulkActionModalOpen, setBulkActionModalOpen] = useState(false);
  const [bulkActionType, setBulkActionType] = useState("APPROVE"); // "APPROVE" or "REJECT"
  const [isBulkSubmitting, setIsBulkSubmitting] = useState(false);
  
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: 10,
  });

  // Role detection
  const userCategory = sessionStorage.getItem("user_category") || "";
  const isCE = userCategory === "Chief Engineer";
  const isDGM = userCategory === "DGM" || userCategory === "Director" || userCategory === "DIRECTOR";
  const isAdmin = userCategory === "Admin";
  const isEE = userCategory === "Electrical Engineer" || userCategory === "EE" || (!isCE && !isDGM && !isAdmin);
  const isReviewer = isCE || isDGM;

  // Tab state
  const [activeTab, setActiveTab] = useState(isReviewer ? "pending" : "all");

  const defaultMonth = React.useMemo(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    return d.toLocaleDateString("en-US", { month: "long", year: "numeric" });
  }, []);

  const [selectedMonth, setSelectedMonth] = useState(defaultMonth);
  const [availableMonths, setAvailableMonths] = useState([]);
  const [activeCycleMonth, setActiveCycleMonth] = useState("");

  const selectedMonthRef = useRef(selectedMonth);
  selectedMonthRef.current = selectedMonth;

  const activeTabRef = useRef(activeTab);
  activeTabRef.current = activeTab;

  const isFinalizeTab = useCallback((tab) => {
    if (isCE) return tab === "finalized";
    if (isDGM) return tab === "approved";
    if (isAdmin) return tab === "finalized";
    return tab === "completed";
  }, [isCE, isDGM, isAdmin]);

  const fetchInvoices = useCallback(async (targetMonth, targetTab) => {
    try {
      setLoading(true);
      const tabToUse = targetTab !== undefined ? targetTab : activeTabRef.current;
      const isFinal = isFinalizeTab(tabToUse);
      let data;
      if (isFinal) {
        const monthParam = targetMonth !== undefined && targetMonth !== null ? targetMonth : selectedMonthRef.current;
        data = await getInvoices({ month: monthParam, status: "FINALIZE" });
      } else {
        data = await getInvoices();
      }
      setInvoices(data || []);
    } catch (err) {
      console.error("Error loading invoices:", err);
      toast.error("Failed to load invoices.");
    } finally {
      setLoading(false);
    }
  }, [isFinalizeTab]);

  const handleTabChange = useCallback((tabName) => {
    setActiveTab(tabName);
    setSelectedInvoiceIds([]);
    setPagination(prev => ({ ...prev, pageIndex: 0 }));
    const isFinal = isFinalizeTab(tabName);
    const monthToUse = isFinal ? (activeCycleMonth || selectedMonthRef.current) : null;
    if (isFinal && activeCycleMonth) {
      setSelectedMonth(activeCycleMonth);
    }
    fetchInvoices(monthToUse, tabName);
  }, [isFinalizeTab, activeCycleMonth, fetchInvoices]);

  const handleMonthChange = (val) => {
    setSelectedMonth(val);
    setPagination(prev => ({ ...prev, pageIndex: 0 }));
    fetchInvoices(val, activeTab);
  };

  // Review modal state
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [reviewRemarks, setReviewRemarks] = useState("");
  const [submittingReview, setSubmittingReview] = useState(false);

  const [areaBill, setAreaBill] = useState(() => getAreaAndBill());
  // eslint-disable-next-line no-unused-vars

  useEffect(() => {
    let isMounted = true;
    async function initMonthsAndData() {
      let activeMonthStr = "";
      try {
        const cycle = await getCurrentOpenBillCycle();
        if (cycle?.bill_month && cycle?.bill_year) {
          const MONTH_NAMES = [
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
          ];
          activeMonthStr = `${MONTH_NAMES[cycle.bill_month - 1]} ${cycle.bill_year}`;
        }
      } catch (err) {
        console.warn("Could not load current open bill cycle:", err);
      }

      let monthsList = [];
      try {
        monthsList = await getInvoiceMonths();
      } catch (err) {
        console.warn("Could not load invoice months:", err);
      }

      if (!activeMonthStr && Array.isArray(monthsList) && monthsList.length > 0) {
        activeMonthStr = monthsList[0];
      }

      const finalActiveMonth = activeMonthStr || defaultMonth;
      if (isMounted) {
        setSelectedMonth(finalActiveMonth);
        setActiveCycleMonth(finalActiveMonth);
        if (Array.isArray(monthsList) && monthsList.length > 0) {
          const allMonths = [...monthsList];
          if (finalActiveMonth && !allMonths.includes(finalActiveMonth)) {
            allMonths.unshift(finalActiveMonth);
          }
          setAvailableMonths(allMonths);
        } else if (finalActiveMonth) {
          setAvailableMonths([finalActiveMonth]);
        }
      }

      if (isMounted) {
        const isFinal = isFinalizeTab(activeTabRef.current);
        fetchInvoices(isFinal ? finalActiveMonth : null, activeTabRef.current);
      }
    }

    initMonthsAndData();

    const handleAreaChange = () => {
      setAreaBill(getAreaAndBill());
    };
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      isMounted = false;
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, [defaultMonth, isFinalizeTab, fetchInvoices]);

  const handleDownloadPdf = useCallback(async (invoice) => {
    if (downloadingId) return;
    setDownloadingId(invoice.id);
    try {
      const response = await generateInvoice({
        accountNumber: invoice.accountNumber,
        areaCode: invoice.areaCode,
        billCycle: invoice.billCycle,
      });

      if (response?.blob) {
        const fileUrl = window.URL.createObjectURL(response.blob);
        const link = document.createElement("a");
        link.href = fileUrl;
        link.setAttribute(
          "download",
          `Invoice-${invoice.accountNumber}-${invoice.invoiceMonth.replace(/\s+/g, "_")}.pdf`
        );
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(fileUrl);
        toast.success("PDF invoice downloaded successfully.");
      } else {
        toast.error("Failed to download PDF invoice.");
      }
    } catch (error) {
      console.error("Error downloading PDF:", error);
      toast.error(error.message || "Failed to download PDF.");
    } finally {
      setDownloadingId(null);
    }
  }, [downloadingId]);

  const handleApprove = useCallback(async () => {
    if (!selectedInvoice) return;
    try {
      setSubmittingReview(true);
      await approveInvoice({
        invoiceId: selectedInvoice.id,
        remarks: reviewRemarks,
      });
      toast.success(isDGM ? "Invoice approved and finalized." : "Invoice approved and forwarded to DGM.");
      setSelectedInvoice(null);
      setReviewRemarks("");
      setShowPreviewModal(false);
      setPreviewInvoiceData(null);
      fetchInvoices();
    } catch (err) {
      console.error("Error approving invoice:", err);
      toast.error(err.message || "Failed to approve invoice.");
    } finally {
      setSubmittingReview(false);
    }
  }, [selectedInvoice, reviewRemarks, isDGM, fetchInvoices]);

  const handleReject = useCallback(async () => {
    if (!selectedInvoice) return;
    try {
      setSubmittingReview(true);
      await rejectInvoice({
        invoiceId: selectedInvoice.id,
        remarks: reviewRemarks,
      });
      toast.success("Invoice rejected and returned to EE.");
      setSelectedInvoice(null);
      setReviewRemarks("");
      setShowPreviewModal(false);
      setPreviewInvoiceData(null);
      fetchInvoices();
    } catch (err) {
      console.error("Error rejecting invoice:", err);
      toast.error(err.message || "Failed to reject invoice.");
    } finally {
      setSubmittingReview(false);
    }
  }, [selectedInvoice, reviewRemarks, fetchInvoices]);

  const handleSubmitDraft = useCallback(async (invoice) => {
    if (submittingDraftId) return;
    setSubmittingDraftId(invoice.id);
    try {
      await submitInvoice({
        accountNumber: invoice.accountNumber,
        areaCode: invoice.areaCode,
        billCycle: invoice.billCycle,
        bypassRu: true,
      });
      toast.success("Invoice submitted for review successfully.");
      fetchInvoices();
    } catch (error) {
      console.error("Error submitting draft invoice:", error);
      toast.error(error.message || "Failed to submit invoice.");
    } finally {
      setSubmittingDraftId(null);
    }
  }, [submittingDraftId, fetchInvoices]);

  const handleResubmitInvoice = useCallback(async (invoice) => {
    if (submittingDraftId) return;
    setSubmittingDraftId(invoice.id);
    try {
      await submitInvoice({
        accountNumber: invoice.accountNumber,
        areaCode: invoice.areaCode,
        billCycle: invoice.billCycle,
        bypassRu: true,
      });
      toast.success("Invoice re-submitted for review successfully.");
      setShowPreviewModal(false);
      fetchInvoices();
    } catch (error) {
      console.error("Error re-submitting invoice:", error);
      toast.error(error.message || "Failed to re-submit invoice.");
    } finally {
      setSubmittingDraftId(null);
    }
  }, [submittingDraftId, fetchInvoices]);

  const handleInvoiceNumberClick = useCallback(async (invoice) => {
    if (loadingPreviewId) return;
    setLoadingPreviewId(invoice.id);
    try {
      const data = await prepareInvoice({
        accountNumber: invoice.accountNumber,
        areaCode: invoice.areaCode,
        billCycle: invoice.billCycle,
      });
      if (data) {
        const enrichedData = {
          ...data,
          areaCode: invoice.areaCode,
          billCycle: invoice.billCycle,
        };
        setPreviewInvoiceData(enrichedData);
        setShowPreviewModal(true);
      } else {
        toast.error("Failed to load invoice preview: Empty response data received.");
      }
    } catch (error) {
      console.error("Error preparing invoice preview:", error);
      toast.error(error.message || "Failed to load invoice preview.");
    } finally {
      setLoadingPreviewId(null);
    }
  }, [loadingPreviewId]);

  const handleReviewClick = useCallback(async (invoice) => {
    if (loadingPreviewId) return;
    setLoadingPreviewId(invoice.id);
    setSelectedInvoice(invoice);
    try {
      const data = await prepareInvoice({
        accountNumber: invoice.accountNumber,
        areaCode: invoice.areaCode,
        billCycle: invoice.billCycle,
      });
      if (data) {
        const enrichedData = {
          ...data,
          areaCode: invoice.areaCode,
          billCycle: invoice.billCycle,
        };
        setPreviewInvoiceData(enrichedData);
        setShowPreviewModal(true);
      } else {
        toast.error("Failed to load invoice details: Empty response data received.");
        setSelectedInvoice(null);
      }
    } catch (error) {
      console.error("Error loading invoice for review:", error);
      toast.error(error.message || "Failed to load invoice details.");
      setSelectedInvoice(null);
    } finally {
      setLoadingPreviewId(null);
    }
  }, [loadingPreviewId]);

  const handleResolveClick = useCallback((invoice) => {
    history.push(`/monthlyReadings/readingsEntry?account=${invoice.accountNumber}&area=${invoice.areaCode}&billCycle=${invoice.billCycle}&source=tempReadings&resolveInvoice=true`);
  }, [history]);

  const matchAreaCode = (a1, a2) => {
    if (!a1 || !a2) return false;
    const s1 = String(a1).replace(/^0+/, '').trim();
    const s2 = String(a2).replace(/^0+/, '').trim();
    return s1 === s2;
  };

  const baseInvoices = React.useMemo(() => {
    let list = invoices;
    // No selection means all permitted areas, which the API already scopes for us.
    if (areaBill?.selectedAreaCode) {
      list = list.filter((i) => matchAreaCode(i.areaCode, areaBill.selectedAreaCode));
    }
    return list;
  }, [invoices, areaBill]);

  const displayMonths = React.useMemo(() => {
    if (availableMonths && availableMonths.length > 0) {
      return availableMonths;
    }
    return selectedMonth ? [selectedMonth] : [];
  }, [availableMonths, selectedMonth]);
  const handleBulkActionConfirm = useCallback(async (remarks) => {
    try {
      setIsBulkSubmitting(true);
      await bulkReviewInvoices({
        invoiceIds: selectedInvoiceIds,
        action: bulkActionType,
        remarks: remarks
      });
      toast.success(
        bulkActionType === "APPROVE"
          ? (isDGM ? "Selected invoices approved and finalized successfully!" : "Selected invoices approved successfully!")
          : "Selected invoices rejected successfully!"
      );
      setSelectedInvoiceIds([]);
      setBulkActionModalOpen(false);
      fetchInvoices();
    } catch (error) {
      toast.error(error.message || "Failed to perform bulk action.");
    } finally {
      setIsBulkSubmitting(false);
    }
  }, [selectedInvoiceIds, bulkActionType, fetchInvoices, isDGM]);

  // Stats for Electrical Engineer
  const draftsCount = baseInvoices.filter((i) => i.status === "DRAFT").length;
  const underReviewCount = baseInvoices.filter((i) => i.status === "RECOMMEND" || i.status === "APPROVE").length;
  const approvedCount = baseInvoices.filter((i) => i.status === "FINALIZE").length;
  const rejectedCount = baseInvoices.filter((i) => i.status === "REJECTED").length;

  // Stats for Chief Engineer
  const cePendingCount = baseInvoices.filter((i) => i.status === "RECOMMEND").length;
  const ceForwardedCount = baseInvoices.filter((i) => i.status === "APPROVE").length;
  const ceFullyApprovedCount = baseInvoices.filter((i) => i.status === "FINALIZE").length;
  const ceRejectedCount = baseInvoices.filter((i) => i.status === "REJECTED").length;

  // Stats for Deputy General Manager (DGM)
  const dgmPendingCount = baseInvoices.filter((i) => i.status === "APPROVE").length;
  const dgmFullyApprovedCount = baseInvoices.filter((i) => i.status === "FINALIZE").length;
  const dgmCePendingCount = baseInvoices.filter((i) => i.status === "RECOMMEND").length;
  const dgmRejectedCount = baseInvoices.filter((i) => i.status === "REJECTED").length;

  // Stats for Admin (Supervisory)
  const adminDraftsCount = baseInvoices.filter((i) => i.status === "DRAFT").length;
  const adminCePendingCount = baseInvoices.filter((i) => i.status === "RECOMMEND").length;
  const adminDgmPendingCount = baseInvoices.filter((i) => i.status === "APPROVE").length;
  const adminApprovedCount = baseInvoices.filter((i) => i.status === "FINALIZE").length;
  const adminRejectedCount = baseInvoices.filter((i) => i.status === "REJECTED").length;

  const isApprovedTab = 
    (isCE && activeTab === "finalized") ||
    (isDGM && activeTab === "approved") ||
    (isAdmin && activeTab === "finalized") ||
    (!isReviewer && !isAdmin && activeTab === "completed");

  const isPreviousBillCycle = React.useMemo(() => {
    if (!activeCycleMonth || !selectedMonth) return false;
    return isApprovedTab && selectedMonth.trim().toLowerCase() !== activeCycleMonth.trim().toLowerCase();
  }, [activeCycleMonth, selectedMonth, isApprovedTab]);

  const getFilteredInvoices = () => {
    let list = [];
    if (isCE) {
      switch (activeTab) {
        case "pending":
          list = baseInvoices.filter((i) => i.status === "RECOMMEND");
          break;
        case "approved":
          list = baseInvoices.filter((i) => i.status === "APPROVE");
          break;
        case "finalized":
          list = baseInvoices.filter((i) => i.status === "FINALIZE");
          break;
        case "rejected":
          list = baseInvoices.filter((i) => i.status === "REJECTED");
          break;
        case "all":
        default:
          list = baseInvoices.filter((i) => i.status !== "DRAFT");
          break;
      }
    } else if (isDGM) {
      switch (activeTab) {
        case "pending":
          list = baseInvoices.filter((i) => i.status === "APPROVE");
          break;
        case "approved":
          list = baseInvoices.filter((i) => i.status === "FINALIZE");
          break;
        case "rejected":
          list = baseInvoices.filter((i) => i.status === "REJECTED");
          break;
        case "all":
        default:
          list = baseInvoices.filter((i) => i.status !== "DRAFT");
          break;
      }
    } else if (isAdmin) {
      switch (activeTab) {
        case "draft":
          list = baseInvoices.filter((i) => i.status === "DRAFT");
          break;
        case "pending_ce":
          list = baseInvoices.filter((i) => i.status === "RECOMMEND");
          break;
        case "pending_dgm":
          list = baseInvoices.filter((i) => i.status === "APPROVE");
          break;
        case "finalized":
          list = baseInvoices.filter((i) => i.status === "FINALIZE");
          break;
        case "rejected":
          list = baseInvoices.filter((i) => i.status === "REJECTED");
          break;
        case "all":
        default:
          list = baseInvoices;
          break;
      }
    } else {
      switch (activeTab) {
        case "draft":
          list = baseInvoices.filter((i) => i.status === "DRAFT");
          break;
        case "submitted":
          list = baseInvoices.filter((i) => i.status === "RECOMMEND" || i.status === "APPROVE");
          break;
        case "completed":
          list = baseInvoices.filter((i) => i.status === "FINALIZE");
          break;
        case "rejected":
          list = baseInvoices.filter((i) => i.status === "REJECTED");
          break;
        case "all":
        default:
          list = baseInvoices;
          break;
      }
    }

    return list;
  };

  const filteredInvoices = React.useMemo(() => {
    let list = getFilteredInvoices();
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase().trim();
      list = list.filter((inv) => {
        const folio = String(inv.folioNo || inv.folio_no || inv.folioNumber || "").toLowerCase();
        const fileRef = String(inv.fileRefNo || inv.fileReferenceNo || inv.file_ref_no || inv.referenceCode || "").toLowerCase();
        const invNo = String(inv.invoiceNumber || "").toLowerCase();
        const accNo = String(inv.accountNumber || "").toLowerCase();
        const project = String(inv.projectName || inv.companyName || "").toLowerCase();

        return (
          folio.includes(term) ||
          fileRef.includes(term) ||
          invNo.includes(term) ||
          accNo.includes(term) ||
          project.includes(term)
        );
      });
    }
    return list;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [baseInvoices, activeTab, isCE, isDGM, selectedMonth, isApprovedTab, searchTerm]);

  const renderStatusBadge = (status) => {
    switch (status) {
      case "DRAFT":
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-ink-100 text-ink-800 border border-ink-200">
            Draft
          </span>
        );
      case "RECOMMEND":
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-warning-50 text-warning-700 border border-warning-200 font-bold">
            CE Recommendation Pending
          </span>
        );
      case "APPROVE":
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-navy-50 text-navy-700 border border-navy-200 font-bold">
            DGM Approval Pending
          </span>
        );
      case "FINALIZE":
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-success-50 text-success-700 border border-success-200 font-bold">
            Approved
          </span>
        );
      case "REJECTED":
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-critical-50 text-critical-700 border border-critical-200 font-bold">
            Rejected
          </span>
        );
      default:
        return (
          <span className="inline-block px-3 py-1 text-xs font-semibold rounded-full bg-ink-100 text-ink-800">
            {status}
          </span>
        );
    }
  };

  const columns = React.useMemo(() => {
    const cols = [];

    if ((isDGM || isCE) && activeTab === "pending") {
      cols.push({
        id: "selection",
        header: () => {
          const startIndex = pagination.pageIndex * pagination.pageSize;
          const pageRows = filteredInvoices.slice(startIndex, startIndex + pagination.pageSize);
          const pageRowIds = pageRows.map(r => r.id);
          const isAllSelected = pageRowIds.length > 0 && pageRowIds.every(id => selectedInvoiceIds.includes(id));
          
          const handleSelectAll = (e) => {
            if (e.target.checked) {
              setSelectedInvoiceIds(prev => {
                const newIds = [...prev];
                pageRowIds.forEach(id => {
                  if (!newIds.includes(id)) newIds.push(id);
                });
                return newIds;
              });
            } else {
              setSelectedInvoiceIds(prev => prev.filter(id => !pageRowIds.includes(id)));
            }
          };

          return (
            <div className="flex items-center justify-center">
              <input
                type="checkbox"
                checked={isAllSelected}
                onChange={handleSelectAll}
                className="w-4 h-4 rounded text-ink-600 border-ink-400 focus:ring-ink-500 cursor-pointer"
              />
            </div>
          );
        },
        cell: ({ row }) => {
          const inv = row.original;
          const isSelected = selectedInvoiceIds.includes(inv.id);
          
          const handleSelectRow = (e) => {
            if (e.target.checked) {
              setSelectedInvoiceIds(prev => [...prev, inv.id]);
            } else {
              setSelectedInvoiceIds(prev => prev.filter(id => id !== inv.id));
            }
          };

          return (
            <div className="flex items-center justify-center">
              <input
                type="checkbox"
                checked={isSelected}
                onChange={handleSelectRow}
                className="w-4 h-4 rounded text-ink-600 border-ink-400 focus:ring-ink-500 cursor-pointer"
              />
            </div>
          );
        },
        size: 50,
      });
    }

    cols.push(
      {
        id: "invoiceNumber",
        header: "Invoice Number",
        accessorKey: "invoiceNumber",
        cell: ({ row }) => {
          const inv = row.original;
          return (
            <button
              type="button"
              onClick={() => handleInvoiceNumberClick(inv)}
              className="hover:underline text-left focus:outline-none flex items-center gap-1.5 font-normal text-navy-800"
              disabled={loadingPreviewId !== null}
            >
              {loadingPreviewId === inv.id && (
                <i className="fas fa-spinner fa-spin text-xs text-ink-400"></i>
              )}
              {inv.invoiceNumber}
            </button>
          );
        },
        size: 180,
      },
      {
        id: "folioNo",
        header: "Folio No",
        accessorKey: "folioNo",
        cell: ({ row }) => row.original.folioNo ?? row.original.folio_no ?? "N/A",
        size: 110,
      },
      {
        id: "fileRefNo",
        header: "File Ref No",
        accessorKey: "fileRefNo",
        cell: ({ row }) => row.original.fileRefNo || row.original.referenceCode,
        size: 140,
      },
      {
        id: "projectName",
        header: "Facility Name",
        accessorKey: "projectName",
        cell: ({ row }) => row.original.projectName || row.original.companyName,
        size: 200,
      }
    );

    if (!isReviewer) {
      cols.push({
        id: "billCycle",
        header: "Bill Cycle",
        accessorKey: "billCycle",
        cell: ({ row }) => row.original.billCycle,
        size: 120,
      });
    }

    cols.push({
      id: "invoiceMonth",
      header: "Invoice Month",
      accessorKey: "invoiceMonth",
      cell: ({ row }) => row.original.invoiceMonth,
      size: 150,
    });

    cols.push({
      id: "costOfEnergy",
      header: isReviewer ? "Amount" : "Cost of Energy",
      accessorKey: "costOfEnergy",
      cell: ({ row }) => {
        const inv = row.original;
        return (
          <span>
            Rs. {inv.costOfEnergy ? parseFloat(inv.costOfEnergy).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : "0.00"}{!isReviewer && " LKR"}
          </span>
        );
      },
      size: 180,
    });

    const showApprovedDetails = 
      (isCE && activeTab === "finalized") ||
      (isDGM && activeTab === "approved") ||
      (isAdmin && activeTab === "finalized") ||
      (!isReviewer && !isAdmin && activeTab === "completed") ||
      (activeTab === "all");

    if (showApprovedDetails) {
      cols.push(
        {
          id: "ratePerKwh",
          header: "Tariff Rate",
          accessorKey: "ratePerKwh",
          cell: ({ row }) => {
            const inv = row.original;
            return inv.ratePerKwh != null ? (
              <span>Rs. {parseFloat(inv.ratePerKwh).toFixed(2)}</span>
            ) : (
              <span>-</span>
            );
          },
          size: 110,
        },
        {
          id: "plantFactorPercent",
          header: "Plant Factor",
          accessorKey: "plantFactorPercent",
          cell: ({ row }) => {
            const inv = row.original;
            return inv.plantFactorPercent != null ? (
              <span>{parseFloat(inv.plantFactorPercent).toFixed(2)}%</span>
            ) : (
              <span>-</span>
            );
          },
          size: 115,
        },
        {
          id: "developerPymnt",
          header: "Amount to be Paid",
          accessorKey: "developerPymnt",
          cell: ({ row }) => {
            const inv = row.original;
            return inv.developerPymnt != null ? (
              <span>
                Rs. {parseFloat(inv.developerPymnt).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            ) : (
              <span>-</span>
            );
          },
          size: 180,
        },
        {
          id: "approvedAt",
          header: "Approved Date",
          accessorKey: "approvedAt",
          cell: ({ row }) => {
            const inv = row.original;
            return inv.status === "FINALIZE" && inv.approvedAt ? (
              <span>{inv.approvedAt.substring(0, 10)}</span>
            ) : (
              <span>-</span>
            );
          },
          size: 130,
        }
      );
    }

    if (isReviewer) {
      cols.push(
        {
          id: "preparedBy",
          header: "Prepared By",
          accessorKey: "preparedBy",
          cell: ({ row }) => row.original.preparedBy,
          size: 150,
        },
        {
          id: "preparedAt",
          header: "Submitted",
          accessorKey: "preparedAt",
          cell: ({ row }) => row.original.preparedAt ? row.original.preparedAt.substring(0, 10) : "",
          size: 130,
        }
      );
    }

    cols.push(
      {
        id: "remarks",
        header: "Remarks",
        accessorKey: "remarks",
        cell: ({ row }) => {
          const inv = row.original;
          if (!inv.remarks) return <span className="text-ink-400">-</span>;
          if (inv.status === "REJECTED") {
            return (
              <span className="inline-flex items-center gap-1 text-critical-700 bg-critical-50 px-2 py-0.5 rounded-md text-xs font-semibold border border-critical-200" title={inv.remarks}>
                <i className="fas fa-exclamation-triangle text-critical-500 text-[10px]"></i>
                <span className="truncate max-w-[160px]">{inv.remarks}</span>
              </span>
            );
          }
          return <span className="truncate max-w-[160px] block" title={inv.remarks}>{inv.remarks}</span>;
        },
        size: 160,
      },
      {
        id: "status",
        header: "Status",
        accessorKey: "status",
        cell: ({ row }) => renderStatusBadge(row.original.status),
        size: 160,
      },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const inv = row.original;

          // For historical / previous bill cycles, users can ONLY VIEW and DOWNLOAD the invoice.
          // No review, approval, rejection, submission, or edit actions are permitted.
          if (isPreviousBillCycle) {
            return (
              <div className="flex items-center justify-center gap-2">
                <button
                  type="button"
                  onClick={() => handleInvoiceNumberClick(inv)}
                  disabled={loadingPreviewId !== null}
                  className="px-3 py-1.5 border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold text-xs rounded-xl shadow-2xs transition-all duration-150 flex items-center gap-1 cursor-pointer"
                  title="View Invoice"
                >
                  <i className="fas fa-eye text-slate-400"></i> View
                </button>
                <button
                  type="button"
                  onClick={() => handleDownloadPdf(inv)}
                  disabled={downloadingId === inv.id}
                  className="w-8 h-8 rounded-full border border-slate-200 bg-white text-slate-500 hover:text-emerald-600 hover:bg-emerald-50 hover:border-emerald-200 hover:shadow-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                  title="Download PDF"
                >
                  {downloadingId === inv.id ? (
                    <i className="fas fa-spinner fa-spin text-xs text-emerald-500"></i>
                  ) : (
                    <i className="fas fa-download text-xs"></i>
                  )}
                </button>
              </div>
            );
          }

          if (isReviewer) {
            return (
              <div className="flex items-center justify-center gap-2">
                {((isCE && inv.status === "RECOMMEND") || (isDGM && inv.status === "APPROVE")) && (
                  <button
                    onClick={() => handleReviewClick(inv)}
                    disabled={loadingPreviewId !== null}
                    className="px-4 py-2 border border-ink-200 hover:bg-ink-50 text-ink-700 font-bold text-xs rounded-xl shadow-xs hover:shadow-xs transition-all duration-150 flex items-center gap-1.5 cursor-pointer"
                  >
                    <i className="fas fa-eye text-ink-400"></i> Review
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => handleDownloadPdf(inv)}
                  disabled={downloadingId === inv.id || inv.status !== "FINALIZE"}
                  className="w-8 h-8 rounded-full border border-ink-200 bg-white text-ink-500 hover:text-success-600 hover:bg-success-50 hover:border-success-200 hover:shadow-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                  title="Download PDF"
                >
                  {downloadingId === inv.id ? (
                    <i className="fas fa-spinner fa-spin text-xs text-success-500"></i>
                  ) : (
                    <i className="fas fa-download text-xs"></i>
                  )}
                </button>
              </div>
            );
          }
          if (isAdmin) {
            return (
              <div className="flex items-center justify-center gap-2">
                <button
                  type="button"
                  onClick={() => handleInvoiceNumberClick(inv)}
                  disabled={loadingPreviewId !== null}
                  className="px-3 py-1.5 border border-ink-200 hover:bg-ink-50 text-ink-700 font-bold text-xs rounded-xl shadow-xs transition-all duration-150 flex items-center gap-1 cursor-pointer"
                  title="Supervisory View"
                >
                  <i className="fas fa-eye text-ink-400"></i> View
                </button>
                <button
                  type="button"
                  onClick={() => handleDownloadPdf(inv)}
                  disabled={downloadingId === inv.id || inv.status !== "FINALIZE"}
                  className="w-8 h-8 rounded-full border border-ink-200 bg-white text-ink-500 hover:text-success-600 hover:bg-success-50 hover:border-success-200 hover:shadow-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                  title="Download PDF"
                >
                  {downloadingId === inv.id ? (
                    <i className="fas fa-spinner fa-spin text-xs text-success-500"></i>
                  ) : (
                    <i className="fas fa-download text-xs"></i>
                  )}
                </button>
              </div>
            );
          }
          return (
            <div className="flex items-center justify-center gap-2">
              {inv.status === "DRAFT" && (
                <button
                  onClick={() => handleSubmitDraft(inv)}
                  disabled={submittingDraftId === inv.id}
                  className="px-4 py-2 bg-navy-800 hover:bg-navy-900 text-white font-bold text-xs rounded-xl shadow-card transition-all duration-150 flex items-center gap-1.5 disabled:opacity-50 cursor-pointer"
                >
                  {submittingDraftId === inv.id ? (
                    <>
                      <i className="fas fa-spinner fa-spin text-white"></i> Submitting...
                    </>
                  ) : (
                    <>
                      <i className="fas fa-paper-plane text-white/80"></i> Submit
                    </>
                  )}
                </button>
              )}
              {inv.status === "REJECTED" && (
                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => handleResubmitInvoice(inv)}
                    disabled={submittingDraftId === inv.id}
                    className="px-3 py-2 bg-navy-600 hover:bg-navy-700 text-white font-bold text-xs rounded-xl shadow-card transition-all duration-150 flex items-center gap-1.5 disabled:opacity-50 cursor-pointer"
                    title="Re-submit this invoice directly to Chief Engineer"
                  >
                    {submittingDraftId === inv.id ? (
                      <i className="fas fa-spinner fa-spin text-white text-xs"></i>
                    ) : (
                      <i className="fas fa-paper-plane text-white/80 text-xs"></i>
                    )}
                    Re-submit
                  </button>
                  <button
                    onClick={() => handleResolveClick(inv)}
                    className="px-3 py-2 bg-warning-500 hover:bg-warning-600 active:bg-warning-700 text-white font-bold text-xs rounded-xl shadow-card transition-all duration-150 flex items-center gap-1.5 cursor-pointer"
                    title="Edit readings in Readings Entry to resolve"
                  >
                    <i className="fas fa-tools text-xs"></i> Resolve
                  </button>
                </div>
              )}
              <button
                type="button"
                onClick={() => handleDownloadPdf(inv)}
                disabled={downloadingId === inv.id || inv.status !== "FINALIZE"}
                className="w-8 h-8 rounded-full border border-ink-200 bg-white text-ink-500 hover:text-success-600 hover:bg-success-50 hover:border-success-200 hover:shadow-xs disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center transition-all duration-200 group relative cursor-pointer focus:outline-none"
                title="Download PDF"
              >
                {downloadingId === inv.id ? (
                  <i className="fas fa-spinner fa-spin text-xs text-success-500"></i>
                ) : (
                  <i className="fas fa-download text-xs"></i>
                )}
              </button>
            </div>
          );
        },
        size: 160,
      }
    );

    return cols;
  }, [isReviewer, isAdmin, isCE, isDGM, isPreviousBillCycle, selectedInvoiceIds, filteredInvoices, pagination, activeTab, loadingPreviewId, downloadingId, submittingDraftId, handleDownloadPdf, handleInvoiceNumberClick, handleResolveClick, handleReviewClick, handleSubmitDraft, handleResubmitInvoice]);

  const [columnOrder, setColumnOrder] = useState(() => columns.map(c => c.id));

  useEffect(() => {
    setColumnOrder(columns.map(c => c.id));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isReviewer, isDGM, activeTab]);

  const table = useReactTable({
    data: filteredInvoices,
    columns,
    state: {
      columnOrder,
      pagination,
    },
    onPaginationChange: setPagination,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    columnResizeMode: "onChange",
    enableColumnResizing: true,
  });

  const moveColumnLeft = (columnId) => {
    const currentOrder = [...columnOrder];
    const index = currentOrder.indexOf(columnId);
    if (index > 0) {
      const temp = currentOrder[index - 1];
      currentOrder[index - 1] = currentOrder[index];
      currentOrder[index] = temp;
      setColumnOrder(currentOrder);
    }
  };

  const moveColumnRight = (columnId) => {
    const currentOrder = [...columnOrder];
    const index = currentOrder.indexOf(columnId);
    if (index !== -1 && index < currentOrder.length - 1) {
      const temp = currentOrder[index + 1];
      currentOrder[index + 1] = currentOrder[index];
      currentOrder[index] = temp;
      setColumnOrder(currentOrder);
    }
  };

  const getCellClassName = (columnId) => {
    switch (columnId) {
      case "selection":
        return "py-4 px-2 text-center whitespace-nowrap";
      case "invoiceNumber":
        return "py-4 px-4 font-mono font-normal text-navy-800 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "projectName":
        return "py-4 px-4 font-normal text-ink-700 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "costOfEnergy":
        return "py-4 px-4 text-ink-500 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "ratePerKwh":
        return "py-4 px-4 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "plantFactorPercent":
        return "py-4 px-4 text-center whitespace-nowrap overflow-hidden text-ellipsis";
      case "developerPymnt":
        return "py-4 px-4 text-ink-500 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "approvedAt":
        return "py-4 px-4 text-left whitespace-nowrap overflow-hidden text-ellipsis";
      case "billCycle":
        return "py-4 px-4 font-medium text-center whitespace-nowrap overflow-hidden text-ellipsis";
      case "actions":
        return "py-4 px-4 text-center whitespace-nowrap";
      default:
        return "py-4 px-4 text-ink-500 text-left whitespace-nowrap overflow-hidden text-ellipsis";
    }
  };

  return (
    <div className="flex flex-col gap-ds-6">
      {/* Page header */}
      <div>
        <div className="w-full mx-auto">
          {/* Title */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2">
            <div>
              <h1 className="ds-page-title">
                {isCE
                  ? "Chief Engineer Invoice Review"
                  : isDGM
                  ? "DGM Invoice Authorization"
                  : isAdmin
                  ? "System Invoice Management (Supervisory View)"
                  : "Invoice Management"}
              </h1>
              <p className="ds-page-subtitle mt-1">
                {isReviewer
                  ? "Review, recommend or approve submitted renewable energy invoices"
                  : isAdmin
                  ? "Supervise all invoice lifecycle stages across the system"
                  : "Prepare, validate, submit and manage renewable energy invoices"}
              </p>
            </div>
            {!isReviewer && !isAdmin && (
              <div>
                <button
                  type="button"
                  onClick={() => history.push("/tempReadings")}
                  className="ds-btn ds-btn-success"
                >
                  <i className="fas fa-plus-circle text-sm"></i>
                  <span>Generate New Invoice</span>
                </button>
              </div>
            )}
          </div>
 
          {/* Key figures */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-ds-4">
            {isCE ? (
              <>
                {/* Pending my review */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-user-check"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Pending Review</p>
                    <p className="ds-stat-value">{cePendingCount}</p>
                    <p className="ds-stat-note">Awaiting CE action</p>
                  </div>
                </div>

                {/* Forwarded to DGM */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-check-double"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Recommended</p>
                    <p className="ds-stat-value">{ceForwardedCount}</p>
                    <p className="ds-stat-note">CE approved</p>
                  </div>
                </div>

                {/* Fully approved */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-success">
                    <i className="fas fa-check-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Approved</p>
                    <p className="ds-stat-value">{ceFullyApprovedCount}</p>
                    <p className="ds-stat-note">DGM signed off</p>
                  </div>
                </div>

                {/* Rejected */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon bg-critical-700">
                    <i className="fas fa-times-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Rejected</p>
                    <p className="ds-stat-value">{ceRejectedCount}</p>
                    <p className="ds-stat-note">Returned to EE</p>
                  </div>
                </div>
              </>
            ) : isDGM ? (
              <>
                {/* Pending my review */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-user-check"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Pending Review</p>
                    <p className="ds-stat-value">{dgmPendingCount}</p>
                    <p className="ds-stat-note">Awaiting DGM action</p>
                  </div>
                </div>

                {/* Fully approved */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-success">
                    <i className="fas fa-check-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Approved</p>
                    <p className="ds-stat-value">{dgmFullyApprovedCount}</p>
                    <p className="ds-stat-note">Finalized</p>
                  </div>
                </div>

                {/* Awaiting CE */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-clock"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Awaiting CE</p>
                    <p className="ds-stat-value">{dgmCePendingCount}</p>
                    <p className="ds-stat-note">Before DGM review</p>
                  </div>
                </div>

                {/* Rejected */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon bg-critical-700">
                    <i className="fas fa-times-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Rejected</p>
                    <p className="ds-stat-value">{dgmRejectedCount}</p>
                    <p className="ds-stat-note">Returned to EE</p>
                  </div>
                </div>
              </>
            ) : isAdmin ? (
              <>
                {/* Drafts */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-warning">
                    <i className="fas fa-file-signature"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Drafts</p>
                    <p className="ds-stat-value">{adminDraftsCount}</p>
                    <p className="ds-stat-note">With Engineers</p>
                  </div>
                </div>

                {/* Pending CE */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-user-clock"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Awaiting CE</p>
                    <p className="ds-stat-value">{adminCePendingCount}</p>
                    <p className="ds-stat-note">Recommendation</p>
                  </div>
                </div>

                {/* Pending DGM */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-user-check"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Awaiting DGM</p>
                    <p className="ds-stat-value">{adminDgmPendingCount}</p>
                    <p className="ds-stat-note">Final Approval</p>
                  </div>
                </div>

                {/* Finalized */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-success">
                    <i className="fas fa-check-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Finalized</p>
                    <p className="ds-stat-value">{adminApprovedCount}</p>
                    <p className="ds-stat-note">Completed</p>
                  </div>
                </div>
              </>
            ) : (
              <>
                {/* Drafts */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-warning">
                    <i className="fas fa-file-signature"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Drafts</p>
                    <p className="ds-stat-value">{draftsCount}</p>
                    <p className="ds-stat-note">Not yet submitted</p>
                  </div>
                </div>

                {/* Under Review */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-info">
                    <i className="fas fa-clock"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Under review</p>
                    <p className="ds-stat-value">{underReviewCount}</p>
                    <p className="ds-stat-note">With CE or DGM</p>
                  </div>
                </div>

                {/* Fully Approved */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon ds-stat-icon-success">
                    <i className="fas fa-check-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Approved</p>
                    <p className="ds-stat-value">{approvedCount}</p>
                    <p className="ds-stat-note">Finalized</p>
                  </div>
                </div>

                {/* Rejected */}
                <div className="ds-stat-card">
                  <div className="ds-stat-icon bg-critical-700">
                    <i className="fas fa-times-circle"></i>
                  </div>
                  <div>
                    <p className="ds-stat-label">Rejected</p>
                    <p className="ds-stat-value">{rejectedCount}</p>
                    <p className="ds-stat-note">Need correction</p>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="px-0 mx-auto w-full relative mb-8">
        <div className="bg-white rounded-2xl border border-ink-200 shadow-xl overflow-hidden">
          {/* Tab Headers */}
          <div className="flex flex-wrap border-b border-ink-200 bg-ink-50 px-6 py-3 gap-2">
            {isCE ? (
              <>
                <button
                  onClick={() => handleTabChange("pending")}
                  className={`ds-tab ${activeTab === "pending" ? "ds-tab-active" : ""}`}
                >
                  Pending review
                </button>
                <button
                  onClick={() => handleTabChange("approved")}
                  className={`ds-tab ${activeTab === "approved" ? "ds-tab-active" : ""}`}
                >
                  Recommended
                </button>
                <button
                  onClick={() => handleTabChange("finalized")}
                  className={`ds-tab ${activeTab === "finalized" ? "ds-tab-active" : ""}`}
                >
                  Approved
                </button>
                <button
                  onClick={() => handleTabChange("rejected")}
                  className={`ds-tab ${activeTab === "rejected" ? "ds-tab-active" : ""}`}
                >
                  Rejected
                </button>
                <button
                  onClick={() => handleTabChange("all")}
                  className={`ds-tab ${activeTab === "all" ? "ds-tab-active" : ""}`}
                >
                  All invoices
                </button>
              </>
            ) : isDGM ? (
              <>
                <button
                  onClick={() => handleTabChange("pending")}
                  className={`ds-tab ${activeTab === "pending" ? "ds-tab-active" : ""}`}
                >
                  Pending review
                </button>
                <button
                  onClick={() => handleTabChange("approved")}
                  className={`ds-tab ${activeTab === "approved" ? "ds-tab-active" : ""}`}
                >
                  Approved
                </button>
                <button
                  onClick={() => handleTabChange("rejected")}
                  className={`ds-tab ${activeTab === "rejected" ? "ds-tab-active" : ""}`}
                >
                  Rejected
                </button>
                <button
                  onClick={() => handleTabChange("all")}
                  className={`ds-tab ${activeTab === "all" ? "ds-tab-active" : ""}`}
                >
                  All invoices
                </button>
              </>
            ) : isAdmin ? (
              <>
                <button
                  onClick={() => handleTabChange("all")}
                  className={`ds-tab ${activeTab === "all" ? "ds-tab-active" : ""}`}
                >
                  All Invoices ({baseInvoices.length})
                </button>
                <button
                  onClick={() => handleTabChange("draft")}
                  className={`ds-tab ${activeTab === "draft" ? "ds-tab-active" : ""}`}
                >
                  Drafts ({adminDraftsCount})
                </button>
                <button
                  onClick={() => handleTabChange("pending_ce")}
                  className={`ds-tab ${activeTab === "pending_ce" ? "ds-tab-active" : ""}`}
                >
                  Awaiting CE ({adminCePendingCount})
                </button>
                <button
                  onClick={() => handleTabChange("pending_dgm")}
                  className={`ds-tab ${activeTab === "pending_dgm" ? "ds-tab-active" : ""}`}
                >
                  Awaiting DGM ({adminDgmPendingCount})
                </button>
                <button
                  onClick={() => handleTabChange("finalized")}
                  className={`ds-tab ${activeTab === "finalized" ? "ds-tab-active" : ""}`}
                >
                  Finalized ({adminApprovedCount})
                </button>
                <button
                  onClick={() => handleTabChange("rejected")}
                  className={`ds-tab ${activeTab === "rejected" ? "ds-tab-active" : ""}`}
                >
                  Rejected ({adminRejectedCount})
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => handleTabChange("all")}
                  className={`ds-tab ${activeTab === "all" ? "ds-tab-active" : ""}`}
                >
                  All invoices
                </button>
                <button
                  onClick={() => handleTabChange("draft")}
                  className={`ds-tab ${activeTab === "draft" ? "ds-tab-active" : ""}`}
                >
                  Drafts
                </button>
                <button
                  onClick={() => handleTabChange("submitted")}
                  className={`ds-tab ${activeTab === "submitted" ? "ds-tab-active" : ""}`}
                >
                  Under review
                </button>
                <button
                  onClick={() => handleTabChange("completed")}
                  className={`ds-tab ${activeTab === "completed" ? "ds-tab-active" : ""}`}
                >
                  Approved
                </button>
                <button
                  onClick={() => handleTabChange("rejected")}
                  className={`ds-tab ${activeTab === "rejected" ? "ds-tab-active" : ""}`}
                >
                  Rejected
                </button>
              </>
            )}
          </div>

          {/* Tab Content */}
          <div className="p-6">
            {/* Filter Toolbar */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
              <SearchInput
                value={searchTerm}
                onChange={handleSearchChange}
                onClear={handleSearchClear}
                placeholder="Search by Folio No, File Ref No..."
              />
              {isApprovedTab && (
                <div className="flex items-center gap-3">
                  <MonthFilter 
                    value={selectedMonth}
                    onChange={handleMonthChange}
                    uniqueMonths={displayMonths}
                  />
                  {isPreviousBillCycle && (
                    <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 bg-slate-100 border border-slate-200 px-3 py-1.5 rounded-xl animate-fade-in shadow-2xs">
                      <i className="fas fa-archive text-slate-400"></i>
                      Historical Bill Cycle (View & Download Only)
                    </span>
                  )}
                </div>
              )}
            </div>

            {(isDGM || isCE) && activeTab === "pending" && selectedInvoiceIds.length > 0 && (
              <div className="mb-4 p-4 bg-ink-50 border border-ink-200 rounded-2xl flex items-center justify-between animate-fade-in">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-navy-800">
                    {selectedInvoiceIds.length} Invoice{selectedInvoiceIds.length > 1 ? "s" : ""} Selected
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setBulkActionType("APPROVE");
                      setBulkActionModalOpen(true);
                    }}
                    className="px-4 py-2 bg-success-600 hover:bg-success-700 active:bg-success-800 text-white font-bold text-xs rounded-xl shadow-card transition-all duration-150 flex items-center gap-1.5 cursor-pointer"
                  >
                    <i className="fas fa-check"></i> Bulk Approve
                  </button>
                  <button
                    onClick={() => {
                      setBulkActionType("REJECT");
                      setBulkActionModalOpen(true);
                    }}
                    className="px-4 py-2 bg-critical-700 hover:bg-critical-800 active:bg-critical-800 text-white font-bold text-xs rounded-xl shadow-card transition-all duration-150 flex items-center gap-1.5 cursor-pointer"
                  >
                    <i className="fas fa-ban"></i> Bulk Reject
                  </button>
                </div>
              </div>
            )}
            {loading ? (
              <div className="flex items-center justify-center gap-3 py-12">
                <span className="ds-spinner" aria-hidden="true"></span>
                <span className="text-ink-500 text-sm font-medium">Loading invoices...</span>
              </div>
            ) : filteredInvoices.length === 0 ? (
              <div className="ds-empty">
                <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
                  <i className={`fas ${searchTerm ? "fa-search" : "fa-file-invoice"}`}></i>
                </span>
                <p className="ds-section-title mt-1">
                  {searchTerm ? "No invoices found matching your search" : "No invoices found under this category"}
                </p>
                {searchTerm && (
                  <button
                    type="button"
                    onClick={handleSearchClear}
                    className="ds-btn ds-btn-secondary ds-btn-sm mt-1"
                  >
                    Clear Search
                  </button>
                )}
              </div>
            ) : (
              <div className="w-full border border-ink-200 rounded-xl shadow-card bg-white overflow-hidden">
                {/* Table Horizontal Scroll Wrapper */}
                <div className="overflow-x-auto w-full">
                  <table 
                    className="min-w-full text-left border-collapse"
                    style={{
                      width: table.getTotalSize(),
                      tableLayout: "fixed"
                    }}
                  >
                    <thead>
                      {table.getHeaderGroups().map(headerGroup => (
                        <tr 
                          key={headerGroup.id}
                          className="border-b border-ink-200 text-ink-400 text-xs uppercase tracking-wider bg-ink-50"
                        >
                          {headerGroup.headers.map(header => (
                            <th
                              key={header.id}
                              colSpan={header.colSpan}
                              style={{
                                width: header.getSize(),
                                position: "relative",
                              }}
                              className="py-3 px-4 font-bold text-[11px] uppercase tracking-wider bg-ink-50 border-b border-ink-200 select-none group text-ink-500"
                            >
                              <div className="flex items-center justify-between">
                                <span className="whitespace-nowrap">
                                  {header.isPlaceholder
                                    ? null
                                    : flexRender(header.column.columnDef.header, header.getContext())}
                                </span>
                                
                                {/* Reordering controls (move left/right) */}
                                <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-150 flex items-center gap-1 ml-2 bg-ink-100 p-0.5 rounded border border-ink-200 shrink-0">
                                  <button
                                    type="button"
                                    onClick={() => moveColumnLeft(header.id)}
                                    className="px-1 py-0.5 hover:bg-ink-300 rounded text-[9px] text-ink-500 font-bold focus:outline-none"
                                    title="Move Left"
                                  >
                                    ◀
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => moveColumnRight(header.id)}
                                    className="px-1 py-0.5 hover:bg-ink-300 rounded text-[9px] text-ink-500 font-bold focus:outline-none"
                                    title="Move Right"
                                  >
                                    ▶
                                  </button>
                                </div>
                              </div>

                              {/* Column Resizing Handle */}
                              {header.column.getCanResize() && (
                                <div
                                  {...{
                                    onMouseDown: header.getResizeHandler(),
                                    onTouchStart: header.getResizeHandler(),
                                    className: `absolute right-0 top-0 h-full w-[4px] cursor-col-resize select-none touch-none hover:bg-ink-400 transition-colors ${
                                      header.column.getIsResizing() ? "bg-navy-800 w-[6px]" : "bg-ink-200"
                                    }`,
                                  }}
                                />
                              )}
                            </th>
                          ))}
                        </tr>
                      ))}
                    </thead>
                    <tbody className="divide-y divide-ink-100 text-sm text-ink-700 bg-white">
                      {table.getRowModel().rows.map((row, rowIndex) => (
                        <tr 
                          key={row.id} 
                          className={`
                            transition-all duration-150 
                            ${rowIndex % 2 === 0 ? "bg-white" : "bg-ink-50/30"}
                            hover:bg-ink-50/80
                          `}
                        >
                          {row.getVisibleCells().map(cell => (
                            <td
                              key={cell.id}
                              style={{ 
                                width: cell.column.getSize(),
                                maxWidth: cell.column.getSize(),
                              }}
                              className={getCellClassName(cell.column.id)}
                            >
                              {flexRender(cell.column.columnDef.cell, cell.getContext())}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Fixed Pagination Controls outside table wrapper */}
                <div className="flex flex-col sm:flex-row items-center justify-between p-4 border-t border-ink-100 bg-ink-50 text-ink-500 text-sm gap-4">
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-1.5">
                      <span>Show</span>
                      <select
                        value={table.getState().pagination.pageSize}
                        onChange={e => {
                          table.setPageSize(Number(e.target.value))
                        }}
                        className="border border-ink-200 rounded-xl px-2 py-1 bg-white text-ink-700 focus:outline-none focus:ring-2 focus:ring-navy-800/20 focus:border-navy-800"
                      >
                        {[5, 10, 20, 50].map(pageSize => (
                          <option key={pageSize} value={pageSize}>
                            {pageSize}
                          </option>
                        ))}
                      </select>
                      <span>entries</span>
                    </div>
                    <span>
                      Showing {table.getRowModel().rows.length === 0 ? 0 : table.getState().pagination.pageIndex * table.getState().pagination.pageSize + 1} to{" "}
                      {Math.min(
                        (table.getState().pagination.pageIndex + 1) * table.getState().pagination.pageSize,
                        filteredInvoices.length
                      )}{" "}
                      of {filteredInvoices.length} entries
                    </span>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => table.previousPage()}
                      disabled={!table.getCanPreviousPage()}
                      className="px-3.5 py-1.5 rounded-xl border border-ink-200 bg-white text-ink-600 hover:bg-ink-50 disabled:opacity-50 disabled:hover:bg-white text-xs font-bold flex items-center gap-1 focus:outline-none select-none cursor-pointer"
                    >
                      ◀ Previous
                    </button>
                    <div className="flex items-center gap-1">
                      {Array.from({ length: table.getPageCount() }, (_, i) => (
                        <button
                          key={i}
                          type="button"
                          onClick={() => table.setPageIndex(i)}
                          className={`px-3 py-1 rounded-lg text-xs font-extrabold focus:outline-none select-none cursor-pointer ${
                            table.getState().pagination.pageIndex === i
                              ? "bg-navy-800 text-white"
                              : "border border-ink-200 bg-white text-ink-600 hover:bg-ink-50"
                          }`}
                        >
                          {i + 1}
                        </button>
                      ))}
                    </div>
                    <button
                      type="button"
                      onClick={() => table.nextPage()}
                      disabled={!table.getCanNextPage()}
                      className="px-3.5 py-1.5 rounded-xl border border-ink-200 bg-white text-ink-600 hover:bg-ink-50 disabled:opacity-50 disabled:hover:bg-white text-xs font-bold flex items-center gap-1 focus:outline-none select-none cursor-pointer"
                    >
                      Next ▶
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Invoice Preview / Review Modal */}
      <InvoicePreviewModal
        isOpen={showPreviewModal}
        onClose={() => {
          setShowPreviewModal(false);
          setPreviewInvoiceData(null);
          setSelectedInvoice(null);
          setReviewRemarks("");
        }}
        invoiceData={previewInvoiceData}
        showValidationBtn={false}
        isReviewMode={!isPreviousBillCycle && selectedInvoice !== null && isReviewer}
        reviewRemarks={reviewRemarks}
        onRemarksChange={setReviewRemarks}
        onApprove={handleApprove}
        onReject={handleReject}
        onResubmit={!isPreviousBillCycle && isEE ? handleResubmitInvoice : null}
        onDownload={handleDownloadPdf}
        downloadingId={downloadingId}
        submittingReview={submittingReview}
      />

      {/* Bulk Action Modal */}
      <BulkActionModal
        isOpen={bulkActionModalOpen}
        actionType={bulkActionType}
        selectedCount={selectedInvoiceIds.length}
        onConfirm={handleBulkActionConfirm}
        onClose={() => setBulkActionModalOpen(false)}
        isLoading={isBulkSubmitting}
      />
    </div>
  );
}
