import React, { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { getDeveloperSuggestions } from "services/developerRegistrationService";

const SEARCH_FIELD_OPTIONS = [
  { value: "folio_no", label: "Folio Number", placeholder: "Type folio number..." },
  { value: "acc_nbr", label: "Account Number", placeholder: "Type account number..." },
  { value: "developer_name", label: "Developer Name", placeholder: "Type developer name..." },
];

export default function DeveloperSearchAutocomplete({
  onSelectDeveloper,
  onManualSearch,
  onFieldChange,
  initialField = "folio_no",
  initialValue = "",
  outerLoading = false,
}) {
  const [searchField, setSearchField] = useState(initialField);
  const [searchTerm, setSearchTerm] = useState(initialValue);
  const [suggestions, setSuggestions] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const containerRef = useRef(null);

  // Sync initial values if provided
  useEffect(() => {
    if (initialValue) setSearchTerm(String(initialValue));
  }, [initialValue]);

  useEffect(() => {
    if (initialField) setSearchField(initialField);
  }, [initialField]);

  // Debounced autocomplete search
  useEffect(() => {
    const trimmed = String(searchTerm ?? "")
      .trim()
      .toString();
    if (trimmed.length < 1) {
      setSuggestions([]);
      setIsOpen(false);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    const handler = setTimeout(async () => {
      try {
        const results = await getDeveloperSuggestions(searchField, trimmed);
        setSuggestions(results);
        setIsOpen(true);
        setHighlightedIndex(-1);
      } catch (err) {
        console.error("Autocomplete error:", err);
        setSuggestions([]);
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => clearTimeout(handler);
  }, [searchTerm, searchField]);

  // Click outside listener to close suggestion dropdown
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSelect = (item) => {
    setIsOpen(false);
    setSuggestions([]);
    setHighlightedIndex(-1);

    // Set the input text to the main field value for visual feedback
    const selectedValue =
      searchField === "acc_nbr"
        ? item.accountNumber
        : searchField === "folio_no"
        ? item.folioNumber
        : searchField === "developer_name"
        ? item.developerName
        : item.projectName;
    setSearchTerm(
      selectedValue === undefined || selectedValue === null
        ? ""
        : String(selectedValue)
    );

    if (onSelectDeveloper) {
      onSelectDeveloper(item);
    }
  };

  const handleKeyDown = (e) => {
    if (!isOpen || suggestions.length === 0) {
      if (e.key === "Enter" && onManualSearch) {
        onManualSearch(searchField, searchTerm);
      }
      return;
    }

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setHighlightedIndex((prevIndex) =>
        prevIndex < suggestions.length - 1 ? prevIndex + 1 : 0
      );
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setHighlightedIndex((prevIndex) =>
        prevIndex > 0 ? prevIndex - 1 : suggestions.length - 1
      );
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (highlightedIndex >= 0 && highlightedIndex < suggestions.length) {
        handleSelect(suggestions[highlightedIndex]);
      } else if (onManualSearch) {
        setIsOpen(false);
        onManualSearch(searchField, searchTerm);
      }
    } else if (e.key === "Escape") {
      setIsOpen(false);
    }
  };

  const currentOption =
    SEARCH_FIELD_OPTIONS.find((opt) => opt.value === searchField) ||
    SEARCH_FIELD_OPTIONS[0];

  const clearSearch = () => {
    setSearchTerm("");
    setSuggestions([]);
    setIsOpen(false);
    setHighlightedIndex(-1);
  };

  return (
    <div className="relative w-full" ref={containerRef}>
      <div className="flex flex-col sm:flex-row sm:items-center space-y-3 sm:space-y-0 sm:space-x-3">
        {/* Field Select Dropdown */}
        <div className="relative min-w-[170px]">
          <select
            value={searchField}
            onChange={(e) => {
              const val = e.target.value;
              setSearchField(val);
              setSuggestions([]);
              setIsOpen(false);
              if (onFieldChange) onFieldChange(val);
            }}
            className="ds-input h-10 font-semibold cursor-pointer"
          >
            {SEARCH_FIELD_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {/* Input box with spinner & clear icon */}
        <div className="relative flex-1">
          <div className="relative flex items-center">
            <i className="fas fa-search absolute left-3.5 text-ink-400 text-sm"></i>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onFocus={() => {
                if (suggestions.length > 0) setIsOpen(true);
              }}
              onKeyDown={handleKeyDown}
              placeholder={currentOption.placeholder}
              className="ds-input h-10 pl-10 pr-10 font-medium"
            />

            {/* Loading Indicator or Clear Button */}
            <div className="absolute right-3 flex items-center space-x-1">
              {isLoading && (
                <i className="fas fa-circle-notch fa-spin text-navy-600 text-sm mr-1"></i>
              )}
              {searchTerm && !isLoading && (
                <button
                  type="button"
                  onClick={clearSearch}
                  className="text-ink-400 hover:text-ink-600 p-1 focus:outline-none transition-colors"
                  title="Clear"
                >
                  <i className="fas fa-times text-xs"></i>
                </button>
              )}
            </div>
          </div>

          {/* Typeahead Suggestions Dropdown */}
          {isOpen && (
            <div className="absolute z-50 left-0 right-0 mt-1.5 bg-white border border-ink-200 rounded-xl shadow-lg max-h-72 overflow-y-auto divide-y divide-ink-100 text-sm">
              {suggestions.length > 0 ? (
                suggestions.map((item, index) => {
                  const isHighlighted = index === highlightedIndex;
                  return (
                    <div
                      key={item.id || index}
                      onClick={() => handleSelect(item)}
                      onMouseEnter={() => setHighlightedIndex(index)}
                      className={`p-3 cursor-pointer transition-colors duration-150 ${
                        isHighlighted
                          ? "bg-navy-50/80 text-navy-800"
                          : "hover:bg-ink-50 text-ink-700"
                      }`}
                    >
                      <div className="flex items-center justify-between font-bold text-ink-800">
                        <span className="truncate">
                          <i className="fas fa-building mr-2 text-xs text-navy-600"></i>
                          {item.projectName || "Unnamed Project"}
                        </span>
                        {item.developerName && (
                          <span className="text-xs font-normal text-ink-500 ml-2 truncate max-w-[150px]">
                            {item.developerName}
                          </span>
                        )}
                      </div>

                      <div className="flex flex-wrap items-center gap-2 mt-1 text-xs text-ink-500">
                        {item.folioNumber && (
                          <span className="bg-navy-50 text-navy-800 border border-navy-200 px-2 py-0.5 rounded-md font-mono text-[11px] font-semibold">
                            Folio: {item.folioNumber}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })
              ) : !isLoading ? (
                <div className="p-4 text-center text-ink-400 text-xs font-medium">
                  No matching developers found
                </div>
              ) : null}
            </div>
          )}
        </div>

        {/* Optional Manual Search Action Button */}
        {onManualSearch && (
          <button
            type="button"
            onClick={() => {
              setIsOpen(false);
              onManualSearch(searchField, searchTerm);
            }}
            disabled={outerLoading || isLoading || !searchTerm.trim()}
            className="px-5 py-2.5 bg-navy-800 text-white rounded-xl font-bold hover:bg-navy-900 disabled:opacity-50 transition-all duration-200 shadow-md cursor-pointer text-sm whitespace-nowrap"
          >
            {outerLoading ? "Searching..." : "Search"}
          </button>
        )}
      </div>
    </div>
  );
}

DeveloperSearchAutocomplete.propTypes = {
  onSelectDeveloper: PropTypes.func.isRequired,
  onManualSearch: PropTypes.func,
  onFieldChange: PropTypes.func,
  initialField: PropTypes.string,
  initialValue: PropTypes.string,
  outerLoading: PropTypes.bool,
};
