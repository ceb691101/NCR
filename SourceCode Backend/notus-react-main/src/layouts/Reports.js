import React from "react"; 
import { Switch, Route, Redirect } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";

// views
import DeveloperReport from "views/Reports/DeveloperReport";
import TariffRatesReport from "views/Reports/TariffRatesReport";

export default function Reports() {
    return (
        <>
          <Sidebar />
          <div className="ds-shell">
            <HeaderTopBar />
            <HeaderStatsWithoutCards />
            <div className="flex-grow ds-content py-ds-6">
                <Switch>
                  <Route path="/reports/developer" exact component={DeveloperReport} />
                  <Route path="/reports/tariff-rates" exact component={TariffRatesReport} />
                  <Route path="/reports" exact component={DeveloperReport} />

                  {/* Redirect any other /reports/* routes to developer report */}
                  <Redirect from="/reports/*" to="/reports/developer" />
                </Switch>
              </div>
              <FooterAdmin />
            </div>
        </>
    );
}
