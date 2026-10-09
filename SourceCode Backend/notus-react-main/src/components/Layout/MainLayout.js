// src/components/Layout/MainLayout.js
import React from "react";
import Sidebar from "components/Sidebar/Sidebar.js";
import { useSidebar } from "../../context/SidebarContext";

export default function MainLayout({ children, className = "" }) {
  const { collapsed } = useSidebar();

  return (
    <>
      <Sidebar />
      <div
        className={`relative transition-all duration-300 ease-in-out ${
          collapsed ? "md:ml-[68px]" : "md:ml-80"
        } ${className}`}
      >
        {children}
      </div>
    </>
  );
}