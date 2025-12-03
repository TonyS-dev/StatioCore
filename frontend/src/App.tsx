
import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { ProtectedRoute } from '@/components/ProtectedRoute';
import { UserRole } from '@/types';

// Pages
import { LoginPage } from '@/pages/auth/LoginPage';
import { RegisterPage } from '@/pages/auth/RegisterPage';
import { DashboardPage } from '@/pages/user/DashboardPage';
import { ReservationPage } from '@/pages/user/ReservationPage';
import { ParkingManagementPage } from '@/pages/user/ParkingManagementPage';
import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { UserManagementPage } from '@/pages/admin/UserManagementPage';
import { ParkingSystemManagementPage } from '@/pages/admin/ParkingSystemManagementPage';
import { LogSystemPage } from '@/pages/admin/LogSystemPage';

function RootRedirect() {
  const { user } = useAuthStore();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to={user.role === UserRole.ADMIN ? '/admin' : '/dashboard'} replace />;
}

function App() {
  const { restoreFromStorage } = useAuthStore();
  const [isRestored, setIsRestored] = useState(false);

  // Restore auth from localStorage on app mount
  useEffect(() => {
    restoreFromStorage();
    setIsRestored(true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!isRestored) {
    return <div>Loading...</div>;
  }

  return (
    <ErrorBoundary>
      <Router>
        <Routes>
        {/* Auth Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* User Routes */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requiredRole={UserRole.USER}>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/reservations"
          element={
            <ProtectedRoute requiredRole={UserRole.USER}>
              <ReservationPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/parking"
          element={
            <ProtectedRoute requiredRole={UserRole.USER}>
              <ParkingManagementPage />
            </ProtectedRoute>
          }
        />

        {/* Admin Routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole={UserRole.ADMIN}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requiredRole={UserRole.ADMIN}>
              <UserManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/parking"
          element={
            <ProtectedRoute requiredRole={UserRole.ADMIN}>
              <ParkingSystemManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/logs"
          element={
            <ProtectedRoute requiredRole={UserRole.ADMIN}>
              <LogSystemPage />
            </ProtectedRoute>
          }
        />

        {/* Default Routes */}
        <Route path="/" element={<RootRedirect />} />
        <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </ErrorBoundary>
  );
}

export default App;

