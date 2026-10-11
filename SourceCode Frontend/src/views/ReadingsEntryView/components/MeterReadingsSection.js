// src/views/ReadingsEntryView/components/MeterReadingsSection.js
import React from "react";

const MeterReadingsSection = ({
  formData,
  accountLoaded,
  editedFields,
  handleInputChange,
  getFieldStyle,
  customerMeterTypes = [],
  isReceivedReading = false
}) => {
  // Meter type mapping
  const meterTypeMapping = {
    'KWD': { label: 'KWH (Day)', prefix: 'kwh_day' },
    'KWP': { label: 'KWH (Peak)', prefix: 'kwh_peak' },
    'KWO': { label: 'KWH (Off Peak)', prefix: 'kwh_offp' },
    'KWT': { label: 'KWH (Total Export)', prefix: 'kwh_tot' }
  };

  // Get only the meter types that belong to this customer
  const getCustomerMeterTypes = () => {
    const customerTypes = customerMeterTypes.map(meter => meter.meter_type);
    
    // Return all available meter types but only show those that belong to the customer
    return Object.entries(meterTypeMapping).map(([type, config]) => ({
      type,
      ...config,
      belongsToCustomer: customerTypes.includes(type)
    }));
  };

  const customerMeterConfigs = getCustomerMeterTypes();

  // Helper function to check if a field has an error
  const hasFieldError = (fieldName) => {
    if (!formData.error_fields || !Array.isArray(formData.error_fields)) {
      return false;
    }
    return formData.error_fields.includes(fieldName);
  };

  return (
    <div className="mb-6">
      {/* Header Section */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between mb-4 gap-3">
        <div className="flex items-center">
          <h3 className="ds-section-title">
            Meter Readings {customerMeterTypes.length > 0 && `(${customerMeterTypes.length} readings)`}
          </h3>
        </div>
      </div>

      {/* Desktop View - Original Table (≥1024px) */}
      <div className="hidden lg:block overflow-x-auto">
        <table className="w-full border-collapse border border-ink-300 border-b-0 border-r-0">
          <thead className="bg-ink-50">
            <tr>
              <th className="border border-ink-300 px-3 py-2 text-right text-xs font-medium text-ink-500 uppercase w-32">
                Type
              </th>
              <th className="border border-ink-300 px-3 py-2 text-right text-xs font-medium text-ink-500 uppercase w-40">
                Meter Number
              </th>
              <th className="border border-ink-300 px-3 py-2 text-right text-xs font-medium text-ink-500 uppercase w-52">
                Present Reading
              </th>
              <th className="border border-ink-300 px-3 py-2 text-right text-xs font-medium text-ink-500 uppercase w-44">
                Previous Reading
              </th>
              <th className="border border-ink-300 px-3 py-2 text-right text-xs font-medium text-ink-500 uppercase w-44">
                Energy Sent to Grid
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-ink-200">
            {customerMeterConfigs.map((meterConfig) => (
              meterConfig.belongsToCustomer ? (
                <MeterReadingRow
                  key={meterConfig.type}
                  type={meterConfig.label}
                  formData={formData}
                  accountLoaded={accountLoaded}
                  editedFields={editedFields}
                  handleInputChange={handleInputChange}
                  getFieldStyle={getFieldStyle}
                  prefix={meterConfig.prefix}
                  isReceivedReading={isReceivedReading}
                  hasFieldError={hasFieldError}
                />
              ) : null
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile & Tablet View - Card Layout (<1024px) */}
      <div className="lg:hidden space-y-4">
        {customerMeterConfigs.map((meterConfig) => (
          meterConfig.belongsToCustomer ? (
            <MeterReadingCard
              key={meterConfig.type}
              title={meterConfig.label}
              formData={formData}
              accountLoaded={accountLoaded}
              editedFields={editedFields}
              handleInputChange={handleInputChange}
              getFieldStyle={getFieldStyle}
              prefix={meterConfig.prefix}
              isReceivedReading={isReceivedReading}
              hasFieldError={hasFieldError}
            />
          ) : null
        ))}
      </div>

      {/* Show message if no meters found */}
      {customerMeterTypes.length === 0 && accountLoaded && (
        <div className="text-center py-4 text-ink-500">
          No meter types found for this customer.
        </div>
      )}

    </div>
  );
};

// Sub-component for desktop table rows
const MeterReadingRow = ({
  type,
  formData,
  accountLoaded,
  editedFields,
  handleInputChange,
  getFieldStyle,
  prefix,
  isReceivedReading = false,
  hasFieldError
}) => {
  const isPresentReadEditable = accountLoaded;
  const hasError = hasFieldError(`${prefix}_presentread`);
  
  return (
    <tr className={hasError ? "bg-critical-50" : ""}>
      <td className="border border-ink-300 px-2 py-2 text-sm font-medium text-right">
        <div className="flex items-center justify-end">
          {type}
        </div>
      </td>
      <td className="border border-ink-300 px-1 py-2">
        <input
          type="text"
          name={`${prefix}_meternum`}
          value={formData[`${prefix}_meternum`] || ""}
          readOnly
          className={`w-full px-1 py-1 border-0 text-sm text-right focus:outline-none focus:ring-0 ${
            hasError ? "bg-critical-50 text-critical-700" : "bg-ink-100 text-ink-600"
          }`}
        />
      </td>
      <td className="border border-ink-300 px-1 py-2">
        <input
          type="number"
          name={`${prefix}_presentread`}
          value={formData[`${prefix}_presentread`] || ""}
          onChange={handleInputChange}
          disabled={!isPresentReadEditable}
          className={`w-full px-1 py-1 border-0 text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 disabled:bg-ink-100 disabled:text-ink-500 text-right ${
            hasError 
              ? "bg-critical-100 text-critical-700 border-critical-500 font-medium"
              : getFieldStyle(`${prefix}_presentread`)
          } appearance-none [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none`}
          placeholder="0"
          onWheel={(e) => e.target.blur()}
          onFocus={(e) => e.target.placeholder = ""}
          onBlur={(e) => e.target.placeholder = "0"}
        />
      </td>
      <td className="border border-ink-300 px-1 py-2">
        <input
          type="text"
          name={`${prefix}_previousread`}
          value={formData[`${prefix}_previousread`] || ""}
          readOnly
          className={`w-full px-1 py-1 border-0 text-sm text-right focus:outline-none focus:ring-0 ${
            hasError ? "bg-critical-50 text-critical-600" : "bg-ink-100 text-ink-600"
          }`}
        />
      </td>
      <td className="border border-ink-300 px-1 py-2">
        <input
          type="text"
          name={`${prefix}_units`}
          value={formData[`${prefix}_units`] || ""}
          readOnly
          className={`w-full px-1 py-1 border-0 text-sm text-right focus:outline-none focus:ring-0 ${
            hasError ? "bg-critical-50 text-critical-600" : "bg-ink-100 text-ink-600"
          }`}
        />
      </td>
    </tr>
  );
};

// Sub-component for mobile cards
const MeterReadingCard = ({
  title,
  formData,
  accountLoaded,
  editedFields,
  handleInputChange,
  getFieldStyle,
  prefix,
  isReceivedReading = false,
  hasFieldError
}) => {
  const isPresentReadEditable = accountLoaded;
  const hasError = hasFieldError(`${prefix}_presentread`);

  return (
    <div className={`bg-white border rounded-lg p-4 ${hasError ? 'border-critical-500 bg-critical-50' : 'border-ink-300'}`}>
      <h4 className="font-semibold text-ink-700 mb-3 flex items-center justify-between">
        <span>{title}</span>
      </h4>
      <div className="grid grid-cols-2 gap-ds-3 text-sm">
        <div>
          <label className="text-xs text-ink-500 font-medium">Meter Number</label>
          <input
            type="text"
            name={`${prefix}_meternum`}
            value={formData[`${prefix}_meternum`] || ""}
            readOnly
            className={`w-full px-2 py-1 border border-ink-300 rounded text-sm text-right ${
              hasError ? "bg-critical-50 text-critical-700" : "bg-ink-100 text-ink-600"
            }`}
          />
        </div>
        <div>
          <label className="text-xs text-ink-500 font-medium">
            Present Reading {isReceivedReading && <span className="text-navy-600">*</span>}
          </label>
          <input
            type="number"
            name={`${prefix}_presentread`}
            value={formData[`${prefix}_presentread`] || ""}
            onChange={handleInputChange}
            disabled={!isPresentReadEditable}
            className={`w-full px-2 py-1 border border-ink-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-navy-500 disabled:bg-ink-100 disabled:text-ink-500 text-right ${
              hasError 
                ? "bg-critical-100 text-critical-700 border-critical-500 font-medium"
                : getFieldStyle(`${prefix}_presentread`)
            } appearance-none [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none`}
            placeholder="0"
            onWheel={(e) => e.target.blur()}
            onFocus={(e) => e.target.placeholder = ""}
            onBlur={(e) => e.target.placeholder = "0"}
          />
        </div>
        <div>
          <label className="text-xs text-ink-500 font-medium">Previous Reading</label>
          <input
            type="text"
            name={`${prefix}_previousread`}
            value={formData[`${prefix}_previousread`] || ""}
            readOnly
            className={`w-full px-2 py-1 border border-ink-300 rounded text-sm text-right ${
              hasError ? "bg-critical-50 text-critical-600" : "bg-ink-100 text-ink-600"
            }`}
          />
        </div>
        <div>
          <label className="text-xs text-ink-500 font-medium">Energy Sent to Grid</label>
          <input
            type="text"
            name={`${prefix}_units`}
            value={formData[`${prefix}_units`] || ""}
            readOnly
            className={`w-full px-2 py-1 border border-ink-300 rounded text-sm text-right ${
              hasError ? "bg-critical-50 text-critical-600 bg-ink-100" : "bg-ink-100 text-ink-600"
            }`}
          />
        </div>
      </div>
    </div>
  );
};

export default MeterReadingsSection;