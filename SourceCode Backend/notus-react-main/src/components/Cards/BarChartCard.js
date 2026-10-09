// src/components/Cards/BarChartCard.js
import React, { useRef, useEffect, useState } from "react";
import { useHistory } from "react-router-dom";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const BarChartCard = ({ data, isLoading }) => {
  const chartRef = useRef(null);
  const history = useHistory();
  const [key, setKey] = useState(0);
  const [screenSize, setScreenSize] = useState("desktop");

  // Check screen size
  const checkScreenSize = () => {
    const width = window.innerWidth;
    if (width < 768) {
      setScreenSize("mobile");
    } else if (width >= 768 && width < 1024) {
      setScreenSize("tablet");
    } else {
      setScreenSize("desktop");
    }
  };

  // Debounce function
  const useDebounce = (value, delay) => {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
      const handler = setTimeout(() => {
        setDebouncedValue(value);
      }, delay);

      return () => {
        clearTimeout(handler);
      };
    }, [value, delay]);

    return debouncedValue;
  };

  const [resizeCount, setResizeCount] = useState(0);
  const debouncedResizeCount = useDebounce(resizeCount, 100);

  useEffect(() => {
    checkScreenSize();

    const handleResize = () => {
      checkScreenSize();
      setResizeCount((prev) => prev + 1);
    };

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  useEffect(() => {
    if (debouncedResizeCount > 0) {
      setKey((prevKey) => prevKey + 1);
    }
  }, [debouncedResizeCount]);

  // FIXED: Handle bar clicks using chart.js onClick option
  const handleChartClick = (event, elements) => {
    if (elements.length === 0 || !data || data.length === 0) return;

    const elementIndex = elements[0].index;
    if (elementIndex >= 0 && elementIndex < data.length) {
      const errorItem = data[elementIndex];
      const errorCode = errorItem.error_code;
      const errorName = errorItem.error_name;
      const accountCount = errorItem.account_count;

      // NEW: Check if bar value is zero - if so, do nothing (make it un-clickable)
      if (accountCount === 0) {
        return; // Don't redirect for zero-value bars
      }

      // NEW: Special handling for "Unread" bar - redirect to pendReadings
      if (errorName === "Unread" || errorCode === 8) {
        history.push("/pendReadings");
        return;
      }

      // Convert error name to URL-friendly format
      const errorNameSlug = errorName.toLowerCase().replace(/\s+/g, "-");

      // Redirect to tempReadings with error filter
      history.push(`/tempReadings/${errorNameSlug}?errorCode=${errorCode}`);
    }
  };

  // Get chart configuration based on screen size
  const getChartConfig = () => {
    switch (screenSize) {
      case "mobile":
        return {
          containerPadding: "p-3",
          chartHeight: "h-64",
          titleSize: "text-section",
          textSize: "text-xs",
        };
      case "tablet":
        return {
          containerPadding: "p-4",
          chartHeight: "h-72",
          titleSize: "text-section",
          textSize: "text-sm",
        };
      default: // desktop
        return {
          containerPadding: "p-5",
          chartHeight: "h-[301px]",
          titleSize: "text-section",
          textSize: "text-sm",
        };
    }
  };

  const chartConfig = getChartConfig();

  if (isLoading) {
    return (
      <div
        className={`ds-card w-full max-w-full mx-auto text-center ${chartConfig.containerPadding}`}
      >
        <h2 className={`font-semibold mb-2 ${chartConfig.titleSize}`}>
          Error Status
        </h2>
        <div className="flex justify-center items-center h-64">
          <div className="ds-spinner"></div>
        </div>
      </div>
    );
  }

  // Check if data is valid
  const isDataValid = data && Array.isArray(data) && data.length > 0;
  const actualData = isDataValid ? data : [];

  if (!isDataValid) {
    return (
      <div
        className={`ds-card w-full max-w-full mx-auto text-center ${chartConfig.containerPadding}`}
      >
        <h2 className={`font-semibold mb-2 ${chartConfig.titleSize}`}>
          Error Status
        </h2>
        <div className="flex justify-center items-center h-64">
          <div className="text-ink-500">
            <i className="fas fa-chart-bar text-lg text-ink-400"></i>
            <p>No error data available</p>
            <p className="text-sm">
              Error statistics will appear here once available
            </p>
          </div>
        </div>
      </div>
    );
  }

  const labels = actualData.map((item) => item.error_name);
  const counts = actualData.map((item) => item.account_count);
  const totalCount = counts.reduce((a, b) => a + b, 0);

  const chartData = {
    labels,
    datasets: [
      {
        label: "Accounts",
        data: counts,
        // Navy bars; muted grey where the count is zero. Flat, no borders.
        backgroundColor: counts.map((count) =>
          count === 0 ? "#D3DBE6" : "#1A4D80" // ink-300 : navy-600
        ),
        borderWidth: 0,
        borderRadius: 2,
        borderSkipped: false,
        categoryPercentage: 0.72,
        barPercentage: 0.9,
        hoverBackgroundColor: counts.map((count) =>
          count === 0 ? "#D3DBE6" : "#0D3A68" // ink-300 : navy-700
        ),
      },
    ],
  };

  // FIXED: Chart options with proper onClick handler
  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: function (context) {
            const percentage =
              totalCount > 0
                ? ((context.parsed.y / totalCount) * 100).toFixed(1)
                : "0";
            return `${context.label}: ${context.parsed.y} accounts (${percentage}%)`;
          },
        },
      },
      // NEW: Customize x-axis ticks to include count below the label
      xAxisTicksWithCount: {
        display: true,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          precision: 0,
        },
      },
      x: {
        ticks: {
          maxRotation: 45,
          minRotation: 45,
          callback: function(value, index) {
            // Get the original label and count
            const label = this.getLabelForValue(value);
            const count = counts[index];
            
            // Only show count if greater than 0
            if (count > 0) {
              return `${label}\n(${count})`;
            }
            return label;
          },
          font: {
            size: 11,
          },
        },
      },
    },
    // FIXED: Use proper onClick handler instead of DOM event listeners
    onClick: handleChartClick,
    interaction: {
      mode: "nearest",
      axis: "x",
      intersect: false,
    },
    // FIXED: Add these options to prevent the null error
    events: ["click", "mousemove", "mouseout", "touchstart", "touchmove"],
    onHover: (event, elements) => {
      // Change cursor style on hover
      if (event.native.target) {
        if (elements.length > 0) {
          const elementIndex = elements[0].index;
          const barValue = counts[elementIndex];

          // Show 'not-allowed' cursor for zero bars, 'pointer' for non-zero bars
          event.native.target.style.cursor =
            barValue === 0 ? "not-allowed" : "pointer";
        } else {
          event.native.target.style.cursor = "default";
        }
      }
    },
  };

  // NEW: Custom plugin to style the count text in red color
  const barCountPlugin = {
    id: 'barCountPlugin',
    beforeDraw(chart) {
      // This plugin is now used to ensure proper rendering
    },
    afterDraw(chart) {
      // We're handling this in x-axis ticks callback now
    }
  };

  return (
    <div
      className={`ds-card w-full max-w-full mx-auto text-center ${chartConfig.containerPadding}`}
    >
      <h2 className={`font-semibold mb-2 ${chartConfig.titleSize}`}>
        Error Status
      </h2>
      <div className={chartConfig.chartHeight}>
        <Bar 
          key={key} 
          ref={chartRef} 
          data={chartData} 
          options={chartOptions} 
          plugins={[barCountPlugin]} // Keep plugin for consistency
        />
      </div>
    </div>
  );
};

export default BarChartCard;