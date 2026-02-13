import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { userService } from '@/services/userService';
import { api } from '@/services/api';
import { SpotType } from '@/types';
import type { ParkingSpot } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import {
  Filter,
  MapPin,
  DollarSign,
  AlertCircle,
  Calendar,
  CheckCircle,
  LogIn
} from 'lucide-react';
import { useToast } from '@/components/ui/toast';
import { navigateWithAnimation } from '@/lib/navigateWithAnimation';

const AvailableSpots = () => {
  const navigate = useNavigate();
  const toast = useToast();
  const [buildingFilter, setBuildingFilter] = useState<string>('all');
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [selectedSpotForCheckIn, setSelectedSpotForCheckIn] = useState<ParkingSpot | null>(null);
  const [showReservationWarning, setShowReservationWarning] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 9; // Show 9 spots per page (3x3 grid)

  // Fetch buildings for filter
  const { data: buildings } = useQuery({
    queryKey: ['buildings'],
    queryFn: async () => {
      const response = await api.get('/buildings');
      return response.data;
    },
  });

  // Fetch available spots with filters
  const { data: spots, isLoading, error } = useQuery({
    queryKey: ['availableSpots', buildingFilter, typeFilter],
    queryFn: () =>
      userService.getAvailableSpots({
        buildingId: buildingFilter === 'all' ? undefined : buildingFilter,
        type: typeFilter === 'all' ? undefined : (typeFilter as SpotType),
      }),
    refetchInterval: 10000,
  });

  // Fetch active session to check if user is already parked
  const { data: activeSession } = useQuery({
    queryKey: ['activeSession'],
    queryFn: () => userService.getActiveSession(),
    refetchInterval: 15000,
  });

  // Fetch reservations to show which spots user has reserved
  const { data: reservations } = useQuery({
    queryKey: ['myReservations'],
    queryFn: () => userService.getMyReservations(),
    refetchInterval: 15000,
  });

  // Reset page to 1 when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [buildingFilter, typeFilter]);

  const handleCheckIn = (spot: ParkingSpot) => {
    // If user has an active session, show error and prevent check-in
    if (activeSession) {
      toast.push({
        message: 'User already has an active session. Please check out before checking in to another spot.',
        variant: 'error',
        duration: 6000
      });
      return;
    }

    // Check if user has ANY reservation (not just for this spot)
    const hasReservation = reservations && reservations.length > 0;

    if (hasReservation) {
      // Show confirmation dialog before proceeding
      setSelectedSpotForCheckIn(spot);
      setShowReservationWarning(true);
    } else {
      // No reservation, proceed directly to check-in
      proceedWithCheckIn(spot);
    }
  };

  const proceedWithCheckIn = (spot: ParkingSpot) => {
    // Get all active/pending reservation IDs to cancel them after check-in
    const reservationIds = (reservations || [])
      .filter((r: any) => r.status === 'PENDING' || r.status === 'ACTIVE')
      .map((r: any) => r.id);

    navigateWithAnimation(navigate, '/user/parking-management', {
      state: {
        action: 'checkin',
        spot,
        hasReservation: reservationIds.length > 0,
        reservationIds: reservationIds, // Pass ALL reservation IDs
      }
    });
  };

  const handleConfirmCheckInWithReservation = () => {
    if (selectedSpotForCheckIn) {
      // Close the warning dialog
      setShowReservationWarning(false);

      // Proceed to check-in (backend will handle nullifying the reservation)
      proceedWithCheckIn(selectedSpotForCheckIn);

      // Show info toast
      toast.push({
        message: '⚠️ Your existing reservation will be cancelled when you complete this check-in.',
        variant: 'warning',
        duration: 5000
      });

      setSelectedSpotForCheckIn(null);
    }
  };

  const handleReserve = (spot: ParkingSpot) => {
    navigateWithAnimation(navigate, '/user/reservations', { state: { spot } });
  };

  const getSpotTypeColor = (type: SpotType) => {
    switch (type) {
      case SpotType.VIP:
        return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case SpotType.HANDICAP:
        return 'bg-red-100 text-red-800 border-red-300';
      case SpotType.EV_CHARGING:
        return 'bg-green-100 text-green-800 border-green-300';
      default:
        return 'bg-teal-100 text-teal-800 border-teal-300';
    }
  };

  const getSpotTypeIcon = (type: SpotType) => {
    switch (type) {
      case SpotType.VIP:
        return '👑';
      case SpotType.HANDICAP:
        return '♿';
      case SpotType.EV_CHARGING:
        return '⚡';
      default:
        return '🚗';
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading available spots...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading spots</p>
        <p className="text-sm">{(error as Error).message}</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Available Parking Spots</h1>
        <p className="text-sm sm:text-base text-gray-600 mt-2">Find and select a spot to check-in or reserve</p>
      </div>

      {/* Active Session Alert */}
      {activeSession && (
        <Card className="border-teal-500 bg-teal-50">
          <CardContent className="py-4">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
              <div className="flex items-center space-x-3">
                <AlertCircle className="h-5 w-5 text-teal-600 flex-shrink-0" />
                <div>
                  <p className="font-semibold text-teal-900">You have an active parking session</p>
                  <p className="text-sm text-teal-700">
                    Spot {activeSession.spotNumber} at {activeSession.buildingName}
                  </p>
                </div>
              </div>
              <Button
                size="sm"
                onClick={() => navigateWithAnimation(navigate, '/user/parking-management', { state: { action: 'checkout' } })}
                className="w-full sm:w-auto"
              >
                Go to Checkout
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Filter className="h-5 w-5 mr-2" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label>Building</Label>
              <Select value={buildingFilter} onValueChange={setBuildingFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All Buildings" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Buildings</SelectItem>
                  {buildings?.map((building: { id: string; name: string }) => (
                    <SelectItem key={building.id} value={building.id}>
                      {building.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Spot Type</Label>
              <Select value={typeFilter} onValueChange={setTypeFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All Types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Types</SelectItem>
                  <SelectItem value={SpotType.STANDARD}>Standard</SelectItem>
                  <SelectItem value={SpotType.VIP}>VIP</SelectItem>
                  <SelectItem value={SpotType.HANDICAP}>Handicap</SelectItem>
                  <SelectItem value={SpotType.EV_CHARGING}>EV Charging</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-end">
              <Button
                variant="outline"
                onClick={() => {
                  setBuildingFilter('all');
                  setTypeFilter('all');
                }}
                className="w-full"
              >
                Clear Filters
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Results Count */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-600">
          Showing <span className="font-semibold">{spots?.length || 0}</span> available spots
        </p>
      </div>

      {/* Spots Grid */}
      {spots && spots.length > 0 ? (
        <>
          {/* Calculate pagination */}
          {(() => {
            const totalPages = Math.ceil(spots.length / itemsPerPage);
            const startIndex = (currentPage - 1) * itemsPerPage;
            const endIndex = startIndex + itemsPerPage;
            const paginatedSpots = spots.slice(startIndex, endIndex);

            return (
              <>
                {/* Spots Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {paginatedSpots.map((spot) => {
                    const hasReservation = reservations?.some(
                      (r: { spotId: string; status: string }) =>
                        r.spotId === spot.id && r.status === 'ACTIVE'
                    );

                    return (
                      <Card
                        key={spot.id}
                        className="hover:shadow-lg transition-shadow"
                      >
                        <CardHeader className="pb-3">
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <CardTitle className="text-lg">
                                {getSpotTypeIcon(spot.type)} Spot {spot.spotNumber}
                              </CardTitle>
                              <CardDescription className="mt-1">
                                <MapPin className="h-3 w-3 inline mr-1" />
                                {spot.buildingName} - Floor {spot.floorNumber}
                              </CardDescription>
                            </div>
                            <Badge className={getSpotTypeColor(spot.type)} variant="outline">
                              {spot.type}
                            </Badge>
                          </div>
                        </CardHeader>
                        <CardContent className="space-y-3">
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-gray-600">Hourly Rate:</span>
                            <span className="font-semibold text-green-600 flex items-center">
                              <DollarSign className="h-4 w-4" />
                              {spot.hourlyRate}/hr
                            </span>
                          </div>
                          <div className="flex items-center text-sm">
                            <CheckCircle className="h-4 w-4 text-green-600 mr-1" />
                            <span className="text-green-600 font-semibold">Available</span>
                          </div>

                          {hasReservation && (
                            <div className="bg-blue-50 border border-blue-200 rounded px-2 py-1">
                              <p className="text-xs text-blue-700 font-medium">
                                ✓ You have a reservation
                              </p>
                            </div>
                          )}

                          <div className="grid grid-cols-2 gap-2 pt-2">
                            <Button
                              size="sm"
                              variant={hasReservation ? "default" : "outline"}
                              onClick={() => handleCheckIn(spot)}
                              className="w-full"
                            >
                              <LogIn className="h-3 w-3 mr-1" />
                              Check-In
                            </Button>
                            <Button
                              size="sm"
                              variant={hasReservation ? "outline" : "default"}
                              onClick={() => handleReserve(spot)}
                              className="w-full"
                            >
                              <Calendar className="h-3 w-3 mr-1" />
                              Reserve
                            </Button>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>

                {/* Pagination Controls */}
                {totalPages > 1 && (
                  <Card>
                    <CardContent className="py-4">
                      <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
                        <div className="text-sm text-gray-600">
                          Page <span className="font-semibold">{currentPage}</span> of{' '}
                          <span className="font-semibold">{totalPages}</span> ({spots.length} total spots)
                        </div>
                        <div className="flex items-center space-x-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                            disabled={currentPage === 1}
                          >
                            ←
                          </Button>

                          {/* Page Numbers - show limited on mobile */}
                          <div className="flex items-center space-x-1">
                            {Array.from({ length: totalPages }, (_, i) => i + 1)
                              .filter((page) => {
                                // On small screens show fewer page buttons
                                if (totalPages <= 5) return true;
                                return page === 1 || page === totalPages || Math.abs(page - currentPage) <= 1;
                              })
                              .map((page, idx, arr) => (
                                <span key={page} className="flex items-center space-x-1">
                                  {idx > 0 && arr[idx - 1] !== page - 1 && <span className="text-gray-400">...</span>}
                                  <Button
                                    variant={page === currentPage ? 'default' : 'outline'}
                                    size="sm"
                                    onClick={() => setCurrentPage(page)}
                                    className="min-w-8 sm:min-w-10"
                                  >
                                    {page}
                                  </Button>
                                </span>
                              ))}
                          </div>

                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                            disabled={currentPage === totalPages}
                          >
                            →
                          </Button>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </>
            );
          })()}
        </>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <AlertCircle className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-semibold text-gray-900">No available spots</p>
            <p className="text-sm text-gray-600 mt-2">
              Try adjusting your filters or check back later
            </p>
          </CardContent>
        </Card>
      )}

      {/* Confirmation Dialog for Check-In with Reservation */}
      {showReservationWarning && (
        <Dialog open={showReservationWarning} onOpenChange={setShowReservationWarning}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Confirm Check-In</DialogTitle>
              <DialogDescription>
                You have an active reservation for this spot. Proceeding with check-in will cancel your reservation. Do you want to continue?
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setShowReservationWarning(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={handleConfirmCheckInWithReservation}
              >
                Confirm Check-In
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

    </div>
  );
};

export default AvailableSpots;

