import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import UnauthorizedView from '../views/UnauthorizedView';

const ProtectedRoute = ({ children, ...rest }) => {
  const { isAuthenticated, loading, permissions, permissionsLoaded, hasPermissionForRoute } = useAuth();

  if (loading) {
    return (
      <div className="ds-page-loading" role="status" aria-live="polite">
        <span className="ds-spinner" aria-hidden="true"></span>
        <span className="ds-body-sm text-ink-600">Loading your workspace...</span>
      </div>
    );
  }

  return (
    <Route
      {...rest}
      render={({ location }) => {
        const hasSession = isAuthenticated || (typeof sessionStorage !== 'undefined' && sessionStorage.getItem('session_id'));
        if (!hasSession) {
          return (
            <Redirect
              to={{
                pathname: "/auth/login",
                state: { from: location }
              }}
            />
          );
        }

        // If permissions are loaded and user does not have permission for direct URL navigation
        if (permissionsLoaded && !hasPermissionForRoute(location.pathname)) {
          return <UnauthorizedView />;
        }

        return children;
      }}
    />
  );
};

export default ProtectedRoute;
