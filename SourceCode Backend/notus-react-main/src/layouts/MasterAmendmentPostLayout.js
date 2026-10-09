// src/layouts/MasterAmendmentPostLayout.js
import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import BulkCustomerAmendmentPost from "views/Amendment/BulkCustomerAmendmentPost";

export default function MasterAmendmentPostLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <BulkCustomerAmendmentPost />
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
