import React from "react";

import UserDropdown from "components/Dropdowns/UserDropdown.js";

export default function Navbar() {
  return (
    <div>
      {/* Navbar */}
        <div className="w-full mx-autp flex md:flex-nowrap flex-wrap">
          {/* User */}
          <ul className="flex-col md:flex-row list-none hidden md:flex mr-2">
            <UserDropdown />
          </ul>
          {/* Form */}
          <form className="md:flex hidden flex-row flex-wrap items-center lg:ml-auto ml-3">
            <div className="relative flex w-full flex-wrap items-stretch">
              <span className="z-10 h-full leading-snug font-normal absolute text-center text-ink-300 absolute bg-transparent rounded text-base items-center justify-center w-8 pl-3 py-3">
                <i className="fas fa-search"></i>
              </span>
              <input
                type="text"
                placeholder="Search here..."
                className="border-0 px-3 py-3 placeholder-ink-300 text-ink-600 relative bg-white bg-white rounded text-sm shadow outline-none focus:outline-none focus:ring w-full pl-10"
              />
            </div>
          </form>
        </div>
      {/* End Navbar */}
    </div>
  );
}
