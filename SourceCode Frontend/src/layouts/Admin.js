import React from "react";
import MapView from "components/MapView/MapView.js";
import { Switch, Route, Redirect, useLocation } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

// views
import Dashboard from "views/admin/Dashboard.js";
import InvoiceManagement from "views/admin/InvoiceManagement.js";
import BillCycleEnding from "views/admin/BillCycleEnding.js";
import ChangeRuDifference from "views/ChangeRuDifference/ChangeRuDifference.js";

// Placeholder components if not implemented yet
const Settings = () => <div className="p-4">Settings Page Placeholder</div>;
const Tables   = () => <div className="p-4">Tables Page Placeholder</div>;

export default function Admin() {
  const location = useLocation();
  const isMapPage = location.pathname === "/admin/maps";

  return (
    <>
      <Sidebar />

      {/* ── Main content area (offset by sidebar width) ── */}
      <div className="ds-shell">
        {/* Top Header Bar: Search, Location Selector, User Profile & Logout */}
        <HeaderTopBar />

        {/* ── Map route: full-bleed canvas inside the page gutter ── */}
        {isMapPage ? (
          <div className="flex-grow ds-content py-ds-4">
            <div className="w-full h-[77vh] min-h-[500px] rounded-xl overflow-hidden border border-ink-200 bg-white shadow-card relative z-10">
              <MapView />
            </div>
          </div>
        ) : (
          /* ── All other routes: normal padded layout ── */
          <div className="flex-grow ds-content py-ds-6">
            <Switch>
              <Route path="/admin/dashboard" exact component={Dashboard} />
              <Route path="/admin/settings"  exact component={Settings}  />
              <Route path="/admin/tables"    exact component={Tables}    />
              <Route path="/admin/invoices"  exact component={InvoiceManagement} />
              <Route path="/admin/bill-cycle-ending" exact component={BillCycleEnding} />
              <Route path="/admin/change-ru" exact component={ChangeRuDifference} />
              <Redirect from="/admin" to="/admin/dashboard" />
            </Switch>
          </div>
        )}

        <FooterAdmin />
      </div>
    </>
  );
}