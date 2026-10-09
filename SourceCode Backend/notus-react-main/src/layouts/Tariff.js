import React from "react"; 
import { Switch, Route, Redirect, useLocation } from "react-router-dom";

// components
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderTopBar from "components/Headers/HeaderTopBar.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";

// views
import TariffNew from "views/tariff/TariffNew";
import TariffArchive from 'views/tariff/TariffArchive';
import CurrentTariff from "views/tariff/CurrentTariff";
import LiveTariff from 'views/tariff/LiveTariff';
import YearTariffSetup from 'views/tariff/YearTariffSetup';

export default function Tariff() {
  //
  const location = useLocation();
  const isYearlySetup = location.pathname === "/tariff/yearly-setup";

  return (
        <>
          <Sidebar />
          <div className="ds-shell">
            <HeaderTopBar />
            {!isYearlySetup && <HeaderStatsWithoutCards />}
            <div className="flex-grow ds-content py-ds-6">
              <Switch>
                <Route path="/tariff" exact component={TariffNew} />
                <Route path="/tariff/yearly-setup" exact component={YearTariffSetup} />
                <Route path="/tariff/current" exact component={CurrentTariff} />
                <Route path="/tariff/live" exact component={LiveTariff} />
                <Route path="/tariff/archive" exact component={TariffArchive} />

                {/* Redirect any other /tariff/* routes to main */}
                <Redirect from="/tariff/*" to="/tariff" />
              </Switch>
            </div>
            <FooterAdmin />
          </div>
        </>
    );
}
