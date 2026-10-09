// src/layouts/MasterAmendmentLayout.js
import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import BulkCustomerAmendment from "views/Amendment/BulkCustomerAmendment";

export default function MasterAmendmentLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <BulkCustomerAmendment />
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
