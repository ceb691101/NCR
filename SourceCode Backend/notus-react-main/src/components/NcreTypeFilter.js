// src/components/NcreTypeFilter.js
import React, { useState, useEffect } from "react";
import { getNcreTypes } from "services/developerRegistrationService";

const NcreTypeFilter = ({ value, onChange, className }) => {
  const [ncreTypes, setNcreTypes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const fetchNcreTypes = async () => {
      try {
        const types = await getNcreTypes();
        if (isMounted) {
          setNcreTypes(types || []);
          setLoading(false);
        }
      } catch (error) {
        console.error("Failed to load NCRE types:", error);
        if (isMounted) {
          setNcreTypes([]);
          setLoading(false);
        }
      }
    };
    fetchNcreTypes();
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <select
      value={value}
      onChange={e => onChange(e.target.value)}
      className={className || "ds-select h-10 w-full sm:w-48"}
    >
      <option value="">{loading ? "Loading..." : "All NCRE Types"}</option>
      {ncreTypes.map(type => (
        <option key={type.typeId} value={type.typeId}>{type.typeName}</option>
      ))}
    </select>
  );
};

export default NcreTypeFilter;
