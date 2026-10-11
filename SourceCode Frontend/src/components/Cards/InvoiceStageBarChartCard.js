// src/components/Cards/InvoiceStageBarChartCard.js
import React, { useEffect, useState } from "react";
import { useHistory } from "react-router-dom";

/* ── Palette: mirrors the status badges used in InvoiceManagement so the same
      stage reads identically in both places. ──────────────────────────────── */
const COLORS = {
  draft: { bar: "#7C8798", track: "#EEF1F5" }, // ink-500 / ink-100
  recommend: { bar: "#D97706", track: "#FDF3E3" }, // warning-600 / warning-50
  approve: { bar: "#1A4D80", track: "#E8EFF6" }, // navy-600 / navy-50
  finalize: { bar: "#059669", track: "#E3F6EE" }, // success-600 / success-50
  rejected: { bar: "#DC2626", track: "#FCEBEB" }, // critical-600 / critical-50
  empty: { bar: "#D3DBE6", track: "#F5F7FA" }, // ink-300 / ink-50
};

/* Workflow order, not sorted by size: a stage reads left-to-right as the
   approval chain progresses. `owner` names the role that must act next. */
const STAGES = [
  { key: "DRAFT", label: "Draft", owner: "Not submitted", ...COLORS.draft },
  {
    key: "RECOMMEND",
    label: "Awaiting CE",
    owner: "Awaiting CE",
    ...COLORS.recommend,
  },
  {
    key: "APPROVE",
    label: "Awaiting DGM",
    owner: "Awaiting DGM",
    ...COLORS.approve,
  },
  { key: "FINALIZE", label: "Finalized", owner: "Complete", ...COLORS.finalize },
  {
    key: "REJECTED",
    label: "Rejected",
    owner: "Needs rework",
    ...COLORS.rejected,
  },
];

const CardHeader = ({ scopeLabel, scopeTitle, cycleLabel }) => (
  <div className="ds-card-header">
    <div className="min-w-0">
      <h3 className="ds-card-title">Invoice Stages</h3>
      <p className="ds-card-subtitle mt-0.5 truncate" title={scopeTitle || scopeLabel}>
        {scopeLabel
          ? `Share of ${scopeLabel} by approval stage`
          : "Share of invoices by approval stage"}
      </p>
    </div>

    <div className="flex-none">
      <span className="ds-badge ds-badge-neutral">{cycleLabel}</span>
    </div>
  </div>
);

/* One stage = label line + proportional track.
   The track is always drawn, even at zero, so an empty stage reads as an empty
   loading bar with a "0%" label rather than as missing content. */
const StageRow = ({ stage, count, total, ready, onClick }) => {
  const pct = total > 0 ? (count / total) * 100 : 0;
  const isEmpty = count === 0;
  const bar = isEmpty ? COLORS.empty.bar : stage.bar;
  const track = isEmpty ? COLORS.empty.track : stage.track;

  return (
    <button
      type="button"
      onClick={onClick}
      title={`${stage.label} — ${count} invoice${count === 1 ? "" : "s"} (${Math.round(
        pct
      )}% of ${total})`}
      className="group w-full rounded-lg px-2 py-1 text-left transition-colors hover:bg-ink-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
    >
      <div className="flex items-baseline justify-between gap-2">
        <span className="flex min-w-0 items-center gap-1.5">
          <span
            className="h-1.5 w-1.5 flex-none rounded-sm"
            style={{ backgroundColor: bar }}
            aria-hidden="true"
          ></span>
          <span
            className={`truncate text-caption font-semibold ${
              isEmpty ? "text-ink-400" : "text-ink-800"
            }`}
          >
            {stage.label}
          </span>
        </span>

        <span className="flex flex-none items-baseline gap-1.5 tabular-nums">
          <span
            className={`text-caption font-semibold ${
              isEmpty ? "text-ink-400" : "text-ink-900"
            }`}
          >
            {count.toLocaleString()}
          </span>
          <span className="text-caption leading-none text-ink-500 tabular-nums">
            {Math.round(pct)}%
          </span>
        </span>
      </div>

      <div className="mt-1 flex items-center gap-2">
        {/* Track: a full-width rounded rail. The fill slides from 0 to its
            share, so the row always looks like a loading bar in progress. */}
        <div
          className="h-1.5 min-w-0 flex-1 overflow-hidden rounded-full ring-1 ring-inset ring-ink-200/60"
          style={{ backgroundColor: track }}
          role="img"
          aria-label={`${stage.label}: ${Math.round(pct)} percent`}
        >
          <div
            className="h-full rounded-full transition-[width] duration-700 ease-out"
            style={{
              width: ready ? `${pct}%` : "0%",
              backgroundColor: bar,
            }}
          ></div>
        </div>

        <span className="w-[84px] flex-none text-right text-caption leading-none text-ink-500 truncate">
          {stage.owner}
        </span>
      </div>
    </button>
  );
};

const InvoiceStageBarChartCard = ({
  counts,
  total,
  isLoading,
  error,
  scopeLabel,
  scopeTitle,
  cycleLabel,
  cycleText,
  scopedTotal,
  areasWithoutCycle,
  onRetry,
}) => {
  const history = useHistory();
  const [ready, setReady] = useState(false);

  // Bars fill from zero on mount, then settle to their final width.
  useEffect(() => {
    setReady(false);
    const id = requestAnimationFrame(() => setReady(true));
    return () => cancelAnimationFrame(id);
  }, [counts, total]);

  const goToInvoices = () => history.push("/admin/invoices");

  const byKey = counts || {};
  const approved = byKey.FINALIZE || 0;
  const awaiting =
    (byKey.DRAFT || 0) + (byKey.RECOMMEND || 0) + (byKey.APPROVE || 0);
  const needsRework = byKey.REJECTED || 0;

  const hasContent = total > 0;

  /* ── Loading ── */
  if (isLoading) {
    return (
      <div className="ds-card-refined w-full h-full min-w-0 flex flex-col">
        <CardHeader
          scopeLabel={scopeLabel}
          scopeTitle={scopeTitle}
          cycleLabel={cycleLabel}
        />
        <div className="ds-card-body flex flex-1 flex-col justify-center items-center gap-3 min-h-[260px]">
          <span className="ds-spinner" aria-hidden="true"></span>
          <p className="ds-body-sm text-ink-500">Loading invoice stages...</p>
        </div>
      </div>
    );
  }

  /* ── Error ── */
  if (error) {
    return (
      <div className="ds-card-refined w-full h-full min-w-0 flex flex-col">
        <CardHeader
          scopeLabel={scopeLabel}
          scopeTitle={scopeTitle}
          cycleLabel={cycleLabel}
        />
        <div className="ds-card-body flex flex-1 items-center justify-center min-h-[260px]">
          <div className="ds-empty max-w-[320px]">
            <span className="w-11 h-11 rounded-lg bg-critical-50 border border-critical-100 text-critical-600 flex items-center justify-center text-lg flex-none">
              <i className="fas fa-triangle-exclamation" aria-hidden="true"></i>
            </span>
            <p className="ds-section-title mt-1">Invoice stages unavailable</p>
            <p className="ds-body-sm">{error}</p>
            <button onClick={onRetry} className="ds-btn ds-btn-secondary ds-btn-sm mt-1">
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  /* ── Rendered at zero as well ──
     When nothing has been raised yet the rows still draw, each showing an
     empty loading bar with "0" and "0%". An earlier version swapped the whole
     card for a "No invoices yet" panel, which made an untouched cycle look
     like a broken widget. The reason for the zeros is stated underneath
     instead. */

  return (
    <div className="ds-card-refined w-full h-full min-w-0 flex flex-col">
      <CardHeader
        scopeLabel={scopeLabel}
        scopeTitle={scopeTitle}
        cycleLabel={cycleLabel}
      />

      <div className="ds-card-body flex flex-1 flex-col justify-center gap-0 min-h-[210px] min-w-0">
        <div className="-mx-2 flex flex-col divide-y divide-ink-100/80">
          {STAGES.map((stage) => (
            <StageRow
              key={stage.key}
              stage={stage}
              count={byKey[stage.key] || 0}
              total={total}
              ready={ready}
              onClick={goToInvoices}
            />
          ))}
        </div>

        {/* Footer roll-up: the three numbers that answer "what needs us today". Kept on
          a single tight band so the section does not read as a separate block. */}
        <div className="mt-2 grid grid-cols-3 gap-1 border-t border-ink-200/80 pt-2">
          <div className="min-w-0 text-center">
            <p className="text-body-lg font-semibold leading-tight text-success-700 tabular-nums">
              {total > 0 ? Math.round((approved / total) * 100) : 0}%
            </p>
            <p className="text-caption leading-tight text-ink-500 truncate">Finalized</p>
          </div>
          <div className="min-w-0 border-x border-ink-200/80 text-center">
            <p className="text-body-lg font-semibold leading-tight text-warning-700 tabular-nums">
              {awaiting.toLocaleString()}
            </p>
            <p className="text-caption leading-tight text-ink-500 truncate">In flight</p>
          </div>
          <div className="min-w-0 text-center">
            <p className="text-body-lg font-semibold leading-tight text-critical-700 tabular-nums">
              {needsRework.toLocaleString()}
            </p>
            <p className="text-caption leading-tight text-ink-500 truncate">Rejected</p>
          </div>
        </div>

        {/* Explains the zeros rather than hiding them behind an empty panel. */}
        <p className="mt-1 text-center text-caption leading-tight text-ink-500">
          {hasContent ? (
            <>
              {total.toLocaleString()} invoice{total === 1 ? "" : "s"} in{" "}
              {cycleText}
            </>
          ) : (scopedTotal || 0) > 0 ? (
            <>
              {(scopedTotal || 0).toLocaleString()} invoice
              {(scopedTotal || 0) === 1 ? "" : "s"} exist outside {cycleText}
            </>
          ) : (
            <>No invoices raised yet for {scopeLabel || "this scope"}</>
          )}
        </p>

        {areasWithoutCycle > 0 && (
          <p className="mt-1 text-center text-caption text-ink-500">
            {areasWithoutCycle} area{areasWithoutCycle === 1 ? "" : "s"} without an
            active cycle excluded
          </p>
        )}
      </div>
    </div>
  );
};

export default InvoiceStageBarChartCard;