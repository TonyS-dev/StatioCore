import { useQuery } from '@tanstack/react-query';
import { userService } from '../../services/userService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import {
  ParkingCircle,
  CheckCircle,
  Clock,
  Calendar
} from 'lucide-react';

const UserDashboard = () => {
  const { data: dashboard, isLoading, error } = useQuery({
    queryKey: ['userDashboard'],
    queryFn: () => userService.getDashboard(),
    refetchInterval: 5000, // Refetch every 5 seconds for real-time updates
    refetchOnWindowFocus: true, // Refetch when user returns to tab
    staleTime: 0, // Always consider data stale for immediate updates
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading dashboard</p>
        <p className="text-sm">{(error as Error).message}</p>
      </div>
    );
  }

  const stats = [
    {
      title: 'Available Spots',
      value: dashboard?.availableSpots || 0,
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
    },
    {
      title: 'Active Sessions',
      value: dashboard?.activeSessions || 0,
      icon: Clock,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
    },
    {
      title: 'My Reservations',
      value: dashboard?.activeReservations || 0,
      icon: Calendar,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-2">Overview of parking system status and your activities</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <Card key={index}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium text-gray-600">
                  {stat.title}
                </CardTitle>
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

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Get started with parking</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <a
              href="/user/spots"
              className="flex items-center space-x-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <ParkingCircle className="h-8 w-8 text-teal-600" />
              <div>
                <p className="font-semibold">View Available Spots</p>
                <p className="text-sm text-gray-600">Browse available spots</p>
              </div>
            </a>
            <a
              href="/user/reservations"
              className="flex items-center space-x-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Calendar className="h-8 w-8 text-purple-600" />
              <div>
                <p className="font-semibold">Reserve Spot</p>
                <p className="text-sm text-gray-600">Book for later</p>
              </div>
            </a>
            <a
              href="/user/parking-management"
              className="flex items-center space-x-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Clock className="h-8 w-8 text-green-600" />
              <div>
                <p className="font-semibold">Check-In/Out</p>
                <p className="text-sm text-gray-600">Manage parking session</p>
              </div>
            </a>
            <a
              href="/user/parking-management"
              className="flex items-center space-x-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
            >
              <CheckCircle className="h-8 w-8 text-orange-600" />
              <div>
                <p className="font-semibold">Payment</p>
                <p className="text-sm text-gray-600">View payment history</p>
              </div>
            </a>
          </div>
        </CardContent>
      </Card>

      {/* Recent Activity */}
      {dashboard?.recentActivity && dashboard.recentActivity.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Your latest parking activities</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {dashboard.recentActivity.slice(0, 5).map((activity, index) => (
                <div
                  key={index}
                  className="flex items-start space-x-4 pb-4 border-b last:border-b-0"
                >
                  <div className="bg-teal-50 p-2 rounded-lg">
                    <Clock className="h-4 w-4 text-teal-600" />
                  </div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium">{activity.action}</p>
                    <p className="text-xs text-gray-500">{activity.details}</p>
                    <p className="text-xs text-gray-400">
                      {new Date(activity.timestamp).toLocaleString()}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

    </div>
  );
};

export default UserDashboard;
