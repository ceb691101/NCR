// src/views/ReadingsEntryView/components/HeaderSection.js
import React from "react";

const HeaderSection = ({ headerConfig, formData, loading, handleRefresh, isReceivedReading }) => {
  return (
    <div className={`rounded-t-lg shadow-lg ${headerConfig.bgColor}`}>
      <div className="px-6 py-4 border-b border-ink-200">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="ds-section-title">
              {headerConfig.title || "Monthly Readings Entry"}
            </h2>
            {headerConfig.showStatus && (
              <div className="flex items-center mt-1">
                <p className="text-sm text-ink-600">
                  Reading Status: 
                </p>
                <span className={`ml-2 px-2 py-1 rounded text-xs font-medium ${
                  isReceivedReading || formData.reading_status === "RECEIVED" 
                    ? "bg-success-100 text-success-800" 
                    : "bg-warning-100 text-warning-800"
                }`}>
                  {isReceivedReading ? "RECEIVED" : formData.reading_status}
                </span>
              </div>
            )}
          </div>
          {headerConfig.showRefresh && (
            <button
              onClick={handleRefresh}
              className="mt-2 sm:mt-0 bg-navy-600 hover:bg-navy-700 text-white px-3 py-1 rounded text-sm transition-colors duration-200 focus:outline-none flex items-center"
              title="Refresh data"
              disabled={loading}
            >
              {loading ? (
                <div className="ds-spinner ds-spinner-sm ds-spinner-invert mr-2"></div>
              ) : (
                <i className="fas fa-sync-alt mr-2"></i>
              )}
              Refresh
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default HeaderSection;