import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { getAllTempPayments, getTempPaymentsByAreaCd, getTempPaymentsByAccNbr, createTempPayment, getDropdownOptions } from '../../services/tempPaymentsService';
import { getDeveloperBySearch } from '../../services/developerRegistrationService';
import TempPaymentsTable from './TempPaymentsTable';
import TempPaymentsFilters from './TempPaymentsFilters';
import Pagination from '../../components/Pagination/Pagination';
import TempPaymentsFormModal from './TempPaymentsFormModal';

export default function TempPaymentsView() {
  // State variables to manage data and UI
  const [payments, setPayments] = useState([]);
  const [filteredPayments, setFilteredPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchType, setSearchType] = useState('all'); // 'all', 'area', 'account'
  const [searchValue, setSearchValue] = useState('');
  const [dropdownOptions, setDropdownOptions] = useState({
    agentCodes: [],
    centerCodes: [],
    counters: [],
    payModes: []
  });
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize, setPageSize] = useState(25);
  const [hasNext, setHasNext] = useState(false);
  const [hasPrevious, setHasPrevious] = useState(false);

  // Load all payments and dropdown options when component first mounts
  useEffect(() => {
    loadAllPayments();
    loadDropdownOptions();
  }, [currentPage]);

  // Load dropdown options from backend
  const loadDropdownOptions = async () => {
    try {
      const options = await getDropdownOptions();
      setDropdownOptions(options);
    } catch (error) {
      console.error('Failed to load dropdown options:', error);
    }
  };

  // Load all temp payments from API
  const loadAllPayments = async () => {
    try {
      setLoading(true);
      const data = await getAllTempPayments(currentPage, pageSize);
      setPayments(data.payments);
      setFilteredPayments(data.payments);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setCurrentPage(data.currentPage);
      setPageSize(data.pageSize);
      setHasNext(data.hasNext);
      setHasPrevious(data.hasPrevious);
      toast.success('Payments loaded successfully');
    } catch (error) {
      console.error('Failed to load payments:', error);
      toast.error(error.message || 'Failed to load payments');
    } finally {
      setLoading(false);
    }
  };

  // Handle search when user changes filters
  const handleSearch = async (type, value) => {
    setSearchType(type);
    setSearchValue(value);
    setCurrentPage(0); // Reset to first page

    try {
      setLoading(true);
      let data;

      if (type === 'all' || !value) {
        data = await getAllTempPayments(0, pageSize);
      } else if (type === 'folio') {
        const dev = await getDeveloperBySearch('folio_no', value.trim());
        if (dev && (dev.accountNumber || dev.accNbr || dev.acc_nbr)) {
          const acc = dev.accountNumber || dev.accNbr || dev.acc_nbr;
          data = await getTempPaymentsByAccNbr(acc, 0, pageSize);
        } else {
          toast.error(`No developer found for Folio Number: ${value}`);
          data = { payments: [], totalPages: 0, totalItems: 0, currentPage: 0, pageSize, hasNext: false, hasPrevious: false };
        }
      } else if (type === 'area') {
        data = await getTempPaymentsByAreaCd(value, 0, pageSize);
      } else if (type === 'account') {
        data = await getTempPaymentsByAccNbr(value, 0, pageSize);
      }

      setFilteredPayments(data.payments);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setCurrentPage(data.currentPage);
      setPageSize(data.pageSize);
      setHasNext(data.hasNext);
      setHasPrevious(data.hasPrevious);
    } catch (error) {
      console.error('Search failed:', error);
      toast.error(error.message || 'Search failed');
      setFilteredPayments([]);
    } finally {
      setLoading(false);
    }
  };

  // Handle page change
  const handlePageChange = async (newPage) => {
    try {
      setLoading(true);
      let data;

      if (searchType === 'all' || !searchValue) {
        data = await getAllTempPayments(newPage, pageSize);
      } else if (searchType === 'folio') {
        const dev = await getDeveloperBySearch('folio_no', searchValue.trim());
        if (dev && (dev.accountNumber || dev.accNbr || dev.acc_nbr)) {
          const acc = dev.accountNumber || dev.accNbr || dev.acc_nbr;
          data = await getTempPaymentsByAccNbr(acc, newPage, pageSize);
        } else {
          data = { payments: [], totalPages: 0, totalItems: 0, currentPage: newPage, pageSize, hasNext: false, hasPrevious: false };
        }
      } else if (searchType === 'area') {
        data = await getTempPaymentsByAreaCd(searchValue, newPage, pageSize);
      } else if (searchType === 'account') {
        data = await getTempPaymentsByAccNbr(searchValue, newPage, pageSize);
      }

      setFilteredPayments(data.payments);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setCurrentPage(data.currentPage);
      setPageSize(data.pageSize);
      setHasNext(data.hasNext);
      setHasPrevious(data.hasPrevious);
      
      // Scroll to top
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
      console.error('Page change failed:', error);
      toast.error(error.message || 'Failed to load page');
    } finally {
      setLoading(false);
    }
  };

  // Handle refresh button click
  const handleRefresh = () => {
    setCurrentPage(0);
    setSearchType('all');
    setSearchValue('');
    loadAllPayments();
  };

  // Handle add payment button click
  const handleAddClick = () => {
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    if (!saving) {
      setIsModalOpen(false);
    }
  };

  const handleCreatePayment = async (payload) => {
    try {
      setSaving(true);
      await createTempPayment(payload);
      toast.success('Payment created successfully');
      setIsModalOpen(false);
      loadAllPayments();
      return true;
    } catch (error) {
      console.error('Create payment failed:', error);
      toast.error(error.message || 'Failed to create payment');
      return false;
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="w-full">
      {/* Header Section */}
      <div className="ds-card p-ds-5 mb-ds-6">
        <h1 className="text-3xl font-bold text-ink-800 mb-2">Temporary Payments</h1>
        <p className="text-ink-600">View and manage temporary payment entries</p>
      </div>

      {/* Filters Section */}
      <TempPaymentsFilters 
        onSearch={handleSearch}
        onRefresh={handleRefresh}
        onAdd={handleAddClick}
        loading={loading}
      />

      {/* Form Modal */}
      <TempPaymentsFormModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleCreatePayment}
        loading={saving}
        options={dropdownOptions}
      />

      {/* Table Section */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden">
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <div className="text-lg text-ink-600">
              <span className="ds-spinner ds-spinner-sm mr-2 align-middle" aria-hidden="true"></span>
              Loading payments...
            </div>
          </div>
        ) : filteredPayments.length > 0 ? (
          <>
            <TempPaymentsTable payments={filteredPayments} />
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalItems={totalItems}
              pageSize={pageSize}
              hasNext={hasNext}
              hasPrevious={hasPrevious}
              onPageChange={handlePageChange}
            />
          </>
        ) : (
          <div className="flex justify-center items-center h-64">
            <p className="text-ink-500 text-lg">No payments found</p>
          </div>
        )}
      </div>

      {/* Summary Section */}
      {filteredPayments.length > 0 && (
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            
            <div className="bg-success-50 p-4 rounded">
              <p className="text-ink-600 text-sm">Current Page Total</p>
              <p className="text-2xl font-bold text-success-600">
                {filteredPayments.reduce((sum, p) => sum + (parseFloat(p.paid_amt) || 0), 0).toFixed(2)}
              </p>
            </div>
            <div className="bg-navy-50 p-4 rounded">
              <p className="text-ink-600 text-sm">Current Page</p>
              <p className="text-2xl font-bold text-navy-600">{currentPage + 1} / {totalPages}</p>
            </div>
            <div className="bg-warning-50 p-4 rounded">
              <p className="text-ink-600 text-sm">Records Per Page</p>
              <p className="text-2xl font-bold text-warning-600">{pageSize}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}