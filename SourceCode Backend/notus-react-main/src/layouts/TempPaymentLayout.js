import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";

// views
import TempPaymentsView from "views/TempPaymentsView/TempPaymentsView";

export default function TempPaymentsLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <Switch>
            <Route path="/tempPayments" component={TempPaymentsView} />
          </Switch>
        </div>
        <FooterAdmin />
      </div>
    </>
  );
}