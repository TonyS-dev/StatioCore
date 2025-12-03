import { create } from 'zustand';
import type { Building, Spot, ParkingSession, Reservation } from '@/types';
import { userService } from '@/services/userService';

interface ParkingStore {
  buildings: Building[];
  spots: Spot[];
  activeSessions: ParkingSession[];
  reservations: Reservation[];
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchBuildings: () => Promise<void>;
  fetchSpots: () => Promise<void>;
  fetchAvailableSpots: (params?: { buildingId?: string; floorId?: string; type?: string }) => Promise<void>;
  fetchActiveSessions: () => Promise<void>;
  fetchReservations: () => Promise<void>;
  checkIn: (spotId: string) => Promise<ParkingSession>;
  checkOut: (data: { sessionId: string; paymentMethod: string }) => Promise<unknown>;
  clearError: () => void;
}

export const useParkingStore = create<ParkingStore>((set) => ({
  buildings: [],
  spots: [],
  activeSessions: [],
  reservations: [],
  isLoading: false,
  error: null,

  fetchBuildings: async () => {
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getBuildings();
      set({ buildings: data, isLoading: false });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to fetch buildings';
      set({ error: message, isLoading: false });
    }
  },

  fetchSpots: async () => {
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getAvailableSpots();
      set({ spots: data, isLoading: false });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to fetch spots';
      set({ error: message, isLoading: false });
    }
  },

  fetchAvailableSpots: async (params) => {
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getAvailableSpots(params);
      set({ spots: data, isLoading: false });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to fetch spots';
      set({ error: message, isLoading: false });
    }
  },

  fetchActiveSessions: async () => {
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getActiveSessions();
      set({ activeSessions: data, isLoading: false });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to fetch active sessions';
      set({ error: message, isLoading: false });
    }
  },

  fetchReservations: async () => {
    set({ isLoading: true, error: null });
    try {
      const data = await userService.getReservations();
      set({ reservations: data, isLoading: false });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Failed to fetch reservations';
      set({ error: message, isLoading: false });
    }
  },

  checkIn: async (spotId: string) => {
    set({ isLoading: true, error: null });
    try {
      const session = await userService.checkIn({ spotId });
      set({ isLoading: false });
      return session;
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Check-in failed';
      set({ error: message, isLoading: false });
      throw error;
    }
  },

  checkOut: async (data: { sessionId: string; paymentMethod: string }) => {
    set({ isLoading: true, error: null });
    try {
      const result = await userService.checkOut(data);
      set({ isLoading: false });
      return result;
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Check-out failed';
      set({ error: message, isLoading: false });
      throw error;
    }
  },

  clearError: () => {
    set({ error: null });
  },
}));
