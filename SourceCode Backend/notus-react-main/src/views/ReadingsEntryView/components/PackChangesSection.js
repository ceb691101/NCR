import React from "react";

const PackChangesSection = ({
  showPackChanges,
  formData,
  packChangesData,
  packChangesEdited,
  savePackChangesActive,
  accountLoaded,
  handlePackChangesInput,
  handleSavePackChanges
}) => {
  // Only show when toggled on
  if (!showPackChanges) return null;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6 p-4 bg-navy-50 rounded-lg border-l-4 border-navy-500">
      <div className="md:col-span-2 lg:col-span-1">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          New Reader Code
        </label>
        <input
          type="text"
          name="new_reader_code"
          value={packChangesData.new_reader_code}
          onChange={handlePackChangesInput}
          className={`ds-input shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-navy-500 ${
            packChangesEdited && packChangesData.new_reader_code
              ? "border-critical-500 bg-critical-50 text-critical-700"
              : ""
          }`}
          placeholder="Enter new reader code"
        />
      </div>

      <div className="md:col-span-2 lg:col-span-1">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          New Daily Pack
        </label>
        <input
          type="text"
          name="new_daily_pack"
          value={packChangesData.new_daily_pack}
          onChange={handlePackChangesInput}
          className={`ds-input shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-navy-500 ${
            packChangesEdited && packChangesData.new_daily_pack
              ? "border-critical-500 bg-critical-50 text-critical-700"
              : ""
          }`}
          placeholder="Enter new daily pack"
        />
      </div>

      <div className="md:col-span-2 lg:col-span-1">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          New Walk Order
        </label>
        <input
          type="text"
          name="new_walk_order"
          value={packChangesData.new_walk_order}
          onChange={handlePackChangesInput}
          className={`ds-input shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-navy-500 ${
            packChangesEdited && packChangesData.new_walk_order
              ? "border-critical-500 bg-critical-50 text-critical-700"
              : ""
          }`}
          placeholder="Enter new walk order"
        />
      </div>

      {/* Save Pack Changes Button */}
      <div className="md:col-span-2 lg:col-span-3 flex justify-end mt-2">
        <button
          onClick={handleSavePackChanges}
          disabled={!savePackChangesActive}
          className={`ds-btn ds-btn-primary ${
            savePackChangesActive
              ? "bg-success-500 hover:bg-success-600 focus:ring-success-500"
              : "bg-success-500 cursor-not-allowed"
          }`}
        >
          <i className="fas fa-save mr-2"></i>
          Save Pack Changes
        </button>
      </div>
    </div>
  );
};

export default PackChangesSection;