import React, { useEffect, useState } from 'react';
import PieChartCard from '../../components/Cards/PieChartCard';
import { getUserReadingStatus } from 'services/readingStatusService';
import { getInvoices } from 'services/invoiceService';
import {
  getSelectedAreaCode,
  getBillCycleForArea,
  getPermittedAreaCodes,
} from 'services/AreaAndBillService';
import { toast } from 'react-toastify';

const JobStatusPie = () => {
  const [meterData, setMeterData] = useState(null);
  const [invoiceData, setInvoiceData] = useState({
    createdCount: 0,
    pendingCount: 0,
    totalCount: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedAreaCode, setSelectedAreaCode] = useState(null);

  const matchAreaCode = (a1, a2) => {
    if (!a1 || !a2) return false;
    const s1 = String(a1).replace(/^0+/, '').trim();
    const s2 = String(a2).replace(/^0+/, '').trim();
    return s1 === s2;
  };

  const loadInvoiceSummary = async (currentSelectedArea) => {
    try {
      const response = await getInvoices();
      const invoices = Array.isArray(response) ? response : [];
      const permittedAreaCodes = getPermittedAreaCodes();
      const scopeAreaCodes = currentSelectedArea ? [currentSelectedArea] : permittedAreaCodes;
      const filtered = invoices.filter((invoice) => {
        if (!invoice) return false;
        const invoiceAreaCode = invoice.areaCode || invoice.area_code || invoice.area || invoice.area_code_id;
        if (!invoiceAreaCode) return false;
        return scopeAreaCodes.some((areaCode) => matchAreaCode(invoiceAreaCode, areaCode));
      });

      const createdCount = filtered.filter((invoice) => {
        const status = String(invoice.status || '').trim().toUpperCase();
        return status && status !== 'DRAFT';
      }).length;

      const totalCount = currentSelectedArea
        ? (meterData?.receivedCount ?? 0)
        : (meterData?.receivedCount ?? 0);

      const pendingCount = Math.max(0, totalCount - createdCount);

      setInvoiceData({
        createdCount,
        pendingCount,
        totalCount,
      });
    } catch (invoiceError) {
      console.error('Error fetching invoice summary:', invoiceError);
      setInvoiceData({ createdCount: 0, pendingCount: 0, totalCount: meterData?.receivedCount ?? 0 });
    }
  };
  
  // Listen for area selection changes
  useEffect(() => {
    const handleAreaChange = () => {
      loadMeterReadingData();
    };

    // Listen for custom events when area selection changes
    window.addEventListener('areaAndBill:changed', handleAreaChange);
    
    return () => {
      window.removeEventListener('areaAndBill:changed', handleAreaChange);
    };
  }, []);

  // Listen for logout start so we can suppress error toasts
  useEffect(() => {
    const handleUserLoggingOut = () => {
      try { window.__userLoggingOut = true; } catch (e) { /* ignore */ }
    };
    window.addEventListener('user:loggingOut', handleUserLoggingOut);
    return () => {
      window.removeEventListener('user:loggingOut', handleUserLoggingOut);
    };
  }, []);

  useEffect(() => {
    loadMeterReadingData();
  }, []);

  const loadMeterReadingData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Get currently selected area
      const currentSelectedArea = getSelectedAreaCode();
      setSelectedAreaCode(currentSelectedArea);

      // A falsy selection means every permitted area is in scope; the backend summary
      // then covers exactly the areas this user may read.
      const isAreaSelected = !!currentSelectedArea;

      const permittedAreaCodes = getPermittedAreaCodes();
      const scopeName = isAreaSelected
        ? 'Selected Area'
        : permittedAreaCodes.length === 1
        ? `Area ${permittedAreaCodes[0]}`
        : `All Areas (${permittedAreaCodes.length})`;

      // Get reading status for the areas in scope
      const response = await getUserReadingStatus(false, false);
      
      if (response.success && response.summary) {
        let transformedData;

        if (isAreaSelected && response.area_reading_status) {
          const selectedAreaData = response.area_reading_status.find(
            area => matchAreaCode(area.area_code, currentSelectedArea)
          );
          
          if (selectedAreaData) {
            // Use data for the selected area only
            transformedData = {
              receivedCount: selectedAreaData.customers_with_readings || 0,
              totalCount: selectedAreaData.total_customers || 0,
              areaCode: selectedAreaData.area_code,
              areaName: selectedAreaData.area_name,
              activeBillCycle: getBillCycleForArea(selectedAreaData.area_code)
            };
          } else {
            // Selected area has no data
            transformedData = {
              receivedCount: 0,
              totalCount: 0,
              areaCode: currentSelectedArea,
              areaName: 'Selected Area',
              activeBillCycle: getBillCycleForArea(currentSelectedArea)
            };
          }
        } else {
          // All permitted areas aggregated
          transformedData = {
            receivedCount: response.summary.total_customers_with_readings || 0,
            totalCount: response.summary.total_customers || 0,
            areaCode: null,
            areaName: scopeName,
            activeBillCycle: null
          };
        }
        
        setMeterData(transformedData);
        await loadInvoiceSummary(currentSelectedArea);
        console.log("Meter reading data loaded:", transformedData);
      } else {
        console.warn("No reading status data available:", response.message);
        // Set empty data instead of dummy data
        setMeterData({
          receivedCount: 0,
          totalCount: 0,
          areaCode: currentSelectedArea,
          areaName: currentSelectedArea ? 'Selected Area' : 'No Data',
          activeBillCycle: currentSelectedArea ? getBillCycleForArea(currentSelectedArea) : null
        });
      }
      
    } catch (error) {
  console.error('Error fetching meter reading data:', error);
  setError(error.message);
  // Only show toast if logout is not in progress
  if (!window.__userLoggingOut) toast.error("Failed to load meter reading status");
      
      // Set empty data in case of error
      setMeterData({
        receivedCount: 0,
        totalCount: 0,
        areaCode: selectedAreaCode,
        areaName: 'Error Loading Data',
        activeBillCycle: selectedAreaCode ? getBillCycleForArea(selectedAreaCode) : null
      });
      setInvoiceData({ createdCount: 0, pendingCount: 0, totalCount: 0 });
    } finally {
      setLoading(false);
    }
  };

  /*
  if (loading) {
    return (
      <div style={{
        border: '1px solid #ddd',
        borderRadius: '12px',
        padding: '40px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        width: '400px',
        margin: '20px auto',
        textAlign: 'center',
        backgroundColor: 'white'
      }}>
        <div className="flex justify-center items-center">
          <div className="ds-spinner"></div>
          <span className="ml-3 text-ink-500">Loading meter reading data...</span>
        </div>
      </div>
    );
  }
  */

  if (error && (!meterData || (meterData.receivedCount === 0 && meterData.totalCount === 0))) {
    return (
      <div className="ds-card w-full h-full flex flex-col">
        <div className="ds-card-header">
          <div>
            <h3 className="ds-card-title">Meter Reading Status</h3>
            <p className="ds-card-subtitle mt-0.5">Readings received vs. outstanding</p>
          </div>
        </div>

        <div className="ds-card-body flex-1 flex items-center justify-center">
          <div className="ds-empty max-w-[320px]">
            <span className="w-11 h-11 rounded-lg bg-critical-50 border border-critical-100 text-critical-600 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-triangle-exclamation"></i>
            </span>
            <p className="ds-section-title mt-1">Reading status unavailable</p>
            <p className="ds-body-sm">
              The meter reading status request did not complete successfully.
            </p>
            <button
              onClick={loadMeterReadingData}
              className="ds-btn ds-btn-secondary ds-btn-sm mt-1"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full">
      <PieChartCard
        receivedCount={meterData?.receivedCount}
        totalCount={meterData?.totalCount}
        areaCode={meterData?.areaCode}
        areaName={meterData?.areaName}
        activeBillCycle={meterData?.activeBillCycle}
        isLoading={loading}
      />
    </div>
  );
};

export default JobStatusPie;