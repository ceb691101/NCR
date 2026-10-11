import React, { useEffect, useState } from "react";
import { Check, Pencil, RotateCcw, X } from "lucide-react";
import yearTariffSetupService from "services/yearTariffSetupService";
import PageHeader from "components/Headers/PageHeader";

const roles = ["Electrical Engineer", "Chief Engineer", "DGM", "Director", "DIRECTOR", "Admin"];
const statusLabels = { "0": "Draft", "1": "Awaiting CE", "4": "Awaiting Director", "2": "Approved" };
const statusBadgeColors = {
  "0": "border-amber-200 bg-amber-50 text-amber-700",
  "1": "border-blue-200 bg-[#e6f0fa] text-[#002244]",
  "4": "border-indigo-200 bg-indigo-50 text-indigo-700",
  "2": "border-emerald-200 bg-emerald-50 text-emerald-700",
};
const progressCards = [
  { key: "0", label: "Drafts", detail: "With Engineers", icon: "fas fa-file-signature", color: "bg-amber-500" },
  { key: "1", label: "Awaiting CE", detail: "Recommendation", icon: "fas fa-user-clock", color: "bg-[#002244]" },
  { key: "4", label: "Awaiting DGM", detail: "Final Approval", icon: "fas fa-user-check", color: "bg-indigo-600" },
  { key: "2", label: "Approved", detail: "Completed", icon: "fas fa-check-circle", color: "bg-emerald-600" },
];
const statusTabs = [
  { key: "all", label: "All Tariffs" },
  { key: "0", label: "Drafts" },
  { key: "1", label: "Awaiting CE" },
  { key: "4", label: "Awaiting DGM" },
  { key: "2", label: "Approved" },
];

export default function YearTariffSetup() {
  const role = sessionStorage.getItem("user_category") || "";
  const userId = (sessionStorage.getItem("user_id") || "").trim();
  const isAdmin = role === "Admin";
  const [rows, setRows] = useState([]);
  const [editing, setEditing] = useState(null);
  const [draft, setDraft] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [activeStatus, setActiveStatus] = useState("all");

  const statusCounts = rows.reduce((counts, row) => {
    const status = String(row.status || "").trim();
    counts[status] = (counts[status] || 0) + 1;
    return counts;
  }, {});
  const visibleRows = activeStatus !== "all"
    ? rows.filter((row) => String(row.status || "").trim() === activeStatus)
    : rows;

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await yearTariffSetupService.getRows();
      setRows(response.data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to load tariff setup.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const save = async (row) => {
    try {
      await yearTariffSetupService.update(row.folioNo, {
        nextTariff: draft.nextTariff,
        tariffChanged: draft.tariffChanged,
      });
      setEditing(null);
      await load();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to save tariff details.");
    }
  };

  const action = async (folioNo, approved) => {
    try {
      await yearTariffSetupService.decide(folioNo, approved);
      await load();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Unable to update tariff status.");
    }
  };

  if (!roles.includes(role)) return <div className="p-8 text-ink-600">Access denied.</div>;
  return (
    <div className="w-full space-y-ds-5">
      <section className="space-y-ds-4">
        <PageHeader
          title={isAdmin ? "System Tariff Setup (Supervisory View)" : "Yearly Tariff Setup"}
          subtitle={isAdmin ? "Supervise yearly tariff changes across all workflow stages." : `${role} workflow`}
          stats={progressCards.map((item) => ({
            key: item.key,
            label: item.label,
            value: loading ? "..." : statusCounts[item.key] || 0,
            detail: item.detail,
          }))}
        />
      </section>

      <section className="ds-card-refined w-full overflow-hidden">
        {error && <div className="m-4 rounded-lg border border-critical-200 bg-critical-50 px-4 py-3 text-sm text-critical-700 sm:m-6">{error}</div>}
        <div className="flex flex-wrap gap-2 border-b border-ink-200 bg-ink-50 px-4 py-3 sm:px-6">
            {statusTabs.map((item) => (
              <button key={item.key} type="button" onClick={() => setActiveStatus(item.key)} className={`cursor-pointer rounded-lg border px-4 py-2 text-xs font-semibold transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-700/30 ${activeStatus === item.key ? "border-navy-200 bg-navy-50 text-navy-800" : "border-transparent bg-transparent text-ink-600 hover:bg-white hover:text-ink-900"}`}>
                {item.label}{item.key !== "all" ? ` (${statusCounts[item.key] || 0})` : ` (${rows.length})`}
              </button>
            ))}
        </div>
        <div className="p-ds-5">
          {loading ? <div className="flex items-center justify-center py-12 text-sm font-medium text-ink-500"><RotateCcw size={20} className="mr-3 animate-spin text-navy-700" aria-hidden="true" />Loading tariff setup...</div> : (
            <>
              <div className="ds-table-wrap w-full">
                <div className="w-full overflow-x-auto">
                  <table className="ds-table w-full min-w-[1100px] text-left text-sm">
                    <thead>
                      <tr>
                        <th className="ds-th">Folio No</th>
                        <th className="ds-th">Developer Name</th>
                        <th className="ds-th">Facility Name</th>
                        <th className="ds-th">Tariff Type</th>
                        <th className="ds-th">Current Tariff</th>
                        <th className="ds-th">Next Tariff</th>
                        <th className="ds-th">Tariff Change Date</th>
                        <th className="ds-th">Status</th>
                        {!isAdmin && <th className="ds-th">Action</th>}
                      </tr>
                    </thead>
                    <tbody>
                      {visibleRows.map((row) => {
                        const isEditing = editing === row.folioNo;
                        const rowStatus = String(row.status || "").trim();
                        const isAssignedEngineer = String(row.responsibleEe || "").trim() === userId;
                        const canEdit = role === "Electrical Engineer" && rowStatus === "0" && isAssignedEngineer;
                        const canReview = role === "Chief Engineer" && rowStatus === "1";
                        const canApprove = (role === "DGM" || role === "Director" || role === "DIRECTOR") && rowStatus === "4";
                        return <tr key={row.folioNo} className="ds-tr">
                          <td className="ds-td whitespace-nowrap font-medium">{row.folioNo}</td>
                          <td className="ds-td">{row.developerName || "-"}</td>
                          <td className="ds-td">{row.facilityName || "-"}</td>
                          <td className="ds-td">{row.tariffType || "-"}</td>
                          <td className="ds-td whitespace-nowrap">{row.currentTariff ?? "-"}</td>
                          <td className="ds-td">{isEditing ? <input className="ds-input w-24" type="number" step="0.01" value={draft.nextTariff} onChange={(e) => setDraft({ ...draft, nextTariff: e.target.value })} /> : row.nextTariff ?? "-"}</td>
                          <td className="ds-td whitespace-nowrap">{isEditing ? <input className="ds-input" type="date" value={draft.tariffChanged} onChange={(e) => setDraft({ ...draft, tariffChanged: e.target.value })} /> : String(row.tariffChanged || "").slice(0, 10) || "-"}</td>
                          <td className="ds-td"><span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${statusBadgeColors[rowStatus] || "border-ink-200 bg-ink-50 text-ink-600"}`}>{statusLabels[rowStatus] || "Unknown"}</span></td>
                          {!isAdmin && <td className="ds-td">
                            <div className="flex items-center gap-2">
                              {canEdit && (isEditing ? <><button type="button" title="Save" onClick={() => save(row)} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-emerald-600 transition hover:border-emerald-200 hover:bg-emerald-50"><Check size={16} /></button><button type="button" title="Cancel" onClick={() => setEditing(null)} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-50"><X size={16} /></button></> : <><button type="button" title="Edit" onClick={() => { setEditing(row.folioNo); setDraft({ nextTariff: row.nextTariff, tariffChanged: String(row.tariffChanged || "").slice(0, 10) }); }} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-500 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-700"><Pencil size={15} /></button><button type="button" title="Submit for verification" onClick={() => yearTariffSetupService.submit(row.folioNo).then(load).catch((requestError) => setError(requestError.response?.data?.message || "Unable to submit tariff details."))} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-emerald-600 transition hover:border-emerald-200 hover:bg-emerald-50"><Check size={16} /></button></>)}
                              {(canReview || canApprove) && <><button type="button" title="Approve" onClick={() => action(row.folioNo, true)} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-emerald-600 transition hover:border-emerald-200 hover:bg-emerald-50"><Check size={16} /></button><button type="button" title="Reject" onClick={() => action(row.folioNo, false)} className="flex h-8 w-8 items-center justify-center rounded-full border border-slate-200 bg-white text-red-600 transition hover:border-red-200 hover:bg-red-50"><X size={16} /></button></>}
                            </div>
                          </td>}
                        </tr>;
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
              {!visibleRows.length && <div className="py-12 text-center text-ink-500">{isAdmin ? "No tariff records match this status." : "No tariff records require your action."}</div>}
            </>
          )}
        </div>
      </section>
    </div>
  );
}
