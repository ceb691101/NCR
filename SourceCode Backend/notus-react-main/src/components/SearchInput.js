import React from "react";
import PropTypes from "prop-types";

const SearchInput = ({
  value = "",
  onChange = () => {},
  onClear = () => {},
  placeholder = "Search...",
  className = "",
}) => {
  return (
    <div className={`relative flex-1 max-w-md ${className}`}>
      <i className="fas fa-search absolute left-3.5 top-1/2 transform -translate-y-1/2 text-ink-400 text-xs"></i>
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className="w-full pl-9 pr-9 py-2 text-xs font-medium text-ink-700 bg-white border border-ink-200 rounded-xl focus:outline-none focus:border-navy-500 focus:ring-1 focus:ring-navy-500 transition-all placeholder:text-ink-400 shadow-xs"
      />
      {value && (
        <button
          type="button"
          onClick={onClear}
          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-ink-400 hover:text-ink-600 p-1 focus:outline-none transition-colors"
          title="Clear search"
        >
          <i className="fas fa-times text-xs"></i>
        </button>
      )}
    </div>
  );
};

SearchInput.propTypes = {
  value: PropTypes.string,
  onChange: PropTypes.func.isRequired,
  onClear: PropTypes.func,
  placeholder: PropTypes.string,
  className: PropTypes.string,
};

export default SearchInput;
