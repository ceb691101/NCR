import React, { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import AccountManagement from "views/SystemAdminView/AccountManagement";
import { getAllUserAccounts } from "services/userAccountService";
import {
  createFunction,
  getFunctions,
  getUserPermissions,
  saveUserPermissions,
  updateFunction,
} from "services/functionManagementService";

const permissionKey = (item) => JSON.stringify([item.funcId, item.subFuncId]);
const activeStatus = (status) => String(status || "").trim() === "2";
const APPLICATION_ID = "NCR";

const FunctionManagement = () => {
  const location = useLocation();
  const [functions, setFunctions] = useState([]);
  const [users, setUsers] = useState([]);
  const [tab, setTab] = useState(location.pathname.endsWith("/functions") ? "catalog" : "users");
  const [loading, setLoading] = useState(true);
  const [permissionsLoading, setPermissionsLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [refreshToken, setRefreshToken] = useState(0);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [permissions, setPermissions] = useState([]);
  const [search, setSearch] = useState("");
  const [dialogFunction, setDialogFunction] = useState(null);
  const [editing, setEditing] = useState(false);

  const filteredFunctions = useMemo(() => {
    const normalized = search.trim().toLowerCase();
    return functions.filter((item) => {
      const matchesApplication = item.applId?.trim() === APPLICATION_ID;
      const searchable = [item.funcId, item.funcNm, item.subFuncId, item.subFuncNm]
        .join(" ")
        .toLowerCase();
      return matchesApplication && (!normalized || searchable.includes(normalized));
    });
  }, [functions, search]);

  const activeUsersCount = users.filter((user) => String(user.status) === "1").length;
  const activeFunctionRows = functions.filter((item) =>
    item.applId?.trim() === APPLICATION_ID && activeStatus(item.status)
  );
  const activeFunctionsCount = new Set(activeFunctionRows
    .map((item) => item.funcId?.trim())
    .filter(Boolean)).size;
  const activeSubFunctionsCount = activeFunctionRows.length - activeFunctionsCount;
  const selectedUser = users.find((user) => user.userId === selectedUserId);

  const loadBaseData = async () => {
    setLoading(true);
    try {
      const [catalog, accountRows] = await Promise.all([getFunctions(), getAllUserAccounts()]);
      const normalizedUsers = accountRows.map((user) => ({
        userId: String(user.userId || user.user_id || "").trim(),
        userName: String(user.userName || user.user_name || "").trim(),
        userCat: String(user.userCat || user.user_cat || "").trim(),
        epfNum: String(user.epfNum || user.epf_num || "").trim(),
        status: user.status,
      })).filter((user) => user.userId);
      setFunctions(catalog);
      setUsers(normalizedUsers);
      setSelectedUserId((current) => current || normalizedUsers[0]?.userId || "");
    } catch (error) {
      toast.error(error.message || "Could not load function management data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBaseData();
  }, []);

  useEffect(() => {
    setTab(location.pathname.endsWith("/functions") ? "catalog" : "users");
  }, [location.pathname]);

  useEffect(() => {
    if (!selectedUserId) {
      setPermissions([]);
      setPermissionsLoading(false);
      return;
    }
    let cancelled = false;
    setPermissions([]);
    setPermissionsLoading(true);
    getUserPermissions(selectedUserId, APPLICATION_ID)
      .then((result) => {
        if (!cancelled) setPermissions(result.permissions || []);
      })
      .catch((error) => {
        if (!cancelled) {
          setPermissions([]);
          toast.error(error.message || "Could not load user permissions.");
        }
      })
      .finally(() => {
        if (!cancelled) setPermissionsLoading(false);
      });
    return () => { cancelled = true; };
  }, [refreshToken, selectedUserId]);

  const refreshData = async () => {
    await loadBaseData();
    setRefreshToken((current) => current + 1);
  };

  const openCreateDialog = () => {
    setEditing(false);
    setDialogFunction({
      funcId: "",
      applId: APPLICATION_ID,
      funcNm: "",
      subFuncId: "",
      subFuncNm: "",
      seqNo: "0",
      status: "2",
    });
  };

  const openEditDialog = (item) => {
    setEditing(true);
    setDialogFunction({
      ...item,
      applId: item.applId?.trim(),
      funcId: item.funcId?.trim(),
      subFuncId: item.subFuncId?.trim(),
      seqNo: item.seqNo ?? "0",
      status: String(item.status || "2").trim(),
    });
  };

  const submitFunction = async (event) => {
    event.preventDefault();
    setSaving(true);
    const payload = {
      ...dialogFunction,
      applId: APPLICATION_ID,
      funcId: dialogFunction.funcId.trim().toUpperCase(),
      subFuncId: dialogFunction.subFuncId.trim().toUpperCase(),
      funcNm: dialogFunction.funcNm.trim(),
      subFuncNm: dialogFunction.subFuncNm.trim(),
      seqNo: Number(dialogFunction.seqNo || 0),
    };
    try {
      if (editing) await updateFunction(payload);
      else await createFunction(payload);
      toast.success(editing ? "Function updated." : "Function created.");
      setDialogFunction(null);
      await loadBaseData();
      setRefreshToken((current) => current + 1);
    } catch (error) {
      toast.error(error.message || "Could not save the function.");
    } finally {
      setSaving(false);
    }
  };

  const togglePermission = (functionRow) => {
    const key = permissionKey(functionRow);
    setPermissions((current) => current.map((item) =>
      permissionKey(item.function) === key ? { ...item, granted: !item.granted } : item
    ));
  };

  const savePermissions = async () => {
    setSaving(true);
    try {
      const selected = permissions
        .filter((item) => item.granted && activeStatus(item.function.status))
        .map(({ function: item }) => ({ funcId: item.funcId.trim(), subFuncId: item.subFuncId.trim() }));
      const result = await saveUserPermissions(selectedUserId, APPLICATION_ID, selected);
      setPermissions(result.permissions || []);
      toast.success("User access permissions saved.");
    } catch (error) {
      toast.error(error.message || "Could not save user permissions.");
    } finally {
      setSaving(false);
    }
  };

  const updateDialogField = (field, value) => {
    setDialogFunction((current) => ({ ...current, [field]: value }));
  };

  return (
    <div className="flex flex-col gap-ds-6">
      <div className=" md:-mx-10 md:px-10">
        <div className="ds-page-toolbar mx-auto">
          <div>
            <h1 className="ds-page-title">System Administration</h1>
            <p className="ds-page-subtitle mt-1">Manage users, NCR functions, and access permissions.</p>
          </div>
          <div className="ds-page-actions">
            <button
              type="button"
              onClick={refreshData}
              disabled={loading}
              className="ds-btn ds-btn-secondary ds-btn-sm"
              title="Refresh data"
            >
              <i className="fas fa-sync-alt" aria-hidden="true"></i>Refresh
            </button>
          </div>
        </div>
        <div className="mt-8 grid grid-cols-1 gap-5 sm:grid-cols-2 md:grid-cols-3">
          {[
            { label: "System Users", value: activeUsersCount, detail: "Users currently active in the NCRE system.", icon: "fas fa-users", color: "ds-stat-icon-info" },
            { label: "Main Functions", value: activeFunctionsCount, detail: "Main functions currently enabled in the system.", icon: "fas fa-list-alt", color: "ds-stat-icon-info" },
            { label: "Sub-functions", value: activeSubFunctionsCount, detail: "Subfunctions currently available.", icon: "fas fa-sitemap", color: "ds-stat-icon-success" },
          ].map((stat) => (
            <div key={stat.label} className="ds-stat-card min-h-24 rounded-xl p-4">
              <div className={`ds-stat-icon ${stat.color}`}>
                <i className={stat.icon} aria-hidden="true"></i>
              </div>
              <div className="min-w-0 flex-1">
                <p className="ds-stat-label">{stat.label}</p>
                <p className="my-0.5 text-xl font-extrabold text-ink-800">{loading ? "..." : stat.value}</p>
                <p className="truncate text-xs font-medium text-ink-400">{stat.detail}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="relative mb-8 overflow-hidden rounded-2xl border border-ink-200 bg-white p-4 shadow-card sm:p-6">

      <div className="flex flex-wrap gap-2 border-b border-ink-200 bg-ink-50 px-4 py-3 sm:px-6" role="tablist" aria-label="Management view">
        {[{ id: "users", label: "Users Management" }, { id: "catalog", label: "Function Catalog" }, { id: "access", label: "User Permission Management" }].map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={tab === item.id}
            onClick={() => setTab(item.id)}
            className={`ds-tab ${tab === item.id ? "ds-tab-active" : ""}`}
          >
            {item.label}
          </button>
        ))}
      </div>

      {tab === "users" ? (
        <AccountManagement embedded onAccountsChanged={refreshData} />
      ) : tab === "catalog" ? (
        <section aria-label="Function catalog" className="overflow-hidden">
          <div className="ds-card-header">
            <h2 className="ds-card-title">Function Catalog</h2>
            <button type="button" onClick={openCreateDialog} disabled={loading} className="ds-btn ds-btn-primary ds-btn-sm">
              <i className="fas fa-plus" aria-hidden="true"></i>Add Function
            </button>
          </div>
          <div className="ds-filter-bar">
            <label className="relative min-w-0 flex-1 lg:flex-initial">
              <span className="sr-only">Search functions</span>
              <i className="fas fa-search ds-filter-icon" aria-hidden="true"></i>
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search functions..." className="ds-filter-input h-10 w-full lg:w-64 xl:w-80" />
            </label>
            {search && (
              <button type="button" onClick={() => setSearch("")} className="ds-btn ds-btn-ghost ds-btn-sm" title="Clear search" aria-label="Clear search">
                <i className="fas fa-times" aria-hidden="true"></i>
              </button>
            )}
            <span className="ds-badge ds-badge-neutral">Application: NCR</span>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
              <thead className="bg-white text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Function</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Sub-function</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Order</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Status</th>
                  <th className="px-3 py-3 text-center text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredFunctions.map((item) => (
                  <tr key={`${item.applId}-${item.funcId}-${item.subFuncId}`} className="hover:bg-slate-50">
                    <td className="px-4 py-3 sm:px-6">
                      <div className="font-medium text-slate-700">{item.funcNm || "(unnamed)"}</div>
                      <div className="mt-0.5 text-xs text-gray-500">{item.funcId?.trim()} · {item.applId?.trim()}</div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="font-medium text-slate-700">{item.subFuncNm || "(unnamed)"}</div>
                      <div className="mt-0.5 text-xs text-gray-500">{item.subFuncId?.trim()}</div>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{item.seqNo ?? "-"}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${activeStatus(item.status) ? "bg-emerald-50 text-emerald-700" : "bg-gray-100 text-gray-600"}`}>
                        {activeStatus(item.status) ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right sm:px-6">
                      <button type="button" onClick={() => openEditDialog(item)} className="inline-flex items-center rounded-md border border-gray-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-gray-50">
                        <i className="fas fa-pen mr-2" aria-hidden="true"></i>Edit
                      </button>
                    </td>
                  </tr>
                ))}
                {!loading && filteredFunctions.length === 0 && (
                  <tr><td colSpan="5" className="px-6 py-12 text-center text-sm text-gray-500">No function records found for this application.</td></tr>
                )}
                {loading && <tr><td colSpan="5" className="px-6 py-12 text-center text-sm text-gray-500">Loading function records...</td></tr>}
              </tbody>
            </table>
          </div>
          <div className="border-t border-gray-200 px-4 py-3 text-xs text-gray-500 sm:px-6">
            {filteredFunctions.length} function record{filteredFunctions.length === 1 ? "" : "s"}
          </div>
        </section>
      ) : (
        <section aria-label="User access permissions">
          <div className="flex flex-col gap-3 border-b border-gray-200 bg-gray-50 px-4 py-4 md:flex-row md:items-end md:justify-between md:px-6">
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="flex flex-col gap-1 text-xs font-medium uppercase tracking-wide text-gray-500">
                User
                <select value={selectedUserId} onChange={(event) => setSelectedUserId(event.target.value)} className="min-w-64 rounded-md border border-gray-300 bg-white px-3 py-2.5 text-sm font-normal normal-case tracking-normal text-slate-700">
                  {users.map((user) => <option key={user.userId} value={user.userId}>{user.userName} ({user.userId})</option>)}
                </select>
              </label>
              <div className="flex flex-col gap-1 text-xs font-medium uppercase tracking-wide text-gray-500">
                Application
                <span className="inline-flex min-h-10 items-center rounded-md border border-gray-200 bg-gray-100 px-3 text-sm font-semibold normal-case tracking-normal text-slate-700">NCR</span>
              </div>
            </div>
            <button type="button" onClick={savePermissions} disabled={saving || loading || permissionsLoading || !selectedUserId} className="inline-flex h-10 items-center justify-center rounded-md bg-emerald-600 px-4 text-sm font-medium text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50">
              <i className="fas fa-save mr-2" aria-hidden="true"></i>{saving ? "Saving..." : "Save access"}
            </button>
          </div>

          <section className="mx-4 my-5 rounded-xl border border-slate-100 bg-slate-50 p-4 sm:mx-6 sm:p-5" aria-label="Selected user information">
            <div className="mb-3 flex items-center gap-2 text-slate-700">
              <i className="fas fa-user-circle text-lg text-[#002244]" aria-hidden="true"></i>
              <h2 className="text-sm font-bold">Selected User</h2>
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <div>
                <p className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Name</p>
                <p className="mt-1 truncate text-sm font-semibold text-slate-800">{selectedUser?.userName || (loading ? "Loading..." : "Not available")}</p>
              </div>
              <div>
                <p className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">User ID</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{selectedUser?.userId || "-"}</p>
              </div>
              <div>
                <p className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">Category</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{selectedUser?.userCat || "-"}</p>
              </div>
              <div>
                <p className="text-[11px] font-extrabold uppercase tracking-wider text-slate-400">EPF Number</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{selectedUser?.epfNum || "-"}</p>
              </div>
            </div>
          </section>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
              <thead className="bg-white text-xs uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="w-20 px-3 py-3 text-center text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Allow</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Function</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Sub-function</th>
                  <th className="px-3 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 lg:px-4 xl:px-6">Catalog status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {permissions.map(({ function: item, granted }) => {
                  const enabled = activeStatus(item.status);
                  return (
                    <tr key={`${item.applId}-${item.funcId}-${item.subFuncId}`} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-center sm:px-6">
                        <input type="checkbox" checked={granted} disabled={!enabled} onChange={() => togglePermission(item)} aria-label={`Allow ${item.subFuncNm || item.subFuncId}`} className="h-4 w-4 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500 disabled:opacity-40" />
                      </td>
                      <td className="px-4 py-3">
                        <div className="font-medium text-slate-700">{item.funcNm || "(unnamed)"}</div>
                        <div className="mt-0.5 text-xs text-gray-500">{item.funcId?.trim()}</div>
                      </td>
                      <td className="px-4 py-3">
                        <div className="font-medium text-slate-700">{item.subFuncNm || "(unnamed)"}</div>
                        <div className="mt-0.5 text-xs text-gray-500">{item.subFuncId?.trim()}</div>
                      </td>
                      <td className="px-4 py-3 text-gray-600">{enabled ? "Active" : "Inactive"}</td>
                    </tr>
                  );
                })}
                {!loading && permissions.length === 0 && (
                  <tr><td colSpan="4" className="px-6 py-12 text-center text-sm text-gray-500">No functions are defined for this application.</td></tr>
                )}
                {(loading || permissionsLoading) && <tr><td colSpan="4" className="px-6 py-12 text-center text-sm text-gray-500">Loading permissions...</td></tr>}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {dialogFunction && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center overflow-y-auto bg-black/40 p-4" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setDialogFunction(null); }}>
          <div role="dialog" aria-modal="true" aria-labelledby="function-dialog-title" className="w-full max-w-2xl rounded-lg bg-white shadow-xl">
            <form onSubmit={submitFunction}>
              <div className="border-b border-ink-200 px-5 py-4 sm:px-6">
                <h2 id="function-dialog-title" className="ds-section-title">{editing ? "Edit function" : "Add function"}</h2>
              </div>
              <div className="grid gap-4 px-5 py-5 sm:grid-cols-2 sm:px-6">
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700">
                  Function ID
                  <input required maxLength="8" disabled={editing} value={dialogFunction.funcId} onChange={(event) => updateDialogField("funcId", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal uppercase disabled:bg-ink-100" />
                </label>
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700">
                  Function name
                  <input required maxLength="60" value={dialogFunction.funcNm} onChange={(event) => updateDialogField("funcNm", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal" />
                </label>
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700">
                  Sub-function ID
                  <input required maxLength="8" disabled={editing} value={dialogFunction.subFuncId} onChange={(event) => updateDialogField("subFuncId", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal uppercase disabled:bg-ink-100" />
                </label>
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700">
                  Sub-function name
                  <input required maxLength="60" value={dialogFunction.subFuncNm} onChange={(event) => updateDialogField("subFuncNm", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal" />
                </label>
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700">
                  Display order
                  <input type="number" step="any" value={dialogFunction.seqNo} onChange={(event) => updateDialogField("seqNo", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal" />
                </label>
                <div className="flex items-center gap-2 text-sm font-medium text-ink-700 sm:col-span-2">
                  Application ID <span className="rounded-lg bg-ink-100 px-3 py-2 text-sm font-semibold text-ink-700">NCR</span>
                </div>
                <label className="flex flex-col gap-1 text-sm font-medium text-ink-700 sm:col-span-2">
                  Status
                  <select value={dialogFunction.status} onChange={(event) => updateDialogField("status", event.target.value)} className="rounded-md border border-ink-300 px-3 py-2 font-normal">
                    <option value="2">Active</option>
                    <option value="1">Inactive</option>
                  </select>
                </label>
              </div>
              <div className="flex justify-end gap-2 border-t border-ink-200 px-5 py-4 sm:px-6">
                <button type="button" onClick={() => setDialogFunction(null)} className="rounded-md border border-ink-300 px-4 py-2 text-sm font-medium text-ink-700 hover:bg-ink-50">Cancel</button>
                <button type="submit" disabled={saving} className="rounded-md bg-success-600 px-4 py-2 text-sm font-medium text-white hover:bg-success-700 disabled:opacity-50">{saving ? "Saving..." : "Save function"}</button>
              </div>
            </form>
          </div>
        </div>
      )}
      </div>
    </div>
  );
};

export default FunctionManagement;