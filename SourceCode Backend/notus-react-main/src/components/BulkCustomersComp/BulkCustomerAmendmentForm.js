// src/components/BulkCustomersComp/BulkCustomerAmendmentForm.js
import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import {
  getBillCycleForArea,
  getSelectedAreaCode,
} from "services/AreaAndBillService";
import { validationRules } from "services/amendmentValidationRules";
const BulkCustomerAmendmentForm = ({ customer, onClose }) => {
  const [formData, setFormData] = useState({
    effectiveBillCycle: "",
    amendmentType: "",
    effectiveDate: new Date().toISOString().split("T")[0],
    oldValue: "",
    newValue: "",
    status: null,
  });
  const [amendmentTypes, setAmendmentTypes] = useState([]);
  const [yrMnthData, setYrMnthData] = useState([]);
  const [selectedAmndType, setSelectedAmndType] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isExisting, setIsExisting] = useState(false);
  const [dateError, setDateError] = useState("");
  const [isEffectiveBillCycleEdited, setIsEffectiveBillCycleEdited] =
    useState(false);
  const baseUrl = process.env.REACT_APP_API_BASE_URL;
  const monthMap = {
    Jan: 1,
    Feb: 2,
    Mar: 3,
    Apr: 4,
    May: 5,
    Jun: 6,
    Jul: 7,
    Aug: 8,
    Sep: 9,
    Oct: 10,
    Nov: 11,
    Dec: 12,
  };
  const tariffOptions = [
    { tariff: "IP1", cusCat: "B", desc: "Industrial Purpose 1 (I-1)" },
    { tariff: "IP2", cusCat: "B", desc: "Industrial Purpose 2 (I-2)" },
    { tariff: "GP1", cusCat: "B", desc: "General Purpose 1 (G.P.-1)" },
    { tariff: "GP2", cusCat: "B", desc: "General Purpose 2 (G.P.-2)" },
    { tariff: "TD1", cusCat: "B", desc: "Time of Day Tariff 1- I-1(TD)" },
    { tariff: "TD2", cusCat: "B", desc: "Time of day Tariff 2 - I-2(TD)" },
    { tariff: "IP3", cusCat: "B", desc: "Industrial Purpose 3 (I-3)" },
    { tariff: "GP3", cusCat: "B", desc: "General Purpose 3 (G.P.-3)" },
    { tariff: "TD3", cusCat: "B", desc: "Time of Day Tariff 3 - I-3(TD)" },
    { tariff: "RL0", cusCat: "O", desc: "Religious conces." },
    { tariff: "RL1", cusCat: "O", desc: "Religious Purpose" },
    { tariff: "DM1", cusCat: "O", desc: "Domestic Tariff 1" },
    { tariff: "GP0", cusCat: "B", desc: "General Purpose 1 (G.P.-1)" },
    { tariff: "HL1", cusCat: "B", desc: "Hotel Tariff 1" },
    { tariff: "HL2", cusCat: "B", desc: "Hotel Tariff 2" },
    { tariff: "HL3", cusCat: "B", desc: "Hotel Tariff 3" },
    { tariff: "HD3", cusCat: "B", desc: "Hotel Time of Day 3" },
    { tariff: "ST2", cusCat: "B", desc: "Standby Tariff" },
    { tariff: "IP0", cusCat: "B", desc: "Industrial Purpose 0 (I-1)" },
    { tariff: "H1G", cusCat: "B", desc: "Hotel General Purpose- H-1(GP)" },
    { tariff: "H2G", cusCat: "B", desc: "Hotel General Purpose- H-2(GP)" },
    { tariff: "H2I", cusCat: "B", desc: "Hotel Industrial - H-2(I)" },
    { tariff: "H3I", cusCat: "B", desc: "Hotel Industrial - H-3(I)" },
    { tariff: "H2T", cusCat: "B", desc: "Hotel Time of Day - H-2(I-TD)" },
    { tariff: "H3T", cusCat: "B", desc: "Hotel Time of Day - H-3(I-TD)" },
    { tariff: "T2D", cusCat: "B", desc: "3 part Time of Day Tariff I-2" },
    { tariff: "T3D", cusCat: "B", desc: "3 part Time of day Tariff I-3" },
    { tariff: "I1", cusCat: "B", desc: "Industrial (I1)" },
    { tariff: "I2", cusCat: "B", desc: "Industrial (I2)" },
    { tariff: "I3", cusCat: "B", desc: "Industrial (I3)" },
    { tariff: "H1", cusCat: "B", desc: "Hotel (H1)" },
    { tariff: "H2", cusCat: "B", desc: "Hotel (H2)" },
    { tariff: "H3", cusCat: "B", desc: "Hotel (H3)" },
    { tariff: "SL1", cusCat: "B", desc: "Street Light 1" },
    { tariff: "UN1", cusCat: "B", desc: "Unautherised Connection" },
    { tariff: "SL2", cusCat: "B", desc: "Street Light 2" },
    { tariff: "TM1", cusCat: "B", desc: "Temporary Connections" },
    { tariff: "GV1", cusCat: "B", desc: "Government(GV-1)" },
    { tariff: "GV2", cusCat: "B", desc: "Government(GV-2)" },
    { tariff: "GV3", cusCat: "B", desc: "Government(GV-3)" },
    { tariff: "DM2", cusCat: "O", desc: "Domestic TOU Tariff" },
  ];
  const getCustomerCategory = () => {
    const cat = customer?.cus_cat || customer?.cusCat || "";
    return cat.toString().trim().toUpperCase();
  };
  const getTariffOptionsForCustomer = () => {
    const cat = getCustomerCategory();
    if (!cat) {
      // If no category, return all tariffs
      console.warn(`Customer ${customer.acc_nbr} has no cus_cat, showing all tariffs`);
      return tariffOptions;
    }
    return tariffOptions.filter((option) => option.cusCat === cat);
  };
  const getMonthRange = (billCycle) => {
    const entry = yrMnthData.find((d) => d.bill_cycle === parseInt(billCycle));
    if (!entry) return null;
    const [yearStr, monthStr] = entry.bill_mnth.split(" ");
    const year = parseInt(yearStr);
    const monthNum = monthMap[monthStr];
    if (!monthNum) return null;
    const start = new Date(year, monthNum - 1, 1);
    const end = new Date(year, monthNum, 0);
    return { start, end, billMnth: entry.bill_mnth };
  };
  const validateEffectiveDate = () => {
    if (!formData.effectiveBillCycle || !formData.effectiveDate) {
      setDateError("");
      return true;
    }
    const range = getMonthRange(formData.effectiveBillCycle);
    if (!range) {
      setDateError("Invalid bill cycle");
      return false;
    }
    const selectedDate = new Date(formData.effectiveDate);
    if (selectedDate < range.start || selectedDate > range.end) {
      setDateError(
        `Effective date must be in ${
          range.billMnth
        } (${range.start.toLocaleDateString()} - ${range.end.toLocaleDateString()})`
      );
      return false;
    }
    setDateError("");
    return true;
  };
  useEffect(() => {
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
          setAmendmentTypes(
            data.map((type) => ({
              value: type.amdType,
              label: `${type.amdType} - ${type.amdDesc}`,
              dtType: type.dtType,
              uptblName: type.uptblName,
              fieldName: type.fieldName,
            }))
          );
        } else {
          toast.error("Failed to load amendment types");
        }
      } catch (error) {
        console.error("Error fetching amendment types:", error);
        toast.error("Failed to load amendment types");
      }
    };
    const fetchYrMnth = async () => {
      try {
        const response = await fetch(`${baseUrl}/api/v1/yr-mnth`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
        });
        if (response.ok) {
          const data = await response.json();
          setYrMnthData(data);
        } else {
          console.error("Failed to load year-month data");
        }
      } catch (error) {
        console.error("Error fetching year-month data:", error);
      }
    };
    fetchAmendmentTypes();
    fetchYrMnth();
  }, [baseUrl]);
  // Validation useEffect
  useEffect(() => {
    validateEffectiveDate();
  }, [formData.effectiveBillCycle, formData.effectiveDate, yrMnthData]);
  useEffect(() => {
    if (customer && customer.area_cd) {
      const billCycle = getBillCycleForArea(customer.area_cd);
      if (billCycle) {
        setFormData((prev) => ({
          ...prev,
          effectiveBillCycle: billCycle.toString(),
        }));
      }
    }
  }, [customer]);
  // NEW: Fetch existing amendment if any
  useEffect(() => {
    const fetchExistingAmendment = async () => {
      // NEW: Check if the prop is an amendment (has amdType)
      if (customer.amdType) {
        // It's a specific amendment (e.g., from rejected list)
        const amnd = customer;
        const selectedType = amendmentTypes.find(
          (type) => type.value === amnd.amdType
        );
        if (selectedType) {
          setSelectedAmndType(selectedType);
        }
        setFormData((prev) => ({
          ...prev,
          amendmentType: amnd.amdType || "",
          effectiveBillCycle: amnd.effctBlcy?.toString() || "",
          effectiveDate:
            amnd.effctDate || new Date().toISOString().split("T")[0],
          oldValue: "", // Will be fetched based on type
          newValue:
            amnd.nmrValue != null
              ? amnd.nmrValue.toString()
              : amnd.chrValue || "",
          status: amnd.status,
        }));
        setIsExisting(true);
        return; // Skip fetching
      }

      // Otherwise, it's a customer, fetch existing amendments
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
          const amendments = data.amendments || []; // Fix: Use plural 'amendments'
          if (amendments.length > 0) {
            const amnd = amendments[0]; // Take the first one (as per original assumption)
            const selectedType = amendmentTypes.find(
              (type) => type.value === amnd.amdType
            );
            if (selectedType) {
              setSelectedAmndType(selectedType);
            }
            setFormData((prev) => ({
              ...prev,
              amendmentType: amnd.amdType || "",
              effectiveBillCycle: amnd.effctBlcy?.toString() || "",
              effectiveDate:
                amnd.effctDate || new Date().toISOString().split("T")[0],
              oldValue: "", // Will be fetched based on type
              newValue:
                amnd.nmrValue != null
                  ? amnd.nmrValue.toString()
                  : amnd.chrValue || "",
              status: amnd.status,
            }));
            setIsExisting(true);
          } else {
            setIsExisting(false);
          }
        } else if (response.status === 404) {
          // No existing amendment, create mode
          setIsExisting(false);
        } else {
          throw new Error("Failed to fetch existing amendment");
        }
      } catch (error) {
        console.error("Error fetching existing amendment:", error);
        setIsExisting(false);
      }
    };
    if (customer && amendmentTypes.length > 0) {
      fetchExistingAmendment();
    }
  }, [customer, amendmentTypes, baseUrl]);
  // UPDATED: Fetch old value using SINGLE API
  useEffect(() => {
    const fetchOldValue = async () => {
      if (!selectedAmndType || !customer || !formData.effectiveBillCycle)
        return;
      try {
        const response = await fetch(
          `${baseUrl}/api/v1/amendments/old-value?accNbr=${
            customer.acc_nbr || customer.accNbr
          }&amdType=${selectedAmndType.value}&billCycle=${
            formData.effectiveBillCycle
          }`,
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
          setFormData((prev) => ({
            ...prev,
            oldValue: data.oldValue || "Not available",
          }));
        } else {
          throw new Error("Failed to fetch old value");
        }
      } catch (error) {
        console.error("Error fetching old value:", error);
        setFormData((prev) => ({
          ...prev,
          oldValue: "Error fetching value",
        }));
      }
    };
    fetchOldValue();
  }, [selectedAmndType, customer, formData.effectiveBillCycle, baseUrl]);
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "effectiveBillCycle") {
      // Only allow digits
      if (!/^\d*$/.test(value)) return;
      setIsEffectiveBillCycleEdited(true);
    }
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };
  const handleAmendmentTypeChange = (e) => {
    const selectedValue = e.target.value;
    const selectedType = amendmentTypes.find(
      (type) => type.value === selectedValue
    );
    setSelectedAmndType(selectedType);
    setFormData((prev) => ({
      ...prev,
      amendmentType: selectedValue,
      oldValue: "",
      newValue: "",
    }));
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    // Validation
    if (!formData.effectiveBillCycle) {
      toast.error("Please enter Effective Bill Cycle");
      setLoading(false);
      return;
    }
    const currentBillCycle = getBillCycleForArea(
      customer.area_cd || customer.areaCd
    );
    if (!currentBillCycle) {
      toast.error("Current bill cycle not available");
      setLoading(false);
      return;
    }
    if (isEffectiveBillCycleEdited) {
      const enteredBillCycle = parseInt(formData.effectiveBillCycle);
      if (
        isNaN(enteredBillCycle) ||
        enteredBillCycle !== currentBillCycle + 1
      ) {
        toast.error(
          `Effective bill cycle must be exactly ${currentBillCycle + 1}`
        );
        setLoading(false);
        return;
      }
    }
    if (!formData.amendmentType) {
      toast.error("Please select Amendment Type");
      setLoading(false);
      return;
    }
    if (!formData.effectiveDate) {
      toast.error("Please select Effective Date");
      setLoading(false);
      return;
    }
    if (!validateEffectiveDate()) {
      toast.error(dateError);
      setLoading(false);
      return;
    }
    const trimmedNewValue = formData.newValue.trim();
    if (!trimmedNewValue) {
      toast.error("Please enter New Value");
      setLoading(false);
      return;
    }
    const rule = validationRules[formData.amendmentType];
    if (!rule) {
      toast.error("No validation rules defined for this amendment type");
      setLoading(false);
      return;
    }
    if (!rule.validate(trimmedNewValue)) {
      toast.error(rule.errorMsg);
      setLoading(false);
      return;
    }
    const transformedNewValue = rule.transform(trimmedNewValue);
    try {
      const currentUserId = sessionStorage.getItem("user_id") || "SYSTEM";
      const areaCode =
        getSelectedAreaCode() || customer.area_cd || customer.areaCd;
      if (!areaCode) {
        toast.error("Area code is required. Please ensure area is selected.");
        setLoading(false);
        return;
      }
      // Build payload with ONLY the fields we need - NO NULL FIELDS
      const payload = {
        accNbr: customer.acc_nbr || customer.accNbr,
        amdType: formData.amendmentType,
        areaCd: areaCode,
        addedBlcy: parseInt(formData.effectiveBillCycle),
        effctBlcy: parseInt(formData.effectiveBillCycle),
        effctDate: formData.effectiveDate,
        amauthCode: "SYS",
        userId: currentUserId,
        editedUserId: currentUserId,
      };
      // CRITICAL FIX: Only include ONE value field based on data type
      const dtType = selectedAmndType?.dtType;
      if (dtType === "N") {
        // For numeric amendments - ONLY include nmrValue, DO NOT include chrValue
        payload.nmrValue = parseFloat(transformedNewValue);
        // Ensure we don't send chrValue at all
      } else {
        // For character/date amendments - ONLY include chrValue, DO NOT include nmrValue
        payload.chrValue = transformedNewValue;
        // Ensure we don't send nmrValue at all
      }
      console.log("Submitting amendment:", payload);
      const response = await fetch(`${baseUrl}/api/v1/amendments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
        body: JSON.stringify(payload),
      });
      if (response.ok) {
        const result = await response.json();
        toast.success("Amendment saved successfully!");
        onClose();
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to save amendment");
      }
    } catch (error) {
      console.error("Error saving amendment:", error);
      toast.error("Failed to save amendment: " + error.message);
    } finally {
      setLoading(false);
    }
  };
  const handleEdit = async () => {
    if (formData.status === 2) {
      toast.error("Cannot update a posted amendment.");
      return;
    }
    setLoading(true);
    try {
      if (!formData.effectiveBillCycle) {
        toast.error("Please enter Effective Bill Cycle");
        setLoading(false);
        return;
      }
      const currentBillCycle = getBillCycleForArea(
        customer.area_cd || customer.areaCd
      );
      if (!currentBillCycle) {
        toast.error("Current bill cycle not available");
        setLoading(false);
        return;
      }
      if (isEffectiveBillCycleEdited) {
        const enteredBillCycle = parseInt(formData.effectiveBillCycle);
        if (
          isNaN(enteredBillCycle) ||
          enteredBillCycle !== currentBillCycle + 1
        ) {
          toast.error(
            `Effective bill cycle must be exactly ${currentBillCycle + 1}`
          );
          setLoading(false);
          return;
        }
      }
      if (!validateEffectiveDate()) {
        toast.error(dateError);
        setLoading(false);
        return;
      }
      const trimmedNewValue = formData.newValue.trim();
      if (!trimmedNewValue) {
        toast.error("Please enter New Value");
        setLoading(false);
        return;
      }
      const rule = validationRules[formData.amendmentType];
      if (!rule) {
        toast.error("No validation rules defined for this amendment type");
        setLoading(false);
        return;
      }
      if (!rule.validate(trimmedNewValue)) {
        toast.error(rule.errorMsg);
        setLoading(false);
        return;
      }
      const transformedNewValue = rule.transform(trimmedNewValue);
      const currentUserId = sessionStorage.getItem("user_id") || "SYSTEM";
      const areaCode =
        getSelectedAreaCode() || customer.area_cd || customer.areaCd;
      if (!areaCode) {
        toast.error("Area code is required. Please ensure area is selected.");
        setLoading(false);
        return;
      }
      // Build payload with ONLY the fields we need - NO NULL FIELDS
      const payload = {
        accNbr: customer.acc_nbr || customer.accNbr,
        amdType: formData.amendmentType,
        areaCd: areaCode,
        addedBlcy: parseInt(formData.effectiveBillCycle),
        effctBlcy: parseInt(formData.effectiveBillCycle),
        effctDate: formData.effectiveDate,
        amauthCode: "SYS",
        userId: currentUserId,
        editedUserId: currentUserId,
      };
      // CRITICAL FIX: Only include ONE value field based on data type
      const dtType = selectedAmndType?.dtType;
      if (dtType === "N") {
        payload.nmrValue = parseFloat(transformedNewValue);
        // DO NOT include chrValue
      } else {
        payload.chrValue = transformedNewValue;
        // DO NOT include nmrValue
      }
      const response = await fetch(
        `${baseUrl}/api/v1/amendments/${customer.acc_nbr || customer.accNbr}/${
          formData.amendmentType
        }/${parseInt(formData.effectiveBillCycle)}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
          body: JSON.stringify(payload),
        }
      );
      if (response.ok) {
        toast.success("Amendment updated successfully!");
        onClose();
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to update amendment");
      }
    } catch (error) {
      console.error("Error updating amendment:", error);
      toast.error("Failed to update amendment: " + error.message);
    } finally {
      setLoading(false);
    }
  };
  const handleDelete = async () => {
    if (!window.confirm("Are you sure you want to delete this amendment?")) {
      return;
    }
    setLoading(true);
    try {
      const response = await fetch(
        `${baseUrl}/api/v1/amendments/${customer.acc_nbr || customer.accNbr}/${
          formData.amendmentType
        }/${parseInt(formData.effectiveBillCycle)}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
        }
      );
      if (response.ok) {
        toast.success("Amendment deleted successfully!");
        onClose();
      } else {
        const errorData = await response.json();
        toast.error(errorData.message || "Failed to delete amendment");
      }
    } catch (error) {
      console.error("Error deleting amendment:", error);
      toast.error("Failed to delete amendment");
    } finally {
      setLoading(false);
    }
  };
  const handleCancel = () => {
    onClose();
  };
  // Get input type based on data type
  const getNewValueInputType = () => {
    if (!selectedAmndType) return "text";
    switch (selectedAmndType.dtType) {
      case "N":
        return "number";
      case "D":
        return "date";
      default:
        return "text";
    }
  };
  const isTariffChange = selectedAmndType?.fieldName === "TARIFF";
  const availableTariffs = getTariffOptionsForCustomer();
  const shouldShowTariffSelect =
    isTariffChange && availableTariffs.length > 0;
  
  // Debug: Log customer category information
  React.useEffect(() => {
    if (isTariffChange) {
      const cat = getCustomerCategory();
      console.log(`Customer ${customer.acc_nbr}: category="${cat}", availableTariffs=${availableTariffs.length}`);
    }
  }, [isTariffChange, customer.acc_nbr]);
  
  return (
    <div className="bg-white rounded-lg shadow-lg p-4 sm:p-6 border border-ink-200">
      <div className="flex items-center justify-between mb-4 border-b border-ink-300 pb-3">
        <div className="flex items-center">
          <div className="ds-btn ds-btn-primary ds-btn-sm ds-btn-icon mr-3">
            <i className="fas fa-edit text-sm"></i>
          </div>
          <h3 className="ds-page-title">
            {isExisting && formData.status === 2
              ? "View Posted"
              : isExisting
              ? "Edit"
              : "Create"}{" "}
            Master Amendments - Folio {customer?.folio_no ?? customer?.folioNo ?? "N/A"}
          </h3>
        </div>
        <button
          onClick={handleCancel}
          className="text-ink-500 hover:text-ink-700 transition-colors p-1"
          title="Close"
          disabled={loading}
        >
          <i className="fas fa-times text-lg"></i>
        </button>
      </div>
      {formData.status && (
        <div
          className={`border rounded mb-4 p-3 ${
            formData.status === 3
              ? "bg-critical-50 border-critical-200 text-critical-800"
              : formData.status === 2
              ? "bg-success-50 border-success-200 text-success-800"
              : ""
          }`}
        >
          {formData.status === 1 && <span>Status: Pending</span>}
          {formData.status === 2 && (
            <span>
              <strong>Posted:</strong> Amendment has been applied successfully.
            </span>
          )}
          {formData.status === 3 && (
            <span>
              <strong>Rejected:</strong> Please review and resubmit if needed.
            </span>
          )}
        </div>
      )}
      <form onSubmit={handleSubmit}>
        {/* Customer Info Display */}
        {isTariffChange && (
          <div className="mb-4 p-3 bg-navy-50 border border-navy-200 rounded-lg text-sm">
            <div className="flex items-start gap-3">
              <i className="fas fa-info-circle text-navy-600 mt-0.5 flex-shrink-0"></i>
              <div>
                <p className="text-navy-900">
                  <strong>Customer Category:</strong>{" "}
                  {getCustomerCategory() ? (
                    <>
                      <code className="bg-navy-100 px-2 py-0.5 rounded">
                        {getCustomerCategory()}
                      </code>{" "}
                      ({getCustomerCategory() === "B"
                        ? "Bulk - Industrial/General Purpose"
                        : getCustomerCategory() === "O"
                        ? "Ordinary - Domestic/Religious"
                        : "Unknown"}
                      )
                    </>
                  ) : (
                    <>
                      <span className="text-critical-600">Not Set</span> - Showing
                      all available tariffs
                    </>
                  )}
                </p>
              </div>
            </div>
          </div>
        )}
        {/* Amendment Details Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4 bg-ink-50 rounded-lg p-4 border border-ink-200">
          <div>
            <label className="block text-xs sm:text-sm font-medium text-ink-700 mb-1">
              Amendment Type <span className="text-critical-500">*</span>
            </label>
            <select
              name="amendmentType"
              value={formData.amendmentType}
              onChange={handleAmendmentTypeChange}
              className="ds-input focus:border-brandred bg-white"
              required
              disabled={loading || (isExisting && formData.status === 2)}
            >
              <option value="">Select Amendment Type</option>
              {amendmentTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-medium text-ink-700 mb-1">
              Effective Bill Cycle <span className="text-critical-500">*</span>
            </label>
            <input
              type="text"
              name="effectiveBillCycle"
              value={formData.effectiveBillCycle}
              onChange={handleInputChange}
              placeholder="Enter bill cycle"
              className="ds-input focus:border-brandred bg-white"
              required
              disabled={loading || (isExisting && formData.status === 2)}
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-medium text-ink-700 mb-1">
              Effective Date <span className="text-critical-500">*</span>
            </label>
            <input
              type="date"
              name="effectiveDate"
              value={formData.effectiveDate}
              onChange={handleInputChange}
              className={`w-full px-3 py-2 border rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-brandred bg-white ${
                dateError
                  ? "border-critical-500 focus:border-critical-500"
                  : "border-ink-300 focus:border-brandred"
              }`}
              required
              disabled={loading || (isExisting && formData.status === 2)}
            />
            {dateError && (
              <p className="text-critical-500 text-xs mt-1">{dateError}</p>
            )}
          </div>
        </div>
        {/* Old Value / New Value Section */}
        <div className="bg-ink-50 rounded-lg p-4 border border-ink-200 mb-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs sm:text-sm font-semibold text-brandred mb-2">
                Old Value
              </label>
              <input
                type="text"
                value={formData.oldValue}
                className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                readOnly
                disabled
                placeholder="Select amendment type to see old value"
              />
            </div>
            <div>
              <label className="block text-xs sm:text-sm font-semibold text-brandred mb-2">
                New Value <span className="text-critical-500">*</span>
              </label>
              {shouldShowTariffSelect ? (
                <select
                  name="newValue"
                  value={formData.newValue}
                  onChange={handleInputChange}
                  className="ds-input focus:border-brandred bg-white"
                  required
                  disabled={
                    loading ||
                    !selectedAmndType ||
                    (isExisting && formData.status === 2)
                  }
                >
                  <option value="">Select tariff</option>
                  {availableTariffs.map((option) => (
                    <option key={option.tariff} value={option.tariff}>
                      {option.tariff} - {option.desc}
                    </option>
                  ))}
                  {formData.newValue &&
                    !availableTariffs.some(
                      (option) => option.tariff === formData.newValue
                    ) && (
                      <option value={formData.newValue}>
                        {formData.newValue}
                      </option>
                    )}
                </select>
              ) : (
                <input
                  type={getNewValueInputType()}
                  name="newValue"
                  value={formData.newValue}
                  onChange={handleInputChange}
                  placeholder={`Enter new value (${ 
                    selectedAmndType?.dtType === "N"
                      ? "Numeric"
                      : selectedAmndType?.dtType === "D"
                      ? "Date"
                      : "Text"
                  })`}
                  className="ds-input focus:border-brandred bg-white"
                  required
                  disabled={
                    loading ||
                    !selectedAmndType ||
                    (isExisting && formData.status === 2)
                  }
                  step={selectedAmndType?.dtType === "N" ? "0.01" : undefined}
                />
              )}
            </div>
          </div>
        </div>
        {/* Action Buttons */}
        <div className="flex flex-wrap gap-2 justify-end pt-4 border-t border-ink-200">
          {!isExisting ? (
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-brandred hover:bg-critical-700 text-white text-sm font-medium rounded-md transition-colors duration-200 flex items-center focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                  Saving...
                </>
              ) : (
                <>
                  <i className="fas fa-save mr-2"></i>
                  Save
                </>
              )}
            </button>
          ) : formData.status === 2 ? (
            <div className="text-sm text-ink-600 py-2">
              Posted amendment - no further action needed.
            </div>
          ) : (
            <button
              type="button"
              onClick={handleEdit}
              disabled={loading}
              className="px-4 py-2 bg-brandred hover:bg-critical-700 text-white text-sm font-medium rounded-md transition-colors duration-200 flex items-center focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                  Saving...
                </>
              ) : (
                <>
                  <i className="fas fa-save mr-2"></i>
                  Save
                </>
              )}
            </button>
          )}
          <button
            type="button"
            onClick={handleCancel}
            disabled={loading}
            className="px-4 py-2 bg-ink-500 hover:bg-ink-600 text-white text-sm font-medium rounded-md transition-colors duration-200 flex items-center focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <i className="fas fa-times mr-2"></i>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};
export default BulkCustomerAmendmentForm;
