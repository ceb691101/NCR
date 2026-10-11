import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

// view
import InvoiceChecklist from "views/admin/InvoiceChecklist.js";

export default function InvoiceChecklistLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
          <Switch>
            <Route path="/monthly/invoice-checklist" exact component={InvoiceChecklist} />
            <Redirect from="/monthly/invoice-checklist/*" to="/monthly/invoice-checklist" />
          </Switch>
        </div>
        <FooterAdmin />
      </div>
    </>
  );
}
