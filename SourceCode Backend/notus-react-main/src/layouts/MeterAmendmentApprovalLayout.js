// src/layouts/MeterAmendmentApprovalLayout.js
import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import MeterAmendmentApproval from "views/Amendment/MeterAmendmentApproval";

export default function MeterAmendmentApprovalLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <MeterAmendmentApproval />
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
