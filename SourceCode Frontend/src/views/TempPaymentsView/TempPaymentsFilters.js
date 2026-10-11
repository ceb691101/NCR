import React, { useState } from 'react';

export default function TempPaymentsFilters({ onSearch, onRefresh, onAdd, loading }) {
  // Local state for filter inputs
  const [filterType, setFilterType] = useState('all');
  const [filterValue, setFilterValue] = useState('');

  // Handle when user clicks search button
  const handleSearchClick = () => {
    onSearch(filterType, filterValue);
  };

  // Handle when user presses Enter key
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearchClick();
    }
  };

  // Handle clearing filters
  const handleClear = () => {
    setFilterType('all');
    setFilterValue('');
    onSearch('all', '');
  };

  return (
    <div className="ds-card p-ds-5 mb-ds-6">
      <h2 className="text-xl font-bold text-ink-800 mb-4">Filters</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Filter Type Dropdown */}
        <div>
          <label className="block text-sm font-medium text-ink-700 mb-2">
            Filter By
          </label>
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="w-full px-4 py-2 border border-ink-300 rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-transparent"
          >
            <option value="all">All Records</option>
            <option value="folio">Folio Number</option>
            <option value="area">Area Code</option>
            <option value="account">Account Number</option>
          </select>
        </div>

        {/* Search Input */}
        <div>
          <label className="block text-sm font-medium text-ink-700 mb-2">
            Search Value
          </label>
          <input
            type="text"
            value={filterValue}
            onChange={(e) => setFilterValue(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Enter search value..."
            className="w-full px-4 py-2 border border-ink-300 rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-transparent"
            disabled={filterType === 'all'}
          />
        </div>

        {/* Button Group */}
        <div className="flex items-end gap-2">
          <button
            onClick={handleSearchClick}
            disabled={loading || (filterType !== 'all' && !filterValue)}
            className="flex-1 px-4 py-2 bg-navy-600 text-white rounded-lg hover:bg-navy-700 disabled:bg-ink-400 transition-colors"
          >
            <i className="fas fa-search mr-2"></i>
            Search
          </button>
        </div>

        {/* Clear Button */}
        <div className="flex items-end">
          <button
            onClick={handleClear}
            disabled={loading}
            className="w-full px-4 py-2 bg-ink-300 text-ink-700 rounded-lg hover:bg-ink-400 disabled:bg-ink-200 transition-colors"
          >
            <i className="fas fa-times mr-2"></i>
            Clear
          </button>
        </div>
      </div>

      {/* Refresh Button */}
      <div className="mt-4 flex flex-wrap gap-3">
        <button
          onClick={onRefresh}
          disabled={loading}
          className="px-6 py-2 bg-success-600 text-white rounded-lg hover:bg-success-700 disabled:bg-ink-400 transition-colors"
        >
          <i className="fas fa-sync-alt mr-2"></i>
          Refresh Data
        </button>
        <button
          onClick={onAdd}
          disabled={loading}
          className="px-6 py-2 bg-navy-600 text-white rounded-lg hover:bg-navy-700 disabled:bg-ink-400 transition-colors"
        >
          <i className="fas fa-plus mr-2"></i>
          Add Payment
        </button>
      </div>
    </div>
  );
}