import { api } from './api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types';
import { decodeJWT, isTokenValid, getRoleFromToken } from '../lib/utils';
import { Role } from '../types';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    
    // Validate token before storing
    if (!isTokenValid(response.data.token)) {
      throw new Error('Received invalid token from server');
    }

    // Decode JWT and add role to user object
    const decoded = decodeJWT(response.data.token);
    if (decoded && decoded.role) {
      response.data.user.role = decoded.role as Role;
    }
    
    return response.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', data);
    
    // Validate token before storing
    if (!isTokenValid(response.data.token)) {
      throw new Error('Received invalid token from server');
    }

    // Decode JWT and add role to user object
    const decoded = decodeJWT(response.data.token);
    if (decoded && decoded.role) {
      response.data.user.role = decoded.role as Role;
    }
    
    return response.data;
  },

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getToken(): string | null {
    const token = localStorage.getItem('token');

    // Validate token on retrieval
    if (token && !isTokenValid(token)) {
      this.logout();
      return null;
    }

    return token;
  },

  setToken(token: string): void {
    if (isTokenValid(token)) {
      localStorage.setItem('token', token);
    } else {
      throw new Error('Attempted to store invalid token');
    }
  },

  getStoredUser(): string | null {
    return localStorage.getItem('user');
  },

  setStoredUser(user: string): void {
    localStorage.setItem('user', user);
  },

  isAuthenticated(): boolean {
    const token = this.getToken();
    return token !== null && isTokenValid(token);
  },

  getRoleFromToken(token: string): string | null {
    return getRoleFromToken(token);
  },

  /**
   * Validate current session
   * Checks if token exists and is valid
   */
  validateSession(): boolean {
    const token = this.getToken();
    if (!token || !isTokenValid(token)) {
      this.logout();
      return false;
    }
    return true;
  }
};
