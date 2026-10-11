import React, { useState, useEffect } from "react";
import PendReadingsTable from "components/PendReadingsComp/PendReadingsTable";
import AreaCodeFilter from "components/AreaCodeFilter";
import NcreTypeFilter from "components/NcreTypeFilter";
import { filterByAreaCode, filterByNcreType } from "utils/filterUtils";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";
import UnsavedChangesModal from "components/Modal/UnsavedChangesModal";
import { toast } from "react-toastify";
import { getPendingReadingsByAreaCd, getAllPendingReadings } from "services/pendingCustomersService";
import {
  getSelectedAreaCode,
  getAreaScopeLabel,
} from "services/AreaAndBillService";
import { useHistory } from "react-router-dom";
import PageHeader from "components/Headers/PageHeader";

const PendCustomersView = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [areaCodeFilter, setAreaCodeFilter] = useState("");
  const [ncreTypeFilter, setNcreTypeFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [recordsPerPage, setRecordsPerPage] = useState(10);
  const [areaInfo, setAreaInfo] = useState({
    area_code: getSelectedAreaCode() || null,
    area_name: null,
    active_bill_cycle: null,
    pending_count: 0
  });
  
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [showUnsavedModal, setShowUnsavedModal] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);
  
  const history = useHistory();

  useEffect(() => {
    loadPendingReadings();
  }, []);

  // Sync state if selected area changes
  useEffect(() => {
    const handleAreaChange = () => {
      loadPendingReadings();
    };
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, []);

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

  const loadPendingReadings = async () => {
    const selectedAreaCode = getSelectedAreaCode();

    // No selection means every permitted area; getAllPendingReadings is already scoped
    // server side by the sec_info region/province/area codes.
    if (!selectedAreaCode) {
      setLoading(true);
      try {
        const response = await getAllPendingReadings();
        const customerList = response.customers || [];
        setCustomers(customerList);
        setAreaInfo({
          area_code: null,
          area_name: getAreaScopeLabel(),
          active_bill_cycle: null,
          pending_count: customerList.length,
        });
      } catch (err) {
        console.error("Error fetching pending readings:", err);
        toast.error("Failed to fetch pending readings from server.");
        setCustomers([]);
        setAreaInfo({
          area_code: null,
          area_name: getAreaScopeLabel(),
          active_bill_cycle: null,
          pending_count: 0,
        });
      } finally {
        setLoading(false);
      }
      return;
    }

    setLoading(true);
    try {      
      const response = await getPendingReadingsByAreaCd(selectedAreaCode);
      let customerList = response.customers || [];
      setCustomers(customerList);
      setAreaInfo({
        area_code: response.area_code,
        area_name: response.area_name,
        active_bill_cycle: response.active_bill_cycle,
        pending_count: customerList.length
      });
      console.log("Successfully fetched pending readings from backend:", response);
    } catch (err) {
      console.error("Error fetching pending readings from backend:", err);
      toast.error("Failed to fetch pending readings from server.");
      setCustomers([]);
      setAreaInfo({
        area_code: selectedAreaCode,
        area_name: null,
        active_bill_cycle: null,
        pending_count: 0
      });
    } finally {
      setLoading(false);
    }
  };

  const handleInsertReading = (customer) => {
    const readingValue = prompt(`Enter meter reading for customer: ${customer.name} (Folio: ${customer.folio_no ?? "N/A"})`);
    
    if (readingValue && readingValue.trim() !== "") {
      toast.success(`Reading ${readingValue} recorded for Folio ${customer.folio_no ?? "N/A"}`);
      loadPendingReadings();
    } else if (readingValue !== null) {
      toast.warning("Please enter a valid meter reading value.");
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

  const handlePageChange = (page) => {
    setCurrentPage(page);
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
      customer.name?.toLowerCase().includes(searchLower) ||
      customer.area_cd?.toLowerCase().includes(searchLower) ||
      customer.bill_cycle?.toString().includes(searchLower) ||
      customer.folio_no?.toString().toLowerCase().includes(searchLower) ||
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
    { label: "Pending Readings", href: null }
  ];

  return (
    <div className="flex flex-col gap-ds-6">
      <UnsavedChangesModal
        isOpen={showUnsavedModal}
        onLeave={handleLeavePage}
        onStay={handleStayOnPage}
      />
      
      <div>
        <PageHeader
          title="Pending Readings"
          subtitle="Manage and insert meter readings for pending customer profiles"
          stats={[
            { label: "Area", value: loading && !areaInfo.area_name ? "..." : areaInfo.area_name || areaInfo.area_code || "N/A", detail: `Code: ${areaInfo.area_code || "..."}` },
            { label: "Bill Cycle", value: loading && !areaInfo.active_bill_cycle ? "..." : areaInfo.active_bill_cycle || "N/A", detail: "Active cycle" },
            { label: "Pending Readings", value: loading ? "..." : areaInfo.pending_count, detail: "Needs entry" },
          ]}
        />
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
          <PendReadingsTable
            customers={currentRecords}
            loading={loading}
            searchTerm={searchTerm}
            onSearch={handleSearch}
            recordsPerPage={recordsPerPage}
            onRecordsPerPageChange={handleRecordsPerPageChange}
            currentPage={currentPage}
            totalPages={totalPages}
            totalRecords={totalRecords}
            onPageChange={handlePageChange}
            onInsertReading={handleInsertReading}
            areaInfo={areaInfo}
          />
        </div>
      </div>
    </div>
  );
};

export default PendCustomersView;