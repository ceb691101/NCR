import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import FooterAdmin from "components/Footers/FooterAdmin.js";
import HeaderStatsWithoutCards from "components/Headers/HeaderStatsWithoutCards";
import UpdateAmendment from "views/Amendment/UpdateAmendment";

export default function RejectedAmendmentLayout() {
  return (
    <>
      <Sidebar />
      <div className="ds-shell">
        <HeaderStatsWithoutCards />
        <div className="flex-grow ds-content py-ds-6">
          <UpdateAmendment />
          <FooterAdmin />
        </div>
      </div>
    </>
  );
}
