import React, { useState, useEffect, useRef } from "react";

const MonthFilter = ({ value, onChange, uniqueMonths }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside of it
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div ref={dropdownRef} className="flex items-center gap-3 bg-ink-50/50 p-2 rounded-2xl border border-ink-200/80 shadow-xs relative animate-fade-in">
      <span className="text-[10px] font-extrabold text-navy-800/80 uppercase tracking-wider pl-1.5 flex items-center gap-2">
        <i className="fas fa-calendar-alt text-navy-800/90 text-sm"></i>
        Billing Month
      </span>
      <div className="relative">
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className="border border-ink-200/60 rounded-xl pl-3 pr-10 py-1.5 bg-white text-xs font-bold text-navy-800 hover:border-navy-400 focus:outline-none focus:ring-2 focus:ring-navy-500/10 focus:border-navy-500 cursor-pointer shadow-3xs transition-all duration-200 min-w-[170px] text-left flex items-center justify-between"
        >
          <span>{value}</span>
          <div className="absolute inset-y-0 right-0 flex items-center px-3 text-navy-800/70 pointer-events-none">
            <i className={`fas fa-chevron-down text-[10px] transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}></i>
          </div>
        </button>
        {isOpen && (
          <div className="ds-menu right-0 mt-2 min-w-[170px]">
            {uniqueMonths.map((m) => (
              <button
                key={m}
                type="button"
                onClick={() => {
                  onChange(m);
                  setIsOpen(false);
                }}
                className={`w-full text-left px-4 py-2 text-xs font-semibold transition-colors duration-150 ${
                  value === m
                    ? "bg-navy-100 text-navy-800 font-bold"
                    : "text-ink-600 hover:bg-ink-50 hover:text-navy-800"
                }`}
              >
                {m}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MonthFilter;
