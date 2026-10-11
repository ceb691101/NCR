import React from "react";
// import ReactDOM from "react-dom";
import 'leaflet/dist/leaflet.css';
import { createRoot } from "react-dom/client";
import { BrowserRouter, Route, Switch, Redirect } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import "@fortawesome/fontawesome-free/css/all.min.css";
import "assets/styles/tailwind.css";
// Design tokens + `ds-*` component primitives for the authenticated app.
// Imported BEFORE index.css so that, once the bundler flattens Tailwind's
// cascade layers, the primitives sit underneath the utility layer - a utility
// such as `pl-10` or `bg-critical-50` can then override a primitive.
import "assets/styles/design-system.css";
import "assets/styles/index.css";

// layouts
import Admin from "layouts/Admin.js";
import Auth from "layouts/Auth.js";

import SystemAdmin from "layouts/SystemAdmin";
import ReadingsEntry from "layouts/ReadingsEntry";
import TempReadings from "layouts/TempReadings";
import BulkCustomers from "layouts/BulkCustomers";
import PendReadings from "layouts/PendReadings";
import DeveloperRegistration from "layouts/DeveloperRegistration";
import Tariff from "layouts/Tariff.js";
import JournalsLayout from "layouts/Journals";
import Reports from "layouts/Reports.js";



// views
import SessionCheck from "views/CheckSession";

// Auth Context and Protected Route
import { AuthProvider } from "./context/AuthContext";
import { AreaAndBillProvider } from "./context/AreaAndBillContext";
import { NavigationProvider } from "./context/NavigationContext";
import ProtectedRoute from "./components/ProtectedRoute";
import NewCommissionLayout from "layouts/newcommission";

import BulkCustomerAmendment from "views/Amendment/BulkCustomerAmendment";
import AmendmentViewLayout from "layouts/AmendmentViewLayout";
import MasterAmendmentLayout from "layouts/MasterAmendmentLayout";
import RejectedAmendmentLayout from "layouts/RejectedAmendmentLayout";
import MasterAmendmentPostLayout from "layouts/MasterAmendmentPostLayout";
import MeterAmendmentLayout from "layouts/MeterAmendmentLayout";
import MeterAmendmentApprovalLayout from "layouts/MeterAmendmentApprovalLayout";
import TempPaymentsLayout from "layouts/TempPaymentLayout";
import InvoiceChecklistLayout from 'layouts/InvoiceChecklistLayout';

import { warmupApiConnection } from './services/monthlyChargeService';

import {
  Chart as ChartJS,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Title,
  Tooltip,
  Legend,
  Filler,
  BarElement,
  ArcElement,
  RadialLinearScale
} from 'chart.js';

// Get the root element
const container = document.getElementById("root");
const root = createRoot(container);

warmupApiConnection().then(success => {
  console.log('API warmup completed:', success ? 'successful' : 'with issues');
});

ChartJS.register(
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Title,
  Tooltip,
  Legend,
  Filler,
  BarElement,
  ArcElement,
  RadialLinearScale
);

root.render(
  <BrowserRouter>
    <AuthProvider>
      <AreaAndBillProvider>
        <NavigationProvider>
          <>
            <SessionCheck />
          <Switch>
            {/* Public routes */}
            <Route path="/auth" component={Auth} />

            {/* Protected routes */}
            <ProtectedRoute path="/admin">
              <Admin />
            </ProtectedRoute>

            {/* my routes */}
            <ProtectedRoute path="/systemAdmin">
              <SystemAdmin />
            </ProtectedRoute>
            <ProtectedRoute path="/systemadmin">
              <SystemAdmin />
            </ProtectedRoute>
            <ProtectedRoute path="/monthlyReadings">
              <ReadingsEntry />
            </ProtectedRoute>
            <ProtectedRoute path="/tempReadings">
              <TempReadings />
            </ProtectedRoute>
            <ProtectedRoute path="/bulkCustomers">
              <BulkCustomers />
            </ProtectedRoute>
            <ProtectedRoute path="/developerRegistration">
              <DeveloperRegistration />
            </ProtectedRoute>
            <ProtectedRoute path="/pendReadings">
              <PendReadings />
            </ProtectedRoute>

            {/* isuranga */}
            <ProtectedRoute path="/newcommission">
              <NewCommissionLayout />
            </ProtectedRoute>

            {/* Tariff Management */}
            <ProtectedRoute path="/tariff">
              <Tariff />
            </ProtectedRoute>

            {/* Journals Management */}
            <ProtectedRoute path="/journals">
              <JournalsLayout />
            </ProtectedRoute>

            {/* Reports */}
            <ProtectedRoute path="/reports">
              <Reports />
            </ProtectedRoute>


            {/* NEW: Route for Amendment View Layout */}
            <ProtectedRoute path="/amendment/customers">
              <AmendmentViewLayout />
            </ProtectedRoute>

            {/* NEW: Route for Master Amendment (only accessible via menu/card for Accountant Assistance) */}
            <ProtectedRoute path="/amendment" exact>
              <MasterAmendmentLayout />
            </ProtectedRoute>
            {/* Rejected Amendments – Update Rejected Ones */}
            <ProtectedRoute path="/amendment/rejected">
              <RejectedAmendmentLayout />
            </ProtectedRoute>

            <ProtectedRoute path="/amendment/posted">
              <MasterAmendmentPostLayout />
            </ProtectedRoute>

            {/* NEW: Route for Meter Amendment */}
            <ProtectedRoute path="/meterAmendment/customers">
              <MeterAmendmentLayout />
            </ProtectedRoute>

            {/* NEW: Route for Meter Amendment Approval (AA1) */}
            <ProtectedRoute path="/meterAmendment/approval">
              <MeterAmendmentApprovalLayout />
            </ProtectedRoute>

            {/* New Route for tmp_payments */}
            <ProtectedRoute path="/tempPayments">
              <TempPaymentsLayout />
            </ProtectedRoute>
                        {/* Invoice Checklist Route */}
            <ProtectedRoute path="/monthly/invoice-checklist">
              <InvoiceChecklistLayout />
            </ProtectedRoute>
            {/* Default redirect */}
            <Redirect from="*" to="/auth/login" />
          </Switch>
            <ToastContainer position="top-right" autoClose={3000} />
          </>
        </NavigationProvider>
      </AreaAndBillProvider>
    </AuthProvider>
  </BrowserRouter>
);
