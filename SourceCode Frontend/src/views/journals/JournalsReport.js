import React, { useState, useEffect } from 'react';
import journalsService from '../../services/journalsService';

export default function JournalsReport() {
    const [journals, setJournals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [areaCode, setAreaCode] = useState(null);
    const [billCycle, setBillCycle] = useState(null);

    useEffect(() => {
        fetchJournalsReport();
    }, []);

    const fetchJournalsReport = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await journalsService.getJournalsReport();

            if (response.success) {
                setJournals(response.data || []);

                // Get area info from first journal if available
                if (response.data && response.data.length > 0) {
                    setAreaCode(response.data[0].areaCd);
                    setBillCycle(response.data[0].addedBlcy);
                }
            } else {
                setError(response.message || 'Failed to load journals');
            }
        } catch (err) {
            console.error('Error fetching journals report:', err);
            setError(err.userMessage || err.message || 'Failed to load journals report');
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

    const printReport = () => {
        window.print();
    };

    if (loading) {
        return (
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-ink-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center flex justify-center">
                        <div className="ds-spinner"></div>
                    </div>
                    <div className="text-center mt-4">
                        <h6 className="text-ink-700 text-xl font-bold">Loading Journals Report...</h6>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-ink-100 border-0">
                <div className="rounded-t bg-white mb-0 px-6 py-6">
                    <div className="text-center">
                        <div className="text-critical-500 mb-4">
                            <i className="fas fa-exclamation-circle text-lg text-critical-600"></i>
                        </div>
                        <h6 className="text-ink-700 text-xl font-bold mb-2">Error Loading Report</h6>
                        <p className="text-ink-500">{error}</p>
                        <button
                            className="bg-ink-700 text-white active:bg-ink-600 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mt-4"
                            onClick={fetchJournalsReport}
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
            {/* Header - Hidden in print */}
            <div className="rounded-t bg-white mb-0 px-6 py-6 print:hidden">
                <div className="text-center flex justify-between">
                    <h6 className="text-ink-700 text-xl font-bold">
                        <i className="fas fa-file-invoice mr-2"></i>
                        Monthly Journals Report
                    </h6>
                    <button
                        className="bg-ink-700 active:bg-ink-600 text-white font-bold uppercase text-xs px-4 py-2 rounded shadow hover:shadow-md outline-none focus:outline-none"
                        type="button"
                        onClick={printReport}
                    >
                        <i className="fas fa-print mr-2"></i>
                        Print
                    </button>
                </div>
            </div>

            {/* Report Content - Formatted for print */}
            <div className="flex-auto px-4 lg:px-10 py-10 pt-0">
                {/* Print Header */}
                <div className="hidden print:block mb-6 text-center border-b-2 border-ink-700 pb-4">
                    <h1 className="text-2xl font-bold mb-2">Edit Report of Monthly Journals</h1>
                    <div className="flex justify-between text-sm mt-4">
                        <div>
                            <span className="font-bold">Area Code: </span>
                            {areaCode || 'N/A'}
                        </div>
                        <div>
                            <span className="font-bold">Current Bill Cycle: </span>
                            {billCycle || 'N/A'}
                        </div>
                        <div>
                            <span className="font-bold">Date & Time: </span>
                            {new Date().toLocaleString('en-GB', {
                                day: '2-digit',
                                month: '2-digit',
                                year: 'numeric',
                                hour: '2-digit',
                                minute: '2-digit',
                                second: '2-digit',
                                hour12: true
                            })}
                        </div>
                    </div>
                </div>

                {/* Info Box - Screen only */}
                <div className="bg-ink-50 border-l-4 border-ink-700 p-4 mb-6 print:hidden">
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
                                    {areaCode || 'Multiple Areas'}
                                </span>
                                <span className="ml-4">
                                    <span className="font-bold">Bill Cycle: </span>
                                    {billCycle || 'Multiple Cycles'}
                                </span>
                            </p>
                        </div>
                    </div>
                </div>

                {/* Table */}
                {journals.length === 0 ? (
                    <div className="text-center py-10">
                        <div className="text-ink-400 mb-4">
                            <i className="fas fa-inbox text-lg text-ink-400"></i>
                        </div>
                        <p className="text-ink-500 text-lg">No journals found for your accessible areas</p>
                    </div>
                ) : (
                    <div className="block w-full overflow-x-auto">
                        <table className="items-center w-full bg-transparent border-collapse" style={{ fontSize: '11px' }}>
                            <thead>
                                <tr>
                                    <th className="ds-th px-2">
                                        Folio Number
                                    </th>
                                    <th className="ds-th px-2">
                                        Journal Type
                                    </th>
                                    <th className="px-2 py-3 border border-solid border-ink-700 bg-ink-50 text-ink-700 align-middle font-semibold text-xs uppercase whitespace-nowrap text-center">
                                        Adjustment Amount
                                    </th>
                                    <th className="px-2 py-3 border border-solid border-ink-700 bg-ink-50 text-ink-700 align-middle font-semibold text-xs uppercase whitespace-nowrap text-center">
                                        Cr/Dr
                                    </th>
                                    <th className="ds-th px-2">
                                        Authority Code
                                    </th>
                                    <th className="ds-th px-2">
                                        Journal Date
                                    </th>
                                    <th className="px-2 py-3 border border-solid border-ink-700 bg-ink-50 text-ink-700 align-middle font-semibold text-xs uppercase whitespace-nowrap text-center">
                                        Doc Exists
                                    </th>
                                    <th className="ds-th px-2">
                                        Entered User ID & Date/Time
                                    </th>
                                    <th className="ds-th px-2">
                                        Edited User & Date/Time
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                {journals.map((journal, index) => (
                                    <tr key={index} className="hover:bg-ink-50">
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {journal.folio_no ?? journal.folioNo ?? 'N/A'}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {journal.jnlType}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-right">
                                            {Math.abs(journal.adjustAmt || 0).toFixed(2)}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-center">
                                            {(journal.adjustAmt || 0) < 0 ? 'Cr' : 'Dr'}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {journal.authCode || ''}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {formatDate(journal.jnlDate)}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-center">
                                            {journal.docAttch || ''}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {journal.userId || ''}<br />
                                            {formatDate(journal.enteredDtime)}
                                        </td>
                                        <td className="border border-ink-700 px-2 py-2 align-middle text-xs whitespace-nowrap text-left">
                                            {journal.editedUserId || ''}<br />
                                            {formatDate(journal.editedDtime)}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Page number for print */}
                <div className="hidden print:block mt-6 text-right text-xs">
                    <span>Page 1 of 1</span>
                </div>
            </div>

            {/* Print Styles */}
            <style jsx>{`
                @media print {
                    @page {
                        size: landscape;
                        margin: 0.5cm;
                    }
                    body {
                        print-color-adjust: exact;
                        -webkit-print-color-adjust: exact;
                    }
                    table {
                        page-break-inside: auto;
                    }
                    tr {
                        page-break-inside: avoid;
                        page-break-after: auto;
                    }
                }
            `}</style>
        </div>
    );
}
