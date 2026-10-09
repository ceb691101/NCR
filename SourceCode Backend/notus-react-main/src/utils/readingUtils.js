// src/utils/readingUtils.js

/**
 * Calculates the signed difference between total energy export (RU/KWT units) 
 * and the sum of individual interval readings (KWD + KWP + KWO units).
 * 
 * Formula: signedRuDifference = KWT_units - (KWD_units + KWP_units + KWO_units)
 * 
 * Supports both signatures for backward compatibility/flexibility:
 * 1. calculateRuDifference(r1, r2, r3, total)
 * 2. calculateRuDifference({ r1, r2, r3, total })
 *
 * @param {number|string|object} r1 - R1 (KWD) units value OR object containing {r1, r2, r3, total}
 * @param {number|string} [r2] - R2 (KWP) units value
 * @param {number|string} [r3] - R3 (KWO) units value
 * @param {number|string} [total] - RU/KWT units value
 * @returns {number|null} The signed difference, or null if total or interval data is missing.
 */
export function calculateRuDifference(r1, r2, r3, total) {
  if (typeof r1 === "object" && r1 !== null && r2 === undefined) {
    total = r1.total ?? r1.kwtUnits;
    r3 = r1.r3 ?? r1.kwoUnits;
    r2 = r1.r2 ?? r1.kwpUnits;
    r1 = r1.r1 ?? r1.kwdUnits;
  }

  if (total === null || total === undefined || total === "") return null;
  
  const valR1 = r1 != null && r1 !== "" ? parseFloat(r1) : 0;
  const valR2 = r2 != null && r2 !== "" ? parseFloat(r2) : 0;
  const valR3 = r3 != null && r3 !== "" ? parseFloat(r3) : 0;
  const valTotal = parseFloat(total);

  if (isNaN(valTotal) || isNaN(valR1) || isNaN(valR2) || isNaN(valR3)) return null;

  const sumOfIntervals = valR1 + valR2 + valR3;
  return valTotal - sumOfIntervals;
}

/**
 * Checks whether the calculated RU difference is within the developer's acceptable RU limit.
 * 
 * Boundary condition: Math.abs(difference) <= acceptRu (inclusive boundary).
 *
 * @param {number|null|undefined} difference - The calculated signed RU difference
 * @param {number|string|null|undefined} acceptRu - The developer's allowed RU difference
 * @returns {boolean} True if within limit, false otherwise (or if unconfigured/null)
 */
export function isRuWithinLimit(difference, acceptRu) {
  if (difference === null || difference === undefined || isNaN(Number(difference))) {
    return false;
  }
  if (acceptRu === null || acceptRu === undefined || acceptRu === "" || isNaN(Number(acceptRu))) {
    return false;
  }
  return Math.abs(Number(difference)) <= Number(acceptRu);
}

