import { api } from './api';
import type {
  AdminDashboardResponse,
  Building,
  User,
  ActivityLog,
  PageRequest,
  PageResponse,
  UserFilters,
  LogFilters,
  RegisterRequest,
} from '../types';

export const adminService = {
  // Dashboard
  async getDashboard(): Promise<AdminDashboardResponse> {
    const response = await api.get<AdminDashboardResponse>('/admin/dashboard');
    return response.data;
  },

  // Buildings
  async getAllBuildings(): Promise<Building[]> {
    const response = await api.get<Building[]>('/admin/buildings');
    return response.data;
  },

  async getBuildings(pagination?: PageRequest): Promise<PageResponse<Building>> {
    const params = new URLSearchParams();
    if (pagination?.page !== undefined) params.append('page', pagination.page.toString());
    if (pagination?.size !== undefined) params.append('size', pagination.size.toString());

    const response = await api.get<PageResponse<Building>>(`/admin/buildings/paginated?${params.toString()}`);
    return response.data;
  },

  async getBuildingById(id: string): Promise<Building> {
    const response = await api.get<Building>(`/admin/buildings/${id}`);
    return response.data;
  },

  async createBuilding(data: { name: string; address: string }): Promise<Building> {
    const response = await api.post<Building>('/admin/buildings', data);
    return response.data;
  },

  async updateBuilding(id: string, data: { name: string; address: string }): Promise<Building> {
    const response = await api.put<Building>(`/admin/buildings/${id}`, data);
    return response.data;
  },

  async deleteBuilding(id: string): Promise<void> {
    await api.delete(`/admin/buildings/${id}`);
  },

  // Floors
  async getAllFloors(): Promise<any[]> {
    const response = await api.get<any[]>('/admin/floors');
    return response.data;
  },

  async getFloors(pagination?: PageRequest): Promise<PageResponse<any>> {
    const params = new URLSearchParams();
    if (pagination?.page !== undefined) params.append('page', pagination.page.toString());
    if (pagination?.size !== undefined) params.append('size', pagination.size.toString());

    const response = await api.get<PageResponse<any>>(`/admin/floors/paginated?${params.toString()}`);
    return response.data;
  },

  async createFloor(data: { floorNumber: number; buildingId: string }): Promise<any> {
    const response = await api.post<any>('/admin/floors', data);
    return response.data;
  },

  async updateFloor(id: string, data: { floorNumber: number; buildingId: string }): Promise<any> {
    const response = await api.put<any>(`/admin/floors/${id}`, data);
    return response.data;
  },

  async deleteFloor(id: string): Promise<void> {
    await api.delete(`/admin/floors/${id}`);
  },

  // Spots
  async getAllSpots(): Promise<any[]> {
    const response = await api.get<any[]>('/admin/spots');
    return response.data;
  },

  async getSpots(pagination?: PageRequest): Promise<PageResponse<any>> {
    const params = new URLSearchParams();
    if (pagination?.page !== undefined) params.append('page', pagination.page.toString());
    if (pagination?.size !== undefined) params.append('size', pagination.size.toString());

    const response = await api.get<PageResponse<any>>(`/admin/spots/paginated?${params.toString()}`);
    return response.data;
  },

  async createSpot(data: { spotNumber: string; type: string; status: string; floorId: string }): Promise<any> {
    const response = await api.post<any>('/admin/spots', data);
    return response.data;
  },

  async updateSpot(id: string, data: { spotNumber: string; type: string; status: string; floorId: string }): Promise<any> {
    const response = await api.put<any>(`/admin/spots/${id}`, data);
    return response.data;
  },

  async deleteSpot(id: string): Promise<void> {
    await api.delete(`/admin/spots/${id}`);
  },

  // Users
  async getUsers(filters?: UserFilters, pagination?: PageRequest): Promise<PageResponse<User>> {
    const params = new URLSearchParams();
    if (pagination?.page !== undefined) params.append('page', pagination.page.toString());
    if (pagination?.size !== undefined) params.append('size', pagination.size.toString());
    if (filters?.role) params.append('role', filters.role);
    if (filters?.active !== undefined) params.append('active', filters.active.toString());
    if (filters?.searchTerm) params.append('search', filters.searchTerm);

    const response = await api.get<PageResponse<User>>(`/admin/users?${params.toString()}`);
    return response.data;
  },

  async getUserById(id: string): Promise<User> {
    const response = await api.get<User>(`/admin/users/${id}`);
    return response.data;
  },

  async createUser(data: RegisterRequest & { role?: string }): Promise<User> {
    const response = await api.post<User>('/admin/users', data);
    return response.data;
  },

  async createAdmin(data: RegisterRequest): Promise<User> {
    const response = await api.post<User>('/admin/users/admin', data);
    return response.data;
  },

  async updateUser(userId: string, data: Partial<User>): Promise<User> {
    const response = await api.put<User>(`/admin/users/${userId}`, data);
    return response.data;
  },

  async updateUserStatus(userId: string, isActive: boolean): Promise<User> {
    const response = await api.patch<User>(`/admin/users/${userId}/status`, { isActive });
    return response.data;
  },

  async deleteUser(userId: string): Promise<void> {
    await api.delete(`/admin/users/${userId}`);
  },

  // Activity Logs
  async getLogs(filters?: LogFilters, pagination?: PageRequest): Promise<PageResponse<ActivityLog>> {
    const params = new URLSearchParams();
    if (pagination?.page !== undefined) params.append('page', pagination.page.toString());
    if (pagination?.size !== undefined) params.append('size', pagination.size.toString());
    if (filters?.userId) params.append('userId', filters.userId);
    if (filters?.action) params.append('action', filters.action);
    if (filters?.startDate) params.append('startDate', filters.startDate);
    if (filters?.endDate) params.append('endDate', filters.endDate);

    const response = await api.get<PageResponse<ActivityLog>>(`/admin/logs?${params.toString()}`);
    return response.data;
  },

  // Spots Management
  async updateSpotStatus(spotId: string, status: string): Promise<void> {
    await api.patch(`/admin/spots/${spotId}/status`, { status });
  },
};

