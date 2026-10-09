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


  return (
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <button
        type="button"
        className="block focus:outline-none bg-transparent border-0 p-0 cursor-pointer"
        onClick={toggleDropdown}
        title="Account"
      >
        <div className="flex items-center">
          <span className="w-9 h-9 md:w-10 md:h-10 text-sm bg-white inline-flex items-center justify-center rounded-full border border-ink-300 p-0.5 transition-colors duration-150 hover:border-navy-400 cursor-pointer">
            <img
              alt="User Profile"
              className="w-full h-full rounded-full object-cover"
              src={profileimg}
            />
          </span>
        </div>
      </button>

      {dropdownPopoverShow && (
        <div
          style={{ zIndex: 99999 }}
          className="absolute right-0 top-full mt-2 bg-white text-base py-1.5 list-none text-left rounded-lg shadow-overlay min-w-48 border border-ink-200 animate-fadeIn"
        >
          <button
            type="button"
            className="text-body-sm py-2.5 px-4 font-medium block w-full whitespace-nowrap bg-transparent text-ink-700 hover:bg-ink-50 hover:text-navy-800 text-left focus:outline-none flex items-center transition-colors cursor-pointer"
            onClick={handleLogout}
          >
            <i className="fas fa-sign-out-alt mr-2.5 text-ink-400"></i>
            Log Out
          </button>
        </div>
      )}
    </div>
  );
};

export default UserDropdown;