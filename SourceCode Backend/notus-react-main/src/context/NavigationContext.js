import React, { createContext, useContext, useEffect, useState } from "react";

const NavigationContext = createContext(null);

export function NavigationProvider({ children }) {
  const [isSidebarCollapsed, setSidebarCollapsed] = useState(false);

  useEffect(() => {
    document.documentElement.classList.toggle(
      "ncre-sidebar-collapsed",
      isSidebarCollapsed
    );
  }, [isSidebarCollapsed]);

  useEffect(() => () => {
      document.documentElement.classList.remove("ncre-sidebar-collapsed");
  }, []);

  const toggleSidebar = () => {
    setSidebarCollapsed((current) => !current);
  };

  return (
    <NavigationContext.Provider value={{ isSidebarCollapsed, toggleSidebar }}>
      {children}
    </NavigationContext.Provider>
  );
}

export function useNavigation() {
  const context = useContext(NavigationContext);
  if (!context) {
    throw new Error("useNavigation must be used within NavigationProvider");
  }
  return context;
}