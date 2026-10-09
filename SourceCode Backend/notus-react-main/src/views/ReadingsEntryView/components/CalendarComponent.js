// src/views/ReadingsEntryView/components/CalendarComponent.js
import React from "react";

const CalendarComponent = ({
  currentMonth,
  selectedDate,
  showCalendar,
  setShowCalendar,
  setCurrentMonth,
  handleDateSelect,
  handleTodayClick,
  navigateMonth,
  getDaysInMonth,
  isReceivedReading = false
}) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return (
    <div className="bg-white border border-ink-300 rounded-lg shadow-lg z-20 w-64 sm:w-72 md:w-80 lg:w-72 xl:w-80">
      <div className="p-2 sm:p-3 md:p-4">
        {/* Calendar Header */}
        <div className="flex items-center justify-between mb-3 sm:mb-4">
          <button
            type="button"
            onClick={() => navigateMonth(-1)}
            className="p-1 sm:p-2 hover:bg-ink-100 rounded transition-colors flex-shrink-0"
          >
            <svg
              className="h-4 w-4 sm:h-5 sm:w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>

          <h3 className="text-sm sm:text-base font-medium text-center flex-1 mx-2 whitespace-nowrap overflow-hidden text-ellipsis">
            {currentMonth.toLocaleDateString("en-US", {
              month: "short",
              year: "numeric",
            })}
          </h3>

          <button
            type="button"
            onClick={() => navigateMonth(1)}
            className="p-1 sm:p-2 hover:bg-ink-100 rounded transition-colors flex-shrink-0"
          >
            <svg
              className="h-4 w-4 sm:h-5 sm:w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>

          <button
            type="button"
            onClick={() => setShowCalendar(false)}
            className="p-1 sm:p-2 hover:bg-ink-100 rounded ml-1 sm:ml-2 transition-colors flex-shrink-0"
          >
            <svg
              className="h-3 w-3 sm:h-4 sm:w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Calendar Grid */}
        <div className="grid grid-cols-7 gap-0.5 mb-2">
          {["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"].map((day) => (
            <div
              key={day}
              className="text-center text-xs sm:text-sm font-medium text-ink-500 p-1"
            >
              {day}
            </div>
          ))}
        </div>

        <div className="grid grid-cols-7 gap-0.5">
          {getDaysInMonth(currentMonth).map((date, index) => {
            if (!date) return <div key={index} className="text-center"></div>;
            
            const isToday = date.toDateString() === today.toDateString();
            const isSelected = selectedDate && date.toDateString() === selectedDate.toDateString();
            const isFuture = date > today;
            
            return (
              <div key={index} className="text-center">
                <button
                  type="button"
                  onClick={() => handleDateSelect(date)}
                  className={`w-6 h-6 sm:w-7 sm:h-7 md:w-8 md:h-8 text-xs sm:text-sm rounded transition-colors ${
                    isSelected
                      ? "bg-navy-600 text-white hover:bg-navy-700 font-medium" // Selected date - Dark blue background, white text
                      : isToday && !isReceivedReading
                      ? "bg-navy-100 text-navy-400 hover:bg-navy-200 font-medium" // Today for Insert New - Light blue background, light blue text
                      : isFuture
                      ? "bg-ink-100 text-ink-400 cursor-not-allowed" // Future dates
                      : "text-ink-700 hover:bg-navy-50" // Normal dates
                  }`}
                  disabled={isFuture}
                  title={date.toLocaleDateString('en-GB')}
                >
                  {date.getDate()}
                </button>
              </div>
            );
          })}
        </div>

        {/* Today Button */}
        <div className="mt-3 sm:mt-4 pt-3 border-t border-ink-200">
          <button
            type="button"
            onClick={handleTodayClick}
            className="w-full px-3 py-2 text-xs sm:text-sm bg-navy-500 text-white rounded hover:bg-navy-600 transition-colors font-medium"
          >
            Today
          </button>
        </div>
        
        {/* Debug info - shows current selected date */}
        {/* {process.env.NODE_ENV === 'development' && (
          <div className="mt-2 pt-2 border-t border-ink-200 text-xs text-ink-500">
            <div>Selected: {selectedDate ? selectedDate.toLocaleDateString('en-GB') : 'None'}</div>
            <div>Today: {today.toLocaleDateString('en-GB')}</div>
            <div>Mode: {isReceivedReading ? 'Edit Received' : 'Insert New'}</div>
          </div>
        )} */}
      </div>
    </div>
  );
};

export default CalendarComponent;