import React, { useEffect, useMemo, useRef, useState } from "react";
import { useHistory } from "react-router-dom";
import { useAuth } from "context/AuthContext";
import {
  ROUTE_PERMISSION_MAP,
  resolvePermissionIcon,
  resolveRoutePathFromPermission,
} from "services/permissionService";

// Small, intentional keyword associations for common features.
// The page list itself is derived from the live navigation (sidebarFunctions/permissions),
// never hard-coded here. These only broaden the matchable terms for well-known features.
const SEARCH_KEYWORDS = {
  "/admin/dashboard": ["dashboard", "home", "overview"],
  "/admin/invoices": ["invoice", "billing", "bill"],
  "/admin/bill-cycle-ending": ["bill cycle", "bill month ending", "cycle close"],
  "/admin/maps": ["map", "location", "gis"],
  "/systemAdmin/administration": ["system", "admin", "administration", "accounts", "users"],
  "/systemAdmin/administration/functions": ["functions", "function master", "user access", "permissions"],
  "/tariff/live": ["tariff", "live tariff", "current tariff", "tariff rate"],
  "/tariff/current": ["tariff", "tariff types", "tariff setup", "rate"],
  "/monthlyReadings/readingsEntry": ["monthly readings", "readings entry", "reading"],
  "/pendReadings": ["pending readings", "pending", "reading"],
  "/tempReadings": ["received readings", "readings received", "reading"],
  "/tempReadings?filter=error": ["error readings", "error", "failed readings"],
  "/developerRegistration": ["developer", "developer management", "registration"],
  "/bulkCustomers": ["developer", "bulk customers", "customers"],
  "/reports/developer": ["developer report", "report"],
  "/reports/tariff-rates": ["tariff rate report", "tariff", "report"],
  "/newcommission": ["customer onboarding", "new commission", "onboarding", "customer"],
  "/amendment/customers": ["add amendment", "amendment"],
  "/amendment": ["master amendment", "amendment"],
  "/amendment/rejected": ["rejected amendment", "amendment"],
  "/amendment/posted": ["posted amendment", "amendment"],
  "/meterAmendment/customers": ["meter changes", "meter amendment"],
  "/meterAmendment/approval": ["meter approval", "meter amendment"],
  "/journals": ["journal entries", "journal"],
  "/journals/report": ["journals report", "journal"],
  "/journals/confirmation": ["journals confirmation", "journal"],
  "/tempPayments": ["temporary payments", "temp payments", "payment"],
};

const DEFAULT_FALLBACK_ITEMS = [
  { label: "Dashboard", path: "/admin/dashboard", icon: "fas fa-home" },
  { label: "System Admin", path: "/systemAdmin/administration", icon: "fas fa-cogs" },
  { label: "Monthly Readings", path: "/monthlyReadings/readingsEntry", icon: "fas fa-tachometer-alt" },
  { label: "Reports", path: "/reports/developer", icon: "fas fa-chart-bar" },
  { label: "Developer Management", path: "/developerRegistration", icon: "fas fa-user-plus" },
  { label: "NCRE Developers", path: "/bulkCustomers", icon: "fas fa-users" },
  { label: "Invoice Management", path: "/admin/invoices", icon: "fas fa-file-invoice" },
  { label: "Map View", path: "/admin/maps", icon: "fas fa-map-marked-alt" },
];

// Mirrors the Sidebar's rendering rules so search shows exactly what the user can see/access.
function buildSearchIndex(sidebarFunctions, permissions) {
  const items = [];
  const seen = new Set();

  const addItem = (label, path, parent, icon) => {
    if (!label || !path) return;
    const key = String(path).split("?")[0].trim().toLowerCase();
    if (seen.has(key)) return;
    seen.add(key);
    items.push({ label: String(label).trim(), path, parent: parent || "", icon: icon || "fas fa-angle-right" });
  };

  if (sidebarFunctions && sidebarFunctions.length > 0) {
    sidebarFunctions.forEach((func) => {
      const funcIdUpper = (func.funcId || "").toUpperCase();
      const mappedFunc = ROUTE_PERMISSION_MAP[funcIdUpper] || {};
      const funcLabel = func.funcNm || mappedFunc.label || func.funcId;
      const funcIcon = resolvePermissionIcon(func) || mappedFunc.icon || "fas fa-folder";
      const subFunctions = func.subFunctions || [];

      if (subFunctions.length >= 2) {
        subFunctions.forEach((sub) => {
          const subIdUpper = (sub.subFuncId || "").toUpperCase();
          const mappedSub = ROUTE_PERMISSION_MAP[subIdUpper] || {};
          const subLabel = subIdUpper === "BLMEND"
            ? mappedSub.label
            : (sub.subFuncNm && sub.subFuncNm.trim()
              ? sub.subFuncNm
              : (mappedSub.label || sub.subFuncId));
          const subPath = resolveRoutePathFromPermission({
            funcId: func.funcId,
            funcNm: func.funcNm,
            subFuncId: sub.subFuncId,
            subFuncNm: sub.subFuncNm,
          }) || mappedSub.path || sub.routePath;
          const subIcon = resolvePermissionIcon(sub) || mappedSub.icon || "fas fa-angle-right";
          addItem(subLabel, subPath, funcLabel, subIcon);
        });
      } else {
        const singleSub = subFunctions[0] || {};
        const mappedSub = ROUTE_PERMISSION_MAP[(singleSub.subFuncId || "").toUpperCase()] || {};
        const routePath = resolveRoutePathFromPermission({
          funcId: func.funcId,
          funcNm: func.funcNm,
          subFuncId: singleSub.subFuncId,
          subFuncNm: singleSub.subFuncNm,
        }) || mappedSub.path || mappedFunc.path || singleSub.routePath;
        const itemIcon = mappedFunc.icon || mappedSub.icon || funcIcon;
          const itemLabel = (singleSub.subFuncId || "").toUpperCase() === "BLMEND"
            ? mappedSub.label
            : funcLabel;
          addItem(itemLabel, routePath, "", itemIcon);
      }
    });
  } else if (permissions && permissions.length > 0) {
    const renderedPaths = new Set();
    permissions.forEach((p) => {
      const subIdUpper = (p.subFuncId || "").toUpperCase();
      const funcIdUpper = (p.funcId || "").toUpperCase();
      const mapped = ROUTE_PERMISSION_MAP[subIdUpper] || ROUTE_PERMISSION_MAP[funcIdUpper];
      if (mapped && mapped.path && !renderedPaths.has(mapped.path)) {
        renderedPaths.add(mapped.path);
        addItem(mapped.label, mapped.path, "", mapped.icon || "fas fa-circle");
      }
    });
  } else {
    DEFAULT_FALLBACK_ITEMS.forEach((item) => addItem(item.label, item.path, "", item.icon));
  }

  // Dashboard is always reachable by any authenticated user; add it if the
  // navigation data did not already provide it.
  if (!seen.has("/admin/dashboard")) {
    addItem("Dashboard", "/admin/dashboard", "", "fas fa-home");
  }

  return items;
}

export default function GlobalSearch() {
  const history = useHistory();
  const { permissions, sidebarFunctions } = useAuth();

  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const wrapperRef = useRef(null);
  const inputRef = useRef(null);

  const searchIndex = useMemo(
    () => buildSearchIndex(sidebarFunctions, permissions),
    [sidebarFunctions, permissions]
  );

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return searchIndex.filter((item) => {
      const keywords = SEARCH_KEYWORDS[item.path] || [];
      const haystack = [item.label, item.parent, item.path, ...keywords]
        .join(" ")
        .toLowerCase();
      return haystack.includes(q);
    });
  }, [query, searchIndex]);

  useEffect(() => {
    setOpen(query.trim().length > 0);
    setActiveIndex(0);
  }, [query]);

  useEffect(() => {
    if (activeIndex >= results.length) setActiveIndex(0);
  }, [results.length, activeIndex]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const navigateTo = (item) => {
    if (!item) return;
    history.push(item.path);
    setQuery("");
    setOpen(false);
    setActiveIndex(0);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Escape") {
      setOpen(false);
      setQuery("");
      inputRef.current?.blur();
      return;
    }
    if (!results.length) return;
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex((i) => (i + 1) % results.length);
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex((i) => (i - 1 + results.length) % results.length);
    } else if (e.key === "Enter") {
      e.preventDefault();
      navigateTo(results[activeIndex] || results[0]);
    }
  };

  return (
    <div className="relative w-full min-w-0 flex-1 sm:max-w-xl" ref={wrapperRef}>
      <label htmlFor="ncre-global-search" className="sr-only">Search pages and features</label>
      <span className="absolute inset-y-0 left-0 flex items-center pl-4 pointer-events-none text-ink-500">
        <i className="fas fa-search text-sm"></i>
      </span>
      <input
        id="ncre-global-search"
        ref={inputRef}
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        onFocus={() => query.trim().length > 0 && setOpen(true)}
        placeholder="Search pages, features, and records"
        className="ds-input h-11 rounded-lg border-ink-300 bg-ink-50/70 pl-11 pr-4 font-medium shadow-xs transition focus:border-navy-500 focus:bg-white focus:ring-2 focus:ring-navy-800/10"
      />

      {open && (
        <div className="absolute left-0 right-0 top-full mt-1.5 bg-white rounded-lg border border-ink-200 shadow-overlay overflow-hidden z-50 select-none">
          <div className="max-h-[60vh] overflow-y-auto py-1">
            {results.length > 0 ? (
              results.map((item, idx) => {
                const isActive = idx === activeIndex;
                return (
                  <button
                    key={`${item.path}-${idx}`}
                    type="button"
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() => navigateTo(item)}
                    onMouseEnter={() => setActiveIndex(idx)}
                    className={`w-full flex items-center gap-3 px-3 py-2 text-left transition-colors ${
                      isActive
                        ? "bg-navy-50 text-navy-900"
                        : "text-ink-700 hover:bg-ink-50"
                    }`}
                  >
                    <div
                      className={`w-8 h-8 rounded-md flex items-center justify-center shrink-0 transition-colors ${
                        isActive
                          ? "bg-navy-800 text-white"
                          : "bg-navy-50 text-navy-700 border border-navy-100"
                      }`}
                    >
                      <i className={`${item.icon || "fas fa-angle-right"} text-xs`}></i>
                    </div>
                    <span className="flex-1 min-w-0">
                      <span className="block text-body-sm font-semibold truncate leading-tight">
                        {item.label}
                      </span>
                      {item.parent && (
                        <span className="block text-caption text-ink-500 truncate leading-tight">
                          {item.parent}
                        </span>
                      )}
                    </span>
                    <i className="fas fa-angle-right text-ink-300 text-xs mb-auto mt-1.5"></i>
                  </button>
                );
              })
            ) : (
              <div className="px-4 py-6 text-center">
                <i className="fas fa-search text-ink-300 text-xl mb-2"></i>
                <p className="text-body-sm text-ink-500 font-medium">
                  No matching pages or features found
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}