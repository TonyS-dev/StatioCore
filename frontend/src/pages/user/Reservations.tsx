import { useState, useEffect, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation, useNavigate } from 'react-router-dom';
import { userService } from '../../services/userService';
import { api } from '../../services/api';
import { SpotType, ReservationStatus } from '../../types';
import type { ParkingSpot, Reservation } from '../../types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Label } from '../../components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog';
import { Input } from '../../components/ui/input';
import { Badge } from '../../components/ui/badge';
import {
  Calendar,
  MapPin,
  DollarSign,
  Filter,
  Clock,
  AlertCircle
} from 'lucide-react';
import { useToast } from '../../components/ui/toast';
import { navigateWithAnimation } from '../../lib/navigateWithAnimation';

const Reservations = () => {
  const queryClient = useQueryClient();
  const location = useLocation();
  const navigate = useNavigate();
  const toast = useToast();
  const [buildingFilter, setBuildingFilter] = useState<string>('all');
  const [typeFilter, setTypeFilter] = useState<string>('all');
  const [selectedSpot, setSelectedSpot] = useState<ParkingSpot | null>(null);
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [reservationStartTime, setReservationStartTime] = useState('');
  const [reserveDialog, setReserveDialog] = useState(false);
  const [redirectCountdown, setRedirectCountdown] = useState<number>(5);
  const [autoRedirectCancelled, setAutoRedirectCancelled] = useState<boolean>(false);

  // Handle pre-selected spot from Available Spots page
  useEffect(() => {
    const preSelectedSpot = (location.state as { spot?: ParkingSpot })?.spot;
    if (preSelectedSpot) {
      // Use setTimeout to avoid synchronous state updates in effect
      setTimeout(() => {
        setSelectedSpot(preSelectedSpot);
        setReserveDialog(true);
      }, 0);
      // Clear the navigation state to prevent re-opening on refresh
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // Fetch buildings for filter
  const { data: buildings } = useQuery({
    queryKey: ['buildings'],
    queryFn: async () => {
      const response = await api.get('/buildings');
      return response.data;
    },
  });

  // Fetch available spots with filters
  const { data: spots, isLoading } = useQuery({
    queryKey: ['availableSpots', buildingFilter, typeFilter],
    queryFn: () =>
      userService.getAvailableSpots({
        buildingId: buildingFilter === 'all' ? undefined : buildingFilter,
        type: typeFilter === 'all' ? undefined : (typeFilter as SpotType),
      }),
    refetchInterval: 10000,
  });

  // Fetch user's reservations
  const { data: reservations, error: reservationsError } = useQuery({
    queryKey: ['myReservations'],
    queryFn: () => userService.getMyReservations(),
    refetchInterval: 15000,
  });

  // Show error toast if reservations fail to load
  useEffect(() => {
    if (reservationsError) {
      console.error('❌ Reservations error:', reservationsError);
      toast.push({
        message: `Failed to load reservations: ${(reservationsError as Error).message}`,
        variant: 'error'
      });
    }
  }, [reservationsError, toast]);

  // Debug: Log reservations data
  useEffect(() => {
    if (reservations) {
      console.log('📋 Reservations received:', reservations);
      console.log('📊 Total count:', reservations.length);
      if (reservations.length > 0) {
        console.log('📝 First reservation:', reservations[0]);
        console.log('📝 Status:', reservations[0].status);
      }
    }
  }, [reservations]);

  // Reserve mutation
  const reserveMutation = useMutation({
    mutationFn: (data: { spotId: string; startTime: string; vehicleNumber?: string }) =>
      userService.createReservation(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availableSpots'] });
      queryClient.invalidateQueries({ queryKey: ['myReservations'] });
      queryClient.invalidateQueries({ queryKey: ['userDashboard'] });
      setReserveDialog(false);
      setSelectedSpot(null);
      setVehicleNumber('');
      setReservationStartTime('');
      toast.push({ message: 'Reservation successful! Check your active reservations.', variant: 'success' });
      // Navigate to reservations tab if not already there
      if (window.location.pathname !== '/user/reservations') {
        setTimeout(() => navigate('/user/reservations', { replace: true }), 100);
      }
    },
    onError: (error: Error) => {
      toast.push({ message: `Reservation failed: ${error.message}`, variant: 'error' });
    },
  });

  // Cancel reservation mutation
  const cancelReservationMutation = useMutation({
    mutationFn: (reservationId: string) => userService.cancelReservation(reservationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myReservations'] });
      queryClient.invalidateQueries({ queryKey: ['availableSpots'] });
      queryClient.invalidateQueries({ queryKey: ['userDashboard'] });
      toast.push({ message: 'Reservation cancelled successfully.', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to cancel reservation: ${error.message}`, variant: 'error' });
    },
  });

  // Check-in mutation (for reservations within 1 hour)
  const checkInMutation = useMutation({
    mutationFn: (data: { spotId: string; vehicleNumber: string }) =>
      userService.checkIn(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myReservations'] });
      queryClient.invalidateQueries({ queryKey: ['userDashboard'] });
      queryClient.invalidateQueries({ queryKey: ['availableSpots'] });
      queryClient.invalidateQueries({ queryKey: ['mySessions'] });
      toast.push({ message: 'Checked in successfully.', variant: 'success' });
      // Optionally navigate to parking management or sessions view
      setTimeout(() => navigate('/user/parking-management', { replace: true }), 200);
    },
    onError: (error: Error) => {
      toast.push({ message: `Check-in failed: ${error.message}`, variant: 'error' });
    },
  });

  const handleSpotClick = (spot: ParkingSpot) => {
    setSelectedSpot(spot);
    setReserveDialog(true);
  };

  const handleReserve = () => {
    if (!selectedSpot || !reservationStartTime) {
      toast.push({ message: 'Please select a reservation start time', variant: 'warning' });
      return;
    }
    const isoDateTime = new Date(reservationStartTime).toISOString();
    reserveMutation.mutate({
      spotId: selectedSpot.id,
      startTime: isoDateTime,
      vehicleNumber: vehicleNumber || undefined,
    });
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

  // Determine active reservations - show PENDING and ACTIVE (exclude CANCELLED and COMPLETED)
  const activeReservations = useMemo(() => {
    return (reservations || []).filter((r: Reservation) =>
      r.status === ReservationStatus.PENDING || r.status === ReservationStatus.ACTIVE
    );
  }, [reservations]);

  // Auto-redirect to available spots when there are no active reservations, unless user cancels or dialog is open
  useEffect(() => {
    let timer: number | undefined;
    // Don't start countdown if dialog is open
    if ((activeReservations || []).length === 0 && !autoRedirectCancelled && !reserveDialog) {
      timer = window.setInterval(() => {
        setRedirectCountdown((c) => {
          if (c <= 1) {
            if (timer) window.clearInterval(timer);
            navigateWithAnimation(navigate, '/user/spots');
            return 0;
          }
          return c - 1;
        });
      }, 1000);
    }

    return () => {
      if (timer) window.clearInterval(timer);
    };
  }, [activeReservations, autoRedirectCancelled, reserveDialog, navigate]);

  // Reset countdown when dialog opens/closes
  useEffect(() => {
    if (reserveDialog) {
      // avoid synchronous setState in effect which can trigger cascading renders
      setTimeout(() => setRedirectCountdown(5), 0);
    }
  }, [reserveDialog]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Reservations</h1>
        <p className="text-gray-600 mt-2">Reserve a parking spot for a specific time period</p>
      </div>

      {/* My Active Reservations */}
      {activeReservations && activeReservations.length > 0 && (
        <Card className="border-teal-500 border-2">
          <CardHeader>
            <CardTitle className="flex items-center text-teal-600">
              <Calendar className="h-5 w-5 mr-2" />
              My Active Reservations
            </CardTitle>
            <CardDescription>Spots you have reserved</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {activeReservations.map((reservation: Reservation) => (
                <div
                  key={reservation.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                >
                  <div className="flex-1">
                    <div className="font-semibold text-lg">Spot {reservation.spotNumber}</div>
                    <div className="text-sm text-gray-600 flex items-center mt-1">
                      <MapPin className="h-3 w-3 mr-1" />
                      {reservation.buildingName} - Floor {reservation.floorNumber}
                    </div>
                    <div className="text-sm text-gray-600 flex items-center mt-1">
                      <Clock className="h-3 w-3 mr-1" />
                      Reserved for: {new Date(reservation.startTime).toLocaleString()}
                    </div>
                    {reservation.vehicleNumber && (
                      <div className="text-xs text-gray-500 mt-1">
                        Vehicle: {reservation.vehicleNumber}
                      </div>
                    )}
                    <div className="text-xs text-gray-400 mt-1">
                      Created: {new Date(reservation.createdAt).toLocaleString()}
                    </div>
                  </div>
                  {/* Actions: Check In (when within 1 hour before start) and Cancel */}
                  <div className="flex flex-col items-end space-y-2">
                    <div className="flex space-x-2">
                      {(() => {
                        const spotId = reservation.spotId;
                        const startMs = new Date(reservation.startTime).getTime();
                        const oneHourBefore = startMs - 60 * 60 * 1000;
                        const now = Date.now();
                        const canCheckIn = reservation.status === ReservationStatus.PENDING && now >= oneHourBefore;

                        if (canCheckIn) {
                          return (
                            <Button
                              size="sm"
                              onClick={() => {
                                if (!spotId) {
                                  toast.push({ message: 'Missing spot information for check-in.', variant: 'error' });
                                  return;
                                }
                                // Use reservation vehicle number if available; otherwise prompt the user
                                let vehicle = reservation.vehicleNumber;
                                if (!vehicle) {
                                  vehicle = window.prompt('Enter vehicle number to check in:') || '';
                                }
                                if (!vehicle) {
                                  toast.push({ message: 'Vehicle number is required to check in.', variant: 'warning' });
                                  return;
                                }
                                checkInMutation.mutate({ spotId, vehicleNumber: vehicle });
                              }}
                              disabled={checkInMutation.isPending}
                            >
                              Check In
                            </Button>
                          );
                        }
                        return null;
                      })()}

                      <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => cancelReservationMutation.mutate(reservation.id)}
                        disabled={cancelReservationMutation.isPending}
                      >
                        Cancel
                      </Button>
                    </div>
                    {/* Show reservation status */}
                    <div className="text-xs text-gray-500">{reservation.status}</div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* If there are no active reservations, show auto-redirect message (can cancel) or the spots UI when cancelled */}
      {(!activeReservations || activeReservations.length === 0) && (
        <>
          {!autoRedirectCancelled ? (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center text-gray-800">
                  <Calendar className="h-5 w-5 mr-2" />
                  No Active Reservations
                </CardTitle>
                <CardDescription>You don't have any active reservations. Redirecting to Available Spots shortly.</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-700">Redirecting to Available Spots in <span className="font-semibold">{redirectCountdown}</span> seconds.</div>
                  <div className="flex space-x-2">
                    <Button onClick={() => navigateWithAnimation(navigate, '/user/spots')}>Go now</Button>
                    <Button variant="outline" onClick={() => setAutoRedirectCancelled(true)}>Cancel</Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : (
            <>
              {/* Filters */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Filter className="h-5 w-5 mr-2" />
                    Find Spots to Reserve
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
                          <SelectItem value={SpotType.REGULAR}>Regular</SelectItem>
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
              {isLoading ? (
                <div className="flex items-center justify-center min-h-[400px]">
                  <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading available spots...</p>
                  </div>
                </div>
              ) : spots && spots.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                  {spots.map((spot) => (
                    <Card
                      key={spot.id}
                      className="hover:shadow-lg transition-shadow cursor-pointer"
                      onClick={() => handleSpotClick(spot)}
                    >
                      <CardHeader className="pb-3">
                        <div className="flex items-start justify-between">
                          <div>
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
                      <CardContent className="space-y-2">
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-gray-600">Hourly Rate:</span>
                          <span className="font-semibold text-green-600 flex items-center">
                            <DollarSign className="h-4 w-4" />
                            {spot.hourlyRate}/hr
                          </span>
                        </div>
                        <Button className="w-full mt-2" size="sm" onClick={() => handleSpotClick(spot)}>
                          <Calendar className="h-4 w-4 mr-2" />
                          Reserve
                        </Button>
                      </CardContent>
                    </Card>
                  ))}
                </div>
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
            </>
          )}
        </>
      )}

      {/* Reserve Dialog */}
      <Dialog open={reserveDialog} onOpenChange={setReserveDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reserve Parking Spot</DialogTitle>
            <DialogDescription>Reserve a parking spot for a specific time period</DialogDescription>
          </DialogHeader>

          {selectedSpot && (
            <div className="space-y-4">
              <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Spot Number:</span>
                  <span className="font-semibold">
                    {getSpotTypeIcon(selectedSpot.type)} {selectedSpot.spotNumber}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Location:</span>
                  <span className="font-semibold">
                    {selectedSpot.buildingName} - Floor {selectedSpot.floorNumber}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Type:</span>
                  <Badge className={getSpotTypeColor(selectedSpot.type)} variant="outline">
                    {selectedSpot.type}
                  </Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Hourly Rate:</span>
                  <span className="font-semibold text-green-600">${selectedSpot.hourlyRate}/hr</span>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="reservationStartTime">Reservation Start Time *</Label>
                <Input
                  id="reservationStartTime"
                  type="datetime-local"
                  value={reservationStartTime}
                  onChange={(e) => setReservationStartTime(e.target.value)}
                  disabled={reserveMutation.isPending}
                  min={new Date().toISOString().slice(0, 16)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="vehicleNumber">Vehicle Number (Optional)</Label>
                <Input
                  id="vehicleNumber"
                  placeholder="e.g., ABC-1234"
                  value={vehicleNumber}
                  onChange={(e) => setVehicleNumber(e.target.value)}
                  disabled={reserveMutation.isPending}
                />
              </div>
            </div>
          )}

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setReserveDialog(false);
                setSelectedSpot(null);
                setVehicleNumber('');
                setReservationStartTime('');
              }}
              disabled={reserveMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={handleReserve}
              disabled={reserveMutation.isPending || !reservationStartTime}
            >
              {reserveMutation.isPending ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Reserving...
                </>
              ) : (
                <>
                  <Calendar className="h-4 w-4 mr-2" />
                  Confirm Reservation
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Reservations;

