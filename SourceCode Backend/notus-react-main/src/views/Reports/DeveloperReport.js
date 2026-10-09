import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import {
  getReportPreview,
  downloadReportPdf,
  downloadReportCsv,
} from "../../services/developerReportService";
import {
  getAllRegions,
  getAllProvinces,
  getAllAreas,
  getProvincesByRegion,
  getAreasByRegion,
  getAreasByRegionAndProvince,
} from "../../services/locationService";
import { getNcreTariffDescriptions } from "../../services/developerRegistrationService";

// Standard column label map fallback for known fields
const columnLabelMap = {
  file_ref_no: "File Ref No",
  sr_no_upto_date: "Sr No Upto Date",
  tariff_type: "Tariff",
  file_no: "File No",
  folio_no: "Folio No",
  type: "Type",
  province: "Province",
  developer_name: "Developer Name",
  facility_name: "Facility Name",
  commissioned_capacity_mw: "Commissioned Capacity (MW)",
  loi_issued: "LOI Issued",
  sppa_signed: "SPPA Signed",
  grid_connection_date: "Grid Connection / Commissioned Date",
  reference_code: "Reference Code",
  region: "Region",
  sr_no: "Region Sr No",
  area: "Area",
  grid_substation: "Grid Substation",
  initial_tariff: "Initial Tariff",
  expiration_date: "EXP Date",
  expiration_extension_date: "EXP Date of Extension",
  sppa_capacity_mw: "SPPA Signed Capacity (MW)",
  feeder_no: "Feeder No",
  commissioned_year: "Commissioned Year",
  ac_expiration_with_extension: "AC EXP Date (5 Yr Extension)",
  ex: "EX",
  ac: "AC",
  flat: "FLAT",
  ttt: "TTT",
  first_tier: "First Tier",
  second_tier: "Second Tier",
  third_tier: "Third Tier",
  new_sppa_signed: "New SPPA Signed",
  validity_start: "Validity Start",
  validity_expiry: "Validity Expiry",
  initial_tariff_revised: "Initial Tariff Revised",
  recommissioned_on: "Recommissioned On",
  ep_expired: "EP Expired",
  gl_expired: "GL Expired",
  voltage_level_kv: "Voltage Level (kV)",
  address_line1: "Address Line 1",
  address_line2: "Address Line 2",
  address_line3: "Address Line 3",
  contact_person: "Contact Person",
  email: "Email",
  gps_coordinates: "GPS Coordinates",
  company_group: "Company Group",
  tendered_or_not: "Tendered or Not",
  reductions: "Reductions",
  agreement_type: "Agreement Type",
  telephone: "Telephone",
  no: "No.",
  status: "Status",
};

const formatHeader = (key) => {
  if (columnLabelMap[key]) return columnLabelMap[key];
  return key
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

export default function DeveloperReport() {
  const [rows, setRows] = useState([]);
  const [loadingPreview, setLoadingPreview] = useState(true);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingCsv, setDownloadingCsv] = useState(false);
  const [previewError, setPreviewError] = useState("");
  const [filters, setFilters] = useState({
    status: "",
    tariffType: "",
    region: "",
    province: "",
    area: "",
  });
  const [filterOptions, setFilterOptions] = useState({
    tariffs: [],
    regions: [],
    provinces: [],
    areas: [],
  });

  const fetchPreview = async () => {
    setLoadingPreview(true);
    setPreviewError("");
    try {
      const data = await getReportPreview(filters);
      setRows(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Preview load error:", error);
      setPreviewError(error.message || "Unable to load report.");
    } finally {
      setLoadingPreview(false);
    }
  };

  useEffect(() => {
    Promise.all([getNcreTariffDescriptions(), getAllRegions()])
      .then(([tariffs, regions]) => {
        setFilterOptions((current) => ({ ...current, tariffs, regions }));
      })
      .catch((error) => console.error("Report filter options load error:", error));
  }, []);

  useEffect(() => {
    let cancelled = false;

    const loadLocationOptions = async () => {
      try {
        let provinces;
        let areas;

        if (!filters.region) {
          [provinces, areas] = await Promise.all([getAllProvinces(), getAllAreas()]);
        } else {
          provinces = await getProvincesByRegion(filters.region);
          areas = filters.province
            ? await getAreasByRegionAndProvince(filters.region, filters.province)
            : await getAreasByRegion(filters.region);
        }

        if (!cancelled) {
          setFilterOptions((current) => ({ ...current, provinces, areas }));
        }
      } catch (error) {
        if (!cancelled) {
          console.error("Report location filter options load error:", error);
          setFilterOptions((current) => ({ ...current, provinces: [], areas: [] }));
        }
      }
    };

    loadLocationOptions();
    return () => {
      cancelled = true;
    };
  }, [filters.region, filters.province]);

  useEffect(() => {
    fetchPreview();
  }, [filters]);

  const updateFilter = (event) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const handleRegionChange = (event) => {
    setFilters((current) => ({
      ...current,
      region: event.target.value,
      province: "",
      area: "",
    }));
  };

  const handleProvinceChange = (event) => {
    setFilters((current) => ({
      ...current,
      province: event.target.value,
      area: "",
    }));
  };

  const handleDownloadPDF = async () => {
    setDownloadingPdf(true);
    try {
      await downloadReportPdf(filters);
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
      await downloadReportCsv(filters);
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
                <h6 className="text-ink-700 text-xl font-bold">Developer Report</h6>
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
            <div className="mb-4 flex flex-wrap items-end gap-3">
              <label className="flex min-w-[150px] flex-1 flex-col text-xs font-semibold text-ink-600">
                Status
                <select name="status" value={filters.status} onChange={updateFilter} className="ds-input mt-1">
                  <option value="">All Statuses</option>
                  <option value="Active">Active</option>
                  <option value="Inactive">Inactive</option>
                </select>
              </label>
              <label className="flex min-w-[180px] flex-1 flex-col text-xs font-semibold text-ink-600">
                Tariff Type
                <select name="tariffType" value={filters.tariffType} onChange={updateFilter} className="ds-input mt-1">
                  <option value="">All Tariffs</option>
                  {filterOptions.tariffs.map((tariff) => <option key={tariff} value={tariff}>{tariff}</option>)}
                </select>
              </label>
              <label className="flex min-w-[130px] flex-1 flex-col text-xs font-semibold text-ink-600">
                Region
                <select name="region" value={filters.region} onChange={handleRegionChange} className="ds-input mt-1">
                  <option value="">All Regions</option>
                  {filterOptions.regions.map((region) => <option key={region} value={region}>{region}</option>)}
                </select>
              </label>
              <label className="flex min-w-[170px] flex-1 flex-col text-xs font-semibold text-ink-600">
                Province
                <select name="province" value={filters.province} onChange={handleProvinceChange} className="ds-input mt-1">
                  <option value="">All Provinces</option>
                  {filterOptions.provinces.map((province) => <option key={province.provCode || province.prov_code} value={province.provCode || province.prov_code}>{province.provName || province.prov_name}</option>)}
                </select>
              </label>
              <label className="flex min-w-[170px] flex-1 flex-col text-xs font-semibold text-ink-600">
                Area
                <select name="area" value={filters.area} onChange={updateFilter} className="ds-input mt-1">
                  <option value="">All Areas</option>
                  {filterOptions.areas.map((area) => <option key={area.areaCode || area.area_code} value={area.areaName || area.area_name}>{area.areaName || area.area_name}</option>)}
                </select>
              </label>
              <button type="button" onClick={() => setFilters({ status: "", tariffType: "", region: "", province: "", area: "" })} className="mb-0.5 rounded border border-ink-300 px-3 py-2 text-sm font-medium text-ink-600 hover:bg-ink-50">Clear</button>
            </div>
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
                          No developer report data available.
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
                              {key === "status" ? (
                                <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${String(row[key]).toLowerCase() === "active" ? "bg-success-100 text-success-800" : "bg-critical-100 text-critical-800"}`}>
                                  {row[key]}
                                </span>
                              ) : row[key] !== null && row[key] !== undefined
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
