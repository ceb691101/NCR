import React, { useState, useEffect } from "react";
import CardStats from "components/Cards/CardStats.js";
import { Link } from "react-router-dom";
import { getUserReadingStatus } from "services/readingStatusService";
import {
  getSelectedAreaCode,
  getPermittedAreaCodes,
} from "services/AreaAndBillService";
import { getActiveDeveloperCount } from "services/developerRegistrationService";

export default function HeaderStats() {

  const [readingStatusData, setReadingStatusData] = useState(null);
  const [activeDeveloperCount, setActiveDeveloperCount] = useState(0);
  const [selectedAreaCode, setSelectedAreaCode] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userCategory, setUserCategory] = useState(null);

  useEffect(() => {
    const category = sessionStorage.getItem("user_category");
    setUserCategory(category);
  }, []);

  useEffect(() => {
    const handleAreaChange = () => loadReadingStatus();
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () =>
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
  }, []);

  useEffect(() => {
    loadReadingStatus();
  }, []);

  const loadReadingStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const currentSelectedArea = getSelectedAreaCode();
      setSelectedAreaCode(currentSelectedArea);
      const response = await getUserReadingStatus(false, false);
      if (response.success) {
        setReadingStatusData(response);
      } else {
        setError("Failed to load reading status: " + response.message);
      }
      try {
        const developerCount = await getActiveDeveloperCount(currentSelectedArea);
        setActiveDeveloperCount(developerCount);
      } catch (countError) {
        console.error("Error loading active developer count:", countError);
        setActiveDeveloperCount(0);
      }
    } catch (err) {
      setError("Error loading reading status");
    } finally {
      setLoading(false);
    }
  };

  const matchAreaCode = (a1, a2) => {
    if (!a1 || !a2) return false;
    const s1 = String(a1).replace(/^0+/, '').trim();
    const s2 = String(a2).replace(/^0+/, '').trim();
    return s1 === s2;
  };

  // No selection means every permitted area is already scoped by the backend, so the
  // server summary covers exactly what the user is allowed to see.
  const isAreaSelected = !!selectedAreaCode;

  const getPendingReadingsCount = () => {
    if (!readingStatusData?.area_reading_status) return 0;
    if (isAreaSelected) {
      const area = readingStatusData.area_reading_status.find(
        (a) => matchAreaCode(a.area_code, selectedAreaCode)
      );
      return area ? area.customers_without_readings : 0;
    }
    return readingStatusData.summary?.total_customers_without_readings || 0;
  };

  const getReceivedReadingsCount = () => {
    if (!readingStatusData?.area_reading_status) return 0;
    if (isAreaSelected) {
      const area = readingStatusData.area_reading_status.find(
        (a) => matchAreaCode(a.area_code, selectedAreaCode)
      );
      return area ? area.customers_with_readings : 0;
    }
    return readingStatusData.summary?.total_customers_with_readings || 0;
  };

  const permittedAreaCodes = getPermittedAreaCodes();
  const scopeLabel = isAreaSelected
    ? `Area ${selectedAreaCode}`
    : permittedAreaCodes.length === 1
    ? `Area ${permittedAreaCodes[0]}`
    : `All ${permittedAreaCodes.length} permitted areas`;

  return (
    <section aria-labelledby="ncre-kpi-heading" className="h-full">
      

      <div className="grid grid-cols-1 gap-ds-3 min-w-0">
        <Link to="/pendReadings" className="block h-full min-w-0">
          <CardStats
            statSubtitle="Pending Readings"
            statTitle={getPendingReadingsCount()}
            isLoading={loading}
            statDescripiton="Awaiting readings"
            statIconName="fas fa-file-alt"
            statIconColor="bg-warning-50 text-warning-700 border-warning-100"
            statusTone="warning"
            sparklineType="red"
            className="ds-kpi-compact h-full"
            isClickable={true}
          />
        </Link>

        <Link to="/tempReadings" className="block h-full min-w-0">
          <CardStats
            statSubtitle="Received Readings"
            statTitle={getReceivedReadingsCount()}
            isLoading={loading}
            statDescripiton="Received this cycle"
            statIconName="fas fa-check-circle"
            statIconColor="bg-success-50 text-success-700 border-success-100"
            statusTone="success"
            sparklineType="green"
            className="ds-kpi-compact h-full"
            isClickable={true}
          />
        </Link>

        <Link to="/bulkCustomers" className="block h-full min-w-0">
          <CardStats
            statSubtitle="NCRE Developers"
            statTitle={activeDeveloperCount}
            isLoading={loading}
            statDescripiton="Active developers"
            statIconName="fas fa-users"
            statIconColor="bg-navy-50 text-navy-700 border-navy-100"
            statusTone="info"
            sparklineType="blue"
            className="ds-kpi-compact h-full"
            isClickable={true}
          />
        </Link>
      </div>

      {error && (
        <div className="mt-ds-4">
          <div className="ds-notice ds-notice-critical">
            <i className="fas fa-circle-exclamation text-critical-600 mt-0.5 flex-none"></i>
            <p className="flex-1 min-w-0">
              <span className="font-semibold">Reading status unavailable.</span> {error}
            </p>
            <button
              onClick={loadReadingStatus}
              className="ds-btn ds-btn-secondary ds-btn-sm flex-none"
            >
              Retry
            </button>
          </div>
        </div>
      )}
    </section>
  );
}