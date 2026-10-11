import { apiPath } from '../config';

const API_BASE_URL = apiPath('/api/v1/pending-meter-readings');

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

// Get pending reading for a specific customer
export const getPendingReadingForCustomer = async (accountNumber, areaCode, folioNo) => {
    try {
        const sessionId = sessionStorage.getItem('session_id');
        const userId = sessionStorage.getItem('user_id');
        
        if (!sessionId || !userId) {
            throw new Error('Session not found. Please login again.');
        }

        const requestBody = {
            session_id: sessionId,
            user_id: userId,
            account_number: accountNumber,
            area_code: areaCode
        };
        if (folioNo !== undefined && folioNo !== null && folioNo !== '') {
            requestBody.folio_no = Number(folioNo);
        }

        const response = await fetch(`${API_BASE_URL}/customer/active`, {
            method: 'POST',
            headers: getAuthHeaders(),
            credentials: 'include',
            body: JSON.stringify(requestBody),
        });

        return await handleApiResponse(response);
    } catch (error) {
        console.error('Error fetching pending reading:', error);
        throw new Error(`Failed to fetch pending reading: ${error.message}`);
    }
};

// Transform pending reading data to frontend format
export const transformPendingReadingToFrontend = (backendData) => {
  if (!backendData || !backendData.pending_reading) {
    return null;
  }

  const pendingReading = backendData.pending_reading;
  
  // Initialize frontend form data structure
  const frontendData = {
    account_number: pendingReading.account_number || "",
    folio_no: pendingReading.folio_no || "",
    developer_name: pendingReading.developer_name || "",
    facility_name: pendingReading.facility_name || "",
    tariff_type: pendingReading.tariff_type || "",
    tariff_value: pendingReading.tariff_value || "",
    meter_number: pendingReading.meter_number || "",
    customer_category: pendingReading.customer_category || "",
    current_billcycle_number: pendingReading.current_bill_cycle || "",
    current_billcycle_date: formatBillCycleDate(pendingReading.bill_cycle_date),
    bill_month: formatBillMonth(
      pendingReading.current_bill_cycle,
      pendingReading.bill_month,
      pendingReading.bill_year,
      pendingReading.bill_cycle_date
    ),
    area_code_number: pendingReading.area_code || "",
    area_code_name: pendingReading.area_name || "",
    reading_date: formatDate(pendingReading.reading_date),
    previous_reading_date: formatDate(pendingReading.previous_reading_date),
    has_reading: Boolean(pendingReading.has_reading),
    reading_status: pendingReading.reading_status || "PENDING",
    accept_ru: pendingReading.accept_ru !== undefined ? pendingReading.accept_ru : (pendingReading.acceptRu !== undefined ? pendingReading.acceptRu : null)
  };

  // Initialize meter type fields with defaults
  const meterTypeMapping = {
    'KWO': { label: 'KWH (Off Peak)', prefix: 'kwh_offp' },
    'KWD': { label: 'KWH (Day)', prefix: 'kwh_day' },
    'KWP': { label: 'KWH (Peak)', prefix: 'kwh_peak' },
    'KWT': { label: 'KWH (Total Export)', prefix: 'kwh_tot' }
  };

  // Initialize all meter fields to empty
  Object.values(meterTypeMapping).forEach(({ prefix }) => {
    frontendData[`${prefix}_meternum`] = "";
    frontendData[`${prefix}_presentread`] = ""; // User will enter
    frontendData[`${prefix}_previousread`] = "0"; // Default to 0, will be populated from actual data
    frontendData[`${prefix}_units`] = ""; // Will be calculated
  });

  // Populate meter type data from backend
  if (pendingReading.meter_types && Array.isArray(pendingReading.meter_types)) {
    pendingReading.meter_types.forEach(meter => {
      const meterType = meter.meter_type?.trim();
      const meterConfig = meterTypeMapping[meterType];
      
      if (meterConfig) {
        const prefix = meterConfig.prefix;
        frontendData[`${prefix}_meternum`] = meter.meter_number || "";
                frontendData[`${prefix}_presentread`] = meter.present_reading != null
                    ? meter.present_reading.toString()
                    : "";
        frontendData[`${prefix}_previousread`] = meter.previous_reading?.toString() || "0";
        frontendData[`${prefix}_units`] = ""; // Will be calculated
      }
    });
  }

  return {
    formData: frontendData,
    meterTypes: pendingReading.meter_types || [],
    totalMeters: pendingReading.total_meters || 0
  };
};

// Helper function to format dates for display
const formatDate = (dateString) => {
    if (!dateString) return "";
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB');
    } catch (error) {
        return dateString;
    }
};

// Helper function to format bill cycle date
const formatBillCycleDate = (dateString) => {
    if (!dateString) return "";
    try {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = date.toLocaleDateString('en-US', { month: 'short' });
        return `${year} ${month}`;
    } catch (error) {
        return dateString;
    }
};

const MONTH_NAMES = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
];

// Build the "Bill Month" display value: "{bill_cycle} - {Month} {year}"
export const formatBillMonth = (billCycle, billMonth, billYear, billCycleDate) => {
    if (!billCycle && !billMonth) return "";

    let month = billMonth != null ? parseInt(billMonth, 10) : null;
    let year = billYear != null ? parseInt(billYear, 10) : null;

    // Fallback: derive month/year from the bill cycle date when not provided
    if ((!month || !year) && billCycleDate) {
        const date = new Date(billCycleDate);
        if (!isNaN(date.getTime())) {
            if (!month) month = date.getMonth() + 1;
            if (!year) year = date.getFullYear();
        }
    }

    const monthName = month >= 1 && month <= 12 ? MONTH_NAMES[month - 1] : "";
    const monthYear = [monthName, year].filter(Boolean).join(" ");

    if (!billCycle) return monthYear;
    return monthYear ? `${billCycle} - ${monthYear}` : `${billCycle}`;
};

// Calculate number of days between reading date and previous reading date
export const calculateNumberOfDays = (readingDate, previousReadingDate) => {
    if (!readingDate || !previousReadingDate) return 0;
    
    try {
        const reading = new Date(readingDate.split('/').reverse().join('-'));
        const previous = new Date(previousReadingDate.split('/').reverse().join('-'));
        
        const diffTime = Math.abs(reading - previous);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        return diffDays > 0 ? diffDays : 0;
    } catch (error) {
        console.error('Error calculating number of days:', error);
        return 0;
    }
};

// Get meter types mapping for display
export const getMeterTypesMapping = () => {
    return {
        'KWO': { label: 'KWH (Off Peak)', prefix: 'kwh_offp' },
        'KWD': { label: 'KWH (Day)', prefix: 'kwh_day' },
        'KWP': { label: 'KWH (Peak)', prefix: 'kwh_peak' },
        'KWT': { label: 'KWH (Total Export)', prefix: 'kwh_tot' }
    };
};