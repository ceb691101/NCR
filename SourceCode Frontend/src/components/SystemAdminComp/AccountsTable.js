import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import Breadcrumb from "components/Breadcrumb/Breadcrumb";

const AccountsTable = ({
  accounts,
  onAddAccount,
  onEditAccount,
  onToggleUserStatus,
  loading,
  // Add these new props
  breadcrumbItems = [],
  onBackClick,
  hasUnsavedChanges = false,
}) => {
  // State for search, sort, show, and pagination
  const [searchTerm, setSearchTerm] = useState("");
  const [sortOption, setSortOption] = useState("user_id_asc");
  const [showOption, setShowOption] = useState("10");
  const [currentPage, setCurrentPage] = useState(1);
  const [filteredAccounts, setFilteredAccounts] = useState([]);
  const [displayedAccounts, setDisplayedAccounts] = useState([]);

  // Records per page mapping
  const recordsPerPageMap = {
    10: 10,
    20: 20,
    30: 30,
    40: 40,
  };

  // Initialize and update filtered accounts
  useEffect(() => {
    let filtered = [...accounts];

    // Apply search filter
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase().trim();
      filtered = filtered.filter(
        (account) =>
          (account.user_id && account.user_id.toLowerCase().includes(term)) ||
          (account.epf_num && account.epf_num.toLowerCase().includes(term)) ||
          (account.user_name &&
            account.user_name.toLowerCase().includes(term)) ||
          (account.user_cat && account.user_cat.toLowerCase().includes(term))
      );
    }

    // Apply show filter
    if (showOption === "active") {
      filtered = filtered.filter((account) => account.status === 1);
    } else if (showOption === "inactive") {
      filtered = filtered.filter((account) => account.status === 0);
    } else if (showOption === "admins") {
      filtered = filtered.filter((account) => account.user_cat === "Admin");
    } else if (showOption === "region") {
      filtered = filtered.filter(
        (account) => account.user_cat === "Region User"
      );
    } else if (showOption === "province") {
      filtered = filtered.filter(
        (account) => account.user_cat === "Province User"
      );
    } else if (showOption === "area") {
      filtered = filtered.filter((account) => account.user_cat === "Area User");
    } else if (showOption === "ee") {
      filtered = filtered.filter(
        (account) => account.user_cat === "Electrical Engineer"
      );
    } else if (showOption === "ce") {
      filtered = filtered.filter(
        (account) => account.user_cat === "Chief Engineer"
      );
    } else if (showOption === "dgm") {
      filtered = filtered.filter((account) => {
        const cat = (account.user_cat || "").toUpperCase();
        return cat === "DGM" || cat === "DIRECTOR";
      });
    }

    // Apply sorting
    filtered.sort((a, b) => {
      switch (sortOption) {
        case "user_id_asc":
          return (a.user_id || "").localeCompare(b.user_id || "");
        case "epf_num_asc":
          return (a.epf_num || "").localeCompare(b.epf_num || "");
        case "name_asc":
          return (a.user_name || "").localeCompare(b.user_name || "");
        case "name_desc":
          return (b.user_name || "").localeCompare(a.user_name || "");
        case "job_num_asc":
          return (a.job_nbr || "").localeCompare(b.job_nbr || "");
        default:
          return (a.user_id || "").localeCompare(b.user_id || "");
      }
    });

    setFilteredAccounts(filtered);
    setCurrentPage(1); // Reset to first page when filters change
  }, [accounts, searchTerm, sortOption, showOption]);

  // Update displayed accounts based on pagination
  useEffect(() => {
    const recordsPerPage = recordsPerPageMap[showOption] || 10;
    const startIndex = (currentPage - 1) * recordsPerPage;
    const endIndex = startIndex + recordsPerPage;
    setDisplayedAccounts(filteredAccounts.slice(startIndex, endIndex));
  }, [filteredAccounts, currentPage, showOption]);

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSortChange = (e) => {
    setSortOption(e.target.value);
  };

  const handleShowChange = (e) => {
    setShowOption(e.target.value);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  const getCategoryColor = (category) => {
    const colors = {
      "Province User": "bg-navy-100 text-navy-800",
      "Area User": "bg-success-100 text-success-800",
      "Region User": "bg-warning-100 text-warning-800",
      Admin: "bg-critical-100 text-critical-800",
      "Accountant Revenue": "bg-critical-100 text-critical-800",
      "Acc Assistance": "bg-ink-100 text-ink-800",
      "Accountant Clark": "bg-rose-100 text-rose-800",
      "Electrical Engineer": "bg-warning-100 text-warning-800",
      "Chief Engineer": "bg-navy-100 text-navy-800",
      "DGM": "bg-navy-100 text-navy-800",
      "Director": "bg-navy-100 text-navy-800",
      "DIRECTOR": "bg-navy-100 text-navy-800",
    };
    return colors[category] || "bg-ink-100 text-ink-800";
  };

  const formatLocationDisplay = (account) => {
    const { user_cat, region_code, province_code, area_code } = account;

    if (user_cat === "Admin" ||
      user_cat === "Electrical Engineer" ||
      user_cat === "Chief Engineer" ||
      user_cat === "DGM" ||
      user_cat === "Director" ||
      user_cat === "DIRECTOR") {
      return "All Regions";
    } else if (user_cat === "Region User") {
      return region_code ? `Region: ${region_code}` : "Not Assigned";
    } else if (
      user_cat === "Province User" ||
      user_cat === "Accountant Revenue" ||
      user_cat === "Acc Assistance" ||
      user_cat === "Accountant Clark"
    ) {
      if (region_code && province_code) {
        return `Region: ${region_code}, Province: ${province_code}`;
      }
      return "Not Assigned";
    } else if (user_cat === "Area User") {
      if (region_code && province_code && area_code) {
        return `Region: ${region_code}, Province: ${province_code}, Area: ${area_code}`;
      }
      return "Not Assigned";
    }

    return "Not Assigned";
  };

  const handleEditClick = (account) => {
    try {
      onEditAccount(account);
    } catch (error) {
      console.error("Error editing account:", error);
      toast.error("Failed to open edit dialog");
    }
  };

  const handleToggleStatus = (userId, currentStatus) => {
    try {
      onToggleUserStatus(userId, currentStatus);
    } catch (error) {
      console.error("Error toggling user status:", error);
      toast.error("Failed to toggle user status");
    }
  };

  const handleAddClick = () => {
    try {
      onAddAccount();
    } catch (error) {
      console.error("Error opening add dialog:", error);
      toast.error("Failed to open add user dialog");
    }
  };

  const handleRefresh = () => {
    // This would typically refresh the data from parent component
    window.location.reload(); // Simple refresh for now
  };

  const StatusToggle = ({ userId, status, onToggle, disabled }) => {
    const isActive = status === 1;

    return (
      <button
        onClick={() => onToggle(userId, status)}
        disabled={disabled}
        className={`
          relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed
          ${
            isActive
              ? "bg-success-500 focus:ring-success-500"
              : "bg-ink-300 focus:ring-ink-400"
          }
        `}
        title={`Click to ${isActive ? "deactivate" : "activate"} user`}
      >
        <span
          className={`
            inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ease-in-out
            ${isActive ? "translate-x-6" : "translate-x-1"}
          `}
        />
      </button>
    );
  };

  // Calculate pagination
  const getTotalPages = () => {
    const recordsPerPage = recordsPerPageMap[showOption] || 10;
    return Math.ceil(filteredAccounts.length / recordsPerPage);
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

  // Get records per page based on show option
  const getRecordsPerPage = () => {
    return recordsPerPageMap[showOption] || 10;
  };

  // Get sort text for display
  const getSortText = () => {
    switch (sortOption) {
      case "user_id_asc":
        return "User ID Ascending";
      case "epf_num_asc":
        return "EPF Num Ascending";
      case "name_asc":
        return "User Name: A-Z";
      case "name_desc":
        return "User Name: Z-A";
      case "job_num_asc":
        return "Job Num Ascending";
      default:
        return "User ID Ascending";
    }
  };

  return (
    <div className="w-full bg-white rounded-lg shadow-lg overflow-hidden">
      {/* Header section matching Bulk Customers */}
      <div className="mb-6 bg-white border-b border-ink-200">
        {/* Main header with title, add button, and refresh */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center px-3 sm:px-6 py-4 border-b border-ink-200 space-y-3 md:space-y-0">
          <div className="flex-1 min-w-0">
            <h2 className="ds-section-title">
              User Management
            </h2>
            <p className="ds-caption mt-1">
              Manage system users and access permissions
            </p>
          </div>

          <div className="flex w-full items-center gap-2 md:mt-0 md:w-auto">
            <button
              onClick={handleAddClick}
              disabled={loading}
              className="ds-btn ds-btn-primary h-[38px] flex-1 md:flex-none"
            >
              <i className="fas fa-user-plus" aria-hidden="true"></i>
              <span>Add User</span>
            </button>
            <button
              type="button"
              onClick={handleRefresh}
              disabled={loading}
              className="ds-btn ds-btn-secondary h-[38px] flex-1 md:flex-none"
              title="Refresh data"
            >
              <i className="fas fa-sync-alt" aria-hidden="true"></i>
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Intermediate Section with Breadcrumb */}
        <div className="px-3 py-3 border-b border-ink-200 bg-ink-50 sm:px-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div className="sm:mb-0">
              {breadcrumbItems.length > 0 && (
                <Breadcrumb
                  items={breadcrumbItems}
                  onBackClick={onBackClick}
                  hasUnsavedChanges={hasUnsavedChanges}
                />
              )}
            </div>
          </div>
        </div>

        {/* Search, Sort, Show controls */}
        <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center px-3 sm:px-6 py-4 border-b border-ink-200 space-y-3 lg:space-y-0">
          {/* Search Input */}
          <div className="flex items-center space-x-2 w-full lg:w-auto">
            <div className="relative flex-1 lg:flex-initial min-w-0">
              <i className="fas fa-search absolute left-3 top-1/2 transform -translate-y-1/2 text-ink-400"></i>
              <input
                type="text"
                placeholder="Search users..."
                value={searchTerm}
                onChange={handleSearchChange}
                className="ds-filter-input h-10 w-full lg:w-64 xl:w-80"
              />
            </div>
            {searchTerm && (
              <button
                onClick={() => setSearchTerm("")}
                className="text-ink-400 hover:text-ink-600 p-1 focus:outline-none flex-shrink-0"
                title="Clear search"
              >
                <i className="fas fa-times"></i>
              </button>
            )}
          </div>

          {/* Sort and Show Dropdowns */}
          <div className="flex flex-row items-center space-x-2 w-full lg:w-auto">
            {/* Sort Dropdown */}
            <div className="flex items-center space-x-2 flex-1 sm:flex-none">
              <span className="text-xs sm:text-sm text-ink-600 whitespace-nowrap hidden sm:inline">
                Sort:
              </span>
              <span
                className="text-xs sm:text-sm text-ink-600 whitespace-nowrap sm:hidden"
                title="Sort by"
              >
                Sort:
              </span>
              <select
                value={sortOption}
                onChange={handleSortChange}
                className="ds-select h-10 pr-8 sm:pr-8 min-w-[160px] w-full sm:w-auto"
              >
                <option value="user_id_asc">User ID Ascending</option>
                <option value="epf_num_asc">EPF Num Ascending</option>
                <option value="name_asc">User Name: A-Z</option>
                <option value="name_desc">User Name: Z-A</option>
                <option value="job_num_asc">Job Num Ascending</option>
              </select>
            </div>

            {/* Show Dropdown */}
            <div className="flex items-center space-x-2 flex-1 sm:flex-none">
              <span className="text-xs sm:text-sm text-ink-600 whitespace-nowrap hidden md:inline">
                Show:
              </span>
              <span
                className="text-xs sm:text-sm text-ink-600 whitespace-nowrap md:hidden"
                title="Show"
              >
                Show:
              </span>
              <select
                value={showOption}
                onChange={handleShowChange}
                className="ds-select h-10 pr-8 sm:pr-8 min-w-[140px] w-full sm:w-auto"
              >
                <option value="active">Active Users</option>
                <option value="inactive">Inactive Users</option>
                <option value="admins">Admins</option>
                <option value="region">Region Users</option>
                <option value="province">Province Users</option>
                <option value="area">Area Users</option>
                <option value="ee">Electrical Engineers</option>
                <option value="ce">Chief Engineers</option>
                <option value="dgm">Director</option>
              </select>
            </div>
          </div>
        </div>

        {/* Results Info */}
        {!loading && (
          <div className="px-3 sm:px-6 py-2 text-xs sm:text-sm text-ink-600 border-ink-200">
            Showing{" "}
            {displayedAccounts.length > 0
              ? (currentPage - 1) * getRecordsPerPage() + 1
              : 0}{" "}
            to{" "}
            {Math.min(
              currentPage * getRecordsPerPage(),
              filteredAccounts.length
            )}{" "}
            of {filteredAccounts.length} users
            {` (Sorted by ${getSortText()})`}
          </div>
        )}
      </div>

      {/* Table - Desktop & Tablet View with PROGRESSIVE COLUMN HIDING */}
      <div className="hidden md:block overflow-x-auto">
        <div className="min-w-full">
          <table className="w-full">
            <thead className="bg-ink-50">
              <tr>
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-left text-xs font-medium text-ink-500 uppercase tracking-wider">
                  User ID
                </th>
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-left text-xs font-medium text-ink-500 uppercase tracking-wider">
                  EPF Number
                </th>
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-left text-xs font-medium text-ink-500 uppercase tracking-wider">
                  User Name
                </th>
                {/* User Category - Hidden on medium screens, shown on large screens */}
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-left text-xs font-medium text-ink-500 uppercase tracking-wider hidden lg:table-cell">
                  User Category
                </th>
                {/* Location Assignment - Hidden on small-medium screens, shown on large screens */}
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-left text-xs font-medium text-ink-500 uppercase tracking-wider hidden xl:table-cell">
                  Location Assignment
                </th>
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-center text-xs font-medium text-ink-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-3 lg:px-4 xl:px-6 py-3 text-center text-xs font-medium text-ink-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-ink-200">
              {loading ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center">
                    <div className="flex justify-center items-center">
                      <div className="ds-spinner"></div>
                      <span className="ml-3 text-ink-500">
                        Loading user accounts...
                      </span>
                    </div>
                  </td>
                </tr>
              ) : displayedAccounts.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center">
                    <div className="text-ink-500">
                      <i className="fas fa-users text-lg text-ink-400"></i>
                      <p className="font-medium">No user accounts found</p>
                      <p className="text-sm">
                        Click "Add User" to create User account
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                displayedAccounts.map((account, index) => (
                  <tr
                    key={account.user_id}
                    className={`
                      transition-colors duration-200 ease-in-out
                      ${index % 2 === 0 ? "bg-white" : "bg-ink-50"}
                      hover:bg-navy-50 hover:shadow-sm cursor-pointer
                    `}
                  >
                    <td className="px-3 lg:px-4 xl:px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-mono font-medium text-ink-900">
                        {account.user_id}
                      </div>
                      {/* User Category for medium screens - shown as secondary text */}
                      <div className="text-xs text-ink-500 lg:hidden mt-1">
                        <span
                          className={`inline-flex px-1.5 py-0.5 text-xs font-medium rounded-full ${getCategoryColor(
                            account.user_cat
                          )}`}
                        >
                          {account.user_cat}
                        </span>
                      </div>
                    </td>
                    <td className="px-3 lg:px-4 xl:px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-mono font-medium text-ink-900">
                        {account.epf_num || "N/A"}
                      </div>
                    </td>
                    <td className="px-3 lg:px-4 xl:px-6 py-4">
                      <div className="text-sm font-medium text-ink-900 max-w-xs truncate">
                        {account.user_name}
                      </div>
                      {/* Location Assignment for small-medium screens - shown as secondary text */}
                      <div className="text-xs text-ink-500 xl:hidden mt-1">
                        {formatLocationDisplay(account)}
                      </div>
                    </td>
                    {/* User Category - Hidden on medium screens, shown on large screens */}
                    <td className="px-3 lg:px-4 xl:px-6 py-4 whitespace-nowrap hidden lg:table-cell">
                      <span
                        className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getCategoryColor(
                          account.user_cat
                        )}`}
                      >
                        {account.user_cat}
                      </span>
                    </td>
                    {/* Location Assignment - Hidden on small-medium screens, shown on large screens */}
                    <td className="px-3 lg:px-4 xl:px-6 py-4 hidden xl:table-cell">
                      <div className="text-sm text-ink-700">
                        {formatLocationDisplay(account)}
                      </div>
                    </td>
                    <td className="px-3 lg:px-4 xl:px-6 py-4 whitespace-nowrap text-center">
                      <div className="flex flex-col items-center space-y-1">
                        <StatusToggle
                          userId={account.user_id}
                          status={account.status}
                          onToggle={handleToggleStatus}
                          disabled={loading}
                        />
                        <span
                          className={`text-xs font-medium ${
                            account.status === 1
                              ? "text-success-600"
                              : "text-ink-500"
                          }`}
                        >
                          {account.status === 1 ? "Active" : "Inactive"}
                        </span>
                      </div>
                    </td>
                    <td className="px-3 lg:px-4 xl:px-6 py-4 whitespace-nowrap text-center">
                      <div className="flex justify-center">
                        <button
                          onClick={() => handleEditClick(account)}
                          disabled={loading}
                          className="bg-navy-500 hover:bg-navy-600 disabled:bg-navy-300 text-white px-2 lg:px-3 py-1.5 rounded text-xs transition-colors duration-200 flex items-center focus:outline-none whitespace-nowrap"
                          title="Edit user account"
                        >
                          <i className="fas fa-edit mr-1"></i>
                          <span className="hidden sm:inline">Edit</span>
                          <span className="sm:hidden">Edit</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Mobile View - Card Layout with Hover Effects */}
      <div className="md:hidden">
        {loading ? (
          <div className="px-4 py-8 text-center">
            <div className="flex justify-center items-center">
              <div className="ds-spinner"></div>
              <span className="ml-3 text-ink-500 text-sm">
                Loading user accounts...
              </span>
            </div>
          </div>
        ) : displayedAccounts.length === 0 ? (
          <div className="px-4 py-8 text-center">
            <div className="text-ink-500">
              <i className="fas fa-users text-lg text-ink-400"></i>
              <p className="font-medium text-sm">No user accounts found</p>
              <p className="text-xs">Click "Add User" to create User account</p>
            </div>
          </div>
        ) : (
          <div className="divide-y divide-ink-200">
            {displayedAccounts.map((account, index) => (
              <div
                key={account.user_id}
                className={`
                  p-3 transition-all duration-200 ease-in-out transform hover:scale-[1.02] hover:shadow-md cursor-pointer
                  ${index % 2 === 0 ? "bg-white" : "bg-ink-50"}
                  hover:bg-navy-50 border-transparent hover:border-navy-300
                `}
              >
                <div className="space-y-2">
                  <div className="flex justify-between items-start">
                    <div className="flex-1 min-w-0">
                      <div className="grid grid-cols-[auto,1fr] gap-x-4 gap-y-1 items-center">
                        {/* User ID */}
                        <span className="text-xs text-ink-500 whitespace-nowrap">
                          User ID:
                        </span>
                        <span className="text-sm font-mono font-medium text-ink-900">
                          {account.user_id}
                        </span>

                        {/* EPF Number */}
                        <span className="text-xs text-ink-500 whitespace-nowrap">
                          EPF:
                        </span>
                        <span className="text-sm font-mono text-ink-900">
                          {account.epf_num || "N/A"}
                        </span>

                        {/* User Name */}
                        <span className="text-xs text-ink-500 whitespace-nowrap">
                          Name:
                        </span>
                        <span className="text-sm text-ink-900 truncate">
                          {account.user_name}
                        </span>

                        {/* User Category */}
                        <span className="text-xs text-ink-500 whitespace-nowrap">
                          Category:
                        </span>
                        <span
                          className={`text-xs font-medium px-1.5 py-0.5 rounded-full ${getCategoryColor(
                            account.user_cat
                          )} w-fit`}
                        >
                          {account.user_cat}
                        </span>

                        {/* Location Assignment */}
                        <span className="text-xs text-ink-500 whitespace-nowrap">
                          Location:
                        </span>
                        <span className="text-xs text-ink-700">
                          {formatLocationDisplay(account)}
                        </span>
                      </div>
                    </div>

                    {/* Status and Edit Button */}
                    <div className="flex flex-col items-end space-y-2 ml-3">
                      <div className="flex flex-col items-center space-y-1">
                        <StatusToggle
                          userId={account.user_id}
                          status={account.status}
                          onToggle={handleToggleStatus}
                          disabled={loading}
                        />
                        <span
                          className={`text-xs font-medium ${
                            account.status === 1
                              ? "text-success-600"
                              : "text-ink-500"
                          }`}
                        >
                          {account.status === 1 ? "Active" : "Inactive"}
                        </span>
                      </div>
                      <button
                        onClick={() => handleEditClick(account)}
                        disabled={loading}
                        className="bg-navy-500 hover:bg-navy-600 disabled:bg-navy-300 text-white px-2 py-1.5 rounded text-xs transition-colors duration-200 flex items-center focus:outline-none whitespace-nowrap"
                        title="Edit user account"
                      >
                        <i className="fas fa-edit mr-1"></i>
                        Edit
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Pagination */}
      {!loading && filteredAccounts.length > 0 && getTotalPages() > 1 && (
        <div className="px-3 sm:px-6 py-4 border-t border-ink-200">
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
                        className={`px-2 sm:px-3 py-1.5 sm:py-2.5 text-xs sm:text-sm font-medium rounded-md focus:outline-none ${
                          currentPage === page
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
  );
};

export default AccountsTable;
