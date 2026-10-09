// src/context/SidebarContext.js
import React, { createContext, useContext, useEffect, useState } from "react";

const STORAGE_KEY = "ncre_sidebar_collapsed";

const SidebarContext = createContext(undefined);

export function SidebarProvider({ children }) {
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return localStorage.getItem(STORAGE_KEY) === "1";
    } catch (err) {
      return false;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, collapsed ? "1" : "0");
    } catch (err) {
      // Ignore localStorage write errors (e.g. private-mode quota)
    }
  }, [collapsed]);

  const toggleSidebar = () => setCollapsed((prev) => !prev);
  const expandSidebar = () => setCollapsed(false);
  const collapseSidebar = () => setCollapsed(true);

  return (
    <SidebarContext.Provider
      value={{ collapsed, toggleSidebar, expandSidebar, collapseSidebar }}
    >
      {children}
    </SidebarContext.Provider>
  );
}

export function useSidebar() {
  const ctx = useContext(SidebarContext);
  if (ctx === undefined) {
    throw new Error("useSidebar must be used within a SidebarProvider");
  }
  return ctx;
}