import React from "react";
import { Link } from "react-router-dom";
import PropTypes from "prop-types";

const Breadcrumb = ({ items, onBackClick, hasUnsavedChanges }) => {
  const handleItemClick = (e, index, item) => {
    // Only check for unsaved changes when clicking on clickable items (not the last one)
    if (index < items.length - 1 && item.href && hasUnsavedChanges && onBackClick) {
      e.preventDefault();
      onBackClick(index);
    }
  };

  return (
    <nav className="mb-0" aria-label="Breadcrumb">
      <ol className="list-none p-0 inline-flex items-center flex-wrap gap-y-1">
        {items.map((item, index) => (
          <li key={index} className="flex items-center">
            {index > 0 && (
              <span className="mx-2 text-ink-300" aria-hidden="true">
                <i className="fas fa-chevron-right text-[10px]"></i>
              </span>
            )}
            {item.href ? (
              <Link
                to={item.href}
                onClick={(e) => handleItemClick(e, index, item)}
                className={`text-body-sm transition-colors duration-150 ${
                  index === items.length - 1
                    ? 'text-ink-500 font-semibold cursor-default'
                    : 'text-navy-700 hover:text-navy-900 font-medium cursor-pointer'
                }`}
              >
                {item.label}
              </Link>
            ) : (
              <span className="text-body-sm text-ink-500">{item.label}</span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
};

Breadcrumb.propTypes = {
  items: PropTypes.arrayOf(
    PropTypes.shape({
      label: PropTypes.string.isRequired,
      href: PropTypes.string,
    })
  ).isRequired,
  onBackClick: PropTypes.func,
  hasUnsavedChanges: PropTypes.bool,
};

export default Breadcrumb;