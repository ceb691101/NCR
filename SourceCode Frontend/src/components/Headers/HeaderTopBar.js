import React, { useEffect, useState } from "react";
import { useAreaAndBill } from "context/AreaAndBillContext";
import UserDropdown from "components/Dropdowns/UserDropdown.js";
import GlobalSearch from "components/Headers/GlobalSearch.js";

export default function HeaderTopBar() {
  const { data, openDialog } = useAreaAndBill();
  const [userName, setUserName] = useState("");
  const [userCategory, setUserCategory] = useState("");

  useEffect(() => {
    const name =
      sessionStorage.getItem("user_name") ||
      sessionStorage.getItem("user_id") ||
      "";
    const category = sessionStorage.getItem("user_category") || "";
    setUserName(name);
    setUserCategory(category);
  }, []);

  const handleLocationClick = () => {
    // Optional action: narrow the data to one permitted area.
    openDialog();
  };

  const permittedAreas = data?.permittedAreas || [];
  const selectedAreaCode = data?.selectedAreaCode || "";

  // No selection means every permitted area is in scope.
  const isAllAreas = !selectedAreaCode;

  // Value shown next to the "Area" label in the selector.
  const areaDisplay = isAllAreas
    ? permittedAreas.length === 1
      ? permittedAreas[0]?.area_code ?? "—"
      : `All (${permittedAreas.length})`
    : selectedAreaCode;

  const activeCycle = isAllAreas
    ? `${permittedAreas.filter((a) => a?.has_bill_cycle).length} with cycles`
    : permittedAreas.find(
        (a) => String(a?.area_code) === String(selectedAreaCode)
      )?.active_bill_cycle || "No Cycle";

  return (
    <header className="sticky top-0 z-30 w-full bg-white border-b border-ink-200">
      {/* min-h (not fixed h) so the stacked mobile layout can grow instead of
          clipping; the gutter matches .ds-content so it aligns with the page. */}
      <div className="w-full px-4 sm:px-6 lg:px-8 min-h-topbar py-2 sm:py-0 flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-4">
        {/* Global Search Bar Input */}
        <GlobalSearch />

        {/* Right Side: Location Selector + Separator + User Info & Dropdown */}
        <div className="flex flex-none items-center gap-3 sm:gap-4 self-end sm:self-auto sm:ml-auto">
          {/* Area / Bill cycle selector */}
          <button
            type="button"
            onClick={handleLocationClick}
            title="Change area or bill cycle"
            className="group flex items-center gap-2.5 h-10 px-3 rounded-lg bg-white border border-ink-300
                       transition-colors duration-150 ease-out cursor-pointer
                       hover:border-navy-400 hover:bg-ink-50
                       focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
          >
            <span className="w-7 h-7 rounded-md bg-navy-50 border border-navy-100 text-navy-700 flex-none flex items-center justify-center">
              <i className="fas fa-map-marker-alt text-xs"></i>
            </span>

            <span className="flex items-center gap-2 text-body-sm whitespace-nowrap">
              <span className="text-label font-semibold uppercase text-ink-400">Area</span>
              <span className="font-semibold text-ink-800 tabular-nums">{areaDisplay}</span>
              <span className="text-ink-300">|</span>
              <span className="text-label font-semibold uppercase text-ink-400">Cycle</span>
              <span className="font-semibold text-ink-800 tabular-nums">{activeCycle}</span>
            </span>

            <i className="fas fa-chevron-down text-[9px] text-ink-400 group-hover:text-navy-600"></i>
          </button>

          {/* Vertical separator */}
          <div className="hidden sm:block h-7 w-px bg-ink-200 flex-none" />

          {/* Login User Info & Logout UserDropdown */}
          <div className="flex items-center gap-3 min-w-0">
            <div className="text-right hidden sm:block min-w-0 max-w-[180px]">
              <span className="block text-body-sm font-semibold text-ink-800 leading-tight truncate">
                {userName}
              </span>
              <span className="block text-caption text-ink-500 leading-tight truncate">
                {userCategory}
              </span>
            </div>

            <div className="relative z-50 flex-none">
              <UserDropdown />
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}