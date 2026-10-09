// src/components/BulkCustomersComp/MeterAmendmentForm.js
import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";

const METER_TYPES = ["KVA", "KVAH", "KWD", "KWO", "KWP"];

const MeterAmendmentForm = ({ customer, onClose }) => {
  const [loading, setLoading] = useState(false);
  const [meterDetails, setMeterDetails] = useState([]);
  const [meterReasons, setMeterReasons] = useState([]);
  const [brands, setBrands] = useState([]);
  const [billCycles, setBillCycles] = useState([]);

  const [formData, setFormData] = useState({
    accountNumber: customer?.acc_nbr || "",
    installationId: customer?.inst_id || "",
    currentBillCycle: customer?.bill_cycle || "",
    area: customer?.area_cd || "",
    meterSequence: "",
    oldMtrNbr: "",   // shared across all meter types
    newMtrNbr: "",   // shared across all meter types
    effectiveBillCycle: "",
    effectiveDate: new Date().toISOString().split("T")[0],
    reasonRemoved: "",
    meterRatio: "",
    oldMtrRatio: "",   // old meter's ratio captured at sequence select — sent to R row
    ctRatio: "",
    multiplyingFactor: "1",
    brandCode: "",
  });

  // Per-type reading data (no meter numbers — those are shared)
  const [meterTypeData, setMeterTypeData] = useState({
    KVA:  { mtrOrder: null, oldBaselineRdn: "0", oldPrsntRdn: "0", oldUnts: "0", oldRate: "0", oldAmt: "0", newPrsntRdn: "", newUnts: "0", newRate: "0", newAmt: "0" },
    KVAH: { mtrOrder: null, oldBaselineRdn: "0", oldPrsntRdn: "0", oldUnts: "0", oldRate: "0", oldAmt: "0", newPrsntRdn: "", newUnts: "0", newRate: "0", newAmt: "0" },
    KWD:  { mtrOrder: null, oldBaselineRdn: "0", oldPrsntRdn: "0", oldUnts: "0", oldRate: "0", oldAmt: "0", newPrsntRdn: "", newUnts: "0", newRate: "0", newAmt: "0" },
    KWO:  { mtrOrder: null, oldBaselineRdn: "0", oldPrsntRdn: "0", oldUnts: "0", oldRate: "0", oldAmt: "0", newPrsntRdn: "", newUnts: "0", newRate: "0", newAmt: "0" },
    KWP:  { mtrOrder: null, oldBaselineRdn: "0", oldPrsntRdn: "0", oldUnts: "0", oldRate: "0", oldAmt: "0", newPrsntRdn: "", newUnts: "0", newRate: "0", newAmt: "0" },
  });

  const baseUrl = process.env.REACT_APP_API_BASE_URL;

  // Fetch meter details, reasons, brands, and bill cycles on mount
  useEffect(() => {
    fetchMeterDetails();
    fetchMeterReasons();
    fetchBrands();
    fetchBillCycles();
  }, []);

  // Auto-populate meter data when meter sequence is selected
  useEffect(() => {
    if (!meterDetails.length || !formData.meterSequence) return;

    const metersForSequence = meterDetails.filter(
      (meter) => String(meter.mtrSeq) === String(formData.meterSequence)
    );

    if (metersForSequence.length > 0) {
      const newMeterTypeData = { ...meterTypeData };

      // All types share the same physical meter number — take it from the first match
      const sharedMtrNbr = metersForSequence[0].mtrNbr?.trim() || "";

      metersForSequence.forEach((meter) => {
        const mtrType = meter.mtrType?.trim();
        if (METER_TYPES.includes(mtrType)) {
          newMeterTypeData[mtrType] = {
            mtrOrder: meter.mtrOrder !== null && meter.mtrOrder !== undefined ? meter.mtrOrder : null,
            oldBaselineRdn: meter.prsntRdn !== null && meter.prsntRdn !== undefined ? String(meter.prsntRdn) : "0",
            // AC1 should enter removal-time present reading manually.
            oldPrsntRdn: "",
            oldUnts: "0",
            oldRate: meter.rate !== null && meter.rate !== undefined ? String(meter.rate) : "0",
            oldAmt: "0",
            newPrsntRdn: "",
            newUnts: "0",
            newRate: meter.rate !== null && meter.rate !== undefined ? String(meter.rate) : "0",
            newAmt: "0",
          };
        }
      });

      setMeterTypeData(newMeterTypeData);

      // Set shared meter number + ratios from first meter
      const firstMeter = metersForSequence[0];
      const loadedMtrRatio = firstMeter.mtrRatio?.trim() || "";
      setFormData((prev) => ({
        ...prev,
        oldMtrNbr: sharedMtrNbr,
        newMtrNbr: "",
        // oldMtrRatio is frozen at what the OLD meter had — used for R row
        oldMtrRatio: loadedMtrRatio,
        // meterRatio starts same as old, but user can change it for the NEW meter
        meterRatio: loadedMtrRatio || prev.meterRatio,
        ctRatio: firstMeter.ctRatio?.trim() || prev.ctRatio,
        multiplyingFactor: firstMeter.mFactor !== null && firstMeter.mFactor !== undefined
          ? String(firstMeter.mFactor)
          : prev.multiplyingFactor,
      }));
    }
  }, [meterDetails, formData.meterSequence]);

  // Auto-map effective date to bill cycle from yr_mnth
  useEffect(() => {
    if (!formData.effectiveDate || !billCycles.length) return;

    const date = new Date(formData.effectiveDate + "T00:00:00Z");
    const year = date.getFullYear();
    const month = date.getMonth() + 1;

    const monthNames = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ];
    const monthStr = monthNames[month - 1];
    const yearStr = String(year);

    const matchingCycle = billCycles.find((bc) =>
      bc.label?.includes(monthStr) && bc.label?.includes(yearStr)
    );

    if (matchingCycle && matchingCycle.value !== formData.effectiveBillCycle) {
      setFormData((prev) => ({
        ...prev,
        effectiveBillCycle: matchingCycle.value,
      }));
    }
  }, [formData.effectiveDate, billCycles]);

  // Old previous reading must come from rdngs.rdn for selected account + sequence.
  useEffect(() => {
    if (!formData.accountNumber || !formData.meterSequence) return;
    fetchPreviousBilledReadings();
  }, [formData.accountNumber, formData.meterSequence, formData.oldMtrNbr, formData.effectiveDate]);

  const fetchMeterDetails = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `${baseUrl}/api/v1/meters/customer/${customer.acc_nbr}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
        }
      );
      if (response.ok) {
        const data = await response.json();
        const meters = data.meters || [];
        setMeterDetails(meters);
        setFormData((prev) => ({
          ...prev,
          installationId: data.inst_id || prev.installationId,
          currentBillCycle: data.bill_cycle || prev.currentBillCycle,
          area: data.area_cd || prev.area,
        }));
      }
    } catch (err) {
      console.error("Error fetching meter details:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchMeterReasons = async () => {
    try {
      const response = await fetch(`${baseUrl}/api/v1/meter-reasons`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        setMeterReasons(data.reasons || []);
      }
    } catch (err) {
      console.error("Error fetching meter reasons:", err);
    }
  };

  const fetchBrands = async () => {
    try {
      const response = await fetch(`${baseUrl}/api/v1/meter-brands`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        setBrands(data.brands || []);
      }
    } catch (err) {
      console.error("Error fetching brands:", err);
    }
  };

  const fetchBillCycles = async () => {
    try {
      const response = await fetch(`${baseUrl}/api/v1/yr-mnth`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        const cycles = Array.isArray(data) ? data : [];
        setBillCycles(
          cycles.map((item) => ({
            value: item.bill_cycle,
            label: item.bill_mnth,
          }))
        );
      }
    } catch (err) {
      console.error("Error fetching bill cycles:", err);
    }
  };

  const fetchPreviousBilledReadings = async () => {
    try {
      const params = new URLSearchParams({
        mtrSeq: String(formData.meterSequence),
      });

      if (formData.oldMtrNbr?.trim()) {
        params.set("mtrNbr", formData.oldMtrNbr.trim());
      }

      if (formData.effectiveDate) {
        params.set("effctDate", formData.effectiveDate);
      }

      const response = await fetch(
        `${baseUrl}/api/v1/meters/customer/${formData.accountNumber}/previous-readings?${params.toString()}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
        }
      );

      if (!response.ok) {
        return;
      }

      const data = await response.json();
      const readings = data.readings || {};
      const rates = data.rates || {};

      setMeterTypeData((prev) => {
        const next = { ...prev };

        METER_TYPES.forEach((meterType) => {
          if (!next[meterType]) return;
          const previousFromRdngs = readings[meterType];
          const latestRate = rates[meterType];

          const oldBaselineRdn =
            previousFromRdngs === undefined || previousFromRdngs === null
              ? next[meterType].oldBaselineRdn
              : String(previousFromRdngs);

          const oldPresent = Number(next[meterType].oldPrsntRdn || 0);
          const baseline = Number(oldBaselineRdn || 0);
          const units = oldPresent - baseline;

          next[meterType] = {
            ...next[meterType],
            oldBaselineRdn,
            oldUnts: Number.isFinite(units) && units >= 0 ? String(units) : "0",
            // Auto-fill rates from billed tariff context (as-of effective date)
            oldRate:
              latestRate !== undefined && latestRate !== null
                ? String(latestRate)
                : next[meterType].oldRate,
            newRate:
              latestRate !== undefined && latestRate !== null
                ? String(latestRate)
                : next[meterType].newRate,
          };
        });

        return next;
      });
    } catch (err) {
      console.error("Error fetching previous billed readings:", err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleMeterTypeChange = (meterType, _section, field, value) => {
    setMeterTypeData((prev) => {
      const updated = { ...prev[meterType], [field]: value };

      // Keep units derived for old/new sections independently.
      if (field === "oldPrsntRdn") {
        const present = Number(value || 0);
        const baseline = Number(updated.oldBaselineRdn || 0);
        const units = present - baseline;
        updated.oldUnts = Number.isFinite(units) && units >= 0 ? String(units) : "0";
      }

      if (field === "newPrsntRdn") {
        // Units for the new meter are always 0 at installation — consumption
        // is recorded in the next billing cycle's rdngs entry.
        updated.newUnts = "0";
      }

      return { ...prev, [meterType]: updated };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.newMtrNbr.trim()) {
      toast.error("Please enter the new meter number");
      return;
    }

    // Only include meter types that appear in the existing meter details for this sequence
    const activeMeterTypes = METER_TYPES.filter(
      (type) => meterTypeData[type].oldBaselineRdn !== "0" || meterTypeData[type].mtrOrder !== null
    );

    if (activeMeterTypes.length === 0) {
      toast.error("Please select a meter sequence first");
      return;
    }

    const missingOldPresentTypes = activeMeterTypes.filter(
      (type) => meterTypeData[type].oldPrsntRdn === "" || meterTypeData[type].oldPrsntRdn === null || meterTypeData[type].oldPrsntRdn === undefined
    );

    if (missingOldPresentTypes.length > 0) {
      toast.error(`Please enter Old Meter Present Reading for: ${missingOldPresentTypes.join(", ")}`);
      return;
    }

    try {
      setLoading(true);

      // Build meter_types array — meter numbers are shared, only readings differ per type
      const meterTypesArray = activeMeterTypes.map((type) => ({
        mtr_type: type,
        mtr_order: meterTypeData[type].mtrOrder,

        // Backward-compatible fields consumed by current backend paths
        prsnt_rdn: Number(meterTypeData[type].newPrsntRdn || 0),
        prv_rdn: Number(meterTypeData[type].oldPrsntRdn || 0),
        unts: Number(meterTypeData[type].newUnts || 0),
        rate: Number(meterTypeData[type].newRate || 0),
        amt: Number(meterTypeData[type].newAmt || 0),

        // Explicit old/new sections
        old_prv_rdn: Number(meterTypeData[type].oldBaselineRdn || 0),
        old_prsnt_rdn: Number(meterTypeData[type].oldPrsntRdn || 0),
        old_unts: Number(meterTypeData[type].oldUnts || 0),
        old_rate: Number(meterTypeData[type].oldRate || 0),
        old_amt: Number(meterTypeData[type].oldAmt || 0),
        // new_prsnt_rdn = initial reading on the NEW meter's dial at installation
        // → stored as prv_rdn in the A row (the starting baseline for future billing)
        new_prsnt_rdn: Number(meterTypeData[type].newPrsntRdn || 0),
        new_unts: 0,  // always 0 — no consumption on new meter yet
        new_rate: Number(meterTypeData[type].newRate || 0),
        new_amt: Number(meterTypeData[type].newAmt || 0),
      }));

      const payload = {
        acc_nbr: formData.accountNumber,
        area_cd: formData.area,
        added_blcy: String(formData.currentBillCycle || ""),
        effct_blcy: String(formData.effectiveBillCycle || ""),
        effct_date: formData.effectiveDate,
        type_chg: "C",
        old_mtr_nbr: formData.oldMtrNbr,
        new_mtr_nbr: formData.newMtrNbr,
        mtr_seq: Number(formData.meterSequence),
        old_mtr_ratio: formData.oldMtrRatio,  // R row: old meter's original ratio
        mtr_ratio: formData.meterRatio,         // A row: new meter's ratio
        ct_ratio: formData.ctRatio,
        m_factor: Number(formData.multiplyingFactor || 1),
        br_code: formData.brandCode,
        rsn_code: formData.reasonRemoved,
        user_id: sessionStorage.getItem("user_id") || "SYSTEM",
        meter_types: meterTypesArray,
      };

      const response = await fetch(`${baseUrl}/api/v1/meter-amendments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
        body: JSON.stringify(payload),
      });

      const result = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(result.message || "Failed to save meter amendment");
      }

      toast.success("Meter amendment submitted successfully");
      onClose();
    } catch (error) {
      toast.error(error.message || "Failed to submit meter amendment");
    } finally {
      setLoading(false);
    }
  };

  const getUniqueMeterSequences = () => {
    return [...new Set(meterDetails.map((m) => m.mtrSeq).filter((v) => v !== null && v !== undefined))];
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-4 sm:p-6 max-h-[90vh] overflow-y-auto">
      <div className="flex items-center justify-between mb-6 border-b border-ink-300 pb-3">
        <div className="flex items-center">
          <div className="ds-btn ds-btn-primary ds-btn-sm ds-btn-icon mr-3">
            <i className="fas fa-exchange-alt text-sm"></i>
          </div>
          <h3 className="ds-page-title">
            Change of Meter - Folio {customer?.folio_no ?? customer?.folioNo ?? "N/A"}
          </h3>
        </div>
        <button
          onClick={onClose}
          className="text-ink-500 hover:text-ink-700 transition-colors p-1"
          disabled={loading}
        >
          <i className="fas fa-times text-lg"></i>
        </button>
      </div>

      <form onSubmit={handleSubmit}>
        {/* Customer Information Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 bg-ink-50 rounded-lg p-4 border border-ink-200">
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Folio Number
            </label>
            <input
              type="text"
              name="folioNumber"
              value={customer?.folio_no ?? customer?.folioNo ?? "N/A"}
              readOnly
              className="ds-input text-sm bg-ink-100 cursor-not-allowed"
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Installation ID
            </label>
            <input
              type="text"
              name="installationId"
              value={formData.installationId}
              readOnly
              className="ds-input text-sm bg-ink-100 cursor-not-allowed"
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Current Bill Cycle
            </label>
            <input
              type="text"
              name="currentBillCycle"
              value={formData.currentBillCycle}
              readOnly
              className="ds-input text-sm bg-ink-100 cursor-not-allowed"
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Area
            </label>
            <input
              type="text"
              name="area"
              value={formData.area}
              readOnly
              className="ds-input text-sm bg-ink-100 cursor-not-allowed"
            />
          </div>
        </div>

        {/* Meter Configuration Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 bg-navy-50 rounded-lg p-4 border border-navy-200">
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-brandred mb-1">
              Meter Sequence <span className="text-critical-500">*</span>
            </label>
            <select
              name="meterSequence"
              value={formData.meterSequence}
              onChange={handleInputChange}
              className="ds-input"
              required
              disabled={loading}
            >
              <option value="">Select Meter Sequence</option>
              {getUniqueMeterSequences().map((seq) => (
                <option key={seq} value={seq}>
                  Sequence {seq}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-brandred mb-1">
              Old Meter Number
            </label>
            <input
              type="text"
              value={formData.oldMtrNbr}
              readOnly
              placeholder="Auto-populated on sequence select"
              className="ds-input text-sm bg-ink-100 cursor-not-allowed"
            />
          </div>
          <div className="md:col-span-2">
            <label className="block text-xs sm:text-sm font-semibold text-brandred mb-1">
              New Meter Number <span className="text-critical-500">*</span>
            </label>
            <input
              type="text"
              name="newMtrNbr"
              value={formData.newMtrNbr}
              onChange={handleInputChange}
              placeholder="Enter new meter number (applies to all types)"
              className="ds-input"
              required
              disabled={loading}
            />
          </div>
        </div>

        {/* Effective Date & Reason Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Effective Bill Cycle <span className="text-critical-500">*</span>
            </label>
            <select
              name="effectiveBillCycle"
              value={formData.effectiveBillCycle}
              onChange={handleInputChange}
              className="ds-input"
              required
              disabled={loading}
            >
              <option value="">Select Bill Cycle</option>
              {billCycles.map((bc) => (
                <option key={bc.value} value={bc.value}>
                  {bc.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Effective Date <span className="text-critical-500">*</span>
            </label>
            <input
              type="date"
              name="effectiveDate"
              value={formData.effectiveDate}
              onChange={handleInputChange}
              className="ds-input"
              required
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Reason Removed <span className="text-critical-500">*</span>
            </label>
            <select
              name="reasonRemoved"
              value={formData.reasonRemoved}
              onChange={handleInputChange}
              className="ds-input"
              required
              disabled={loading}
            >
              <option value="">Select Reason</option>
              {meterReasons.map((reason) => (
                <option key={reason.rsn_code} value={reason.rsn_code}>
                  {reason.rsn_code} - {reason.rsn_desc}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Brand <span className="text-critical-500">*</span>
            </label>
            <select
              name="brandCode"
              value={formData.brandCode}
              onChange={handleInputChange}
              className="ds-input"
              required
              disabled={loading}
            >
              <option value="">Select Brand</option>
              {brands.map((brand) => (
                <option key={brand.br_code} value={brand.br_code}>
                  {brand.br_code} - {brand.br_desc}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Meter Ratio & Technical Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6 bg-ink-50 rounded-lg p-4 border border-ink-200">
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Meter Ratio
            </label>
            <input
              type="text"
              name="meterRatio"
              value={formData.meterRatio}
              onChange={handleInputChange}
              placeholder="e.g., 400:5"
              className="ds-input"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              CT Ratio
            </label>
            <input
              type="text"
              name="ctRatio"
              value={formData.ctRatio}
              onChange={handleInputChange}
              placeholder="e.g., 800:5"
              className="ds-input"
              disabled={loading}
            />
          </div>
          <div>
            <label className="block text-xs sm:text-sm font-semibold text-ink-700 mb-1">
              Multiplying Factor
            </label>
            <input
              type="number"
              name="multiplyingFactor"
              value={formData.multiplyingFactor}
              onChange={handleInputChange}
              placeholder="e.g., 1"
              className="ds-input"
              disabled={loading}
              step="0.01"
            />
          </div>
        </div>

        {/* Meter Types Section */}
        <div className="mb-6 border border-ink-300 rounded-lg overflow-hidden">
          <div className="bg-brandred text-white p-3 font-semibold text-sm">
            Meter Reading Details - separate sections for Old Meter and New Meter per type
          </div>
          <div className="p-4 space-y-4">
            {METER_TYPES.map((meterType) => {
              const isActive = meterTypeData[meterType].mtrOrder !== null || meterTypeData[meterType].oldBaselineRdn !== "0";
              return (
                <div
                  key={meterType}
                  className={`border rounded-lg p-4 ${isActive ? "border-brandred bg-critical-50" : "border-ink-200 bg-ink-50 opacity-60"}`}
                >
                  <div className="flex items-center gap-2 mb-3">
                    <h4 className="font-bold text-sm text-brandred">{meterType}</h4>
                    {isActive ? (
                      <span className="text-xs bg-success-100 text-success-700 px-2 py-0.5 rounded-full font-medium">Active</span>
                    ) : (
                      <span className="text-xs bg-ink-200 text-ink-500 px-2 py-0.5 rounded-full font-medium">Not in use</span>
                    )}
                  </div>
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                    <div className="border border-ink-200 rounded-lg p-3 bg-ink-50">
                      <div className="text-xs font-bold text-ink-700 mb-3 uppercase tracking-wide">
                        Old Meter
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-ds-3">
                        <div>
                          <label className="ds-label-field">
                            Old Meter Number
                          </label>
                          <input
                            type="text"
                            value={formData.oldMtrNbr}
                            readOnly
                            className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Previous Reading
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].oldBaselineRdn}
                            readOnly
                            className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Present Reading
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].oldPrsntRdn}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "old", "oldPrsntRdn", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Units
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].oldUnts}
                            readOnly
                            className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Rate
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].oldRate}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "old", "oldRate", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Amount
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].oldAmt}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "old", "oldAmt", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                      </div>
                    </div>

                    <div className="border border-brandred/20 rounded-lg p-3 bg-white">
                      <div className="text-xs font-bold text-brandred mb-3 uppercase tracking-wide">
                        New Meter
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-ds-3">
                        <div>
                          <label className="ds-label-field">
                            New Meter Number
                          </label>
                          <input
                            type="text"
                            value={formData.newMtrNbr}
                            readOnly
                            className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                            placeholder="Enter above in Meter Configuration"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Initial Reading
                            <span className="ml-1 text-ink-400 font-normal" title="Reading shown on the new meter's dial at the time of installation. Stored as the starting baseline for next billing.">
                              (at installation)
                            </span>
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].newPrsntRdn}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "new", "newPrsntRdn", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Units
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].newUnts}
                            readOnly
                            className="ds-input text-sm bg-ink-100 cursor-not-allowed"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Rate
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].newRate}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "new", "newRate", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                        <div>
                          <label className="ds-label-field">
                            Amount
                          </label>
                          <input
                            type="number"
                            value={meterTypeData[meterType].newAmt}
                            onChange={(e) =>
                              handleMeterTypeChange(meterType, "new", "newAmt", e.target.value)
                            }
                            placeholder="0"
                            className="ds-input"
                            disabled={loading || !isActive}
                            step="0.01"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            className="px-6 py-2 rounded-lg border border-ink-300 text-ink-700 font-medium hover:bg-ink-50 transition"
            disabled={loading}
          >
            <i className="fas fa-times mr-2"></i>
            Cancel
          </button>
          <button
            type="submit"
            className="px-6 py-2 rounded-lg bg-success-600 text-white font-medium hover:bg-success-700 transition disabled:bg-ink-400"
            disabled={loading}
          >
            <i className="fas fa-check mr-2"></i>
            {loading ? "Submitting..." : "Submit"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default MeterAmendmentForm;
