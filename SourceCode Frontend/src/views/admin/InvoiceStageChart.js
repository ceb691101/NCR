// src/views/admin/InvoiceStageChart.js
import React, { useCallback, useEffect, useState } from "react";
import InvoiceStageBarChartCard from "components/Cards/InvoiceStageBarChartCard.js";
import { getInvoices } from "services/invoiceService";
import { getCurrentOpenBillCycle } from "services/billCycleEndingService";
import {
  getSelectedAreaCode,
  getPermittedAreas,
  matchAreaCode,
} from "services/AreaAndBillService";
import { toast } from "react-toastify";

/* Bill cycles can arrive as a number or a zero-padded CHAR, so compare on the
   trimmed string form rather than with ===. */
const sameCycle = (a, b) =>
  a !== null && a !== undefined && String(a).trim() !== "" &&
  b !== null && b !== undefined && String(b).trim() !== "" &&
  String(a).trim() === String(b).trim();

/* Only these four values are actionable stages. Anything else the enum may carry
   (NONE, or an unexpected future value) has not been submitted for review, so it
   is folded into DRAFT rather than silently dropped from the total. */
const normaliseStatus = (raw) => {
  const status = String(raw || "").trim().toUpperCase();
  if (status === "RECOMMEND" || status === "APPROVE" || status === "FINALIZE") {
    return status;
  }
  if (status === "REJECTED") return "REJECTED";
  return "DRAFT";
};

const codeOf = (value) => (value == null ? "" : String(value).trim());

/* Distinct regions represented in the permitted set, used for the default label. */
const countRegions = (areas) =>
  new Set(areas.map((a) => codeOf(a.region_code)).filter(Boolean)).size;

const InvoiceStageChart = () => {
  const [counts, setCounts] = useState({});
  const [total, setTotal] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const [scopeLabel, setScopeLabel] = useState("");
  const [scopeTitle, setScopeTitle] = useState("");
  const [cycleLabel, setCycleLabel] = useState("");
  const [cycleText, setCycleText] = useState("");
  const [scopedTotal, setScopedTotal] = useState(0);
  const [areasWithoutCycle, setAreasWithoutCycle] = useState(0);

  const loadStages = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      /* ── 1. Resolve the scope ──────────────────────────────────────────
         Nothing selected is the default and means every permitted area,
         which normally spans all regions. A selection is always exactly one
         area; its region and province are then read back off the permitted
         list so the header can show the full path the user navigated. */
      const permittedAreas = getPermittedAreas();
      const selectedAreaCode = getSelectedAreaCode();

      const scopeAreas = selectedAreaCode
        ? permittedAreas.filter((a) => matchAreaCode(a.area_code, selectedAreaCode))
        : permittedAreas;

      if (scopeAreas.length === 0) {
        setScopeLabel("");
        setScopeTitle("");
        setCycleLabel("No scope");
        setCounts({});
        setTotal(0);
        setScopedTotal(0);
        setAreasWithoutCycle(0);
        return;
      }

      if (selectedAreaCode) {
        const area = scopeAreas[0];
        const areaName = codeOf(area.area_name) || `Area ${codeOf(area.area_code)}`;
        const regionName = codeOf(area.region_code)
          ? `Region ${codeOf(area.region_code)}`
          : "";
        const provinceName = codeOf(area.province_name) || codeOf(area.province_code);
        setScopeLabel(areaName);
        setScopeTitle(
          [regionName, provinceName, areaName].filter(Boolean).join("  ›  ")
        );
      } else if (permittedAreas.length === 1) {
        const area = permittedAreas[0];
        const areaName = codeOf(area.area_name) || `Area ${codeOf(area.area_code)}`;
        setScopeLabel(areaName);
        setScopeTitle(areaName);
      } else {
        const regions = countRegions(scopeAreas);
        setScopeLabel(`All ${permittedAreas.length} areas`);
        setScopeTitle(
          `All permitted areas across ${regions} region${regions === 1 ? "" : "s"}`
        );
      }

      /* Use the global active cycle from ncre_bill_cycle for every area in
         scope. Never fall back to area cycles or combine historical cycles. */
      const current = await getCurrentOpenBillCycle();
      const cycle = current?.bill_cycle ?? current?.billCycle;
      if (cycle === null || cycle === undefined || String(cycle).trim() === "") {
        throw new Error("The current active bill cycle is unavailable.");
      }
      setCycleLabel(`Cycle ${codeOf(cycle)}`);
      setCycleText(`cycle ${codeOf(cycle)}`);

      const scopeCodes = scopeAreas.map((a) => a.area_code).filter(Boolean);

      /* ── 3. Load invoices and keep only the current cycle ───────────────
         GET /api/v1/invoice returns every cycle, so the cycle filter is
         applied here rather than relying on the endpoint. */
      const response = await getInvoices();
      const invoices = Array.isArray(response) ? response : [];

      const inScope = invoices.filter((invoice) => {
        if (!invoice || !invoice.areaCode) return false;
        return scopeCodes.some((code) => matchAreaCode(invoice.areaCode, code));
      });
      setScopedTotal(inScope.length);

      const currentCycleInvoices = inScope.filter((invoice) =>
        sameCycle(invoice.billCycle, cycle)
      );
      setAreasWithoutCycle(0);

      const tally = {
        DRAFT: 0,
        RECOMMEND: 0,
        APPROVE: 0,
        FINALIZE: 0,
        REJECTED: 0,
      };
      currentCycleInvoices.forEach((invoice) => {
        tally[normaliseStatus(invoice.status)] += 1;
      });

      setCounts(tally);
      setTotal(currentCycleInvoices.length);
    } catch (err) {
      console.error("Error loading invoice stages:", err);
      setCycleLabel("Cycle unavailable");
      setCounts({});
      setTotal(0);
      setError(
        err && err.message ? err.message : "The invoice list request did not complete."
      );
      if (!window.__userLoggingOut) toast.error("Failed to load invoice stages");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStages();
  }, [loadStages]);

  // Reload whenever the header area / bill cycle selection changes.
  useEffect(() => {
    const handleAreaChange = () => loadStages();
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => window.removeEventListener("areaAndBill:changed", handleAreaChange);
  }, [loadStages]);

  // Track the logout flag so a torn-down session does not raise a toast.
  useEffect(() => {
    const handleUserLoggingOut = () => {
      try {
        window.__userLoggingOut = true;
      } catch (e) {
        /* ignore */
      }
    };
    window.addEventListener("user:loggingOut", handleUserLoggingOut);
    return () => window.removeEventListener("user:loggingOut", handleUserLoggingOut);
  }, []);

  return (
    <div className="h-full min-w-0 md:col-span-2 xl:col-span-1">
      <InvoiceStageBarChartCard
        counts={counts}
        total={total}
        isLoading={isLoading}
        error={error}
        scopeLabel={scopeLabel}
        scopeTitle={scopeTitle}
        cycleLabel={cycleLabel}
        cycleText={cycleText}
        scopedTotal={scopedTotal}
        areasWithoutCycle={areasWithoutCycle}
        onRetry={loadStages}
      />
    </div>
  );
};

export default InvoiceStageChart;