import React from 'react';

/**
 * StatusBadge Component
 * Displays a clickable badge showing Active/Inactive status
 *
 * @param {string} status - Current status: 'A' for Active, 'I' for Inactive
 * @param {function} onToggle - Callback function to toggle status
 */
export default function StatusBadge({ status, onToggle }) {
  const isActive = status === 'A';

  return (
    <button
      type="button"
      onClick={(e) => {
        e.stopPropagation();
        onToggle();
      }}
      className={`ds-badge cursor-pointer transition-colors duration-150 focus:outline-none ${
        isActive
          ? 'ds-badge-success hover:bg-success-100'
          : 'ds-badge-neutral hover:bg-ink-200'
      }`}
      title={`Click to ${isActive ? 'deactivate' : 'activate'}`}
      aria-label={`Status: ${isActive ? 'Active' : 'Inactive'}. Click to ${
        isActive ? 'deactivate' : 'activate'
      }.`}
    >
      <span
        className={`ds-dot ${isActive ? 'bg-success-600' : 'bg-ink-400'}`}
        aria-hidden="true"
      />
      {isActive ? 'Active' : 'Inactive'}
    </button>
  );
}
