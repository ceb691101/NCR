// src/components/AreaCodeFilter.js
import React from "react";

const AreaCodeFilter = ({ value, onChange, className }) => (
  <input
    type="text"
    placeholder="Filter by Area Code"
    value={value}
    onChange={e => onChange(e.target.value)}
    className={className || "ds-select h-10 w-full sm:w-48"}
  />
);

export default AreaCodeFilter;
