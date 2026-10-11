// Modified: src/components/AreaAndBill/AreaAndBillDialog.js
//
// Optional dialog opened from the header bar to narrow the data to one permitted area.
//
// The cascade is strictly sequential: region -> province -> area. Each level is only
// offered once the level above it has been chosen, and Apply stays disabled until a
// complete region/province/area path exists. Every option is drawn from the permitted
// areas the backend resolved from the sec_info region/province/area codes, so no
// unauthorised area can be reached through the UI.
import React, { useEffect, useMemo, useState } from "react";
import { useAreaAndBill } from "../../context/AreaAndBillContext";
import { normalizeAreaCode } from "../../services/AreaAndBillService";

const SCOPE_ALL = "all";
const SCOPE_AREA = "area";

const codeOf = (value) => (value == null ? "" : String(value).trim());

export default function AreaAndBillDialog() {
  const { data, isDialogOpen, closeDialog, selectArea, loading, error } =
    useAreaAndBill();

  const permittedAreas = useMemo(
    () => (Array.isArray(data?.permittedAreas) ? data.permittedAreas : []),
    [data?.permittedAreas]
  );
  const selectedAreaCode = data?.selectedAreaCode || "";

  const [scope, setScope] = useState(SCOPE_ALL);
  const [region, setRegion] = useState("");
  const [province, setProvince] = useState("");
  const [areaCode, setAreaCode] = useState("");

  const equalsCode = (a, b) => {
    const left = normalizeAreaCode(a);
    const right = normalizeAreaCode(b);
    return left !== "" && left === right;
  };

  // Regions present in the permitted set, with how many areas sit under each.
  const regions = useMemo(() => {
    const byCode = new Map();
    permittedAreas.forEach((area) => {
      const regionCode = codeOf(area.region_code);
      if (!regionCode) return;
      if (!byCode.has(regionCode)) {
        byCode.set(regionCode, { code: regionCode, areaCount: 0 });
      }
      byCode.get(regionCode).areaCount += 1;
    });
    return Array.from(byCode.values()).sort((a, b) =>
      a.code.localeCompare(b.code, undefined, { numeric: true })
    );
  }, [permittedAreas]);

  // Provinces inside the chosen region only.
  const provinces = useMemo(() => {
    if (!region) return [];
    const byCode = new Map();
    permittedAreas.forEach((area) => {
      if (codeOf(area.region_code) !== region) return;
      const provinceCode = codeOf(area.province_code);
      if (!provinceCode) return;
      if (!byCode.has(provinceCode)) {
        byCode.set(provinceCode, {
          code: provinceCode,
          name: codeOf(area.province_name) || provinceCode,
          areaCount: 0,
        });
      }
      byCode.get(provinceCode).areaCount += 1;
    });
    return Array.from(byCode.values()).sort((a, b) =>
      a.code.localeCompare(b.code, undefined, { numeric: true })
    );
  }, [permittedAreas, region]);

  // Areas inside the chosen province only.
  const areasInProvince = useMemo(() => {
    if (!region || !province) return [];
    return permittedAreas.filter(
      (area) =>
        codeOf(area.region_code) === region &&
        codeOf(area.province_code) === province
    );
  }, [permittedAreas, region, province]);

  // Reopening re-seeds the cascade from the area currently in effect.
  useEffect(() => {
    if (!isDialogOpen) return;

    if (!selectedAreaCode) {
      setScope(SCOPE_ALL);
      setRegion("");
      setProvince("");
      setAreaCode("");
      return;
    }

    const current = permittedAreas.find((area) =>
      equalsCode(area.area_code, selectedAreaCode)
    );
    setScope(SCOPE_AREA);
    setRegion(current ? codeOf(current.region_code) : "");
    setProvince(current ? codeOf(current.province_code) : "");
    setAreaCode(codeOf(current ? current.area_code : selectedAreaCode));
  }, [isDialogOpen, selectedAreaCode, permittedAreas]);

  // Switching to the aggregate view clears the partial path.
  const handleScopeChange = (nextScope) => {
    setScope(nextScope);
    if (nextScope === SCOPE_ALL) {
      setRegion("");
      setProvince("");
      setAreaCode("");
    }
  };

  // Choosing a region invalidates the province and area below it.
  const handleRegionChange = (event) => {
    setRegion(event.target.value);
    setProvince("");
    setAreaCode("");
  };

  // Choosing a province invalidates the area below it.
  const handleProvinceChange = (event) => {
    setProvince(event.target.value);
    setAreaCode("");
  };

  const handleApply = async () => {
    await selectArea(scope === SCOPE_ALL ? null : areaCode);
    closeDialog();
  };

  if (!isDialogOpen) return null;

  const hasPermittedAreas = permittedAreas.length > 0;

  // A complete region -> province -> area path is required before applying.
  const completePath =
    scope === SCOPE_ALL || (!!region && !!province && !!areaCode);
  const canApply = hasPermittedAreas && completePath && !loading;

  const selectedArea = permittedAreas.find((area) =>
    equalsCode(area.area_code, areaCode)
  );
  const step1Done = !!region;
  const step2Done = !!province;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center md:pl-80">
      <div className="absolute inset-0 bg-black/45" />

      <div className="relative bg-white rounded-lg shadow-lg w-full max-w-3xl mx-4 pointer-events-auto max-h-[90vh] overflow-hidden flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b">
          <div>
            <h3 className="text-lg font-semibold text-ink-800">
              Select Area To Load Bill Cycle
            </h3>
            <p className="text-xs text-ink-500 mt-0.5">
              You have {permittedAreas.length} permitted area
              {permittedAreas.length === 1 ? "" : "s"}. Pick a region, then a
              province, then an area.
            </p>
          </div>
          <button
            type="button"
            onClick={closeDialog}
            className="text-ink-400 hover:text-ink-700 hover:bg-ink-100 w-8 h-8 rounded-full flex items-center justify-center transition-colors focus:outline-none cursor-pointer"
            title="Close dialog"
          >
            <i className="fas fa-times text-base"></i>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1">
          {!hasPermittedAreas ? (
            <div className="text-center py-8 text-gray-500">
              {loading
                ? "Loading your permitted areas..."
                : "No areas are configured for your account."}
            </div>
          ) : (
            <>
              {/* Scope choice: aggregate default, or narrow to one area */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
                <button
                  type="button"
                  onClick={() => handleScopeChange(SCOPE_ALL)}
                  className={`text-left px-4 py-3 rounded-lg border transition-colors cursor-pointer ${
                    scope === SCOPE_ALL
                      ? "border-[#002244] bg-[#002244]/5"
                      : "border-slate-200 hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-slate-800">
                        All permitted areas
                      </div>
                      <div className="text-xs text-slate-500">
                        Aggregated data across all {permittedAreas.length}
                      </div>
                    </div>
                    {scope === SCOPE_ALL && (
                      <i className="fas fa-check text-[#002244]"></i>
                    )}
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => handleScopeChange(SCOPE_AREA)}
                  className={`text-left px-4 py-3 rounded-lg border transition-colors cursor-pointer ${
                    scope === SCOPE_AREA
                      ? "border-[#002244] bg-[#002244]/5"
                      : "border-slate-200 hover:bg-slate-50"
                  }`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="min-w-0">
                      <div className="text-sm font-semibold text-slate-800">
                        One specific area
                      </div>
                      <div className="text-xs text-slate-500">
                        Narrow the data to a single area
                      </div>
                    </div>
                    {scope === SCOPE_AREA && (
                      <i className="fas fa-check text-[#002244]"></i>
                    )}
                  </div>
                </button>
              </div>

              {scope === SCOPE_AREA && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
                  {/* Step 1 - region */}
                  <div>
                    <label
                      htmlFor="area-cascade-region"
                      className="block text-sm font-medium text-slate-700 mb-1"
                    >
                      <span className="inline-flex items-center gap-1.5">
                        <span
                          className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-xs font-bold ${
                            step1Done
                              ? "bg-emerald-100 text-emerald-700"
                              : "bg-[#002244] text-white"
                          }`}
                        >
                          {step1Done ? <i className="fas fa-check text-[10px]"></i> : "1"}
                        </span>
                        Region
                        <span className="text-red-500">*</span>
                      </span>
                    </label>
                    <select
                      id="area-cascade-region"
                      value={region}
                      onChange={handleRegionChange}
                      className="w-full border border-slate-300 rounded px-3 py-2 text-sm disabled:bg-slate-100 disabled:text-slate-400"
                    >
                      <option value="">-- Select Region --</option>
                      {regions.map((item) => (
                        <option key={item.code} value={item.code}>
                          Region {item.code} ({item.areaCount} areas)
                        </option>
                      ))}
                    </select>
                  </div>

                  {/* Step 2 - province, gated on step 1 */}
                  <div>
                    <label
                      htmlFor="area-cascade-province"
                      className="block text-sm font-medium text-slate-700 mb-1"
                    >
                      <span className="inline-flex items-center gap-1.5">
                        <span
                          className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-xs font-bold ${
                            step2Done
                              ? "bg-emerald-100 text-emerald-700"
                              : step1Done
                              ? "bg-[#002244] text-white"
                              : "bg-slate-200 text-slate-500"
                          }`}
                        >
                          {step2Done ? <i className="fas fa-check text-[10px]"></i> : "2"}
                        </span>
                        Province
                        <span className="text-red-500">*</span>
                      </span>
                    </label>
                    <select
                      id="area-cascade-province"
                      value={province}
                      onChange={handleProvinceChange}
                      disabled={!step1Done}
                      className="w-full border border-slate-300 rounded px-3 py-2 text-sm disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed"
                    >
                      <option value="">
                        {step1Done ? "-- Select Province --" : "Select a region first"}
                      </option>
                      {provinces.map((item) => (
                        <option key={item.code} value={item.code}>
                          {item.name} ({item.code}) ({item.areaCount} areas)
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              )}

              {scope === SCOPE_AREA && (
                <>
                  {/* Step 3 - areas within the chosen province */}
                  <div className="mb-2 flex items-center gap-1.5">
                    <span
                      className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-xs font-bold ${
                        areaCode
                          ? "bg-emerald-100 text-emerald-700"
                          : step2Done
                          ? "bg-[#002244] text-white"
                          : "bg-ink-200 text-ink-500"
                      }`}
                    >
                      {areaCode ? <i className="fas fa-check text-[10px]"></i> : "3"}
                    </span>
                    <span className="text-sm font-medium text-ink-700">
                      Area
                      <span className="text-red-500"> *</span>
                    </span>
                    {step2Done && (
                      <span className="text-xs text-ink-500">
                        {areasInProvince.length} area
                        {areasInProvince.length === 1 ? "" : "s"} in this province
                      </span>
                    )}
                  </div>

                  {!step2Done ? (
                    <div className="text-center py-6 text-ink-500 text-sm border border-dashed border-ink-300 rounded-lg">
                      Select a region and province to list its areas.
                    </div>
                  ) : (
                    <div className="border border-ink-200 rounded-lg overflow-hidden divide-y divide-ink-100">
                      {areasInProvince.map((area) => {
                        const isSelected = equalsCode(
                          area.area_code,
                          areaCode
                        );
                        return (
                          <button
                            type="button"
                            key={area.area_code}
                            onClick={() => setAreaCode(codeOf(area.area_code))}
                            className={`w-full text-left px-4 py-3 flex items-center justify-between gap-4 transition-colors cursor-pointer ${
                              isSelected
                                ? "bg-navy-100/60"
                                : "hover:bg-navy-50/70"
                            }`}
                          >
                            <div className="min-w-0 flex items-center gap-3">
                              <input
                                type="radio"
                                checked={isSelected}
                                onChange={() =>
                                  setAreaCode(codeOf(area.area_code))
                                }
                                className="cursor-pointer flex-none"
                                aria-label={`Select area ${codeOf(area.area_code)}`}
                              />
                              <div className="min-w-0">
                                <div className="text-sm font-semibold text-ink-800">
                                  {codeOf(area.area_name) ||
                                    `Area ${codeOf(area.area_code)}`}
                                </div>
                                <div className="text-xs text-ink-500">
                                  Code {codeOf(area.area_code)}
                                </div>
                              </div>
                            </div>
                            <span
                              className={`flex-none px-2 py-1 rounded text-xs ${
                                area.has_bill_cycle
                                  ? "bg-success-100 text-success-800"
                                  : "bg-ink-100 text-ink-800"
                              }`}
                            >
                              {area.active_bill_cycle || "No Cycle"}
                            </span>
                          </button>
                        );
                      })}
                    </div>
                  )}
                </>
              )}
            </>
          )}

          {error && <div className="mt-4 text-sm text-critical-600">{error}</div>}
        </div>

        <div className="flex items-center justify-between px-6 py-4 border-t bg-ink-50 gap-4">
          <span className="text-xs text-ink-500">
            {scope === SCOPE_ALL
              ? `Will show all ${permittedAreas.length} permitted areas`
              : selectedArea
              ? `Will show ${
                  codeOf(selectedArea.area_name) ||
                  `Area ${codeOf(selectedArea.area_code)}`
                }`
              : "Complete all three steps to apply"}
          </span>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={closeDialog}
              className="px-4 py-2 rounded text-sm border border-ink-300 text-ink-700 hover:bg-ink-100 cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleApply}
              disabled={!canApply}
              className="bg-success-600 text-white px-4 py-2 rounded text-sm hover:bg-success-700 focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            >
              {loading ? "Applying..." : "Apply"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}