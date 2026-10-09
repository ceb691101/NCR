// src/views/admin/DifferenceChart.js
import React, { useEffect, useState, useCallback } from "react";
import DifferenceBarChartCard from "../../components/Cards/DifferenceBarChartCard";
import {
  getSelectedAreaCode,
  getEffectiveAreaCodes,
} from "../../services/AreaAndBillService";

const DifferenceChart = () => {
  const [chartData, setChartData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [areaLabel, setAreaLabel] = useState("");
  const baseUrl = process.env.REACT_APP_API_BASE_URL;

  // areaCode === null means "every permitted area"; the backend resolves each area's
  // own active bill cycle in that case.
  const fetchDifferenceStatistics = useCallback(
    async (areaCode) => {
      if (areaCode !== null && areaCode === undefined) {
        setChartData([]);
        return;
      }

      setIsLoading(true);
      try {
        const sessionId = sessionStorage.getItem("session_id");
        const userId = sessionStorage.getItem("user_id");

        if (!sessionId || !userId) {
          console.error("Session ID or User ID not found in session storage");
          setChartData([]);
          setIsLoading(false);
          return;
        }

        const effectiveAreas = areaCode
          ? [areaCode]
          : getEffectiveAreaCodes();

        if (!effectiveAreas.length) {
          setChartData([]);
          setAreaLabel("");
          return;
        }

        const requestBody = {
          session_id: sessionId,
          user_id: userId,
        };

        if (areaCode) {
          requestBody.area_code = areaCode;
        } else {
          // Explicit list so the response echoes back what was covered.
          requestBody.area_code = "ALL";
          requestBody.area_codes = effectiveAreas;
        }

        const response = await fetch(
          `${baseUrl}/api/v1/difference-statistics/area-differences`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "Authorization": "Basic " + btoa("user:admin123"),
              "X-Session-Id": sessionId,
            },
            credentials: "include",
            body: JSON.stringify(requestBody),
          }
        );

        const data = await response.json().catch(() => null);

        if (!response.ok) {
          console.error(
            "Difference statistics API returned error status:",
            response.status,
            data ? data.message : "No error details"
          );
          setChartData([]);
          return;
        }

        if (data && data.success && Array.isArray(data.differences)) {
          setChartData(data.differences);
          const covered = Array.isArray(data.area_codes) ? data.area_codes : effectiveAreas;
          setAreaLabel(
            covered.length === 1 ? `Area ${covered[0]}` : `All Areas (${covered.length})`
          );
        } else {
          console.warn(
            "Difference data retrieval returned unsuccessful or empty:",
            data ? data.message : "Unknown error"
          );
          setChartData([]);
        }
      } catch (error) {
        console.error("Error fetching difference statistics:", error);
        setChartData([]);
      } finally {
        setIsLoading(false);
      }
    },
    [baseUrl]
  );

  // Initial load
  useEffect(() => {
    fetchDifferenceStatistics(getSelectedAreaCode());
  }, [fetchDifferenceStatistics]);

  // Listen for area changes
  useEffect(() => {
    const handleAreaChange = (event) => {
      const newAreaCode =
        event && event.detail && event.detail.currentArea !== undefined
          ? event.detail.currentArea || null
          : getSelectedAreaCode();
      fetchDifferenceStatistics(newAreaCode);
    };

    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, [fetchDifferenceStatistics]);

  return (
    /* h-full lets the card fill the stretched grid cell so it matches the
       reading-status card beside it. */
    <div className="h-full">
      <DifferenceBarChartCard
        data={chartData}
        isLoading={isLoading}
        areaLabel={areaLabel}
      />
    </div>
  );
};

export default DifferenceChart;
