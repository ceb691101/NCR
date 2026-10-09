import React from "react";

const ReaderInfoSection = ({ 
  formData, 
  accountLoaded, 
  showPackChanges, 
  handlePackChangesToggle 
}) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
      <div className="sm:col-span-1 flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Reader Code
        </label>
        <input
          type="text"
          name="reader_code"
          value={formData.reader_code}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="sm:col-span-1 flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Daily Pack
        </label>
        <input
          type="text"
          name="daily_pack"
          value={formData.daily_pack}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="sm:col-span-1 md:col-span-1 lg:col-span-1 flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Walk Order
        </label>
        <input
          type="text"
          name="walk_order"
          value={formData.walk_order}
          readOnly
          className="w-full px-2 py-2 border border-ink-300 rounded-md shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      {/* Pack Changes Checkbox - Now in ReaderInfoSection */}
      <div className="sm:col-span-1 md:col-span-3 lg:col-span-1 flex flex-col justify-end">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Pack Changes
          <input
            type="checkbox"
            checked={showPackChanges}
            onChange={handlePackChangesToggle}
            className="ml-2 rounded focus:outline-none focus:ring-0 focus:ring-offset-0"
            disabled={!accountLoaded}
          />
        </label>
      </div>
    </div>
  );
};

export default ReaderInfoSection;