import React, { useRef, useEffect } from "react";
import { createPopper } from "@popperjs/core";

const NotificationDropdown = () => {
  // dropdown props
  const [dropdownPopoverShow, setDropdownPopoverShow] = React.useState(false);
  const btnDropdownRef = React.useRef();
  const popoverDropdownRef = React.useRef();
  const dropdownContainerRef = React.useRef();

  const openDropdownPopover = () => {
    createPopper(btnDropdownRef.current, popoverDropdownRef.current, {
      placement: "bottom-start",
    });
    setDropdownPopoverShow(true);
  };

  const closeDropdownPopover = () => {
    setDropdownPopoverShow(false);
  };

  // Click outside handler
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownPopoverShow && 
          popoverDropdownRef.current && 
          !popoverDropdownRef.current.contains(event.target) &&
          btnDropdownRef.current &&
          !btnDropdownRef.current.contains(event.target)) {
        closeDropdownPopover();
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownPopoverShow]);

  return (
    <>
      <a
        className="block py-1 px-2 text-lg md:text-base text-current opacity-80 hover:opacity-100"
        href="#pablo"
        ref={btnDropdownRef}
        onClick={(e) => {
          e.preventDefault();
          dropdownPopoverShow ? closeDropdownPopover() : openDropdownPopover();
        }}
      >
        {/* Responsive bell icon */}
        <i className="fas fa-bell text-xl md:text-lg"></i>
      </a>
      <div
        ref={popoverDropdownRef}
        className={
          (dropdownPopoverShow ? "block " : "hidden ") +
          "bg-white text-body-sm z-50 float-left py-1.5 list-none text-left rounded-lg border border-ink-200 shadow-overlay mt-1.5 min-w-48"
        }
      >
        <a
          href="#pablo"
          className={
            "block w-full py-2 px-4 whitespace-nowrap bg-transparent text-left text-body-sm font-medium text-ink-700 hover:bg-ink-50 hover:text-navy-800"
          }
          onClick={(e) => e.preventDefault()}
        >
          Action
        </a>
        <a
          href="#pablo"
          className={
            "block w-full py-2 px-4 whitespace-nowrap bg-transparent text-left text-body-sm font-medium text-ink-700 hover:bg-ink-50 hover:text-navy-800"
          }
          onClick={(e) => e.preventDefault()}
        >
          Another action
        </a>
        <a
          href="#pablo"
          className={
            "block w-full py-2 px-4 whitespace-nowrap bg-transparent text-left text-body-sm font-medium text-ink-700 hover:bg-ink-50 hover:text-navy-800"
          }
          onClick={(e) => e.preventDefault()}
        >
          Something else here
        </a>
        <div className="h-0 my-1.5 border-t border-ink-200" />
        <a
          href="#pablo"
          className={
            "block w-full py-2 px-4 whitespace-nowrap bg-transparent text-left text-body-sm font-medium text-ink-700 hover:bg-ink-50 hover:text-navy-800"
          }
          onClick={(e) => e.preventDefault()}
        >
          Separated link
        </a>
      </div>
    </>
  );
};

export default NotificationDropdown;