import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// ============= JWT UTILITIES =============

interface JWTPayload {
  sub: string;
  email?: string;
  role?: string;
  exp: number;
  iat: number;
  [key: string]: any;
}

/**
 * Decode JWT token to extract payload
 */
export const decodeJWT = (token: string): JWTPayload | null => {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) {
      return null;
    }

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );

    return JSON.parse(jsonPayload) as JWTPayload;
  } catch (error) {
    return null;
  }
};

/**
 * Check if JWT token is expired
 */
export const isTokenExpired = (token: string): boolean => {
  const payload = decodeJWT(token);
  if (!payload || !payload.exp) {
    return true;
  }

  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp < currentTime;
};

/**
 * Get time until token expiration in milliseconds
 */
export const getTimeUntilExpiration = (token: string): number => {
  const payload = decodeJWT(token);
  if (!payload || !payload.exp) {
    return 0;
  }

  const currentTime = Math.floor(Date.now() / 1000);
  const timeLeft = payload.exp - currentTime;

  return timeLeft > 0 ? timeLeft * 1000 : 0;
};

/**
 * Validate JWT token structure and expiration
 */
export const isTokenValid = (token: string | null): boolean => {
  if (!token) {
    return false;
  }

  const parts = token.split(".");
  if (parts.length !== 3) {
    return false;
  }

  return !isTokenExpired(token);
};

/**
 * Extract role from JWT token
 */
export const getRoleFromToken = (token: string): string | null => {
  const payload = decodeJWT(token);
  return payload?.role || null;
};

/**
 * Extract user ID from JWT token
 */
export const getUserIdFromToken = (token: string): string | null => {
  const payload = decodeJWT(token);
  return payload?.sub || null;
};

/**
 * Check if token will expire soon (within given minutes)
 */
export const isTokenExpiringSoon = (token: string, minutes: number = 5): boolean => {
  const timeLeft = getTimeUntilExpiration(token);
  const threshold = minutes * 60 * 1000;

  return timeLeft > 0 && timeLeft < threshold;
};

// ============= PARKING DURATION UTILITIES =============

/**
 * Format duration for parking sessions
 * @param checkIn - Check-in timestamp
 * @param checkOut - Check-out timestamp (optional)
 * @param storedDuration - Pre-calculated duration in minutes (optional)
 * @returns Formatted duration string
 */
export function formatParkingDuration(
  checkIn: string | null | undefined,
  checkOut: string | null | undefined,
  storedDuration?: number | null
): string {
  // Use stored duration if available (for completed sessions)
  if (storedDuration !== null && storedDuration !== undefined && storedDuration >= 0) {
    return formatMinutes(storedDuration);
  }

  // Calculate from timestamps
  if (!checkIn) return "-";

  const checkInTime = new Date(checkIn);
  if (isNaN(checkInTime.getTime())) return "-";

  const checkOutTime = checkOut ? new Date(checkOut) : null;

  // If checkOut is provided and valid
  if (checkOutTime && !isNaN(checkOutTime.getTime())) {
    // Validate check-out is after check-in
    if (checkOutTime < checkInTime) return "Invalid";

    const diffMs = checkOutTime.getTime() - checkInTime.getTime();
    const diffMinutes = Math.floor(diffMs / 60000);
    return formatMinutes(diffMinutes);
  }

  // No check-out time provided (show nothing for completed sessions without checkOut)
  return "-";
}

/**
 * Format minutes into human-readable duration
 * @param minutes - Duration in minutes
 * @returns Formatted duration string
 */
function formatMinutes(minutes: number): string {
  if (minutes < 0) return "-";
  if (minutes < 1) return "< 1min";
  if (minutes < 60) return `${minutes}min`;

  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (hours < 24) {
    return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}min` : `${hours}h`;
  }

  const days = Math.floor(hours / 24);
  const remainingHours = hours % 24;

  if (remainingHours === 0) return `${days}d`;
  return `${days}d ${remainingHours}h`;
}
