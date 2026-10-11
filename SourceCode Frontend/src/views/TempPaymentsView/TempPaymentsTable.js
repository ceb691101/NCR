import React, { useState } from 'react';

export default function TempPaymentsTable({ payments }) {
  // State for sorting
  const [sortConfig, setSortConfig] = useState({
    key: 'credit_date',
    direction: 'desc' // 'asc' or 'desc'
  });

  // Sort payments based on current sort config
  const sortedPayments = [...payments].sort((a, b) => {
    const aValue = a[sortConfig.key];
    const bValue = b[sortConfig.key];

    // Handle null/undefined values
    if (aValue === null || aValue === undefined) return 1;
    if (bValue === null || bValue === undefined) return -1;

    // Compare values
    if (aValue < bValue) {
      return sortConfig.direction === 'asc' ? -1 : 1;
    }
    if (aValue > bValue) {
      return sortConfig.direction === 'asc' ? 1 : -1;
    }
    return 0;
  });

  // Handle column header click to sort
  const handleSort = (key) => {
    let direction = 'asc';
    // If clicking same column, toggle direction
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // Format currency values
  const formatCurrency = (value) => {
    if (!value) return '0.00';
    return parseFloat(value).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  // Format date values
  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  // Column header component with sort indicator
  const SortableHeader = ({ label, sortKey }) => (
    <th
      onClick={() => handleSort(sortKey)}
      className="px-4 py-3 bg-ink-100 text-left text-sm font-semibold text-ink-700 cursor-pointer hover:bg-ink-200 transition-colors"
    >
      <div className="flex items-center">
        {label}
        {sortConfig.key === sortKey && (
          <i className={`fas fa-arrow-${sortConfig.direction === 'asc' ? 'up' : 'down'} ml-2 text-xs`}></i>
        )}
      </div>
    </th>
  );

  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <SortableHeader label="Agent Code" sortKey="agent_code" />
            <SortableHeader label="Center Code" sortKey="cent_code" />
            <SortableHeader label="Folio Number" sortKey="folio_no" />
            <SortableHeader label="Counter" sortKey="counter" />
            <SortableHeader label="Lot" sortKey="lot" />
            <SortableHeader label="Stub No" sortKey="stub_no" />
            <SortableHeader label="Actual Pay Date" sortKey="actl_pay_date" />
            <SortableHeader label="Credit Date" sortKey="credit_date" />
            <SortableHeader label="Pay Mode" sortKey="pay_mode" />
            <SortableHeader label="Amount" sortKey="paid_amt" />
          </tr>
        </thead>
        <tbody>
          {sortedPayments.map((payment, index) => (
            <tr
              key={index}
              className={`border-b border-ink-200 ${
                index % 2 === 0 ? 'bg-white' : 'bg-ink-50'
              } hover:bg-navy-50 transition-colors`}
            >
              <td className="px-4 py-3 text-sm text-ink-900">
                {payment.agent_code || '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900 font-medium">
                {payment.cent_code || '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900 font-medium">
                {payment.folio_no ?? payment.folioNo ?? '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900">
                {payment.counter || '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900">
                {payment.lot || '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900 text-right">
                {payment.stub_no || '-'}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900">
                {formatDate(payment.actl_pay_date)}
              </td>
              <td className="px-4 py-3 text-sm text-ink-900">
                {formatDate(payment.credit_date)}
              </td>
              <td className="px-4 py-3 text-sm">
                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                  payment.pay_mode === 'Q' ? 'bg-navy-100 text-navy-800' :
                  payment.pay_mode === 'C' ? 'bg-success-100 text-success-800' :
                  payment.pay_mode === 'M' ? 'bg-navy-100 text-navy-800' :
                  'bg-ink-100 text-ink-800'
                }`}>
                  {payment.pay_mode || '-'}
                </span>
              </td>
              <td className="px-4 py-3 text-sm text-right font-semibold text-success-600">
                {formatCurrency(payment.paid_amt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}