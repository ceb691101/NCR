import React from "react";
import { Switch, Route } from "react-router-dom";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";

// IMPORTANT: import PAGE, not layout
import Journals from "views/journals/Journals";
import JournalsReport from "views/journals/JournalsReport";
import JournalsConfirmation from "views/journals/JournalsConfirmation";

export default function JournalsLayout() {
    return (
        <>
            <Sidebar />
            <div className="ds-shell">
                <HeaderStatsWithoutCards />
                <div className="flex-grow ds-content py-ds-6">
                    <Switch>
                        <Route exact path="/journals" component={Journals} />
                        <Route exact path="/journals/report" component={JournalsReport} />
                        <Route exact path="/journals/confirmation" component={JournalsConfirmation} />
                    </Switch>
                    <FooterAdmin />
                </div>
            </div>
        </>
    );
}
