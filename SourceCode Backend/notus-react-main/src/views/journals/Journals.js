import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import journalsService from "../../services/journalsService";
import { getSelectedAreaCode, getBillCycleForArea } from "../../services/AreaAndBillService";
import { getDeveloperBySearch } from "../../services/developerRegistrationService";

export default function Journals() {
    const [journals, setJournals] = useState([]);
    const [filteredJournals, setFilteredJournals] = useState([]); // New state for filtered journals
    const [loading, setLoading] = useState(true);
    const [selectedJournal, setSelectedJournal] = useState(null);
    const [expandedRowId, setExpandedRowId] = useState(null); // Track which row is expanded using composite key
    const [loadingDetails, setLoadingDetails] = useState(false);
    const [backendStatus, setBackendStatus] = useState('checking');
    const [connectionError, setConnectionError] = useState(null);
    const [userCategory, setUserCategory] = useState(null); // User category from session

    // Modal state for Edit button
    const [modalJournal, setModalJournal] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [loadingModal, setLoadingModal] = useState(false);
    const [editedAdjustAmt, setEditedAdjustAmt] = useState('');
    const [updatingModal, setUpdatingModal] = useState(false);

    // Pagination state
    const [currentPage, setCurrentPage] = useState(1);
    const [showOption, setShowOption] = useState("10");
    const [displayedJournals, setDisplayedJournals] = useState([]);

    // Journal type filter state
    const [journalTypes, setJournalTypes] = useState([]);
    const [selectedJournalType, setSelectedJournalType] = useState(""); // Empty string means "All"

    // Approved by users state
    const [approvedByUsers, setApprovedByUsers] = useState([]);

    // ===== ADD JOURNAL modal state =====
    const [showAddModal, setShowAddModal] = useState(false);
    const [addingJournal, setAddingJournal] = useState(false);
    const [field1Label, setField1Label] = useState('Field 1'); // Dynamic label
    const [field2Label, setField2Label] = useState('Field 2'); // Dynamic label for paired journal
    const [pairedJnlType, setPairedJnlType] = useState(''); // Store paired journal type code
    const [newJournal, setNewJournal] = useState({
        accNbr: '',
        folioNo: '',
        areaCd: '',
        currentBillCycle: '',
        jnlType: '',
        jnlNo: '',
        jnlDate: '',
        field1: '',
        field1Type: 'DEBIT',
        field2: '',
        field2Type: 'DEBIT',
        totalAmt: '',
        totalAmtType: 'DEBIT',
        approvedBy: '',
        name: '',
        docAttch: 'N',
        individuallyConfirmed: false,
    });

    // Records per page mapping
    const recordsPerPageMap = {
        10: 10,
        20: 20,
        30: 30,
        40: 40,
    };

    // Function to generate unique row ID using multiple fields
    const getRowId = (journal) => {
        return `${journal.accNbr || 'NA'}_${journal.jnlType || 'NA'}_${journal.jnlNo || 'NA'}`;
    };

    // Filter journals by selected journal type
    useEffect(() => {
        if (selectedJournalType === "") {
            // Show all journals
            setFilteredJournals(journals);
        } else {
            // Filter by selected journal type
            const filtered = journals.filter(j => j.jnlType === selectedJournalType);
            setFilteredJournals(filtered);
        }
        // Reset to page 1 when filter changes
        setCurrentPage(1);
        setExpandedRowId(null);
        setSelectedJournal(null);
    }, [selectedJournalType, journals]);

    // Update displayed journals based on pagination
    useEffect(() => {
        const recordsPerPage = recordsPerPageMap[showOption] || 10;
        const startIndex = (currentPage - 1) * recordsPerPage;
        const endIndex = startIndex + recordsPerPage;
        setDisplayedJournals(filteredJournals.slice(startIndex, endIndex));
    }, [filteredJournals, currentPage, showOption]);

    // Test backend connection on component mount
    useEffect(() => {
        testBackendConnection();
        fetchJournalTypes();
        fetchApprovedByUsers();
    }, []);

    // Load user category from session storage
    useEffect(() => {
        const category = sessionStorage.getItem('user_category');
        setUserCategory(category);
    }, []);

    // Auto-calculate total amount from field1 and field2
    useEffect(() => {
        const field1Amount = parseFloat(newJournal.field1) || 0;
        const field2Amount = parseFloat(newJournal.field2) || 0;
        const calculatedTotal = field1Amount + field2Amount;

        // Update total amount with calculated sum (user can still edit it manually)
        if (calculatedTotal >= 0) {
            setNewJournal(prev => ({
                ...prev,
                totalAmt: calculatedTotal > 0 ? calculatedTotal.toFixed(2) : ''
            }));
        }
    }, [newJournal.field1, newJournal.field2]);

    const fetchJournalTypes = async () => {
        try {
            console.log("Fetching journal types...");
            const response = await journalsService.getAllJournalTypes();
            console.log("Journal types API response:", response);

            if (response && response.success && Array.isArray(response.data)) {
                setJournalTypes(response.data);
                console.log(`Loaded ${response.data.length} journal types`);
            } else {
                console.warn("No journal types found");
                setJournalTypes([]);
            }
        } catch (error) {
            console.error("Error fetching journal types:", error);
            // Don't show error toast, just log it
            setJournalTypes([]);
        }
    };

    const fetchApprovedByUsers = async () => {
        try {
            console.log("Fetching approved by users...");
            const response = await journalsService.getAllJurnlAuth();
            console.log("Jurnl_auth API response:", response);

            if (response && response.success && Array.isArray(response.data)) {
                setApprovedByUsers(response.data);
                console.log(`Loaded ${response.data.length} approved by users`);
            } else {
                console.warn("No approved by users found");
                setApprovedByUsers([]);
            }
        } catch (error) {
            console.error("Error fetching approved by users:", error);
            // Don't show error toast, just log it
            setApprovedByUsers([]);
        }
    };

    const testBackendConnection = async () => {
        try {
            console.log('Testing backend connection...');
            setConnectionError(null);
            const result = await journalsService.testConnection();

            if (result.success) {
                setBackendStatus('online');
                fetchJournals();
            } else {
                setBackendStatus('offline');
                setConnectionError(result.error || 'Backend not available');
                toast.error(` Backend connection failed: ${result.error}`);
            }
        } catch (error) {
            setBackendStatus('error');
            setConnectionError(error.message);
            console.error('Connection test error:', error);
            toast.error(` Backend connection test error: ${error.message}`);
        }
    };

    const fetchJournals = async () => {
        try {
            setLoading(true);
            setConnectionError(null);
            console.log("Starting to fetch journals...");

            const response = await journalsService.getAllJournals();
            console.log("Journals API response:", response);

            let journalsList = [];
            let message = "";

            if (response && response.success !== undefined) {
                if (response.success && Array.isArray(response.data)) {
                    journalsList = response.data;
                    message = response.message || `Found ${response.data.length} journals`;
                } else if (!response.success && response.message) {
                    toast.warning(response.message);
                    message = response.message;
                }
            }

            setJournals(journalsList);
            setCurrentPage(1);
            setSelectedJournal(null);
            setExpandedRowId(null);
            // Reset filter when refreshing
            setSelectedJournalType("");

            if (journalsList.length === 0) {
                toast.info(message || "No journals found with area_cd='27'");
            } else {
                toast.success(message || "Journals loaded successfully");
            }

        } catch (error) {
            console.error("Error fetching journals:", error);

            let errorMessage = "Failed to fetch journals";
            let details = "";

            if (error.code === 'ECONNABORTED') {
                errorMessage = "Backend server not responding";
                details = "Spring Boot application might not be running. Please start the backend server.";
                setConnectionError(details);
            } else if (error.code === 'ERR_NETWORK') {
                errorMessage = "Network connection error";
                details = "Cannot connect to backend. Check if backend is running on http://localhost:8080";
                setConnectionError(details);
            } else if (error.response) {
                const status = error.response.status;
                const data = error.response.data;

                if (status === 404) {
                    errorMessage = data?.message || "No journals found";
                    setConnectionError(null);
                } else if (status === 500) {
                    errorMessage = "Server error";
                    details = data?.message || "Internal server error occurred";
                    setConnectionError(details);
                }
            }

            toast.error(
                <div>
                    <strong>{errorMessage}</strong>
                    {details && <div className="text-sm mt-1">{details}</div>}
                </div>
            );

            setJournals([]);

        } finally {
            setLoading(false);
        }
    };

    const handleViewDetails = async (journal) => {
        try {
            if (!journal || !journal.jnlNo) {
                toast.error("Invalid journal data");
                return;
            }

            const currentRowId = getRowId(journal);

            // If clicking on the already expanded row, collapse it
            if (expandedRowId === currentRowId) {
                setExpandedRowId(null);
                setSelectedJournal(null);
                return;
            }

            setLoadingDetails(true);
            console.log(`Fetching details for journal ${journal.jnlNo}...`);

            const response = await journalsService.getJournalDetail(journal);

            console.log("Complete API response:", response);

            let details = null;
            if (response && response.success) {
                details = response.data;

                if (details) {
                    console.log("Journal details received:", details);
                    setSelectedJournal(details);
                    setExpandedRowId(currentRowId); // Set using composite key
                    toast.success("Journal details loaded");
                }
            } else if (response && response.success === false) {
                toast.error(response.message || "Journal not found");
                return;
            }

            if (!details) {
                toast.error(`Journal ${journal.jnlNo} not found or area_cd is not '27'`);
            }

        } catch (error) {
            console.error('Error fetching journal details:', error);

            if (error.response) {
                console.error('Error response:', {
                    status: error.response.status,
                    data: error.response.data,
                    headers: error.response.headers
                });
            }

            let errorMessage = `Failed to fetch journal ${journal?.jnlNo} details`;

            if (error.response) {
                const status = error.response.status;
                const data = error.response.data;

                if (status === 404) {
                    errorMessage = data?.message || `Journal ${journal.jnlNo} not found`;
                } else if (status === 500) {
                    errorMessage = data?.message || "Server error while fetching journal details";
                    console.error('Server error details:', data);
                } else {
                    errorMessage = data?.message || `Error ${status}: Failed to fetch journal details`;
                }
            } else if (error.code === 'ERR_NETWORK') {
                errorMessage = "Network error. Cannot connect to backend server.";
            } else {
                errorMessage = error.userMessage || error.message || "Unknown error";
            }

            toast.error(errorMessage);

        } finally {
            setLoadingDetails(false);
        }
    };

    const handleEditDetails = async (journal) => {
        try {
            if (!journal || !journal.jnlNo) {
                toast.error("Invalid journal data");
                return;
            }
            setLoadingModal(true);
            setShowModal(true);
            setModalJournal(null);

            const response = await journalsService.getJournalDetail(journal);

            if (response && response.success && response.data) {
                setModalJournal(response.data);
                setEditedAdjustAmt(response.data.adjustAmt ?? '');
            } else {
                toast.error(response?.message || `Journal ${journal.jnlNo} not found`);
                setShowModal(false);
            }
        } catch (error) {
            console.error('Error fetching journal details for modal:', error);
            toast.error(error?.message || "Failed to load journal details");
            setShowModal(false);
        } finally {
            setLoadingModal(false);
        }
    };

    const closeModal = () => {
        setShowModal(false);
        setModalJournal(null);
        setEditedAdjustAmt('');
        setUpdatingModal(false);
    };

    const handleUpdate = async () => {
        if (!modalJournal) return;
        const parsedAmt = parseFloat(editedAdjustAmt);
        if (isNaN(parsedAmt)) {
            toast.error("Please enter a valid Adjustment Amount");
            return;
        }
        try {
            setUpdatingModal(true);
            const updatedJournal = { ...modalJournal, adjustAmt: parsedAmt };
            const response = await journalsService.updateJournal(updatedJournal);
            if (response && response.success) {
                toast.success("Journal updated successfully");
                // Refresh the table list
                fetchJournals();
                closeModal();
            } else {
                toast.error(response?.message || "Failed to update journal");
            }
        } catch (error) {
            console.error("Error updating journal:", error);
            toast.error(error?.message || "Failed to update journal");
        } finally {
            setUpdatingModal(false);
        }
    };

    // ===== ADD JOURNAL handlers =====
    const emptyJournal = {
        accNbr: '', folioNo: '', areaCd: '', currentBillCycle: '',
        jnlType: '', jnlNo: '', jnlDate: '',
        field1: '', field1Type: 'DEBIT',
        field2: '', field2Type: 'DEBIT',
        totalAmt: '', totalAmtType: 'DEBIT',
        approvedBy: '', name: '', docAttch: 'N',
        individuallyConfirmed: false,
    };

    const openAddModal = () => {
        // Get the selected area code and bill cycle from storage
        const selectedArea = getSelectedAreaCode();
        const activeBillCycle = selectedArea ? getBillCycleForArea(selectedArea) : '';

        // Pre-populate the form with area code and bill cycle
        setNewJournal({
            ...emptyJournal,
            areaCd: selectedArea || '',
            currentBillCycle: activeBillCycle || ''
        });
        setField1Label('Field 1'); // Reset label
        setField2Label('Field 2'); // Reset label
        setPairedJnlType(''); // Reset paired type
        setShowAddModal(true);
    };

    const closeAddModal = () => {
        setShowAddModal(false);
        setAddingJournal(false);
        setNewJournal(emptyJournal);
        setField1Label('Field 1'); // Reset label
        setField2Label('Field 2'); // Reset label
        setPairedJnlType(''); // Reset paired type
    };

    const handleNewJournalChange = (field, value) => {
        setNewJournal(prev => ({ ...prev, [field]: value }));

        // Auto-populate name when approvedBy changes
        if (field === 'approvedBy' && value) {
            const selectedUser = approvedByUsers.find(user => user.authCode === value);
            if (selectedUser && selectedUser.name) {
                setNewJournal(prev => ({ ...prev, name: selectedUser.name }));
            } else {
                setNewJournal(prev => ({ ...prev, name: '' }));
            }
        }

        // Update Field 1 label and check for A/G pairing when journal type changes
        // A/G Pairing Pattern:
        // - Codes ending with 'A' = Main transaction (e.g., PCHA = Processing Charge)
        // - Codes ending with 'G' = Tax journal (e.g., PCHG = GST for Processing Charge)
        // - First 3 chars = base code, last char determines type (A or G)
        if (field === 'jnlType' && value) {
            const selectedType = journalTypes.find(type => type.jnlType === value);
            if (selectedType && selectedType.jnlDesc) {
                setField1Label(selectedType.jnlDesc);

                // Check for A/G pairing
                const lastChar = value.charAt(value.length - 1);
                if (lastChar === 'A' || lastChar === 'G') {
                    // Get the paired journal type code
                    const baseCode = value.substring(0, value.length - 1);
                    const pairedChar = lastChar === 'A' ? 'G' : 'A';
                    const pairedCode = baseCode + pairedChar;

                    // Find if the paired journal type exists
                    const pairedType = journalTypes.find(type => type.jnlType === pairedCode);
                    if (pairedType && pairedType.jnlDesc) {
                        setField2Label(pairedType.jnlDesc);
                        setPairedJnlType(pairedCode); // Store paired journal type code
                    } else {
                        setField2Label('Field 2');
                        setPairedJnlType(''); // No pair found
                    }
                } else {
                    setField2Label('Field 2');
                    setPairedJnlType(''); // Not A/G type
                }
            } else {
                setField1Label('Field 1');
                setField2Label('Field 2');
                setPairedJnlType(''); // Reset paired type
            }
        }
    };

    const handleAddJournal = async () => {
        if (!newJournal.folioNo || !newJournal.folioNo.trim()) { toast.error("Folio Number is required"); return; }
        if (!newJournal.jnlType.trim()) { toast.error("Journal Type is required"); return; }
        if (!newJournal.jnlNo.trim()) { toast.error("Journal No is required"); return; }
        const parsedAmt = parseFloat(newJournal.totalAmt);
        if (isNaN(parsedAmt)) { toast.error("Please enter a valid Total Amount"); return; }

        let resolvedAccNbr = newJournal.accNbr || '';
        if (!resolvedAccNbr) {
            try {
                const dev = await getDeveloperBySearch('folio_no', newJournal.folioNo.trim());
                if (dev && (dev.acc_nbr || dev.accNbr)) {
                    resolvedAccNbr = dev.acc_nbr || dev.accNbr;
                }
            } catch (e) {
                console.warn("Could not resolve developer by folio_no:", e);
            }
        }

        if (!resolvedAccNbr) {
            toast.error(`Developer with Folio Number ${newJournal.folioNo} not found`);
            return;
        }

        try {
            setAddingJournal(true);

            // Prepare journal data for backend
            // DEBIT = +1, CREDIT = -1 (for database storage)
            const journalToAdd = {
                accNbr: resolvedAccNbr,
                areaCd: newJournal.areaCd,
                currentBillCycle: parseInt(newJournal.currentBillCycle, 10),
                jnlType: newJournal.jnlType,
                pairedJnlType: pairedJnlType, // Send paired journal type for A/G pairing
                jnlNo: parseInt(newJournal.jnlNo, 10),
                jnlDate: newJournal.jnlDate,
                field1: parseFloat(newJournal.field1) || 0,
                field1Type: newJournal.field1Type, // DEBIT or CREDIT
                field2: parseFloat(newJournal.field2) || 0,
                field2Type: newJournal.field2Type, // DEBIT or CREDIT
                totalAmt: parsedAmt,
                totalAmtType: newJournal.totalAmtType,
                approvedBy: newJournal.approvedBy,
                name: newJournal.name,
                docAttch: newJournal.docAttch, // "N" or "Y"
                individuallyConfirmed: newJournal.individuallyConfirmed
            };

            const response = await journalsService.createJournal(journalToAdd);
            if (response && response.success) {
                toast.success("Journal added successfully");
                fetchJournals();
                closeAddModal();
            } else {
                toast.error(response?.message || "Failed to add journal");
            }
        } catch (error) {
            console.error("Error adding journal:", error);
            toast.error(error?.message || "Failed to add journal");
        } finally {
            setAddingJournal(false);
        }
    };

    // Format currency display
    const formatCurrency = (amount) => {
        if (!amount && amount !== 0) return '0.00';
        if (typeof amount === 'number') {
            return amount.toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });
        }
        return amount ? amount.toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }) : '0.00';
    };

    // Format date display
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return dateString;
            return date.toLocaleString();
        } catch (error) {
            return dateString;
        }
    };

    // Pagination helper functions
    const handleShowChange = (e) => {
        setShowOption(e.target.value);
        setCurrentPage(1);
        setExpandedRowId(null);
        setSelectedJournal(null);
    };

    const handlePageChange = (page) => {
        setCurrentPage(page);
        setExpandedRowId(null);
        setSelectedJournal(null);
    };

    const getTotalPages = () => {
        const recordsPerPage = recordsPerPageMap[showOption] || 10;
        return Math.ceil(filteredJournals.length / recordsPerPage);
    };

    const getPageNumbers = () => {
        const totalPages = getTotalPages();
        const pages = [];
        const maxPagesToShow = 5;
        const maxPagesToShowMobile = 3;
        const isMobile = window.innerWidth < 768;
        const maxPages = isMobile ? maxPagesToShowMobile : maxPagesToShow;

        if (totalPages <= maxPages) {
            for (let i = 1; i <= totalPages; i++) {
                pages.push(i);
            }
        } else {
            if (currentPage <= Math.ceil(maxPages / 2)) {
                for (let i = 1; i <= maxPages - 1; i++) {
                    pages.push(i);
                }
                pages.push("...");
                pages.push(totalPages);
            } else if (currentPage >= totalPages - Math.floor(maxPages / 2)) {
                pages.push(1);
                pages.push("...");
                for (let i = totalPages - (maxPages - 2); i <= totalPages; i++) {
                    pages.push(i);
                }
            } else {
                pages.push(1);
                pages.push("...");
                if (isMobile) {
                    pages.push(currentPage);
                } else {
                    for (let i = currentPage - 1; i <= currentPage + 1; i++) {
                        pages.push(i);
                    }
                }
                pages.push("...");
                pages.push(totalPages);
            }
        }

        return pages;
    };

    const getRecordsPerPage = () => {
        return recordsPerPageMap[showOption] || 10;
    };

    return (
        <>
            <div className="flex flex-col gap-ds-6">
                <div className="w-full">
                    <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded bg-white">
                        <div className="rounded-t mb-0 px-4 py-3 border-0">
                            <div className="flex flex-wrap items-center">
                                <div className="relative w-full px-4 max-w-full flex-grow flex-1">
                                    <h3 className="ds-section-title">
                                        Journals Management
                                    </h3>
                                </div>
                                <div className="relative w-full px-4 max-w-full flex-grow flex-1 text-right space-x-2 flex items-center justify-end">
                                    {/* Journal Type Filter Dropdown */}
                                    <div className="flex items-center space-x-2">
                                        <span className="text-xs sm:text-sm text-ink-600 whitespace-nowrap">
                                            Type:
                                        </span>
                                        <select
                                            value={selectedJournalType}
                                            onChange={(e) => setSelectedJournalType(e.target.value)}
                                            className="border border-ink-300 rounded-md px-2 sm:px-3 pr-6 sm:pr-8 py-2 text-xs sm:text-sm focus:outline-none focus:ring-1 min-w-[150px]"
                                        >
                                            <option value="">All Types</option>
                                            {journalTypes.map((type) => (
                                                <option key={type.jnlType} value={type.jnlType}>
                                                    {type.jnlType} - {type.jnlDesc}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    {/* Show Dropdown */}
                                    <div className="flex items-center space-x-2">
                                        <span className="text-xs sm:text-sm text-ink-600 whitespace-nowrap">
                                            Show:
                                        </span>
                                        <select
                                            value={showOption}
                                            onChange={handleShowChange}
                                            className="border border-ink-300 rounded-md px-2 sm:px-3 pr-6 sm:pr-8 py-2 text-xs sm:text-sm focus:outline-none focus:ring-1 min-w-[120px]"
                                        >
                                            <option value="10">10 Journals</option>
                                            <option value="20">20 Journals</option>
                                            <option value="30">30 Journals</option>
                                            <option value="40">40 Journals</option>
                                        </select>
                                    </div>

                                    <button
                                        onClick={fetchJournals}
                                        className="bg-success-500 text-white px-4 py-2 rounded hover:bg-navy-600 text-sm disabled:opacity-50"
                                        disabled={loading || backendStatus !== 'online'}
                                    >
                                        {loading ? 'Refreshing...' : 'Refresh'}
                                    </button>
                                </div>
                            </div>

                            {/* Add Journal Button - Second Row - Only for Accountant Clark */}
                            {userCategory === 'Accountant Clark' && (
                                <div className="flex justify-end px-8 mt-3">
                                    <button
                                        onClick={openAddModal}
                                        className="flex items-center gap-3 bg-critical-800 text-white px-8 py-2 rounded hover:bg-critical-700 text-sm disabled:opacity-50 transition-colors duration-200"
                                        disabled={backendStatus !== 'online'}
                                        title="Add New Journal"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        Add Journal
                                    </button>
                                </div>
                            )}
                        </div>

                        {/* Results Info */}
                        {!loading && journals.length > 0 && (
                            <div className="px-6 py-2 text-xs sm:text-sm text-ink-600 border-b border-ink-200">
                                Showing{" "}
                                {displayedJournals.length > 0
                                    ? (currentPage - 1) * getRecordsPerPage() + 1
                                    : 0}{" "}
                                to{" "}
                                {Math.min(
                                    currentPage * getRecordsPerPage(),
                                    filteredJournals.length
                                )}{" "}
                                of {filteredJournals.length} journals
                                {selectedJournalType && ` (filtered by type: ${selectedJournalType})`}
                            </div>
                        )}

                        {/* Only show connection error if backendStatus is offline/error */}
                        {(backendStatus === 'offline' || backendStatus === 'error') && connectionError ? (
                            <div className="p-8 text-center">
                                <div className="bg-critical-50 border border-critical-200 rounded-lg p-6">
                                    <svg className="w-16 h-16 text-critical-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.196 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                                    </svg>
                                    <h4 className="text-lg font-semibold text-critical-700 mb-2">Backend Server Not Available</h4>
                                    <p className="text-critical-600 mb-4">
                                        {connectionError}
                                    </p>
                                    <div className="text-left bg-white p-4 rounded border mb-4">
                                        <p className="font-semibold mb-2">Troubleshooting steps:</p>
                                        <ol className="list-decimal pl-5 space-y-1 text-sm">
                                            <li>Start your Spring Boot application</li>
                                            <li>Check console for "Started Application" message</li>
                                            <li>Test URL in browser: <a href="http://localhost:8080/HSB/api/journals/health" target="_blank" rel="noopener noreferrer" className="text-navy-500 hover:underline">http://localhost:8080/HSB/api/journals/health</a></li>
                                            <li>Verify application is running on port 8080</li>
                                        </ol>
                                    </div>
                                    <button
                                        onClick={testBackendConnection}
                                        className="bg-critical-500 text-white px-4 py-2 rounded hover:bg-critical-600"
                                    >
                                        Retry Connection
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="block w-full overflow-x-auto">
                                {loading ? (
                                    <div className="flex justify-center items-center py-8">
                                        <div className="ds-spinner"></div>
                                        <span className="ml-3 text-ink-600">Loading journals...</span>
                                    </div>
                                ) : (
                                    <table className="items-center w-full bg-transparent border-collapse">
                                        <thead>
                                            <tr>
                                                {/* ← Left arrow column header (empty) */}
                                                <th className="px-3 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left w-12">
                                                </th>
                                                <th className="px-6 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left">
                                                    Folio Number
                                                </th>
                                                <th className="px-6 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left">
                                                    Journal Type
                                                </th>
                                                <th className="px-6 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left">
                                                    Journal No
                                                </th>
                                                <th className="px-6 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left">
                                                    Adjustment Amount
                                                </th>
                                                <th className="px-6 bg-critical-800 text-white align-middle border border-solid border-critical-600 py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left">
                                                    Actions
                                                </th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {Array.isArray(displayedJournals) && displayedJournals.length > 0 ? (
                                                displayedJournals.map((journal, index) => {
                                                    // Generate unique ID for this row using multiple fields
                                                    const currentRowId = getRowId(journal);
                                                    // Check if THIS SPECIFIC row is the expanded one
                                                    const isThisRowExpanded = expandedRowId === currentRowId;

                                                    return (
                                                        <React.Fragment key={currentRowId}>
                                                            {/* Main row */}
                                                            <tr className="hover:bg-ink-50">
                                                                {/* ← Arrow button moved to LEFT side */}
                                                                <td className="border-t-0 px-3 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4 w-12">
                                                                    <button
                                                                        className="flex items-center justify-center w-7 h-7 rounded bg-success-500 hover:bg-success-600 disabled:opacity-50 transition-colors duration-200"
                                                                        onClick={() => handleViewDetails(journal)}
                                                                        disabled={loadingDetails && isThisRowExpanded}
                                                                        title={isThisRowExpanded ? "Hide Details" : "View Journal Details"}
                                                                    >
                                                                        {loadingDetails && isThisRowExpanded ? (
                                                                            <div className="ds-spinner ds-spinner-sm ds-spinner-invert"></div>
                                                                        ) : (
                                                                            <svg
                                                                                xmlns="http://www.w3.org/2000/svg"
                                                                                className="h-4 w-4 text-white"
                                                                                fill="none"
                                                                                viewBox="0 0 24 24"
                                                                                stroke="currentColor"
                                                                                style={{
                                                                                    transform: isThisRowExpanded ? 'rotate(90deg)' : 'none',
                                                                                    transition: 'transform 0.2s'
                                                                                }}
                                                                            >
                                                                                <path
                                                                                    strokeLinecap="round"
                                                                                    strokeLinejoin="round"
                                                                                    strokeWidth={2}
                                                                                    d="M9 5l7 7-7 7"
                                                                                />
                                                                            </svg>
                                                                        )}
                                                                    </button>
                                                                </td>
                                                                <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                                                    {journal.folio_no ?? journal.folioNo ?? 'N/A'}
                                                                </td>
                                                                <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                                                    {journal.jnlType || 'N/A'}
                                                                </td>
                                                                <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                                                    {journal.jnlNo || 'N/A'}
                                                                </td>
                                                                <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                                                    {formatCurrency(journal.adjustAmt)}
                                                                </td>
                                                                <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                                                    <button
                                                                        className="flex items-center gap-1 bg-navy-500 hover:bg-navy-600 text-white text-xs font-medium px-3 py-1.5 rounded transition-colors duration-200"
                                                                        onClick={() => handleEditDetails(journal)}
                                                                        title="Edit Journal"
                                                                    >
                                                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                                                        </svg>
                                                                        Edit
                                                                    </button>
                                                                </td>
                                                            </tr>

                                                            {/* Details row - ONLY shows if THIS SPECIFIC row is expanded AND we have details AND they match this row */}
                                                            {isThisRowExpanded && selectedJournal && (
                                                                <tr className="bg-ink-50 border-t border-b border-ink-200">
                                                                    <td colSpan="6" className="px-6 py-4">
                                                                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Journal No</label>
                                                                                <p className="mt-1 text-sm text-ink-900 font-semibold">{selectedJournal.jnlNo || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Folio Number</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.folio_no ?? selectedJournal.folioNo ?? 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Area Code</label>
                                                                                <p className="mt-1 text-sm text-ink-900 font-semibold">{selectedJournal.areaCd || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Journal Type</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.jnlType || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Adjustment Amount</label>
                                                                                <p className="mt-1 text-sm text-ink-900 font-semibold">{formatCurrency(selectedJournal.adjustAmt)}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Adjustment Status</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.adjustStat || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Auth Code</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.authCode || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Document Attachment</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.docAttch || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Journal Date</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{formatDate(selectedJournal.jnlDate) || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Confirmed</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.confirmed || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">User ID</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.userId || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Entered DateTime</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{formatDate(selectedJournal.enteredDtime) || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-700">Edited User ID</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{selectedJournal.editedUserId || 'N/A'}</p>
                                                                            </div>
                                                                            <div className="bg-white p-3 rounded shadow-sm">
                                                                                <label className="block text-xs font-medium text-ink-500">Edited DateTime</label>
                                                                                <p className="mt-1 text-sm text-ink-900">{formatDate(selectedJournal.editedDtime) || 'N/A'}</p>
                                                                            </div>
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                            )}
                                                        </React.Fragment>
                                                    );
                                                })
                                            ) : (
                                                <tr>
                                                    <td colSpan="6" className="text-center py-8 text-ink-500">
                                                        <div className="flex flex-col items-center">
                                                            <svg className="w-12 h-12 text-ink-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                                                            </svg>
                                                            <p>No journals found with area_cd='27'</p>
                                                            <p className="text-sm mt-1">
                                                                {backendStatus === 'online'
                                                                    ? 'Backend is connected but no data returned'
                                                                    : 'Backend is not connected'}
                                                            </p>
                                                        </div>
                                                    </td>
                                                </tr>
                                            )}
                                        </tbody>
                                    </table>
                                )}

                                {/* Pagination */}
                                {!loading && journals.length > 0 && getTotalPages() > 1 && (
                                    <div className="px-3 sm:px-6 py-4 border-t border-ink-200 bg-white">
                                        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 sm:gap-0">
                                            <div className="text-xs sm:text-sm text-ink-700 order-2 sm:order-1">
                                                Page {currentPage} of {getTotalPages()}
                                            </div>

                                            <div className="flex items-center space-x-1 sm:space-x-2 order-1 sm:order-2">
                                                {/* Previous Button */}
                                                <button
                                                    onClick={() => handlePageChange(currentPage - 1)}
                                                    disabled={currentPage === 1}
                                                    className="px-2 sm:px-3 py-1.5 sm:py-2.5 text-xs sm:text-sm font-medium text-ink-500 bg-white border border-ink-300 rounded-md hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none"
                                                >
                                                    <span className="hidden sm:inline">Previous</span>
                                                    <i className="fas fa-chevron-left sm:hidden"></i>
                                                </button>

                                                {/* Page Numbers */}
                                                <div className="flex items-center space-x-1">
                                                    {getPageNumbers().map((page, index) => (
                                                        <React.Fragment key={index}>
                                                            {page === "..." ? (
                                                                <span className="px-2 sm:px-3 py-1.5 sm:py-2.5 text-xs sm:text-sm text-ink-500">
                                                                    ...
                                                                </span>
                                                            ) : (
                                                                <button
                                                                    onClick={() => handlePageChange(page)}
                                                                    className={`px-2 sm:px-3 py-1.5 sm:py-2.5 text-xs sm:text-sm font-medium rounded-md focus:outline-none ${currentPage === page
                                                                        ? "bg-success-500 text-white"
                                                                        : "text-ink-700 bg-white border border-ink-300 hover:bg-ink-50"
                                                                        }`}
                                                                >
                                                                    {page}
                                                                </button>
                                                            )}
                                                        </React.Fragment>
                                                    ))}
                                                </div>

                                                {/* Next Button */}
                                                <button
                                                    onClick={() => handlePageChange(currentPage + 1)}
                                                    disabled={currentPage === getTotalPages()}
                                                    className="px-2 sm:px-3 py-1.5 sm:py-2.5 text-xs sm:text-sm font-medium text-ink-500 bg-white border border-ink-300 rounded-md hover:bg-ink-50 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none"
                                                >
                                                    <span className="hidden sm:inline">Next</span>
                                                    <i className="fas fa-chevron-right sm:hidden"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* ===== Edit Modal Popup ===== */}
            {showModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center">
                    {/* Backdrop */}
                    <div
                        className="absolute inset-0 bg-black opacity-50"
                        onClick={closeModal}
                    />
                    {/* Modal Box */}
                    <div className="relative bg-white rounded-lg shadow-overlay w-full max-w-3xl mx-4 max-h-screen overflow-y-auto z-10">
                        {/* Modal Header */}
                        <div className="flex items-center justify-between px-6 py-4 bg-critical-800 rounded-t-lg">
                            <h3 className="text-white font-semibold text-lg">Journal Details</h3>
                            <button
                                onClick={closeModal}
                                className="text-white hover:text-ink-200 transition-colors duration-200"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        {/* Modal Body */}
                        <div className="px-6 py-5">
                            {loadingModal ? (
                                <div className="flex justify-center items-center py-12">
                                    <div className="ds-spinner"></div>
                                    <span className="ml-3 text-ink-600">Loading details...</span>
                                </div>
                            ) : modalJournal ? (
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Journal No</label>
                                        <p className="mt-1 text-sm text-ink-900 font-semibold">{modalJournal.jnlNo || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Folio Number</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.folio_no ?? modalJournal.folioNo ?? 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Area Code</label>
                                        <p className="mt-1 text-sm text-ink-900 font-semibold">{modalJournal.areaCd || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Journal Type</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.jnlType || 'N/A'}</p>
                                    </div>
                                    <div className="bg-navy-50 p-3 rounded shadow-sm border border-navy-200">
                                        <label className="block text-xs font-medium text-navy-600">Adjustment Amount <span className="text-navy-400 font-normal">(editable)</span></label>
                                        <div className="mt-1 flex items-center">

                                            <input
                                                type="number"
                                                step="0.01"
                                                value={editedAdjustAmt}
                                                onChange={(e) => setEditedAdjustAmt(e.target.value)}
                                                className="w-full text-sm font-semibold text-ink-900 bg-navy-50 border-0 border-b-2 border-navy-300 focus:border-navy-500 focus:outline-none focus:ring-0 px-0 py-0.5"
                                            />
                                        </div>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Adjustment Status</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.adjustStat || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Auth Code</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.authCode || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Document Attachment</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.docAttch || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Journal Date</label>
                                        <p className="mt-1 text-sm text-ink-900">{formatDate(modalJournal.jnlDate) || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Confirmed</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.confirmed || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">User ID</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.userId || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Entered DateTime</label>
                                        <p className="mt-1 text-sm text-ink-900">{formatDate(modalJournal.enteredDtime) || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Edited User ID</label>
                                        <p className="mt-1 text-sm text-ink-900">{modalJournal.editedUserId || 'N/A'}</p>
                                    </div>
                                    <div className="bg-ink-50 p-3 rounded shadow-sm">
                                        <label className="block text-xs font-medium text-ink-500">Edited DateTime</label>
                                        <p className="mt-1 text-sm text-ink-900">{formatDate(modalJournal.editedDtime) || 'N/A'}</p>
                                    </div>
                                </div>
                            ) : null}
                        </div>

                        {/* Modal Footer */}
                        <div className="flex justify-end gap-3 px-6 py-4 border-t border-ink-200 bg-ink-50 rounded-b-lg">
                            <button
                                onClick={closeModal}
                                disabled={updatingModal}
                                className="px-5 py-2 text-sm font-medium text-ink-700 bg-white border border-ink-300 rounded-md hover:bg-ink-100 disabled:opacity-50 transition-colors duration-200"
                            >
                                Close
                            </button>
                            <button
                                onClick={handleUpdate}
                                disabled={updatingModal || loadingModal}
                                className="flex items-center gap-2 px-5 py-2 text-sm font-medium text-white bg-success-500 rounded-md hover:bg-success-600 disabled:opacity-50 transition-colors duration-200"
                            >
                                {updatingModal ? (
                                    <>
                                        <div className="ds-spinner ds-spinner-sm ds-spinner-invert"></div>
                                        Updating...
                                    </>
                                ) : (
                                    <>
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                        </svg>
                                        Update
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ===== Add Journal Modal Popup ===== */}
            {showAddModal && (
                <div className="fixed inset-0 bg-ink-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
                    <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
                        <div className="flex justify-between items-center pb-3 border-b">
                            <h3 className="ds-section-title">
                                Add New Journal
                            </h3>
                            <button
                                onClick={closeAddModal}
                                className="text-ink-400 hover:text-ink-600"
                                disabled={addingJournal}
                            >
                                <i className="fas fa-times text-xl"></i>
                            </button>
                        </div>

                        <div className="mt-4">

                            {/* Row 1: Account No + Area Code */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">
                                        Folio Number <span className="text-critical-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={newJournal.folioNo}
                                        onChange={(e) => handleNewJournalChange('folioNo', e.target.value)}
                                        placeholder="e.g. 1081"
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                        disabled={addingJournal}
                                    />
                                </div>
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Area Code</label>
                                    <input
                                        type="text"
                                        value={newJournal.areaCd}
                                        readOnly
                                        placeholder="27"
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full cursor-not-allowed"
                                    />
                                </div>
                            </div>

                            {/* Row 2: Current Bill Cycle (full width) */}
                            <div className="mb-4">
                                <label className="block text-ink-600 text-sm font-bold mb-2">Current Bill Cycle</label>
                                <input
                                    type="text"
                                    value={newJournal.currentBillCycle}
                                    readOnly
                                    placeholder="Bill cycle"
                                    className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full cursor-not-allowed"
                                />
                            </div>

                            {/* Row 3: Journal Type + Journal No + Journal Date */}
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">
                                        Journal Type <span className="text-critical-500">*</span>
                                    </label>
                                    {journalTypes.length > 0 ? (
                                        <select
                                            value={newJournal.jnlType}
                                            onChange={(e) => handleNewJournalChange('jnlType', e.target.value)}
                                            className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                            disabled={addingJournal}
                                        >
                                            <option value="">Select</option>
                                            {journalTypes.map((type) => (
                                                <option key={type.jnlType} value={type.jnlType}>{type.jnlType} - {type.jnlDesc}</option>
                                            ))}
                                        </select>
                                    ) : (
                                        <input
                                            type="text"
                                            value={newJournal.jnlType}
                                            onChange={(e) => handleNewJournalChange('jnlType', e.target.value)}
                                            placeholder="e.g. PAYA"
                                            className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                            disabled={addingJournal}
                                        />
                                    )}
                                </div>
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">
                                        Journal No <span className="text-critical-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={newJournal.jnlNo}
                                        onChange={(e) => handleNewJournalChange('jnlNo', e.target.value)}
                                        placeholder="e.g. 1"
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                        disabled={addingJournal}
                                    />
                                </div>
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Journal Date</label>
                                    <input
                                        type="date"
                                        value={newJournal.jnlDate}
                                        onChange={(e) => handleNewJournalChange('jnlDate', e.target.value)}
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                        disabled={addingJournal}
                                    />
                                </div>
                            </div>

                            {/* Field 1 */}
                            <div className="mb-4">
                                <label className="block text-ink-600 text-sm font-bold mb-2">{field1Label}</label>
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        value={newJournal.field1}
                                        onChange={(e) => handleNewJournalChange('field1', e.target.value)}
                                        placeholder={field1Label + " value"}
                                        className="flex-1 border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    />
                                    <select
                                        value={newJournal.field1Type}
                                        onChange={(e) => handleNewJournalChange('field1Type', e.target.value)}
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    >
                                        <option value="DEBIT">DEBIT</option>
                                        <option value="CREDIT">CREDIT</option>
                                    </select>
                                </div>
                            </div>

                            {/* Field 2 */}
                            <div className="mb-4">
                                <label className="block text-ink-600 text-sm font-bold mb-2">{field2Label}</label>
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        value={newJournal.field2}
                                        onChange={(e) => handleNewJournalChange('field2', e.target.value)}
                                        placeholder={field2Label + " value"}
                                        className="flex-1 border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    />
                                    <select
                                        value={newJournal.field2Type}
                                        onChange={(e) => handleNewJournalChange('field2Type', e.target.value)}
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    >
                                        <option value="DEBIT">DEBIT</option>
                                        <option value="CREDIT">CREDIT</option>
                                    </select>
                                </div>
                            </div>

                            {/* Total Amount */}
                            <div className="mb-4">
                                <label className="block text-ink-600 text-sm font-bold mb-2">
                                    Total Amount <span className="text-critical-500">*</span>
                                    <span className="text-xs font-normal text-ink-500 ml-2">(Auto-calculated from Field 1 + Field 2, editable)</span>
                                </label>
                                <div className="flex gap-2">
                                    <input
                                        type="number"
                                        step="0.01"
                                        value={newJournal.totalAmt}
                                        onChange={(e) => handleNewJournalChange('totalAmt', e.target.value)}
                                        placeholder="0.00"
                                        className="flex-1 border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    />
                                    <select
                                        value={newJournal.totalAmtType}
                                        onChange={(e) => handleNewJournalChange('totalAmtType', e.target.value)}
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring"
                                        disabled={addingJournal}
                                    >
                                        <option value="DEBIT">DEBIT</option>
                                        <option value="CREDIT">CREDIT</option>
                                    </select>
                                </div>
                            </div>

                            {/* Row: Approved By + Name */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Approved By</label>
                                    {approvedByUsers.length > 0 ? (
                                        <select
                                            value={newJournal.approvedBy}
                                            onChange={(e) => handleNewJournalChange('approvedBy', e.target.value)}
                                            className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                            disabled={addingJournal}
                                        >
                                            <option value="">Select</option>
                                            {approvedByUsers.map((user) => (
                                                <option key={user.authCode} value={user.authCode}>
                                                    {user.authCode} - {user.desig}
                                                </option>
                                            ))}
                                        </select>
                                    ) : (
                                        <select
                                            value={newJournal.approvedBy}
                                            onChange={(e) => handleNewJournalChange('approvedBy', e.target.value)}
                                            className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                            disabled={addingJournal}
                                        >
                                            <option value="">Select</option>
                                        </select>
                                    )}
                                </div>
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Name</label>
                                    <input
                                        type="text"
                                        value={newJournal.name}
                                        readOnly
                                        placeholder="Auto-filled from Approved By"
                                        className="border-0 px-3 py-3 bg-ink-100 rounded text-sm shadow focus:outline-none focus:ring w-full cursor-not-allowed"
                                        disabled={true}
                                    />
                                </div>
                            </div>

                            {/* Row: Document Attached + Individually Confirmed */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Document Attached</label>
                                    <select
                                        value={newJournal.docAttch}
                                        onChange={(e) => handleNewJournalChange('docAttch', e.target.value)}
                                        className="border-0 px-3 py-3 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full"
                                        disabled={addingJournal}
                                    >
                                        <option value="N">No</option>
                                        <option value="Y">Yes</option>
                                    </select>
                                </div>
                                <div className="mb-4">
                                    <label className="block text-ink-600 text-sm font-bold mb-2">Individually Confirmed</label>
                                    <div className="flex items-center h-12">
                                        <label className="flex items-center cursor-pointer">
                                            <input
                                                type="checkbox"
                                                checked={newJournal.individuallyConfirmed}
                                                onChange={(e) => handleNewJournalChange('individuallyConfirmed', e.target.checked)}
                                                className="form-checkbox h-5 w-5 text-success-500"
                                                disabled={addingJournal}
                                            />
                                            <span className="ml-2 text-sm text-ink-600">Yes</span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="flex justify-end space-x-3 mt-6 pt-3 border-t">
                            <button
                                type="button"
                                onClick={closeAddModal}
                                className="bg-ink-500 text-white active:bg-ink-600 font-bold uppercase text-xs px-6 py-3 rounded shadow hover:shadow-md outline-none focus:outline-none ease-linear transition-all duration-150"
                                disabled={addingJournal}
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                onClick={handleAddJournal}
                                className="bg-success-500 text-white active:bg-success-600 font-bold uppercase text-xs px-6 py-3 rounded shadow hover:shadow-md outline-none focus:outline-none ease-linear transition-all duration-150"
                                disabled={addingJournal}
                            >
                                {addingJournal ? (
                                    <>
                                        <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                                        Saving...
                                    </>
                                ) : (
                                    'Save Journal'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}