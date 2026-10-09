import React from "react";

const ChargesSection = ({ formData }) => {
  return (
    <div className="mb-4 sm:mb-6">
      <h3 className="text-base sm:ds-section-title mb-3 sm:mb-4">
        Additional Charges
      </h3>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-ds-3 sm:gap-4 items-stretch">
        <div className="flex flex-col justify-between">
          <label className="block text-ink-600 text-sm font-medium mb-1 sm:mb-2 px-1 text-right">
            Fixed Charge
          </label>
          <input
            type="text"
            name="fixed_charge"
            value={parseFloat(formData.fixed_charge || 0).toFixed(2)}
            readOnly
            className="w-full px-1 py-2 border border-ink-300 rounded-md shadow-sm text-sm bg-ink-100 text-ink-600 text-right"
            placeholder="0.00"
          />
        </div>

        <div className="flex flex-col justify-between">
          <label className="block text-ink-600 text-sm font-medium mb-1 sm:mb-2 px-1 text-right">
            Monthly Charge
          </label>
          <input
            type="text"
            name="monthly_charge"
            value={parseFloat(formData.monthly_charge || 0).toFixed(2)}
            readOnly
            className="w-full px-1 py-2 border border-ink-300 rounded-md shadow-sm text-sm bg-ink-100 text-ink-600 text-right"
            placeholder="0.00"
          />
        </div>

        <div className="flex flex-col justify-between">
          <label className="block text-ink-600 text-sm font-medium mb-1 sm:mb-2 px-1 text-right">
            VAT (SSCL)
          </label>
          <input
            type="text"
            name="vat"
            value={parseFloat(formData.vat || 0).toFixed(2)}
            readOnly
            className="w-full px-1 py-2 border border-ink-300 rounded-md shadow-sm text-sm bg-ink-100 text-ink-600 text-right"
            placeholder="0.00"
          />
        </div>

        <div className="flex flex-col justify-between">
          <label className="block text-ink-600 text-sm font-medium mb-1 sm:mb-2 px-1 text-right">
            Total Amount
          </label>
          <input
            type="text"
            name="tot_amount"
            value={parseFloat(formData.tot_amount || 0).toFixed(2)}
            readOnly
            className="w-full px-1 py-2 border border-ink-300 rounded-md shadow-sm text-sm bg-ink-100 text-ink-600 font-medium text-right"
            placeholder="0.00"
          />
        </div>
      </div>
    </div>
  );
};

export default ChargesSection;