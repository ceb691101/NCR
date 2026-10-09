// Updated Layout: src/layouts/newcommission.js
import React from "react";
import { Switch, Route } from "react-router-dom";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import NewCommission from "views/NewCommissioning/NewCommission";

export default function NewCommissionLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <Switch>
            <Route path="/newcommission" component={NewCommission} />
          </Switch>
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
