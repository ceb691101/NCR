// src/utils/filterUtils.js

/**
 * Maps the 3-letter NCRE type codes from dbadmin.ncre_type.type_id to the
 * single-letter code stored in the customer table's ncre_type column.
 * This mirrors the backend mapping in NcreDeveloperService and MapView.
 */
const NCRE_TYPE_TO_CUSTOMER_CODE = {
  SPP: "S",
  MHP: "M",
  DPP: "D",
  BMP: "B",
  WPP: "W",
  WHP: "W",
};

/**
 * Convert an NCRE type value to the customer table's single-letter code.
 * Legacy single-letter values (B, M, W, S, D) pass through unchanged.
 */
function toCustomerNcreCode(ncreType) {
  const key = String(ncreType || "").trim().toUpperCase();
  return NCRE_TYPE_TO_CUSTOMER_CODE[key] || key;
}

/**
 * Filter an array of customer objects by area code (case-insensitive, partial match)
 * @param {Array} customers - Array of customer objects with area_cd property
 * @param {string} areaCode - Area code filter string
 * @returns {Array} Filtered customers
 */
export function filterByAreaCode(customers, areaCode) {
  if (!areaCode) return customers;
  const code = areaCode.toLowerCase();
  return customers.filter(cust => (cust.area_cd || "").toLowerCase().includes(code));
}

/**
 * Filter an array of customer objects by ncre_type (exact match on the
 * customer table's single-letter ncre_type code)
 * @param {Array} customers - Array of customer objects with ncre_type property
 * @param {string} ncreType - NCRE type filter value (from dbadmin.ncre_type.type_id)
 * @returns {Array} Filtered customers
 */
export function filterByNcreType(customers, ncreType) {
  if (!ncreType) return customers;
  const code = toCustomerNcreCode(ncreType);
  return customers.filter(cust => String(cust.ncre_type || "").trim().toUpperCase() === code);
}
