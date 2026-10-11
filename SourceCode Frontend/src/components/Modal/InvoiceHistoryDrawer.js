import React, { useState, useEffect } from "react";
import { getInvoiceHistory } from "../../services/invoiceService";

const InvoiceHistoryDrawer = ({ accountNumber, folioNo, currentInvoiceId, currentInvoiceMonth, isOpen, setIsOpen }) => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && accountNumber) {
      const loadHistory = async () => {
        setLoading(true);
        try {
          const data = await getInvoiceHistory(accountNumber);
          const filtered = (data || []).filter(
            (inv) => inv.id !== currentInvoiceId && inv.invoiceMonth !== currentInvoiceMonth
          );
          setHistory(filtered);
        } catch (err) {
          console.error("Failed to load history:", err);
        } finally {
          setLoading(false);
        }
      };
      loadHistory();
    }
  }, [isOpen, accountNumber, currentInvoiceId, currentInvoiceMonth]);

  return (
    <>
      {/* Floating Toggle Handle on right edge of page */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="fixed right-0 top-1/2 -translate-y-1/2 z-50 bg-navy-800 hover:bg-navy-950 text-white px-3 py-4 rounded-l-2xl shadow-xl flex flex-col items-center gap-2 border-l border-y border-navy-500/20 cursor-pointer transition-all duration-200 group"
        title="View Previous Cycle Invoices"
      >
        <i className="fas fa-history text-sm animate-pulse group-hover:scale-110 transition-transform"></i>
        <span className="text-[10px] font-bold uppercase tracking-widest writing-mode-vertical">History</span>
      </button>



      {/* Slide-out Drawer Panel */}
      <div
        className={`fixed right-0 top-0 bottom-0 z-50 w-full sm:w-[450px] bg-white border-l border-ink-200 shadow-overlay transition-transform duration-300 transform flex flex-col ${
          isOpen ? "translate-x-0" : "translate-x-full"
        }`}
      >
        {/* Drawer Header */}
        <div className="p-6 border-b border-ink-100 bg-navy-900 text-white flex items-center justify-between">
          <div>
            <h3 className="text-sm font-extrabold flex items-center gap-2">
              <i className="fas fa-history text-navy-400"></i> Folio billing history
            </h3>
            <p className="text-[11px] text-navy-200 mt-1">Previous cycles for Folio: {folioNo ?? "N/A"}</p>
          </div>
          <button
            type="button"
            onClick={() => setIsOpen(false)}
            className="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <i className="fas fa-times"></i>
          </button>
        </div>

        {/* Drawer Body content */}
        <div className="flex-1 overflow-y-auto p-6 bg-ink-50">
          {loading ? (
            <div className="flex flex-col items-center justify-center h-full gap-2">
              <span className="ds-spinner" aria-hidden="true"></span>
              <span className="text-xs text-ink-500 font-medium">Fetching history...</span>
            </div>
          ) : history.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full gap-2 text-center">
              <i className="fas fa-folder-open text-lg text-ink-400"></i>
              <span className="text-xs text-ink-500 font-medium">No previous finalized invoices found.</span>
            </div>
          ) : (
            <div className="space-y-4">
              {history.map((inv) => (
                <div key={inv.id} className="bg-white p-4 rounded-2xl border border-ink-200/60 shadow-xs hover:shadow-md transition-shadow duration-150">
                  {/* Row 1: Month & Status */}
                  <div className="flex items-center justify-between border-b border-ink-100 pb-2 mb-2">
                    <span className="text-xs font-bold text-navy-800">{inv.invoiceMonth}</span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-success-100 text-success-800 uppercase tracking-wider">
                      Finalized
                    </span>
                  </div>

                  {/* Row 2: Grid values */}
                  <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-xs">
                    <div>
                      <p className="text-[10px] uppercase font-bold text-ink-400">Cost of Energy</p>
                      <p className="font-semibold text-ink-700">
                        Rs. {inv.costOfEnergy ? parseFloat(inv.costOfEnergy).toLocaleString("en-US", { minimumFractionDigits: 2 }) : "0.00"}
                      </p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase font-bold text-ink-400">Amount Paid</p>
                      <p className="font-bold text-ink-700">
                        Rs. {inv.developerPymnt ? parseFloat(inv.developerPymnt).toLocaleString("en-US", { minimumFractionDigits: 2 }) : "0.00"}
                      </p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase font-bold text-ink-400">Tariff Rate</p>
                      <p className="font-semibold text-ink-600">
                        Rs. {inv.ratePerKwh ? parseFloat(inv.ratePerKwh).toFixed(2) : "0.00"} / kWh
                      </p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase font-bold text-ink-400">Plant Factor</p>
                      <p className="font-semibold text-ink-600">
                        {inv.plantFactorPercent ? parseFloat(inv.plantFactorPercent).toFixed(2) : "0.00"}%
                      </p>
                    </div>
                  </div>

                  {/* Row 3: Approved Date */}
                  <div className="mt-3 border-t border-ink-100 pt-2 flex items-center justify-between text-[10px] text-ink-400">
                    <span>Approved At:</span>
                    <span className="font-semibold text-ink-600">
                      {inv.approvedAt ? inv.approvedAt.substring(0, 10) : "-"}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default InvoiceHistoryDrawer;
