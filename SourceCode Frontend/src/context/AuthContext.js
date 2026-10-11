import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  clearAreaAndBill,
  setAreaAndBill,
  getAreaAndBill,
} from '../services/AreaAndBillService';
import { fetchUserPermissions, canAccessPath } from '../services/permissionService';
import { apiPath } from '../config';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [permissions, setPermissions] = useState([]);
  const [sidebarFunctions, setSidebarFunctions] = useState([]);
  const [permissionsLoaded, setPermissionsLoaded] = useState(false);

  const loadPermissions = useCallback(async (sessionId) => {
    if (!sessionId) {
      setPermissions([]);
      setSidebarFunctions([]);
      setPermissionsLoaded(true);
      return;
    }
    const res = await fetchUserPermissions(sessionId);
    if (res.success) {
      setPermissions(res.permissions || []);
      setSidebarFunctions(res.sidebarFunctions || []);
      console.log("[AuthContext] Loaded permissions count:", (res.permissions || []).length);
      console.log("[AuthContext] Loaded sidebarFunctions count:", (res.sidebarFunctions || []).length);
      try {
        sessionStorage.setItem('user_permissions', JSON.stringify(res.permissions || []));
      } catch (e) {}
    } else {
      setPermissions([]);
      setSidebarFunctions([]);
    }
    setPermissionsLoaded(true);
  }, []);

  const checkAuthStatus = useCallback(async () => {
    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');

    if (!sessionId || !userId) {
      setIsAuthenticated(false);
      setPermissions([]);
      setSidebarFunctions([]);
      setPermissionsLoaded(false);
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(apiPath('/api/v1/secinfo/validate-session'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_id: sessionId,
          user_id: userId
        })
      });

      const data = await response.json();
      if (data.valid) {
        // Keep the permitted areas in step with the server after a page refresh.
        if (Array.isArray(data.permitted_areas)) {
          try {
            const current = getAreaAndBill();
            setAreaAndBill({
              userCategory: data.user_info?.user_category || current?.userCategory || '',
              regionCode: data.user_info?.region_code ?? current?.regionCode ?? '',
              provinceCode: data.user_info?.province_code ?? current?.provinceCode ?? '',
              areaCode: data.user_info?.area_code ?? current?.areaCode ?? '',
              accessScope: data.user_info?.access_scope || current?.accessScope || '',
              permittedAreas: data.permitted_areas,
              selectedAreaCode: current?.selectedAreaCode || '',
            });
          } catch (e) {
            console.error('Failed to refresh permitted areas:', e);
          }
        }
        setIsAuthenticated(true);
        await loadPermissions(sessionId);
      } else {
        setIsAuthenticated(false);
        setPermissions([]);
        setSidebarFunctions([]);
      }
    } catch (error) {
      console.error('Session validation error:', error);
      setIsAuthenticated(false);
      setPermissions([]);
      setSidebarFunctions([]);
    }

    setLoading(false);
  }, [loadPermissions]);

  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  // Mark the user as logged in and persist session data
  const login = async (loginData) => {
    try {
      if (!loginData) return;
      if (loginData.session_id) sessionStorage.setItem('session_id', loginData.session_id);
      if (loginData.user_info) {
        sessionStorage.setItem('user_id', loginData.user_info.user_id || '');
        sessionStorage.setItem('user_name', loginData.user_info.user_name || '');
        sessionStorage.setItem('user_category', loginData.user_info.user_category || '');
        // A blank code means the user's scope covers everything below that level.
        sessionStorage.setItem('region_code', loginData.user_info.region_code || '');
        sessionStorage.setItem('province_code', loginData.user_info.province_code || '');
        sessionStorage.setItem('area_code', loginData.user_info.area_code || '');
        sessionStorage.setItem('access_scope', loginData.user_info.access_scope || '');
      }
      if (loginData.login_time) sessionStorage.setItem('login_time', loginData.login_time);
      if (loginData.expires_at) sessionStorage.setItem('expires_at', loginData.expires_at);

      // Seed the permitted areas returned by login. Nothing is selected: the user starts
      // on all of their permitted areas and can narrow down from the header bar.
      const permittedAreas =
        loginData.permitted_areas || loginData.user_info?.peritted_areas || [];
      try {
        clearAreaAndBill();
        setAreaAndBill({
          userCategory: loginData.user_info?.user_category || '',
          regionCode: loginData.user_info?.region_code || '',
          provinceCode: loginData.user_info?.province_code || '',
          areaCode: loginData.user_info?.area_code || '',
          accessScope: loginData.user_info?.access_scope || '',
          permittedAreas,
          selectedAreaCode: '',
        });
      } catch (e) {
        console.error('Failed to seed permitted areas:', e);
      }

      setIsAuthenticated(true);
      if (loginData.session_id) {
        await loadPermissions(loginData.session_id);
      }
    } catch (err) {
      console.error('Auth login error:', err);
    }
  };

  const logout = async () => {
    try {
      localStorage.setItem('ncre_logout_event', Date.now().toString());
    } catch (e) {}

    const sessionId = sessionStorage.getItem('session_id');
    const userId = sessionStorage.getItem('user_id');

    try {
      await fetch(apiPath('/api/v1/secinfo/logout'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          session_id: sessionId,
          user_id: userId
        })
      });
    } catch (error) {
      console.error('Logout error:', error);
    }

    // Clear session storage
    sessionStorage.clear();
    try { clearAreaAndBill(); } catch (e) { /* ignore */ }
    setIsAuthenticated(false);
    setPermissions([]);
    setSidebarFunctions([]);
  };

  const hasPermissionForRoute = (routePath) => {
    return canAccessPath(permissions, routePath);
  };

  return (
    <AuthContext.Provider value={{
      isAuthenticated,
      loading,
      logout,
      login,
      permissions,
      sidebarFunctions,
        permissionsLoaded,
      loadPermissions,
      hasPermissionForRoute
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
