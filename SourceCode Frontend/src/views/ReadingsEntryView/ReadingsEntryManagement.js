// src/views/ReadingsEntryView/ReadingsEntryManagement.js
import React from "react";
import { useReadingsManagement } from "./hooks/useReadingsManagement";
import NcreAccountDetailsSection from "./components/NcreAccountDetailsSection";
import MeterReadingsSection from "./components/MeterReadingsSection";
import ActionButtons from "./components/ActionButtons";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";
import UnsavedChangesModal from "components/Modal/UnsavedChangesModal";
import ResolveInvoiceConfirmationModal from "components/Modal/ResolveInvoiceConfirmationModal";

const ReadingsEntryManagement = () => {
  const {
    formData,
    originalData,
    loading,
    accountLoaded,
    showCalendar,
    currentMonth,
    selectedDate,
    showLoadButton,
    editedFields,
    saveUpdatesActive,
    saveReadingsActive,
    resetActive,
    customerMeterTypes,
    headerConfig,
    readingDateInputRef,
    isReceivedReading,
    handleFolioNumberChange,
    loadAccountData,
    handleInputChange,
    handleRefresh,
    setShowCalendar,
    setCurrentMonth,
    setSelectedDate,
    handleDateSelect,
    handleTodayClick,
    navigateMonth,
    handleSaveUpdates,
    handleSaveReadings,
    handleResetEdits,
    getFieldStyle,
    getDaysInMonth,
    hasUnsavedChanges,
    showUnsavedModal,
    handleBackClick,
    handleLeavePage,
    handleStayOnPage,
    breadcrumbItems,
    showResolveConfirmationModal,
    setShowResolveConfirmationModal,
    isResolveActionLoading,
    handleConfirmResolveSubmit,
    handleConfirmResolveDraft
  } = useReadingsManagement();

  console.log("Rendering with customerMeterTypes:", customerMeterTypes);

  return (
    <div className="flex flex-col gap-ds-6">
      {/* ADD UNSAVED CHANGES MODAL */}
      <UnsavedChangesModal
        isOpen={showUnsavedModal}
        onLeave={handleLeavePage}
        onStay={handleStayOnPage}
      />

      {/* RESOLVE INVOICE CONFIRMATION MODAL */}
      <ResolveInvoiceConfirmationModal
        isOpen={showResolveConfirmationModal}
        onClose={() => setShowResolveConfirmationModal(false)}
        onSubmitForReview={handleConfirmResolveSubmit}
        onSaveAsDraft={handleConfirmResolveDraft}
        isActionLoading={isResolveActionLoading}
      />
      
      {/* Premium Navy Gradient Stripe Header Block */}
      <div>
        <div className="ds-page-toolbar mx-auto">
          <div>
            <h1 className="ds-page-title">
              {headerConfig.title || "Monthly Readings Entry"}
            </h1>
            <p className="ds-page-subtitle mt-1">
              {isReceivedReading 
                ? "Edit and update received meter readings for this account profile" 
                : "Insert and record new meter readings for this account profile"}
            </p>
            {headerConfig.showStatus && (
              <div className="flex items-center mt-1 space-x-2">
                <span className="ds-body-sm font-semibold text-ink-700">Reading Status:</span>
                <span className={`px-2 py-0.5 rounded-lg text-xs font-bold ${
                  isReceivedReading || formData.reading_status === "RECEIVED" 
                    ? "bg-success-500/20 text-success-500 border border-success-500/30" 
                    : "bg-warning-500/20 text-warning-500 border border-warning-500/30"
                }`}>
                  {isReceivedReading ? "RECEIVED" : formData.reading_status}
                </span>
              </div>
            )}
          </div>
          {headerConfig.showRefresh && (
            <div className="ds-page-actions">
              <button
                onClick={handleRefresh}
                className="ds-btn ds-btn-secondary ds-btn-sm disabled:opacity-50"
                title="Refresh data"
                disabled={loading}
              >
                {loading ? (
                  <div className="ds-spinner ds-spinner-sm ds-spinner-invert"></div>
                ) : (
                  <i className="fas fa-sync-alt"></i>
                )}
                <span>Refresh</span>
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="px-0 mx-auto w-full relative mb-8">
        <div className="ds-card p-ds-5 overflow-hidden">
          {/* Breadcrumb wrapper */}
          <div className="mb-6 p-3.5 bg-ink-50 border border-ink-100 rounded-2xl shadow-xs">
            <Breadcrumb 
              items={breadcrumbItems} 
              onBackClick={handleBackClick}
              hasUnsavedChanges={!!hasUnsavedChanges}
            />
          </div>

          <div>
            <NcreAccountDetailsSection
              formData={formData}
              accountLoaded={accountLoaded}
              loading={loading}
              showCalendar={showCalendar}
              selectedDate={selectedDate}
              currentMonth={currentMonth}
              editedFields={editedFields}
              setShowCalendar={setShowCalendar}
              setCurrentMonth={setCurrentMonth}
              setSelectedDate={setSelectedDate}
              handleDateSelect={handleDateSelect}
              handleTodayClick={handleTodayClick}
              navigateMonth={navigateMonth}
              getFieldStyle={getFieldStyle}
              getDaysInMonth={getDaysInMonth}
              readingDateInputRef={readingDateInputRef}
              handleFolioNumberChange={handleFolioNumberChange}
              loadAccountData={loadAccountData}
              showLoadButton={showLoadButton}
            />

            <MeterReadingsSection
              formData={formData}
              accountLoaded={accountLoaded}
              editedFields={editedFields}
              handleInputChange={handleInputChange}
              getFieldStyle={getFieldStyle}
              customerMeterTypes={customerMeterTypes}
              isReceivedReading={isReceivedReading}
            />

            <ActionButtons
              saveReadingsActive={saveReadingsActive}
              saveUpdatesActive={saveUpdatesActive}
              resetActive={resetActive}
              handleSaveReadings={handleSaveReadings}
              handleSaveUpdates={handleSaveUpdates}
              handleResetEdits={handleResetEdits}
              isReceivedReading={isReceivedReading}
              editedFields={editedFields}
              formData={formData}
              originalData={originalData}
              calculationSuccessful={true}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReadingsEntryManagement;