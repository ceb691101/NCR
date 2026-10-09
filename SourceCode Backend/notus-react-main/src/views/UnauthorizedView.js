import React from "react";
import { Link } from "react-router-dom";

export default function UnauthorizedView() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-ink-100 p-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center border border-ink-200">
        <div className="w-16 h-16 bg-critical-100 text-critical-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">
          <i className="fas fa-lock"></i>
        </div>
        <h2 className="text-2xl font-bold text-ink-800 mb-2">403 - Access Denied</h2>
        <p className="text-ink-600 mb-6 text-sm">
          You do not have active database permissions to access this page or functionality.
        </p>
        <Link
          to="/admin/dashboard"
          className="inline-flex items-center justify-center px-5 py-2.5 bg-navy-800 text-white font-medium text-sm rounded-xl hover:bg-navy-950 transition-colors shadow-sm"
        >
          <i className="fas fa-home mr-2"></i> Return to Dashboard
        </Link>
      </div>
    </div>
  );
}
