import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '../types/index';
import { getErrorMessage, getErrorMeta } from '../lib/getErrorMessage';
import { isTokenValid } from '../lib/utils';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request interceptor - Add JWT token to all requests and validate it
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');

    // Validate token before making request
    if (token) {
      if (!isTokenValid(token)) {
        // Token is invalid or expired, clear storage and redirect
        localStorage.removeItem('token');
        localStorage.removeItem('user');

        // Only redirect if not already on login page
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }

        // Reject the request
        return Promise.reject(new Error('Token expired or invalid'));
      }

      if (config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    // Handle 401 Unauthorized - Token expired or invalid
    if (error.response?.status === 401) {
      const storedToken = localStorage.getItem('token');

      // If we have a stored token, treat 401 as session expiration
      if (storedToken) {
        console.warn('Session expired - 401 response from server');
        localStorage.removeItem('token');
        localStorage.removeItem('user');

        // Only redirect if not already on login page
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      }
      // If there's no stored token (e.g. login attempt with invalid credentials),
      // don't auto-redirect so the UI can show the backend error message
    }

    // Handle 403 Forbidden - Insufficient permissions
    if (error.response?.status === 403) {
      console.error('Access denied - insufficient permissions');
    }

    const message = getErrorMessage(error, 'An unexpected error occurred');
    const meta = getErrorMeta(error);

    // Create standardized error object
    const apiError: ApiError = {
      message,
      status: error.response?.status,
      timestamp: meta.timestamp,
      path: meta.path,
    };

    return Promise.reject(apiError);
  }
);

export default api;
