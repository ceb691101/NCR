import React from "react";
import CalendarComponent from "./CalendarComponent";

const ReadOnlyField = ({ label, value }) => (
  <div className="flex min-w-0 flex-col">
    <label className="ds-label-field">{label}</label>
    <input
      type="text"
      value={value || ""}
      readOnly
      className="ds-input mt-auto h-10 bg-ink-100 text-body-sm text-ink-600"
    />
  </div>
);

const NcreAccountDetailsSection = ({
  formData,
  accountLoaded,
  loading,
  showCalendar,
  selectedDate,
  currentMonth,
  editedFields,
  setShowCalendar,
  setCurrentMonth,
  setSelectedDate,
  handleDateSelect,
  handleTodayClick,
  navigateMonth,
  getFieldStyle,
  getDaysInMonth,
  readingDateInputRef,
  handleFolioNumberChange,
  loadAccountData,
  showLoadButton,
}) => (
  <section className="mb-6 rounded-xl border border-ink-200 bg-ink-50 p-4 sm:p-5" aria-labelledby="account-cycle-details-title">
    <h3 id="account-cycle-details-title" className="ds-section-title mb-4 flex items-center gap-2">
      <i className="fas fa-info-circle text-navy-600" aria-hidden="true"></i>
      Account &amp; Cycle Details
    </h3>

    <div className="grid grid-cols-1 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <div className="flex min-w-0 flex-col md:col-span-2 lg:col-span-2">
        <label htmlFor="reading-folio-number" className="ds-label-field">
          Folio Number <span className="text-critical-600">*</span>
        </label>
        <div className="mt-auto flex gap-2">
          <input
            id="reading-folio-number"
            type="text"
            name="folio_no"
            value={formData.folio_no || ""}
            onChange={handleFolioNumberChange}
            className={`ds-input h-10 min-w-0 flex-1 ${
              !accountLoaded && !String(formData.folio_no || "").trim()
                ? "border-navy-500 ring-2 ring-navy-500"
                : ""
            }`}
            placeholder="Enter Folio Number"
            maxLength="6"
            disabled={accountLoaded && !showLoadButton}
          />
          {showLoadButton && (
            <button
              type="button"
              onClick={loadAccountData}
              disabled={loading || !String(formData.folio_no ?? "").trim()}
              className="ds-btn ds-btn-primary h-10 whitespace-nowrap"
            >
              {loading && (
                <span className="ds-spinner ds-spinner-sm ds-spinner-invert" aria-hidden="true"></span>
              )}
              Load
            </button>
          )}
        </div>
      </div>

      <ReadOnlyField label="Developer Name" value={formData.developer_name} />
      <ReadOnlyField label="Facility Name" value={formData.facility_name} />
      <ReadOnlyField label="Customer Category" value={formData.customer_category} />
      <ReadOnlyField label="Tariff Type" value={formData.tariff_type} />
      <ReadOnlyField label="Tariff Rate" value={formData.tariff_value} />
      <ReadOnlyField label="Meter Number" value={formData.meter_number} />
      <ReadOnlyField label="Area Code" value={formData.area_code_number} />
      <ReadOnlyField label="Area Name (City)" value={formData.area_code_name} />
      <ReadOnlyField label="Bill Month" value={formData.bill_month} />
      <ReadOnlyField label="Current Bill Cycle" value={formData.current_billcycle_number} />
      <ReadOnlyField label="Bill Cycle Date" value={formData.current_billcycle_date} />
      <ReadOnlyField label="Previous Reading Date" value={formData.previous_reading_date} />

      <div className="relative flex min-w-0 flex-col">
        <label htmlFor="reading-date" className="ds-label-field">
          Reading Date <span className="text-critical-600">*</span>
        </label>
        <div className="relative mt-auto">
          <input
            ref={readingDateInputRef}
            id="reading-date"
            type="text"
            name="reading_date"
            value={formData.reading_date || ""}
            readOnly
            aria-haspopup="dialog"
            aria-expanded={showCalendar}
            className={`ds-input h-10 cursor-pointer pr-10 ${getFieldStyle("reading_date")}`}
            onClick={() => accountLoaded && setShowCalendar(!showCalendar)}
          />
          <button
            type="button"
            className="absolute inset-y-0 right-0 flex items-center px-3 text-ink-400 hover:text-navy-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20"
            aria-label="Choose reading date"
            onClick={() => accountLoaded && setShowCalendar(!showCalendar)}
          >
            <i className="fas fa-calendar-alt" aria-hidden="true"></i>
          </button>
          {showCalendar && (
            <div className="absolute right-0 top-full z-20 mt-1">
              <CalendarComponent
                currentMonth={currentMonth}
                selectedDate={selectedDate}
                showCalendar={showCalendar}
                setShowCalendar={setShowCalendar}
                setCurrentMonth={setCurrentMonth}
                handleDateSelect={handleDateSelect}
                handleTodayClick={handleTodayClick}
                navigateMonth={navigateMonth}
                getDaysInMonth={getDaysInMonth}
                isReceivedReading={true}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  </section>
);

export default NcreAccountDetailsSection;
