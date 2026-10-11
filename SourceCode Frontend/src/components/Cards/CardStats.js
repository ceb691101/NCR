import React, { useState, useRef, useEffect } from "react";
import PropTypes from "prop-types";
import { useHistory } from "react-router-dom";

/* Restrained status dot per semantic state. Literal class strings so Tailwind
   picks them up when scanning this file. */
const toneDot = {
  warning: "bg-warning-600",
  success: "bg-success-600",
  critical: "bg-critical-600",
  info: "bg-navy-600",
  neutral: "bg-ink-400",
};

export default function CardStats({
  statSubtitle,
  statTitle,
  statArrow,
  statPercent,
  statPercentColor,
  statDescripiton,
  statIconName,
  statIconColor,
  sparklineType = "red",
  statsData,
  navigatePath,
  isLoading = false,
  hasError,
  validNumbers,
  onNumberClick,
  onItemClick,
  isClickable = false,
  showItemList = false,
  onClick,
  className = "",
  disableDropdown = true,
  statusTone = "neutral",
}) {
  const [isValidating, setIsValidating] = useState(false);
  const [validationError, setValidationError] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const [customNumber, setCustomNumber] = useState("");
  const dropdownRef = useRef(null);
  const history = useHistory();
  const cardRef = useRef(null);

  const handleIconClick = (e) => {
    if (disableDropdown) {
      e.stopPropagation();
      if (isClickable && onClick) {
        onClick();
      }
    } else {
      e.stopPropagation();
      setShowDropdown(!showDropdown);
    }
  };

  const handleCardClick = (e) => {
    if (isClickable && onClick && !showDropdown) {
      const clickedElement = e.target;
      const isIconClick = clickedElement.closest(
        `.${statIconColor.split(" ")[0]}`
      );
      const isDropdownClick = clickedElement.closest(".absolute.mt-2");

      if (!isIconClick && !isDropdownClick) {
        onClick();
      }
    }
  };

  const renderSparkline = () => {
    const values = (Array.isArray(statsData) ? statsData : [])
      .map(Number)
      .filter(Number.isFinite);
    if (values.length < 2) return null;

    const minValue = Math.min(...values);
    const range = Math.max(...values) - minValue || 1;
    const points = values
      .map((value, index) => {
        const x = (index / (values.length - 1)) * 100;
        const y = 34 - ((value - minValue) / range) * 28;
        return `${x},${y}`;
      })
      .join(" ");
    const stroke = sparklineType === "green"
      ? "#047857"
      : sparklineType === "blue"
        ? "#0D3A68"
        : "#B45309";

    return (
      <svg
        className="w-20 h-8 overflow-visible"
        viewBox="0 0 100 40"
        role="img"
        aria-label="Recent metric trend"
      >
        <polyline
          points={points}
          fill="none"
          stroke={stroke}
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>
    );
  };

  return (
    <div
      ref={cardRef}
      className={`ds-kpi ${className}`}
      onClick={handleCardClick}
      aria-busy={isLoading}
      style={{
        cursor: isClickable ? "pointer" : "default",
      }}
    >
      {/* Label row — status dot, label, and the (quiet) feature icon */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 min-w-0">
          <span
            className={`w-1.5 h-1.5 rounded-full flex-none ${
              toneDot[statusTone] || toneDot.neutral
            }`}
            aria-hidden="true"
          />
          <span className="ds-label truncate">{statSubtitle}</span>
        </div>

        <div
          /* `border` is needed here because statIconColor only supplies a border
             colour token — without a width the tinted frame never rendered. */
          className={`flex h-8 w-8 flex-none items-center justify-center rounded-lg border text-[13px] ${statIconColor}`}
          onClick={handleIconClick}
          title={statSubtitle}
        >
          <i className={statIconName}></i>
        </div>
      </div>

      {/* Primary figure */}
      <div className="mt-1.5">
        {isLoading ? (
          <div role="status" aria-label={`Loading ${statSubtitle}`} className="flex h-7 items-center">
            <span className="ds-skeleton block h-6 w-16 rounded-md"></span>
          </div>
        ) : (
          <span className="ds-kpi-value block leading-none">{statTitle}</span>
        )}
      </div>

      {/* Supporting line. Kept out of the old trend row because statPercent was
          never supplied, so that row asserted a hardcoded "↑ 0.0%" rise on every
          card regardless of the data. */}
      {statDescripiton && (
        <p className="mt-1 text-caption text-ink-500 truncate leading-tight">{statDescripiton}</p>
      )}

      {/* Trend + sparkline, only when there is a real series to plot. */}
      {(statPercent || (Array.isArray(statsData) && statsData.length > 1)) && (
        <div className="mt-auto pt-2 flex items-end justify-between gap-3">
          {statPercent && (
            <div className="flex items-center gap-1.5 text-caption min-w-0">
              <span className={`font-semibold ${statPercentColor || "text-success-700"}`}>
                ↑ {statPercent}
              </span>
            </div>
          )}

          {Array.isArray(statsData) && statsData.length > 1 && (
            <div className="hidden sm:block flex-none opacity-60">
              {renderSparkline()}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

CardStats.defaultProps = {
  statSubtitle: "Pending Readings",
  statIconName: "fas fa-file-alt",
  statIconColor: "bg-warning-50 text-warning-700 border-warning-100",
  statusTone: "neutral",
  sparklineType: "red",
  statsData: [],
  navigatePath: "",
  isLoading: false,
  hasError: null,
  onClick: null,
  className: "",
  disableDropdown: true,
};

CardStats.propTypes = {
  statSubtitle: PropTypes.string,
  statTitle: PropTypes.node,
  statArrow: PropTypes.string,
  statPercent: PropTypes.string,
  statPercentColor: PropTypes.string,
  statDescripiton: PropTypes.string,
  statIconName: PropTypes.string,
  statIconColor: PropTypes.string,
  sparklineType: PropTypes.string,
  statsData: PropTypes.array,
  navigatePath: PropTypes.string,
  isLoading: PropTypes.bool,
  hasError: PropTypes.string,
  onClick: PropTypes.func,
  className: PropTypes.string,
  isClickable: PropTypes.bool,
  disableDropdown: PropTypes.bool,
  statusTone: PropTypes.string,
};