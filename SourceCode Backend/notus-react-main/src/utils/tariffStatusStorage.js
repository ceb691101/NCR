/**
 * Tariff Status Storage Utility
 * Manages Active/Inactive status for tariffs in browser localStorage
 *
 * Storage Format: {"11": "A", "13": "I", "14": "A", ...}
 * Status Values: "A" = Active, "I" = Inactive
 */

const STORAGE_KEY = 'tariff_status_map';

/**
 * Initialize tariff statuses for all provided tariff IDs
 * Sets default "A" (Active) status for any tariff IDs not already in storage
 *
 * @param {Array<number|string>} tariffIds - Array of tariff IDs to initialize
 */
export const initializeTariffStatus = (tariffIds) => {
  if (!Array.isArray(tariffIds) || tariffIds.length === 0) {
    return;
  }

  const currentStatuses = getAllTariffStatuses();
  let hasChanges = false;

  tariffIds.forEach((tariffId) => {
    const id = String(tariffId);
    if (!currentStatuses[id]) {
      currentStatuses[id] = 'A'; // Default to Active
      hasChanges = true;
    }
  });

  if (hasChanges) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(currentStatuses));
  }
};

/**
 * Retrieve status for a specific tariff
 *
 * @param {number|string} tariffId - The tariff ID to retrieve
 * @returns {string} Status: "A" (Active) or "I" (Inactive), defaults to "A"
 */
export const getTariffStatus = (tariffId) => {
  const statuses = getAllTariffStatuses();
  return statuses[String(tariffId)] || 'A';
};

/**
 * Update status for a specific tariff
 *
 * @param {number|string} tariffId - The tariff ID to update
 * @param {string} status - New status: "A" (Active) or "I" (Inactive)
 */
export const setTariffStatus = (tariffId, status) => {
  if (status !== 'A' && status !== 'I') {
    console.warn(`Invalid status "${status}". Must be "A" or "I"`);
    return;
  }

  const statuses = getAllTariffStatuses();
  statuses[String(tariffId)] = status;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(statuses));
};

/**
 * Get all tariff statuses as an object
 *
 * @returns {Object} Status map: {tariffId: status}
 */
export const getAllTariffStatuses = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : {};
  } catch (error) {
    console.error('Error reading tariff statuses from localStorage:', error);
    return {};
  }
};

/**
 * Clear all tariff statuses from localStorage
 * Used when tariff records are completely removed/cleared
 */
export const clearAllTariffStatuses = () => {
  try {
    localStorage.removeItem(STORAGE_KEY);
    console.log('Cleared all tariff statuses from localStorage');
  } catch (error) {
    console.error('Error clearing tariff statuses from localStorage:', error);
  }
};
