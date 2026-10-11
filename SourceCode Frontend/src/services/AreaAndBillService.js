// Modified: src/services/AreaAndBillService.js
//
// Area permissions come from the backend, which resolves them from the
// region_code / province_code / area_code columns in dbadmin.sec_info:
//   region_code NULL                       -> every area
//   region set, province_code NULL         -> every area in that region
//   region + province set, area_code NULL  -> every area in that province
//   region + province + area set           -> only that area
//
// Nothing is selected at login. The default state is "all permitted areas";
// the header bar may narrow it down to one permitted area.
const STORAGE_KEY = "ceb_area_bill";

export const ALL_AREAS = "ALL";

export function getAreaAndBill() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
}

export function setAreaAndBill(payload) {
  try {
    const previousData = getAreaAndBill();
    const previousSelectedArea = previousData
      ? previousData.selectedAreaCode
      : null;

    const dataToStore = {
      userCategory: payload.userCategory || "",
      // Raw sec_info codes. A blank value means "all of the level below".
      regionCode: payload.regionCode || "",
      provinceCode: payload.provinceCode || "",
      areaCode: payload.areaCode || "",
      accessScope: payload.accessScope || "",
      // Every area this user may read, with its active bill cycle.
      permittedAreas: payload.permittedAreas || [],
      // "" means all permitted areas (the default). A code means that single area.
      selectedAreaCode: payload.selectedAreaCode || "",
      timestamp: new Date().toISOString(),
    };

    localStorage.setItem(STORAGE_KEY, JSON.stringify(dataToStore));

    if (previousSelectedArea !== dataToStore.selectedAreaCode) {
      window.dispatchEvent(
        new CustomEvent("areaAndBill:changed", {
          detail: {
            previousArea: previousSelectedArea,
            currentArea: dataToStore.selectedAreaCode,
            data: dataToStore,
          },
        })
      );
    }

    return true;
  } catch (e) {
    console.error("Failed to save area and bill data:", e);
    return false;
  }
}

export function clearAreaAndBill() {
  try {
    const previousData = getAreaAndBill();
    localStorage.removeItem(STORAGE_KEY);

    window.dispatchEvent(new CustomEvent("areaAndBill:clear"));

    if (previousData && previousData.selectedAreaCode) {
      window.dispatchEvent(
        new CustomEvent("areaAndBill:changed", {
          detail: {
            previousArea: previousData.selectedAreaCode,
            currentArea: null,
            data: null,
          },
        })
      );
    }
  } catch (e) {
    // ignore
  }
}

// Helper function to get user's location info from stored data
export function getUserLocationFromStorage() {
  try {
    const data = getAreaAndBill();
    if (!data) return null;

    return {
      userCategory: data.userCategory,
      regionCode: data.regionCode,
      provinceCode: data.provinceCode,
      areaCode: data.areaCode,
      accessScope: data.accessScope,
      displayLabel: data.displayLabel,
      displayValue: data.displayValue,
    };
  } catch (e) {
    return null;
  }
}

/**
 * Every area the logged in user is permitted to read.
 * @returns {Array<{area_code: string, area_name: string, region_code: string,
 *                   province_code: string, province_name: string,
 *                   active_bill_cycle: number, has_bill_cycle: boolean}>}
 */
export function getPermittedAreas() {
  try {
    const data = getAreaAndBill();
    if (!data || !Array.isArray(data.permittedAreas)) return [];
    return data.permittedAreas;
  } catch (e) {
    return [];
  }
}

export function getPermittedAreaCodes() {
  return getPermittedAreas()
    .map((area) => area.area_code)
    .filter(Boolean);
}

/**
 * The area narrowed to in the header bar, or null when all permitted areas are shown.
 */
export function getSelectedAreaCode() {
  try {
    const data = getAreaAndBill();
    if (!data) return null;
    const code = data.selectedAreaCode;
    if (!code || code === ALL_AREAS) return null;
    return code;
  } catch (e) {
    return null;
  }
}

/** True when no specific area was chosen, i.e. all permitted areas are in scope. */
export function isAllAreasSelected() {
  return getSelectedAreaCode() === null;
}

/**
 * The areas whose data should currently be loaded: the selected area when the user
 * narrowed the scope, otherwise every permitted area.
 */
export function getEffectiveAreaCodes() {
  const selected = getSelectedAreaCode();
  if (!selected) return getPermittedAreaCodes();
  return [selected];
}

/**
 * Label for the header pill.
 */
export function getAreaScopeLabel() {
  const selected = getSelectedAreaCode();
  if (!selected) {
    const permitted = getPermittedAreas();
    if (permitted.length === 1) return `Area ${permitted[0].area_code}`;
    return `All Areas (${permitted.length})`;
  }
  return `Area ${selected}`;
}

/**
 * Helper function to get bill cycles from stored data.
 * Kept for callers that used to read the pre-permission field name.
 */
export function getBillCyclesFromStorage() {
  return getPermittedAreas();
}

// Helper function to check if user has access to specific location.
// Access is checked against the permitted area list resolved by the backend.
export function hasLocationAccess(
  targetRegionCode,
  targetProvinceCode,
  targetAreaCode
) {
  try {
    if (!targetAreaCode) return false;
    return getPermittedAreaCodes().some(
      (code) => normalizeAreaCode(code) === normalizeAreaCode(targetAreaCode)
    );
  } catch (e) {
    return false;
  }
}

// Helper function to get bill cycle for a specific area
export function getBillCycleForArea(areaCode) {
  try {
    if (!areaCode) return null;
    const target = normalizeAreaCode(areaCode);
    const match = getPermittedAreas().find(
      (area) => normalizeAreaCode(area.area_code) === target
    );
    return match ? match.active_bill_cycle : null;
  } catch (e) {
    return null;
  }
}

// Helper function to get areas with active bill cycles
export function getAreasWithActiveBillCycles() {
  try {
    return getPermittedAreas().filter(
      (cycle) => cycle.has_bill_cycle && cycle.active_bill_cycle
    );
  } catch (e) {
    return [];
  }
}

// Helper function to get bill cycle summary
export function getBillCycleSummary() {
  try {
    const permittedAreas = getPermittedAreas();
    const totalAreas = permittedAreas.length;
    const areasWithCycles = permittedAreas.filter(
      (cycle) => cycle.has_bill_cycle
    ).length;
    const areasWithoutCycles = totalAreas - areasWithCycles;

    return {
      totalAreas,
      areasWithCycles,
      areasWithoutCycles,
      billCycles: permittedAreas,
    };
  } catch (e) {
    return {
      totalAreas: 0,
      areasWithCycles: 0,
      areasWithoutCycles: 0,
      billCycles: [],
    };
  }
}

// Helper function to get permitted areas grouped by region
export function getBillCyclesByRegion() {
  try {
    const groupedByRegion = {};
    getPermittedAreas().forEach((cycle) => {
      const regionCode = cycle.region_code || "UNKNOWN";
      if (!groupedByRegion[regionCode]) {
        groupedByRegion[regionCode] = [];
      }
      groupedByRegion[regionCode].push(cycle);
    });
    return groupedByRegion;
  } catch (e) {
    return {};
  }
}

// Helper function to get permitted areas grouped by province
export function getBillCyclesByProvince() {
  try {
    const groupedByProvince = {};
    getPermittedAreas().forEach((cycle) => {
      const provinceKey = `${cycle.province_code || "UNKNOWN"}_${
        cycle.province_name || "Unknown"
      }`;
      if (!groupedByProvince[provinceKey]) {
        groupedByProvince[provinceKey] = [];
      }
      groupedByProvince[provinceKey].push(cycle);
    });
    return groupedByProvince;
  } catch (e) {
    return {};
  }
}

/**
 * Area codes are CHAR(2) in the database ("01") but arrive as "1" from some screens,
 * so they are compared on their numeric form. This must match the backend's
 * UserAreaPermissionService.normalizeAreaCode, otherwise a permitted area like "01"
 * never matches the same area referred to as "1" and the selection silently fails.
 */
export function normalizeAreaCode(areaCode) {
  if (areaCode === null || areaCode === undefined) return "";
  const trimmed = String(areaCode).trim();
  if (trimmed === "") return "";
  const stripped = trimmed.replace(/^0+/, "");
  // "00" and "0" both collapse to "0" rather than the empty string.
  return stripped === "" ? "0" : stripped;
}

/**
 * Matches two area codes ignoring leading zeros and CHAR padding.
 */
export function matchAreaCode(a, b) {
  const s1 = normalizeAreaCode(a);
  const s2 = normalizeAreaCode(b);
  return !!s1 && !!s2 && s1 === s2;
}

/**
 * Whether the stored data is usable. Age is intentionally not enforced: the permitted
 * areas are refreshed from the backend on login and on session validation.
 */
export function validateAreaData() {
  try {
    const data = getAreaAndBill();
    if (!data) return { valid: false, reason: "No data stored" };
    if (!Array.isArray(data.permittedAreas)) {
      return { valid: false, reason: "Missing or invalid permitted areas array" };
    }
    return { valid: true };
  } catch (e) {
    return { valid: false, reason: "Error validating data: " + e.message };
  }
}
