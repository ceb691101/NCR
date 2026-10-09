import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

// views
import BulkCustomersView from "views/BulkCustomersView/BulkCustomersView";

export default function BulkCustomers() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
            <Switch>
              <Route path="/bulkCustomers" component={BulkCustomersView} />
            </Switch>
          </div>
        <FooterAdmin />
      </div>
    </>
  );
}