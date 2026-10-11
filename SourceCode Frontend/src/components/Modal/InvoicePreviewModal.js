import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { validateInvoice, submitInvoice } from "../../services/invoiceService";
import { toast } from "react-toastify";
import ValidationStatusModal from "./ValidationStatusModal";
import InvoiceHistoryDrawer from "./InvoiceHistoryDrawer";

const convertNumberToWords = (numStr) => {
  if (!numStr) return "";
  const num = parseFloat(numStr.replace(/,/g, ""));
  if (isNaN(num)) return "";
  
  const ones = ["", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"];
  const tens = ["", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"];
  
  const convertLessThanThousand = (n) => {
    if (n === 0) return "";
    let str = "";
    if (n >= 100) {
      str += ones[Math.floor(n / 100)] + " Hundred ";
      n %= 100;
    }
    if (n >= 20) {
      str += tens[Math.floor(n / 10)] + " ";
      n %= 10;
    }
    if (n > 0) {
      str += ones[n] + " ";
    }
    return str.trim();
  };
  
  const convert = (n) => {
    if (n === 0) return "Zero";
    let str = "";
    if (n >= 1000000) {
      str += convertLessThanThousand(Math.floor(n / 1000000)) + " Million ";
      n %= 1000000;
    }
    if (n >= 1000) {
      str += convertLessThanThousand(Math.floor(n / 1000)) + " Thousand ";
      n %= 1000;
    }
    if (n > 0) {
      str += convertLessThanThousand(n) + " ";
    }
    return str.trim();
  };
  
  const parts = num.toFixed(2).split(".");
  const mainPart = parseInt(parts[0], 10);
  const centsPart = parseInt(parts[1], 10);
  
  let words = convert(mainPart) + " Rupees";
  if (centsPart > 0) {
    words += " and " + convertLessThanThousand(centsPart) + " Cents";
  }
  words += " Only";
  return words;
};

const formatChiefEngineerTitle = (designation) => {
  if (!designation) return "";
  const trimmed = designation.trim();
  if (trimmed.toLowerCase() === "chief engineer") {
    return `${trimmed} (REP)`;
  }
  return trimmed;
};

const formatDirectorTitle = (designation) => {
  if (!designation) return "";
  const trimmed = designation.trim();
  if (trimmed.toLowerCase() === "director") {
    return `${trimmed} (ET-GL)`;
  }
  return trimmed;
};

const InvoicePreviewModal = ({ 
  isOpen, 
  onClose, 
  invoiceData, 
  showValidationBtn = true,
  isReviewMode = false,
  reviewRemarks = "",
  onRemarksChange,
  onApprove,
  onReject,
  onResubmit,
  onDownload,
  downloadingId,
  submittingReview = false
}) => {
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState(null);
  const [showValidationModal, setShowValidationModal] = useState(false);
  const [isActionLoading, setIsActionLoading] = useState(false);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  const userCategory = sessionStorage.getItem("user_category") || "";
  const isCE = userCategory === "Chief Engineer";
  const isDGM = userCategory === "DGM" || userCategory === "Director" || userCategory === "DIRECTOR";
  const isAdmin = userCategory === "Admin";
  const isEE = userCategory === "Electrical Engineer" || userCategory === "EE" || (!isCE && !isDGM && !isAdmin);

  const handleValidateClick = async () => {
    if (isValidating) return;
    setIsValidating(true);
    try {
      const response = await validateInvoice({
        accountNumber: invoiceData.accountNumber,
        areaCode: invoiceData.areaCode || "GL",
        billCycle: invoiceData.billCycle || "1",
      });
      setValidationResult(response);
      setShowValidationModal(true);
    } catch (error) {
      console.error("Error validating invoice:", error);
      toast.error(error.message || "Failed to run invoice validation.");
    } finally {
      setIsValidating(false);
    }
  };

  const handleSubmit = async () => {
    if (isActionLoading) return;
    setIsActionLoading(true);
    try {
      await submitInvoice({
        accountNumber: invoiceData.accountNumber,
        areaCode: invoiceData.areaCode || "GL",
        billCycle: invoiceData.billCycle || "1",
      });
      toast.success("Invoice submitted for review successfully.");
      setShowValidationModal(false);
      onClose();
    } catch (error) {
      console.error("Error submitting invoice:", error);
      toast.error(error.message || "Failed to submit invoice.");
    } finally {
      setIsActionLoading(false);
    }
  };

  if (!isOpen || !invoiceData) return null;





  const printStyles = `
    @media print {
      /* Hide everything except the modal print area */
      body * {
        visibility: hidden;
      }
      #invoice-print-area, #invoice-print-area * {
        visibility: visible;
      }
      #invoice-print-area {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        margin: 0;
        padding: 0;
        box-shadow: none;
        border: none;
        background: white;
      }
      .no-print {
        display: none !important;
      }
      /* Remove background colors and shadows from read-only inputs for print clarity */
      input {
        background-color: transparent !important;
        border-color: #cbd5e1 !important;
        box-shadow: none !important;
        color: #0f172a !important;
      }
      .card-section {
        box-shadow: none !important;
        border: 1px solid #e2e8f0 !important;
        margin-bottom: 1.5rem !important;
      }
    }
  `;

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: printStyles }} />
      <div className="fixed inset-0 z-50 overflow-y-auto flex items-start justify-center p-4 md:py-3">
        {/* Backdrop */}
        <div className="fixed inset-0 transition-opacity bg-ink-900/60" onClick={onClose}></div>

        {/* Modal Main container */}
        <div 
          id="invoice-print-area"
          className={`relative w-full max-w-5xl bg-ink-50 rounded-xl shadow-overlay overflow-hidden transform transition-all duration-300 my-8 z-10 flex flex-col border border-ink-200 ${
            isHistoryOpen ? "lg:-translate-x-40" : ""
          }`}
        >
          {/* Header Section */}
          <div className="ds-modal-header no-print bg-navy-800 border-navy-900 flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="p-1.5 bg-success-800/40 rounded-lg border border-success-500/20">
                <i className="fas fa-file-invoice text-lg"></i>
              </span>
              <div>
                <h2 className="text-xl font-bold">{isReviewMode ? "Review Invoice" : "Invoice Preview"}</h2>
                <div className="text-xs opacity-90 mt-0.5">
                  Folio: {invoiceData.folioNo ?? invoiceData.folio_no ?? "N/A"}
                </div>
              </div>
            </div>
            <button
              onClick={onClose}
              className="text-white/80 hover:text-white transition-colors p-1.5 rounded-full hover:bg-success-800/40 focus:outline-none"
              title="Close modal"
            >
              <i className="fas fa-times text-xl"></i>
            </button>
          </div>

          {/* Form Content Body */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            
            <div className="max-w-4xl mx-auto flex flex-col">
              {invoiceData.status === "REJECTED" && (
                <div className="mb-4 bg-critical-50 border-l-4 border-critical-500 p-4 rounded-r-xl shadow-xs no-print">
                  <div className="flex items-start space-x-3">
                    <div className="flex-shrink-0 text-critical-500 mt-0.5">
                      <i className="fas fa-exclamation-circle text-lg"></i>
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-critical-800">Invoice Status: REJECTED</h4>
                      <p className="text-xs text-critical-700 mt-1 leading-relaxed">
                        <strong>Reviewer Remarks:</strong> {invoiceData.remarks || "No specific remarks provided."}
                      </p>
                      <p className="text-[11px] text-critical-600 mt-1">
                        Please review the rejection reason above and ensure readings or parameters are corrected before re-submitting.
                      </p>
                    </div>
                  </div>
                </div>
              )}
              {invoiceData.folioNo && (
                <div className="text-right font-bold text-xs text-ink-700 mb-1.5 pr-1">
                  Folio No: {invoiceData.folioNo}
                </div>
              )}
              <div className="bg-white p-8 text-sm text-ink-900 border border-ink-200 shadow-sm rounded-lg">

              {/* Header */}
              <div className="flex justify-between items-start mb-8">
                <div>
                  <h2 className="text-lg font-semibold underline">
                    Payment Invoice
                  </h2>
                </div>
                <div>
                  <table className="text-sm">
                    <tbody>
                      <tr>
                        <td className="pr-4 text-left">Ref No</td>
                        <td className="px-2">:</td>
                        <td className="font-bold text-left">{invoiceData.refNo || "-"}</td>
                      </tr>
                      <tr>
                        <td className="text-left">Ref Code</td>
                        <td className="px-2">:</td>
                        <td className="font-bold text-left">{invoiceData.refCode || "-"}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Company */}
              <div className="mb-6">
                <table>
                  <tbody>
                    <tr>
                      <td className="w-32 text-left">Company</td>
                      <td className="px-2">:</td>
                      <td className="font-medium text-left">
                        {invoiceData.companyName || "-"}
                      </td>
                    </tr>
                    <tr>
                      <td className="text-left">Project</td>
                      <td className="px-2">:</td>
                      <td className="font-medium text-left">
                        {invoiceData.projectName || "-"}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              {/* Invoice Month */}
              <div className="border-b border-black pb-1 mb-6 text-left">
                <h3 className="font-semibold text-left">
                  Invoice for the month of {invoiceData.invoiceMonth || "-"}
                </h3>
              </div>

              {/* Plant Info */}
              <div className="mb-8">
                <table className="w-full text-left">
                  <tbody>
                    <tr>
                      <td>Capacity of the Plant</td>
                      <td className="w-20 text-center">MW</td>
                      <td className="w-8 text-center">=</td>
                      <td className="text-right font-mono">{invoiceData.capacityMw || "0.000"}</td>
                    </tr>
                    <tr>
                      <td>Allowed Power Generation</td>
                      <td className="text-center">MW</td>
                      <td className="text-center">=</td>
                      <td className="text-right font-mono">{invoiceData.allowedGenerationMw || "0.000"}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              {/* Reading Table */}
              <table className="w-full border border-black border-collapse text-center mb-2">
                <thead>
                  <tr>
                    <th rowSpan="3" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider align-middle">Date</th>
                    <th colSpan="6" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider">Reading</th>
                    <th rowSpan="3" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider align-middle">mf</th>
                    <th rowSpan="3" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider align-middle">Energy (kWh)</th>
                  </tr>
                  <tr>
                    <th colSpan="3" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider">Present</th>
                    <th colSpan="3" className="border border-black p-1.5 bg-ink-50 font-semibold text-xs uppercase tracking-wider">Previous</th>
                  </tr>
                  <tr>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R1</th>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R2</th>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R3</th>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R1</th>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R2</th>
                    <th className="border border-black p-1 bg-ink-50 font-semibold text-xs">R3</th>
                  </tr>
                </thead>
                <tbody className="font-mono text-sm">
                  <tr>
                    <td className="border border-black p-1.5 font-sans text-ink-700 font-semibold text-left whitespace-nowrap">{invoiceData.presentReadingDate || "-"}</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.presentR1 || "0"}</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.presentR2 || "0"}</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.presentR3 || "0"}</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td rowSpan="2" className="border border-black p-1.5 align-middle text-center">{invoiceData.multiplyFactor || "1.0"}</td>
                    <td rowSpan="2" className="border border-black p-1.5 align-middle text-right font-bold">{invoiceData.energyKwh || "0.0"}</td>
                  </tr>
                  <tr>
                    <td className="border border-black p-1.5 font-sans text-ink-700 font-semibold text-left whitespace-nowrap">{invoiceData.previousReadingDate || "-"}</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td className="border border-black p-1.5 bg-ink-50/50 text-center">-</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.previousR1 || "0"}</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.previousR2 || "0"}</td>
                    <td className="border border-black p-1.5 text-right">{invoiceData.previousR3 || "0"}</td>
                  </tr>
                </tbody>
              </table>

              <p className="text-xs mb-6 text-left text-ink-500 italic">
                Please refer the invoice for details of date & corresponding readings
              </p>

              {/* Summary */}
              <div className="flex justify-start">
                <table className="w-full">
                  <tbody>
                    <tr>
                      <td className="text-left text-ink-600">{invoiceData.eligibleEnergyLabel || `Energy after reduction of ${invoiceData.generationLossPercent || "0"}% Energy losses`}</td>
                      <td className="w-20 text-center">kWh</td>
                      <td className="w-8 text-center">=</td>
                      <td className="text-right font-mono">{invoiceData.eligibleEnergyKwh || "0"}</td>
                    </tr>
                    <tr>
                      <td className="text-left text-ink-600">Period of generation</td>
                      <td className="text-center">Day</td>
                      <td className="text-center">=</td>
                      <td className="text-right font-mono">{invoiceData.periodOfGeneration || "0"}</td>
                    </tr>
                    <tr>
                      <td className="text-left text-ink-600">Plant Factor of the power plant</td>
                      <td></td>
                      <td className="text-center">=</td>
                      <td className="text-right font-mono">{invoiceData.plantFactorPercent || "0.00"}%</td>
                    </tr>
                    {(!invoiceData.tariffChargeLines || invoiceData.tariffChargeLines.length === 0) && (
                      <>
                        <tr>
                          <td className="text-left text-ink-600">Energy Purchased</td>
                          <td className="text-center">kWh</td>
                          <td className="text-center">=</td>
                          <td className="text-right font-mono">{invoiceData.energyPurchasedKwh || "0"}</td>
                        </tr>
                        <tr>
                          <td className="text-left text-ink-600">Rate / kWh</td>
                          <td className="text-center">Rs.</td>
                          <td className="text-center">=</td>
                          <td className="text-right font-mono">{invoiceData.ratePerKwh || "0.00"}</td>
                        </tr>
                        <tr className="font-bold border-t border-black">
                          <td className="text-left">Cost of Energy</td>
                          <td className="text-center">Rs.</td>
                          <td className="text-center">=</td>
                          <td className="text-right font-mono text-success-800">{invoiceData.costOfEnergy || "0.00"}</td>
                        </tr>
                      </>
                    )}
                  </tbody>
                </table>
              </div>

              {/* Transition Month Split Table */}
              {invoiceData.tariffChargeLines && invoiceData.tariffChargeLines.length > 0 && (
                <div className="mt-4">
                  <table className="w-full border-collapse border border-black text-xs font-sans">
                    <thead>
                      <tr className="border border-black">
                        <th className="border border-black px-2 py-1.5 text-center font-bold" style={{ width: "16%" }}>Tariff Rate</th>
                        <th className="border border-black px-2 py-1.5 text-center font-bold" style={{ width: "14%" }}>No. of Days</th>
                        <th colSpan={2} className="border border-black px-2 py-1.5 text-center font-bold" style={{ width: "38%" }}>Energy (kWh)</th>
                        <th className="border border-black px-2 py-1.5 text-center font-bold" style={{ width: "20%" }}>Cost (Rs.)</th>
                        <th className="border border-black px-2 py-1.5 text-center font-bold" style={{ width: "12%" }}>Remarks</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoiceData.tariffChargeLines.map((line, idx) => {
                        const dateParts = line.dateRange ? line.dateRange.split(" to ") : [line.periodStart, line.periodEnd];
                        return (
                          <tr key={idx} className="border border-black">
                            <td className="border border-black px-2 py-1.5 text-right font-mono">{line.ratePerKwh}</td>
                            <td className="border border-black px-2 py-1.5 text-center font-mono">{line.numberOfDays}</td>
                            <td className="border border-black px-2 py-1.5 text-left text-xs leading-tight whitespace-nowrap" style={{ width: "20%" }}>
                              {dateParts.length > 1 ? (
                                <>
                                  <div>{dateParts[0]} to</div>
                                  <div>{dateParts[1]}</div>
                                </>
                              ) : (
                                line.dateRange || `${line.periodStart} to ${line.periodEnd}`
                              )}
                            </td>
                            <td className="border border-black px-2 py-1.5 text-right font-mono" style={{ width: "18%" }}>{line.energyKwh}</td>
                            <td className="border border-black px-2 py-1.5 text-right font-mono">{line.costOfEnergy}</td>
                            <td className="border border-black px-2 py-1.5 text-center text-xs leading-tight">
                              {line.remarks ? (
                                line.remarks.includes("Tariff Change") ? (
                                  <>
                                    <div>Tariff Change</div>
                                    <div>{line.remarks.replace("Tariff Change", "").trim()}</div>
                                  </>
                                ) : (
                                  line.remarks
                                )
                              ) : null}
                            </td>
                          </tr>
                        );
                      })}
                      {/* Total row */}
                      <tr className="border border-black font-bold">
                        <td className="border border-black px-2 py-1.5 text-center font-bold">Total</td>
                        <td className="border border-black px-2 py-1.5 text-center font-mono">
                          {invoiceData.tariffChargeLines.reduce((acc, l) => acc + (parseInt(l.numberOfDays, 10) || 0), 0) || invoiceData.periodOfGeneration || "0"}
                        </td>
                        <td className="border border-black px-2 py-1.5"></td>
                        <td className="border border-black px-2 py-1.5 text-right font-mono">{invoiceData.energyPurchasedKwh || "0"}</td>
                        <td className="border border-black px-2 py-1.5 text-right font-mono">{invoiceData.costOfEnergy || "0.00"}</td>
                        <td className="border border-black px-2 py-1.5"></td>
                      </tr>
                    </tbody>
                  </table>

                  {/* Cost of Energy row beneath table */}
                  <table className="w-full mt-3 border-collapse text-xs font-sans">
                    <tbody>
                      <tr className="font-bold">
                        <td className="text-left py-1 text-sm font-bold text-black" style={{ width: "50%" }}>Cost of Energy</td>
                        <td className="text-right py-1 text-xs font-bold text-black" style={{ width: "18%" }}>Rs.</td>
                        <td className="text-center py-1 text-xs font-bold text-black" style={{ width: "4%" }}>=</td>
                        <td
                          className="text-right py-1 font-mono text-sm font-bold text-black"
                          style={{
                            width: "28%",
                            borderTop: "1px solid black",
                            borderBottom: "3px double black"
                          }}
                        >
                          {invoiceData.costOfEnergy || "0.00"}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}

              {/* Payment Deductions */}
              {invoiceData.paymentDeductions && invoiceData.paymentDeductions.length > 0 && (
                <div className="mt-6 flex justify-start">
                  <table className="w-full border-t border-black pt-4">
                    <tbody>
                      <tr>
                        <td colSpan={4} className="font-semibold underline pb-2 text-left mb-10">
                          Payment Deductions
                        </td>
                      </tr>
                      {invoiceData.paymentDeductions.map((deduction, idx) => (
                        <tr key={idx}>
                          <td className="text-left text-ink-600">{deduction.type}</td>
                          <td className="w-20 text-center font-mono text-ink-600">{deduction.percentage}%</td>
                          <td className="w-8 text-center text-ink-600">=</td>
                          <td className="text-right text-critical-600 font-mono">
                            - {deduction.deductionAmount}
                          </td>
                        </tr>
                      ))}
                      <tr className="font-bold border-t border-black">
                        <td className="text-left">Final Amount To Be Paid To The Developer</td>
                        <td className="text-center">Rs.</td>
                        <td className="text-center">=</td>
                        <td className="text-right font-bold text-success-800 font-mono">
                          {invoiceData.finalAmountToBePaid}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}

              {/* Signature */}
              <div className="mt-12 text-left">
                <p className="mb-2">Prepared by: </p>
                
                <p className="font-bold">{invoiceData.preparedBy?.name || invoiceData.preparedByName || "N/A"}</p>
                <p className="text-ink-600 font-medium">{invoiceData.preparedBy?.designation || invoiceData.preparedByTitle || ""}</p>
                <p className="text-xs text-ink-400 mt-1">Date : {invoiceData.preparedBy?.date || invoiceData.printDate || ""}</p>
              </div>

              {/* Director Approval */}
              <div className="mt-10 text-left">
                <h4 className="font-semibold underline">
                  DIRECTOR (ET-GL)
                </h4>
                <p className="mt-2 text-ink-800 leading-relaxed font-medium">
                  Payment of {convertNumberToWords(invoiceData.finalAmountToBePaid)} (Rs. {invoiceData.finalAmountToBePaid}) is certified correct please.
                </p>
              </div>

              {/* Chief Engineer */}
              <div className="mt-10 text-left">
                <p className="font-bold">{invoiceData.chiefEngineer?.name || ""}</p>
                <p className="text-slate-600 font-medium">{formatChiefEngineerTitle(invoiceData.chiefEngineer?.designation)}</p>
                <p className="text-xs text-slate-400 mt-1">Date : {invoiceData.chiefEngineer?.date || ""}</p>
              </div>

              {/* Director (BSTA & TM) */}
              <div className="mt-8 text-left">
                <h4 className="underline font-semibold">
                  Director (BSTA &amp; TM)
                </h4>
                <p className="text-slate-600 font-medium mt-1">Payment to be settled ASAP</p>
              </div>

              {/* Director */}
              <div className="mt-12 text-left">
                <p className="font-bold">{invoiceData.director?.name || ""}</p>
                <p className="text-slate-600 font-medium">{formatDirectorTitle(invoiceData.director?.designation)}</p>
                <p className="text-xs text-slate-400 mt-1">Date : {invoiceData.director?.date || ""}</p>
              </div>

              <div className="mt-10 grid gap-3 border-t border-ink-200 pt-4 text-sm sm:grid-cols-2">
                <p className="font-semibold text-ink-700">
                  Invoice created on: <span className="font-normal text-ink-600">{invoiceData.invoiceCreatedOn || invoiceData.preparedBy?.date || ""}</span>
                </p>
                <p className="font-semibold text-ink-700">
                  Invoice approved on: <span className="font-normal text-ink-600">{invoiceData.invoiceApprovedOn || invoiceData.director?.date || ""}</span>
                </p>
              </div>

              {/* Footer */}
              <div className="mt-16 text-right text-xs text-ink-400 font-mono">
                Print on : {invoiceData.printDate}
              </div>

            </div>
          </div>

            {isReviewMode && (
              <div className="bg-white rounded-lg shadow-sm border border-ink-200 p-6 card-section space-y-3">
                <label className="block text-xs font-bold uppercase tracking-wider text-ink-500">
                  Review Remarks / Comments
                </label>
                <textarea
                  rows={4}
                  value={reviewRemarks}
                  onChange={(e) => onRemarksChange && onRemarksChange(e.target.value)}
                  placeholder="Provide any comments or reason for rejection..."
                  className="w-full bg-white border border-ink-300 rounded-lg p-3 text-ink-800 text-sm focus:outline-none focus:border-success-500 focus:ring-1 focus:ring-success-500 transition-colors"
                ></textarea>
              </div>
            )}

          </div>

          {/* Action Buttons Footer */}
          <div className="ds-modal-footer no-print flex-col-reverse sm:flex-row-reverse">
            {isReviewMode ? (
              <>
                <button
                  type="button"
                  onClick={onApprove}
                  disabled={submittingReview}
                  className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-transparent shadow-sm px-5 py-2 bg-success-600 hover:bg-success-700 active:bg-success-800 text-white text-sm font-semibold transition-all duration-150 gap-2 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none"
                >
                  <i className="fas fa-check-circle"></i>
                  {isCE ? "Recommend" : "Approve"}
                </button>
                <button
                  type="button"
                  onClick={onReject}
                  disabled={submittingReview}
                  className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-transparent shadow-sm px-5 py-2 bg-critical-600 hover:bg-critical-700 active:bg-critical-800 text-white text-sm font-semibold transition-all duration-150 gap-2 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none"
                >
                  <i className="fas fa-times-circle"></i>
                  Reject
                </button>
                <button
                  type="button"
                  onClick={onClose}
                  disabled={submittingReview}
                  className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-ink-300 shadow-sm px-5 py-2 bg-white text-ink-700 text-sm font-semibold hover:bg-ink-50 active:bg-ink-100 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-150 focus:outline-none"
                >
                  Cancel
                </button>
              </>
            ) : (
              <>

                {showValidationBtn && (
                  <button
                    type="button"
                    onClick={handleValidateClick}
                    disabled={isValidating}
                    className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-navy-600 text-sm font-medium text-white hover:bg-navy-700 disabled:opacity-60 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500"
                  >
                    {isValidating ? (
                      <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                    ) : (
                      <i className="fas fa-shield-alt mr-2"></i>
                    )}
                    Validate Invoice
                  </button>
                )}
                {invoiceData?.status === "REJECTED" && onResubmit && isEE && (
                  <button
                    type="button"
                    onClick={() => onResubmit(invoiceData)}
                    disabled={isValidating}
                    className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-navy-600 text-sm font-medium text-white hover:bg-navy-700 disabled:opacity-60 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-navy-500 cursor-pointer"
                  >
                    <i className="fas fa-paper-plane mr-2"></i>
                    Re-submit for Review
                  </button>
                )}
                {onDownload && (
                  <button
                    type="button"
                    onClick={() => onDownload(invoiceData)}
                    disabled={downloadingId === invoiceData?.id}
                    className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-slate-300 shadow-sm px-4 py-2 bg-white text-sm font-medium text-emerald-700 hover:bg-emerald-50 hover:border-emerald-300 focus:outline-none transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {downloadingId === invoiceData?.id ? (
                      <i className="fas fa-spinner fa-spin mr-2 text-emerald-600"></i>
                    ) : (
                      <i className="fas fa-download mr-2 text-emerald-600"></i>
                    )}
                    Download PDF
                  </button>
                )}
                <button
                  type="button"
                  onClick={onClose}
                  className="w-full sm:w-auto inline-flex items-center justify-center rounded-md border border-ink-300 shadow-sm px-4 py-2 bg-white text-sm font-medium text-ink-700 hover:bg-ink-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-ink-500"
                >
                  Close
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      <ValidationStatusModal
        isOpen={showValidationModal}
        isValid={validationResult?.valid || false}
        errors={validationResult?.errors || []}
        onSubmit={handleSubmit}
        onClose={() => setShowValidationModal(false)}
        isActionLoading={isActionLoading}
        accountNumber={invoiceData?.accountNumber}
        areaCode={invoiceData?.areaCode}
        billCycle={invoiceData?.billCycle}
      />
      <InvoiceHistoryDrawer 
        accountNumber={invoiceData?.accountNumber} 
        folioNo={invoiceData?.folioNo ?? invoiceData?.folio_no}
        currentInvoiceId={invoiceData?.id}
        currentInvoiceMonth={invoiceData?.invoiceMonth}
        isOpen={isHistoryOpen}
        setIsOpen={setIsHistoryOpen}
      />
    </>
  );
};

InvoicePreviewModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  invoiceData: PropTypes.object,
  showValidationBtn: PropTypes.bool,
  isReviewMode: PropTypes.bool,
  reviewRemarks: PropTypes.string,
  onRemarksChange: PropTypes.func,
  onApprove: PropTypes.func,
  onReject: PropTypes.func,
  onResubmit: PropTypes.func,
  onDownload: PropTypes.func,
  downloadingId: PropTypes.any,
  submittingReview: PropTypes.bool,
};

export default InvoicePreviewModal;
