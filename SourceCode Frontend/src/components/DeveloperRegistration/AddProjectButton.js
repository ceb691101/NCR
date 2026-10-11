import React from 'react';

export default function AddProjectButton({ onClick, disabled = false }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="w-full rounded-md bg-ink-700 px-4 py-3 text-sm font-medium text-white transition-colors duration-150 hover:bg-ink-800 disabled:cursor-not-allowed disabled:opacity-60"
    >
      Add Project
    </button>
  );
}