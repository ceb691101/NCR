// FILE: src/services/monthlyChargeService.js
import { apiPath } from '../config';

// Use proxy path instead of direct IP address
const MONTHLY_CHARGE_API_URL = '/api/MonthlyCharge/monthly-charge';

// Connection state tracking
let connectionWarmedUp = false;
let connectionInitialized = false;

// Helper function to handle API responses
const handleApiResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const errorMessage = errorData.message || errorData.error || `HTTP error! Status: ${response.status}`;
    console.error('API response error:', {
      status: response.status,
      message: errorMessage,
      data: errorData
    });
    throw new Error(errorMessage);
  }
  return response.json();
};

// Connection warmup function
export const warmupApiConnection = async () => {
  if (connectionWarmedUp) {
    console.log('API connection already warmed up');
    return true;
  }
  
  try {
    console.log('🔄 Warming up API connection...');
    
    // Try multiple connection strategies
    const warmupStrategies = [
      // Strategy 1: Simple OPTIONS request (preflight)
      async () => {
        console.log('Trying OPTIONS request...');
        return fetch(MONTHLY_CHARGE_API_URL, {
          method: 'OPTIONS',
          headers: {
            'Content-Type': 'application/json',
          },
        });
      },
      
      // Strategy 2: Simple GET request (if supported)
      async () => {
        console.log('Trying GET request...');
        return fetch(MONTHLY_CHARGE_API_URL.replace('/monthly-charge', ''), {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });
      },
      
      // Strategy 3: Minimal POST request
      async () => {
        console.log('Trying minimal POST request...');
        const minimalData = {
          tariffCode: 'I2',
          consumerType: 'B',
          fromDate: new Date().toISOString(),
          toDate: new Date().toISOString(),
          peakUnits: 0,
          dayUnits: 0,
          offPeakUnits: 0,
          demandKva: 0,
          isSSCL: false
        };
        
        return fetch(MONTHLY_CHARGE_API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(minimalData)
        });
      }
    ];
    
    let warmupSuccess = false;
    
    for (let i = 0; i < warmupStrategies.length; i++) {
      try {
        console.log(`Warmup attempt ${i + 1}/${warmupStrategies.length}`);
        const response = await warmupStrategies[i]();
        console.log(`Warmup attempt ${i + 1} response status:`, response.status);
        
        if (response.status !== 500) {
          warmupSuccess = true;
          break;
        }
      } catch (error) {
        console.log(`Warmup attempt ${i + 1} failed (non-critical):`, error.message);
        // Continue to next strategy
      }
      
      // Wait between attempts
      if (i < warmupStrategies.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 300));
      }
    }
    
    if (warmupSuccess) {
      console.log('✅ API connection warmed up successfully');
    } else {
      console.log('⚠️ API warmup had issues, but connection may still work');
    }
    
    // Small delay to ensure connection is established
    await new Promise(resolve => setTimeout(resolve, 500));
    
    connectionWarmedUp = true;
    return warmupSuccess;
  } catch (error) {
    console.warn('API warmup failed:', error.message);
    connectionWarmedUp = true; // Still mark as warmed up to avoid infinite retries
    return false;
  }
};

// Initialize connection on module load (optional)
if (typeof window !== 'undefined') {
  // Warm up connection after a short delay when app starts
  setTimeout(() => {
    warmupApiConnection().catch(() => {
      // Silent fail for warmup
    });
  }, 2000);
}

// Enhanced calculateMonthlyCharges function with retry logic
export const calculateMonthlyCharges = async (requestData, maxRetries = 3) => {
  let lastError;
  let lastResponseStatus;
  
  console.log('🚀 Starting monthly charges calculation...');
  console.log('Request data:', JSON.stringify(requestData, null, 2));
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      // Apply exponential backoff for retries
      if (attempt > 0) {
        const delay = 300 * Math.pow(2, attempt - 1);
        console.log(`⏳ Retry attempt ${attempt}/${maxRetries} after ${delay}ms delay`);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
      
      console.log(`📤 Attempt ${attempt + 1}/${maxRetries + 1}: Sending to ${MONTHLY_CHARGE_API_URL}`);
      
      // Prepare fetch options
      const fetchOptions = {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify(requestData),
      };
      
      // Add timeout to fetch
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 15000); // 15 second timeout
      fetchOptions.signal = controller.signal;
      
      const response = await fetch(MONTHLY_CHARGE_API_URL, fetchOptions);
      clearTimeout(timeoutId);
      
      lastResponseStatus = response.status;
      console.log(`📥 Response status: ${response.status}`);
      
      // Handle different status codes
      if (response.status === 500) {
        const errorText = await response.text();
        console.error('Server returned 500 error:', errorText.substring(0, 200));
        
        if (attempt < maxRetries) {
          console.log('Server error, will retry...');
          continue;
        }
      }
      
      const result = await handleApiResponse(response);
      
      if (!result) {
        throw new Error('Empty response from server');
      }
      
      console.log(`✅ Attempt ${attempt + 1} successful!`);
      console.log('API Response:', result);
      
      // Mark connection as initialized on success
      connectionInitialized = true;
      return result;
      
    } catch (error) {
      lastError = error;
      
      // Categorize the error
      if (error.name === 'AbortError') {
        console.error(`⏰ Attempt ${attempt + 1} timed out`);
      } else if (error.message.includes('Failed to fetch')) {
        console.error(`🌐 Attempt ${attempt + 1} failed: Network error`);
      } else if (error.message.includes('CORS')) {
        console.error(`🛡️ Attempt ${attempt + 1} failed: CORS error`);
      } else if (lastResponseStatus === 500) {
        console.error(`💥 Attempt ${attempt + 1} failed: Server error (500)`);
      } else {
        console.error(`❌ Attempt ${attempt + 1} failed:`, error.message);
      }
      
      // Decide whether to retry based on error type
      const shouldRetry = (
        attempt < maxRetries && 
        (
          error.name === 'AbortError' ||
          error.message.includes('Failed to fetch') ||
          error.message.includes('Network') ||
          error.message.includes('CORS') ||
          lastResponseStatus === 500
        )
      );
      
      if (!shouldRetry) {
        break;
      }
    }
  }
  
  // All attempts failed
  console.error('💥 All attempts failed. Last error:', lastError?.message);
  
  // Provide helpful error message based on the last error
  let userErrorMessage = 'Cannot connect to API server. Please check network connectivity.';
  
  if (lastError?.message.includes('Failed to fetch')) {
    userErrorMessage = 'Network connection failed. Please check if the API server is running and accessible.';
  } else if (lastError?.message.includes('CORS')) {
    userErrorMessage = 'CORS policy blocked the request. Please check server configuration.';
  } else if (lastResponseStatus === 500) {
    userErrorMessage = 'API server returned an internal error. Please try again or contact support.';
  }
  
  throw new Error(userErrorMessage);
};

// Function to fetch customer GST info
export const getCustomerGstInfo = async (accountNumber, areaCode) => {
  try {
    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');
    
    // Use the bulk-customers endpoint to get customer data including gst_apl
    const response = await fetch(apiPath(`/api/v1/bulk-customers/account/${accountNumber}?session_id=${sessionId}&user_id=${userId}`), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa('user:admin123'),
      },
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    
    const result = await response.json();
    
    if (result.customer) {
      return {
        gstApl: result.customer.gst_apl || " ",
        success: true
      };
    }
    
    return {
      gstApl: " ",
      success: false,
      message: result.message || "Customer data not found"
    };
    
  } catch (error) {
    console.error('Error fetching customer GST info:', error);
    return {
      gstApl: " ",
      success: false,
      error: error.message
    };
  }
};

// Helper function to prepare request data from form data - UPDATED WITH GST
export const prepareMonthlyChargeRequest = async (formData, customerMeterTypes = []) => {
  // Get tariff and consumer type from form data
  const tariffCode = formData.tariff || 'I2'; // Default to I2 if not available
  const consumerType = formData.customer_category || 'B'; // Default to B if not available
  
  // Convert reading date to ISO format (from DD/MM/YYYY)
  let fromDate = new Date().toISOString(); // Default to current date
  if (formData.reading_date) {
    try {
      const parts = formData.reading_date.split('/');
      if (parts.length === 3) {
        const day = parts[0];
        const month = parts[1];
        const year = parts[2];
        fromDate = new Date(year, month - 1, day).toISOString();
      }
    } catch (error) {
      console.error('Error parsing reading date:', error);
    }
  }

  // Convert previous reading date to ISO format for toDate
  let toDate = new Date().toISOString(); // Default to current date
  if (formData.previous_reading_date) {
    try {
      const parts = formData.previous_reading_date.split('/');
      if (parts.length === 3) {
        const day = parts[0];
        const month = parts[1];
        const year = parts[2];
        toDate = new Date(year, month - 1, day).toISOString();
      }
    } catch (error) {
      console.error('Error parsing previous reading date:', error);
    }
  }

  // Calculate units by subtracting previous reading from present reading, then multiplying with M factor
  const getUnitsWithMFactor = (presentReading, previousReading, multiplyBy) => {
    const present = parseFloat(presentReading) || 0;
    const previous = parseFloat(previousReading) || 0;
    const multiply = parseFloat(multiplyBy) || 1;
    
    // Calculate actual units consumed: Present - Previous
    const actualUnits = present - previous;
    
    // Only use positive units
    if (actualUnits < 0) {
      console.warn(`Negative units calculated: ${actualUnits}. Using 0 instead.`);
      return 0;
    }
    
    // Multiply by M factor
    return Math.round(actualUnits * multiply);
  };

  // Initialize units
  let peakUnits = 0;
  let dayUnits = 0;
  let offPeakUnits = 0;
  let demandKva = 0;

  // Calculate units for each meter type
  customerMeterTypes.forEach(meter => {
    const meterType = meter.meter_type?.trim();
    const prefix = getPrefixFromMeterType(meterType);
    
    if (!prefix) return;

    const presentReading = parseFloat(formData[`${prefix}_presentread`]) || 0;
    const previousReading = parseFloat(formData[`${prefix}_previousread`]) || 0;
    const multiplyBy = parseFloat(formData[`${prefix}_multiplyby`]) || 1;
    
    // Calculate actual units: (Present - Previous) × MultiplyBy
    const units = getUnitsWithMFactor(presentReading, previousReading, multiplyBy);

    console.log(`Meter ${meterType}: Present=${presentReading}, Previous=${previousReading}, MultiplyBy=${multiplyBy}, FinalUnits=${units}`);

    switch (meterType) {
      case 'KWP': // KWH Peak
        peakUnits = units;
        break;
      case 'KWD': // KWH Day
        dayUnits = units;
        break;
      case 'KWO': // KWH Off Peak
        offPeakUnits = units;
        break;
      case 'KVA': // KVA
        demandKva = units;
        break;
      default:
        break;
    }
  });

  // Get GST info for isSSCL - ASYNC CALL
  let isSSCL = false;
  try {
    console.log('Fetching GST info for account:', formData.account_number, 'area:', formData.area_code_number);
    const gstInfo = await getCustomerGstInfo(formData.account_number, formData.area_code_number);
    const gstAplValue = gstInfo.gstApl || " ";
    
    // Map GST_Apl to isSSCL
    // "Y" means isSSCL = true
    // "N" or " " (single space) or empty means isSSCL = false
    isSSCL = gstAplValue.trim() === "Y";
    
    console.log('GST_Apl value:', JSON.stringify(gstAplValue), 'Mapped to isSSCL:', isSSCL);
  } catch (error) {
    console.error('Error getting GST info, defaulting to false:', error);
    isSSCL = false;
  }

  const requestData = {
    tariffCode: tariffCode.trim(), // Clean up any spaces
    consumerType: consumerType.trim(),
    fromDate,
    toDate,
    peakUnits,
    dayUnits,
    offPeakUnits,
    demandKva,
    isSSCL: isSSCL  // Add isSSCL here
  };

  console.log('Prepared monthly charge request with isSSCL:', isSSCL, requestData);
  return requestData;
};

// Helper function to get field prefix from meter type
const getPrefixFromMeterType = (meterType) => {
  const mapping = {
    'KWO': 'kwh_offp',
    'KWD': 'kwh_day', 
    'KWP': 'kwh_peak',
    'KVA': 'kva',
    'KVAH': 'kvah'
  };
  return mapping[meterType] || '';
};

// Helper function to map API response to form fields - UPDATED TO USE API'S TOTAL
export const mapApiResponseToFormFields = (apiResponse, formData, customerMeterTypes = []) => {
  const updates = {};
  
  if (!apiResponse || !apiResponse.rows) {
    console.warn('Empty or invalid API response:', apiResponse);
    return updates;
  }

  console.log('Mapping API response with rows:', apiResponse.rows.length);

  // Map rates and amounts for each meter type
  customerMeterTypes.forEach(meter => {
    const meterType = meter.meter_type?.trim();
    const prefix = getPrefixFromMeterType(meterType);
    
    if (!prefix) return;

    let rate = 0;
    let amount = 0;

    switch (meterType) {
      case 'KWP': // KWH Peak
        const peakRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'PEAK'
        );
        rate = peakRow?.energyRate || 0;
        amount = peakRow?.chargeAmount || 0;
        break;
        
      case 'KWD': // KWH Day
        const dayRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'DAY'
        );
        rate = dayRow?.energyRate || 0;
        amount = dayRow?.chargeAmount || 0;
        break;
        
      case 'KWO': // KWH Off Peak
        const offPeakRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'OFFPEAK'
        );
        rate = offPeakRow?.energyRate || 0;
        amount = offPeakRow?.chargeAmount || 0;
        break;
        
      case 'KVA': // KVA
        const demandRow = apiResponse.rows.find(row => 
          row.rowType === 'DEMAND' && row.period === ''
        );
        rate = demandRow?.demandRate || 0;
        amount = demandRow?.chargeAmount || 0;
        break;
        
      default:
        break;
    }

    if (rate > 0) {
      updates[`${prefix}_rate`] = rate.toFixed(2);
    }
    if (amount > 0) {
      updates[`${prefix}_amount`] = amount.toFixed(2);
    }
  });

  // Map fixed charge
  const fixedRow = apiResponse.rows.find(row => row.rowType === 'FIXED');
  if (fixedRow) {
    updates['fixed_charge'] = fixedRow.fixedCharge?.toFixed(2) || '0.00';
  }

  // CRITICAL: Map SSCL charge (VAT) from API response
  if (apiResponse.ssclCharge !== undefined && apiResponse.ssclCharge !== null) {
    const ssclCharge = parseFloat(apiResponse.ssclCharge) || 0;
    updates['vat'] = ssclCharge.toFixed(2);
    console.log('Mapped SSCL charge (VAT):', ssclCharge, 'from API response:', apiResponse.ssclCharge);
  } else {
    // If API doesn't return ssclCharge, default to 0
    updates['vat'] = '0.00';
    console.log('No SSCL charge in API response, defaulting VAT to 0.00');
  }

  // USE API's grandTotal directly for total_amount - DO NOT CALCULATE MANUALLY
  if (apiResponse.grandTotal !== undefined && apiResponse.grandTotal !== null) {
    updates['tot_amount'] = parseFloat(apiResponse.grandTotal).toFixed(2);
    console.log('Using API grandTotal for total_amount:', apiResponse.grandTotal);
  } else {
    // Fallback: Use totalCharge if grandTotal not available
    updates['tot_amount'] = parseFloat(apiResponse.totalCharge || 0).toFixed(2);
    console.log('Using API totalCharge for total_amount:', apiResponse.totalCharge);
  }

  // Map monthly charge if available from API (show it even though it's included in total)
  if (apiResponse.totalCharge !== undefined && apiResponse.totalCharge !== null) {
    updates['monthly_charge'] = parseFloat(apiResponse.totalCharge).toFixed(2);
    console.log('Setting monthly_charge from API totalCharge:', apiResponse.totalCharge);
  }

  console.log('Mapped API response to form updates (using API total):', updates);
  console.log('API Response summary:', {
    totalCharge: apiResponse.totalCharge,
    ssclCharge: apiResponse.ssclCharge,
    grandTotal: apiResponse.grandTotal
  });
  
  return updates;
};

// Helper function to map API response to form fields (legacy version without VAT)
export const mapApiResponseToFormFieldsLegacy = (apiResponse, formData, customerMeterTypes = []) => {
  const updates = {};
  
  if (!apiResponse || !apiResponse.rows) {
    return updates;
  }

  // Map rates and amounts for each meter type
  customerMeterTypes.forEach(meter => {
    const meterType = meter.meter_type?.trim();
    const prefix = getPrefixFromMeterType(meterType);
    
    if (!prefix) return;

    let rate = 0;
    let amount = 0;

    switch (meterType) {
      case 'KWP': // KWH Peak
        const peakRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'PEAK'
        );
        rate = peakRow?.energyRate || 0;
        amount = peakRow?.chargeAmount || 0;
        break;
        
      case 'KWD': // KWH Day
        const dayRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'DAY'
        );
        rate = dayRow?.energyRate || 0;
        amount = dayRow?.chargeAmount || 0;
        break;
        
      case 'KWO': // KWH Off Peak
        const offPeakRow = apiResponse.rows.find(row => 
          row.rowType === 'ENERGY' && row.period === 'OFFPEAK'
        );
        rate = offPeakRow?.energyRate || 0;
        amount = offPeakRow?.chargeAmount || 0;
        break;
        
      case 'KVA': // KVA
        const demandRow = apiResponse.rows.find(row => 
          row.rowType === 'DEMAND' && row.period === ''
        );
        rate = demandRow?.demandRate || 0;
        amount = demandRow?.chargeAmount || 0;
        break;
        
      default:
        break;
    }

    if (rate > 0) {
      updates[`${prefix}_rate`] = rate.toFixed(2);
    }
    if (amount > 0) {
      updates[`${prefix}_amount`] = amount.toFixed(2);
    }
  });

  // Map fixed charge and monthly charge
  const fixedRow = apiResponse.rows.find(row => row.rowType === 'FIXED');
  if (fixedRow) {
    updates['fixed_charge'] = fixedRow.fixedCharge?.toFixed(2) || '0.00';
    // For fixed charge row, chargeAmount represents the fixed charge amount
    updates['monthly_charge'] = fixedRow.chargeAmount?.toFixed(2) || '0.00';
  }

  // Update total charge (monthly charge should be the total from API)
  if (apiResponse.totalCharge) {
    updates['monthly_charge'] = apiResponse.totalCharge.toFixed(2);
  }

  // Also update the total amount field
  if (apiResponse.totalCharge) {
    updates['tot_amount'] = apiResponse.totalCharge.toFixed(2);
  }

  console.log('Mapped API response to form updates (legacy):', updates);
  return updates;
};

// Health check function
export const checkApiHealth = async () => {
  try {
    const response = await fetch(MONTHLY_CHARGE_API_URL.replace('/monthly-charge', '/health'), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    }).catch(() => null);
    
    if (response && response.ok) {
      return {
        healthy: true,
        status: 'API server is reachable',
        response: await response.json().catch(() => ({})),
      };
    }
    
    return {
      healthy: false,
      status: 'API server may be down',
      error: response ? `Status: ${response.status}` : 'No response',
    };
  } catch (error) {
    return {
      healthy: false,
      status: 'Error checking API health',
      error: error.message,
    };
  }
};

// Export connection status for debugging
export const getConnectionStatus = () => ({
  warmedUp: connectionWarmedUp,
  initialized: connectionInitialized,
  apiUrl: MONTHLY_CHARGE_API_URL,
});