import React, { useState, useEffect, useRef } from "react";
import { toast } from "react-toastify";
import Breadcrumb from "components/Breadcrumb/Breadcrumb.js";
import { searchDevelopers, getDeveloperDetails, updateRuDifference } from "../../services/changeRuDifferenceService";
import { getActiveDeveloperCount } from "services/developerRegistrationService";
import { getSelectedAreaCode } from "services/AreaAndBillService";

const DEFAULT_ACCEPTED_RU = 24;

const hasRuValue = (value) => value !== null && value !== undefined && value !== "";

const toRuInputValue = (value) => (hasRuValue(value) ? String(value) : "");

export default function ChangeRuDifference() {
  const [searchType, setSearchType] = useState("FOLIO");
  const [searchQuery, setSearchQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isSearching, setIsSearching] = useState(false);

  const [selectedDeveloper, setSelectedDeveloper] = useState(null);
  const [newRu, setNewRu] = useState("");
  const [isUpdating, setIsUpdating] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [activeDeveloperCount, setActiveDeveloperCount] = useState(0);
  const [selectedAreaCode, setSelectedAreaCode] = useState(null);

  const searchRef = useRef(null);
  const activeSearchRef = useRef(0);

  const breadcrumbItems = [
    { label: "Dashboard", href: "/admin/dashboard" },
    { label: "Change Accepting Energy Difference (RU)", href: null },
  ];

  // Debounce search with faster 250ms response
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (searchQuery.trim().length >= 1) {
        performSearch(searchQuery.trim());
      } else {
        setSuggestions([]);
        setShowSuggestions(false);
      }
    }, 250);

    return () => clearTimeout(delayDebounceFn);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchQuery, searchType]);

  const loadDeveloperCount = async () => {
    try {
      const currentSelectedArea = getSelectedAreaCode();
      setSelectedAreaCode(currentSelectedArea);
      const developerCount = await getActiveDeveloperCount(currentSelectedArea);
      setActiveDeveloperCount(developerCount);
    } catch (countError) {
      console.error("Error loading active developer count:", countError);
      setActiveDeveloperCount(0);
    }
  };

  useEffect(() => {
    loadDeveloperCount();
    const handleAreaChange = () => loadDeveloperCount();
    window.addEventListener("areaAndBill:changed", handleAreaChange);
    return () => window.removeEventListener("areaAndBill:changed", handleAreaChange);
  }, []);

  // Click outside to close suggestions
  useEffect(() => {
    function handleClickOutside(event) {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const performSearch = async (query) => {
    const requestId = ++activeSearchRef.current;
    setIsSearching(true);
    try {
      const results = await searchDevelopers(searchType, query);
      if (requestId === activeSearchRef.current) {
        setSuggestions(results);
        setShowSuggestions(true);
      }
    } catch (error) {
      if (requestId === activeSearchRef.current) {
        console.error("Search error:", error);
      }
    } finally {
      if (requestId === activeSearchRef.current) {
        setIsSearching(false);
      }
    }
  };

  const handleSelectSuggestion = async (dev) => {
    setSearchQuery(dev.folioNo ? dev.folioNo.toString() : "");
    setShowSuggestions(false);
    // Instant optimistic display
    setSelectedDeveloper(dev);
    setNewRu(toRuInputValue(dev.acceptRu));

    try {
      const details = await getDeveloperDetails(dev.folioNo);
      setSelectedDeveloper(details);
      setNewRu(toRuInputValue(details.acceptRu));
    } catch (error) {
      console.warn("Background fetch failed, using suggestion data:", error);
    }
  };

  const handleDirectSearch = async () => {
    if (!searchQuery || searchQuery.trim() === "") return;
    const query = searchQuery.trim();
    const requestId = ++activeSearchRef.current;
    setIsSearching(true);

    try {
      // 1. If searching by Folio and a valid numeric folio is entered, fetch developer directly
      if (searchType === "FOLIO" && !isNaN(parseInt(query, 10))) {
        const folioNumber = parseInt(query, 10);
        try {
          const directDev = await getDeveloperDetails(folioNumber);
          if (requestId === activeSearchRef.current && directDev) {
            setSelectedDeveloper(directDev);
            setNewRu(toRuInputValue(directDev.acceptRu));
            setShowSuggestions(false);
            setSuggestions([]);
            return;
          }
        } catch (directErr) {
          // If direct fetch by folio fails (e.g. not found), fall through to search
        }
      }

      // 2. Otherwise search across developers
      const results = await searchDevelopers(searchType, query);
      if (requestId === activeSearchRef.current) {
        if (results && results.length > 0) {
          // Check for exact match
          const exactMatch = results.find(
            (d) =>
              (searchType === "FOLIO" && String(d.folioNo) === query) ||
              (searchType === "NAME" && d.developerName && d.developerName.toLowerCase() === query.toLowerCase())
          );

          if (exactMatch) {
            // Auto-load exact match
            setSelectedDeveloper(exactMatch);
            setNewRu(toRuInputValue(exactMatch.acceptRu));
            setShowSuggestions(false);
            setSuggestions([]);
          } else if (results.length === 1) {
            // If only one match, auto-load that developer
            const single = results[0];
            setSelectedDeveloper(single);
            setNewRu(toRuInputValue(single.acceptRu));
            setShowSuggestions(false);
            setSuggestions([]);
          } else {
            // Multiple partial matches, show dropdown
            setSuggestions(results);
            setShowSuggestions(true);
          }
        } else {
          setSuggestions([]);
          setShowSuggestions(false);
          toast.info(`No developer found for ${searchType === "FOLIO" ? "Folio Number" : "Developer Name"}: ${query}`);
        }
      }
    } catch (error) {
      if (requestId === activeSearchRef.current) {
        console.error("Search error:", error);
        toast.error("Failed to perform search.");
      }
    } finally {
      if (requestId === activeSearchRef.current) {
        setIsSearching(false);
      }
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleDirectSearch();
    }
  };

  const handleUpdate = () => {
    if (!selectedDeveloper || !newRu || newRu.trim() === "") {
      toast.error("Please enter a valid Accepting Energy Difference(RU)");
      return;
    }

    const parsedNewRu = parseInt(newRu, 10);
    if (isNaN(parsedNewRu)) {
      toast.error("Accepting Energy Difference(RU) must be a valid number.");
      return;
    }

    if (parsedNewRu === selectedDeveloper.acceptRu) {
      toast.error("New Accepting Energy Difference(RU) must be different from the current Accepting Energy Difference(RU).");
      return;
    }

    // Open Custom Modal
    setShowConfirmModal(true);
  };

  const confirmUpdate = async () => {
    setShowConfirmModal(false);
    setIsUpdating(true);
    const parsedNewRu = parseInt(newRu, 10);

    try {
      await updateRuDifference(selectedDeveloper.folioNo, parsedNewRu);
      toast.success(`Accepting Energy Difference(RU) updated successfully for Folio ${selectedDeveloper.folioNo}.`);

      // Refresh details
      const refreshedDetails = await getDeveloperDetails(selectedDeveloper.folioNo);
      setSelectedDeveloper(refreshedDetails);
      setNewRu(toRuInputValue(refreshedDetails.acceptRu));
    } catch (error) {
      toast.error(error.message || "Failed to update Accepting Energy Difference(RU).");
    } finally {
      setIsUpdating(false);
    }
  };

  const currentRu = selectedDeveloper ? toRuInputValue(selectedDeveloper.acceptRu) : "";
  const isValueUnchanged =
    Boolean(selectedDeveloper) &&
    newRu.trim() !== "" &&
    parseInt(newRu, 10) === selectedDeveloper.acceptRu;

  const summaryStats = [
    {
      label: "NCRE Developers",
      value: activeDeveloperCount,
      detail:
        selectedAreaCode && selectedAreaCode !== "ALL"
          ? `Active developers (Area ${selectedAreaCode})`
          : "Active developers",
      icon: "fas fa-users",
      color: "bg-indigo-50 text-indigo-700",
    },
    {
      label: "Default Accepted Energy Difference (RU)",
      value: DEFAULT_ACCEPTED_RU,
      detail: "Default accepted energy difference",
      icon: "fas fa-exchange-alt",
      color: "bg-emerald-50 text-emerald-700",
    },
  ];

  const detailFields = selectedDeveloper
    ? [
        { label: "Developer Name", value: selectedDeveloper.developerName, mono: false },
        { label: "Facility Name", value: selectedDeveloper.facilityName, mono: false },
        { label: "NCRE Type", value: selectedDeveloper.ncreType, mono: false },
        {
          label: "Tariff Type",
          value:
            selectedDeveloper.tariffDesc && selectedDeveloper.tariffDesc !== "N/A"
              ? selectedDeveloper.tariffDesc
              : selectedDeveloper.tariffType,
          mono: false,
        },
      ]
    : [];

  return (
    <div className="w-full space-y-ds-5">
      <header className="flex flex-col gap-ds-4">
        <div className="w-full mx-auto">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2">
            <div>
              <h1 className="ds-page-title">
                Change Accepting Energy Difference (RU)
              </h1>
              <p className="ds-page-subtitle mt-1">
                Search a developer by folio number or name and update the accepted energy difference (RU).
              </p>
            </div>
          </div>

          {/* Stats Cards Section inside Stripe */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-ds-4">
            {summaryStats.map((stat) => (
              <div
                key={stat.label}
                className="ds-card flex items-center gap-4 p-ds-5 transition-shadow hover:shadow-md"
              >
                <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-lg text-lg ${stat.color}`}>
                  <i className={stat.icon} aria-hidden="true"></i>
                </div>
                <div className="min-w-0 flex-1">
                  <p className="ds-label">{stat.label}</p>
                  <p className="ds-kpi-value my-0.5 truncate">{stat.value}</p>
                  <p className="ds-caption truncate" title={String(stat.detail)}>
                    {stat.detail}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </header>

      <div className="w-full">
        <div className="ds-card-refined w-full overflow-hidden">
          {/* Breadcrumb wrapper */}
          <div className="border-b border-ink-200 bg-ink-50 px-ds-5 py-ds-3">
            <Breadcrumb items={breadcrumbItems} />
          </div>

          {/* ── Developer Search Panel ── */}
          <section className="border-b border-ink-200" aria-label="Developer search">
            <div className="flex flex-col gap-3 border-b border-ink-200 bg-ink-50 px-ds-5 py-ds-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-navy-50 text-navy-700">
                  <i className="fas fa-search" aria-hidden="true"></i>
                </div>
                <div className="min-w-0">
                  <h2 className="ds-section-title">Developer Search</h2>
                  <p className="ds-caption mt-0.5">
                    Look up a developer by folio number or developer name.
                  </p>
                </div>
              </div>
            </div>

            <div className="p-ds-5">
              <div ref={searchRef} className="relative flex flex-col lg:flex-row lg:items-end gap-4">
                <div className="w-full lg:w-56">
                    <label htmlFor="ru-search-type" className="mb-1.5 block text-label font-semibold text-ink-600">
                    Search By
                  </label>
                  <select
                    id="ru-search-type"
                      className="ds-input w-full cursor-pointer font-semibold"
                    value={searchType}
                    onChange={(e) => {
                      setSearchType(e.target.value);
                      setSearchQuery("");
                      setSuggestions([]);
                      setShowSuggestions(false);
                    }}
                  >
                    <option value="FOLIO">Folio Number</option>
                    <option value="NAME">Developer Name</option>
                  </select>
                </div>

                <div className="w-full lg:flex-1">
                    <label htmlFor="ru-search-query" className="mb-1.5 block text-label font-semibold text-ink-600">
                    Search Query
                  </label>
                  <div className="flex flex-col sm:flex-row gap-3">
                    <div className="relative flex-1 min-w-0">
                      <i className="fas fa-search absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm" aria-hidden="true"></i>
                      <input
                        id="ru-search-query"
                        type="text"
                        className="ds-input w-full pl-10 pr-10 font-medium"
                        placeholder={`Enter ${searchType === "FOLIO" ? "Folio Number" : "Developer Name"}...`}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={handleKeyDown}
                        onFocus={() => {
                          if (suggestions.length > 0) setShowSuggestions(true);
                        }}
                      />
                      {isSearching && (
                        <i
                          className="fas fa-circle-notch fa-spin absolute right-3.5 top-1/2 -translate-y-1/2 text-navy-700 text-sm"
                          aria-hidden="true"
                        ></i>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={handleDirectSearch}
                      disabled={isSearching || searchQuery.trim() === ""}
                      className="ds-btn ds-btn-primary min-h-11 whitespace-nowrap"
                    >
                      {isSearching ? "Searching..." : "Search"}
                    </button>
                  </div>

                  {/* Typeahead Suggestions Dropdown */}
                  {showSuggestions && suggestions.length > 0 && (
                    <ul className="absolute z-50 left-0 right-0 mt-1.5 bg-white border border-slate-200 rounded-xl shadow-lg max-h-72 overflow-y-auto divide-y divide-slate-100 text-sm">
                      {suggestions.map((dev, idx) => (
                        <li key={dev.folioNo || idx}>
                          <button
                            type="button"
                            onClick={() => handleSelectSuggestion(dev)}
                            className="w-full cursor-pointer p-3 text-left transition-colors hover:bg-ink-50 focus:outline-none focus:bg-ink-50"
                          >
                            <div className="flex items-center justify-between gap-3 font-semibold text-ink-800">
                              <span className="truncate">
                                <i className="fas fa-building mr-2 text-xs text-navy-700" aria-hidden="true"></i>
                                {dev.developerName || "Unnamed Developer"}
                              </span>
                              <span className="ds-badge ds-badge-info shrink-0 font-mono">
                                Folio: {dev.folioNo}
                              </span>
                            </div>
                            <div className="ds-caption mt-1">
                              Accepting Energy Difference (RU):{" "}
                              <span className="font-semibold text-ink-700">
                                {hasRuValue(dev.acceptRu) ? dev.acceptRu : "Not set"}
                              </span>
                            </div>
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                  {showSuggestions && searchQuery.trim() !== "" && !isSearching && suggestions.length === 0 && (
                    <div className="absolute left-0 right-0 z-50 mt-1.5 rounded-lg border border-ink-200 bg-white p-4 text-center text-caption font-medium text-ink-500 shadow-lg">
                      No matching developers found
                    </div>
                  )}
                </div>
              </div>
            </div>
          </section>

          {/* ── Developer RU Information Panel ── */}
          <section aria-label="Developer RU information">
            <div className="flex flex-col gap-3 border-b border-ink-200 bg-ink-50 px-ds-5 py-ds-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-success-50 text-success-700">
                  <i className="fas fa-user-circle" aria-hidden="true"></i>
                </div>
                <div className="min-w-0">
                  <h2 className="ds-section-title">Developer RU Information</h2>
                  <p className="ds-caption mt-0.5">
                    Review the current accepted energy difference (RU) before updating it.
                  </p>
                </div>
              </div>
              {selectedDeveloper && (
                <span className="ds-badge ds-badge-info w-fit shrink-0">
                  Folio {selectedDeveloper.folioNo}
                </span>
              )}
            </div>

            <div className="p-ds-5">
              {!selectedDeveloper ? (
                <div className="ds-empty rounded-lg border-dashed bg-ink-50/50 p-8 text-center">
                  <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-lg border border-ink-200 bg-ink-100 text-xl text-ink-400">
                    <i className="fas fa-user-circle" aria-hidden="true"></i>
                  </div>
                  <p className="ds-section-title mb-1">No developer selected</p>
                  <p className="ds-body-sm mx-auto max-w-sm">
                    Search by folio number or developer name above to load the RU difference details.
                  </p>
                </div>
              ) : (
                <>
                  {/* Selected developer summary */}
                  <section
                    className="ds-panel p-4"
                    aria-label="Selected developer information"
                  >
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
                      <div className="min-w-0">
                        <p className="ds-caption">Folio Number</p>
                        <p className="ds-mono mt-1 truncate font-semibold">
                          {selectedDeveloper.folioNo}
                        </p>
                      </div>
                      {detailFields.map((field) => (
                        <div key={field.label} className="min-w-0">
                          <p className="ds-caption">{field.label}</p>
                          <p className="mt-1 truncate text-sm font-medium text-ink-800" title={String(field.value || "")}>
                            {field.value || "-"}
                          </p>
                        </div>
                      ))}
                    </div>
                  </section>

                  {/* RU difference editor */}
                  <div className="mt-5 flex flex-col gap-4 border-t border-ink-200 pt-5 lg:flex-row lg:items-end">
                    <div className="w-full lg:w-1/3">
                      <label className="mb-1.5 block text-label font-semibold text-ink-600">
                        Current Accepted Energy Difference (RU)
                      </label>
                      <div className="ds-input flex min-h-11 items-center font-semibold text-ink-800">
                        {currentRu === "" ? "Not Set" : currentRu}
                      </div>
                    </div>

                    <div className="w-full lg:w-1/3">
                      <label htmlFor="ru-new-value" className="mb-1.5 block text-label font-semibold text-ink-600">
                        New Accepted Energy Difference (RU)
                      </label>
                      <input
                        id="ru-new-value"
                        type="number"
                        step="1"
                        className="ds-input w-full font-medium"
                        placeholder="Enter new RU value"
                        value={newRu}
                        onChange={(e) => setNewRu(e.target.value)}
                      />
                    </div>

                    <div className="w-full lg:w-1/3 lg:flex lg:justify-end">
                      <button
                        type="button"
                        onClick={handleUpdate}
                        disabled={isUpdating || newRu.trim() === "" || isValueUnchanged}
                        className="ds-btn ds-btn-primary min-h-11 w-full gap-2 lg:w-auto"
                      >
                        {isUpdating ? (
                          <i className="fas fa-spinner fa-spin" aria-hidden="true"></i>
                        ) : (
                          <i className="fas fa-save" aria-hidden="true"></i>
                        )}
                        {isUpdating ? "Updating..." : "Update Accepting Energy Difference (RU)"}
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          </section>
        </div>
      </div>

      {/* Custom Confirmation Modal */}
      {showConfirmModal && selectedDeveloper && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-navy-950/60 p-4"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="ru-confirm-title"
          aria-describedby="ru-confirm-message"
        >
          <div className="ds-modal max-w-md animate-fade-in">
              <div className="px-6 pt-6 pb-4">
                <div className="flex items-start gap-4">
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-600">
                    <i className="fas fa-exclamation-triangle text-xl" aria-hidden="true"></i>
                  </div>
                  <div className="min-w-0 pt-0.5">
                    <h3 id="ru-confirm-title" className="text-lg font-bold text-slate-800">
                      Confirm Update
                    </h3>
                    <p id="ru-confirm-message" className="mt-1.5 text-sm leading-6 text-slate-500">
                      You are about to change the Accepted Energy Difference (RU) for this developer. Please
                      confirm the details below before continuing.
                    </p>
                  </div>
                </div>

                <dl className="mt-4 divide-y divide-slate-100 rounded-xl border border-slate-200 bg-slate-50 px-4">
                  <div className="flex items-center justify-between gap-3 py-2.5">
                    <dt className="text-xs font-semibold text-slate-500">Folio Number</dt>
                    <dd className="text-sm font-mono font-semibold text-slate-800">{selectedDeveloper.folioNo}</dd>
                  </div>
                  <div className="flex items-center justify-between gap-3 py-2.5">
                    <dt className="text-xs font-semibold text-slate-500">Developer</dt>
                    <dd className="truncate text-sm font-medium text-slate-800">
                      {selectedDeveloper.developerName || "N/A"}
                    </dd>
                  </div>
                  <div className="flex items-center justify-between gap-3 py-2.5">
                    <dt className="text-xs font-semibold text-slate-500">Current RU Difference</dt>
                    <dd className="text-sm font-bold text-slate-800">{currentRu === "" ? "Not Set" : currentRu}</dd>
                  </div>
                  <div className="flex items-center justify-between gap-3 py-2.5">
                    <dt className="text-xs font-semibold text-slate-500">New RU Difference</dt>
                    <dd className="text-sm font-bold text-[#002244]">{newRu}</dd>
                  </div>
                </dl>

                <div className="mt-4 flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 p-3">
                  <i className="fas fa-info-circle text-amber-500 mt-0.5" aria-hidden="true"></i>
                  <p className="text-xs leading-5 text-amber-800">
                    This change affects future invoice calculations for this developer.
                  </p>
                </div>
              </div>

              {/* Actions */}
              <div className="bg-slate-50 px-6 py-4 flex flex-col-reverse sm:flex-row gap-3 sm:justify-end border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowConfirmModal(false)}
                  className="ds-btn ds-btn-secondary w-full sm:w-auto"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={confirmUpdate}
                  className="ds-btn ds-btn-primary w-full sm:w-auto"
                >
                  <i className="fas fa-check mr-2" aria-hidden="true"></i>
                  Yes, Update
                </button>
              </div>
          </div>
        </div>
      )}
    </div>
  );
}