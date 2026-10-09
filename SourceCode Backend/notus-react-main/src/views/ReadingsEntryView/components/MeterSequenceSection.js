import React from "react";

const MeterSequenceSection = ({ formData }) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Meter Sequence
        </label>
        <input
          type="text"
          name="meter_sequence"
          value={formData.meter_sequence}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          B/F Balance
        </label>
        <input
          type="text"
          name="b_f_balance"
          value={formData.b_f_balance}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>
    </div>
  );
};

export default MeterSequenceSection;