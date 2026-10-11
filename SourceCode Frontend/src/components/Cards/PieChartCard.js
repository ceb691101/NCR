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
  // "readings" | "invoices" | "both". The dashboard renders two separate cards,
  // so this component can also draw a single donut per card.
  variant = "both",
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
    const [animatedPercentage, setAnimatedPercentage] = useState(0);

    useEffect(() => {
      const frame = requestAnimationFrame(() => setAnimatedPercentage(percentage));
      return () => cancelAnimationFrame(frame);
    }, [percentage]);

    const animatedDash = circumference * (animatedPercentage / 100);
    const pendingDash = circumference - animatedDash;

    return (
      <button
        type="button"
        onClick={onClick}
        className="flex h-full min-h-[288px] w-full flex-col items-center justify-center gap-3 rounded-xl border border-navy-100 bg-white p-4 text-center shadow-sm transition-colors hover:border-navy-200 hover:bg-ink-50/40 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20 sm:p-5"
        title={navLabel}
      >
        <div className="relative mx-auto aspect-square w-full max-w-[216px]">
          <svg viewBox="0 0 120 120" className="h-full w-full -rotate-90" aria-label={`${label} chart`}>
            <circle cx="60" cy="60" r={radius} stroke={pendingColor} strokeWidth="12" fill="none" />
            <circle
              cx="60"
              cy="60"
              r={radius}
              stroke={color}
              strokeWidth="12"
              fill="none"
              strokeDasharray={`${animatedDash} ${pendingDash}`}
              strokeLinecap="round"
              style={{ transition: 'stroke-dasharray 850ms ease, stroke 200ms ease' }}
            />
          </svg>
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-2xl font-bold text-ink-900 tabular-nums">{Math.round(animatedPercentage)}%</span>
          </div>
        </div>

        <div className="space-y-0.5 text-center">
          <div className="text-label font-semibold uppercase tracking-[0.1em] text-ink-500">{label}</div>
          <div className="flex items-center justify-center gap-1.5 text-body-sm text-ink-700 tabular-nums">
            <span className="inline-flex items-center gap-1.5">
              <span className="h-2 w-2 rounded-full" style={{ backgroundColor: color }} aria-hidden="true"></span>
              {Number(value).toLocaleString()}
            </span>
            <span className="text-ink-400">/</span>
            <span className="text-ink-500">{Number(total).toLocaleString()}</span>
          </div>
        </div>
      </button>
    );
  };

  /* Title depends on which donut this card is showing. */
  const variantTitle =
    variant === 'readings'
      ? 'Reading Status'
      : variant === 'invoices'
        ? 'Invoice Status'
        : 'Meter Reading Status';

  const variantSubtitle =
    variant === 'readings'
      ? 'Readings received vs. outstanding'
      : variant === 'invoices'
        ? 'Invoices created vs. still to raise'
        : 'Readings received vs. outstanding';

  /* Shared header: title on the left, scope context on the right. */
  const renderHeader = () => (
    <div className="ds-card-header">
      <div className="min-w-0">
        <h3 className={`ds-card-title ${chartConfig.titleSize}`}>{variantTitle}</h3>
        {variant !== 'invoices' && (areaName || areaCode) && (
          <p className="ds-card-subtitle mt-0.5 truncate">
            {(areaName || `Area ${areaCode}`).trim()}
          </p>
        )}
        {variant === 'invoices' && (
          <p className="ds-card-subtitle mt-0.5 truncate">
            {variantSubtitle}
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
      <div className="ds-card-refined w-full h-full flex flex-col">
        {renderHeader()}
        <div className="ds-card-body flex-1 min-h-0 flex flex-col justify-center items-center gap-2.5 min-h-[150px]">
          <span className="ds-spinner" aria-hidden="true"></span>
          <p className="ds-body-sm text-ink-500">Loading {variantTitle.toLowerCase()} data...</p>
        </div>
      </div>
    );
  }

  // No valid data — professional empty state instead of a placeholder chart.
  if (!isDataValid) {
    return (
      <div className="ds-card-refined w-full h-full flex flex-col">
        {renderHeader()}
        <div className="ds-card-body flex-1 min-h-0 flex items-center justify-center">
          <div className="ds-empty max-w-[280px]">
            <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-gauge"></i>
            </span>
            <p className="ds-section-title mt-1">No data available</p>
            <p className="ds-body-sm">
              {areaCode
                ? `Nothing reported for ${(areaName || `Area ${areaCode}`).trim()} in the current cycle.`
                : "Select an area to view this breakdown."}
            </p>
          </div>
        </div>
      </div>
    );
  }

  // The invoice donut is drawn against the received-reading count, so its
  // pending share is simply the remainder of that same bar.
  const invoiceTotal = Math.max(0, Number(receivedCount) || 0);

  const showReadings = variant === 'readings' || variant === 'both';
  const showInvoices = variant === 'invoices' || variant === 'both';

  return (
    <div className="ds-card-refined w-full h-full flex flex-col">
      {renderHeader()}

      <div className="ds-card-body flex flex-1 min-h-0 items-stretch">
        <div className="grid h-full min-h-[288px] w-full grid-cols-1">
          {showReadings && (
            <DonutMeter
              label="Received"
              value={receivedCount}
              total={Math.max(totalCount, 0)}
              color="#059669"
              pendingColor="#E5E7EB"
              onClick={() => history.push('/tempReadings')}
              navLabel="Open received readings"
            />
          )}

          {showInvoices && (
            <DonutMeter
              label="Created"
              value={invoiceCreatedCount}
              total={Math.max(invoiceTotal, 0)}
              color="#4F5FD0"
              pendingColor="#E5E7EB"
              onClick={() => history.push('/admin/invoices')}
              navLabel="Open invoice management"
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default PieChartCard;