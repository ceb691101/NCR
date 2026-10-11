import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderTopBar from "components/Headers/HeaderTopBar";
// views
import FunctionManagement from "views/SystemAdminView/FunctionManagement";
export default function SystemAdmin() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
            <Switch>
              <Route path="/systemAdmin/administration/functions" exact component={FunctionManagement} />
              <Redirect from="/systemAdmin" to="/systemAdmin/administration" />
            </Switch>
          </div>
        <FooterAdmin />
      </div>
    </>
  );
}