// src/components/Cards/DifferenceBarChartCard.js
import React, { useMemo, useState, useEffect } from "react";
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

/* ── Palette: NSO navy + blue (within) + yellow (outside) ────────────── */
const COLORS = {
  navy: "#0B2A43",
  within: "#2F6DB5",
  withinHover: "#1F548F",
  withinText: "#1F548F",
  outside: "#F2B705",
  outsideHover: "#D49A00",
  outsideText: "#8A6100",
  slate: "#64748B",
  border: "#E2E8F0",
};

const AXIS_WIDTH = 64;
const X_AXIS_HEIGHT = 72;
const PAD_TOP = 22;
const PAD_BOTTOM = 4;

/* ── Display helpers (no business logic) ─────────────────────────────── */
const num = (v) => Number(v) || 0;

const fmt = (v, min = 0, max = 3) =>
  Number(v).toLocaleString(undefined, {
    minimumFractionDigits: min,
    maximumFractionDigits: max,
  });

// Unchanged rule: an unconfigured limit counts as outside.
const isItemExceeded = (item) => {
  if (item.accept_ru == null || isNaN(Number(item.accept_ru))) return true;
  return Math.abs(num(item.difference)) > Number(item.accept_ru);
};

const folioLabel = (item) =>
  item.folio_no != null && String(item.folio_no).trim() !== ""
    ? String(item.folio_no).trim()
    : item.facility_name || "—";

/* Display-only scale transform for the Outside-limit view: a signed log scale so
   small and very large differences are both visible. Real values are always
   shown in labels, ticks and tooltips. */
const toDisplay = (v) => Math.sign(v) * Math.log10(1 + Math.abs(v));
const fromDisplay = (t) => Math.sign(t) * (Math.pow(10, Math.abs(t)) - 1);

const byFolio = (a, b) =>
  folioLabel(a).localeCompare(folioLabel(b), undefined, { numeric: true, sensitivity: "base" });

/* ── Inline plugin: value labels + accepted-limit reference line(s) ──── */
const guidePlugin = {
  id: "guides",
  afterDatasetsDraw(chart, _args, opts) {
    const { ctx, chartArea, scales } = chart;
    const y = scales.y;
    if (!y || !chartArea) return;

    // Explicit zero baseline (drawn over the grid so it is always visible,
    // including when all values are positive and zero sits at the chart bottom)
    const zeroY = y.getPixelForValue(0);
    if (zeroY >= chartArea.top - 1 && zeroY <= chartArea.bottom + 1) {
      ctx.save();
      ctx.strokeStyle = COLORS.navy;
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(chartArea.left, zeroY);
      ctx.lineTo(chartArea.right, zeroY);
      ctx.stroke();
      ctx.restore();
    }

    const meta = chart.getDatasetMeta(0);
    const values = (opts && opts.real) || chart.data.datasets[0].data;
    ctx.save();
    ctx.font = "600 10px ui-monospace, SFMono-Regular, Menlo, monospace";
    ctx.fillStyle = COLORS.navy;
    ctx.textAlign = "center";
    meta.data.forEach((bar, i) => {
      const v = values[i];
      const label = fmt(v, 0, 1);
      if (v >= 0) {
        ctx.textBaseline = "bottom";
        ctx.fillText(label, bar.x, bar.y - 3);
      } else {
        ctx.textBaseline = "top";
        ctx.fillText(label, bar.x, bar.y + 3);
      }
    });
    ctx.restore();

    if (opts && opts.value != null) {
      const positions = opts.mirror ? [opts.value, -opts.value] : [opts.value];
      ctx.save();
      ctx.strokeStyle = COLORS.navy;
      ctx.lineWidth = 1.5;
      ctx.setLineDash([6, 4]);
      positions.forEach((p) => {
        const py = y.getPixelForValue(p);
        if (py < chartArea.top || py > chartArea.bottom) return;
        ctx.beginPath();
        ctx.moveTo(chartArea.left, py);
        ctx.lineTo(chartArea.right, py);
        ctx.stroke();
      });
      ctx.setLineDash([]);
      const py = y.getPixelForValue(opts.value);
      if (py >= chartArea.top && py <= chartArea.bottom) {
        ctx.font = "700 10px system-ui, sans-serif";
        ctx.fillStyle = COLORS.navy;
        ctx.textAlign = "left";
        ctx.textBaseline = "bottom";
        ctx.fillText(`Limit ${fmt(opts.limit)}`, chartArea.left + 6, py - 3);
      }
      ctx.restore();
    }
  },
};

/* ── Segmented control button ────────────────────────────────────────── */
const SEG_BASE =
  "inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-caption font-semibold transition-colors cursor-pointer border focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 focus-visible:ring-[#0B2A43]";

const SEG_ACTIVE = {
  outside: "bg-[#F2B705] border-[#F2B705] text-[#0B2A43]",
  within: "bg-[#2F6DB5] border-[#2F6DB5] text-white",
  neutral: "bg-[#0B2A43] border-[#0B2A43] text-white",
};
const SEG_IDLE =
  "bg-white border-[#E2E8F0] text-[#64748B] hover:text-[#0B2A43] hover:border-[#64748B]";

const SegButton = ({ active, tone, onClick, children, title }) => (
  <button
    type="button"
    onClick={onClick}
    aria-pressed={active}
    title={title}
    className={`${SEG_BASE} ${active ? SEG_ACTIVE[tone] : SEG_IDLE}`}
  >
    {children}
  </button>
);

const CardHeader = ({ areaLabel }) => (
  <div className="ds-card-header">
    <div className="min-w-0">
      <div className="flex items-center gap-2 flex-wrap">
        <h3 className="ds-card-title text-section">Difference</h3>
        {areaLabel && (
          <span className="inline-flex items-center px-2 py-0.5 rounded-md text-caption font-medium bg-ink-100 text-ink-600 border border-ink-200">
            {areaLabel}
          </span>
        )}
      </div>
      <p className="ds-card-subtitle mt-0.5">Per-folio (developer) RU difference in kWh</p>
    </div>
  </div>
);

const DifferenceBarChartCard = ({ data, isLoading, areaLabel }) => {
  const [viewMode, setViewMode] = useState("chart"); // "chart" | "table"
  const [limitView, setLimitView] = useState("outside"); // "outside" | "within"
  const [scaleMode, setScaleMode] = useState("compressed"); // Outside view only
  const [isMobile, setIsMobile] = useState(
    typeof window !== "undefined" && window.innerWidth < 640
  );

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 640);
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  const actualData = useMemo(() => (Array.isArray(data) ? data : []), [data]);

  const { outsideItems, withinItems, sharedLimit } = useMemo(() => {
    const outside = [];
    const within = [];
    actualData.forEach((item) => (isItemExceeded(item) ? outside : within).push(item));

    const limits = new Set(
      actualData
        .filter((i) => i.accept_ru != null && !isNaN(Number(i.accept_ru)))
        .map((i) => Number(i.accept_ru))
    );
    return {
      outsideItems: outside.sort(byFolio), // folio-number order
      withinItems: within.sort(byFolio),
      sharedLimit: limits.size === 1 ? [...limits][0] : null,
    };
  }, [actualData]);

  const isOutside = limitView === "outside";
  const rows = isOutside ? outsideItems : withinItems;

  /* Shared y-scale so the fixed axis and the scrolling plot always agree */
  const compressed = isOutside && scaleMode === "compressed";
  const tf = compressed ? toDisplay : (v) => v;

  const yRange = useMemo(() => {
    const f = compressed ? toDisplay : (v) => v;
    const vals = rows.map((r) => f(num(r.difference)));
    const lim = sharedLimit != null ? f(sharedLimit) : 0;
    const maxV = Math.max(0, ...vals, lim);
    const minV = Math.min(0, ...vals);
    const hasNeg = minV < 0;
    const top = maxV === 0 ? 1 : maxV * 1.12;
    const bottom = hasNeg ? Math.min(minV * 1.12, -lim * 1.05) : 0;
    return { min: bottom, max: top, hasNeg };
  }, [rows, sharedLimit, compressed]);

  /* ── Loading ── */
  if (isLoading) {
    return (
      <div className="ds-card w-full min-w-0 flex flex-col">
        <CardHeader areaLabel={areaLabel} />
        <div className="ds-card-body flex flex-col justify-center items-center gap-3 min-h-[260px]">
          <span className="ds-spinner" aria-hidden="true"></span>
          <p className="ds-body-sm text-ink-500">Loading difference data...</p>
        </div>
      </div>
    );
  }

  /* ── No data at all ── */
  if (actualData.length === 0) {
    return (
      <div className="ds-card w-full min-w-0 flex flex-col">
        <CardHeader areaLabel={areaLabel} />
        <div className="ds-card-body flex items-center justify-center min-h-[240px]">
          <div className="ds-empty max-w-[380px]">
            <span className="w-11 h-11 rounded-lg bg-ink-100 border border-ink-200 text-ink-400 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-chart-column"></i>
            </span>
            <p className="ds-section-title mt-1">No difference data available</p>
            <p className="ds-body-sm">
              {areaLabel
                ? `No per-folio difference records have been reported for ${areaLabel}.`
                : "Select an area to load per-folio difference statistics."}
            </p>
            <p className="ds-caption">
              This panel will populate once folio difference records are available for the
              selected scope.
            </p>
          </div>
        </div>
      </div>
    );
  }

  /* ── Chart config ── */
  const barColor = isOutside ? COLORS.outside : COLORS.within;
  const barHover = isOutside ? COLORS.outsideHover : COLORS.withinHover;
  const slot = isMobile ? 32 : 40; // px per developer
  const chartHeight = isMobile ? 340 : 420;
  const plotWidth = rows.length * slot;

  const labels = rows.map(folioLabel);

  const sharedYScale = {
    min: yRange.min,
    max: yRange.max,
    ticks: {
      color: COLORS.slate,
      font: { size: 11 },
      maxTicksLimit: 7,
      callback: (v) => (compressed ? fmt(Math.round(fromDisplay(v) * 1000) / 1000, 0, 0) : fmt(v, 0, 3)),
    },
    ...(compressed
      ? {
          afterBuildTicks: (scale) => {
            const reals = [1, 10, 100, 1000, 10000, 100000, 1000000];
            if (sharedLimit != null) reals.push(sharedLimit);
            const all = [0, ...reals, ...reals.map((r) => -r)]
              .map(toDisplay)
              .filter((t) => t >= scale.min - 1e-9 && t <= scale.max + 1e-9)
              .sort((a, b) => a - b);
            scale.ticks = all.map((value) => ({ value }));
          },
        }
      : {}),
  };

  const sharedXTicks = {
    autoSkip: false,
    minRotation: 90,
    maxRotation: 90,
    font: { size: 10, family: "ui-monospace, SFMono-Regular, Menlo, monospace" },
    callback(value) {
      const label = this.getLabelForValue(value);
      return typeof label === "string" && label.length > 14 ? label.substring(0, 12) + "…" : label;
    },
  };

  const fixXHeight = (scale) => {
    scale.height = X_AXIS_HEIGHT;
  };

  const layout = { padding: { top: PAD_TOP, bottom: PAD_BOTTOM, left: 0, right: 8 } };

  const chartData = {
    labels,
    datasets: [
      {
        label: "Difference (kWh)",
        data: rows.map((r) => tf(num(r.difference))),
        backgroundColor: barColor,
        hoverBackgroundColor: barHover,
        borderWidth: 0,
        borderRadius: 2,
        borderSkipped: false,
        categoryPercentage: 0.85,
        barPercentage: 0.8,
      },
    ],
  };

  /* Scrolling plot: bars, gridlines, zero line, threshold, tooltips */
  const plotOptions = {
    responsive: true,
    maintainAspectRatio: false,
    layout,
    interaction: { mode: "index", axis: "x", intersect: false },
    plugins: {
      legend: { display: false },
      guides: {
        real: rows.map((r) => num(r.difference)),
        ...(sharedLimit != null
          ? { value: tf(sharedLimit), limit: sharedLimit, mirror: yRange.hasNeg }
          : {}),
      },
      tooltip: {
        backgroundColor: COLORS.navy,
        padding: 10,
        cornerRadius: 6,
        titleFont: { size: 12, weight: "600" },
        bodyFont: { size: 12 },
        displayColors: true,
        boxWidth: 8,
        boxHeight: 8,
        boxPadding: 4,
        callbacks: {
          title: (items) => {
            if (!items || !items.length) return "";
            const item = rows[items[0].dataIndex];
            return item && item.folio_no != null && String(item.folio_no).trim() !== ""
              ? `Folio No: ${item.folio_no}`
              : `Folio No: ${items[0].label}`;
          },
          label: (ctx) => {
            const item = rows[ctx.dataIndex];
            const limit = item && item.accept_ru != null ? item.accept_ru : null;
            const lines = [];
            if (item && item.facility_name) lines.push(`Facility: ${item.facility_name}`);
            lines.push(`RU difference: ${fmt(item ? num(item.difference) : ctx.parsed.y, 2, 3)} kWh`);
            if (limit == null) {
              lines.push("Accepted limit: not configured");
            } else {
              lines.push(`Accepted limit: ±${limit} kWh`);
              lines.push(isItemExceeded(item) ? "Status: Outside limit" : "Status: Within limit");
            }
            return lines;
          },
        },
      },
    },
    scales: {
      y: {
        ...sharedYScale,
        ticks: { ...sharedYScale.ticks, display: false },
        grid: {
          color: (c) => (c.tick && c.tick.value === 0 ? COLORS.slate : COLORS.border),
          lineWidth: (c) => (c.tick && c.tick.value === 0 ? 1.5 : 1),
          drawTicks: false,
        },
        border: { display: false },
        afterFit: (scale) => {
          scale.width = 0;
        },
      },
      x: {
        ticks: { ...sharedXTicks, color: "#334155", padding: 6 },
        grid: { display: false },
        border: { color: COLORS.border },
        afterFit: fixXHeight,
      },
    },
  };

  /* Fixed left axis: same scale, same padding, same x-axis height */
  const axisData = {
    labels,
    datasets: [{ data: rows.map((r) => tf(num(r.difference))), backgroundColor: "rgba(0,0,0,0)" }],
  };
  const axisOptions = {
    responsive: true,
    maintainAspectRatio: false,
    events: [],
    animation: false,
    layout,
    plugins: { legend: { display: false }, tooltip: { enabled: false } },
    scales: {
      y: {
        ...sharedYScale,
        title: {
          display: true,
          text: compressed ? "RU Difference (kWh, compressed)" : "RU Difference (kWh)",
          color: COLORS.slate,
          font: { size: 10, weight: "600" },
        },
        grid: { display: false },
        border: { color: COLORS.border },
        afterFit: (scale) => {
          scale.width = AXIS_WIDTH;
        },
      },
      x: {
        ticks: { ...sharedXTicks, color: "rgba(0,0,0,0)" },
        grid: { display: false },
        border: { display: false },
        afterFit: fixXHeight,
      },
    },
  };

  const limitText =
    sharedLimit != null ? `${fmt(sharedLimit)} kWh` : "each folio's configured limit";

  return (
    <div className="ds-card w-full min-w-0 flex flex-col">
      <CardHeader areaLabel={areaLabel} />

      <div className="ds-card-body flex flex-col gap-4 min-w-0">
        {/* Controls */}
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Limit filter">
            <SegButton
              tone="outside"
              active={isOutside}
              onClick={() => setLimitView("outside")}
              title="Show developers above the accepted limit"
            >
              Outside limit
              <span className="opacity-90 tabular-nums">{outsideItems.length}</span>
            </SegButton>
            <SegButton
              tone="within"
              active={!isOutside}
              onClick={() => setLimitView("within")}
              title="Show developers at or below the accepted limit"
            >
              Within limit
              <span className="opacity-90 tabular-nums">{withinItems.length}</span>
            </SegButton>
          </div>

          <div className="flex items-center gap-2" role="group" aria-label="View mode">
            <SegButton
              tone="neutral"
              active={viewMode === "chart"}
              onClick={() => setViewMode("chart")}
              title="Chart view"
            >
              <i className="fas fa-chart-column" aria-hidden="true"></i>Chart
            </SegButton>
            <SegButton
              tone="neutral"
              active={viewMode === "table"}
              onClick={() => setViewMode("table")}
              title="Table view"
            >
              <i className="fas fa-table" aria-hidden="true"></i>Table
            </SegButton>
          </div>
        </div>

        {/* Context line */}
        <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-body-sm text-ink-600">
          <span className="inline-flex items-center gap-2">
            <span
              className="w-2.5 h-2.5 rounded-sm flex-none"
              style={{ backgroundColor: barColor }}
              aria-hidden="true"
            ></span>
            <span>
              <strong className="text-ink-800 tabular-nums">{rows.length}</strong>{" "}
              {rows.length === 1 ? "developer" : "developers"}{" "}
              {isOutside ? "outside" : "within"} the accepted limit
              {isOutside ? " (RU difference greater than " : " (RU difference at or below "}
              {limitText}), in folio number order
            </span>
          </span>
          {viewMode === "chart" && sharedLimit != null && (
            <span className="inline-flex items-center gap-1.5 text-ink-500">
              <span
                className="inline-block w-5 border-t-2 border-dashed"
                style={{ borderColor: COLORS.navy }}
                aria-hidden="true"
              ></span>
              Accepted limit {fmt(sharedLimit)}
            </span>
          )}
          {viewMode === "chart" && isOutside && (
            <span className="inline-flex items-center gap-1.5 sm:ml-auto" role="group" aria-label="Chart scale">
              <span className="text-caption text-ink-500">Scale</span>
              <SegButton
                tone="neutral"
                active={scaleMode === "compressed"}
                onClick={() => setScaleMode("compressed")}
                title="Compressed (log) scale keeps small and large differences visible"
              >
                Compressed
              </SegButton>
              <SegButton
                tone="neutral"
                active={scaleMode === "linear"}
                onClick={() => setScaleMode("linear")}
                title="True linear scale"
              >
                Linear
              </SegButton>
            </span>
          )}
        </div>

        {rows.length === 0 ? (
          <div className="flex flex-col items-center justify-center gap-1 min-h-[200px] rounded-lg border border-dashed border-ink-200 bg-[#F5F7FA] text-center px-4">
            <p className="ds-section-title">
              {isOutside ? "No developers outside the limit" : "No developers within the limit"}
            </p>
            <p className="ds-body-sm text-ink-500">
              {isOutside
                ? "Every folio in this scope is within its accepted RU difference limit."
                : "No folio in this scope is currently within its accepted RU difference limit."}
            </p>
          </div>
        ) : viewMode === "chart" ? (
          <div className="w-full rounded-lg border border-ink-200 bg-white overflow-hidden">
            <div className="flex w-full" style={{ height: `${chartHeight}px` }}>
              {/* Fixed y-axis */}
              <div className="flex-none relative" style={{ width: `${AXIS_WIDTH}px` }}>
                <Bar data={axisData} options={axisOptions} />
              </div>
              {/* Horizontally scrollable plot */}
              <div className="flex-1 min-w-0 overflow-x-auto overflow-y-hidden">
                <div
                  className="relative h-full"
                  style={{ width: `${plotWidth}px`, minWidth: "100%" }}
                >
                  <Bar data={chartData} options={plotOptions} plugins={[guidePlugin]} />
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="w-full max-h-[640px] overflow-auto rounded-lg border border-ink-200">
            <table className="ds-table w-full">
              <thead className="sticky top-0 z-10">
                <tr>
                  <th className="ds-th">Folio</th>
                  <th className="ds-th">Facility</th>
                  <th className="ds-th text-right">Diff (kWh)</th>
                  <th className="ds-th text-center">Limit</th>
                  <th className="ds-th text-center">Status</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((item, idx) => {
                  const diff = num(item.difference);
                  const exceeded = isItemExceeded(item);
                  const limit = item.accept_ru != null ? `±${item.accept_ru}` : "—";
                  return (
                    <tr key={item.account_number || `${item.folio_no}-${idx}`} className="ds-tr">
                      <td className="ds-td ds-mono whitespace-nowrap">{item.folio_no || "—"}</td>
                      <td className="ds-td truncate max-w-[240px]" title={item.facility_name}>
                        {item.facility_name || "—"}
                      </td>
                      <td
                        className="ds-td text-right ds-mono font-semibold whitespace-nowrap"
                        style={{ color: exceeded ? COLORS.outsideText : COLORS.withinText }}
                      >
                        {diff.toLocaleString(undefined, {
                          minimumFractionDigits: 1,
                          maximumFractionDigits: 2,
                        })}
                      </td>
                      <td className="ds-td text-center ds-mono text-ink-500 whitespace-nowrap">
                        {limit}
                      </td>
                      <td className="ds-td text-center whitespace-nowrap">
                        <span
                          className={`ds-badge ${exceeded ? "ds-badge-warning" : "ds-badge-info"}`}
                        >
                          {item.accept_ru == null
                            ? "Unconfigured"
                            : exceeded
                              ? "Outside limit"
                              : "Within limit"}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default DifferenceBarChartCard;