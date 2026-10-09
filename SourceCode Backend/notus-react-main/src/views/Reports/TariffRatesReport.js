import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import {
  getTariffRatesPreview,
  downloadTariffRatesPdf,
  downloadTariffRatesCsv,
} from "../../services/tariffRatesReportService";

// Standard column label map for known fields
const columnLabelMap = {
  row_no: "No.",
  folio_no: "Folio Number",
  developer_name: "Developer Name",
  facility_name: "Project Name",
  techno_type: "Technology",
  tariff_desc: "Tariff Category",
  prv_tariff_rate: "Previous Tariff (Rs/kWh)",
  tariff_changed: "Tariff Changed On",
  cur_tariff_rate: "Current Tariff (Rs/kWh)",
};

const formatHeader = (key) => {
  if (columnLabelMap[key]) return columnLabelMap[key];
  return key
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

export default function TariffRatesReport() {
  const [rows, setRows] = useState([]);
  const [loadingPreview, setLoadingPreview] = useState(true);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingCsv, setDownloadingCsv] = useState(false);
  const [previewError, setPreviewError] = useState("");

  const fetchPreview = async () => {
    setLoadingPreview(true);
    setPreviewError("");
    try {
      const data = await getTariffRatesPreview();
      setRows(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Preview load error:", error);
      setPreviewError(error.message || "Unable to load report.");
    } finally {
      setLoadingPreview(false);
    }
  };

  useEffect(() => {
    fetchPreview();
  }, []);

  const handleDownloadPDF = async () => {
    setDownloadingPdf(true);
    try {
      await downloadTariffRatesPdf();
      toast.success("PDF downloaded successfully!");
    } catch (error) {
      console.error("PDF export failed:", error);
      toast.error(`Failed to download PDF: ${error.message}`);
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleDownloadCSV = async () => {
    setDownloadingCsv(true);
    try {
      await downloadTariffRatesCsv();
      toast.success("CSV downloaded successfully!");
    } catch (error) {
      console.error("CSV export failed:", error);
      toast.error(`Failed to download CSV: ${error.message}`);
    } finally {
      setDownloadingCsv(false);
    }
  };

  // Extract dynamic column keys from rows if available
  const columnKeys =
    rows.length > 0
      ? Object.keys(rows[0])
      : Object.keys(columnLabelMap);

  return (
    <div className="flex flex-col gap-ds-6">
      <div className="w-full">
        <div className="ds-card">
          {/* Card Header with ONLY Title & Download Buttons */}
          <div className="ds-page-header">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h6 className="text-ink-700 text-xl font-bold">Tariff Rate Report</h6>
              </div>
              <div className="ml-auto flex flex-wrap justify-end gap-2">
                <button
                  type="button"
                  disabled={downloadingPdf || downloadingCsv || loadingPreview}
                  onClick={handleDownloadPDF}
                  className={`ds-btn ds-btn-primary ${
                    downloadingPdf || downloadingCsv || loadingPreview
                      ? "bg-ink-400 cursor-not-allowed"
                      : "bg-critical-600 hover:bg-critical-700 active:bg-critical-800"
                  }`}
                >
                  <i className={`fas ${downloadingPdf ? "fa-spinner fa-spin" : "fa-file-pdf"} mr-2`}></i>
                  {downloadingPdf ? "Generating PDF..." : "Download PDF"}
                </button>
                <button
                  type="button"
                  disabled={downloadingPdf || downloadingCsv || loadingPreview}
                  onClick={handleDownloadCSV}
                  className={`ds-btn ds-btn-primary ${
                    downloadingPdf || downloadingCsv || loadingPreview
                      ? "bg-ink-400 cursor-not-allowed"
                      : "bg-success-600 hover:bg-success-700 active:bg-success-800"
                  }`}
                >
                  <i className={`fas ${downloadingCsv ? "fa-spinner fa-spin" : "fa-file-csv"} mr-2`}></i>
                  {downloadingCsv ? "Generating CSV..." : "Download CSV"}
                </button>
              </div>
            </div>
          </div>

          {/* Card Content containing ONLY Report Preview Title & Table */}
          <div className="flex-auto px-4 lg:px-6 py-6">
            <div className="mb-4">
              <h5 className="text-ink-700 text-lg font-semibold">Report Preview</h5>
            </div>

            {previewError ? (
              <div className="px-4 py-8 text-center text-critical-600 bg-critical-50 rounded-lg border border-critical-200">
                <i className="fas fa-exclamation-circle mr-2"></i>
                Unable to load report: {previewError}
                <div className="mt-3">
                  <button
                    type="button"
                    onClick={fetchPreview}
                    className="ds-btn ds-btn-danger ds-btn-sm"
                  >
                    Retry
                  </button>
                </div>
              </div>
            ) : (
              <div className="overflow-x-auto max-h-[600px] overflow-y-auto border border-ink-200 rounded-lg">
                <table className="min-w-full divide-y divide-ink-200 border-collapse">
                  <thead className="bg-ink-100 sticky top-0 z-10 shadow-sm">
                    <tr>
                      {columnKeys.map((key) => (
                        <th
                          key={key}
                          className="px-4 py-3 text-left text-xs font-bold text-ink-700 uppercase whitespace-nowrap bg-ink-100 border-b border-ink-200"
                        >
                          {formatHeader(key)}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-ink-200 bg-white">
                    {loadingPreview ? (
                      <tr>
                        <td
                          colSpan={columnKeys.length}
                          className="px-4 py-12 text-center text-ink-500"
                        >
                          <span role="status" aria-live="polite" className="inline-flex items-center justify-center gap-3">
                            <span className="ds-spinner" aria-hidden="true"></span>
                            <span>Loading preview data...</span>
                          </span>
                        </td>
                      </tr>
                    ) : rows.length === 0 ? (
                      <tr>
                        <td
                          colSpan={columnKeys.length}
                          className="px-4 py-12 text-center text-ink-500"
                        >
                          <i className="fas fa-folder-open text-lg text-ink-400"></i>
                          No tariff rate report data available.
                        </td>
                      </tr>
                    ) : (
                      rows.map((row, index) => (
                        <tr
                          key={index}
                          className="hover:bg-ink-50 transition-colors"
                        >
                          {columnKeys.map((key) => (
                            <td
                              key={key}
                              className="px-4 py-3 text-sm text-ink-700 whitespace-nowrap border-b border-ink-100"
                            >
                              {row[key] !== null && row[key] !== undefined
                                ? String(row[key])
                                : ""}
                            </td>
                          ))}
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}