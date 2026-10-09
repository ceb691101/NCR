import React, { useRef, useEffect, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { Pie } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend
} from 'chart.js';

// Register only once
ChartJS.register(ArcElement, Tooltip, Legend);

const PieChartCard = ({
  receivedCount,
  totalCount,
  areaCode,
  areaName,
  activeBillCycle,
  isLoading = false,
  invoiceCreatedCount = 0,
  invoicePendingCount = 0,
}) => {
  const history = useHistory();
  const chartRef = useRef(null);
  const [key, setKey] = useState(0); // Key to force re-render
  const [screenSize, setScreenSize] = useState('desktop');

  // Check if data is available and valid
  const isDataValid = (
    receivedCount !== undefined && 
    totalCount !== undefined &&
    !isNaN(receivedCount) && 
    !isNaN(totalCount) &&
    totalCount > 0
  );

  // Check screen size
  const checkScreenSize = () => {
    const width = window.innerWidth;
    if (width < 768) {
      setScreenSize('mobile');
    } else if (width >= 768 && width < 1024) {
      setScreenSize('tablet');
    } else {
      setScreenSize('desktop');
    }
  };

  // Debounce function to limit resize events
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

  // Force chart re-render on window resize with debounce
  const [resizeCount, setResizeCount] = useState(0);
  const debouncedResizeCount = useDebounce(resizeCount, 100);

  useEffect(() => {
    checkScreenSize(); // Initial check
    
    const handleResize = () => {
      checkScreenSize();
      setResizeCount(prev => prev + 1);
    };

    window.addEventListener('resize', handleResize);
    
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  useEffect(() => {
    if (debouncedResizeCount > 0) {
      setKey(prevKey => prevKey + 1);
    }
  }, [debouncedResizeCount]);

  // Chart configuration by screen size.
  // Deliberately minimal: a flat doughnut with a generous hole so the total can
  // sit in the middle. No slice borders, no gradients, no 3D.
  const getChartConfig = () => {
    switch (screenSize) {
      case 'mobile':
        return {
          cutout: '64%',
          chartSize: 'max-w-[180px]',
          tooltipFontSize: 11,
          titleSize: 'text-section'
        };
      case 'tablet':
        return {
          cutout: '68%',
          chartSize: 'max-w-[200px]',
          tooltipFontSize: 12,
          titleSize: 'text-section'
        };
      default: // desktop
        return {
          cutout: '70%',
          chartSize: 'max-w-[216px]',
          tooltipFontSize: 12,
          titleSize: 'text-section'
        };
    }
  };

  const chartConfig = getChartConfig();

  const safePct = (value, total) => {
    const safeTotal = Number(total) || 0;
    const safeValue = Number(value) || 0;
    if (safeTotal <= 0) return 0;
    return Math.min(100, (safeValue / safeTotal) * 100);
  };

  const DonutMeter = ({ label, value, total, color, pendingColor, onClick, navLabel }) => {
    const percentage = safePct(value, total);
    const radius = 42;
    const circumference = 2 * Math.PI * radius;
    const dash = circumference * (percentage / 100);
    const pendingDash = circumference - dash;

    return (
      <button
        type="button"
        onClick={onClick}
        className="flex w-full flex-col items-center justify-center rounded-xl border border-ink-200 bg-ink-50/40 p-3 text-center transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
        title={navLabel}
      >
        <div className="relative flex h-32 w-32 items-center justify-center">
          <svg viewBox="0 0 120 120" className="h-28 w-28 -rotate-90" aria-label={`${label} chart`}>
            <circle cx="60" cy="60" r={radius} stroke={pendingColor} strokeWidth="12" fill="none" />
            <circle
              cx="60"
              cy="60"
              r={radius}
              stroke={color}
              strokeWidth="12"
              fill="none"
              strokeDasharray={`${dash} ${pendingDash}`}
              strokeLinecap="round"
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-2xl font-bold text-ink-900">{Math.round(percentage)}%</span>
          </div>
        </div>

        <div className="mt-2 space-y-1 text-center">
          <div className="text-xs font-semibold uppercase tracking-[0.12em] text-ink-500">{label}</div>
          <div className="flex items-center justify-center gap-3 text-sm text-ink-700">
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: color }} aria-hidden="true"></span>
              {Number(value).toLocaleString()}
            </span>
            <span className="text-ink-400">/</span>
            <span className="text-ink-500">{Number(total).toLocaleString()}</span>
          </div>
        </div>
      </button>
    );
  };

  /* Shared header: title on the left, scope context on the right. */
  const renderHeader = () => (
    <div className="ds-card-header">
      <div className="min-w-0">
        <h3 className={`ds-card-title ${chartConfig.titleSize}`}>Meter Reading Status</h3>
        {(areaName || areaCode) && (
          <p className="ds-card-subtitle mt-0.5 truncate">
            {(areaName || `Area ${areaCode}`).trim()}
          </p>
        )}
      </div>

      <div className="flex-none flex items-center gap-2">
        {activeBillCycle !== null && activeBillCycle !== undefined ? (
          <span className="ds-badge ds-badge-info">Cycle {activeBillCycle}</span>
        ) : (
          <span className="ds-badge ds-badge-neutral">No active cycle</span>
        )}
      </div>
    </div>
  );

  if (isLoading) {
    return (
      <div className="ds-card w-full h-full flex flex-col">
        {renderHeader()}
        <div className="ds-card-body flex-1 min-h-0 flex flex-col justify-center items-center gap-3 min-h-[240px]">
          <span className="ds-spinner" aria-hidden="true"></span>
          <p className="ds-body-sm text-ink-500">Loading meter reading data...</p>
        </div>
      </div>
    );
  }

  // No valid data — professional empty state instead of a placeholder chart.
  if (!isDataValid) {
    return (
      <div className="ds-card w-full h-full flex flex-col">
        {renderHeader()}
        <div className="ds-card-body flex-1 min-h-0 flex items-center justify-center">
          <div className="ds-empty max-w-[320px]">
            <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-gauge"></i>
            </span>
            <p className="ds-section-title mt-1">No reading status available</p>
            <p className="ds-body-sm">
              {areaCode
                ? `No meter reading counts have been reported for ${(areaName || `Area ${areaCode}`).trim()}.`
                : "Select an area to view meter reading status."}
            </p>
            <p className="ds-caption">
              This panel will populate once reading counts are available for the selected scope.
            </p>
          </div>
        </div>
      </div>
    );
  }

  const notReceivedCount = Math.max(0, totalCount - receivedCount);
  const readingPct = safePct(receivedCount, totalCount);
  const readingPendingPct = safePct(notReceivedCount, totalCount);
  const invoiceTotal = Math.max(0, Number(receivedCount) || 0);
  const invoiceCreatedPct = safePct(invoiceCreatedCount, invoiceTotal);
  const invoicePendingPct = safePct(invoicePendingCount, invoiceTotal);

  return (
    <div className="ds-card w-full h-full flex flex-col">
      {renderHeader()}

      <div className="ds-card-body flex-1 min-h-0">
        <div className="grid gap-4 md:grid-cols-2 md:items-center">
          <DonutMeter
            label="Reading Status"
            value={receivedCount}
            total={Math.max(totalCount, 0)}
            color="#059669"
            pendingColor="#E5E7EB"
            onClick={() => history.push('/tempReadings')}
            navLabel="Open received readings"
          />

          <DonutMeter
            label="Invoice Status"
            value={invoiceCreatedCount}
            total={Math.max(invoiceTotal, 0)}
            color="#4F5FD0"
            pendingColor="#E5E7EB"
            onClick={() => history.push('/admin/invoices')}
            navLabel="Open invoice management"
          />
        </div>

        <div className="mt-4 grid gap-3 border-t border-ink-200 pt-3 md:grid-cols-2">
          <button
            type="button"
            onClick={() => history.push('/tempReadings')}
            className="flex items-center justify-between gap-2 rounded-lg border border-ink-200 bg-white px-2.5 py-2 text-left text-sm transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
            title="Open received readings"
          >
            <span className="flex min-w-0 items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-success-600" aria-hidden="true"></span>
              <span className="truncate text-ink-700">Received</span>
            </span>
            <span className="flex items-baseline gap-1.5 text-ink-900">
              <span className="font-semibold">{Number(receivedCount || 0).toLocaleString()}</span>
              <span className="text-xs text-ink-500">{Math.round(readingPct)}%</span>
            </span>
          </button>

          <button
            type="button"
            onClick={() => history.push('/pendReadings')}
            className="flex items-center justify-between gap-2 rounded-lg border border-ink-200 bg-white px-2.5 py-2 text-left text-sm transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
            title="Open pending readings"
          >
            <span className="flex min-w-0 items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-ink-300" aria-hidden="true"></span>
              <span className="truncate text-ink-700">Pending</span>
            </span>
            <span className="flex items-baseline gap-1.5 text-ink-900">
              <span className="font-semibold">{Number(notReceivedCount || 0).toLocaleString()}</span>
              <span className="text-xs text-ink-500">{Math.round(readingPendingPct)}%</span>
            </span>
          </button>

          <button
            type="button"
            onClick={() => history.push('/admin/invoices')}
            className="flex items-center justify-between gap-2 rounded-lg border border-ink-200 bg-white px-2.5 py-2 text-left text-sm transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
            title="Open invoice management"
          >
            <span className="flex min-w-0 items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-[#4F5FD0]" aria-hidden="true"></span>
              <span className="truncate text-ink-700">Created</span>
            </span>
            <span className="flex items-baseline gap-1.5 text-ink-900">
              <span className="font-semibold">{Number(invoiceCreatedCount || 0).toLocaleString()}</span>
              <span className="text-xs text-ink-500">{Math.round(invoiceCreatedPct)}%</span>
            </span>
          </button>

          <button
            type="button"
            onClick={() => history.push('/admin/invoices')}
            className="flex items-center justify-between gap-2 rounded-lg border border-ink-200 bg-white px-2.5 py-2 text-left text-sm transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
            title="Open invoice management"
          >
            <span className="flex min-w-0 items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-ink-300" aria-hidden="true"></span>
              <span className="truncate text-ink-700">Pending</span>
            </span>
            <span className="flex items-baseline gap-1.5 text-ink-900">
              <span className="font-semibold">{Number(invoicePendingCount || 0).toLocaleString()}</span>
              <span className="text-xs text-ink-500">{Math.round(invoicePendingPct)}%</span>
            </span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default PieChartCard;