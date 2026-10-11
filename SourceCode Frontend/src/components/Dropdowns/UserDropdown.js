import React, { useRef, useEffect, useState } from "react";
import profileimg from "../../assets/img/profile.jpeg";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import { apiPath } from "../../config";

const UserDropdown = () => {
  const [dropdownPopoverShow, setDropdownPopoverShow] = useState(false);
  const auth = useAuth();
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownPopoverShow(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const toggleDropdown = (e) => {
    e.preventDefault();
    setDropdownPopoverShow((prev) => !prev);
  };

  const handleLogout = async () => {
    setDropdownPopoverShow(false);
    try { window.dispatchEvent(new Event("user:loggingOut")); } catch (e) {}
    try {
      // Broadcast logout event to all other open tabs
      try {
        localStorage.setItem("ncre_logout_event", Date.now().toString());
      } catch (e) {}

      // Await the auth.logout() to ensure all cleanup (like clearAreaAndBill) happens before redirect
      if (auth && auth.logout) {
        await auth.logout();
      }

      sessionStorage.clear();
      localStorage.removeItem("ceb_area_bill");
      try { window.dispatchEvent(new Event("areaAndBill:clear")); } catch (e) {}
      
      toast.success("Logout successful");
      window.location.href = "/auth/login";
    } catch (error) {
      console.error("Error during logout:", error);
      toast.error("An error occurred during logout. Please try again.");
      
      // Fallback redirect even if error occurs
      sessionStorage.clear();
      localStorage.removeItem("ceb_area_bill");
      window.location.href = "/auth/login";
    }
  };

  // Read user info from sessionStorage
  const userName = sessionStorage.getItem("user_name") || "User";
  const epfNumber = sessionStorage.getItem("user_id") || "—";
  const userCategory = sessionStorage.getItem("user_category") || "User";
  const userInitial = userName.charAt(0).toUpperCase();

  // Format user category for display
  const formatCategory = (cat) => {
    if (!cat) return "User";
    return cat
      .split(/[\s_]+/)
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
      .join(" ");
  };

  const displayCategory = formatCategory(userCategory);

  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <button
        type="button"
        className="block focus:outline-none bg-transparent border-0 p-0 cursor-pointer"
        onClick={toggleDropdown}
        title="Account"
        aria-label="User menu"
        aria-haspopup="true"
        aria-expanded={dropdownPopoverShow}
      >
        <div className="flex items-center gap-2">
          {/* Avatar with subtle ring animation */}
          <div className="relative">
            <span className="w-9 h-9 md:w-10 md:h-10 text-sm bg-white inline-flex items-center justify-center rounded-full border-2 border-ink-200 p-0.5 transition-all duration-200 hover:border-navy-400 cursor-pointer">
              <img
                alt="User Profile"
                className="w-full h-full rounded-full object-cover"
                src={profileimg}
              />
            </span>
            {/* Online indicator */}
            <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-success-500 border-2 border-white" aria-hidden="true" />
          </div>
        </div>
      </button>

      {dropdownPopoverShow && (
        <>
          {/* Backdrop for click-outside detection on mobile */}
          <div
            className="fixed inset-0 z-40 bg-transparent"
            onClick={() => setDropdownPopoverShow(false)}
            aria-hidden="true"
          />
          <div
            style={{ zIndex: 50 }}
            className="absolute right-0 top-full mt-2 bg-white text-base py-0 rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.12)] min-w-[280px] border border-ink-200 animate-fadeIn overflow-hidden"
            role="menu"
            aria-orientation="vertical"
          >
            {/* User Info Header */}
            <div className="px-4 py-3 bg-gradient-to-r from-navy-800/95 to-navy-700/90 text-white relative overflow-hidden">
              {/* Subtle background pattern */}
              <div className="absolute inset-0 opacity-10" aria-hidden="true">
                <svg width="100%" height="100%" viewBox="0 0 100 100" preserveAspectRatio="none">
                  <defs>
                    <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                      <path d="M 20 0 L 0 0 0 20" fill="none" stroke="currentColor" stroke-width="0.5" />
                    </pattern>
                  </defs>
                  <rect width="100%" height="100%" fill="url(#grid)" />
                </svg>
              </div>
              <div className="relative flex items-center gap-3">
                {/* Avatar with white ring */}
                <div className="relative flex-shrink-0">
                  <span className="w-12 h-12 inline-flex items-center justify-center rounded-full bg-white/20 border-2 border-white/30">
                    <span className="text-xl font-bold text-white">{userInitial}</span>
                  </span>
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold text-white truncate">{userName}</p>
                  <p className="text-[11px] font-medium text-white/70 truncate">EPF: {epfNumber}</p>
                  <p className="text-[10px] font-medium text-white/60 uppercase tracking-wider mt-0.5">{displayCategory}</p>
                </div>
              </div>
            </div>

            {/* Divider */}
            <div className="h-px bg-gradient-to-r from-transparent via-ink-200 to-transparent mx-3" role="separator" />

            {/* Action Items - reserved for future use */}
            <div className="px-2 py-2" role="none" />

            {/* Divider */}
            <div className="h-px bg-gradient-to-r from-transparent via-ink-200 to-transparent mx-3" role="separator" />

            {/* Logout Button - Red, prominent, full width */}
            <button
              type="button"
              className="w-full flex items-center justify-center gap-2.5 px-3 py-2.5 text-body-sm font-semibold text-white bg-gradient-to-r from-critical-600 to-critical-700 hover:from-critical-700 hover:to-critical-800 active:from-critical-800 active:to-critical-900 rounded-xl transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-critical-500/40 focus-visible:ring-offset-2 shadow-sm hover:shadow-md cursor-pointer group"
              onClick={handleLogout}
              role="menuitem"
            >
              <i className="fas fa-sign-out-alt text-lg transition-transform group-hover:-translate-x-1" aria-hidden="true" />
              <span>Log Out</span>
              <i className="fas fa-arrow-right text-[11px] transition-transform group-hover:translate-x-1" aria-hidden="true" />
            </button>

            {/* Footer hint */}
            <div className="px-3 pb-2 text-center">
              <p className="text-[10px] text-ink-400">NCRE System v1.2.3</p>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default UserDropdown;