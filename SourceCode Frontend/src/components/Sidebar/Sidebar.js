/*eslint-disable*/
import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import ceb from "../../assets/img/ceb-1.png";
import NotificationDropdown from "components/Dropdowns/NotificationDropdown.js";
import UserDropdown from "components/Dropdowns/UserDropdown.js";
import AreaAndBillDialog from "components/AreaAndBill/AreaAndBillDialog";
import { useAreaAndBill } from "context/AreaAndBillContext";
import { useAuth } from "../../context/AuthContext";
import { useNavigation } from "context/NavigationContext";
import {
  ROUTE_PERMISSION_MAP,
  resolvePermissionIcon,
  resolveRoutePathFromPermission,
} from "../../services/permissionService";

export default function Sidebar() {
  const location = useLocation();
  const { openDialog } = useAreaAndBill();
  const { permissions, sidebarFunctions } = useAuth();
  const { isSidebarCollapsed, toggleSidebar } = useNavigation();
  const [collapseShow, setCollapseShow] = useState("hidden");
  const [openDropdowns, setOpenDropdowns] = useState({});
  const [activeSubKey, setActiveSubKey] = useState(null);

  useEffect(() => {
    // Auto expand active dropdown section based on current path
    const path = location.pathname;
    const currentUrl = location.pathname + location.search;
    const newOpens = {};
    if (path.includes("/systemAdmin")) {
      newOpens["SYS_ADMIN"] = true;
    }
    if (path.includes("/tariff")) newOpens["TARIFF"] = true;
    if (path.includes("/monthlyReadings") || path.includes("/pendReadings") || path.includes("/tempReadings")) {
      newOpens["MONTHLY_READINGS"] = true;
      if (path.includes("/tempReadings")) newOpens["READINGS_RECEIVED"] = true;
    }
    if (path.includes("/amendment")) newOpens["AMENDMENT"] = true;
    if (path.includes("/meterAmendment")) newOpens["METER_AMENDMENT"] = true;
    if (path.includes("/journals")) newOpens["JOURNALS"] = true;
    if (path.includes("/reports")) newOpens["REPORTS"] = true;
    if (path.includes("/monthly")) {
      newOpens["BM"] = true;
    }

    (sidebarFunctions || []).forEach((func) => {
      const funcIdUpper = (func.funcId || "").toUpperCase();
      const funcNmUpper = (func.funcNm || "").trim().toUpperCase();
      const isBM =
        funcIdUpper === "BM" ||
        funcIdUpper === "BILL_MONTH" ||
        funcIdUpper === "BILLMONTH" ||
        funcNmUpper === "BILL MONTH" ||
        funcNmUpper.includes("BILL MONTH");

      const hasActiveSubFunction = (func.subFunctions || []).some((sub) => {
        const mappedSub = ROUTE_PERMISSION_MAP[(sub.subFuncId || "").toUpperCase()] || {};
        const subPath = resolveRoutePathFromPermission({
          funcId: func.funcId,
          funcNm: func.funcNm,
          subFuncId: sub.subFuncId,
          subFuncNm: sub.subFuncNm,
        }) || mappedSub.path || sub.routePath;

        if (!subPath) return false;
        return subPath.includes("?")
          ? currentUrl === subPath
          : path === subPath || path.startsWith(subPath + "/");
      });

      if (hasActiveSubFunction || (isBM && path.includes("/monthly"))) {
        newOpens[funcIdUpper] = true;
      }
    });

    setOpenDropdowns(newOpens);
  }, [location.pathname, location.search, sidebarFunctions]);

  const closeMenu = () => setCollapseShow("hidden");

  const toggleDropdown = (key) => {
    setOpenDropdowns((prev) => (prev[key] ? {} : { [key]: true }));
  };

  /* ── Mobile drawer behaviour ──────────────────────────────────────────── */
  const drawerOpen = collapseShow !== "hidden";

  // Close the drawer on Escape.
  useEffect(() => {
    if (!drawerOpen) return undefined;
    const onKeyDown = (e) => {
      if (e.key === "Escape") closeMenu();
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [drawerOpen]);

  // Prevent the page behind the drawer from scrolling on small screens.
  useEffect(() => {
    if (!drawerOpen) return undefined;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [drawerOpen]);

  const renderMenuItem = (to, iconClass, label, exact = false, key = to, onClick = null) => {
    const isAction = typeof onClick === "function" || to === "#" || !to;
    const isActive = !isAction && (exact
      ? location.pathname === to
      : location.pathname === to || (to !== "/" && location.pathname.startsWith(to + "/")));

    const handleClick = (e) => {
      closeMenu();
      if (typeof onClick === "function") {
        e.preventDefault();
        onClick(e);
      }
    };

    const rowClass = `ds-nav-item w-full${isActive ? " ds-nav-item-active" : ""}`;
    const rowIcon = (
      <span className="ds-nav-icon">
        <i className={`${iconClass} text-sm`} aria-hidden="true"></i>
      </span>
    );
    const rowLabel = (
      <span className="ds-nav-label flex-1 min-w-0 truncate text-left">{label}</span>
    );

    if (isAction) {
      return (
        <button
          type="button"
          key={key}
          onClick={handleClick}
          className={rowClass}
          title={label}
        >
          {rowIcon}
          {rowLabel}
        </button>
      );
    }

    return (
      <a
        href={to}
        key={key}
        onClick={handleClick}
        aria-current={isActive ? "page" : undefined}
        className={rowClass}
        title={label}
      >
        {rowIcon}
        {rowLabel}
      </a>
    );
  };

  const renderDropdownMenu = (
    iconClass,
    label,
    isOpen,
    onToggle,
    subItems,
    key = label,
    mainTo = null
  ) => {
    const isAnySubActive = subItems.some(
      (sub) =>
        location.pathname === sub.to ||
        (sub.to && location.pathname.startsWith(sub.to + "/")) ||
        (sub.isNested && sub.isAnyActive)
    ) || (mainTo && (location.pathname === mainTo || location.pathname.startsWith(mainTo + "/")));

    return (
      <div className="w-full" key={key}>
        {/* Parent row: a plain toggle (or link when it has its own route).
            A parent is only brightened when it holds the current page - the
            accent rail is reserved for the page itself. */}
        <span
          className={`ds-nav-item w-full ${
            isAnySubActive ? "ds-nav-item-open" : ""
          }`}
          title={label}
          onClick={isSidebarCollapsed ? toggleSidebar : undefined}
        >
          <span className="ds-nav-icon">
            <i className={`${iconClass} text-sm`}></i>
          </span>

          {mainTo ? (
            <Link
              to={mainTo}
              onClick={closeMenu}
              className="ds-nav-label flex-1 min-w-0 truncate text-left"
            >
              {label}
            </Link>
          ) : (
            <button
              type="button"
              onClick={onToggle}
              aria-expanded={isOpen}
              className="ds-nav-label flex-1 min-w-0 truncate text-left"
            >
              {label}
            </button>
          )}

          <button
            type="button"
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              onToggle();
            }}
            aria-expanded={isOpen}
            className="ds-nav-disclosure flex-none pl-1 cursor-pointer"
            title={isOpen ? `Collapse ${label}` : `Expand ${label}`}
          >
            <i
              className={`fas fa-chevron-${isOpen ? "down" : "right"} ds-nav-chevron`}
            ></i>
          </button>
        </span>

        {isOpen && (
          <div className="ds-nav-sub">
            {subItems.map((sub, idx) => {
              if (sub.isNested) {
                return (
                  <div key={idx} className="w-full">
                    <button
                      type="button"
                      onClick={sub.onToggle}
                      aria-expanded={sub.isOpen}
                      className={`ds-nav-sub-item w-full flex items-center justify-between gap-2 ${
                        sub.isAnyActive ? "ds-nav-sub-item-active" : ""
                      }`}
                    >
                      <span className="flex items-center min-w-0">
                        <i className={`${sub.iconClass} text-[10px] flex-none mr-2`}></i>
                        <span className="truncate">{sub.label}</span>
                      </span>
                      <i
                        className={`fas fa-chevron-${sub.isOpen ? "down" : "right"} ds-nav-chevron`}
                      ></i>
                    </button>

                    {sub.isOpen && (
                      <div className="ds-nav-sub-sub">
                        {sub.nestedItems.map((nSub, nIdx) => {
                          const currentUrl = location.pathname + location.search;
                          const isNActive = nSub.to.includes("?")
                            ? currentUrl === nSub.to
                            : location.pathname === nSub.to && !location.search.includes("filter=error");
                          return (
                            <Link
                              key={nIdx}
                              to={nSub.to}
                              onClick={closeMenu}
                              className={`ds-nav-sub-sub-item ${
                                isNActive ? "ds-nav-sub-sub-item-active" : ""
                              }`}
                            >
                              {nSub.label}
                            </Link>
                          );
                        })}
                      </div>
                    )}
                  </div>
                );
              }

              const currentUrl = location.pathname + location.search;
              const isSubActive = Boolean(sub.to && sub.to !== "#" && (sub.to.includes("?")
                ? currentUrl === sub.to
                : activeSubKey
                  ? activeSubKey === sub.key
                  : location.pathname === sub.to));

              return (
                <Link
                  key={idx}
                  to={sub.to}
                  onClick={() => {
                    setActiveSubKey(sub.key);
                    setOpenDropdowns({ [key]: true });
                    closeMenu();
                  }}
                  aria-current={isSubActive ? "page" : undefined}
                  className={`ds-nav-sub-item flex items-center ${
                    isSubActive ? "ds-nav-sub-item-active" : ""
                  }`}
                >
                  <i
                    className={`${sub.iconClass || "fas fa-angle-right"} text-[10px] mr-2 opacity-70`}
                  ></i>
                  {sub.label}
                </Link>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  /**
   * Render menu items dynamically based on database-driven permissions
   */
  const renderMenuItems = () => {
    if (sidebarFunctions && sidebarFunctions.length > 0) {
      return sidebarFunctions.map((func) => {
        const funcIdUpper = (func.funcId || "").toUpperCase();
        const funcNmUpper = (func.funcNm || "").trim().toUpperCase();
        const mappedFunc = ROUTE_PERMISSION_MAP[funcIdUpper] || {};
        const funcLabel = func.funcNm || mappedFunc.label || func.funcId;
        const funcIcon = resolvePermissionIcon(func) || mappedFunc.icon || "fas fa-calendar-alt";
        const subFunctions = func.subFunctions || [];

        const isBM =
          funcIdUpper === "BM" ||
          funcIdUpper === "BILL_MONTH" ||
          funcIdUpper === "BILLMONTH" ||
          funcNmUpper === "BILL MONTH" ||
          funcNmUpper.includes("BILL MONTH");

        if (isBM && subFunctions.length === 0) return null;

        // Render dropdown if 2+ subfunctions OR if it is Bill Month (isBM)
        if (subFunctions.length >= 2 || isBM) {
          const subItems = subFunctions.map((sub) => {
            const subIdUpper = (sub.subFuncId || "").toUpperCase();
            const mappedSub = ROUTE_PERMISSION_MAP[subIdUpper] || {};
            const subLabel = subIdUpper === "BLMEND"
              ? mappedSub.label
              : ((sub.subFuncNm && sub.subFuncNm.trim())
                ? sub.subFuncNm
                : (mappedSub.label || sub.subFuncId));
            const isSubDashboard =
              subIdUpper === "DASBRD" ||
              subIdUpper === "DASHBOARD" ||
              (subLabel || "").toLowerCase().includes("dash");
            const subPath = resolveRoutePathFromPermission({
              funcId: func.funcId,
              funcNm: func.funcNm,
              subFuncId: sub.subFuncId,
              subFuncNm: sub.subFuncNm,
            }) || mappedSub.path || sub.routePath || (isSubDashboard ? "/admin/dashboard" : "#");
            const subIcon = resolvePermissionIcon(sub) || mappedSub.icon || "fas fa-angle-right";

            return {
              to: subPath,
              key: `${funcIdUpper}:${subIdUpper || subPath}`,
              label: subLabel,
              iconClass: subIcon
            };
          });

          return renderDropdownMenu(
            funcIcon,
            funcLabel,
            !!openDropdowns[funcIdUpper],
            () => toggleDropdown(funcIdUpper),
            subItems,
            funcIdUpper,
            null
          );
        } else {
          // If this is "Bill Month" or "Bill Cycle", render it as an action to open the Area & Bill Cycle dialog
          const normalizedLabel = (funcLabel || "").trim().toLowerCase();
          const isBillMonth =
            normalizedLabel === "bill month" ||
            normalizedLabel === "bill cycle" ||
            funcIdUpper === "BILMNT" ||
            funcIdUpper === "BILL_MONTH" ||
            funcIdUpper === "BILLMONTH";

          if (isBillMonth) {
            return renderMenuItem(
              "#",
              "fas fa-calendar-alt",
              funcLabel,
              false,
              funcIdUpper,
              () => openDialog()
            );
          }

          // If only 1 sub-function (or 0 sub-functions), do NOT render a dropdown.
          // Render the main function name as a direct clickable link to the target route!
          const singleSub = subFunctions[0] || {};
          const subIdUpper = (singleSub.subFuncId || "").toUpperCase();
          const mappedSub = ROUTE_PERMISSION_MAP[subIdUpper] || {};

          const resolvedRoute = resolveRoutePathFromPermission({
            funcId: func.funcId,
            funcNm: func.funcNm,
            subFuncId: singleSub.subFuncId,
            subFuncNm: singleSub.subFuncNm,
          }) || mappedSub.path || mappedFunc.path || singleSub.routePath;

          const isDashboard =
            funcIdUpper === "DASBRD" ||
            funcIdUpper === "DASHBOARD" ||
            normalizedLabel.includes("dash");

          const routePath = resolvedRoute || (isDashboard ? "/admin/dashboard" : null);
          const itemIcon = mappedFunc.icon || mappedSub.icon || funcIcon;
          const itemLabel = subIdUpper === "BLMEND" ? mappedSub.label : funcLabel;

          return renderMenuItem(routePath, itemIcon, itemLabel, routePath === "/admin/dashboard", funcIdUpper);
        }
      });
    }

    // Fallback: If DB permissions exist in flat permissions array, render authorized items
    if (permissions && permissions.length > 0) {
      const renderedPaths = new Set();
      return permissions.map((p, idx) => {
        const subIdUpper = (p.subFuncId || "").toUpperCase();
        const funcIdUpper = (p.funcId || "").toUpperCase();
        const mapped = ROUTE_PERMISSION_MAP[subIdUpper] || ROUTE_PERMISSION_MAP[funcIdUpper];

        if (mapped && mapped.path && !renderedPaths.has(mapped.path)) {
          renderedPaths.add(mapped.path);
          return renderMenuItem(mapped.path, mapped.icon || "fas fa-circle", mapped.label, false, `perm_${idx}`);
        }
        return null;
      }).filter(Boolean);
    }

    // Default fallback menu while permissions are initializing or if unconfigured
    return (
      <>
        {renderMenuItem("/admin/dashboard", "fas fa-home", "Dashboard", true, "nav_dashboard")}
        {renderMenuItem("/systemAdmin/administration", "fas fa-cogs", "System Admin")}
        {renderMenuItem("/monthlyReadings/readingsEntry", "fas fa-tachometer-alt", "Monthly Readings")}
        {renderMenuItem("/reports/developer", "fas fa-chart-bar", "Reports")}
        {renderMenuItem("/developerRegistration", "fas fa-user-plus", "Developer Management")}
        {renderMenuItem("/bulkCustomers", "fas fa-users", "NCRE Developers")}
        {renderMenuItem("/admin/invoices", "fas fa-file-invoice", "Invoice Management")}
        {renderMenuItem("/admin/change-ru", "fas fa-exchange-alt", "Change RU Difference")}
        {renderMenuItem("/admin/maps", "fas fa-map-marked-alt", "Map View")}
      </>
    );
  };

  return (
    <>
      <AreaAndBillDialog />

      {/* ── Mobile: scrim behind the open drawer (tap to dismiss) ── */}
      {drawerOpen && (
        <div
          className="md:hidden fixed inset-0 z-40 bg-navy-950/60"
          onClick={closeMenu}
          aria-hidden="true"
        />
      )}

      {/* ── Mobile: compact top bar (hamburger, mark, notifications, user) ── */}
      <div className="md:hidden fixed top-0 left-0 right-0 z-50 h-topbar bg-navy-800 px-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <button
            className="text-white/80 hover:text-white -ml-2 p-2 cursor-pointer"
            type="button"
            onClick={() => setCollapseShow(drawerOpen ? "hidden" : "block")}
            aria-label="Open navigation"
            aria-expanded={drawerOpen}
            aria-controls="ncre-sidebar"
          >
            <i className="fas fa-bars text-lg"></i>
          </button>

          <img
            src={ceb}
            alt="NCRE Billing Automation System logo"
            className="w-7 h-7 rounded bg-white p-1 flex-none object-contain"
          />
          <span className="text-xs font-semibold text-white leading-tight truncate">
            NCRE Billing Automation System
          </span>
        </div>

        <div className="flex items-center gap-1 flex-none">
          <NotificationDropdown />
          <UserDropdown />
        </div>
      </div>

      {/* ── Sidebar: fixed rail on desktop, slide-over drawer on mobile ── */}
      <nav
        id="ncre-sidebar"
        aria-label="Main navigation"
        className={
          "md:left-0 md:block md:fixed md:top-0 md:bottom-0 bg-navy-800 z-50 select-none transition-all duration-200 " +
          "md:w-sidebar md:shadow-overlay " +
          (drawerOpen
            ? "fixed inset-y-0 left-0 block w-sidebar shadow-overlay"
            : "hidden")
        }
      >
        <div className="relative flex flex-col h-full w-full overflow-hidden">
          {/* Brand lockup: square mark + system name (name hides when collapsed) */}
          <div className="ds-sidebar-brand relative z-20 flex-none flex items-center gap-3 h-16 px-4 border-b border-white/10">
            <a
              href="/admin/dashboard"
              onClick={closeMenu}
              aria-label="NCRE Billing Automation System — go to dashboard"
              className="flex-none w-10 h-10 rounded-lg bg-white border border-white/20 shadow-md overflow-hidden flex items-center justify-center transition-transform duration-200 hover:scale-105"
            >
              <img
                src={ceb}
                alt=""
                className="w-full h-full object-contain p-1"
              />
            </a>
            <span className="ds-sidebar-brand-copy min-w-0 leading-tight">
              <span className="block text-[13px] font-semibold text-white truncate">
                NCRE
              </span>
              <span className="block text-[10px] font-medium text-navy-200 truncate">
                Billing Automation System
              </span>
            </span>
            <button
              type="button"
              onClick={toggleSidebar}
              aria-label={isSidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
              aria-expanded={!isSidebarCollapsed}
              aria-controls="ncre-sidebar"
              title={isSidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
              className="ds-sidebar-toggle hidden md:inline-flex ml-auto flex-none w-8 h-8 rounded-lg items-center justify-center text-white/70 hover:text-white hover:bg-white/10 transition-colors duration-150 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-white/40"
            >
              <i
                className={`fas fa-chevron-${isSidebarCollapsed ? "right" : "left"} text-xs`}
                aria-hidden="true"
              ></i>
            </button>
          </div>

          {/* Navigation list */}
          <div
            id="ncre-sidebar-nav"
            className="ds-nav relative z-10 flex-1 overflow-y-auto overflow-x-hidden px-3 py-4"
          >
            {renderMenuItems()}
          </div>
        </div>
      </nav>
    </>
  );
}
