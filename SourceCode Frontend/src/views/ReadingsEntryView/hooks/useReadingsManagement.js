// src/views/ReadingsEntryView/hooks/useReadingsManagement.js
import { useState, useEffect, useRef } from "react";
import { toast } from "react-toastify";
import { useLocation, useHistory } from "react-router-dom";
import {
  updatePackChanges,
  hasValidPackChanges,
  resetPackChangesData,
} from "../../../services/packChangesService";
import {
  getMeterReadingInfo,
  transformMeterReadingToFrontend,
  prepareMeterReadingEditData,
  editMeterReadings,
} from "../../../services/meterReadingInfoService";
import {
  getPendingReadingForCustomer,
  transformPendingReadingToFrontend,
  calculateNumberOfDays,
  formatBillMonth,
} from "../../../services/pendingReadingsService";
import { getTempReadingsByAccNbr } from "../../../services/tempReadingsService";
import { getSelectedAreaCode, getBillCycleForArea } from "../../../services/AreaAndBillService";
import { getCurrentOpenBillCycle } from "../../../services/billCycleEndingService";
import {
  calculateMonthlyCharges,
  prepareMonthlyChargeRequest,
  mapApiResponseToFormFields,
} from "../../../services/monthlyChargeService";
import { getErrorDetailsWithMeterReadings } from "../../../services/errorStatisticsService";
import { updateMeterReadings } from "../../../services/meterReadingUpdateService";
import { 
    insertNewMeterReadings, 
    prepareInsertData, 
    validateSaveData 
} from "../../../services/insertNewReadingsService";
import { submitInvoice, saveDraftInvoice } from "../../../services/invoiceService";
import { getDeveloperBySearch } from "../../../services/developerRegistrationService";

export const useReadingsManagement = () => {
  const location = useLocation();
  const history = useHistory();
  const readingDateInputRef = useRef(null);

  const [formData, setFormData] = useState({
    account_number: "",
    folio_no: "",
    developer_name: "",
    facility_name: "",
    tariff: "",
    current_billcycle_number: "",
    global_billcycle_number: "",
    current_billcycle_date: "",
    bill_month: "",
    area_code_number: "",
    area_code_name: "",
    reader_code: "",
    daily_pack: "",
    walk_order: "",
    new_reader_code: "",
    new_daily_pack: "",
    new_walk_order: "",
    installation_id: "",
    customer_category: "",
    reading_date: new Date().toLocaleDateString("en-GB"),
    previous_reading_date: "",
    no_of_days: "",
    meter_sequence: "",
    b_f_balance: "",
    kwh_offp_meternum: "",
    kwh_day_meternum: "",
    kwh_peak_meternum: "",
    kva_meternum: "",
    kvah_meternum: "",
    kvah_presentread: "",
    kvah_previousread: "",
    kvah_units: "",
    kwh_offp_presentread: "",
    kwh_day_presentread: "",
    kwh_peak_presentread: "",
    kva_presentread: "",
    kwh_offp_previousread: "",
    kwh_day_previousread: "",
    kwh_peak_previousread: "",
    kva_previousread: "",
    kwh_offp_units: "",
    kwh_day_units: "",
    kwh_peak_units: "",
    kva_units: "",
    kwh_offp_assessedcode: "",
    kwh_day_assessedcode: "",
    kwh_peak_assessedcode: "",
    kva_assessedcode: "",
    kvah_assessedcode: "",
    kwh_offp_multiplyby: "",
    kwh_day_multiplyby: "",
    kwh_peak_multiplyby: "",
    kva_multiplyby: "",
    kvah_multiplyby: "",
    kwh_offp_rate: "",
    kwh_day_rate: "",
    kwh_peak_rate: "",
    kva_rate: "",
    kvah_rate: "",
    kwh_offp_amount: "",
    kwh_day_amount: "",
    kwh_peak_amount: "",
    kva_amount: "",
    kvah_amount: "",
    fixed_charge: "",
    monthly_charge: "",
    vat: "",
    tot_amount: "",
    has_reading: false,
    reading_status: "PENDING",
    source: "pending", // 'pending' or 'tempReadings'
  });

  const [showPackChanges, setShowPackChanges] = useState(false);
  const [accountLoaded, setAccountLoaded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showCalendar, setShowCalendar] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(null);
  const [showLoadButton, setShowLoadButton] = useState(true);
  const [originalData, setOriginalData] = useState({});
  const [editedFields, setEditedFields] = useState({});
  const [isEditing, setIsEditing] = useState(false);
  const [saveUpdatesActive, setSaveUpdatesActive] = useState(false);
  const [saveReadingsActive, setSaveReadingsActive] = useState(false);
  const [resetActive, setResetActive] = useState(false);
  const [customerMeterTypes, setCustomerMeterTypes] = useState([]);
  const [calculateButtonEnabled, setCalculateButtonEnabled] = useState(false);
  const [calculating, setCalculating] = useState(false);
  const [calculationSuccessful, setCalculationSuccessful] = useState(false);

  const [packChangesData, setPackChangesData] = useState({
    new_reader_code: "",
    new_daily_pack: "",
    new_walk_order: "",
  });
  const [packChangesEdited, setPackChangesEdited] = useState(false);
  const [savePackChangesActive, setSavePackChangesActive] = useState(false);

  // New states for breadcrumb and unsaved changes
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [showUnsavedModal, setShowUnsavedModal] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);

  // States for Resolve Invoice flow
  const [isResolveInvoice, setIsResolveInvoice] = useState(false);
  const [showResolveConfirmationModal, setShowResolveConfirmationModal] = useState(false);
  const [isResolveActionLoading, setIsResolveActionLoading] = useState(false);

  // Check if this is a received reading (from temp readings)
  const isReceivedReading = formData.source === "tempReadings" || formData.has_reading;

  const getHeaderConfig = () => {
    if (!accountLoaded) {
      return {
        bgColor: "bg-navy-50",
        showRefresh: false,
        showStatus: false,
        title: "Monthly Readings Entry",
      };
    }

    // Different header colors for received vs pending readings
    if (isReceivedReading) {
      return {
        bgColor: "bg-success-50",
        showRefresh: true,
        showStatus: true,
        title: "Edit Received Readings",
      };
    }

    switch (formData.reading_status) {
      case "RECEIVED":
        return {
          bgColor: "bg-success-50",
          showRefresh: true,
          showStatus: true,
          title: "Edit Received Readings",
        };
      case "PENDING":
        return {
          bgColor: "bg-warning-50",
          showRefresh: true,
          showStatus: true,
          title: "Insert New Readings",
        };
      default:
        return {
          bgColor: "bg-navy-50",
          showRefresh: false,
          showStatus: false,
          title: "Monthly Readings Entry",
        };
    }
  };

  const headerConfig = getHeaderConfig();

  useEffect(() => {
    let isMounted = true;
    getCurrentOpenBillCycle()
      .then((cycle) => {
        if (isMounted) {
          setFormData((prev) => ({
            ...prev,
            global_billcycle_number: cycle.bill_cycle != null ? String(cycle.bill_cycle) : "",
          }));
        }
      })
      .catch((error) => {
        console.error("Unable to load global bill cycle:", error);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // Focus on Reading Date field when account is loaded (for pending readings)
  useEffect(() => {
    if (
      accountLoaded &&
      formData.reading_status === "PENDING" &&
      readingDateInputRef.current
    ) {
      setTimeout(() => {
        readingDateInputRef.current.focus();
      }, 100);
    }
  }, [accountLoaded, formData.reading_status]);

  // Set default assessed code values for new readings
  useEffect(() => {
    if (accountLoaded && !isReceivedReading) {
      // Set default assessed code to "Not-A" (single white space) for new readings
      const meterTypes = ["kwh_offp", "kwh_day", "kwh_peak", "kva", "kvah"];
      let hasDefaultsSet = false;

      meterTypes.forEach((prefix) => {
        if (
          formData[`${prefix}_assessedcode`] === undefined ||
          formData[`${prefix}_assessedcode`] === ""
        ) {
          hasDefaultsSet = true;
          setFormData((prev) => ({
            ...prev,
            [`${prefix}_assessedcode`]: " ", // Single white space for Not-A
          }));
        }
      });

      // Update original data if defaults were set
      if (hasDefaultsSet) {
        setOriginalData((prev) => ({
          ...prev,
          kwh_offp_assessedcode: " ",
          kwh_day_assessedcode: " ",
          kwh_peak_assessedcode: " ",
          kva_assessedcode: " ",
          kvah_assessedcode: " ",
        }));
      }
    }
  }, [accountLoaded, isReceivedReading]);

  useEffect(() => {
    const hasChanges = hasValidPackChanges(packChangesData);
    setSavePackChangesActive(hasChanges);
    setPackChangesEdited(hasChanges);
  }, [packChangesData]);

  useEffect(() => {
    if (!showPackChanges && packChangesEdited) {
      toast.warning(
        "You have unsaved pack changes. Please save them before closing the section."
      );
    }
  }, [showPackChanges, packChangesEdited]);

// Inside the hook, update the useEffect that handles URL parameters:
// In the useEffect that handles URL parameters:
useEffect(() => {
  const params = new URLSearchParams(location.search);
  const accountNumber = params.get("account");
  const areaCode = params.get("area");
  const billCycle = params.get("billCycle");
  const source = params.get("source");
  const errorCode = params.get("errorCode"); // Get error code from URL
  const resolveInvoice = params.get("resolveInvoice") === "true";

  console.log("URL Parameters:", {
    accountNumber,
    areaCode,
    billCycle,
    source,
    errorCode,
    resolveInvoice
  });

  setIsResolveInvoice(resolveInvoice);

  if (accountNumber && areaCode) {
    setFormData((prev) => ({
      ...prev,
      account_number: accountNumber,
      area_code_number: areaCode,
      source: source || "pending",
    }));
    setShowLoadButton(false);

    // Load data based on source
    if (source === "tempReadings") {
      if (errorCode) {
        // NEW: Load error-specific data when coming from error page
        console.log("Loading error-specific data for error code:", errorCode);
        loadErrorReadingData(accountNumber, areaCode, billCycle, parseInt(errorCode));
      } else {
        // Load regular received readings data
        console.log("Loading regular received readings data");
        loadReceivedReadingData(accountNumber, areaCode, billCycle);
      }
    } else {
      // Load pending reading data for reading-not-received customers
      loadPendingReadingData(accountNumber, areaCode);
    }
  } else {
    // No account in the URL. The area is not needed here: the form loads by
    // Folio Number and the backend resolves the account's own area, so a user
    // viewing all areas can still work with any account they have access to.
    const selectedAreaCode = getSelectedAreaCode();
    if (selectedAreaCode) {
      const activeBillCycle = getBillCycleForArea(selectedAreaCode);
      setFormData((prev) => ({
        ...prev,
        area_code_number: selectedAreaCode,
        current_billcycle_number: activeBillCycle || "",
        current_billcycle_date: "",
        bill_month: activeBillCycle ? String(activeBillCycle) : "",
        area_code_name: `Area ${selectedAreaCode}`,
      }));
    }
  }
}, [location.search]);

// Add this new function to load error-specific data
// Update the loadErrorReadingData function in useReadingsManagement.js
const loadErrorReadingData = async (accountNumber, areaCode, billCycle, errorCode) => {
  if (!accountNumber) {
    toast.error("Folio number is required");
    return;
  }

  if (!areaCode) {
    const selectedAreaCode = getSelectedAreaCode();
    if (!selectedAreaCode) {
      toast.error("Area code is required. Please select an area first.");
      return;
    }
    areaCode = selectedAreaCode;
  }

  setLoading(true);

  try {
    console.log("Loading error-specific readings for account:", accountNumber, "error:", errorCode);

    // We need to call the error statistics API
    const sessionId = sessionStorage.getItem("session_id");
    const userId = sessionStorage.getItem("user_id");
    const baseUrl = process.env.REACT_APP_API_BASE_URL;

    const response = await fetch(
      `${baseUrl}/api/v1/error-statistics/error-details-with-readings`,
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
          bill_cycle: billCycle,
          error_code: errorCode,
        }),
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const result = await response.json();

    console.log("Error details API response:", result);

    if (result.success && result.accounts_with_error) {
      // Find the specific account in the response
      const accountData = result.accounts_with_error.find(
        (account) => account.account_number === accountNumber
      );

      if (accountData) {
        console.log("Found account data with errors:", accountData);
        
        // First, get the regular meter reading info to have complete data
        const regularResponse = await getMeterReadingInfo(
          accountNumber,
          areaCode,
          billCycle
        );

        if (regularResponse.success && regularResponse.meter_reading_info) {
          const transformedData = transformMeterReadingToFrontend(regularResponse);
          
          if (transformedData) {
            // Now enhance with error information
            const enhancedData = enhanceWithErrorData(transformedData, accountData, errorCode);
            
            setOriginalData(enhancedData);
            setFormData((prev) => ({
              ...prev,
              ...enhancedData,
              source: "tempReadings",
              error_code: errorCode,
            }));

            // Extract meter types
            if (regularResponse.meter_reading_info.meter_types) {
              const meterTypes = regularResponse.meter_reading_info.meter_types.map((meter) => ({
                meter_type: meter.meter_type?.trim(),
                meter_number: meter.meter_number?.trim(),
              }));
              setCustomerMeterTypes(meterTypes);
            }

            setAccountLoaded(true);
            setIsEditing(false);
            setEditedFields({});

            toast.success(`Error reading data loaded successfully`);
          }
        }
      } else {
        toast.error(`No error readings found for account ${accountNumber}`);
        // Fall back to regular received readings
        loadReceivedReadingData(accountNumber, areaCode, billCycle);
      }
    } else {
      // Fall back to regular received readings
      toast.warning(`No error-specific data found. Loading regular readings...`);
      loadReceivedReadingData(accountNumber, areaCode, billCycle);
    }
  } catch (error) {
    console.error("Error loading error reading data:", error);
    toast.error(`Failed to load error reading data: ${error.message}. Loading regular readings...`);
    loadReceivedReadingData(accountNumber, areaCode, billCycle);
  } finally {
    setLoading(false);
  }
};

// Add this helper function to enhance data with error information
const enhanceWithErrorData = (regularData, errorAccountData, errorCode) => {
  const enhancedData = { ...regularData };
  
  // Create error_fields array
  enhancedData.error_fields = [];
  enhancedData.error_code = errorCode;
  
  if (errorAccountData.all_meter_readings) {
    errorAccountData.all_meter_readings.forEach(meterReading => {
      if (meterReading.has_error) {
        // Map meter type to prefix
        const meterTypeToPrefix = {
          'KWO': 'kwh_offp',
          'KWD': 'kwh_day', 
          'KWP': 'kwh_peak',
          'KVA': 'kva',
          'KVAH': 'kvah'
        };
        
        const prefix = meterTypeToPrefix[meterReading.meter_type];
        if (prefix) {
          enhancedData.error_fields.push(`${prefix}_presentread`);
          enhancedData.error_fields.push(`${prefix}_previousread`);
          enhancedData.error_fields.push(`${prefix}_units`);
        }
      }
    });
  }
  
  console.log("Enhanced data with error fields:", enhancedData.error_fields);
  return enhancedData;
};

// Add this new transformation function
const transformErrorReadingToFrontend = (accountData, response, errorCode) => {
  if (!accountData) {
    return null;
  }

  // Start with basic form data structure
  const frontendData = {
    account_number: accountData.account_number,
    tariff: accountData.tariff || "",
    current_billcycle_number: response.active_bill_cycle || "",
    current_billcycle_date: "", // Will be formatted if available
    bill_month: formatBillMonth(
      response.active_bill_cycle,
      response.bill_month,
      response.bill_year,
      response.bill_cycle_date
    ),
    area_code_number: accountData.area_code || "",
    area_code_name: response.area_name || "",
    reader_code: accountData.reader_code || "",
    daily_pack: accountData.daily_pack || "",
    walk_order: accountData.walk_order || "",
    installation_id: accountData.installation_id || "",
    customer_category: accountData.customer_category || "",
    reading_date: "", // Will be set from first reading
    previous_reading_date: "", // Will be set from readings
    no_of_days: "",
    meter_sequence: "",
    b_f_balance: "",
    fixed_charge: "",
    monthly_charge: "",
    vat: "",
    tot_amount: "",
    has_reading: true,
    reading_status: "RECEIVED",
    source: "tempReadings",
    error_code: errorCode,
    error_fields: [], // Will track which fields have errors
  };

  // Initialize all meter fields to empty
  const meterTypeDefaults = {
    kwh_offp: { type: 'KWO', prefix: 'kwh_offp' },
    kwh_day: { type: 'KWD', prefix: 'kwh_day' },
    kwh_peak: { type: 'KWP', prefix: 'kwh_peak' },
    kva: { type: 'KVA', prefix: 'kva' },
    kvah: { type: 'KVAH', prefix: 'kvah' }
  };

  Object.values(meterTypeDefaults).forEach(({ prefix }) => {
    frontendData[`${prefix}_meternum`] = "";
    frontendData[`${prefix}_presentread`] = "";
    frontendData[`${prefix}_previousread`] = "";
    frontendData[`${prefix}_units`] = "";
    frontendData[`${prefix}_assessedcode`] = "";
    frontendData[`${prefix}_multiplyby`] = "";
    frontendData[`${prefix}_rate`] = "";
    frontendData[`${prefix}_amount`] = "";
  });

  // Populate meter type data from accountData.all_meter_readings
  if (accountData.all_meter_readings && Array.isArray(accountData.all_meter_readings)) {
    // Track reading date from first reading
    if (accountData.all_meter_readings.length > 0) {
      const firstReading = accountData.all_meter_readings[0];
      if (firstReading.reading_date) {
        try {
          const date = new Date(firstReading.reading_date);
          frontendData.reading_date = date.toLocaleDateString('en-GB');
        } catch (error) {
          console.error("Error parsing reading date:", error);
        }
      }
      if (firstReading.previous_reading_date) {
        try {
          const date = new Date(firstReading.previous_reading_date);
          frontendData.previous_reading_date = date.toLocaleDateString('en-GB');
        } catch (error) {
          console.error("Error parsing previous reading date:", error);
        }
      }
    }

    // Process each meter reading
    accountData.all_meter_readings.forEach(meter => {
      const meterType = meter.meter_type?.trim();
      const meterConfig = Object.values(meterTypeDefaults).find(config => config.type === meterType);
      
      if (meterConfig) {
        const prefix = meterConfig.prefix;
        
        // Set meter data
        frontendData[`${prefix}_meternum`] = meter.meter_number || "";
        frontendData[`${prefix}_presentread`] = meter.present_reading?.toString() || "";
        frontendData[`${prefix}_previousread`] = meter.previous_reading?.toString() || "";
        frontendData[`${prefix}_units`] = meter.units?.toString() || "";
        frontendData[`${prefix}_assessedcode`] = meter.assessed_code || "";
        frontendData[`${prefix}_multiplyby`] = meter.multiplied_by?.toString() || "";
        frontendData[`${prefix}_rate`] = meter.rate?.toString() || "";
        frontendData[`${prefix}_amount`] = meter.amount?.toString() || "";
        
        // Track error fields for highlighting
        if (meter.has_error) {
          frontendData.error_fields.push(`${prefix}_presentread`);
          // Also add other related fields if needed
          frontendData.error_fields.push(`${prefix}_previousread`);
          frontendData.error_fields.push(`${prefix}_units`);
        }
      }
    });
  }

  return frontendData;
};

  useEffect(() => {
    if (formData.reading_date) {
      try {
        const parts = formData.reading_date.split("/");
        if (parts.length === 3) {
          const date = new Date(parts[2], parts[1] - 1, parts[0]);
          if (!isNaN(date.getTime())) {
            setSelectedDate(date);
            setCurrentMonth(date);
            console.log("Set calendar to existing reading date:", date);
          }
        }
      } catch (error) {
        console.error("Error parsing reading date:", error);
        // For Insert New Readings, don't set selectedDate to today by default
        if (isReceivedReading) {
          const today = new Date();
          setSelectedDate(today);
          setCurrentMonth(today);
        }
      }
    } else {
      // For Insert New Readings with no reading_date, don't set selectedDate initially
      if (isReceivedReading && formData.reading_date) {
        // Only set for received readings that have a reading_date
        try {
          const parts = formData.reading_date.split("/");
          if (parts.length === 3) {
            const date = new Date(parts[2], parts[1] - 1, parts[0]);
            if (!isNaN(date.getTime())) {
              setSelectedDate(date);
              setCurrentMonth(date);
            }
          }
        } catch (error) {
          console.error("Error parsing reading date:", error);
        }
      }
    }
  }, [formData.reading_date, isReceivedReading]);

  useEffect(() => {
    if (accountLoaded && Object.keys(originalData).length > 0) {
      const hasChanges = Object.keys(editedFields).length > 0;
      setResetActive(hasChanges);

      // For received readings (editing mode)
      if (isReceivedReading || formData.reading_status === "RECEIVED") {
        setSaveUpdatesActive(hasChanges);
        setSaveReadingsActive(false);
      }
      // For pending readings (insert new readings mode)
      else if (formData.reading_status === "PENDING") {
        setSaveUpdatesActive(false);
        const hasRequiredFields =
          formData.reading_date && formData.reading_date.trim() !== "";
        setSaveReadingsActive(hasChanges && hasRequiredFields);
      }
    } else {
      setSaveUpdatesActive(false);
      setSaveReadingsActive(false);
      setResetActive(false);
    }
  }, [
    editedFields,
    accountLoaded,
    originalData,
    formData,
    isReceivedReading
  ]);

useEffect(() => {
  if (!accountLoaded) {
    setCalculationSuccessful(false);
  }
}, [accountLoaded]);

  // Add this useEffect to handle calculate button enable/disable logic
  useEffect(() => {
    if (!accountLoaded) {
      setCalculateButtonEnabled(false);
      return;
    }

    // For Insert New Readings (PENDING)
    if (!isReceivedReading) {
      // Check if reading date is selected and at least one present reading has value
      const hasReadingDate =
        formData.reading_date && formData.reading_date.trim() !== "";
      const hasAtLeastOnePresentReading = [
        formData.kwh_offp_presentread,
        formData.kwh_day_presentread,
        formData.kwh_peak_presentread,
        formData.kwh_tot_presentread,
      ].some(
        (reading) => reading && reading.trim() !== "" && parseFloat(reading) >= 0
      );

      setCalculateButtonEnabled(hasReadingDate && hasAtLeastOnePresentReading);
    }
    // For Edit Received Readings (RECEIVED)
    else if (isReceivedReading) {
      // Check if reading date is edited OR at least one present reading is edited
      const isReadingDateEdited = editedFields.reading_date !== undefined;
      const isAnyPresentReadingEdited = [
        "kwh_offp_presentread",
        "kwh_day_presentread",
        "kwh_peak_presentread",
        "kwh_tot_presentread",
      ].some((field) => editedFields[field] !== undefined);

      setCalculateButtonEnabled(
        isReadingDateEdited || isAnyPresentReadingEdited
      );
    } else {
      setCalculateButtonEnabled(false);
    }
  }, [
    formData.reading_date,
    formData.kwh_offp_presentread,
    formData.kwh_day_presentread,
    formData.kwh_peak_presentread,
    formData.kwh_tot_presentread,
    editedFields,
    accountLoaded,
    isReceivedReading,
  ]);

  // Function to check for unsaved changes
  const checkUnsavedChanges = () => {
    // For received readings (editing mode), check if there are edited fields
    if (isReceivedReading || formData.reading_status === "RECEIVED") {
      return Object.keys(editedFields).length > 0 || packChangesEdited;
    }
    // For pending readings (insert new readings mode), check if there are any changes
    return saveReadingsActive || packChangesEdited;
  };

  // Update hasUnsavedChanges when relevant state changes
  useEffect(() => {
    setHasUnsavedChanges(checkUnsavedChanges());
  }, [
    editedFields,
    packChangesEdited,
    saveReadingsActive,
    isReceivedReading,
    formData.reading_status,
  ]);

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
  }, [
    hasUnsavedChanges,
    history,
    editedFields,
    packChangesEdited,
    saveReadingsActive,
  ]);

  // Add this helper function to calculate units with multiplyBy factor
const calculateUnitsWithMultiplyBy = (presentReading, previousReading, multiplyBy) => {
    const present = parseFloat(presentReading) || 0;
    const previous = parseFloat(previousReading) || 0;
    const multiply = parseFloat(multiplyBy) || 1.000;
    
    // Calculate units: (Present - Previous) × MultiplyBy
    let units = (present - previous) * multiply;
    
    // Never allow negative units
    if (units < 0 || isNaN(units)) {
        units = 0;
    }
    
    // Round to match API format (3 decimal places)
    return Math.round(units * 1000) / 1000;
};

  // No manual calculations required for rates and amounts

  // Alternative approach: Modify loadReceivedReadingData to check for errors
const loadReceivedReadingData = async (accountNumber, areaCode, billCycle, errorCodeFromUrl = null) => {
  if (!accountNumber) {
    toast.error("Folio number is required");
    return;
  }

  if (!areaCode) {
    const selectedAreaCode = getSelectedAreaCode();
    if (!selectedAreaCode) {
      toast.error("Area code is required. Please select an area first.");
      return;
    }
    areaCode = selectedAreaCode;
  }

  setLoading(true);

  try {
    console.log(
      "Loading received readings for account:",
      accountNumber,
      "area:",
      areaCode,
      "errorCode param:",
      errorCodeFromUrl
    );

    // First, try the regular meter-reading-info API
    const response = await getMeterReadingInfo(
      accountNumber,
      areaCode,
      billCycle
    );

    console.log("Received readings API response:", response);

    if (response.success && response.meter_reading_info) {
      const transformedData = transformMeterReadingToFrontend(response);

      if (transformedData) {
        // If we have an error code from URL, add error fields
        if (errorCodeFromUrl) {
          // We need to determine which readings have errors
          // For now, let's check if we can get error data from localStorage or sessionStorage
          // This is a temporary solution - in production, you'd call a proper API
          
          // Check if there are temp readings with errors
          try {
            const tempReadings = await getTempReadingsByAccNbr(accountNumber, false);
            if (tempReadings.success && tempReadings.readings) {
              const errorFields = [];
              
              tempReadings.readings.forEach(reading => {
                if (reading.err_stat && reading.err_stat === errorCodeFromUrl) {
                  // Map meter type to prefix
                  const meterTypeToPrefix = {
                    'KWO': 'kwh_offp',
                    'KWD': 'kwh_day', 
                    'KWP': 'kwh_peak',
                    'KVA': 'kva',
                    'KVAH': 'kvah'
                  };
                  
                  const prefix = meterTypeToPrefix[reading.mtr_type];
                  if (prefix) {
                    errorFields.push(`${prefix}_presentread`);
                  }
                }
              });
              
              if (errorFields.length > 0) {
                transformedData.error_fields = errorFields;
                transformedData.error_code = errorCodeFromUrl;
                console.log("Added error fields:", errorFields);
              }
            }
          } catch (err) {
            console.log("Could not fetch error data:", err);
          }
        }

        setOriginalData(transformedData);
        setFormData((prev) => ({
          ...prev,
          ...transformedData,
          source: "tempReadings",
        }));

        // Extract meter types from the response properly
        if (
          response.meter_reading_info.meter_types &&
          Array.isArray(response.meter_reading_info.meter_types)
        ) {
          const meterTypes = response.meter_reading_info.meter_types.map(
            (meter) => ({
              meter_type: meter.meter_type?.trim(),
              meter_number: meter.meter_number?.trim(),
            })
          );
          setCustomerMeterTypes(meterTypes);
        } else {
          setCustomerMeterTypes([]);
        }

        setAccountLoaded(true);
        setIsEditing(false);
        setEditedFields({});

        // CRITICAL: Set calendar to the existing reading date after data is loaded
        if (transformedData.reading_date) {
          setTimeout(() => {
            try {
              const parts = transformedData.reading_date.split("/");
              if (parts.length === 3) {
                const date = new Date(parts[2], parts[1] - 1, parts[0]);
                if (!isNaN(date.getTime())) {
                  setSelectedDate(date);
                  setCurrentMonth(date);
                  console.log(
                    "Calendar focused on existing reading date:",
                    date
                  );
                }
              }
            } catch (error) {
              console.error("Error setting calendar date:", error);
            }
          }, 100);
        }

        // toast.success(`Received reading data loaded successfully`);
      } else {
        // toast.error("Failed to transform received reading data");
      }
    } else {
      // If no readings found via regular API, check if we have error data
      const errorMessage =
        response?.message || "No received readings found for this account";
      console.log(
        "No readings found via regular API, checking for error data:",
        errorMessage
      );

      toast.error(`No received readings found: ${errorMessage}`);
      setAccountLoaded(true);
      setCustomerMeterTypes([]);
    }
  } catch (error) {
    console.error("Error loading received reading data:", error);
    toast.error(`Failed to load received reading data: ${error.message}`);
    setAccountLoaded(true);
    setCustomerMeterTypes([]);
  } finally {
    setLoading(false);
  }
};

  // Existing function to load pending reading data
  const loadPendingReadingData = async (accountNumber, areaCode, folioNo) => {
    if (!accountNumber && !folioNo) {
      // Either identifier resolves the account; the backend derives the area from it.
      toast.error("An account number or Folio Number is required");
      return;
    }

    if (!areaCode) {
      // Prefer the header-bar selection, but it is optional: a Folio Number is
      // enough because the backend resolves the account's own area.
      const selectedAreaCode = getSelectedAreaCode();
      if (selectedAreaCode) {
        areaCode = selectedAreaCode;
      }
    }

    if (!areaCode && !folioNo) {
      toast.error("Enter a Folio Number, or select an area in the header.");
      return;
    }

    setLoading(true);

    try {
      const response = await getPendingReadingForCustomer(
        accountNumber,
        areaCode,
        folioNo
      );

      if (response.success && response.pending_reading) {
        const transformedData = transformPendingReadingToFrontend(response);

        if (transformedData) {
          setOriginalData(transformedData.formData);
          setFormData((prev) => ({
            ...prev,
            ...transformedData.formData,
            source: "pending",
          }));
          setCustomerMeterTypes(transformedData.meterTypes || []);
          setAccountLoaded(true);
          setIsEditing(false);
          setEditedFields({});

          toast.success(`Pending reading data loaded successfully`);
        } else {
          toast.error("Failed to transform pending reading data");
        }
      } else {
        toast.error(response.message || "Failed to load pending reading data");
      }
    } catch (error) {
      console.error("Error loading pending reading data:", error);
      toast.error(`Failed to load pending reading data: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Load account data by Folio Number
  const loadAccountData = async () => {
    const folioNumber = (formData.folio_no || "").toString().trim();

    if (!folioNumber) {
      toast.error("Please enter a Folio Number");
      return;
    }

    // Look up the developer by folio number (folio number is unique in ncre_developers)
    setLoading(true);
    let dev;
    try {
      dev = await getDeveloperBySearch("folio_no", folioNumber);
    } catch (err) {
      console.error("Error resolving Folio Number:", err);
      toast.error(`Failed to find developer by Folio Number: ${err.message}`);
      setLoading(false);
      return;
    }

    const resolvedAccNbr = dev && (dev.accountNumber || dev.accNbr || dev.acc_nbr);
    if (!resolvedAccNbr) {
      toast.error(`Could not find developer with Folio Number: ${folioNumber}`);
      setLoading(false);
      return;
    }

    const resolvedFolio = dev.folioNumber || dev.folioNo || dev.folio_no || folioNumber;
    setFormData((prev) => ({
      ...prev,
      account_number: resolvedAccNbr,
      folio_no: resolvedFolio,
    }));

    // Load pending reading details; backend resolves area from the developer record
    await loadPendingReadingData(resolvedAccNbr, null, resolvedFolio);
  };

  // Helper function to validate present reading input
  const validatePresentReading = (value, previousReading) => {
    if (!value || value.trim() === "") {
      return { isValid: true, value: "", units: 0 };
    }

    const present = parseFloat(value);
    if (isNaN(present)) {
      return { isValid: false, value: "", units: 0 };
    }

    const prev = parseFloat(previousReading) || 0;
    let units = present - prev;

    // Never allow negative units
    if (units < 0) {
      units = 0;
    }

    return { isValid: true, value: value, units: units };
  };

  // New function to handle present reading changes and update units
const handlePresentReadingChange = (fieldName, value) => {
    const prefix = fieldName.replace("_presentread", "");
    const previousReading = formData[`${prefix}_previousread`] || "0";
    const multiplyBy = formData[`${prefix}_multiplyby`] || "1.000";
    
    // Calculate units using the helper function
    const units = calculateUnitsWithMultiplyBy(value, previousReading, multiplyBy);
    
    // Update form data
    setFormData((prev) => ({
        ...prev,
        [fieldName]: value,
        [`${prefix}_units`]: units.toString(),
    }));
    
    // Update edited fields
    if (accountLoaded) {
        const newEditedFields = { ...editedFields };
        
        if (originalData[fieldName] !== value) {
            newEditedFields[fieldName] = value;
        } else {
            delete newEditedFields[fieldName];
        }
        
        if (originalData[`${prefix}_units`] !== units.toString()) {
            newEditedFields[`${prefix}_units`] = units.toString();
        } else {
            delete newEditedFields[`${prefix}_units`];
        }
        
        setEditedFields(newEditedFields);
    }
};

  const handleInputChange = (e) => {
    const { name, value } = e.target;

    // For received readings, only allow editing of specific fields
    if (isReceivedReading) {
      const editableFields = [
        "reading_date",
        "kwh_offp_presentread",
        "kwh_day_presentread",
        "kwh_peak_presentread",
        "kva_presentread",
        "kvah_presentread",
        "bypass_ru",
      ];

      if (!editableFields.includes(name)) {
        toast.warning("This field cannot be edited for received readings");
        return;
      }
    }

    // Handle present reading changes - automatically update units
    if (name.endsWith("_presentread")) {
      handlePresentReadingChange(name, value);
      return;
    }

if (name === "kvah_presentread") {
    const presentReading = parseFloat(value) || 0;
    const multiplyBy = parseFloat(formData.kvah_multiplyby) || 1.000;

    // For KVAH, units = present reading × multiplyBy (no subtraction)
    let units = presentReading * multiplyBy;
    
    if (units < 0 || isNaN(units)) {
        units = 0;
    }
    
    // Round to appropriate decimal places
    units = Math.round(units * 1000) / 1000;

    setFormData((prev) => ({
        ...prev,
        kvah_presentread: value,
        kvah_units: units.toString(),
    }));

    if (accountLoaded) {
        const newEditedFields = { ...editedFields };

        if (originalData.kvah_presentread !== value) {
            newEditedFields.kvah_presentread = value;
        } else {
            delete newEditedFields.kvah_presentread;
        }

        if (originalData.kvah_units !== units.toString()) {
            newEditedFields.kvah_units = units.toString();
        } else {
            delete newEditedFields.kvah_units;
        }

        setEditedFields(newEditedFields);
    }
    return;
}

    // For all other fields
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (
      accountLoaded &&
      originalData[name] !== undefined &&
      originalData[name] !== value
    ) {
      setEditedFields((prev) => ({
        ...prev,
        [name]: value,
      }));
    } else if (accountLoaded && editedFields[name]) {
      const newEditedFields = { ...editedFields };
      delete newEditedFields[name];
      setEditedFields(newEditedFields);
    }
  };

  const handleFolioNumberChange = (e) => {
    const value = e.target.value.replace(/[^0-9]/g, "").slice(0, 6);
    setFormData((prev) => ({
      ...prev,
      folio_no: value,
    }));
  };

  const handleRefresh = () => {
    // area_code_number is filled in once the account loads, so by this point it
    // reflects the account's own area rather than the header-bar selection.
    if (formData.account_number && formData.area_code_number) {
      // Check if we should use pending reading data or regular meter reading data
      if (formData.reading_status === "PENDING" && !formData.has_reading) {
        loadPendingReadingData(
          formData.account_number,
          formData.area_code_number
        );
      } else if (isReceivedReading) {
        loadReceivedReadingData(
          formData.account_number,
          formData.area_code_number,
          formData.current_billcycle_number
        );
      } else {
        // For received readings that aren't from tempReadings source
        loadReceivedReadingData(
          formData.account_number,
          formData.area_code_number,
          formData.current_billcycle_number
        );
      }
    } else {
      toast.info("No account data to refresh");
    }

    setCalculationSuccessful(false);
  };

  const getDaysInMonth = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const days = [];

    for (let i = 0; i < startingDayOfWeek; i++) {
      days.push(null);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      days.push(new Date(year, month, day));
    }

    return days;
  };

  const formatDate = (date) => {
    return date.toLocaleDateString("en-GB");
  };

  // Update the date selection handler to calculate number of days
  const handleDateSelect = (date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (date > today) {
      toast.error("Reading date cannot be a future date");
      return;
    }

    setSelectedDate(date); // This will now properly set the selected date
    const formattedDate = formatDate(date);

    // Calculate number of days when reading date is selected
    const numberOfDays = calculateNumberOfDays(
      formattedDate,
      formData.previous_reading_date
    );

    setFormData((prev) => ({
      ...prev,
      reading_date: formattedDate,
      no_of_days: numberOfDays.toString(),
    }));

    // Ensure editedFields is properly updated for date selection too
    if (accountLoaded) {
      const isDateChanged = originalData.reading_date !== formattedDate;

      if (isDateChanged) {
        setEditedFields((prev) => ({
          ...prev,
          reading_date: formattedDate,
          no_of_days: numberOfDays.toString(),
        }));
      } else {
        const newEditedFields = { ...editedFields };
        delete newEditedFields.reading_date;
        delete newEditedFields.no_of_days;
        setEditedFields(newEditedFields);
      }
    }

    setShowCalendar(false);

    console.log(
      "Date selected - Date:",
      formattedDate,
      "Edited fields updated:",
      accountLoaded && originalData.reading_date !== formattedDate
    );
  };

  const handleTodayClick = () => {
    const today = new Date();
    setSelectedDate(today); // Set selected date when Today button is clicked
    setCurrentMonth(today);
    const formattedDate = formatDate(today);

    // Calculate number of days for today's date
    const numberOfDays = calculateNumberOfDays(
      formattedDate,
      formData.previous_reading_date
    );

    setFormData((prev) => ({
      ...prev,
      reading_date: formattedDate,
      no_of_days: numberOfDays.toString(),
    }));

    // CRITICAL FIX: Update editedFields when Today button is clicked
    if (accountLoaded) {
      // Check if the date is different from original data
      const isDateChanged = originalData.reading_date !== formattedDate;

      if (isDateChanged) {
        setEditedFields((prev) => ({
          ...prev,
          reading_date: formattedDate,
          no_of_days: numberOfDays.toString(),
        }));
      } else {
        // If date is same as original, remove from editedFields
        const newEditedFields = { ...editedFields };
        delete newEditedFields.reading_date;
        delete newEditedFields.no_of_days;
        setEditedFields(newEditedFields);
      }
    }

    setShowCalendar(false);

    console.log(
      "Today button clicked - Date:",
      formattedDate,
      "Edited fields updated:",
      accountLoaded && originalData.reading_date !== formattedDate
    );
  };

  const navigateMonth = (direction) => {
    setCurrentMonth((prev) => {
      const newMonth = new Date(prev);
      newMonth.setMonth(newMonth.getMonth() + direction);
      return newMonth;
    });
  };

  const handleResetEdits = () => {
  if (Object.keys(editedFields).length > 0 || packChangesEdited) {
    setFormData((prev) => ({
      ...prev,
      ...originalData,
    }));
    setEditedFields({});
    setPackChangesData(resetPackChangesData());
    setPackChangesEdited(false);
    setHasUnsavedChanges(false);
    setCalculationSuccessful(false); // Reset calculation flag
    toast.info("All edits have been reset to original values");
  }
};

 // FILE: src\views\ReadingsEntryView\hooks\useReadingsManagement.js
// Update the handleSaveUpdates function
const executeSaveUpdates = async () => {
  try {
    // Log debug information
    logUpdateDebugInfo(formData, editedFields, customerMeterTypes);
    
    // Prepare data for the update API
    const updateData = prepareMeterReadingUpdateData(formData, editedFields, customerMeterTypes);

    console.log("Sending update data to API:", JSON.stringify(updateData, null, 2));

    // Call the update API
    const response = await updateMeterReadings(updateData);

    if (response.success) {
      toast.success("Meter readings updated successfully!");
      
      // Reset unsaved changes state
      setHasUnsavedChanges(false);
      setEditedFields({});
      setSaveUpdatesActive(false);

      // Refresh the data after successful update
      if (isReceivedReading) {
        await loadReceivedReadingData(
          formData.account_number,
          formData.area_code_number,
          formData.current_billcycle_number
        );
      }
      return true;
    } else {
      toast.error(response.message || "Failed to update readings");
      return false;
    }
  } catch (error) {
    console.error("Error saving updates:", error);
    toast.error("Failed to save updates: " + error.message);
    return false;
  }
};

const handleSaveUpdates = async () => {
  if (!saveUpdatesActive) return;

  if (isResolveInvoice) {
    setShowResolveConfirmationModal(true);
    return;
  }

  await executeSaveUpdates();
};

const handleConfirmResolveSubmit = async () => {
  try {
    setIsResolveActionLoading(true);
    const saveSuccess = await executeSaveUpdates();
    if (!saveSuccess) return;

    await submitInvoice({
      accountNumber: formData.account_number,
      areaCode: formData.area_code_number,
      billCycle: formData.current_billcycle_number,
      bypassRu: formData.bypass_ru,
    });

    toast.success("Invoice resolved and submitted for review successfully!");
    setShowResolveConfirmationModal(false);
    history.push("/admin/invoices");
  } catch (error) {
    console.error("Error resolving and submitting invoice:", error);
    toast.error(error.message || "Failed to submit resolved invoice.");
  } finally {
    setIsResolveActionLoading(false);
  }
};

const handleConfirmResolveDraft = async () => {
  try {
    setIsResolveActionLoading(true);
    const saveSuccess = await executeSaveUpdates();
    if (!saveSuccess) return;

    await saveDraftInvoice({
      accountNumber: formData.account_number,
      areaCode: formData.area_code_number,
      billCycle: formData.current_billcycle_number,
      bypassRu: formData.bypass_ru,
    });

    toast.success("Invoice resolved and saved as draft successfully!");
    setShowResolveConfirmationModal(false);
    history.push("/admin/invoices");
  } catch (error) {
    console.error("Error resolving and saving draft invoice:", error);
    toast.error(error.message || "Failed to save draft invoice.");
  } finally {
    setIsResolveActionLoading(false);
  }
};

// FILE: src\views\ReadingsEntryView\hooks\useReadingsManagement.js
// Update the prepareMeterReadingUpdateData function
const prepareMeterReadingUpdateData = (formData, editedFields, customerMeterTypes) => {
  const sessionId = sessionStorage.getItem('session_id');
  const userId = sessionStorage.getItem('user_id');

  // Map meter types from frontend to backend
  const meterTypeMapping = {
    'kwh_offp': 'KWO',
    'kwh_day': 'KWD',
    'kwh_peak': 'KWP',
    'kwh_tot': 'KWT'
  };

  // Prepare meter readings array
  const meterReadings = [];

  // Process each meter type that belongs to the customer
  customerMeterTypes.forEach(meter => {
    const meterType = meter.meter_type?.trim();
    const meterConfig = Object.entries(meterTypeMapping).find(([key, value]) => value === meterType);
    
    if (meterConfig) {
      const [frontendPrefix, backendType] = meterConfig;
      const meterUpdate = {
        meter_type: backendType
      };

      let hasUpdate = false;

      // Check for present reading update
      const presentReadingField = `${frontendPrefix}_presentread`;
      if (editedFields[presentReadingField] !== undefined) {
        meterUpdate.present_reading = parseFloat(formData[presentReadingField]) || 0;
        hasUpdate = true;
      }

      // Check for units update
      const unitsField = `${frontendPrefix}_units`;
      if (editedFields[unitsField] !== undefined) {
        meterUpdate.units = parseFloat(formData[unitsField]) || 0;
        hasUpdate = true;
      }

      // Only add to array if there's at least one update
      if (hasUpdate) {
        meterReadings.push(meterUpdate);
      }
    }
  });

  // Prepare the update data structure
  const updateData = {
    session_id: sessionId,
    user_id: userId,
    account_number: formData.account_number,
    area_code: formData.area_code_number,
    bill_cycle: formData.current_billcycle_number,
  };

  // Add reading date if edited
  if (editedFields.reading_date !== undefined) {
    // Convert DD/MM/YYYY to ISO format (YYYY-MM-DD)
    const dateParts = formData.reading_date.split('/');
    if (dateParts.length === 3) {
      const isoDate = `${dateParts[2]}-${dateParts[1].padStart(2, '0')}-${dateParts[0].padStart(2, '0')}`;
      updateData.reading_date = isoDate;
    }
  }

  // Add meter readings if any
  if (meterReadings.length > 0) {
    updateData.meter_readings = meterReadings;
  }

  // Log what's being sent to backend
  console.log("Prepared update data for backend:", {
    updateData,
    editedFields: Object.keys(editedFields),
    meterReadingsCount: meterReadings.length
  });

  return updateData;
};

// FILE: src\views\ReadingsEntryView\hooks\useReadingsManagement.js
// Add this helper function
const logUpdateDebugInfo = (formData, editedFields, customerMeterTypes) => {
  console.log("=== DEBUG: Update Information ===");
  console.log("Edited fields count:", Object.keys(editedFields).length);
  console.log("Edited fields:", Object.keys(editedFields));
  
  // Check which meter fields are edited
  const meterTypeMapping = {
    'kwh_offp': 'KWO',
    'kwh_day': 'KWD', 
    'kwh_peak': 'KWP',
    'kva': 'KVA',
    'kvah': 'KVAH'
  };
  
  customerMeterTypes.forEach(meter => {
    const meterType = meter.meter_type?.trim();
    const meterConfig = Object.entries(meterTypeMapping).find(([key, value]) => value === meterType);
    
    if (meterConfig) {
      const [frontendPrefix] = meterConfig;
      
      const fieldsToCheck = [
        `${frontendPrefix}_presentread`,
        `${frontendPrefix}_units`,
        `${frontendPrefix}_rate`,
        `${frontendPrefix}_amount`
      ];
      
      const editedMeterFields = fieldsToCheck.filter(field => editedFields[field] !== undefined);
      
      if (editedMeterFields.length > 0) {
        console.log(`Meter ${meterType} edited fields:`, editedMeterFields);
        editedMeterFields.forEach(field => {
          console.log(`  ${field}: ${formData[field]} (edited)`);
        });
      }
    }
  });
  
  // Check charge fields
  const chargeFields = ['fixed_charge', 'monthly_charge', 'vat', 'tot_amount'];
  const editedChargeFields = chargeFields.filter(field => editedFields[field] !== undefined);
  
  if (editedChargeFields.length > 0) {
    console.log("Edited charge fields:", editedChargeFields);
    editedChargeFields.forEach(field => {
      console.log(`  ${field}: ${formData[field]} (edited)`);
    });
  }
  
  console.log("=== END DEBUG ===");
};

// Helper function to count updates
const getUpdateCount = (updateData) => {
  return {
    readingDate: updateData.reading_date !== undefined,
    meters: updateData.meter_readings ? updateData.meter_readings.length : 0,
    charges: [
      updateData.fixed_charge,
      updateData.monthly_charge,
      updateData.vat_amount,
      updateData.total_amount
    ].filter(value => value !== undefined).length
  };
};

  const handleSaveReadings = async () => {
    if (!saveReadingsActive) return;

    try {
        // Show loading state
        setLoading(true);
        
        // Prepare charges data from form
        const charges = {
            fixed_charge: formData.fixed_charge || "0.00",
            monthly_charge: formData.monthly_charge || "0.00",
            vat: formData.vat || "0.00",
            total_amount: formData.tot_amount || "0.00"
        };

        // Validate data before saving
        const validation = validateSaveData(formData, charges);
        if (!validation.isValid) {
            toast.error(`Cannot save readings: ${validation.message}`);
            setLoading(false);
            return;
        }

        // Prepare data for backend API
        const insertData = prepareInsertData(formData, customerMeterTypes, charges);
        
        console.log('Sending insert data to backend:', insertData);

        // Call the API to insert new readings
        const response = await insertNewMeterReadings(insertData);

        if (response.success) {
            toast.success(
                <div>
                    <div className="font-semibold">New readings saved successfully!</div>
                    <div className="text-sm mt-1">
                        {response.inserted_count} meter reading(s) inserted
                    </div>
                </div>,
                { autoClose: 3000 }
            );

            // Reset unsaved changes after successful save
            setHasUnsavedChanges(false);
            setSaveReadingsActive(false);
            
            // Redirect to pending readings page after a short delay
            setTimeout(() => {
                history.push('/pendReadings');
            }, 1500);
            
        } else {
            toast.error(`Failed to save readings: ${response.message}`);
        }

    } catch (error) {
        console.error('Error saving readings:', error);
        toast.error(`Failed to save readings: ${error.message}`);
    } finally {
        setLoading(false);
    }
};

  const handlePackChangesInput = (e) => {
    const { name, value } = e.target;
    setPackChangesData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSavePackChanges = async () => {
    if (!savePackChangesActive) return;

    try {
      const response = await updatePackChanges(
        formData.account_number,
        formData.area_code_number,
        formData.current_billcycle_number,
        packChangesData
      );

      if (response.success) {
        toast.success("Pack changes updated successfully!");
        setPackChangesData(resetPackChangesData());
        setPackChangesEdited(false);
        setSavePackChangesActive(false);
        setHasUnsavedChanges(false);

        if (formData.account_number && formData.area_code_number) {
          // Refresh the data after saving pack changes
          if (isReceivedReading) {
            loadReceivedReadingData(
              formData.account_number,
              formData.area_code_number,
              formData.current_billcycle_number
            );
          } else if (
            formData.reading_status === "PENDING" &&
            !formData.has_reading
          ) {
            loadPendingReadingData(
              formData.account_number,
              formData.area_code_number
            );
          } else {
            loadReceivedReadingData(
              formData.account_number,
              formData.area_code_number,
              formData.current_billcycle_number
            );
          }
        }
      } else {
        toast.error(response.message || "Failed to update pack changes");
      }
    } catch (error) {
      console.error("Error saving pack changes:", error);
      toast.error("Failed to save pack changes: " + error.message);
    }
  };

  const handlePackChangesToggle = (e) => {
    const shouldShow = e.target.checked;

    if (!shouldShow && packChangesEdited) {
      if (
        !window.confirm(
          "You have unsaved pack changes. Are you sure you want to close without saving?"
        )
      ) {
        e.preventDefault();
        return;
      }
    }

    setShowPackChanges(shouldShow);

    if (!shouldShow && packChangesEdited) {
      setPackChangesData(resetPackChangesData());
      setPackChangesEdited(false);
      setSavePackChangesActive(false);
      setHasUnsavedChanges(false);
    }
  };

// In the getFieldStyle function in useReadingsManagement.js
const getFieldStyle = (fieldName) => {
  // Fields that can be updated by API calculation
  const apiCalculatedFields = [
    // Rate fields
    "kwh_offp_rate",
    "kwh_day_rate",
    "kwh_peak_rate",
    "kva_rate",
    "kvah_rate",
    // Amount fields
    "kwh_offp_amount",
    "kwh_day_amount",
    "kwh_peak_amount",
    "kva_amount",
    "kvah_amount",
    // Charge fields
    "fixed_charge",
    "monthly_charge",
    "tot_amount",
  ];

  // For received readings, only specific fields are editable
  const editableFieldsForReceived = [
    "reading_date",
    "kwh_offp_presentread",
    "kwh_day_presentread",
    "kwh_peak_presentread",
    "kva_presentread",
    "kvah_presentread",
  ];

  // For pending readings (insert new), assessed code is also editable via dropdown
  const editableFieldsForPending = [
    "reading_date",
    "kwh_offp_presentread",
    "kwh_day_presentread",
    "kwh_peak_presentread",
    "kva_presentread",
    "kvah_presentread",
    "kwh_offp_assessedcode",
    "kwh_day_assessedcode",
    "kwh_peak_assessedcode",
    "kva_assessedcode",
    "kvah_assessedcode",
  ];

  const isEditable = isReceivedReading
    ? editableFieldsForReceived.includes(fieldName)
    : editableFieldsForPending.includes(fieldName);

  const isEdited = editedFields[fieldName] !== undefined;
  const isApiCalculated = apiCalculatedFields.includes(fieldName);

  let style = "";

  // CRITICAL FIX: Check if this field has an error (from error status)
  // First check if formData has error_fields array
  const hasError = formData.error_fields && 
                   Array.isArray(formData.error_fields) && 
                   formData.error_fields.includes(fieldName);

  if (hasError) {
    // Highlight error fields with red background - IMPORTANT: This takes precedence
    style += "bg-critical-100 text-critical-700 border-critical-500 ";
  } else if (isEditable && accountLoaded) {
    // For ALL editable fields in pending readings (including assessed code), use blue background
    style += "bg-navy-100/80 text-navy-600 "; // Blue background for all editable fields
  } else if (accountLoaded && isApiCalculated) {
    // For API-calculated fields, use light blue background with blue text
    style += "bg-navy-50 text-navy-700 font-medium "; // Blue styling for API-calculated values
  } else if (accountLoaded) {
    // For non-editable fields in both modes, use gray background
    style += "bg-ink-100 text-ink-600 "; // Gray background for non-editable fields
  }

  if (isEdited) {
    style += "text-critical-600 border-critical-500 "; // Red text when edited
  }

  return style.trim();
};
  // NEW: Helper function to get assessed code display value
  const getAssessedCodeDisplayValue = (backendValue) => {
    if (backendValue === "A") return "A";
    if (backendValue === " " || backendValue === "" || backendValue === null)
      return "Not-A";
    return backendValue; // Fallback
  };

  // NEW: Helper function to get assessed code dropdown value
  const getAssessedCodeDropdownValue = (backendValue) => {
    if (backendValue === "A") return "A";
    if (backendValue === " " || backendValue === "" || backendValue === null)
      return "Not-A";
    return "Not-A"; // Default
  };

const handleCalculateRates = async () => {
  if (!calculateButtonEnabled || calculating) {
    return;
  }

  try {
    console.log("Starting rates calculation with API...");
    setCalculating(true);

    const loadingToast = toast.loading(
      "Calculating rates and amounts via API..."
    );

    // TEMPORARY FIX: Ensure reading_date is properly formatted for API
    let readingDateForApi = formData.reading_date;
    let previousReadingDateForApi = formData.previous_reading_date;
    
    console.log("Original dates from formData:", {
      reading_date: formData.reading_date,
      previous_reading_date: formData.previous_reading_date
    });

    // Convert DD/MM/YYYY to ISO format if needed
    const convertToISO = (dateString) => {
      if (!dateString) return new Date().toISOString();
      
      if (dateString.includes('/')) {
        // DD/MM/YYYY format
        const parts = dateString.split('/');
        if (parts.length === 3) {
          const day = parseInt(parts[0], 10);
          const month = parseInt(parts[1], 10) - 1; // Month is 0-indexed
          const year = parseInt(parts[2], 10);
          const date = new Date(year, month, day);
          
          if (!isNaN(date.getTime())) {
            return date.toISOString();
          }
        }
      }
      
      // If not in DD/MM/YYYY format, try to parse as is
      try {
        const date = new Date(dateString);
        if (!isNaN(date.getTime())) {
          return date.toISOString();
        }
      } catch (error) {
        console.error('Error parsing date:', error);
      }
      
      // Fallback to current date
      return new Date().toISOString();
    };

    // Create temporary formData with ISO dates
    const tempFormData = {
      ...formData,
      reading_date: convertToISO(readingDateForApi),
      previous_reading_date: convertToISO(previousReadingDateForApi)
    };

    console.log("Converted dates for API:", {
      reading_date: tempFormData.reading_date,
      previous_reading_date: tempFormData.previous_reading_date
    });

    // Prepare request data for API using temporary formData
    const requestData = await prepareMonthlyChargeRequest(
      tempFormData,
      customerMeterTypes
    );

    console.log('Calling monthly charge API with data (including isSSCL):', requestData);
    console.log('Reading date in API request:', {
      fromDate: requestData.fromDate,
      toDate: requestData.toDate,
      readingDate: requestData.reading_date // Check if this field exists
    });

    // Call the external API with timeout handling
    const apiResponse = await calculateMonthlyCharges(requestData);

    // Dismiss loading toast
    toast.dismiss(loadingToast);
    setCalculating(false);

    console.log("API response received with VAT info:", {
      totalCharge: apiResponse.totalCharge,
      ssclCharge: apiResponse.ssclCharge,
      grandTotal: apiResponse.grandTotal,
      rows: apiResponse.rows?.length || 0
    });

    // Map API response to form field updates
    const fieldUpdates = mapApiResponseToFormFields(
      apiResponse,
      formData,
      customerMeterTypes
    );

    // CRITICAL: Update editedFields to include all calculated fields
    const newEditedFields = { ...editedFields };
    
    // Add all calculated fields to edited fields
    Object.keys(fieldUpdates).forEach((field) => {
      // Check if the field has actually changed from original
      if (originalData[field] !== fieldUpdates[field]) {
        newEditedFields[field] = fieldUpdates[field];
      }
    });

    // Update form data with API response values AND update editedFields
    setFormData((prev) => ({
      ...prev,
      ...fieldUpdates,
    }));

    // Update edited fields state
    setEditedFields(newEditedFields);

    // Show success message
    const presentReadingsCount = [
      formData.kwh_offp_presentread,
      formData.kwh_day_presentread,
      formData.kwh_peak_presentread,
      formData.kva_presentread,
      formData.kvah_presentread,
    ].filter(
      (reading) => reading && reading.trim() !== "" && parseFloat(reading) > 0
    ).length;

    const totalAmount = parseFloat(fieldUpdates.tot_amount) || 0;
    const vatAmount = parseFloat(fieldUpdates.vat) || 0;

    toast.success(
      <div>
        <div className="font-semibold">
          Rates and amounts calculated successfully!
        </div>
      </div>,
      {
        autoClose: 5000,
        hideProgressBar: false,
      }
    );

    // Log detailed calculation for debugging
    console.log("Calculation completed via API:", {
      originalReadingDate: readingDateForApi,
      convertedReadingDate: tempFormData.reading_date,
      requestData: {
        ...requestData,
        isSSCL: requestData.isSSCL // Include isSSCL in logs
      },
      apiResponse: {
        totalCharge: apiResponse.totalCharge,
        ssclCharge: apiResponse.ssclCharge,
        grandTotal: apiResponse.grandTotal
      },
      fieldUpdates,
      updatedEditedFields: newEditedFields,
      total: totalAmount.toFixed(2),
      vat: vatAmount.toFixed(2)
    });

    setCalculationSuccessful(true);

  } catch (error) {
    console.error("Error calculating rates and amounts via API:", error);

    // Dismiss any loading toast if it exists and clear loading state
    toast.dismiss();
    setCalculating(false);

    // Show specific error messages based on error type - ONLY SHOW ERROR MESSAGE, NO FALLBACK CALCULATION
    let errorMessage = "Cannot connect to API server. Please check network connectivity.";

    if (error.message.includes("timed out")) {
      errorMessage = "API request timed out. Please check network connectivity.";
    } else if (error.message.includes("Failed to fetch")) {
      errorMessage = "Cannot connect to API server. Please check network connectivity.";
    } else if (
      error.message.includes("network") ||
      error.message.includes("CORS")
    ) {
      errorMessage = "Network error. Please check API connectivity.";
    }

    // Show only the error message without fallback calculation
    toast.error(
      <div>
        <div className="font-semibold">{errorMessage}</div>
      </div>,
      { autoClose: 6000 }
    );

    setCalculationSuccessful(false);
  }
};

  // Fallback manual calculation function
const performManualCalculation = () => {
  try {
    // Recalculate units for all meter types
    const calculateUnits = (presentKey, previousKey, unitsKey) => {
      const present = parseFloat(formData[presentKey]) || 0;
      const previous = parseFloat(formData[previousKey]) || 0;
      const units = present - previous;

      if (units >= 0) {
        return units.toString();
      } else {
        // If negative units, show warning and set to 0
        console.warn(`Negative units calculated for ${unitsKey}: ${units}`);
        return "0";
      }
    };

    // Calculate amounts for all meter types
    const calculateAmount = (unitsKey, multiplyKey, rateKey, amountKey) => {
      const units = parseFloat(formData[unitsKey]) || 0;
      const multiply = parseFloat(formData[multiplyKey]) || 1;
      const rate = parseFloat(formData[rateKey]) || 0;
      const amount = units * multiply * rate;
      return amount.toFixed(2);
    };

    // KWH Off Peak calculations
    const kwhOffpUnits = calculateUnits(
      "kwh_offp_presentread",
      "kwh_offp_previousread",
      "kwh_offp_units"
    );
    const kwhOffpAmount = calculateAmount(
      "kwh_offp_units",
      "kwh_offp_multiplyby",
      "kwh_offp_rate",
      "kwh_offp_amount"
    );

    // KWH Day calculations
    const kwhDayUnits = calculateUnits(
      "kwh_day_presentread",
      "kwh_day_previousread",
      "kwh_day_units"
    );
    const kwhDayAmount = calculateAmount(
      "kwh_day_units",
      "kwh_day_multiplyby",
      "kwh_day_rate",
      "kwh_day_amount"
    );

    // KWH Peak calculations
    const kwhPeakUnits = calculateUnits(
      "kwh_peak_presentread",
      "kwh_peak_previousread",
      "kwh_peak_units"
    );
    const kwhPeakAmount = calculateAmount(
      "kwh_peak_units",
      "kwh_peak_multiplyby",
      "kwh_peak_rate",
      "kwh_peak_amount"
    );

    // KVA calculations
    const kvaUnits = calculateUnits(
      "kva_presentread",
      "kva_previousread",
      "kva_units"
    );
    const kvaAmount = calculateAmount(
      "kva_units",
      "kva_multiplyby",
      "kva_rate",
      "kva_amount"
    );

    // KVAH calculations (special handling - units = present reading)
    const kvahUnits = formData.kvah_presentread || "0";
    const kvahAmount = calculateAmount(
      "kvah_units",
      "kvah_multiplyby",
      "kvah_rate",
      "kvah_amount"
    );

    // Update form data with calculated values
    setFormData((prev) => ({
      ...prev,
      // Update units
      kwh_offp_units: kwhOffpUnits,
      kwh_day_units: kwhDayUnits,
      kwh_peak_units: kwhPeakUnits,
      kva_units: kvaUnits,
      kvah_units: kvahUnits,

      // Update amounts
      kwh_offp_amount: kwhOffpAmount,
      kwh_day_amount: kwhDayAmount,
      kwh_peak_amount: kwhPeakAmount,
      kva_amount: kvaAmount,
      kvah_amount: kvahAmount,

      // DO NOT UPDATE tot_amount here - it will come from API
    }));

    // Update edited fields if this is for received readings
    if (isReceivedReading && accountLoaded) {
      const newEditedFields = { ...editedFields };

      // Add calculated fields to edited fields
      const calculatedFields = [
        "kwh_offp_units",
        "kwh_day_units",
        "kwh_peak_units",
        "kva_units",
        "kvah_units",
        "kwh_offp_amount",
        "kwh_day_amount",
        "kwh_peak_amount",
        "kva_amount",
        "kvah_amount",
        // REMOVE: "tot_amount" - not calculated manually
      ];

      calculatedFields.forEach((field) => {
        if (originalData[field] !== formData[field]) {
          newEditedFields[field] = formData[field];
        }
      });

      setEditedFields(newEditedFields);
    }
  } catch (error) {
    console.error("Error in fallback calculation:", error);
    toast.error(
      <div>
        <div className="font-semibold">Calculation Failed!</div>
        <div className="text-sm mt-1">
          Please check your input values and try again.
        </div>
      </div>,
      { autoClose: 5000 }
    );
  }
};

  // Breadcrumb navigation handler
  const handleBackClick = () => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => {
        // Determine where to go based on the source
        if (isReceivedReading) {
          // For received readings, go back to Received Readings page
          history.push("/tempReadings");
        } else if (formData.reading_status === "PENDING") {
          // For pending readings (insert new readings), go back to Pending Readings
          history.push("/pendReadings");
        } else {
          // For other cases, go back to Dashboard
          history.push("/admin/dashboard");
        }
      });
    } else {
      if (isReceivedReading) {
        history.push("/tempReadings");
      } else if (formData.reading_status === "PENDING") {
        history.push("/pendReadings");
      } else {
        history.push("/admin/dashboard");
      }
    }
  };

  // Update handleBackClick to handle navigation to the correct page
  const handleBreadcrumbBackClick = (itemIndex) => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => {
        // Determine where to go based on the clicked item
        if (itemIndex === 0) {
          // Dashboard clicked
          history.push("/admin/dashboard");
        } else if (itemIndex === 1) {
          // Second item clicked (Received Readings or Pending Readings)
          if (isReceivedReading) {
            history.push("/tempReadings");
          } else if (formData.reading_status === "PENDING") {
            history.push("/pendReadings");
          } else {
            history.push("/admin/dashboard");
          }
        }
      });
    } else {
      if (itemIndex === 0) {
        history.push("/admin/dashboard");
      } else if (itemIndex === 1) {
        if (isReceivedReading) {
          history.push("/tempReadings");
        } else if (formData.reading_status === "PENDING") {
          history.push("/pendReadings");
        } else {
          history.push("/admin/dashboard");
        }
      }
    }
  };

  // Modal handlers
  const handleLeavePage = () => {
    setShowUnsavedModal(false);
    if (pendingNavigation) {
      pendingNavigation();
    }
    // Reset unsaved changes flags
    setHasUnsavedChanges(false);
    setEditedFields({});
    setPackChangesEdited(false);
  };

  const handleStayOnPage = () => {
    setShowUnsavedModal(false);
    setPendingNavigation(null);
  };

  // Breadcrumb items based on source and reading status
  const breadcrumbItems = isReceivedReading
    ? [
        { label: "Dashboard", href: "/admin/dashboard" },
        { label: "Received Readings", href: "/tempReadings" },
        { label: "Edit Received Reading", href: null },
      ]
    : formData.reading_status === "PENDING"
    ? [
        { label: "Dashboard", href: "/admin/dashboard" },
        { label: "Pending Readings", href: "/pendReadings" },
        { label: "Insert New Reading", href: null },
      ]
    : [
        { label: "Dashboard", href: "/admin/dashboard" },
        { label: "Edit Received Reading", href: null },
      ];

  return {
    formData,
    loading,
    accountLoaded,
    showPackChanges,
    packChangesData,
    packChangesEdited,
    savePackChangesActive,
    showCalendar,
    currentMonth,
    selectedDate,
    showLoadButton,
    editedFields,
    saveUpdatesActive,
    saveReadingsActive,
    resetActive,
    customerMeterTypes,
    headerConfig,
    readingDateInputRef,
    isReceivedReading,
    handleFolioNumberChange,
    loadAccountData,
    handleInputChange,
    handlePackChangesInput,
    handleSavePackChanges,
    handlePackChangesToggle,
    handleRefresh,
    setShowCalendar,
    setCurrentMonth,
    setSelectedDate,
    handleDateSelect,
    handleTodayClick,
    navigateMonth,
    handleSaveUpdates,
    handleSaveReadings,
    handleResetEdits,
    getFieldStyle,
    getDaysInMonth,
    formatDate,
    getAssessedCodeDisplayValue,
    getAssessedCodeDropdownValue,
    loadPendingReadingData,
    loadReceivedReadingData,
    handleCalculateRates,
    calculateButtonEnabled,
    calculating,
    // New values for breadcrumb and unsaved changes
    hasUnsavedChanges,
    showUnsavedModal,
    handleBackClick: handleBreadcrumbBackClick,
    handleLeavePage,
    handleStayOnPage,
    breadcrumbItems,
    loadErrorReadingData,
    calculationSuccessful,
    // Resolve Invoice states and handlers
    isResolveInvoice,
    showResolveConfirmationModal,
    setShowResolveConfirmationModal,
    isResolveActionLoading,
    handleConfirmResolveSubmit,
    handleConfirmResolveDraft,
  };
};
