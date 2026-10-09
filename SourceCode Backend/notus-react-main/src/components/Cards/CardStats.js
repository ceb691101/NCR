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
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2 min-w-0">
          <span
            className={`w-1.5 h-1.5 rounded-full flex-none ${
              toneDot[statusTone] || toneDot.neutral
            }`}
            aria-hidden="true"
          />
          <span className="ds-label truncate">{statSubtitle}</span>
        </div>

        <div
          className={`w-7 h-7 rounded-md flex-none flex items-center justify-center text-xs ${statIconColor}`}
          onClick={handleIconClick}
          title={statSubtitle}
        >
          <i className={statIconName}></i>
        </div>
      </div>

      {/* Primary figure */}
      <div className="mt-2">
        {isLoading ? (
          <div role="status" aria-label={`Loading ${statSubtitle}`} className="flex h-9 items-center">
            <span className="ds-skeleton block h-8 w-24 rounded-md"></span>
          </div>
        ) : (
          <span className="ds-kpi-value block">{statTitle}</span>
        )}
      </div>

      {/* Trend + sparkline */}
      <div className="mt-auto pt-2 flex items-end justify-between gap-3">
        <div className="flex items-center gap-1.5 text-caption min-w-0">
          <span className={`font-semibold ${statPercentColor || "text-success-700"}`}>
            ↑ {statPercent || "0.0%"}
          </span>
          {statDescripiton && (
            <span className="text-ink-500 truncate">{statDescripiton}</span>
          )}
        </div>

        {Array.isArray(statsData) && statsData.length > 1 && (
          <div className="hidden sm:block flex-none opacity-60">
            {renderSparkline()}
          </div>
        )}
      </div>
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