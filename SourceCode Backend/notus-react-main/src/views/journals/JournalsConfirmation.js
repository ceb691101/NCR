import React, { useState, useEffect } from 'react';
import journalsService from '../../services/journalsService';
import { toast } from 'react-toastify';

export default function JournalsConfirmation() {
    const [journals, setJournals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [areaCode, setAreaCode] = useState(null);
    const [billCycle, setBillCycle] = useState(null);
    const [selectedJournals, setSelectedJournals] = useState(new Set());
    const [confirmingJournals, setConfirmingJournals] = useState(false);
    const [confirmationInfo, setConfirmationInfo] = useState(null);
    const [isConfirmed, setIsConfirmed] = useState(false);
    const [alreadyConfirmed, setAlreadyConfirmed] = useState(false);
    const [editingRowId, setEditingRowId] = useState(null);
    const [editData, setEditData] = useState({});

    useEffect(() => {
        fetchUnconfirmedJournals();
    }, []);

    const fetchUnconfirmedJournals = async () => {
        try {
            setLoading(true);
            setError(null);
            setConfirmationInfo(null);
            setIsConfirmed(false);
            setAlreadyConfirmed(false);

            // Fetch all journals from the report for the selected area
            const response = await journalsService.getJournalsReport();

            if (response.success) {
                const journalData = response.data || [];
                setJournals(journalData);

                // Get area info from first journal if available
                if (journalData.length > 0) {
                    const area = journalData[0].areaCd;
                    const cycle = journalData[0].addedBlcy;
                    setAreaCode(area);
                    setBillCycle(cycle);

                    // Check if this area and bill cycle is already confirmed
                    try {
                        const checkResponse = await journalsService.checkIfConfirmed(area, cycle);
                        if (checkResponse.success && checkResponse.isConfirmed) {
                            setAlreadyConfirmed(true);
                            toast.info('These journals have already been confirmed');
                        }
                    } catch (checkErr) {
                        console.error('Error checking confirmation status:', checkErr);
                    }
                }
            } else {
                setError(response.message || 'Failed to load journals');
            }
        } catch (err) {
            console.error('Error fetching journals:', err);
            setError(err.userMessage || err.message || 'Failed to load journals');
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        }).replace(',', '');
    };

    const formatDateForInput = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16); // Format: yyyy-MM-ddThh:mm
    };

    const handleEdit = (journal) => {
        if (alreadyConfirmed || isConfirmed) {
            toast.error('Cannot edit journals that have been confirmed');
            return;
        }
        const journalId = getJournalId(journal);
        setEditingRowId(journalId);
        setEditData({
            accNbr: journal.accNbr,
            areaCd: journal.areaCd,
            addedBlcy: journal.addedBlcy,
            jnlType: journal.jnlType,
            jnlNo: journal.jnlNo,
            adjustAmt: journal.adjustAmt,
            authCode: journal.authCode || '',
            jnlDate: journal.jnlDate,
            userId: journal.userId,
            enteredDtime: journal.enteredDtime
        });
    };

    const handleCancelEdit = () => {
        setEditingRowId(null);
        setEditData({});
    };

    const handleSaveEdit = async () => {
        try {
            // Validate the data
            if (!editData.accNbr || !editData.jnlType || !editData.jnlNo === undefined) {
                toast.error('Please fill all required fields');
                return;
            }

            const response = await journalsService.updateJournal(editData);

            if (response.success) {
                toast.success('Journal updated successfully');

                // Update the journals array
                const updatedJournals = journals.map(j => {
                    if (getJournalId(j) === editingRowId) {
                        return { ...j, ...editData };
                    }
                    return j;
                });
                setJournals(updatedJournals);

                // Reset edit state
                setEditingRowId(null);
                setEditData({});
            } else {
                toast.error(response.message || 'Failed to update journal');
            }
        } catch (err) {
            console.error('Error updating journal:', err);
            toast.error(err.userMessage || err.message || 'Failed to update journal');
        }
    };

    const handleEditFieldChange = (field, value) => {
        setEditData(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const handleSelectAll = (e) => {
        if (e.target.checked) {
            // Select all journals
            const allIds = new Set(journals.map(j => getJournalId(j)));
            setSelectedJournals(allIds);
        } else {
            // Deselect all
            setSelectedJournals(new Set());
        }
    };

    const handleSelectJournal = (journal) => {
        const journalId = getJournalId(journal);
        const newSelected = new Set(selectedJournals);

        if (newSelected.has(journalId)) {
            newSelected.delete(journalId);
        } else {
            newSelected.add(journalId);
        }

        setSelectedJournals(newSelected);
    };

    const getJournalId = (journal) => {
        return `${journal.accNbr}_${journal.areaCd}_${journal.addedBlcy}_${journal.jnlType}_${journal.jnlNo}`;
    };

    const isSelected = (journal) => {
        return selectedJournals.has(getJournalId(journal));
    };

    const handleConfirmSelected = async () => {
        if (selectedJournals.size === 0) {
            setError('Please select at least one journal to confirm');
            return;
        }

        // Check if already confirmed
        if (alreadyConfirmed) {
            toast.error('These journals have already been confirmed');
            return;
        }

        if (!window.confirm(`Are you sure you want to confirm ${selectedJournals.size} journal(s)?`)) {
            return;
        }

        try {
            setConfirmingJournals(true);
            setError(null);

            // Convert selected journals to array of journal objects
            const journalsToConfirm = journals.filter(j => isSelected(j));

            const response = await journalsService.confirmJournals(journalsToConfirm);

            if (response.success) {
                // Show toast notification
                toast.success(`Journals confirmed successfully!`);

                // Store confirmation info for display
                setConfirmationInfo({
                    count: response.confirmedCount,
                    areaCode: areaCode,
                    billCycle: billCycle,
                    logId: response.logId
                });
                setSelectedJournals(new Set());
                setIsConfirmed(true);
                setAlreadyConfirmed(true);
            } else {
                toast.error(response.message || 'Failed to confirm journals');
                setError(response.message || 'Failed to confirm journals');
            }
        } catch (err) {
            console.error('Error confirming journals:', err);
            const errorMsg = err.userMessage || err.message || 'Failed to confirm journals';
            toast.error(errorMsg);
            setError(errorMsg);
        } finally {
            setConfirmingJournals(false);
        }
    };

    if (loading) {
        return (
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-ink-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center flex justify-center">
                        <div className="ds-spinner"></div>
                    </div>
                    <div className="text-center mt-4">
                        <h6 className="text-ink-700 text-xl font-bold">Loading Journals...</h6>
                    </div>
                </div>
            </div>
        );
    }

    if (error && !confirmationInfo) {
        return (
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-ink-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center">
                        <div className="text-critical-500 mb-4">
                            <i className="fas fa-exclamation-circle text-lg text-critical-600"></i>
                        </div>
                        <h6 className="text-ink-700 text-xl font-bold mb-2">Error</h6>
                        <p className="text-ink-500">{error}</p>
                        <button
                            className="bg-ink-700 text-white active:bg-ink-600 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mt-4"
                            onClick={fetchUnconfirmedJournals}
                        >
                            <i className="fas fa-redo mr-2"></i>
                            Retry
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-white border-0">
            {/* Header */}
            <div className="rounded-t bg-white mb-0 px-6 py-6 border-b border-ink-200">
                <div className="text-center flex justify-between items-center">
                    <h6 className="text-ink-700 text-xl font-bold">
                        <i className="fas fa-check-circle mr-2"></i>
                        Journal Confirmation
                    </h6>
                    <div className="flex gap-3">
                        {!isConfirmed && !alreadyConfirmed && (
                            <button
                                className="bg-success-500 active:bg-success-600 text-white font-bold uppercase text-xs px-4 py-2 rounded shadow hover:shadow-md outline-none focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
                                type="button"
                                onClick={handleConfirmSelected}
                                disabled={selectedJournals.size === 0 || confirmingJournals}
                            >
                                {confirmingJournals ? (
                                    <>
                                        <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
                                        Confirming...
                                    </>
                                ) : (
                                    <>
                                        <i className="fas fa-check mr-2"></i>
                                        Confirm Selected ({selectedJournals.size})
                                    </>
                                )}
                            </button>
                        )}
                        {alreadyConfirmed && !isConfirmed && (
                            <div className="bg-warning-100 text-warning-800 px-4 py-2 rounded text-xs font-semibold">
                                <i className="fas fa-info-circle mr-2"></i>
                                Already Confirmed
                            </div>
                        )}
                        <button
                            className="bg-ink-500 active:bg-ink-600 text-white font-bold uppercase text-xs px-4 py-2 rounded shadow hover:shadow-md outline-none focus:outline-none"
                            type="button"
                            onClick={fetchUnconfirmedJournals}
                            disabled={confirmingJournals}
                        >
                            <i className="fas fa-sync-alt mr-2"></i>
                            Refresh
                        </button>
                    </div>
                </div>
            </div>

            {/* Content */}
            <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
                {/* Show success message after confirmation */}
                {isConfirmed && confirmationInfo ? (
                    <div className="mt-6">
                        <div className="bg-success-50 border-l-4 border-success-500 p-8 mb-6 rounded-lg shadow-lg">
                            <div className="flex items-center justify-center mb-4">
                                <i className="fas fa-check-circle text-lg text-success-600"></i>
                            </div>
                            <div className="text-center">
                                <h3 className="text-2xl font-bold text-success-800 mb-4">
                                    Confirmation Successful!
                                </h3>
                                <p className="text-lg text-success-800 mb-6">
                                    <span className="font-bold">Total Journals:</span> {confirmationInfo.count} | {' '}
                                    <span className="font-bold">Area Code:</span> {confirmationInfo.areaCode} | {' '}
                                    <span className="font-bold">Bill Cycle:</span> {confirmationInfo.billCycle}
                                    {' '}confirmed successfully.
                                </p>
                                <p className="text-sm text-success-700">
                                    Log entry saved with ID: <span className="font-bold">{confirmationInfo.logId}</span>
                                </p>
                            </div>
                        </div>
                    </div>
                ) : (
                    <>
                        {/* Error Message */}
                        {error && (
                            <div className="bg-critical-50 border-l-4 border-critical-500 p-4 mb-6 mt-6">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0">
                                        <i className="fas fa-exclamation-circle text-critical-500 text-xl"></i>
                                    </div>
                                    <div className="ml-3">
                                        <p className="text-sm text-critical-700 font-semibold">
                                            {error}
                                        </p>
                                    </div>
                                    <button
                                        className="ml-auto text-critical-500 hover:text-critical-700"
                                        onClick={() => setError(null)}
                                    >
                                        <i className="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* Info Box */}
                        <div className="bg-ink-50 border-l-4 border-ink-700 p-4 mb-6 mt-6">
                            <div className="flex items-center">
                                <div className="flex-shrink-0">
                                    <i className="fas fa-info-circle text-ink-700 text-xl"></i>
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm text-ink-700">
                                        <span className="font-bold">Total Journals: </span>
                                        {journals.length}
                                        <span className="ml-4">
                                            <span className="font-bold">Area Code: </span>
                                            {areaCode || 'N/A'}
                                        </span>
                                        <span className="ml-4">
                                            <span className="font-bold">Bill Cycle: </span>
                                            {billCycle || 'N/A'}
                                        </span>
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Instructions */}
                        <div className="bg-navy-50 border border-navy-200 rounded-lg p-4 mb-6">
                            <div className="flex items-start">
                                <div className="flex-shrink-0">
                                    <i className="fas fa-lightbulb text-navy-500 text-lg mt-0.5"></i>
                                </div>
                                <div className="ml-3">
                                    <h3 className="text-sm font-bold text-navy-900 mb-2">Instructions:</h3>
                                    <ul className="text-sm text-navy-800 space-y-1 list-disc list-inside">
                                        <li>This shows all journals for the selected area and bill cycle</li>
                                        <li>Click "Edit" button to modify journal details</li>
                                        <li><strong>Editable:</strong> Adjustment Amount, Authority Code, Journal Date, Entered By</li>
                                        <li><strong>NOT Editable:</strong> Folio Number, Journal Type, Journal No (primary keys), Area Code, Bill Cycle</li>
                                        <li>After confirmation, journals cannot be edited or confirmed again for the same area and bill cycle</li>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        {/* Table */}
                        {journals.length === 0 ? (
                            <div className="text-center py-10">
                                <div className="text-ink-400 mb-4">
                                    <i className="fas fa-inbox text-lg text-ink-400"></i>
                                </div>
                                <p className="text-ink-500 text-lg font-semibold">No journals found</p>
                                <p className="text-ink-400 text-sm mt-2">No journals available for the selected area</p>
                            </div>
                        ) : (
                            <div className="block w-full overflow-x-auto">
                                <table className="items-center w-full bg-transparent border-collapse">
                                    <thead>
                                        <tr>
                                            {!isConfirmed && !alreadyConfirmed && (
                                                <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-center">
                                                    <input
                                                        type="checkbox"
                                                        className="w-4 h-4 cursor-pointer"
                                                        checked={selectedJournals.size === journals.length && journals.length > 0}
                                                        onChange={handleSelectAll}
                                                        disabled={alreadyConfirmed}
                                                    />
                                                </th>
                                            )}
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-left">
                                                Folio Number
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-left">
                                                Journal Type
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-center">
                                                Journal No
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-right">
                                                Adjustment Amount
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-left">
                                                Authority Code
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-left">
                                                Journal Date
                                            </th>
                                            <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-left">
                                                Entered By
                                            </th>
                                            {!isConfirmed && !alreadyConfirmed && (
                                                <th className="px-4 py-3 border border-solid border-ink-300 bg-ink-100 text-ink-700 align-middle font-bold text-xs uppercase whitespace-nowrap text-center">
                                                    Actions
                                                </th>
                                            )}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {journals.map((journal, index) => {
                                            const journalId = getJournalId(journal);
                                            const isEditing = editingRowId === journalId;

                                            return (
                                                <tr
                                                    key={index}
                                                    className={`hover:bg-ink-50 ${isSelected(journal) ? 'bg-navy-50' : ''} ${isEditing ? 'bg-warning-50' : ''}`}
                                                >
                                                    {!isConfirmed && !alreadyConfirmed && (
                                                        <td className="border border-ink-300 px-4 py-3 align-middle text-xs text-center">
                                                            <input
                                                                type="checkbox"
                                                                className="w-4 h-4 cursor-pointer"
                                                                checked={isSelected(journal)}
                                                                onChange={() => handleSelectJournal(journal)}
                                                                disabled={isEditing || alreadyConfirmed}
                                                            />
                                                        </td>
                                                    )}
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-left">
                                                        {isEditing ? (
                                                            <input
                                                                type="text"
                                                                value={journal.folio_no ?? journal.folioNo ?? editData.accNbr ?? ''}
                                                                disabled
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs bg-ink-100"
                                                                title="Folio Number cannot be changed (primary key)"
                                                            />
                                                        ) : (
                                                            journal.folio_no ?? journal.folioNo ?? 'N/A'
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-left">
                                                        {isEditing ? (
                                                            <input
                                                                type="text"
                                                                value={editData.jnlType || ''}
                                                                disabled
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs bg-ink-100"
                                                                title="Journal Type cannot be changed (primary key)"
                                                            />
                                                        ) : (
                                                            journal.jnlType
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-center">
                                                        {isEditing ? (
                                                            <input
                                                                type="number"
                                                                value={editData.jnlNo || ''}
                                                                disabled
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs text-center bg-ink-100"
                                                                title="Journal No cannot be changed (primary key)"
                                                            />
                                                        ) : (
                                                            journal.jnlNo
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-right font-mono">
                                                        {isEditing ? (
                                                            <input
                                                                type="number"
                                                                step="0.01"
                                                                value={editData.adjustAmt || 0}
                                                                onChange={(e) => handleEditFieldChange('adjustAmt', parseFloat(e.target.value))}
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs text-right"
                                                            />
                                                        ) : (
                                                            <>
                                                                {Math.abs(journal.adjustAmt || 0).toFixed(2)}
                                                                {' '}
                                                                <span className={`px-2 py-1 rounded text-xs font-semibold ${(journal.adjustAmt || 0) < 0
                                                                    ? 'bg-critical-100 text-critical-700'
                                                                    : 'bg-success-100 text-success-700'
                                                                    }`}>
                                                                    {(journal.adjustAmt || 0) < 0 ? 'Cr' : 'Dr'}
                                                                </span>
                                                            </>
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-left">
                                                        {isEditing ? (
                                                            <input
                                                                type="text"
                                                                value={editData.authCode || ''}
                                                                onChange={(e) => handleEditFieldChange('authCode', e.target.value)}
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs"
                                                            />
                                                        ) : (
                                                            journal.authCode || '-'
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-left">
                                                        {isEditing ? (
                                                            <input
                                                                type="datetime-local"
                                                                value={formatDateForInput(editData.jnlDate)}
                                                                onChange={(e) => handleEditFieldChange('jnlDate', e.target.value)}
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs"
                                                            />
                                                        ) : (
                                                            formatDate(journal.jnlDate)
                                                        )}
                                                    </td>
                                                    <td className="border border-ink-300 px-4 py-3 align-middle text-xs whitespace-nowrap text-left">
                                                        {isEditing ? (
                                                            <input
                                                                type="text"
                                                                value={editData.userId || ''}
                                                                onChange={(e) => handleEditFieldChange('userId', e.target.value)}
                                                                className="border border-ink-300 px-2 py-1 rounded w-full text-xs"
                                                            />
                                                        ) : (
                                                            <div>
                                                                <span className="font-semibold">{journal.userId || '-'}</span>
                                                                <br />
                                                                <span className="text-ink-500">{formatDate(journal.enteredDtime)}</span>
                                                            </div>
                                                        )}
                                                    </td>
                                                    {!isConfirmed && !alreadyConfirmed && (
                                                        <td className="border border-ink-300 px-4 py-3 align-middle text-xs text-center">
                                                            {isEditing ? (
                                                                <div className="flex gap-1 justify-center">
                                                                    <button
                                                                        onClick={handleSaveEdit}
                                                                        className="bg-success-500 text-white px-2 py-1 rounded text-xs hover:bg-success-600"
                                                                        title="Save"
                                                                    >
                                                                        <i className="fas fa-check"></i>
                                                                    </button>
                                                                    <button
                                                                        onClick={handleCancelEdit}
                                                                        className="bg-critical-500 text-white px-2 py-1 rounded text-xs hover:bg-critical-600"
                                                                        title="Cancel"
                                                                    >
                                                                        <i className="fas fa-times"></i>
                                                                    </button>
                                                                </div>
                                                            ) : (
                                                                <button
                                                                    onClick={() => handleEdit(journal)}
                                                                    className="bg-navy-500 text-white px-3 py-1 rounded text-xs hover:bg-navy-600"
                                                                    disabled={alreadyConfirmed}
                                                                >
                                                                    <i className="fas fa-edit mr-1"></i>
                                                                    Edit
                                                                </button>
                                                            )}
                                                        </td>
                                                    )}
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
