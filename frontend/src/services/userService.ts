import { api } from './api';
import type {
  ParkingSpot,
  SpotFilters,
  CheckInRequest,
  ParkingSession,
  FeeCalculationResponse,
  CheckOutRequest,
  Bill,
  ReservationRequest,
  Reservation,
  DashboardResponse,
} from '../types';

export const userService = {
  // Dashboard
  async getDashboard(): Promise<DashboardResponse> {
    const response = await api.get<DashboardResponse>('/user/dashboard');
    return response.data;
  },

  // Parking Spots
  async getAvailableSpots(filters?: SpotFilters): Promise<ParkingSpot[]> {
    const params = new URLSearchParams();
    if (filters?.buildingId) params.append('buildingId', filters.buildingId);
    if (filters?.floorId) params.append('floorId', filters.floorId);
    if (filters?.type) params.append('type', filters.type);

    const response = await api.get<ParkingSpot[]>(`/spots/available?${params.toString()}`);
    return response.data;
  },

  // Reservations
  async createReservation(data: ReservationRequest): Promise<Reservation> {
    const response = await api.post<Reservation>('/reservations', data);
    return response.data;
  },

  async getMyReservations(): Promise<Reservation[]> {
    const response = await api.get<Reservation[]>('/reservations');
    return response.data;
  },

  async cancelReservation(reservationId: string): Promise<void> {
    await api.delete(`/reservations/${reservationId}`);
  },

  // Parking Sessions
  async checkIn(data: CheckInRequest): Promise<ParkingSession> {
    const response = await api.post<ParkingSession>('/parking/check-in', data);
    return response.data;
  },

  async getActiveSession(): Promise<ParkingSession | null> {
    try {
      const response = await api.get<ParkingSession[]>('/parking/sessions/active');
      // Backend returns array; we take the first active session or null
      return response.data && response.data.length > 0 ? response.data[0] : null;
    } catch (error: any) {
      if (error.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async getSessionById(sessionId: string): Promise<ParkingSession> {
    const response = await api.get<ParkingSession>(`/parking/sessions/${sessionId}`);
    return response.data;
  },

  async getMySessions(): Promise<ParkingSession[]> {
    const response = await api.get<ParkingSession[]>('/parking/sessions/my');
    return response.data;
  },

  // Checkout & Payment
  async calculateFee(sessionId: string): Promise<FeeCalculationResponse> {
    const response = await api.post<FeeCalculationResponse>(`/parking/calculate-fee?sessionId=${sessionId}`);
    return response.data;
  },

  async checkOut(sessionId: string, data: CheckOutRequest): Promise<Bill> {
    const response = await api.post<Bill>(`/parking/check-out?sessionId=${sessionId}&paymentMethod=${data.paymentMethod}`);
    return response.data;
  },

  async getMyBills(): Promise<Bill[]> {
    const response = await api.get<Bill[]>('/bills/my');
    return response.data;
  },
};

