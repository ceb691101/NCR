import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

// views
import TempReadingsView from "views/TempReadingsView/TempReadingsView";

export default function TempReadings() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
            <Switch>
              <Route path="/tempReadings/:errorSlug?" component={TempReadingsView} />
            </Switch>
          </div>
        <FooterAdmin />
      </div>
    </>
  );
}