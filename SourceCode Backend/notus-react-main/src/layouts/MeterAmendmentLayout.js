// src/layouts/MeterAmendmentLayout.js
import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import BulkCustomerMeterAmendmentView from "views/Amendment/BulkCustomerMeterAmendmentView";

export default function MeterAmendmentLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <BulkCustomerMeterAmendmentView />
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
