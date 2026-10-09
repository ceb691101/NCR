import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/tmp-readings');

// Helper function to handle API responses
const handleApiResponse = async (response) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
    }
    return response.json();
};

// Helper function to create authorization headers
const getAuthHeaders = () => {
    const sessionId = sessionStorage.getItem('session_id') || localStorage.getItem('session_id');
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa('user:admin123'),
    };
    if (sessionId) {
        headers['X-Session-Id'] = sessionId;
    }
    return headers;
};

// Get all temporary readings with bill cycle filter option
export const getAllTempReadings = async (includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        
        const response = await fetch(`${API_BASE_URL}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching all temp readings:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get readings by account number with bill cycle filter option - UPDATED
export const getTempReadingsByAccNbr = async (accNbr, includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        
        const response = await fetch(`${API_BASE_URL}/account/${accNbr}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        
        const result = await handleApiResponse(response);
        
        // FIXED: Return the full response structure including readings array
        if (result.readings) {
            return {
                success: true,
                readings: result.readings,
                account_number: result.account_number,
                total_readings: result.total_readings,
                filtered_by_active_bill_cycle: result.filtered_by_active_bill_cycle,
                message: result.message
            };
        } else {
            // Handle case where readings might be at root level
            return {
                success: true,
                readings: Array.isArray(result) ? result : [],
                account_number: accNbr,
                total_readings: Array.isArray(result) ? result.length : 0,
                filtered_by_active_bill_cycle: !includeAllCycles,
                message: Array.isArray(result) && result.length > 0 ? 
                    'Account readings retrieved' : 'No readings found for account'
            };
        }
    } catch (error) {
        console.error('Error fetching temp readings by account number:', error);
        // Return structured error response instead of throwing
        return {
            success: false,
            readings: [],
            account_number: accNbr,
            total_readings: 0,
            error: `Failed to fetch temp readings: ${error.message}`
        };
    }
};

// Get readings by area code with bill cycle and finalized invoice filter options
export const getTempReadingsByAreaCd = async (areaCd, includeAllCycles = false, excludeFinalized = true) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        params.append('exclude_finalized', excludeFinalized ? 'true' : 'false');
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCd}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return {
            readings: result.readings || result,
            activeBillCycle: result.active_bill_cycle,
            filteredByActiveBillCycle: result.filtered_by_active_bill_cycle,
            excludeFinalized: result.exclude_finalized
        };
    } catch (error) {
        console.error('Error fetching temp readings by area code:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get readings for every area in scope (all permitted areas, or the single
// area narrowed to in the header bar). The backend resolves the area list.
export const getTempReadingsByAreaCodes = async (includeAllCycles = false, excludeFinalized = true) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        params.append('exclude_finalized', excludeFinalized ? 'true' : 'false');

        const response = await fetch(`${API_BASE_URL}/areas?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return {
            readings: result.readings || result,
            areaCode: result.area_code || null,
            areaCodes: result.area_codes || [],
            activeBillCycle: result.active_bill_cycle,
            filteredByActiveBillCycle: result.filtered_by_active_bill_cycle,
            excludeFinalized: result.exclude_finalized
        };
    } catch (error) {
        console.error('Error fetching temp readings by area codes:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get readings by area code and specific bill cycle (for historical data)
export const getTempReadingsByAreaCdAndBillCycle = async (areaCd, billCycle) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        
        const response = await fetch(`${API_BASE_URL}/area/${areaCd}/bill-cycle/${billCycle}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching temp readings by area code and bill cycle:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get active bill cycle for area
export const getActiveBillCycleForArea = async (areaCd) => {
    try {
        const response = await fetch(`${API_BASE_URL}/area/${areaCd}/active-bill-cycle`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result;
    } catch (error) {
        console.error('Error fetching active bill cycle:', error);
        throw new Error(`Failed to fetch active bill cycle: ${error.message}`);
    }
};

// Get readings by meter number with bill cycle filter option
export const getTempReadingsByMtrNbr = async (mtrNbr, includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        
        const response = await fetch(`${API_BASE_URL}/meter/${mtrNbr}?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching temp readings by meter number:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get readings by date range with bill cycle filter option
export const getTempReadingsByDateRange = async (startDate, endDate, includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const formattedStartDate = startDate instanceof Date 
            ? startDate.toISOString().split('T')[0]
            : startDate;
        const formattedEndDate = endDate instanceof Date 
            ? endDate.toISOString().split('T')[0]
            : endDate;
            
        const params = new URLSearchParams();
        params.append('startDate', formattedStartDate);
        params.append('endDate', formattedEndDate);
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
            
        const response = await fetch(`${API_BASE_URL}/date-range?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching temp readings by date range:', error);
        throw new Error(`Failed to fetch temp readings: ${error.message}`);
    }
};

// Get latest readings for an account with bill cycle filter option
export const getLatestTempReadingsByAccNbr = async (accNbr, includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        
        const response = await fetch(`${API_BASE_URL}/account/${accNbr}/latest?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching latest temp readings:', error);
        throw new Error(`Failed to fetch latest temp readings: ${error.message}`);
    }
};

// Get readings with errors with bill cycle filter option
export const getTempReadingsWithErrors = async (includeAllCycles = false) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const params = new URLSearchParams();
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
        if (includeAllCycles) params.append('include_all_cycles', 'true');
        
        const response = await fetch(`${API_BASE_URL}/errors?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.readings || result;
    } catch (error) {
        console.error('Error fetching temp readings with errors:', error);
        throw new Error(`Failed to fetch temp readings with errors: ${error.message}`);
    }
};

// Get specific reading
export const getSpecificTempReading = async (accNbr, areaCd, addedBlcy, mtrSeq, mtrType, rdngDate) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        const formattedDate = rdngDate instanceof Date 
            ? rdngDate.toISOString().split('T')[0]
            : rdngDate;
            
        const params = new URLSearchParams();
        params.append('accNbr', accNbr);
        params.append('areaCd', areaCd);
        params.append('addedBlcy', addedBlcy);
        params.append('mtrSeq', mtrSeq);
        params.append('mtrType', mtrType);
        params.append('rdngDate', formattedDate);
        if (sessionId) params.append('session_id', sessionId);
        if (userId) params.append('user_id', userId);
            
        const response = await fetch(`${API_BASE_URL}/specific?${params.toString()}`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.reading || result;
    } catch (error) {
        console.error('Error fetching specific temp reading:', error);
        throw new Error(`Failed to fetch specific temp reading: ${error.message}`);
    }
};

// Get distinct account numbers
export const getDistinctAccNbrs = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/accounts/distinct`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.accounts || result;
    } catch (error) {
        console.error('Error fetching distinct account numbers:', error);
        throw new Error(`Failed to fetch distinct account numbers: ${error.message}`);
    }
};

// Get distinct meter types
export const getDistinctMtrTypes = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/meter-types/distinct`, {
            method: 'GET',
            headers: getAuthHeaders(),
            credentials: 'include',
        });
        const result = await handleApiResponse(response);
        return result.meter_types || result;
    } catch (error) {
        console.error('Error fetching distinct meter types:', error);
        throw new Error(`Failed to fetch distinct meter types: ${error.message}`);
    }
};

// Create new temp reading
export const createTempReading = async (readingData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(readingData),
        });
        
        const result = await handleApiResponse(response);
        return result.reading || result;
    } catch (error) {
        console.error('Error creating temp reading:', error);
        throw new Error(`Failed to create temp reading: ${error.message}`);
    }
};

// Update temp reading
export const updateTempReading = async (readingData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'PUT',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(readingData),
        });
        
        const result = await handleApiResponse(response);
        return result.reading || result;
    } catch (error) {
        console.error('Error updating temp reading:', error);
        throw new Error(`Failed to update temp reading: ${error.message}`);
    }
};

// FIXED: Helper function to group readings by account number - now captures bill cycle correctly
export const groupReadingsByAccount = (readings) => {
    if (!readings || !Array.isArray(readings)) return [];
    
    const grouped = readings.reduce((acc, reading) => {
        const accNbr = reading.acc_nbr;
        if (!acc[accNbr]) {
            acc[accNbr] = {
                acc_nbr: accNbr,
                folio_no: reading.folio_no ?? reading.folioNo ?? null,
                area_cd: reading.area_cd,
                added_blcy: reading.added_blcy, // FIXED: Capture bill cycle from first reading
                responsible_ee: reading.responsible_ee || reading.responsibleEe || reading.responsble_ee || null,
                accept_ru: reading.accept_ru !== undefined ? reading.accept_ru : (reading.acceptRu !== undefined ? reading.acceptRu : null),
                tariff_desc: reading.tariff_desc || reading.tariffDesc || reading.tariff_type || reading.tariffType || null,
                tariff_type: reading.tariff_desc || reading.tariffDesc || reading.tariff_type || reading.tariffType || null,
                reading_count: 0,
                readings: []
            };
        } else if (acc[accNbr].accept_ru == null && (reading.accept_ru != null || reading.acceptRu != null)) {
            acc[accNbr].accept_ru = reading.accept_ru != null ? reading.accept_ru : reading.acceptRu;
        }
        if (!acc[accNbr].folio_no && (reading.folio_no != null || reading.folioNo != null)) {
            acc[accNbr].folio_no = reading.folio_no ?? reading.folioNo ?? null;
        }
        if (!acc[accNbr].tariff_desc && (reading.tariff_desc || reading.tariffDesc || reading.tariff_type || reading.tariffType)) {
            const desc = reading.tariff_desc || reading.tariffDesc || reading.tariff_type || reading.tariffType;
            acc[accNbr].tariff_desc = desc;
            acc[accNbr].tariff_type = desc;
        }
        acc[accNbr].reading_count++;
        acc[accNbr].readings.push(reading);
        return acc;
    }, {});
    
    // Convert object to array and sort by folio number (fallback to account number)
    return Object.values(grouped).sort((a, b) => {
        const aFolio = String(a.folio_no ?? "");
        const bFolio = String(b.folio_no ?? "");
        if (aFolio && bFolio) {
            return aFolio.localeCompare(bFolio, undefined, { numeric: true });
        }
        return a.acc_nbr.localeCompare(b.acc_nbr);
    });
};

// Transform backend response to frontend format if needed
export const transformTempReadingBackendToFrontend = (backendReading) => {
    return {
        acc_nbr: backendReading.acc_nbr,
        folio_no: backendReading.folio_no ?? backendReading.folioNo ?? null,
        inst_id: backendReading.inst_id,
        area_cd: backendReading.area_cd,
        added_blcy: backendReading.added_blcy,
        mtr_seq: backendReading.mtr_seq,
        mtr_type: backendReading.mtr_type,
        prv_date: backendReading.prv_date,
        rdng_date: backendReading.rdng_date,
        prsnt_rdn: backendReading.prsnt_rdn,
        prv_rdn: backendReading.prv_rdn,
        mtr_nbr: backendReading.mtr_nbr,
        units: backendReading.units,
        rate: backendReading.rate,
        computed_chg: backendReading.computed_chg,
        mnt_chg: backendReading.mnt_chg,
        acode: backendReading.acode,
        m_factor: backendReading.m_factor,
        bill_stat: backendReading.bill_stat,
        err_stat: backendReading.err_stat,
        mtr_stat: backendReading.mtr_stat,
        rdn_stat: backendReading.rdn_stat,
        user_id: backendReading.user_id,
        accept_ru: backendReading.accept_ru !== undefined ? backendReading.accept_ru : (backendReading.acceptRu !== undefined ? backendReading.acceptRu : null),
        tariff_desc: backendReading.tariff_desc || backendReading.tariffDesc || backendReading.tariff_type || backendReading.tariffType || null,
        tariff_type: backendReading.tariff_desc || backendReading.tariffDesc || backendReading.tariff_type || backendReading.tariffType || null,
        entered_dtime: backendReading.entered_dtime,
        edited_user_id: backendReading.edited_user_id,
        edited_dtime: backendReading.edited_dtime
    };
};