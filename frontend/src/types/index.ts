// ==================== Enums ====================
export const UserRole = {
  ADMIN: "ADMIN" as const,
  USER: "USER" as const,
} as const;
export type UserRole = typeof UserRole[keyof typeof UserRole];

export const SpotStatus = {
  AVAILABLE: "AVAILABLE" as const,
  OCCUPIED: "OCCUPIED" as const,
  RESERVED: "RESERVED" as const,
  MAINTENANCE: "MAINTENANCE" as const,
} as const;
export type SpotStatus = typeof SpotStatus[keyof typeof SpotStatus];

export const SpotType = {
  REGULAR: "REGULAR" as const,
  VIP: "VIP" as const,
  HANDICAP: "HANDICAP" as const,
  EV_CHARGING: "EV_CHARGING" as const,
} as const;
export type SpotType = typeof SpotType[keyof typeof SpotType];

export const PaymentMethod = {
  CREDIT_CARD: "CREDIT_CARD" as const,
  DEBIT_CARD: "DEBIT_CARD" as const,
  DIGITAL_WALLET: "DIGITAL_WALLET" as const,
} as const;
export type PaymentMethod = typeof PaymentMethod[keyof typeof PaymentMethod];

export const ActivityAction = {
  USER_LOGIN: "USER_LOGIN" as const,
  USER_LOGOUT: "USER_LOGOUT" as const,
  RESERVATION_CREATED: "RESERVATION_CREATED" as const,
  RESERVATION_CANCELLED: "RESERVATION_CANCELLED" as const,
  SESSION_STARTED: "SESSION_STARTED" as const,
  CHECK_OUT: "CHECK_OUT" as const,
  PAYMENT_PROCESSED: "PAYMENT_PROCESSED" as const,
  SPOT_STATUS_UPDATED: "SPOT_STATUS_UPDATED" as const,
  USER_CREATED: "USER_CREATED" as const,
  USER_DELETED: "USER_DELETED" as const,
} as const;
export type ActivityAction = typeof ActivityAction[keyof typeof ActivityAction];

// ==================== Models ====================
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Building {
  id: string;
  name: string;
  address: string;
  floors: number;
  totalSpots: number;
  occupiedSpots: number;
  availableSpots: number;
  createdAt: string;
  updatedAt: string;
}

export interface Spot {
  id: string;
  buildingId: string;
  buildingName?: string;
  floorNumber: number;
  spotNumber: string;
  type: SpotType;
  status: SpotStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Reservation {
  id: string;
  userId: string;
  spotId: string;
  spot?: Spot;
  startTime: string;
  endTime: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ParkingSession {
  id: string;
  userId: string;
  spotId: string;
  spot?: Spot;
  checkInTime: string;
  checkOutTime?: string;
  duration?: number; // in minutes
  fee?: number;
  transactionId?: string;
  paymentMethod?: PaymentMethod;
  createdAt: string;
  updatedAt: string;
}

export interface Payment {
  id: string;
  sessionId: string;
  amount: number;
  method: PaymentMethod;
  transactionId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface ActivityLog {
  id: string;
  userId: string;
  action: ActivityAction;
  details: string;
  status: string;
  createdAt: string;
}

export interface DashboardMetrics {
  totalSpots: number;
  occupiedSpots: number;
  availableSpots: number;
  totalRevenue: number;
  activeUsers: number;
  adminCount: number;
  averageFee: number;
  occupancyPercentage: number;
}

export interface BuildingMetrics {
  buildingId: string;
  buildingName: string;
  totalFloors: number;
  totalSpots: number;
  occupiedCount: number;
  occupancyPercentage: number;
}

// ==================== API Request/Response Types ====================
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface ReservationRequest {
  spotId: string;
  startTime: string;
  endTime: string;
}

export interface CheckInRequest {
  spotId: string;
}

export interface CheckOutRequest {
  sessionId: string;
  paymentMethod: PaymentMethod;
}

export interface CreateAdminRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface UpdateUserStatusRequest {
  active: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errors?: Record<string, string>;
}
