// src/layouts/AmendmentViewLayout.js
import React from "react";
import { Switch, Route } from "react-router-dom";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import BulkCustomerAmendmentView from "views/Amendment/BulkCustomerAmendmentView";

export default function AmendmentViewLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <Switch>
            <Route
              path="/amendment/customers"
              component={BulkCustomerAmendmentView}
            />
          </Switch>
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
