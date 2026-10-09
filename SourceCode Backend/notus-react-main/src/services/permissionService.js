import { apiPath } from '../config';

// Registry mapping func_id or sub_func_id identifiers to UI route paths and FontAwesome icon classes
export const ROUTE_PERMISSION_MAP = {
  // Dashboard
  "DASBRD": { path: "/admin/dashboard", icon: "fas fa-home", label: "Dash Board" },

  // System Admin
  "FUNMNG": { path: "/systemAdmin/administration/functions", icon: "fas fa-cogs", label: "Function Management" },
  
  // Tariff Management
  "LIVTRF": { path: "/tariff/live", icon: "fas fa-bolt", label: "Live Tariff" },
  "TRFHTR": { path: "/tariff/current", icon: "fas fa-bolt", label: "Tariff Types" },
  "TRFSTP": { path: "/tariff/yearly-setup", icon: "fas fa-sliders-h", label: "Yearly Tariff Setup" },
  "TRFSETUP": { path: "/tariff/yearly-setup", icon: "fas fa-sliders-h", label: "Yearly Tariff Setup" },
  "YTRFSTP": { path: "/tariff/yearly-setup", icon: "fas fa-sliders-h", label: "Yearly Tariff Setup" },
  
  // Monthly Readings
  "REDENT": { path: "/monthlyReadings/readingsEntry", icon: "fas fa-pen-alt", label: "Readings Entry" },
  "PENRED": { path: "/pendReadings", icon: "fas fa-clock", label: "Pending Readings" },
  "ALLRED": { path: "/tempReadings", icon: "fas fa-check-circle", label: "Received Readings" },
  "ERRRED": { path: "/tempReadings?filter=error", icon: "fas fa-exclamation-triangle", label: "Error Readings" },

  // Developer Management
  "DEVMNG": { path: "/developerRegistration", icon: "fas fa-user-plus", label: "Developer Management" },
  
  // Reports
  "DEVREP": { path: "/reports/developer", icon: "fas fa-file-pdf", label: "Developer Report" },
  "TRFREP": { path: "/reports/tariff-rates", icon: "fas fa-file-pdf", label: "Tariff Rate Report" },
  
  // Bill Month & Invoice Checklist
  "INVCHK": { path: "/monthly/invoice-checklist", icon: "fas fa-clipboard-check", label: "Invoice Creation Status" },

  // Invoice Management
  "INVMNG": { path: "/admin/invoices", icon: "fas fa-file-invoice", label: "Invoice Management" },

  // Bill Cycle Ending
  "BLMEND": { path: "/admin/bill-cycle-ending", icon: "fas fa-calendar-check", label: "Bill Month Ending" },
  // RU Management
  "CHGRUD": { path: "/admin/change-ru", icon: "fas fa-balance-scale", label: "Change RU Difference" },

  // Map View
  "MAPVIV": { path: "/admin/maps", icon: "fas fa-map-marked-alt", label: "Map View" },
  
  // Amendments
  "AMDCUST": { path: "/amendment/customers", icon: "fas fa-plus", label: "Add Amendment" },
  "AMD_CUST_ADD": { path: "/amendment/customers", icon: "fas fa-plus", label: "Add Amendment" },
  "AMDREJ": { path: "/amendment/rejected", icon: "fas fa-times-circle", label: "Rejected" },
  "AMD_REJECTED": { path: "/amendment/rejected", icon: "fas fa-times-circle", label: "Rejected" },
  "AMDMAST": { path: "/amendment", icon: "fas fa-edit", label: "Master Amendment" },
  "AMD_MASTER": { path: "/amendment", icon: "fas fa-edit", label: "Master Amendment" },
  "AMDPOST": { path: "/amendment/posted", icon: "fas fa-check-circle", label: "Posted Amendment" },
  "AMD_POSTED": { path: "/amendment/posted", icon: "fas fa-check-circle", label: "Posted Amendment" },

  // Meter Amendments
  "MCHG": { path: "/meterAmendment/customers", icon: "fas fa-pen-alt", label: "Meter Changes" },
  "MTR_AMD_CHANGES": { path: "/meterAmendment/customers", icon: "fas fa-pen-alt", label: "Meter Changes" },
  "MAPP": { path: "/meterAmendment/approval", icon: "fas fa-check-double", label: "Meter Approval" },
  "MTR_AMD_APPROVAL": { path: "/meterAmendment/approval", icon: "fas fa-check-double", label: "Meter Approval" },

  // Journals
  "JRNENT": { path: "/journals", icon: "fas fa-book", label: "Journal Entries" },
  "JOURNALS_ENTRIES": { path: "/journals", icon: "fas fa-book", label: "Journal Entries" },
  "JRNRPT": { path: "/journals/report", icon: "fas fa-file-invoice", label: "Journals Report" },
  "JOURNALS_REPORT": { path: "/journals/report", icon: "fas fa-file-invoice", label: "Journals Report" },
  "JRNCNF": { path: "/journals/confirmation", icon: "fas fa-check-circle", label: "Journals Confirmation" },
  "JOURNALS_CONFIRMATION": { path: "/journals/confirmation", icon: "fas fa-check-circle", label: "Journals Confirmation" },

  // Customer Onboarding / New Commission
  "ONBD": { path: "/newcommission", icon: "fas fa-file-alt", label: "Customer Onboarding" },
  "CUST_ONBOARDING": { path: "/newcommission", icon: "fas fa-file-alt", label: "Customer Onboarding" },

  // Other Features
  "NCRDEV": { path: "/bulkCustomers", icon: "fas fa-users", label: "Bulk Customers" },
  "TEMP_PAYMENTS": { path: "/tempPayments", icon: "fas fa-money-bill-wave", label: "Temp Payments" }
};

export function resolvePermissionIcon(permission = {}) {
  const mapped = ROUTE_PERMISSION_MAP[
    String(permission.subFuncId || permission.funcId || '').trim().toUpperCase()
  ];
  if (mapped && mapped.icon) return mapped.icon;

  const value = normalizeLookupValue(
    permission.subFuncNm || permission.funcNm || permission.routePath || ''
  );

  if (value.includes('dashboard')) return 'fas fa-home';
  if (value.includes('admin') || value.includes('account')) return 'fas fa-cogs';
  if (value.includes('tariff')) return 'fas fa-bolt';
  if (value.includes('pending')) return 'fas fa-clock';
  if (value.includes('error')) return 'fas fa-exclamation-triangle';
  if (value.includes('received') || value.includes('completed')) return 'fas fa-check-circle';
  if (value.includes('reading')) return 'fas fa-pen-alt';
  if (value.includes('developer')) return 'fas fa-user-plus';
  if (value.includes('report')) return 'fas fa-file-pdf';
  if (value.includes('invoice')) return 'fas fa-file-invoice';
  if (value.includes('map')) return 'fas fa-map-marked-alt';
  if (value.includes('journal')) return 'fas fa-book';
  if (value.includes('amendment')) return 'fas fa-edit';
  if (value.includes('payment')) return 'fas fa-money-bill-wave';
  if (value.includes('customer') || value.includes('bulk')) return 'fas fa-users';
  if (value.includes('bill') || value.includes('month') || value.includes('cycle')) return 'fas fa-calendar-alt';
  return 'fas fa-folder';
}

const normalizeLookupValue = (value = '') => {
  if (value == null) return '';

  return String(value)
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
};

const labelRouteLookup = Object.values(ROUTE_PERMISSION_MAP).reduce((acc, entry) => {
  if (entry && entry.path && entry.label) {
    acc[normalizeLookupValue(entry.label)] = entry.path;
  }
  return acc;
}, {});

export function resolveRoutePathFromPermission(permission = {}) {
  const candidates = [
    { value: permission.subFuncId, type: 'id' },
    { value: permission.funcId, type: 'id' },
    { value: permission.subFuncNm, type: 'label' },
    { value: permission.funcNm, type: 'label' }
  ];

  for (const candidate of candidates) {
    if (!candidate.value) continue;

    if (candidate.type === 'id') {
      const mapped = ROUTE_PERMISSION_MAP[String(candidate.value).trim().toUpperCase()];
      if (mapped && mapped.path) {
        return mapped.path;
      }
    } else {
      const lookupKey = normalizeLookupValue(candidate.value);
      if (labelRouteLookup[lookupKey]) {
        return labelRouteLookup[lookupKey];
      }
    }
  }

  return null;
}

/**
 * Fetch permissions and sidebar tree for the authenticated session user
 */
export async function fetchUserPermissions(sessionId, applId = 'NCR') {
  if (!sessionId) {
    return { success: false, permissions: [], sidebarFunctions: [] };
  }

  try {
    const response = await fetch(apiPath(`/api/v1/auth/permissions?appl_id=${applId}`), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId
      }
    });

    if (!response.ok) {
      console.warn('Failed to fetch user permissions:', response.status);
      return { success: false, permissions: [], sidebarFunctions: [] };
    }

    const data = await response.json();
    if (data.success && data.data) {
      console.log("[permissionService] Logged-in User ID:", data.data.userId);
      console.log("[permissionService] Permissions returned:", data.data.permissions);
      console.log("[permissionService] Sidebar Functions returned:", data.data.sidebarFunctions);
      const bmFunc = (data.data.sidebarFunctions || []).find(f => (f.funcId || '').toUpperCase() === 'BM');
      if (bmFunc) {
        console.log("[permissionService] BM SubFunctions:", bmFunc.subFunctions);
      }
      return {
        success: true,
        userId: data.data.userId,
        applId: data.data.applId,
        permissions: data.data.permissions || [],
        sidebarFunctions: data.data.sidebarFunctions || []
      };
    }

    return { success: false, permissions: [], sidebarFunctions: [] };
  } catch (error) {
    console.error('Error fetching permissions from backend:', error);
    return { success: false, permissions: [], sidebarFunctions: [] };
  }
}

/**
 * Helper to check if permission list grants access to a specific route URL path
 */
export function canAccessPath(permissions = [], routePath = '') {
  if (!routePath || routePath === '/auth/login' || routePath === '/auth') {
    return true;
  }

  // Dashboard is accessible to all authenticated users
  if (routePath === '/admin/dashboard' || routePath === '/admin') {
    return true;
  }

  if (!permissions || permissions.length === 0) {
    return false;
  }

  const cleanPath = routePath.split('?')[0].trim().toLowerCase();

  return permissions.some(p => {
    const permissionPath = resolveRoutePathFromPermission(p);

    if (permissionPath) {
      const mappedPath = permissionPath.split('?')[0].trim().toLowerCase();
      if (mappedPath === cleanPath || cleanPath.startsWith(mappedPath) || mappedPath.startsWith(cleanPath)) {
        return true;
      }
    }

    return false;
  });
}
