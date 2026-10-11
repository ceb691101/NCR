// src/views/admin/ApplicationStatusChart.js
import React, { useEffect, useState } from "react";
import BarChartCard from "../../components/Cards/BarChartCard";
import {
  getSelectedAreaCode,
  getEffectiveAreaCodes,
} from "../../services/AreaAndBillService";

const ApplicationStatusChart = () => {
  const [chartData, setChartData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const baseUrl = process.env.REACT_APP_API_BASE_URL;

  // areaCode === null means every permitted area; the backend aggregates those.
  const fetchErrorStatistics = async (areaCode) => {
    setIsLoading(true);
    try {
      const sessionId = sessionStorage.getItem("session_id");
      const userId = sessionStorage.getItem("user_id");

      if (!sessionId || !userId) {
        console.error("Session ID or User ID not found");
        return;
      }

      const body = {
        session_id: sessionId,
        user_id: userId,
      };

      if (areaCode) {
        body.area_code = areaCode;
      } else {
        body.area_code = "ALL";
        body.area_codes = getEffectiveAreaCodes();
      }

      const response = await fetch(`${baseUrl}/api/v1/error-statistics/area-statistics`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Basic " + btoa("user:admin123"),
          "X-Session-Id": sessionId,
        },
        credentials: "include",
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Error statistics response:", data);
      
      if (data.success && data.error_statistics) {
        transformErrorData(data.error_statistics);
      } else {
        console.error("Failed to fetch error statistics:", data.message);
        setChartData([]);
      }
      
    } catch (error) {
      console.error("Error fetching error statistics:", error);
      setChartData([]);
    } finally {
      setIsLoading(false);
    }
  };

  const transformErrorData = (errorStats) => {
    const errorData = [];
    
    // Define all error types including unread
    const errorTypes = [
      { code: 1, name: "High Consumption" },
      { code: 2, name: "Low Consumption" },
      { code: 3, name: "Reading Error" },
      { code: 4, name: "Charge Error" },
      { code: 5, name: "Negative Error" },
      { code: 6, name: "Total Charge Error" },
      { code: 7, name: "Zero Consumption" },
      { code: 8, name: "Unread" }
    ];

    errorTypes.forEach(errorType => {
      if (errorType.code === 8) {
        // Handle unread accounts
        errorData.push({
          error_code: 8,
          error_name: "Unread",
          account_count: errorStats.unread_accounts_count || 0
        });
      } else {
        // Handle regular error types
        const errorCount = errorStats.error_counts && errorStats.error_counts[errorType.code];
        if (errorCount) {
          errorData.push({
            error_code: errorCount.error_code,
            error_name: errorCount.error_name,
            account_count: errorCount.account_count
          });
        } else {
          // Include error types with zero count
          errorData.push({
            error_code: errorType.code,
            error_name: errorType.name,
            account_count: 0
          });
        }
      }
    });

    setChartData(errorData);
  };

  // Initial load: no selection means all permitted areas.
  useEffect(() => {
    fetchErrorStatistics(getSelectedAreaCode());
  }, []);

  // Listen for area changes
  useEffect(() => {
    const handleAreaChange = () => {
      fetchErrorStatistics(getSelectedAreaCode());
    };

    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => {
      window.removeEventListener("areaAndBill:changed", handleAreaChange);
    };
  }, []);

  return (
    <div>
      <BarChartCard 
        data={chartData} 
        isLoading={isLoading} 
      />
    </div>
  );
};

export default ApplicationStatusChart;