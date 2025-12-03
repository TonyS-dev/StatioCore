import { create } from 'zustand';
import type { User } from '../types';
import { authService } from '../services/authService';
import { isTokenValid, getRoleFromToken } from '../lib/utils';

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

    authService.setToken(token);
    authService.setStoredUser(JSON.stringify(user));
    set({
      token,
      user,
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

    if (userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        
        // Extract role from token if missing
        if (!user.role) {
          const role = getRoleFromToken(token);
          if (role) {
            user.role = role as any;
            authService.setStoredUser(JSON.stringify(user));
          } else {
            authService.logout();
            set({
              token: null,
              user: null,
              isAuthenticated: false,
              isLoading: false
            });
            return;
          }
        }
        
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
}));
