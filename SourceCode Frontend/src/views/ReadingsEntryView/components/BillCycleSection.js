import React from "react";

const BillCycleSection = ({ formData, isReceivedReading }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          {isReceivedReading ? "Received Bill Month" : "Current Bill Month"}
        </label>
        <input
          type="text"
          name="current_billcycle_number"
          value={formData.global_billcycle_number}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Bill Month Date
        </label>
        <input
          type="text"
          name="current_billcycle_date"
          value={formData.current_billcycle_date}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Area Code
        </label>
        <input
          type="text"
          name="area_code_number"
          value={formData.area_code_number}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Area Name
        </label>
        <input
          type="text"
          name="area_code_name"
          value={formData.area_code_name}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>
    </div>
  );
};

export default BillCycleSection;