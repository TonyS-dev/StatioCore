import { create } from 'zustand';
import type { User } from '../types';
import { authService } from '../services/authService';
import { isTokenValid, getRoleFromToken } from '../lib/utils';
import { Role } from '../types';

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setAuth: (token: string, user: User) => void;
  logout: () => void;
  initAuth: () => void;
  validateAuth: () => boolean;
  getUserRole: () => Role | null;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: null,
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setAuth: (token: string, user: User) => {
    // Validate token before setting
    if (!isTokenValid(token)) {
      console.error('Attempted to set invalid token');
      get().logout();
      return;
    }

    // SECURITY FIX: Extract role from JWT token, not from user object
    const roleFromToken = getRoleFromToken(token);
    if (!roleFromToken) {
      console.error('Token does not contain valid role');
      get().logout();
      return;
    }

    authService.setToken(token);

    // SECURITY FIX: Never store role in sessionStorage - it can be tampered with
    // Store only non-sensitive user data (id, email, fullName)
    const userToStore = {
      id: user.id,
      email: user.email,
      fullName: user.fullName,
      // DO NOT store role here - it will be extracted from JWT on every check
    };
    authService.setStoredUser(JSON.stringify(userToStore));

    // In-memory state includes role from token for UI rendering
    const userWithRole: User = {
      ...userToStore,
      role: roleFromToken as Role,
      isActive: true,
      createdAt: user.createdAt || new Date().toISOString(),
      updatedAt: user.updatedAt || new Date().toISOString()
    };

    set({
      token,
      user: userWithRole,
      isAuthenticated: true,
      isLoading: false
    });
  },

  logout: () => {
    authService.logout();
    set({
      token: null,
      user: null,
      isAuthenticated: false,
      isLoading: false
    });
  },

  initAuth: () => {
    const token = authService.getToken();
    const userStr = authService.getStoredUser();

    // Validate token immediately
    if (!token || !isTokenValid(token)) {
      authService.logout();
      set({
        token: null,
        user: null,
        isAuthenticated: false,
        isLoading: false
      });
      return;
    }

    // SECURITY FIX: Always extract role from JWT token, never trust sessionStorage
    const roleFromToken = getRoleFromToken(token);
    if (!roleFromToken) {
      console.error('Token does not contain valid role');
      authService.logout();
      set({
        token: null,
        user: null,
        isAuthenticated: false,
        isLoading: false
      });
      return;
    }

    if (userStr) {
      try {
        const storedUser = JSON.parse(userStr);

        // SECURITY FIX: Build user object with role from JWT, not from storage
        const user: User = {
          id: storedUser.id,
          email: storedUser.email,
          fullName: storedUser.fullName,
          role: roleFromToken as Role, // Always from token, never from storage
          isActive: storedUser.isActive !== undefined ? storedUser.isActive : true,
          createdAt: storedUser.createdAt || new Date().toISOString(),
          updatedAt: storedUser.updatedAt || new Date().toISOString()
        };

        set({
          token,
          user,
          isAuthenticated: true,
          isLoading: false
        });
      } catch (error) {
        console.error('Failed to parse stored user:', error);
        authService.logout();
        set({
          token: null,
          user: null,
          isAuthenticated: false,
          isLoading: false
        });
      }
    } else {
      set({ isLoading: false });
    }
  },

  /**
   * Validate current authentication state
   * Call this periodically or on critical actions
   */
  validateAuth: (): boolean => {
    const { token } = get();

    if (!token || !isTokenValid(token)) {
      get().logout();
      return false;
    }

    return true;
  },

  /**
   * SECURITY: Get user role directly from JWT token
   * This prevents role tampering via sessionStorage manipulation
   * ALWAYS use this method for authorization checks, never trust user.role from state
   */
  getUserRole: (): Role | null => {
    const { token } = get();

    if (!token || !isTokenValid(token)) {
      return null;
    }

    const role = getRoleFromToken(token);
    return role as Role | null;
  },
}));
