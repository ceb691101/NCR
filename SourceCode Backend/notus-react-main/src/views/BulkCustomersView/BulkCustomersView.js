// src/views/BulkCustomersView/BulkCustomersView.js
import React, { useState, useEffect } from "react";
import BulkCustomersTable from "components/BulkCustomersComp/BulkCustomersTable";
import AreaCodeFilter from "components/AreaCodeFilter";
import NcreTypeFilter from "components/NcreTypeFilter";
import { filterByAreaCode, filterByNcreType } from "utils/filterUtils";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";
import UnsavedChangesModal from "components/Modal/UnsavedChangesModal";
import { toast } from "react-toastify";
import { getBulkCustomersByAreaCd, getAllBulkCustomers, updateBulkCustomerLocation } from "services/bulkCustomersService";
import MapPickerModal from "components/Modal/MapPickerModal";
import {
  getAreaReadingStatus,
  getUserReadingStatus,
} from "services/readingStatusService";
import { getPendingReadingsByAreaCd } from "services/pendingCustomersService";
import {
  getSelectedAreaCode,
  getAreaScopeLabel,
} from "services/AreaAndBillService";
import { useHistory } from "react-router-dom";

const BulkCustomersView = () => {
  const [customers, setCustomers] = useState([]);
  const [originalCustomers, setOriginalCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [areaCodeFilter, setAreaCodeFilter] = useState("");
  const [ncreTypeFilter, setNcreTypeFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [sortOption, setSortOption] = useState("folio_num_asc");
  const [areaInfo, setAreaInfo] = useState({
    area_code: getSelectedAreaCode() || null,
    area_name: null,
    active_bill_cycle: null,
    total_customers: 0,
    with_readings: 0,
    without_readings: 0
  });
  
  const [showMapModal, setShowMapModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [showUnsavedModal, setShowUnsavedModal] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);
  
  const history = useHistory();

  useEffect(() => {
    loadBulkCustomersWithReadingStatus();
  }, []);

  // Sync state if selected area changes
  useEffect(() => {
    const handleAreaChange = () => {
      loadBulkCustomersWithReadingStatus();
    };
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, []);

  useEffect(() => {
    if (originalCustomers.length > 0) {
      applySorting();
    }
  }, [sortOption, originalCustomers]);

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

  const applySorting = () => {
    let sortedCustomers = [...originalCustomers];
    
    switch(sortOption) {
      case "acc_num_asc":
        sortedCustomers.sort((a, b) => {
          const aAcc = a.acc_nbr || "";
          const bAcc = b.acc_nbr || "";
          return aAcc.localeCompare(bAcc, undefined, { numeric: true });
        });
        break;
        
      case "status_received":
        sortedCustomers.sort((a, b) => {
          if (a.has_reading === true && b.has_reading !== true) return -1;
          if (a.has_reading !== true && b.has_reading === true) return 1;
          const aAcc = a.acc_nbr || "";
          const bAcc = b.acc_nbr || "";
          return aAcc.localeCompare(bAcc, undefined, { numeric: true });
        });
        break;
        
      case "status_not_received":
        sortedCustomers.sort((a, b) => {
          if (a.has_reading === false && b.has_reading !== false) return -1;
          if (a.has_reading !== false && b.has_reading === false) return 1;
          const aAcc = a.acc_nbr || "";
          const bAcc = b.acc_nbr || "";
          return aAcc.localeCompare(bAcc, undefined, { numeric: true });
        });
        break;
        
      case "name_asc":
        sortedCustomers.sort((a, b) => {
          const aName = (a.name || "").toLowerCase();
          const bName = (b.name || "").toLowerCase();
          return aName.localeCompare(bName);
        });
        break;
        
      case "folio_num_asc":
        sortedCustomers.sort((a, b) => {
          const aFolio = String(a.folio_no ?? "");
          const bFolio = String(b.folio_no ?? "");
          return aFolio.localeCompare(bFolio, undefined, { numeric: true });
        });
        break;
        
      default:
        sortedCustomers.sort((a, b) => {
          const aAcc = a.acc_nbr || "";
          const bAcc = b.acc_nbr || "";
          return aAcc.localeCompare(bAcc, undefined, { numeric: true });
        });
    }
    
    setCustomers(sortedCustomers);
    setCurrentPage(1);
  };

  const checkUnsavedChanges = () => {
    return hasUnsavedChanges;
  };

  const handleBackClick = () => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => history.push("/admin/dashboard"));
    } else {
      history.push("/admin/dashboard");
    }
  };

  const handleLeavePage = () => {
    setShowUnsavedModal(false);
    if (pendingNavigation) {
      pendingNavigation();
    }
    setHasUnsavedChanges(false);
  };

  const handleStayOnPage = () => {
    setShowUnsavedModal(false);
    setPendingNavigation(null);
  };

  const loadBulkCustomersWithReadingStatus = async () => {
    const selectedAreaCode = getSelectedAreaCode();

    // No selection means every permitted area. Both endpoints below are already scoped
    // server side by the sec_info region/province/area codes.
    if (!selectedAreaCode) {
      setLoading(true);
      try {
        const [customerResponse, readingStatusResponse] = await Promise.all([
          getAllBulkCustomers(),
          getUserReadingStatus(true, false),
        ]);
        const customerData = customerResponse.customers || [];
        const readingStatusMap = new Map();
        if (
          readingStatusResponse.success &&
          Array.isArray(readingStatusResponse.area_reading_status)
        ) {
          for (const area of readingStatusResponse.area_reading_status) {
            const withReadings = area.customers_with_readings_list || [];
            const withoutReadings = area.customers_without_readings_list || [];
            withReadings.forEach((cust) => {
              const acc = (cust.acc_nbr || "").trim();
              readingStatusMap.set(acc, { ...cust, has_reading: true });
            });
            withoutReadings.forEach((cust) => {
              const acc = (cust.acc_nbr || "").trim();
              readingStatusMap.set(acc, { ...cust, has_reading: false });
            });
          }
        }
        const customersWithReadingStatus = customerData.map((customer) => {
          const acc = (customer.acc_nbr || "").trim();
          const readingInfo = readingStatusMap.get(acc);
          return {
            ...customer,
            has_reading: readingInfo ? readingInfo.has_reading : null,
            reading_count: readingInfo ? readingInfo.reading_count : 0,
          };
        });

        setOriginalCustomers(customersWithReadingStatus);
        setAreaInfo({
          area_code: null,
          area_name: getAreaScopeLabel(),
          active_bill_cycle: null,
          total_customers: customersWithReadingStatus.length,
          with_readings: customersWithReadingStatus.filter((c) => c.has_reading).length,
          without_readings: customersWithReadingStatus.filter((c) => !c.has_reading)
            .length,
        });
      } catch (err) {
        console.error(
          "Error loading bulk customers or reading status:",
          err
        );
        toast.error("Failed to fetch customer or reading status data.");
        setCustomers([]);
        setOriginalCustomers([]);
        setAreaInfo({
          area_code: null,
          area_name: getAreaScopeLabel(),
          active_bill_cycle: null,
          total_customers: 0,
          with_readings: 0,
          without_readings: 0,
        });
      } finally {
        setLoading(false);
      }
      return;
    }

    setLoading(true);
    try {
      const [customerResponse, readingStatusResponse, areaInfoResponse] = await Promise.all([
        getBulkCustomersByAreaCd(selectedAreaCode),
        getAreaReadingStatus(selectedAreaCode, true, false),
        getPendingReadingsByAreaCd(selectedAreaCode).catch(() => ({ area_name: null, active_bill_cycle: null }))
      ]);

      const customerData = customerResponse.customers || [];

      let customersWithReadingStatus = [];
      let withReadingsCount = 0;
      let withoutReadingsCount = 0;
      let activeBillCycle = null;

      if (readingStatusResponse.success && readingStatusResponse.reading_status && readingStatusResponse.reading_status.length > 0) {
        const areaReadingStatus = readingStatusResponse.reading_status[0];
        const customersWithReadings = areaReadingStatus.customers_with_readings_list || [];
        const customersWithoutReadings = areaReadingStatus.customers_without_readings_list || [];

        withReadingsCount = customersWithReadings.length;
        withoutReadingsCount = customersWithoutReadings.length;
        activeBillCycle = areaReadingStatus.active_bill_cycle;

        const readingStatusMap = new Map();

        customersWithReadings.forEach(customer => {
          const acc = (customer.acc_nbr || "").trim();
          readingStatusMap.set(acc, {
            ...customer,
            has_reading: true
          });
        });

        customersWithoutReadings.forEach(customer => {
          const acc = (customer.acc_nbr || "").trim();
          readingStatusMap.set(acc, {
            ...customer,
            has_reading: false
          });
        });

        customersWithReadingStatus = customerData.map(customer => {
          const acc = (customer.acc_nbr || "").trim();
          const readingInfo = readingStatusMap.get(acc);

          return {
            ...customer,
            has_reading: readingInfo ? readingInfo.has_reading : null,
            reading_count: readingInfo ? readingInfo.reading_count : 0
          };
        });

        readingStatusMap.forEach((readingCustomer, accNbr) => {
          if (!customerData.find(c => (c.acc_nbr || "").trim() === accNbr)) {
            customersWithReadingStatus.push({
              ...readingCustomer,
              job_nbr: readingCustomer.job_nbr || null,
              facility_name: readingCustomer.facility_name || null,
              folio_no: readingCustomer.folio_no || null,
              area_cd: readingCustomer.area_cd || selectedAreaCode,
              bill_cycle: readingCustomer.bill_cycle || null,
              cus_cat: readingCustomer.cus_cat || null,
              name: readingCustomer.name || null,
              address_l1: readingCustomer.address_l1 || null,
              mobile_no: readingCustomer.mobile_no || null,
              tel_nbr: readingCustomer.tel_nbr || null,
              tariff: readingCustomer.tariff || null
            });
          }
        });

      } else {
        customersWithReadingStatus = customerData.map(customer => ({
          ...customer,
          has_reading: null,
          reading_count: 0
        }));
      }

      const userCat = sessionStorage.getItem("user_category");
      const userId = sessionStorage.getItem("user_id");
      const isEE = userCat === "EE" || userCat === "Electrical Engineer";
      const cleanId = (str) => (str || '').replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
      const matchEE = (resp, uId) => {
        if (!resp || !uId) return false;
        const cResp = cleanId(resp);
        const cUser = cleanId(uId);
        if (!cResp || !cUser) return false;
        return cResp === cUser || cResp.includes(cUser);
      };

      if (isEE && userId) {
        customersWithReadingStatus = customersWithReadingStatus.filter(c => {
          const resp = c.responsible_ee || c.responsibleEe || c.responsble_ee || '';
          return matchEE(resp, userId);
        });
      }

      setOriginalCustomers(customersWithReadingStatus);

      setAreaInfo({
        area_code: selectedAreaCode,
        area_name: areaInfoResponse.area_name || selectedAreaCode,
        active_bill_cycle: activeBillCycle || areaInfoResponse.active_bill_cycle,
        total_customers: customersWithReadingStatus.length,
        with_readings: customersWithReadingStatus.filter(c => c.has_reading).length,
        without_readings: customersWithReadingStatus.filter(c => !c.has_reading).length
      });

    } catch (err) {
      console.error("Error loading ncre developers with reading status:", err);

      try {
        const [customerResponse, areaInfoResponse] = await Promise.all([
          getBulkCustomersByAreaCd(selectedAreaCode),
          getPendingReadingsByAreaCd(selectedAreaCode).catch(() => ({ area_name: null, active_bill_cycle: null }))
        ]);

        const customerData = customerResponse.customers || [];
        const customersWithoutStatus = customerData.map(customer => ({
          ...customer,
          has_reading: null,
          reading_count: 0
        }));

        setOriginalCustomers(customersWithoutStatus);

        setAreaInfo({
          area_code: selectedAreaCode,
          area_name: areaInfoResponse.area_name || selectedAreaCode,
          active_bill_cycle: areaInfoResponse.active_bill_cycle || null,
          total_customers: customersWithoutStatus.length,
          with_readings: 0,
          without_readings: 0
        });
        toast.warning("Loaded customers but could not fetch reading status.");

      } catch (fallbackErr) {
        console.error("Fallback customer loading also failed:", fallbackErr);
        toast.error("Failed to fetch ncre developers from server.");
        setCustomers([]);
        setOriginalCustomers([]);
        setAreaInfo({
          area_code: selectedAreaCode,
          area_name: null,
          active_bill_cycle: null,
          total_customers: 0,
          with_readings: 0,
          without_readings: 0
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleViewCustomer = (customer) => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => history.push("/developerRegistration", { searchField: "folio_no", searchValue: customer.folio_no }));
    } else {
      history.push("/developerRegistration", { searchField: "folio_no", searchValue: customer.folio_no });
    }
  };

  const handleUpdateLocation = (customer) => {
    setSelectedCustomer(customer);
    setShowMapModal(true);
  };

  const handleSaveLocation = async (position) => {
    if (!selectedCustomer) return;
    const [latitude, longitude] = position;
    try {
      await updateBulkCustomerLocation(selectedCustomer.acc_nbr, latitude, longitude);
      toast.success("Location updated successfully!");
      setShowMapModal(false);
      setSelectedCustomer(null);
    } catch (err) {
      toast.error(err.message || "Failed to update location");
    }
  };

  const handleSearch = (term) => {
    setSearchTerm(term);
    setCurrentPage(1);
  };

  const handleRecordsPerPageChange = (records) => {
    setRecordsPerPage(records);
    setCurrentPage(1);
  };

  const handleSortOptionChange = (option) => {
    setSortOption(option);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  const handleRefresh = () => {
    loadBulkCustomersWithReadingStatus();
  };

  let filteredCustomers = customers;
  if (areaInfo.area_code === "ALL" && areaCodeFilter) {
    filteredCustomers = filterByAreaCode(filteredCustomers, areaCodeFilter);
  }
  if (areaInfo.area_code === "ALL" && ncreTypeFilter) {
    filteredCustomers = filterByNcreType(filteredCustomers, ncreTypeFilter);
  }
  filteredCustomers = filteredCustomers.filter(customer => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();
    return (
      customer.acc_nbr?.toLowerCase().includes(searchLower) ||
      String(customer.folio_no ?? "").toLowerCase().includes(searchLower) ||
      customer.area_cd?.toLowerCase().includes(searchLower) ||
      customer.bill_cycle?.toString().includes(searchLower) ||
      customer.cus_cat?.toLowerCase().includes(searchLower) ||
      customer.name?.toLowerCase().includes(searchLower) ||
      customer.mobile_no?.toLowerCase().includes(searchLower) ||
      customer.tariff?.toLowerCase().includes(searchLower)
    );
  });

  const totalRecords = filteredCustomers.length;
  const totalPages = Math.ceil(totalRecords / recordsPerPage);
  const startIndex = (currentPage - 1) * recordsPerPage;
  const endIndex = startIndex + recordsPerPage;
  const currentRecords = filteredCustomers.slice(startIndex, endIndex);

  const breadcrumbItems = [
    { label: "Dashboard", href: "/admin/dashboard" },
    { label: "NCRE Developers", href: null }
  ];

  return (
    <div className="flex flex-col gap-ds-6">
      <UnsavedChangesModal
        isOpen={showUnsavedModal}
        onLeave={handleLeavePage}
        onStay={handleStayOnPage}
      />
      
      {/* Premium Navy Gradient Stripe Header Block */}
      <div>
        <div className="ds-page-toolbar mx-auto">
          <div>
            <h1 className="ds-page-title">
              NCRE Developers
            </h1>
            <p className="ds-page-subtitle mt-1">Manage and register grid-connected NCRE developer profiles and coordinates</p>
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
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-ds-4 mt-8">
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
              <p className="ds-stat-note">Code: {areaInfo.area_code || "..."}</p>
            </div>
          </div>

          {/* Stat: Bill Cycle */}
          <div className="ds-stat-card">
            <div className="ds-stat-icon ds-stat-icon-info">
              <i className="fas fa-calendar-alt"></i>
            </div>
            <div className="flex-1 min-w-0">
              <p className="ds-stat-label">Bill Cycle</p>
              {loading && !areaInfo.active_bill_cycle ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (
                <p className="text-xl font-extrabold text-ink-800 my-0.5 truncate">
                  {areaInfo.active_bill_cycle || "N/A"}
                </p>
              )}
              <p className="ds-stat-note">Active cycle</p>
            </div>
          </div>

          {/* Stat: Received Readings */}
          <div className="ds-stat-card">
            <div className="ds-stat-icon ds-stat-icon-success">
              <i className="fas fa-check-circle"></i>
            </div>
            <div className="flex-1 min-w-0">
              <p className="ds-stat-label">Readings Received</p>
              {loading ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (
                <p className="text-xl font-extrabold text-ink-800 my-0.5">
                  {areaInfo.with_readings}
                </p>
              )}
              <p className="ds-stat-note">Received count</p>
            </div>
          </div>

          {/* Stat: Pending Readings */}
          <div className="ds-stat-card">
            <div className="ds-stat-icon ds-stat-icon-critical">
              <i className="fas fa-times-circle"></i>
            </div>
            <div className="flex-1 min-w-0">
              <p className="ds-stat-label">Readings Pending</p>
              {loading ? (
                <div className="flex items-center space-x-1.5 my-2.5 h-8">
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                  <span className="w-2.5 h-2.5 bg-ink-400 rounded-full animate-bounce"></span>
                </div>
              ) : (
                <p className="text-xl font-extrabold text-ink-800 my-0.5">
                  {areaInfo.without_readings}
                </p>
              )}
              <p className="ds-stat-note">Pending count</p>
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

          {/* Area Code & NCRe Type Filters (only for ALL regions) */}
          {areaInfo.area_code === "ALL" && (
            <div className="mb-4 flex flex-col sm:flex-row gap-2 items-start sm:items-center">
              <AreaCodeFilter value={areaCodeFilter} onChange={setAreaCodeFilter} />
              <NcreTypeFilter value={ncreTypeFilter} onChange={setNcreTypeFilter} />
            </div>
          )}

          {/* Main Table */}
          <BulkCustomersTable
            customers={currentRecords}
            loading={loading}
            searchTerm={searchTerm}
            onSearch={handleSearch}
            recordsPerPage={recordsPerPage}
            onRecordsPerPageChange={handleRecordsPerPageChange}
            sortOption={sortOption}
            onSortOptionChange={handleSortOptionChange}
            currentPage={currentPage}
            totalPages={totalPages}
            totalRecords={totalRecords}
            onPageChange={handlePageChange}
            onViewCustomer={handleViewCustomer}
            onUpdateLocation={handleUpdateLocation}
            areaInfo={areaInfo}
          />
        </div>
      </div>

      {/* Map Picker Modal */}
      <MapPickerModal
        isOpen={showMapModal}
        onClose={() => { setShowMapModal(false); setSelectedCustomer(null); }}
        onSave={handleSaveLocation}
        initialPosition={null}
      />
    </div>
  );
};

export default BulkCustomersView;