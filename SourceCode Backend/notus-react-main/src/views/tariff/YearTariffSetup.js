import React, { useEffect, useState } from "react";
import { Check, Pencil, RotateCcw, X } from "lucide-react";
import yearTariffSetupService from "services/yearTariffSetupService";

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
    <div className="w-full pt-4">
      <div className="relative pb-32 pt-10 -mx-4 md:-mx-10 px-4 md:px-10 bg-gradient-to-b from-[#001a33] via-[#002244] to-[#001122] border-b border-blue-800/40 shadow-xl rounded-b-3xl">
        <div className="mx-auto flex w-full flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h1 className="text-xl font-extrabold tracking-wide text-white">
              {isAdmin ? "System Tariff Setup (Supervisory View)" : "Yearly Tariff Setup"}
            </h1>
            <p className="mt-1 text-xs text-blue-200">
              {isAdmin ? "Supervise yearly tariff changes across all workflow stages." : `${role} workflow`}
            </p>
          </div>
          <button type="button" title="Refresh" onClick={load} disabled={loading} className="inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-xl bg-emerald-500 px-4 text-xs font-bold text-white shadow-md transition hover:bg-emerald-600 disabled:opacity-50">
            <RotateCcw size={15} aria-hidden="true" /> Refresh
          </button>
        </div>
        <div className="mt-8 grid grid-cols-1 gap-5 sm:grid-cols-2 md:grid-cols-4">
        {progressCards.map((item) => {
          return <div key={item.key} className="flex min-h-24 items-center space-x-4 rounded-2xl border border-slate-100 bg-white p-5 shadow-sm transition-all duration-200 hover:shadow-md">
            <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl text-xl text-white shadow-xs ${item.color}`}>
              <i className={item.icon} aria-hidden="true"></i>
            </div>
            <div className="min-w-0">
              <p className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">{item.label}</p>
              <p className="my-0.5 text-2xl font-extrabold text-slate-800">{loading ? "..." : statusCounts[item.key] || 0}</p>
              <p className="text-xs font-medium text-slate-400">{item.detail}</p>
            </div>
          </div>;
        })}
        </div>
        </div>

      <div className="relative z-20 -mt-20 mb-8 mx-auto w-full overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xl">
        {error && <div className="m-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 sm:m-6">{error}</div>}
        <div className="flex flex-wrap gap-2 border-b border-slate-200 bg-slate-50 px-4 py-3 sm:px-6">
            {statusTabs.map((item) => (
              <button key={item.key} type="button" onClick={() => setActiveStatus(item.key)} className={`cursor-pointer rounded-xl border px-4 py-2 text-xs font-bold transition-all duration-200 focus:outline-none ${activeStatus === item.key ? "border-blue-200/60 bg-[#e6f0fa] text-[#002244] shadow-2xs" : "border-transparent bg-transparent text-slate-600 hover:bg-slate-100 hover:text-[#002244]"}`}>
                {item.label}{item.key !== "all" ? ` (${statusCounts[item.key] || 0})` : ` (${rows.length})`}
              </button>
            ))}
        </div>
        <div className="p-4 sm:p-6">
          {loading ? <div className="flex items-center justify-center py-12 text-sm font-medium text-slate-500"><RotateCcw size={20} className="mr-3 animate-spin text-[#002244]" aria-hidden="true" />Loading tariff setup...</div> : (
            <>
              <div className="w-full overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
                <div className="w-full overflow-x-auto">
                  <table className="w-full min-w-[1100px] border-collapse text-left text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Folio No</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Developer Name</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Facility Name</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Tariff Type</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Current Tariff</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Next Tariff</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Tariff Change Date</th>
                        <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Status</th>
                        {!isAdmin && <th className="border-b border-slate-200 bg-slate-50 px-4 py-3 text-[11px] font-bold uppercase tracking-wider">Action</th>}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 bg-white text-sm text-slate-700">
                      {visibleRows.map((row) => {
                        const isEditing = editing === row.folioNo;
                        const rowStatus = String(row.status || "").trim();
                        const isAssignedEngineer = String(row.responsibleEe || "").trim() === userId;
                        const canEdit = role === "Electrical Engineer" && rowStatus === "0" && isAssignedEngineer;
                        const canReview = role === "Chief Engineer" && rowStatus === "1";
                        const canApprove = (role === "DGM" || role === "Director" || role === "DIRECTOR") && rowStatus === "4";
                        return <tr key={row.folioNo} className="transition-colors duration-150 hover:bg-slate-50">
                          <td className="whitespace-nowrap px-4 py-4 font-medium text-slate-700">{row.folioNo}</td>
                          <td className="px-4 py-4">{row.developerName || "-"}</td>
                          <td className="px-4 py-4">{row.facilityName || "-"}</td>
                          <td className="px-4 py-4">{row.tariffType || "-"}</td>
                          <td className="whitespace-nowrap px-4 py-4">{row.currentTariff ?? "-"}</td>
                          <td className="px-4 py-4">{isEditing ? <input className="w-24 rounded-lg border border-slate-300 px-2 py-1.5 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100" type="number" step="0.01" value={draft.nextTariff} onChange={(e) => setDraft({ ...draft, nextTariff: e.target.value })} /> : row.nextTariff ?? "-"}</td>
                          <td className="whitespace-nowrap px-4 py-4">{isEditing ? <input className="rounded-lg border border-slate-300 px-2 py-1.5 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100" type="date" value={draft.tariffChanged} onChange={(e) => setDraft({ ...draft, tariffChanged: e.target.value })} /> : String(row.tariffChanged || "").slice(0, 10) || "-"}</td>
                          <td className="px-4 py-4"><span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${statusBadgeColors[rowStatus] || "border-slate-200 bg-slate-50 text-slate-600"}`}>{statusLabels[rowStatus] || "Unknown"}</span></td>
                          {!isAdmin && <td className="px-4 py-4">
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
              {!visibleRows.length && <div className="py-12 text-center text-slate-500">{isAdmin ? "No tariff records match this status." : "No tariff records require your action."}</div>}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
