import React, { useEffect, useState } from 'react';
import PieChartCard from '../../components/Cards/PieChartCard';
import { getUserReadingStatus } from 'services/readingStatusService';
import {
  getCurrentOpenBillCycle,
  getInvoiceCreateEntries,
} from 'services/billCycleEndingService';
import { getActiveDeveloperCount } from 'services/developerRegistrationService';
import {
  getSelectedAreaCode,
  getPermittedAreaCodes,
} from 'services/AreaAndBillService';
import { toast } from 'react-toastify';

const JobStatusPie = () => {
  const [meterData, setMeterData] = useState(null);
    let activeBillCycle = null;
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

  /*
   * Created invoices as a share of received readings.

   * `receivedCount` is passed in explicitly rather than read from state: the
   * reading-status call that produces it resolves asynchronously, so reading
   * meterData here would still see the previous render's value (null on the
   * first load) and divide by zero.
   */
  const loadInvoiceSummary = async (currentSelectedArea, receivedCount, activeBillCycle) => {
    const denominator = Math.max(0, Number(receivedCount) || 0);

    try {
      if (activeBillCycle === null || activeBillCycle === undefined || activeBillCycle === '') {
        setInvoiceData({ createdCount: 0, pendingCount: denominator, totalCount: denominator });
        return;
      }

      const entries = await getInvoiceCreateEntries(activeBillCycle);
      const permittedAreaCodes = getPermittedAreaCodes();
      const scopeAreaCodes = currentSelectedArea ? [currentSelectedArea] : permittedAreaCodes;

      const createdCount = (Array.isArray(entries) ? entries : []).filter((entry) => {
        if (!entry || Number(entry.is_create) !== 1) return false;
        const entryAreaCode =
          entry.area_cd || entry.areaCode || entry.area_code || entry.area;
        return entryAreaCode && scopeAreaCodes.some((areaCode) =>
          matchAreaCode(entryAreaCode, areaCode)
        );
      });

      setInvoiceData({
        createdCount: createdCount.length,
        pendingCount: Math.max(0, denominator - createdCount.length),
        totalCount: denominator,
      });
    } catch (invoiceError) {
      console.error('Error fetching invoice summary:', invoiceError);
      setInvoiceData({ createdCount: 0, pendingCount: 0, totalCount: denominator });
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

      const currentSelectedArea = getSelectedAreaCode();
      setSelectedAreaCode(currentSelectedArea);

      try {
        const currentCycle = await getCurrentOpenBillCycle();
        activeBillCycle = currentCycle?.bill_cycle ?? currentCycle?.billCycle ?? null;
      } catch (cycleError) {
        console.warn('Could not resolve the current bill cycle:', cycleError);
      }

      const isAreaSelected = !!currentSelectedArea;
      const permittedAreaCodes = getPermittedAreaCodes();
      const scopeName = isAreaSelected
        ? 'Selected Area'
        : permittedAreaCodes.length === 1
        ? `Area ${permittedAreaCodes[0]}`
        : `All Areas (${permittedAreaCodes.length})`;

      const activeDeveloperTotal = await getActiveDeveloperCount(currentSelectedArea);

      const response = await getUserReadingStatus(false, false);

      if (response.success && response.summary) {
        let transformedData;

        if (isAreaSelected && response.area_reading_status) {
          const selectedAreaData = response.area_reading_status.find(
            area => matchAreaCode(area.area_code, currentSelectedArea)
          );

          if (selectedAreaData) {
            transformedData = {
              receivedCount: Number(selectedAreaData.customers_with_readings) || 0,
              totalCount: Number(activeDeveloperTotal) || 0,
              areaCode: selectedAreaData.area_code,
              areaName: selectedAreaData.area_name,
              activeBillCycle
            };
          } else {
            transformedData = {
              receivedCount: 0,
              totalCount: Number(activeDeveloperTotal) || 0,
              areaCode: currentSelectedArea,
              areaName: 'Selected Area',
              activeBillCycle
            };
          }
        } else {
          transformedData = {
            receivedCount: Number(response.summary.total_customers_with_readings) || 0,
            totalCount: Number(activeDeveloperTotal) || 0,
            areaCode: null,
            areaName: scopeName,
            activeBillCycle
          };
        }

        setMeterData(transformedData);
        await loadInvoiceSummary(
          currentSelectedArea,
          transformedData.receivedCount,
          activeBillCycle
        );
      } else {
        console.warn('No reading status data available:', response.message);
        const fallbackTotal = Number(await getActiveDeveloperCount(currentSelectedArea).catch(() => 0)) || 0;
        setMeterData({
          receivedCount: 0,
          totalCount: fallbackTotal,
          areaCode: currentSelectedArea,
          areaName: currentSelectedArea ? 'Selected Area' : 'No Data',
          activeBillCycle
        });
        setInvoiceData({ createdCount: 0, pendingCount: 0, totalCount: 0 });
      }

    } catch (error) {
      console.error('Error fetching meter reading data:', error);
      setError(error.message);
      if (!window.__userLoggingOut) toast.error('Failed to load meter reading status');

      setMeterData({
        receivedCount: 0,
        totalCount: Number(selectedAreaCode ? (Number(await getActiveDeveloperCount(selectedAreaCode).catch(() => 0)) || 0) : 0),
        areaCode: selectedAreaCode,
        areaName: selectedAreaCode ? 'Selected Area' : 'Error Loading Data',
        activeBillCycle
      });
      setInvoiceData({ createdCount: 0, pendingCount: 0, totalCount: 0 });
    } finally {
      setLoading(false);
    }
  };

  if (error && (!meterData || (meterData.receivedCount === 0 && meterData.totalCount === 0))) {
    return (
      /* `contents` so this wrapper does not occupy a grid cell: the error card
         below spans the two chart columns it would otherwise replace. */
      <div className="contents">
        <div className="ds-card-refined w-full h-full flex flex-col md:col-span-2">
          <div className="ds-card-header">
            <div>
              <h3 className="ds-card-title">Reading &amp; Invoice Status</h3>
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
      </div>
    );
  }

  return (
    /* One fetch feeds both cards, so the reading and invoice figures can never
       disagree. `contents` promotes the two cards to sibling grid items,
       leaving them as two of the three columns beside the KPI stack. */
    <div className="contents">
      <div className="h-full min-w-0">
        <PieChartCard
          variant="readings"
          receivedCount={meterData?.receivedCount}
          totalCount={meterData?.totalCount}
          areaCode={meterData?.areaCode}
          areaName={meterData?.areaName}
          activeBillCycle={meterData?.activeBillCycle}
          isLoading={loading}
        />
      </div>

      <div className="h-full min-w-0">
        <PieChartCard
          variant="invoices"
          receivedCount={meterData?.receivedCount}
          totalCount={meterData?.totalCount}
          areaCode={meterData?.areaCode}
          areaName={meterData?.areaName}
          activeBillCycle={meterData?.activeBillCycle}
          isLoading={loading}
          invoiceCreatedCount={invoiceData.createdCount}
        />
      </div>
    </div>
  );
};

export default JobStatusPie;