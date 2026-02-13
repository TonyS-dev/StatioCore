import { useQuery } from '@tanstack/react-query';
import { adminService } from '../../services/adminService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Badge } from '../../components/ui/badge';
import { Building, MapPin, Layers } from 'lucide-react';

const BuildingManagement = () => {
  const { data: buildings, isLoading, error } = useQuery({
    queryKey: ['adminBuildings'],
    queryFn: () => adminService.getAllBuildings(),
    refetchInterval: 30000,
  });

  const getOccupancyColor = (occupied: number, total: number) => {
    const percentage = (occupied / total) * 100;
    if (percentage < 50) return 'bg-green-500';
    if (percentage < 80) return 'bg-yellow-500';
    return 'bg-red-500';
  };

  const getOccupancyBadge = (occupied: number, total: number) => {
    const percentage = (occupied / total) * 100;
    if (percentage < 50) return 'bg-green-100 text-green-800';
    if (percentage < 80) return 'bg-yellow-100 text-yellow-800';
    return 'bg-red-100 text-red-800';
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading buildings...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading buildings</p>
        <p className="text-sm">{(error as any).message}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Building Management</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-2">Manage parking buildings and their facilities</p>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-6">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Buildings</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{buildings?.length || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Floors</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {buildings?.reduce((sum, b) => sum + (b.totalFloors || 0), 0) || 0}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Spots</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {buildings?.reduce((sum, b) => sum + (b.totalSpots || 0), 0) || 0}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Occupied Spots</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-red-600">
              {buildings?.reduce((sum, b) => sum + (b.occupiedSpots || 0), 0) || 0}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Buildings Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
        {buildings?.map((building) => {
          const occupancyPercentage = building.totalSpots
            ? ((building.occupiedSpots || 0) / building.totalSpots) * 100
            : 0;

          return (
            <Card key={building.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3">
                    <div className="bg-teal-50 p-3 rounded-lg">
                      <Building className="h-6 w-6 text-teal-600" />
                    </div>
                    <div>
                      <CardTitle className="text-lg">{building.name}</CardTitle>
                      <CardDescription className="flex items-center mt-1">
                        <MapPin className="h-3 w-3 mr-1" />
                        {building.address}
                      </CardDescription>
                    </div>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="space-y-4">
                {/* Building Stats */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 p-3 rounded-lg">
                    <div className="flex items-center space-x-2 mb-1">
                      <Layers className="h-4 w-4 text-gray-600" />
                      <p className="text-xs text-gray-600">Floors</p>
                    </div>
                    <p className="text-xl font-bold">{building.totalFloors}</p>
                  </div>

                  <div className="bg-gray-50 p-3 rounded-lg">
                    <p className="text-xs text-gray-600 mb-1">Total Spots</p>
                    <p className="text-xl font-bold">{building.totalSpots}</p>
                  </div>
                </div>

                {/* Occupancy Stats */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">Occupancy</span>
                    <Badge className={getOccupancyBadge(building.occupiedSpots || 0, building.totalSpots)}>
                      {occupancyPercentage.toFixed(1)}%
                    </Badge>
                  </div>

                  <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${getOccupancyColor(
                        building.occupiedSpots || 0,
                        building.totalSpots
                      )}`}
                      style={{ width: `${occupancyPercentage}%` }}
                    />
                  </div>

                  <div className="flex items-center justify-between text-xs text-gray-500">
                    <span>
                      Occupied: <span className="font-semibold text-red-600">{building.occupiedSpots || 0}</span>
                    </span>
                    <span>
                      Available: <span className="font-semibold text-green-600">{building.availableSpots || 0}</span>
                    </span>
                  </div>
                </div>

                {/* Metadata */}
                <div className="pt-4 border-t text-xs text-gray-500">
                  <p>Created: {new Date(building.createdAt).toLocaleDateString()}</p>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Empty State */}
      {buildings && buildings.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Building className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-semibold text-gray-900">No buildings found</p>
            <p className="text-sm text-gray-600 mt-2">Buildings will appear here once they are added to the system</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default BuildingManagement;

