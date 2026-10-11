// src/layouts/ReadingsEntry.js
import React from "react";
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";

// views
import ReadingsEntryManagement from "views/ReadingsEntryView/ReadingsEntryManagement";

export default function ReadingsEntry() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderTopBar />
        <div className="flex-grow ds-content py-ds-6">
            <Switch>
              <Route path="/monthlyReadings/readingsEntry" exact component={ReadingsEntryManagement} />
              <Redirect from="/monthlyReadings" to="/monthlyReadings/readingsEntry" />
            </Switch>
          </div>
        <FooterAdmin />
      </div>
    </>
  );
}