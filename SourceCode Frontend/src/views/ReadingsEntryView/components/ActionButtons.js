// FILE: src\views\ReadingsEntryView\components\ActionButtons.js
import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";

const ActionButtons = ({
  saveReadingsActive = false,
  saveUpdatesActive = false,
  resetActive = false,
  handleSaveReadings = () => {},
  handleSaveUpdates = () => {},
  handleResetEdits = () => {},
  isReceivedReading = false,
  editedFields = {},
  formData = {},
  originalData = {}
}) => {
  const [showUpdateSummary, setShowUpdateSummary] = useState(false);
  const [updateSummary, setUpdateSummary] = useState({
    readingDate: false,
    meterUpdates: [],
    chargeUpdates: [],
    totalCount: 0
  });

  // Calculate update summary when editedFields changes
  useEffect(() => {
    if (isReceivedReading && saveUpdatesActive) {
      const summary = calculateUpdateSummary();
      setUpdateSummary(summary);
    }
  }, [editedFields, isReceivedReading, saveUpdatesActive, formData, originalData]);

  const calculateUpdateSummary = () => {
    const summary = {
      readingDate: false,
      meterUpdates: [],
      chargeUpdates: [],
      totalCount: 0
    };

    // Check reading date update
    if (editedFields.reading_date !== undefined) {
      const oldValue = originalData.reading_date || 'N/A';
      const newValue = editedFields.reading_date;
      summary.readingDate = {
        label: 'Reading Date',
        oldValue,
        newValue,
        field: 'reading_date'
      };
      summary.totalCount++;
    }

    // Check which meters have present reading updates
    const meterFields = [
      { prefix: 'kwh_offp', label: 'KWH (Off Peak)', field: 'presentread' },
      { prefix: 'kwh_day', label: 'KWH (Day)', field: 'presentread' },
      { prefix: 'kwh_peak', label: 'KWH (Peak)', field: 'presentread' },
      { prefix: 'kva', label: 'KVA', field: 'presentread' },
      { prefix: 'kvah', label: 'KVAH / KVArh', field: 'presentread' }
    ];

    meterFields.forEach(meter => {
      const fieldName = `${meter.prefix}_${meter.field}`;
      if (editedFields[fieldName] !== undefined) {
        const oldValue = originalData[fieldName] || '0';
        const newValue = editedFields[fieldName] || formData[fieldName] || '0';
        
        // Also check for units update
        const unitsFieldName = `${meter.prefix}_units`;
        let unitsUpdate = null;
        if (editedFields[unitsFieldName] !== undefined) {
          const oldUnits = originalData[unitsFieldName] || '0';
          const newUnits = editedFields[unitsFieldName] || formData[unitsFieldName] || '0';
          unitsUpdate = { oldUnits, newUnits };
        }

        // Check for amount update
        const amountFieldName = `${meter.prefix}_amount`;
        let amountUpdate = null;
        if (editedFields[amountFieldName] !== undefined) {
          const oldAmount = originalData[amountFieldName] ? parseFloat(originalData[amountFieldName]).toFixed(2) : '0.00';
          const newAmount = editedFields[amountFieldName] ? parseFloat(editedFields[amountFieldName]).toFixed(2) : '0.00';
          amountUpdate = { oldAmount, newAmount };
        }

        summary.meterUpdates.push({
          label: meter.label,
          oldValue,
          newValue,
          field: fieldName,
          hasUnitsUpdate: !!unitsUpdate,
          hasAmountUpdate: !!amountUpdate,
          unitsUpdate,
          amountUpdate
        });
        summary.totalCount++;
      }
    });

    // Check which charges have updates
    const chargeFields = [
      { field: 'fixed_charge', label: 'Fixed Charge' },
      { field: 'monthly_charge', label: 'Monthly Charge' },
      { field: 'vat', label: 'VAT (SSCL)' },
      { field: 'tot_amount', label: 'Total Amount' }
    ];

    chargeFields.forEach(charge => {
      if (editedFields[charge.field] !== undefined) {
        const oldValue = originalData[charge.field] ? 
          parseFloat(originalData[charge.field]).toFixed(2) : '0.00';
        const newValue = editedFields[charge.field] ? 
          parseFloat(editedFields[charge.field]).toFixed(2) : '0.00';
        
        summary.chargeUpdates.push({
          label: charge.label,
          oldValue,
          newValue,
          field: charge.field
        });
        summary.totalCount++;
      }
    });

    return summary;
  };

  // Format value for display
  const formatValue = (value, isCurrency = false) => {
    if (value === undefined || value === null || value === '') return 'N/A';
    if (isCurrency) {
      return `Rs. ${parseFloat(value).toFixed(2)}`;
    }
    return value.toString();
  };

  // Handle save with confirmation
  const handleSaveWithConfirmation = () => {
    if (!saveUpdatesActive) return;
    
    // Show summary tooltip briefly, then save
    setShowUpdateSummary(true);
    setTimeout(() => {
      handleSaveUpdates();
      setShowUpdateSummary(false);
    }, 800);
  };

  // Calculate badge color based on update count
  const getBadgeColor = (count) => {
    if (count === 0) return "bg-ink-100 text-ink-800";
    if (count <= 2) return "bg-success-100 text-success-800";
    if (count <= 5) return "bg-warning-100 text-warning-800";
    return "bg-critical-100 text-critical-800";
  };

  // Get update icon based on type
  const getUpdateIcon = (type) => {
    switch(type) {
      case 'readingDate':
        return <i className="fas fa-calendar-alt mr-1 text-navy-500"></i>;
      case 'meter':
        return <i className="fas fa-tachometer-alt mr-1 text-success-500"></i>;
      case 'charge':
        return <i className="fas fa-money-bill-wave mr-1 text-navy-500"></i>;
      default:
        return <i className="fas fa-edit mr-1 text-ink-500"></i>;
    }
  };

  return (
    <div className="flex flex-col sm:flex-row flex-wrap gap-2 sm:gap-3 justify-end pt-4 sm:pt-6 border-t border-ink-200">
      {/* For Received Readings (Editing) - Show Save Edits button */}
      {isReceivedReading && (
        <div className="relative">
          <button
            onClick={handleSaveWithConfirmation}
            onMouseEnter={() => setShowUpdateSummary(true)}
            onMouseLeave={() => setShowUpdateSummary(false)}
            disabled={!saveUpdatesActive}
            className={`ds-btn ds-btn-primary ${
              saveUpdatesActive
                ? "bg-navy-500 hover:bg-navy-600 focus:ring-2 focus:ring-navy-500/20"
                : "bg-navy-300 cursor-not-allowed"
            }`}
          >
            <i className="fas fa-save mr-2 text-white/80"></i>
            <span>Save Edits</span>
          </button>
        </div>
      )}

      {/* For Pending Readings (Insert New Readings) - Show Save Readings button */}
      {!isReceivedReading && (
        <button
          onClick={handleSaveReadings}
          disabled={!saveReadingsActive}
          className={`ds-btn ds-btn-primary ${
            saveReadingsActive
              ? "bg-success-500 hover:bg-success-600 focus:ring-2 focus:ring-success-500/20"
              : "bg-success-500 cursor-not-allowed"
          }`}
        >
          <i className="fas fa-save mr-2 text-white/80"></i>
          <span>Save Readings</span>
        </button>
      )}

      {/* Reset button - Show for both scenarios */}
      <button
        onClick={handleResetEdits}
        disabled={!resetActive}
        className={`ds-btn ds-btn-primary ${
          resetActive
            ? "bg-warning-500 hover:bg-warning-600 focus:ring-2 focus:ring-warning-500/20"
            : "bg-warning-500 cursor-not-allowed"
        }`}
      >
        <i className="fas fa-undo mr-2 text-white/80"></i>
        <span>Reset</span>
      </button>
    </div>
  );
};

// Add propTypes for better development experience
ActionButtons.propTypes = {
  saveReadingsActive: PropTypes.bool,
  saveUpdatesActive: PropTypes.bool,
  resetActive: PropTypes.bool,
  handleSaveReadings: PropTypes.func,
  handleSaveUpdates: PropTypes.func,
  handleResetEdits: PropTypes.func,
  isReceivedReading: PropTypes.bool,
  editedFields: PropTypes.object,
  formData: PropTypes.object,
  originalData: PropTypes.object
};



export default ActionButtons;