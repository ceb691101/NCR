import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import {
  getAreaAndBill,
  setAreaAndBill,
  clearAreaAndBill,
} from "../services/AreaAndBillService";
import {
  fetchPermittedAreas,
  selectArea as selectAreaApi,
} from "../services/areaPermissionService";

const AreaAndBillContext = createContext(null);

export const AreaAndBillProvider = ({ children }) => {
  const [data, setData] = useState(() => getAreaAndBill());
  // No area is forced at login. The dialog only opens from the header bar.
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const openDialog = useCallback(() => setIsDialogOpen(true), []);
  const closeDialog = useCallback(() => setIsDialogOpen(false), []);

  /**
   * Pulls the permitted areas from the backend and stores them locally.
   * The selection resets to "all permitted areas" on every login.
   */
  const refreshPermittedAreas = useCallback(async ({ resetSelection = true } = {}) => {
    if (!sessionStorage.getItem("session_id")) {
      return null;
    }

    setLoading(true);
    setError("");
    try {
      const result = await fetchPermittedAreas();
      const payload = {
        userCategory: sessionStorage.getItem("user_category") || "",
        regionCode: sessionStorage.getItem("region_code") || "",
        provinceCode: sessionStorage.getItem("province_code") || "",
        areaCode: sessionStorage.getItem("area_code") || "",
        accessScope: result.accessScope || "",
        permittedAreas: result.permittedAreas || [],
        selectedAreaCode: resetSelection
          ? ""
          : result.selectedAreaCode || "",
      };
      const stored = setAreaAndBill(payload);
      if (stored) {
        // setAreaAndBill only notifies on a selection change; mirror it here so
        // consumers update even when the permitted list changed but the area did not.
        setData(getAreaAndBill());
        window.dispatchEvent(
          new CustomEvent("areaAndBill:changed", {
            detail: {
              previousArea: null,
              currentArea: payload.selectedAreaCode,
              data: getAreaAndBill(),
            },
          })
        );
      }
      return result;
    } catch (err) {
      console.error("Failed to load permitted areas:", err);
      setError(err.message || "Failed to load permitted areas");
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Narrow to one permitted area, or pass null to go back to all of them.
   * The selection is persisted server side so every API call is scoped the same way.
   */
  const selectArea = useCallback(
    async (areaCode) => {
      setLoading(true);
      setError("");
      try {
        const result = await selectAreaApi(areaCode);
        const current = getAreaAndBill() || {};
        const payload = {
          userCategory: current.userCategory || "",
          regionCode: current.regionCode || "",
          provinceCode: current.provinceCode || "",
          areaCode: current.areaCode || "",
          accessScope: current.accessScope || "",
          permittedAreas: result.permittedAreas?.length
            ? result.permittedAreas
            : current.permittedAreas || [],
          selectedAreaCode: result.selectedAreaCode || "",
        };
        setAreaAndBill(payload);
        setData(getAreaAndBill());
        return result;
      } catch (err) {
        console.error("Failed to update area selection:", err);
        setError(err.message || "Failed to update area selection");
        return null;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const save = useCallback((payload) => {
    const ok = setAreaAndBill(payload);
    if (ok) {
      setData(getAreaAndBill());
      setIsDialogOpen(false);
    }
    return ok;
  }, []);

  const clear = useCallback(() => {
    clearAreaAndBill();
    setData(null);
    // After a logout the areas are re-resolved on the next login.
    setIsDialogOpen(false);
  }, []);

  useEffect(() => {
    const handler = () => {
      setData(getAreaAndBill());
    };
    window.addEventListener("areaAndBill:clear", handler);
    window.addEventListener("areaAndBill:changed", handler);
    return () => {
      window.removeEventListener("areaAndBill:clear", handler);
      window.removeEventListener("areaAndBill:changed", handler);
    };
  }, []);

  // Seed from storage when a session already exists (page refresh mid-session).
  useEffect(() => {
    if (!sessionStorage.getItem("session_id")) {
      return;
    }
    if (getAreaAndBill()?.permittedAreas?.length) {
      setData(getAreaAndBill());
      return;
    }
    refreshPermittedAreas({ resetSelection: true });
  }, [refreshPermittedAreas]);

  return (
    <AreaAndBillContext.Provider
      value={{
        data,
        save,
        clear,
        loading,
        error,
        isDialogOpen,
        openDialog,
        closeDialog,
        selectArea,
        refreshPermittedAreas,
      }}
    >
      {children}
    </AreaAndBillContext.Provider>
  );
};

export const useAreaAndBill = () => useContext(AreaAndBillContext);
export { AreaAndBillContext };
