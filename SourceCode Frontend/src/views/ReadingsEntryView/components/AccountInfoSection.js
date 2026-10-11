import React from "react";

const AccountInfoSection = ({
  formData,
  accountLoaded,
  loading,
  showLoadButton,
  handleAccountNumberChange,
  loadAccountData
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div className="md:col-span-2">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Folio Number <span className="text-critical-500">*</span>
        </label>
        <div className="flex gap-2">
          <input
            type="text"
            name="account_number"
            value={formData.folio_no || formData.account_number}
            onChange={handleAccountNumberChange}
            className={`flex-1 px-3 py-2 border rounded-md shadow-sm text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 focus:border-navy-500 ${
              !accountLoaded && formData.account_number === ""
                ? "ring-2 ring-navy-500 border-navy-500"
                : "border-ink-300"
            }`}
            placeholder="Enter Folio Number"
            maxLength="12"
            disabled={!showLoadButton}
          />
          {showLoadButton && (
            <button
              onClick={loadAccountData}
              disabled={loading || !(formData.account_number || "").trim()}
              className="ds-btn ds-btn-primary whitespace-nowrap"
            >
              {loading && (
                <div className="ds-spinner ds-spinner-sm ds-spinner-invert mr-2"></div>
              )}
              Load
            </button>
          )}
        </div>
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Tariff
        </label>
        <input
          type="text"
          name="tariff"
          value={formData.tariff}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>
    </div>
  );
};

export default AccountInfoSection;