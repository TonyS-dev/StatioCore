// ============= ENUMS =============
export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export enum SpotType {
  REGULAR = 'REGULAR',
  VIP = 'VIP',
  HANDICAP = 'HANDICAP',
  EV_CHARGING = 'EV_CHARGING'
}

export enum SpotStatus {
  AVAILABLE = 'AVAILABLE',
  OCCUPIED = 'OCCUPIED',
  RESERVED = 'RESERVED',
  MAINTENANCE = 'MAINTENANCE'
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum PaymentMethod {
  CASH = 'CASH',
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  UPI = 'UPI'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

// ============= USER & AUTH =============
export interface User {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// ============= BUILDING & FLOOR =============
export interface Building {
  id: string;
  name: string;
  address: string;
  totalFloors: number;
  totalSpots: number;
  occupiedSpots?: number;
  availableSpots?: number;
  createdAt: string;
}

export interface Floor {
  id: string;
  floorNumber: number;
  buildingId: string;
  buildingName: string;
  totalSpots: number;
  occupiedSpots?: number;
  availableSpots?: number;
}

// ============= PARKING SPOT =============
export interface ParkingSpot {
  id: string;
  spotNumber: string;
  type: SpotType;
  status: SpotStatus;
  floorId: string;
  floorNumber: number;
  buildingId: string;
  buildingName: string;
  buildingAddress: string;
  hourlyRate: number;
}

// ============= RESERVATION =============
export interface ReservationRequest {
  spotId: string;
  startTime: string; // ISO string (OffsetDateTime)
  vehicleNumber?: string;
  durationMinutes?: number;
}

export interface Reservation {
  id: string;
  userId: string;
  spotId: string;
  spotNumber: string;
  buildingName: string;
  floorNumber: number;
  vehicleNumber?: string;
  startTime: string;
  endTime: string;
  status: ReservationStatus;
  createdAt: string;
}

// ============= PARKING SESSION =============
export interface CheckInRequest {
  spotId: string;
  vehicleNumber: string;
}

export interface ParkingSession {
  id: string;
  userId: string;
  spotId: string;
  spotNumber: string;
  buildingName: string;
  floorNumber: number;
  vehicleNumber?: string;
  checkInTime: string;
  checkOutTime?: string;
  duration?: number; // in minutes
  fee?: number;
  transactionId?: string;
  paymentMethod?: string;
  status: string;
  createdAt: string;
  updatedAt?: string;
  spot?: any; // Optional nested spot data
}

// ============= PAYMENT =============
export interface FeeCalculationResponse {
  sessionId: string;
  spotNumber: string;
  checkInTime: string;
  calculatedCheckOutTime: string;
  durationMinutes: number;
  hourlyRate: number;
  amountDue: number;
  spotType: string;
  message: string;
}

export interface CheckOutRequest {
  sessionId: string;
  paymentMethod: PaymentMethod;
}

export interface Bill {
  sessionId: string;
  spotId: string;
  spotNumber: string;
  checkInTime: string;
  checkOutTime: string;
  durationMinutes: number;
  amountDue: number;
  paymentId: string;
  paymentStatus: string;
  transactionId: string;
  paymentMethod: string;
  paidAt: string;
  message: string;
  createdAt?: string;
}

export interface PaymentSimulationRequest {
  amount: number;
  paymentMethod: PaymentMethod;
  description?: string;
}

export interface PaymentSimulationResponse {
  transactionId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  message: string;
  timestamp: string;
}

// ============= DASHBOARD =============
export interface DashboardResponse {
  // Overall Stats
  totalSpots: number;
  occupiedSpots: number;
  availableSpots: number;
  occupancyPercentage: number;
  
  // User Stats
  activeReservations: number;
  activeSessions: number;
  totalReservations: number;
  totalCompletedSessions: number;
  
  // Financial Stats
  totalEarnings: number;
  outstandingFees: number;
  averageSessionFee: number;
  
  // Recent Activity
  recentActivity: ActivityRecord[];
}

export interface ActivityRecord {
  action: string;
  details: string;
  timestamp: string;
}

export interface AdminDashboardResponse {
  totalSpots: number;
  occupiedSpots: number;
  availableSpots: number;
  totalRevenue: number;
  totalUsers: number;
  totalAdmins: number;
  activeUsers: number;
  activeSessions: number;
  totalReservations: number;
  totalPayments: number;
  recentActivity: ActivityLog[];
  buildingStats: BuildingStats[];
}

export interface BuildingStats {
  buildingId: string;
  buildingName: string;
  totalSpots: number;
  occupiedSpots: number;
  availableSpots: number;
  occupancyPercentage: number;
  totalFloors: number;
}

// ============= ACTIVITY LOG =============
export interface ActivityLog {
  id: string;
  userId: string;
  userEmail: string;
  action: string;
  details: string;
  createdAt: string;
  ipAddress?: string;
}

// ============= PAGINATION =============
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ============= FILTERS =============
export interface SpotFilters {
  buildingId?: string;
  floorId?: string;
  type?: SpotType;
  status?: SpotStatus;
}

export interface UserFilters {
  role?: Role;
  active?: boolean;
  searchTerm?: string;
}

export interface LogFilters {
  userId?: string;
  action?: string;
  startDate?: string;
  endDate?: string;
}

// ============= API ERROR =============
export interface ApiError {
  message: string;
  status?: number;
  timestamp?: string;
  path?: string;
}
