import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

/**
 * Hook to use authentication state and actions
 */
export const useAuth = () => {
  const { token, user, isAuthenticated, isLoading, setAuth, logout, validateAuth } = useAuthStore();

  return {
    token,
    user,
    isAuthenticated,
    isLoading,
    setAuth,
    logout,
    validateAuth,
  };
};

/**
 * Hook to validate authentication on page load
 * Use this in sensitive pages that need extra security
 */
export const useAuthValidation = () => {
  const { validateAuth, logout } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!validateAuth()) {
      logout();
      navigate('/login', { replace: true });
    }
  }, [validateAuth, logout, navigate]);
};
