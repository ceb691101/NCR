import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

import DeveloperRegistrationView from "views/DeveloperRegistration/DeveloperRegistration";

export default function DeveloperRegistrationLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
          <Switch>
            <Route path="/developerRegistration" component={DeveloperRegistrationView} />
          </Switch>
        </div>
        <FooterAdmin />
      </div>
    </>
  );
}