import { useQuery } from '@tanstack/react-query';
import { adminService } from '@/services/adminService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  ParkingCircle,
  CheckCircle,
  DollarSign,
  Users,
  Shield,
  Activity,
  TrendingUp,
  Building as BuildingIcon,
} from 'lucide-react';
import type { BuildingStats } from '@/types';

const AdminDashboard = () => {
  const { data: dashboard, isLoading, error } = useQuery({
    queryKey: ['adminDashboard'],
    queryFn: () => adminService.getDashboard(),
    refetchInterval: 30000, // Refetch every 30 seconds
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading admin dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading dashboard</p>
        <p className="text-sm">{(error as any).message}</p>
      </div>
    );
  }

  const mainStats = [
    {
      title: 'Total Spots',
      value: dashboard?.totalSpots || 0,
      icon: ParkingCircle,
      color: 'text-teal-600',
      bgColor: 'bg-teal-50',
    },
    {
      title: 'Occupied',
      value: dashboard?.occupiedSpots || 0,
      icon: Activity,
      color: 'text-red-600',
      bgColor: 'bg-red-50',
    },
    {
      title: 'Available',
      value: dashboard?.availableSpots || 0,
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
    },
    {
      title: 'Total Revenue',
      value: `$${(dashboard?.totalRevenue || 0).toFixed(2)}`,
      icon: DollarSign,
      color: 'text-emerald-600',
      bgColor: 'bg-emerald-50',
    },
    {
      title: 'Total Users',
      value: dashboard?.totalUsers || 0,
      icon: Users,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
    },
    {
      title: 'Admins',
      value: dashboard?.totalAdmins || 0,
      icon: Shield,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
    },
    {
      title: 'Active Users',
      value: dashboard?.activeUsers || 0,
      icon: TrendingUp,
      color: 'text-cyan-600',
      bgColor: 'bg-cyan-50',
    },
    {
      title: 'Active Sessions',
      value: dashboard?.activeSessions || 0,
      icon: Activity,
      color: 'text-pink-600',
      bgColor: 'bg-pink-50',
    },
  ];

  const getOccupancyColor = (percentage: number) => {
    if (percentage < 50) return 'bg-green-500';
    if (percentage < 80) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const getOccupancyTextColor = (percentage: number) => {
    if (percentage < 50) return 'text-green-700';
    if (percentage < 80) return 'text-yellow-700';
    return 'text-red-700';
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-2">System overview and analytics</p>
      </div>

      {/* Main Stats Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-4 gap-3 sm:gap-6">
        {mainStats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <Card key={index}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-gray-600">{stat.title}</CardTitle>
                <div className={`${stat.bgColor} p-2 rounded-lg`}>
                  <Icon className={`h-4 w-4 ${stat.color}`} />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stat.value}</div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Overall Occupancy */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <TrendingUp className="h-5 w-5 mr-2" />
            Overall Occupancy Rate
          </CardTitle>
          <CardDescription>System-wide parking utilization</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-600">Occupied Spots</span>
              <span className="text-sm font-semibold">
                {dashboard?.occupiedSpots || 0} / {dashboard?.totalSpots || 0}
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-6 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 flex items-center justify-center text-white text-xs font-semibold ${getOccupancyColor(
                  dashboard?.totalSpots
                    ? ((dashboard.occupiedSpots || 0) / dashboard.totalSpots) * 100
                    : 0
                )}`}
                style={{
                  width: `${
                    dashboard?.totalSpots
                      ? ((dashboard.occupiedSpots || 0) / dashboard.totalSpots) * 100
                      : 0
                  }%`,
                  minWidth: '40px',
                }}
              >
                {dashboard?.totalSpots
                  ? (((dashboard.occupiedSpots || 0) / dashboard.totalSpots) * 100).toFixed(1)
                  : 0}
                %
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Building Statistics */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <BuildingIcon className="h-5 w-5 mr-2" />
            Building Statistics
          </CardTitle>
          <CardDescription>Occupancy breakdown by building</CardDescription>
        </CardHeader>
        <CardContent>
          {dashboard?.buildingStats && dashboard.buildingStats.length > 0 ? (
            <div className="space-y-4">
              {dashboard.buildingStats.map((building: BuildingStats) => (
                <div key={building.buildingId} className="border rounded-lg p-4 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-semibold text-gray-900">{building.buildingName}</h3>
                      <p className="text-xs text-gray-500">
                        {building.totalFloors} floors • {building.totalSpots} spots
                      </p>
                    </div>
                    <div className="text-right">
                      <p className={`text-2xl font-bold ${getOccupancyTextColor(building.occupancyPercentage)}`}>
                        {building.occupancyPercentage.toFixed(1)}%
                      </p>
                      <p className="text-xs text-gray-500">occupancy</p>
                    </div>
                  </div>

                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div>
                      <p className="text-xs text-gray-500">Total</p>
                      <p className="text-lg font-semibold">{building.totalSpots}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Occupied</p>
                      <p className="text-lg font-semibold text-red-600">{building.occupiedSpots}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Available</p>
                      <p className="text-lg font-semibold text-green-600">
                        {building.availableSpots}
                      </p>
                    </div>
                  </div>

                  <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${getOccupancyColor(
                        building.occupancyPercentage
                      )}`}
                      style={{ width: `${building.occupancyPercentage}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-4">No building data available</p>
          )}
        </CardContent>
      </Card>

      {/* Recent Activity */}
      {dashboard?.recentActivity && dashboard.recentActivity.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Latest system activities</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
                {dashboard.recentActivity.slice(0, 10).map((activity) => (
                <div className="flex items-start space-x-3 sm:space-x-4 pb-4 border-b last:border-b-0">
                  <div className="bg-teal-50 p-2 rounded-lg">
                    <Activity className="h-4 w-4 text-teal-600" />
                  </div>
                  <div className="flex-1 min-w-0 space-y-1">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-0.5">
                      <p className="text-sm font-medium truncate">{activity.action}</p>
                      <p className="text-xs text-gray-400 flex-shrink-0">
                        {new Date(activity.createdAt).toLocaleString()}
                      </p>
                    </div>
                    <p className="text-xs text-gray-600 truncate">{activity.userEmail}</p>
                    <p className="text-xs text-gray-500 truncate">{activity.details}</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Quick Stats Summary */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Total Reservations</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{dashboard?.totalReservations || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Total Payments</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{dashboard?.totalPayments || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Average Revenue per Session</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">
              $
              {dashboard?.totalPayments && dashboard.totalPayments > 0
                ? ((dashboard.totalRevenue || 0) / dashboard.totalPayments).toFixed(2)
                : '0.00'}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboard;

