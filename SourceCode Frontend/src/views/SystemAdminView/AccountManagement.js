import React, { useState, useEffect } from "react";
import AccountsTable from "components/SystemAdminComp/AccountsTable";
import AddAccountDialog from "components/SystemAdminComp/AddAccountDialog";
import UnsavedChangesModal from "components/Modal/UnsavedChangesModal";
import { toast } from "react-toastify";
import {
  getAllUserAccounts,
  createUserAccount,
  updateUserAccount,
  toggleUserStatus,
  transformBackendToFrontend
} from "services/userAccountService";
import { useHistory } from "react-router-dom";

const AccountManagement = ({ onAccountsChanged, embedded = false }) => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editAccount, setEditAccount] = useState(null);
  
  // New states for breadcrumb and unsaved changes
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [showUnsavedModal, setShowUnsavedModal] = useState(false);
  const [pendingNavigation, setPendingNavigation] = useState(null);
  
  const history = useHistory();

  useEffect(() => {
    fetchAccounts();
  }, []);

  // Handle browser back button and unsaved changes
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (checkUnsavedChanges()) {
        e.preventDefault();
        e.returnValue = '';
        return '';
      }
    };

    const handlePopState = (e) => {
      if (checkUnsavedChanges()) {
        e.preventDefault();
        setShowUnsavedModal(true);
        setPendingNavigation(() => () => {
          // Go back in history
          history.go(-1);
        });
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
      window.removeEventListener('popstate', handlePopState);
    };
  }, [hasUnsavedChanges, history]);

  // Function to check if there are unsaved changes
  const checkUnsavedChanges = () => {
    // Check if we're in edit mode with unsaved changes
    // You can customize this logic based on your form state
    return hasUnsavedChanges || (showAddDialog && editAccount);
  };

  // Handle breadcrumb back click
  const handleBackClick = () => {
    if (checkUnsavedChanges()) {
      setShowUnsavedModal(true);
      setPendingNavigation(() => () => history.push("/admin/dashboard"));
    } else {
      history.push("/admin/dashboard");
    }
  };

  // Handle leaving the page (from modal)
  const handleLeavePage = () => {
    setShowUnsavedModal(false);
    if (pendingNavigation) {
      pendingNavigation();
    }
    // Reset unsaved changes flag
    setHasUnsavedChanges(false);
    // Close any open dialog
    if (showAddDialog) {
      setShowAddDialog(false);
      setEditAccount(null);
    }
  };

  // Handle staying on the page (from modal)
  const handleStayOnPage = () => {
    setShowUnsavedModal(false);
    setPendingNavigation(null);
  };

  // Handle when user makes edits in the form
  const handleFormEdit = () => {
    setHasUnsavedChanges(true);
  };

  const fetchAccounts = async () => {
    setLoading(true);
    
    try {      
      const response = await getAllUserAccounts();
      
      // Transform backend response to frontend format
      const transformedAccounts = response.map(transformBackendToFrontend);
      setAccounts(transformedAccounts);

      console.log('Successfully fetched accounts from backend:', transformedAccounts);
    } catch (err) {
      console.error('Error fetching accounts from backend:', err);
      toast.error("Failed to fetch accounts from server.");
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddAccount = async (accountData) => {
    setLoading(true);
    
    try {      
      const createdAccount = await createUserAccount(accountData);
      const transformedAccount = transformBackendToFrontend(createdAccount);
      setAccounts(prevAccounts => [...prevAccounts, transformedAccount]);
      
      toast.success("User account created successfully!");
      setShowAddDialog(false);
      setHasUnsavedChanges(false); // Reset unsaved changes after save
      onAccountsChanged?.();
      
      console.log('Successfully created account:', transformedAccount);
      
    } catch (err) {
      console.error('Error adding account:', err);
      toast.error(`Failed to create user account: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleEditAccount = (account) => {
    setEditAccount(account);
    setShowAddDialog(true);
    setHasUnsavedChanges(false); // Reset when starting edit
  };

  const handleUpdateAccount = async (accountData) => {
    setLoading(true);
    
    try {
      const updatedAccount = await updateUserAccount(editAccount.user_id, accountData);
      const transformedAccount = transformBackendToFrontend(updatedAccount);
      setAccounts(prevAccounts => 
        prevAccounts.map(account => 
          account.user_id === editAccount.user_id 
            ? transformedAccount
            : account
        )
      );
      
      toast.success("User account updated successfully!");
      setShowAddDialog(false);
      setEditAccount(null);
      setHasUnsavedChanges(false); // Reset unsaved changes after save
      onAccountsChanged?.();
      
      console.log('Successfully updated account:', transformedAccount);
      
    } catch (err) {
      console.error('Error updating account:', err);
      toast.error(`Failed to update user account: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleUserStatus = async (userId, currentStatus) => {
    setLoading(true);
    
    try {
      const updatedAccount = await toggleUserStatus(userId);
      const transformedAccount = transformBackendToFrontend(updatedAccount);
      setAccounts(prevAccounts => 
        prevAccounts.map(account => 
          account.user_id === userId 
            ? transformedAccount
            : account
        )
      );
      
      const statusText = transformedAccount.status === 1 ? 'activated' : 'deactivated';
      toast.success(`User account ${statusText} successfully!`);
      onAccountsChanged?.();
      
      console.log('Successfully toggled user status:', transformedAccount);
      
    } catch (err) {
      console.error('Error toggling user status:', err);
      toast.error(`Failed to toggle user status: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCloseDialog = () => {
    if (hasUnsavedChanges) {
      // Show confirmation before closing
      if (window.confirm("You have unsaved changes. Are you sure you want to close?")) {
        setShowAddDialog(false);
        setEditAccount(null);
        setHasUnsavedChanges(false);
      }
    } else {
      setShowAddDialog(false);
      setEditAccount(null);
    }
  };

  // Breadcrumb items
  const breadcrumbItems = [
    { label: "Dashboard", href: "/admin/dashboard" },
    { label: "User Administration", href: null }
  ];

  return (
    <div className={embedded ? "w-full" : "container mx-auto rounded-lg mb-20 px-2 sm:px-4"}>
      <UnsavedChangesModal
        isOpen={showUnsavedModal}
        onLeave={handleLeavePage}
        onStay={handleStayOnPage}
      />
      
      <div className={embedded ? "w-full" : "mb-8 mx-0 sm:mx-2 mt-5 rounded-lg"}>
        {/* Main Content */}
        <AccountsTable
          accounts={accounts}
          onAddAccount={() => setShowAddDialog(true)}
          onEditAccount={handleEditAccount}
          onToggleUserStatus={handleToggleUserStatus}
          loading={loading}
          // Pass breadcrumb props
          breadcrumbItems={breadcrumbItems}
          onBackClick={handleBackClick}
          hasUnsavedChanges={hasUnsavedChanges}
        />

        {/* Add/Edit Account Dialog - Updated to track unsaved changes */}
        {showAddDialog && (
          <AddAccountDialog
            isOpen={showAddDialog}
            onClose={handleCloseDialog}
            onSave={editAccount ? handleUpdateAccount : handleAddAccount}
            account={editAccount}
            loading={loading}
            onFormEdit={handleFormEdit}
          />
        )}
      </div>
    </div>
  );
};

export default AccountManagement;