import React, { useEffect, useState, useCallback } from "react";
import { toast } from "react-toastify";
import { useAreaAndBill } from "../../context/AreaAndBillContext"; // Adjust path if needed (e.g., ../contexts/AreaBillContext.js)
import debounce from "lodash/debounce"; // Add lodash for debounce (npm install lodash)
export default function NewCommission({ color }) {
  const [customers, setCustomers] = useState([]);
  const [filteredCustomers, setFilteredCustomers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [customerDetails, setCustomerDetails] = useState({});
  const [meterDetails, setMeterDetails] = useState({});
  const [inputValues, setInputValues] = useState({});
  const { data: areaBillData } = useAreaAndBill(); // Added: Get area/bill data for dynamic billCycle
  const baseUrl = process.env.REACT_APP_API_BASE_URL || ""; // Fallback to "" for relative if not set
  const dotnetBaseUrl = "http://10.128.1.59:5005"; // Direct base URL for .NET API
  // Helper: Make fetch with timeout and auth for Spring Boot
  const apiFetch = useCallback(
    async (url, options = {}) => {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 30000); // 30s timeout
      try {
        const response = await fetch(`${baseUrl}${url}`, {
          ...options,
          signal: controller.signal,
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
            ...options.headers,
          },
        });
        clearTimeout(timeoutId);
        if (response.status === 504) {
          throw new Error(
            "Gateway timeout - Check backend server connection (e.g., 10.128.1.59:5005)"
          );
        }
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Failed: ${response.status} - ${errorText}`);
        }
        return response;
      } catch (err) {
        clearTimeout(timeoutId);
        if (err.name === "AbortError") {
          throw new Error(
            "Request timed out after 30 seconds - Backend may be slow"
          );
        }
        throw err;
      }
    },
    [baseUrl]
  );
  // Toggle row expansion
  const toggleRowExpansion = async (jobNbr) => {
    const newExpandedRows = new Set(expandedRows);
    if (newExpandedRows.has(jobNbr)) {
      newExpandedRows.delete(jobNbr);
    } else {
      newExpandedRows.add(jobNbr);
    }
    setExpandedRows(newExpandedRows);
  };
  // Handle input changes (no validation or submission)
  const handleInputChange = (jobNbr, field, value) => {
    setInputValues((prev) => ({
      ...prev,
      [jobNbr]: {
        ...prev[jobNbr],
        [field]: value,
      },
    }));
  };
  // Fetch customer list
  const fetchCustomerDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const selectedAreaCd = areaBillData?.selectedAreaCode;
      if (!selectedAreaCd) {
        throw new Error("No area code selected. Please select an area.");
      }
      // Fetch customer list from .NET API
      const listUrl = `${dotnetBaseUrl}/api/Customer/GetCustomerDetailsToAccountGeneration?areaCode=${selectedAreaCd}`;
      const listResponse = await fetch(listUrl, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (!listResponse.ok) {
        throw new Error(
          `HTTP ${listResponse.status}: ${listResponse.statusText}`
        );
      }
      const listData = await listResponse.json();
      if (!listData.success) {
        throw new Error(listData.message || "Failed to fetch customer list");
      }
      // Fetch walk sequence defaults from .NET API
      const walkUrl = `${dotnetBaseUrl}/api/Customer/GetWalkSequenceDetails?AreaCode=${selectedAreaCd}`;
      const walkResponse = await fetch(walkUrl, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (!walkResponse.ok) {
        throw new Error(
          `HTTP ${walkResponse.status}: ${walkResponse.statusText}`
        );
      }
      const walkData = await walkResponse.json();
      if (!walkData.isSuccess) {
        throw new Error(
          walkData.message || "Failed to fetch walk sequence details"
        );
      }
      const defaults = {
        readerCode: walkData.readerCode || "",
        dailyPack: walkData.dailyPack || "",
        walkOrder: walkData.walkOrder || "",
      };
      const transformedData = listData.data.map((item) => ({
        jobNbr: item.jobNbr,
        name: item.name,
        telNbr: "", // Not provided in new API, set to empty
        addressL1: item.addressL1,
        addressL2: item.addressL2,
        city: item.city,
        cntrDmnd: 0, // Not provided in new API, default to 0
      }));
      // Set initial input values with defaults for all customers
      const initialInputValues = transformedData.reduce((acc, cust) => {
        acc[cust.jobNbr] = { ...defaults };
        return acc;
      }, {});
      setInputValues(initialInputValues);
      setCustomers(transformedData);
      setFilteredCustomers(transformedData);
      // Fetch customer and meter details from .NET API
      const detailsUrl = `${dotnetBaseUrl}/api/Customer/GetTmpCustomerNMtrDetailsAccGen?AreaCode=${selectedAreaCd}`;
      const detailsResponse = await fetch(detailsUrl, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Basic " + btoa("user:admin123"),
        },
      });
      if (!detailsResponse.ok) {
        throw new Error(
          `HTTP ${detailsResponse.status}: ${detailsResponse.statusText}`
        );
      }
      const detailsData = await detailsResponse.json();
      if (!detailsData.isSuccess) {
        throw new Error(
          detailsData.msg || "Failed to fetch customer and meter details"
        );
      }
      const customerDetailsMap = {};
      const meterDetailsMap = {};
      detailsData.customerWiithMeterList.forEach((item) => {
        const cust = item.customer;
        const jobNbr = cust.jobNbr;
        const mappedCust = {
          ...cust,
          loanAmt: parseFloat(cust.loanAmount) || 0,
          estAmnt: parseFloat(cust.estAmnt) || 0,
          depositAmt: parseFloat(cust.depositAmt) || 0,
          totSecDep: parseFloat(cust.totSecDep) || 0,
          noOfPhases: parseInt(cust.noOfPhases, 10) || 0,
          lnStatus: cust.lnStatus === "1" ? "Y" : "N",
          gstApl: cust.taxNum ? "Y" : "N",
          idType: cust.idType.toUpperCase(),
        };
        customerDetailsMap[jobNbr] = mappedCust;
        meterDetailsMap[jobNbr] = item.meterTempList.map((m) => ({
          meterType: m.mtrType,
          presentReading: parseInt(m.prsntRdn, 10) || 0,
          multiplicationFactor: parseFloat(m.mFactor) || 0,
          ctRatio: m.ctRatio,
          meterRatio: m.mtrRatio,
          meterNumber: m.mtrNbr,
        }));
      });
      setCustomerDetails(customerDetailsMap);
      setMeterDetails(meterDetailsMap);
      toast.success("Customer data loaded successfully!");
    } catch (error) {
      console.error("Error fetching customer details:", error);
      setError(error.message);
      toast.error(`Failed to load customer data: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };
  // Debounced search
  const debouncedSearch = useCallback(
    debounce((term) => {
      let result = customers;
      if (term) {
        result = result.filter((c) =>
          [c.jobNbr, c.name, c.telNbr, c.addressL1, c.addressL2, c.city].some(
            (val) =>
              val && val.toString().toLowerCase().includes(term.toLowerCase())
          )
        );
      }
      setFilteredCustomers(result);
      setCurrentPage(1);
    }, 300),
    [customers]
  );
  useEffect(() => {
    if (areaBillData?.selectedAreaCode) {
      fetchCustomerDetails();
    }
  }, [areaBillData?.selectedAreaCode]);
  useEffect(() => {
    debouncedSearch(searchTerm);
  }, [searchTerm, debouncedSearch]);
  const totalPages = Math.ceil(filteredCustomers.length / rowsPerPage);
  const currentData = filteredCustomers.slice(
    (currentPage - 1) * rowsPerPage,
    currentPage * rowsPerPage
  );
  // Updated: Generate account number by calling .NET API
  const handleGenerateAccountNumber = async (customer) => {
    const areaCd = areaBillData?.selectedAreaCode;
    if (!areaCd) {
      toast.error("No area selected");
      return;
    }
    const billCycle =
      areaBillData?.billCycles?.find((cycle) => cycle.area_code === areaCd)
        ?.active_bill_cycle || 0;
    const userId = sessionStorage.getItem("user_id") || "defaultUser";
    const tariff = customerDetails[customer.jobNbr]?.tariff || "";
    const currentInputs = inputValues[customer.jobNbr] || {};
    const readerCode = currentInputs.readerCode || "";
    const dailyPack = currentInputs.dailyPack || "";
    const walkOrder = currentInputs.walkOrder || "";
    // Validation for the three fields
    const fieldsToValidate = [
      { key: "readerCode", name: "Reader Code" },
      { key: "dailyPack", name: "Daily Pack" },
      { key: "walkOrder", name: "Walk Order" },
    ];
    for (const { key, name } of fieldsToValidate) {
      const val = currentInputs[key] || "";
      if (val && isNaN(Number(val))) {
        toast.error(
          `${name} must be a valid number (integer or decimal, e.g., 123 or 123.45). Current value: "${val}"`
        );
        return;
      }
    }
    // Construct body for new API
    const body = {
      areaCode: areaCd,
      jobNumber: customer.jobNbr,
      billCycle: billCycle.toString(),
      readerCode: readerCode,
      dailyPack: dailyPack,
      walkOrder: walkOrder,
      userId: userId,
      tarriff: tariff,
    };
    try {
      toast.info(`Generating account number for ${customer.name}...`);
      console.log(
        "Sending request to .NET API with body:",
        JSON.stringify(body, null, 2)
      );
      // Use direct .NET API path for new endpoint
      const response = await fetch(
        `${dotnetBaseUrl}/api/Customer/GetCustomerOnBoard`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("user:admin123"),
          },
          body: JSON.stringify(body),
        }
      );
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      const result = await response.json();
      const generatedAcc = result.generatedAccNo?.result || "Unknown";
      toast.success(
        `Account generated successfully! New Acc No: ${generatedAcc}`
      );
      // Reload customer list (generated accounts should now have acc_nbr and be filtered out)
      await fetchCustomerDetails();
      // Also clear the expanded row if it's open
      const newExpandedRows = new Set(expandedRows);
      newExpandedRows.delete(customer.jobNbr);
      setExpandedRows(newExpandedRows);
    } catch (error) {
      console.error("Full error details for account generation:", error);
      toast.error(`Failed to generate account: ${error.message}`);
    }
  };
  // Customer Details Section Component
  const CustomerDetailsSection = ({ customerData }) => {
    if (!customerData) return <div>Loading customer details...</div>;
    const categoryMap = {
      B: "Bulk",
      O: "Ordinary",
    };
    const natSupMap = {
      C: "Construction",
      P: "Permanent",
    };
    const idTypeMap = {
      N: "NIC",
    };
    const showLoanFields = customerData.lnStatus === "Y";
    return (
      <div className="bg-ink-50 rounded-lg p-4 mb-2">
        <h4 className="font-semibold text-lg mb-3 text-brandred border-b pb-2">
          <i className="fas fa-user mr-2"></i>Customer Information
        </h4>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="ds-label-field">
              Customer Category
            </label>
            <div className="p-2 bg-white border rounded">
              {categoryMap[customerData.cusCat] || customerData.cusCat || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Nature of Supplier
            </label>
            <div className="p-2 bg-white border rounded">
              {natSupMap[customerData.natSup] || customerData.natSup || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Id Number
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.idNbr || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Type of Identification
            </label>
            <div className="p-2 bg-white border rounded">
              {idTypeMap[customerData.idType] || customerData.idType || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Loan
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.lnStatus === "Y" ? "YES" : "NO"}
            </div>
          </div>
          {/* New Loan Fields - Only show if lnStatus is Y */}
          {showLoanFields && (
            <>
              <div>
                <label className="ds-label-field">
                  Loan Amount
                </label>
                <div className="p-2 bg-white border rounded">
                  {customerData.loanAmt
                    ? `${customerData.loanAmt.toFixed(2)}`
                    : "N/A"}
                </div>
              </div>
              <div>
                <label className="ds-label-field">
                  Loan Type
                </label>
                <div className="p-2 bg-white border rounded">
                  {customerData.loanType || "N/A"}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    );
  };
  // Job Details Section Component
  const JobDetailsSection = ({ customerData, meterData }) => {
    if (!customerData) return <div>Loading job details...</div>;
    return (
      <div className="bg-ink-50 rounded-lg p-4 mb-2">
        <h4 className="font-semibold text-lg mb-3 text-brandred border-b pb-2">
          <i className="fas fa-briefcase mr-2"></i>Job Information
        </h4>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label className="ds-label-field">
              Est PIV Number
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.estPivNbr || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Est Amount
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.estAmnt
                ? `$${customerData.estAmnt.toFixed(2)}`
                : "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Est pay Date
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.espayDt || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Dep PIV Number
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.depPivNbr || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Deposit Amount
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.depositAmt
                ? customerData.depositAmt.toFixed(2)
                : "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Deposit Date
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.depDate || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Tariff
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.tariff || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Total Sec Deposit
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.totSecDep
                ? customerData.totSecDep.toFixed(2)
                : "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Connection Date
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.cnectDate || "N/A"}
            </div>
          </div>
          <div>
            <label className="ds-label-field">
              Number of Phase
            </label>
            <div className="p-2 bg-white border rounded">
              {customerData.noOfPhases === 1
                ? "Single Phase"
                : customerData.noOfPhases === 3
                ? "Three Phase"
                : customerData.noOfPhases || "N/A"}
            </div>
          </div>
          {customerData.gstApl === "Y" && (
            <div>
              <label className="ds-label-field">
                Tax Number
              </label>
              <div className="p-2 bg-white border rounded">
                {customerData.taxNum || "N/A"}
              </div>
            </div>
          )}
        </div>
      </div>
    );
  };
  // Meter Details Section Component
  const MeterDetailsSection = ({ meterData }) => {
    if (!meterData) return <div>Loading meter details...</div>;
    // Get unique meter numbers from the meter data
    const meterNumbers = [
      ...new Set(meterData.map((meter) => meter.meterNumber).filter(Boolean)),
    ];
    return (
      <div className="bg-ink-50 rounded-lg p-4">
        <h4 className="font-semibold text-lg mb-3 text-brandred border-b pb-2">
          <i className="fas fa-tachometer-alt mr-2"></i>Meter Information
        </h4>
        {/* Meter Numbers Display */}
        {meterNumbers.length > 0 && (
          <div className="mb-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="lg:col-span-2">
                <label className="ds-label-field">
                  Meter Number(s)
                </label>
                <div className="p-2 bg-white border rounded">
                  {meterNumbers.join(", ") || "N/A"}
                </div>
              </div>
            </div>
          </div>
        )}
        <div className="overflow-x-auto">
          <table className="w-full text-sm border-collapse bg-white rounded-lg overflow-hidden shadow-sm">
            <thead>
              <tr className="bg-brandred text-white">
                <th className="p-3 text-center font-medium whitespace-nowrap">
                  Meter Type
                </th>
                <th className="p-3 text-center font-medium whitespace-nowrap">
                  Initial Reading
                </th>
                <th className="p-3 text-center font-medium whitespace-nowrap">
                  Multiplication Factor
                </th>
                <th className="p-3 text-center font-medium whitespace-nowrap">
                  CT Ratio
                </th>
                <th className="p-3 text-center font-medium whitespace-nowrap">
                  Meter Ratio
                </th>
              </tr>
            </thead>
            <tbody>
              {meterData.map((meter, index) => (
                <tr
                  key={`${meter.accountNumber}-${index}`}
                  className={index % 2 === 0 ? "bg-white" : "bg-ink-50"}
                >
                  <td className="p-3 border-b border-ink-200">
                    <div className="p-2 border border-ink-300 rounded bg-ink-100 min-h-[2.5rem] flex items-center justify-end">
                      {meter.meterType || "N/A"}
                    </div>
                  </td>
                  <td className="p-3 border-b border-ink-200">
                    <div className="p-2 border border-ink-300 rounded bg-ink-100 min-h-[2.5rem] flex items-center justify-end">
                      {meter.presentReading !== null &&
                      meter.presentReading !== undefined
                        ? meter.presentReading
                        : "N/A"}
                    </div>
                  </td>
                  <td className="p-3 border-b border-ink-200">
                    <div className="p-2 border border-ink-300 rounded bg-ink-100 min-h-[2.5rem] flex items-center justify-end">
                      {meter.multiplicationFactor !== null &&
                      meter.multiplicationFactor !== undefined
                        ? meter.multiplicationFactor
                        : "N/A"}
                    </div>
                  </td>
                  <td className="p-3 border-b border-ink-200">
                    <div className="p-2 border border-ink-300 rounded bg-ink-100 min-h-[2.5rem] flex items-center justify-center">
                      {meter.ctRatio || "N/A"}
                    </div>
                  </td>
                  <td className="p-3 border-b border-ink-200">
                    <div className="p-2 border border-ink-300 rounded bg-ink-100 min-h-[2.5rem] flex items-center justify-center">
                      {meter.meterRatio || "N/A"}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };
  if (loading) {
    return (
      <div className="flex flex-col min-h-screen">
        <div className="w-full max-w-[92rem] px-6 mx-auto">
          <div className="relative flex flex-col bg-white w-full mb-6 shadow-lg rounded-md px-6 mt-6">
            <div className="flex flex-col justify-center items-center py-10">
              <div className="text-ink-500 text-base mb-4">
                Loading customer data...
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
  if (error) {
    return (
      <div className="flex flex-col min-h-screen">
        <div className="w-full max-w-[92rem] px-6 mx-auto">
          <div className="relative flex flex-col bg-white w-full mb-6 shadow-lg rounded-md px-6 mt-6">
            <div className="flex flex-col justify-center items-center py-10">
              <div className="text-ink-500 text-base mb-4">Error: {error}</div>
              <button
                onClick={fetchCustomerDetails}
                className="bg-brandred text-white text-sm px-4 py-2 rounded-md transition-opacity hover:opacity-90 border-none cursor-pointer"
              >
                Retry
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
  return (
    <div className="flex flex-col min-h-screen">
      <div className="w-full max-w-[92rem] px-6 mx-auto">
        <div className="relative flex flex-col bg-white w-full mb-6 shadow-lg rounded-md px-6 mt-6">
          <div className="flex justify-between items-center px-6 mt-6 mb-4">
            <h3 className="ds-section-title">Customer Onboarding</h3>
          </div>
          {/* Filters and Search */}
          <div className="flex justify-between items-center px-6 mb-4 md:flex-row flex-col gap-4 items-stretch">
            <div className="flex items-center">
              <div className="flex items-center gap-2">
                <label className="text-sm">Show</label>
                <select
                  value={rowsPerPage}
                  onChange={(e) => setRowsPerPage(Number(e.target.value))}
                  className="border border-ink-300 rounded-md px-2 py-1 text-sm w-24"
                >
                  {[5, 10, 20, 50].map((num) => (
                    <option key={num} value={num}>
                      {num}
                    </option>
                  ))}
                </select>
                <label className="text-sm">entries</label>
              </div>
            </div>
            <input
              type="text"
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="border border-ink-300 rounded-md px-3 py-1 text-sm"
            />
          </div>
          {/* Table */}
          <div className="overflow-x-auto max-h-[600px] overflow-y-auto">
            <table className="w-full text-sm border-collapse min-w-[1000px] md:min-w-full">
              <thead>
                <tr className="text-left bg-brandred">
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap w-10">
                    {/* Expand column */}
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Job Number
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Customer Name
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Address
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap text-center">
                    Cntr Dmnd(Kvh)
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Reader Code
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Daily Pack
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Walk Order
                  </th>
                  <th className="p-3 text-white font-medium sticky top-0 z-10 whitespace-nowrap">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody>
                {currentData.length > 0 ? (
                  currentData.map((customer) => {
                    const currentInputs = inputValues[customer.jobNbr] || {};
                    const isExpanded = expandedRows.has(customer.jobNbr);
                    const customerDetailData = customerDetails[customer.jobNbr];
                    const meterDetailData = meterDetails[customer.jobNbr];
                    return (
                      <React.Fragment key={customer.jobNbr}>
                        <tr className="ds-tr">
                          <td className="p-3 text-center">
                            <button
                              onClick={() =>
                                toggleRowExpansion(customer.jobNbr)
                              }
                              className="text-brandred hover:text-critical-700 transition-colors focus:outline-none"
                            >
                              <i
                                className={`fas fa-chevron-${
                                  isExpanded ? "down" : "right"
                                } transition-transform`}
                              ></i>
                            </button>
                          </td>
                          <td className="p-3">{customer.jobNbr}</td>
                          <td className="p-3">
                            {customer.name}
                            {customer.telNbr && ` (${customer.telNbr})`}
                          </td>
                          <td className="p-3">
                            {`${customer.addressL1 || ""} ${
                              customer.addressL2 || ""
                            } ${customer.city || ""}`.trim()}
                          </td>
                          <td className="p-3 text-center">
                            {customer.cntrDmnd
                              ? Number.isInteger(customer.cntrDmnd)
                                ? customer.cntrDmnd.toString()
                                : customer.cntrDmnd.toFixed(2)
                              : "0"}
                          </td>
                          <td className="p-3">
                            <input
                              type="text"
                              placeholder="Reader Code"
                              value={currentInputs.readerCode || ""}
                              onChange={(e) =>
                                handleInputChange(
                                  customer.jobNbr,
                                  "readerCode",
                                  e.target.value
                                )
                              }
                              className="w-full p-1 border border-ink-300 rounded-md text-sm"
                            />
                          </td>
                          <td className="p-3">
                            <input
                              type="text"
                              placeholder="Daily Pack"
                              value={currentInputs.dailyPack || ""}
                              onChange={(e) =>
                                handleInputChange(
                                  customer.jobNbr,
                                  "dailyPack",
                                  e.target.value
                                )
                              }
                              className="w-full p-1 border border-ink-300 rounded-md text-sm"
                            />
                          </td>
                          <td className="p-3">
                            <input
                              type="text"
                              placeholder="Walk Order"
                              value={currentInputs.walkOrder || ""}
                              onChange={(e) =>
                                handleInputChange(
                                  customer.jobNbr,
                                  "walkOrder",
                                  e.target.value
                                )
                              }
                              className="w-full p-1 border border-ink-300 rounded-md text-sm"
                            />
                          </td>
                          <td className="p-3">
                            <div className="flex gap-1 flex-nowrap">
                              <button
                                className="px-2 py-1 rounded-md text-white text-xs font-medium bg-success-600 hover:bg-success-700 border-none cursor-pointer"
                                onClick={() => {
                                  const confirmGeneration = () => {
                                    toast.dismiss();
                                    handleGenerateAccountNumber(customer);
                                  };
                                  const cancelGeneration = () => {
                                    toast.dismiss();
                                  };
                                  toast(
                                    <div className="flex flex-col space-y-2">
                                      <p className="text-sm">
                                        Are you sure you want to generate an
                                        account number for Job Number:{" "}
                                        <strong>{customer.jobNbr}</strong>?
                                      </p>
                                      <div className="flex gap-2 justify-end">
                                        <button
                                          onClick={cancelGeneration}
                                          className="px-3 py-1 rounded-md text-white text-xs font-medium bg-ink-500 hover:bg-ink-600 border-none cursor-pointer"
                                        >
                                          Cancel
                                        </button>
                                        <button
                                          onClick={confirmGeneration}
                                          className="px-3 py-1 rounded-md text-white text-xs font-medium bg-success-600 hover:bg-success-700 border-none cursor-pointer"
                                        >
                                          Generate
                                        </button>
                                      </div>
                                    </div>,
                                    {
                                      position: "top-center",
                                      autoClose: false,
                                      closeOnClick: false,
                                      closeButton: false,
                                      draggable: false,
                                    }
                                  );
                                }}
                              >
                                Generate Account Number
                              </button>
                            </div>
                          </td>
                        </tr>
                        {/* Expanded Row Content */}
                        {isExpanded && (
                          <tr className="bg-ink-50">
                            <td colSpan="9" className="p-4">
                              <div className="space-y-2">
                                <CustomerDetailsSection
                                  customerData={customerDetailData}
                                />
                                <JobDetailsSection
                                  customerData={customerDetailData}
                                  meterData={meterDetailData}
                                />
                                <MeterDetailsSection
                                  meterData={meterDetailData}
                                />
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })
                ) : (
                  <tr>
                    <td colSpan="9" className="text-center py-6 text-ink-500">
                      No customer data found
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          {/* Pagination */}
          <div className="flex justify-between items-center px-6 py-4">
            <div className="text-sm text-ink-700">
              Showing{" "}
              {filteredCustomers.length === 0
                ? 0
                : (currentPage - 1) * rowsPerPage + 1}{" "}
              to {Math.min(currentPage * rowsPerPage, filteredCustomers.length)}{" "}
              of {filteredCustomers.length} entries
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="ds-btn ds-btn-primary ds-btn-sm"
              >
                Previous
              </button>
              <span className="px-3 py-1 bg-ink-100 rounded-md text-sm">
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={() =>
                  setCurrentPage((prev) => Math.min(prev + 1, totalPages))
                }
                disabled={currentPage === totalPages || totalPages === 0}
                className="ds-btn ds-btn-primary ds-btn-sm"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
