// src/views/TempReadingsView/TempReadingsView.js
import React, { useState, useEffect, useCallback } from "react";
import { useLocation, useHistory } from "react-router-dom";
import TempReadingsTable from "components/TempReadingsComp/TempReadingsTable";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";
import UnsavedChangesModal from "components/Modal/UnsavedChangesModal";
import { toast } from "react-toastify";
import {
  getTempReadingsByAreaCd,
  getTempReadingsByAreaCodes,
  groupReadingsByAccount,
} from "services/tempReadingsService";
import { prepareInvoice } from "services/invoiceService";
import InvoicePreviewModal from "components/Modal/InvoicePreviewModal";
import { getPendingReadingsByAreaCd } from "services/pendingCustomersService";
import { getCurrentOpenBillCycle } from "services/billCycleEndingService";
import {
  getSelectedAreaCode,
  getAreaScopeLabel,
} from "services/AreaAndBillService";
import { calculateRuDifference, isRuWithinLimit } from "utils/readingUtils";

const TempReadingsView = () => {
  const [readings, setReadings] = useState([]);
  const [groupedReadings, setGroupedReadings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [includeAllCycles, setIncludeAllCycles] = useState(false);
  const [activeBillCycle, setActiveBillCycle] = useState(null);
  const [filteredByActiveBillCycle, setFilteredByActiveBillCycle] =
    useState(true);
  const [areaInfo, setAreaInfo] = useState({
    area_code: getSelectedAreaCode() || null,
    area_name: null,
    active_bill_cycle: null,
    reading_count: 0,
  });
  const [errorFilter, setErrorFilter] = useState(null);
  const [errorName, setErrorName] = useState(null);
  const [initialLoad, setInitialLoad] = useState(true);
  const [preparingInvoiceAccountNumber, setPreparingInvoiceAccountNumber] =
    useState(null);
  const [showInvoiceModal, setShowInvoiceModal] = useState(false);
  const [invoiceModalData, setInvoiceModalData] = useState(null);
  const [invoiceFilter, setInvoiceFilter] = useState(
    typeof sessionStorage !== "undefined" &&
    sessionStorage.getItem("user_category") === "Electrical Engineer"
      ? "pending"
      : "all"
  ); // "pending" | "all"

  // New states for breadcrumb and unsaved changes
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [showUnsavedModal, setShowUnsavedModal] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);

  const location = useLocation();
  const history = useHistory();

  useEffect(() => {
    let isMounted = true;
    getCurrentOpenBillCycle()
      .then((cycle) => {
        if (isMounted && cycle.bill_cycle != null) {
          setActiveBillCycle(String(cycle.bill_cycle));
        }
      })
      .catch((error) => {
        console.error("Unable to load global bill cycle:", error);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // Memoized function to extract error filter from URL
  const extractErrorFilterFromURL = useCallback(() => {
    const pathParts = location.pathname.split("/");
    let errorSlug = null;
    let errorCode = null;
    let errorNameFromSlug = null;
    let isRuErrorFilter = false;

    const urlParams = new URLSearchParams(location.search);
    const filterParam = urlParams.get("filter");

    if (
      filterParam === "error" ||
      filterParam === "ru24" ||
      (pathParts.length > 2 && pathParts[1] === "tempReadings" && pathParts[2] === "error")
    ) {
      isRuErrorFilter = true;
      errorNameFromSlug = "Error";
    } else if (
      pathParts.length > 2 &&
      pathParts[1] === "tempReadings" &&
      pathParts[2] !== ""
    ) {
      errorSlug = pathParts[2];
      errorNameFromSlug = errorSlug
        .split("-")
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");

      errorCode = urlParams.get("errorCode");
    } else if (urlParams.get("errorCode")) {
      errorCode = urlParams.get("errorCode");
    }

    return {
      errorCode: errorCode ? parseInt(errorCode) : null,
      errorName: errorNameFromSlug,
      isRuErrorFilter,
    };
  }, [location]);

  // Handle browser back button and unsaved changes
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (checkUnsavedChanges()) {
        e.preventDefault();
        e.returnValue = "";
        return "";
      }
    };

    const handlePopState = (e) => {
      if (checkUnsavedChanges()) {
        e.preventDefault();
        setShowUnsavedModal(true);
        setPendingNavigation(() => () => {
          // Go back in history
          history.go(-1);
        });
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      window.removeEventListener("popstate", handlePopState);
    };
  }, [hasUnsavedChanges, history]);

  // Sync state if selected area changes
  useEffect(() => {
    const handleAreaChange = () => {
      const code = getSelectedAreaCode();
      setAreaInfo((prev) => ({
        ...prev,
        area_code: code,
      }));
    };
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, []);

  // Function to check if there are unsaved changes
  const checkUnsavedChanges = () => {
    return hasUnsavedChanges;
  };

  // Handle breadcrumb back click
  const handleBackClick = () => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => history.push("/admin/dashboard"));
    } else {
      history.push("/admin/dashboard");
    }
  };

  // Handle leaving the page (from modal)
  const handleLeavePage = () => {
    setShowUnsavedModal(false);
    if (pendingNavigation) {
      pendingNavigation();
    }
    // Reset unsaved changes flag
    setHasUnsavedChanges(false);
  };

  // Handle staying on the page (from modal)
  const handleStayOnPage = () => {
    setShowUnsavedModal(false);
    setPendingNavigation(null);
  };

  const handleInvoiceFilterChange = (filter) => {
    setInvoiceFilter(filter);
    setCurrentPage(1);
  };

  // Load data based on current filters
  const loadTempReadings = useCallback(async () => {
    const selectedAreaCode = getSelectedAreaCode();

    setLoading(true);

    try {
      let tempReadingsResponse;
      let areaInfoResponse;

      // Extract current error filter
      const { errorCode, errorName, isRuErrorFilter } = extractErrorFilterFromURL();
      const currentFilterKey = errorCode || (isRuErrorFilter ? "ru24" : null);
      setErrorFilter(currentFilterKey);
      setErrorName(errorName);

      if (selectedAreaCode) {
        // Always fetch area info for proper area_name and active_bill_cycle
        areaInfoResponse = await getPendingReadingsByAreaCd(selectedAreaCode);
      }

      if (errorCode) {
        // Fetch error-specific readings immediately
        console.log(
          `Loading error-specific readings for error code: ${errorCode}`
        );
        tempReadingsResponse = selectedAreaCode
          ? await getErrorReadings(selectedAreaCode, errorCode)
          // No single area selected: the error list is already scoped to every
          // permitted area by the backend.
          : await getErrorReadingsForScope(errorCode);
      } else if (!selectedAreaCode) {
        // All permitted areas. The backend resolves the area list and each
        // area's own active bill cycle.
        console.log(
          `Loading temporary readings for all permitted areas (excludeFinalized=${invoiceFilter === "pending"})`
        );
        tempReadingsResponse = await getTempReadingsByAreaCodes(
          includeAllCycles,
          invoiceFilter === "pending"
        );
      } else {
        // Fetch temp readings with finalized filter
        console.log(`Loading temporary readings (excludeFinalized=${invoiceFilter === "pending"})`);
        tempReadingsResponse = await getTempReadingsByAreaCd(
          selectedAreaCode,
          includeAllCycles,
          invoiceFilter === "pending"
        );
      }

      // Handle the enhanced response structure
      let filteredList = tempReadingsResponse.readings || (Array.isArray(tempReadingsResponse) ? tempReadingsResponse : []);
      setReadings(filteredList);
      if (tempReadingsResponse.activeBillCycle) {
        setActiveBillCycle(tempReadingsResponse.activeBillCycle);
      }
      if (tempReadingsResponse.filteredByActiveBillCycle !== undefined) {
        setFilteredByActiveBillCycle(
          tempReadingsResponse.filteredByActiveBillCycle
        );
      }

      // Group readings by account number
      const grouped = groupReadingsByAccount(filteredList);

      // Show all accounts and their differences for this area
      setGroupedReadings(grouped);

      setAreaInfo({
        area_code: selectedAreaCode || tempReadingsResponse.areaCode || null,
        area_name: selectedAreaCode
          ? areaInfoResponse.area_name || selectedAreaCode
          : getAreaScopeLabel(),
        active_bill_cycle:
          tempReadingsResponse.activeBillCycle ||
          (areaInfoResponse ? areaInfoResponse.active_bill_cycle : null),
        reading_count: grouped.length,
      });

      console.log("Successfully fetched temp readings from backend:", {
        errorFilter: currentFilterKey,
        readingCount: grouped.length,
        areaCode: selectedAreaCode || "ALL_PERMITTED",
      });
    } catch (err) {
      console.error("Error fetching temp readings from backend:", err);
      toast.error("Failed to fetch readings from server.");
      setReadings([]);
      setGroupedReadings([]);
      setAreaInfo({
        area_code: selectedAreaCode,
        area_name: null,
        active_bill_cycle: null,
        reading_count: 0,
      });
    } finally {
      setLoading(false);
      setInitialLoad(false);
    }
  }, [includeAllCycles, invoiceFilter, extractErrorFilterFromURL]);

  // Effect for initial load and URL changes
  useEffect(() => {
    const { errorCode, errorName, isRuErrorFilter } = extractErrorFilterFromURL();
    const currentFilterKey = errorCode || (isRuErrorFilter ? "ru24" : null);

    // Only reset and reload if the error filter has actually changed
    if (errorFilter !== currentFilterKey || initialLoad) {
      setErrorFilter(currentFilterKey);
      setErrorName(errorName);
      setCurrentPage(1); // Reset to first page when filter changes
      loadTempReadings();
    }
  }, [
    location,
    loadTempReadings,
    errorFilter,
    initialLoad,
    extractErrorFilterFromURL,
  ]);

  // Effect for includeAllCycles or invoiceFilter changes (only when no error filter is active)
  useEffect(() => {
    if (!errorFilter && !initialLoad) {
      loadTempReadings();
    }
  }, [includeAllCycles, invoiceFilter, errorFilter, initialLoad, loadTempReadings]);

  const getErrorReadings = async (areaCode, errorCode) => {
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      const baseUrl = process.env.REACT_APP_API_BASE_URL;

      const response = await fetch(
        `${baseUrl}/api/v1/error-statistics/error-details`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
          credentials: "include",
          body: JSON.stringify({
            session_id: sessionId,
            user_id: userId,
            area_code: areaCode,
            error_code: errorCode,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();

      if (data.success && data.accounts_with_error) {
        // Get active bill cycle for the area
        const billCycleResponse = await fetch(
          `${baseUrl}/api/v1/tmp-readings/area/${areaCode}/active-bill-cycle`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: "Basic " + btoa("user:admin123"),
            },
            credentials: "include",
          }
        );

        let activeBillCycle = "";
        if (billCycleResponse.ok) {
          const billCycleData = await billCycleResponse.json();
          activeBillCycle = billCycleData.active_bill_cycle ? String(billCycleData.active_bill_cycle) : "";
        }

        // Transform error details to include ALL readings with error flag
        const errorReadings = [];

        data.accounts_with_error.forEach((account) => {
          // Get all readings for this account (including non-error ones)
          account.error_instances.forEach((instance) => {
            errorReadings.push({
              acc_nbr: account.account_number,
              inst_id: "N/A",
              area_cd: account.area_code,
              added_blcy: activeBillCycle,
              mtr_seq: 1,
              mtr_type: instance.meter_type,
              prv_date: instance.reading_date, // Will be updated when loading full data
              rdng_date: instance.reading_date,
              prsnt_rdn: instance.present_reading,
              prv_rdn: instance.previous_reading,
              mtr_nbr: "N/A",
              units: instance.units,
              rate: 0.0,
              computed_chg: 0.0,
              mnt_chg: 0.0,
              acode: " ",
              m_factor: 1.0,
              bill_stat: null,
              err_stat: instance.has_error ? errorCode : 0, // Set error status only for error readings
              mtr_stat: "0",
              rdn_stat: null,
              user_id: null,
              entered_dtime: new Date().toISOString(),
              edited_user_id: null,
              edited_dtime: null,
              // Add error flag for frontend highlighting
              has_error: instance.has_error || false,
              error_code: instance.error_code,
              error_name: instance.error_name,
            });
          });
        });

        return {
          readings: errorReadings,
          active_bill_cycle: activeBillCycle,
          area_name: data.area_name || `Area ${areaCode}`,
        };
      }

      return { readings: [] };
    } catch (error) {
      console.error("Error fetching error details:", error);
      throw error;
    }
  };

  // Error readings for every area in scope. Bill cycles differ per area, so each
  // area is asked for its own active cycle instead of assuming one shared value.
  const getErrorReadingsForScope = async (errorCode) => {
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");
      const baseUrl = process.env.REACT_APP_API_BASE_URL;

      const response = await fetch(`${baseUrl}/api/v1/error-statistics/error-details-all`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
          "X-Session-Id": sessionId,
        },
        credentials: "include",
        body: JSON.stringify({
          session_id: sessionId,
          user_id: userId,
          error_code: errorCode,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      if (!data.success || !Array.isArray(data.accounts_with_error)) {
        return { readings: [] };
      }

      // Each account carries its own area and bill cycle from the response, so
      // multi-area results stay correctly attributed.
      const errorReadings = [];
      data.accounts_with_error.forEach((account) => {
        const activeBillCycle = account.bill_cycle || "444";
        (account.error_instances || []).forEach((instance) => {
          errorReadings.push({
            acc_nbr: account.account_number,
            inst_id: "N/A",
            area_cd: account.area_code,
            added_blcy: activeBillCycle,
            mtr_seq: 1,
            mtr_type: instance.meter_type,
            prv_date: instance.reading_date,
            rdng_date: instance.reading_date,
            prsnt_rdn: instance.present_reading,
            prv_rdn: instance.previous_reading,
            mtr_nbr: "N/A",
            units: instance.units,
            rate: 0.0,
            computed_chg: 0.0,
            mnt_chg: 0.0,
            acode: " ",
            m_factor: 1.0,
            bill_stat: null,
            err_stat: instance.has_error ? errorCode : 0,
            mtr_stat: "0",
            rdn_stat: null,
            user_id: null,
            entered_dtime: new Date().toISOString(),
            edited_user_id: null,
            edited_dtime: null,
            has_error: instance.has_error || false,
            error_code: instance.error_code,
            error_name: instance.error_name,
          });
        });
      });

      return { readings: errorReadings };
    } catch (error) {
      console.error("Error fetching error readings for all areas:", error);
      throw error;
    }
  };

  const handleViewReading = (reading) => {
    const details = `
Folio Number: ${reading.folio_no ?? "N/A"}
Area Code: ${reading.area_cd}
Meter Type: ${reading.mtr_type}
Reading Date: ${new Date(reading.rdng_date).toLocaleDateString()}
Present Reading: ${reading.prsnt_rdn || "N/A"}
Previous Reading: ${reading.prv_rdn || "N/A"}
Units: ${reading.units || "N/A"}
    `.trim();
    alert(details);
  };

  const handleSearch = (term) => {
    setSearchTerm(term);
    setCurrentPage(1);
  };

  const handleRecordsPerPageChange = (records) => {
    setRecordsPerPage(records);
    setCurrentPage(1);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  const handleToggleAllCycles = () => {
    setIncludeAllCycles(!includeAllCycles);
    setCurrentPage(1);
  };

  const handleRefresh = () => {
    loadTempReadings();
  };

  const handlePrepareInvoice = async (group) => {
    if (!group?.acc_nbr || !group?.area_cd || !group?.added_blcy) {
      toast.error(
        "Missing account, area, or bill cycle details for invoice preparation."
      );
      return;
    }

    setPreparingInvoiceAccountNumber(group.acc_nbr);

    try {
      const data = await prepareInvoice({
        accountNumber: group.acc_nbr,
        areaCode: group.area_cd,
        billCycle: group.added_blcy,
      });

      if (data) {
        const enrichedData = {
          ...data,
          areaCode: group.area_cd,
          billCycle: group.added_blcy,
        };
        setInvoiceModalData(enrichedData);
        setShowInvoiceModal(true);
        toast.success(`Invoice prepared successfully for Folio ${group.folio_no ?? "N/A"}.`);
      } else {
        toast.error("Failed to prepare invoice: Empty response data received.");
      }
    } catch (error) {
      toast.error(error.message || "Failed to prepare invoice.");
    } finally {
      setPreparingInvoiceAccountNumber(null);
    }
  };

  const getGroupRuDiff = (group) => {
    const kwd = group.readings?.find((r) => r.mtr_type === "KWD");
    const kwp = group.readings?.find((r) => r.mtr_type === "KWP");
    const kwo = group.readings?.find((r) => r.mtr_type === "KWO");
    const ru = group.readings?.find((r) => r.mtr_type === "RU" || r.mtr_type === "KWT");
    return calculateRuDifference(
      kwd ? kwd.units : null,
      kwp ? kwp.units : null,
      kwo ? kwo.units : null,
      ru ? ru.units : null
    );
  };

  const isGroupRuExceeded = (group) => {
    const diff = getGroupRuDiff(group);
    if (diff === null || diff === undefined) return false;
    const limit = group.accept_ru ?? group.readings?.find((r) => r.mtr_type === "RU" || r.mtr_type === "KWT")?.accept_ru;
    return !isRuWithinLimit(diff, limit);
  };

  const isRuError =
    errorFilter === "ru24" ||
    errorFilter === 24 ||
    errorFilter === "24" ||
    location.search.includes("filter=error") ||
    location.search.includes("filter=ru24") ||
    location.pathname.includes("/tempReadings/error");

  const outsideLimitCount = groupedReadings.filter(isGroupRuExceeded).length;

  // Filter grouped readings based on search term and RU limit on error page
  const filteredGroupedReadings = groupedReadings.filter((group) => {
    // On the error reading page, only show accounts where RU difference is exceeded
    if (isRuError && !isGroupRuExceeded(group)) {
      return false;
    }

    if (!searchTerm) return true;

    const searchLower = searchTerm.toLowerCase();
    return (
      group.acc_nbr?.toLowerCase().includes(searchLower) ||
      String(group.folio_no ?? "").toLowerCase().includes(searchLower) ||
      group.area_cd?.toLowerCase().includes(searchLower) ||
      group.added_blcy?.toLowerCase().includes(searchLower) ||
      group.readings.some(
        (reading) =>
          reading.mtr_type?.toLowerCase().includes(searchLower) ||
          reading.rdng_date?.toString().includes(searchLower) ||
          reading.mtr_nbr?.toLowerCase().includes(searchLower)
      )
    );
  });

  // Calculate pagination for grouped data
  const totalRecords = filteredGroupedReadings.length;
  const totalPages = Math.ceil(totalRecords / recordsPerPage);
  const startIndex = (currentPage - 1) * recordsPerPage;
  const endIndex = startIndex + recordsPerPage;
  const currentRecords = filteredGroupedReadings.slice(startIndex, endIndex);

  const getHeaderTitle = () => {
    if (isRuError) {
      return `RU Difference Readings`;
    }
    if (errorName) {
      return `${errorName} Readings`;
    }
    return `Received Readings`;
  };

  // Breadcrumb items
  const breadcrumbItems = [
    { label: "Dashboard", href: "/admin/dashboard" },
    { label: "Monthly Readings", href: null },
    {
      label: isRuError ? "RU Difference Readings" : (errorName ? `${errorName} Readings` : "Received Readings"),
      href: null,
    },
  ];

  return (
    <div className="flex flex-col gap-ds-6">
      <UnsavedChangesModal
        isOpen={showUnsavedModal}
        onLeave={handleLeavePage}
        onStay={handleStayOnPage}
      />

      <InvoicePreviewModal
        isOpen={showInvoiceModal}
        onClose={() => {
          setShowInvoiceModal(false);
          setInvoiceModalData(null);
        }}
        invoiceData={invoiceModalData}
      />

      {/* Premium Navy Gradient Stripe Header Block */}
      <div>
        <div className="ds-page-toolbar mx-auto">
          <div>
            <h1 className="ds-page-title">
              {getHeaderTitle()}
            </h1>
            <p className="ds-page-subtitle mt-1">
              {isRuError 
                ? "Showing accounts where RU difference exceeds acceptable limit"
                : "Manage and view received meter readings and prepare bills"}
            </p>
          </div>
          <div className="ds-page-actions">
            <button
              onClick={handleRefresh}
              className="ds-btn ds-btn-secondary ds-btn-sm"
              title="Refresh data"
            >
              <i className="fas fa-sync-alt"></i>
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Stats Cards Section inside Stripe */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-ds-4 mt-8">
          {/* Stat: Area */}
          <div className="ds-stat-card">
            <div className="ds-stat-icon ds-stat-icon-info">
              <i className="fas fa-map-marked-alt"></i>
            </div>
            <div className="flex-1 min-w-0">
              <p className="ds-stat-label">Area</p>
              {loading && !areaInfo.area_name ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (
                <p className="text-xl font-extrabold text-ink-800 my-0.5 truncate">
                  {areaInfo.area_name || areaInfo.area_code || "N/A"}
                </p>
              )}
              <p className="ds-stat-note">
                Code: {areaInfo.area_code || "..."}
              </p>
            </div>
          </div>

          {/* Stat: Bill Cycle */}
          <div className="ds-stat-card">
            <div className="ds-stat-icon ds-stat-icon-info">
              <i className="fas fa-calendar-alt"></i>
            </div>
            <div className="flex-1 min-w-0">

              <p className="ds-stat-label">Bill Cycle</p>
              {loading && !activeBillCycle && !areaInfo.active_bill_cycle ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (

                <p className="text-xl font-extrabold text-ink-800 my-0.5 truncate">
                  {activeBillCycle ?? areaInfo.active_bill_cycle ?? "N/A"}
                </p>
              )}
              <p className="ds-stat-note">Active cycle</p>
            </div>
          </div>

          {/* Stat: Received / Error Readings count */}
          <div className="ds-stat-card">
            <div className={`ds-stat-icon ${isRuError ? "ds-stat-icon-critical" : (invoiceFilter === "pending" ? "ds-stat-icon-info" : "ds-stat-icon-success")}`}>
              <i className={isRuError ? "fas fa-exclamation-triangle" : (invoiceFilter === "pending" ? "fas fa-file-invoice" : "fas fa-list-alt")}></i>
            </div>
            <div className="flex-1 min-w-0">
              <p className="ds-stat-label">
                {isRuError ? "Error Accounts" : (invoiceFilter === "pending" ? "Pending Invoices" : "Received Readings")}
              </p>
              {loading ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (
                <p className="text-xl font-extrabold text-ink-800 my-0.5">
                  {isRuError ? outsideLimitCount : areaInfo.reading_count}
                </p>
              )}
              <p className="ds-stat-note">
                {isRuError ? `${outsideLimitCount} outside limit` : (invoiceFilter === "pending" ? "To be prepared" : "All received")}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="px-0 mx-auto w-full relative mb-8">
        <div className="ds-card p-ds-5 overflow-hidden">
          {/* Breadcrumb wrapper */}
          <div className="mb-6 p-3.5 bg-ink-50 border border-ink-100 rounded-2xl shadow-xs">
            <Breadcrumb
              items={breadcrumbItems}
              onBackClick={handleBackClick}
              hasUnsavedChanges={hasUnsavedChanges}
            />
          </div>

          {/* Main Table */}
          <TempReadingsTable
            groupedReadings={currentRecords}
            loading={loading}
            searchTerm={searchTerm}
            onSearch={handleSearch}
            recordsPerPage={recordsPerPage}
            onRecordsPerPageChange={handleRecordsPerPageChange}
            currentPage={currentPage}
            totalPages={totalPages}
            totalRecords={totalRecords}
            onPageChange={handlePageChange}
            onViewReading={handleViewReading}
            activeBillCycle={activeBillCycle}
            includeAllCycles={includeAllCycles}
            onToggleAllCycles={handleToggleAllCycles}
            filteredByActiveBillCycle={filteredByActiveBillCycle}
            areaInfo={areaInfo}
            errorFilter={errorFilter}
            errorName={errorName}
            invoiceFilter={invoiceFilter}
            onInvoiceFilterChange={handleInvoiceFilterChange}
            onPrepareInvoice={handlePrepareInvoice}
            preparingInvoiceAccountNumber={preparingInvoiceAccountNumber}
          />
        </div>
      </div>
    </div>
  );
};

export default TempReadingsView;
