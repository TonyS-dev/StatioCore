import { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Role } from '../../types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles: Role[];
}

const ProtectedRoute = ({ children, allowedRoles }: ProtectedRouteProps) => {
  const { isAuthenticated, user, validateAuth, logout, getUserRole } = useAuthStore();

  // Validate JWT on mount and periodically
  useEffect(() => {
    // Initial validation
    if (!validateAuth()) {
      return;
    }

    // Set up periodic validation (every 30 seconds)
    const interval = setInterval(() => {
      if (!validateAuth()) {
        logout();
      }
    }, 30000); // 30 seconds

    return () => clearInterval(interval);
  }, [validateAuth, logout]);

  // Check authentication
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  // Get role from JWT token, not from stored user object
  // This prevents users from tampering with sessionStorage to escalate privileges
  const roleFromToken = getUserRole();

  if (!roleFromToken) {
    logout();
    return <Navigate to="/login" replace />;
  }

  // Check role authorization using JWT role, not user.role
  if (!allowedRoles.includes(roleFromToken)) {
    // Redirect to appropriate dashboard based on actual role from token
    const redirectPath = roleFromToken === Role.ADMIN ? '/admin/dashboard' : '/user/dashboard';
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
