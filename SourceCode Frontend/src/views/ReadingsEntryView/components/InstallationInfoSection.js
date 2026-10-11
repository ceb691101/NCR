// src/views/ReadingsEntryView/components/InstallationInfoSection.js
import React from "react";
import CalendarComponent from "./CalendarComponent";

const InstallationInfoSection = ({
  formData,
  accountLoaded,
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
  isReceivedReading = false
}) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-6">
      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Installation ID
        </label>
        <input
          type="text"
          name="installation_id"
          value={formData.installation_id}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Customer Category
        </label>
        <input
          type="text"
          name="customer_category"
          value={formData.customer_category}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="sm:col-span-1 md:col-span-1 lg:col-span-1 flex flex-col relative">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Reading Date <span className="text-critical-500">*</span>
        </label>
        <div className="relative mt-auto">
          <input
            ref={readingDateInputRef}
            type="text"
            name="reading_date"
            value={formData.reading_date}
            readOnly={!accountLoaded || !isReceivedReading}
            className={`w-full px-3 py-2 pr-10 border border-ink-300 rounded-md shadow-sm text-sm cursor-pointer ${getFieldStyle(
              "reading_date"
            )}`}
            onClick={() => setShowCalendar(!showCalendar)}
          />
          <div
            className="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer"
            onClick={() => setShowCalendar(!showCalendar)}
          >
            <svg
              className="h-5 w-5 text-ink-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>

          {showCalendar && (
            <div className="absolute top-full right-0 mt-1 z-20">
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
                isReceivedReading={isReceivedReading} // Pass this prop
              />
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          Previous Reading Date
        </label>
        <input
          type="text"
          name="previous_reading_date"
          value={formData.previous_reading_date}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>

      <div className="flex flex-col">
        <label className="block text-ink-600 text-sm font-medium mb-2">
          No of Days
        </label>
        <input
          type="text"
          name="no_of_days"
          value={formData.no_of_days}
          readOnly
          className="ds-input shadow-sm text-sm bg-ink-100 text-ink-600 mt-auto"
        />
      </div>
    </div>
  );
};

export default InstallationInfoSection;