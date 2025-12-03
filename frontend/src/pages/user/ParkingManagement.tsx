import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useLocation, useNavigate } from 'react-router-dom';
import { userService } from '../../services/userService';
import { PaymentMethod, SpotType } from '../../types';
import type { ParkingSession, ParkingSpot, CheckInRequest } from '../../types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Label } from '../../components/ui/label';
import { Badge } from '../../components/ui/badge';
import { Input } from '../../components/ui/input';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import { navigateWithAnimation } from '../../lib/navigateWithAnimation';
import { formatDuration, formatDurationFromTimestamps } from '../../lib/formatDuration';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import { Clock, MapPin, CreditCard, Receipt, AlertCircle, LogIn } from 'lucide-react';
import { useToast } from '../../components/ui/toast';
import { useAuthValidation } from '../../hooks/useAuth';

// Check-in Section Component
interface CheckInSectionProps {
  preSelectedSpot?: ParkingSpot | null;
  hasReservation?: boolean;
}

const CheckInSection = ({ preSelectedSpot, hasReservation }: CheckInSectionProps) => {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [selectedSpot, setSelectedSpot] = useState<ParkingSpot | null>(null);
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [checkInDialog, setCheckInDialog] = useState(false);

  // Handle pre-selected spot from Available Spots
  useEffect(() => {
    if (preSelectedSpot) {
      // Use setTimeout to avoid synchronous state updates in effect
      setTimeout(() => {
        setSelectedSpot(preSelectedSpot);
        setCheckInDialog(true);
      }, 0);
    }
  }, [preSelectedSpot]);

  // Note: available spots and building lists are shown in dedicated pages; this section only handles check-in dialog when a spot is pre-selected.

  // Check-in mutation
  const checkInMutation = useMutation({
    mutationFn: (data: CheckInRequest) => userService.checkIn(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['activeSession'] });
      queryClient.invalidateQueries({ queryKey: ['availableSpots'] });
      queryClient.invalidateQueries({ queryKey: ['userDashboard'] });
      setCheckInDialog(false);
      setSelectedSpot(null);
      setVehicleNumber('');

      // Clear navigation state to prevent dialog from reopening
      window.history.replaceState({}, document.title);

    },
  });

  const handleCheckIn = () => {
    if (!selectedSpot || !vehicleNumber) {
      toast.push({ message: 'Please enter vehicle number', variant: 'warning' });
      return;
    }
    checkInMutation.mutate({
      spotId: selectedSpot.id,
      vehicleNumber: vehicleNumber,
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
        return 'bg-blue-100 text-blue-800 border-blue-300';
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

  return (
    <>
      {/* Check-in Dialog */}
      <Dialog open={checkInDialog} onOpenChange={setCheckInDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Check In to Parking Spot</DialogTitle>
            <DialogDescription>Start your parking session</DialogDescription>
          </DialogHeader>

          {selectedSpot && (
            <div className="space-y-4">
              {hasReservation && (
                <div className="bg-blue-50 border border-blue-200 rounded-md p-3">
                  <p className="text-sm text-blue-800">
                    <strong>✓ You have a reservation for this spot</strong>
                  </p>
                  <p className="text-xs text-blue-600 mt-1">
                    Proceeding with check-in will activate your reservation
                  </p>
                </div>
              )}

              <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Spot:</span>
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
                <Label htmlFor="vehicleNumber">Vehicle Number *</Label>
                <Input
                  id="vehicleNumber"
                  placeholder="e.g., ABC-1234"
                  value={vehicleNumber}
                  onChange={(e) => setVehicleNumber(e.target.value)}
                  disabled={checkInMutation.isPending}
                />
              </div>
            </div>
          )}

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setCheckInDialog(false);
                setSelectedSpot(null);
                setVehicleNumber('');
              }}
              disabled={checkInMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={handleCheckIn}
              disabled={checkInMutation.isPending || !vehicleNumber}
            >
              {checkInMutation.isPending ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Checking in...
                </>
              ) : (
                <>
                  <LogIn className="h-4 w-4 mr-2" />
                  Check In
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};

interface FeeCalculation {
  durationMinutes: number;
  hourlyRate: number;
  amountDue: number;
}

const ParkingManagement = () => {
  const queryClient = useQueryClient();
  const location = useLocation();
  const navigate = useNavigate();
  const toast = useToast();
  const [selectedSession, setSelectedSession] = useState<ParkingSession | null>(null);
  const [checkoutDialog, setCheckoutDialog] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PaymentMethod.CREDIT_CARD);
  const [feeCalculation, setFeeCalculation] = useState<FeeCalculation | null>(null);

  // Get navigation state
  const navigationState = location.state as {
    action?: 'checkin' | 'checkout';
    spot?: ParkingSpot;
    hasReservation?: boolean;
  } | null;

  // Refetch data when component mounts (tab switches)
  useEffect(() => {
    queryClient.invalidateQueries({ queryKey: ['activeSession'] });
    queryClient.invalidateQueries({ queryKey: ['mySessions'] });
  }, [queryClient]);

  // Fetch active session
  const { data: activeSession, isLoading: loadingActive } = useQuery({
    queryKey: ['activeSession'],
    queryFn: () => userService.getActiveSession(),
    refetchInterval: 15000, // Refresh every 15 seconds
  });

  // Fetch session history
  const { data: sessions, isLoading: loadingSessions } = useQuery({
    queryKey: ['mySessions'],
    queryFn: () => userService.getMySessions(),
  });

  // Calculate fee mutation
  const calculateFeeMutation = useMutation({
    mutationFn: (sessionId: string) => userService.calculateFee(sessionId),
    onSuccess: (data) => {
      setFeeCalculation(data);
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to calculate fee: ${error.message}`, variant: 'error' });
    },
  });

  // Checkout mutation
  const checkoutMutation = useMutation({
    mutationFn: ({ sessionId, paymentMethod }: { sessionId: string; paymentMethod: PaymentMethod }) =>
      userService.checkOut(sessionId, { sessionId, paymentMethod }),
    onSuccess: (bill) => {
      queryClient.invalidateQueries({ queryKey: ['activeSession'] });
      queryClient.invalidateQueries({ queryKey: ['mySessions'] });
      queryClient.invalidateQueries({ queryKey: ['userDashboard'] });
      setCheckoutDialog(false);
      setSelectedSession(null);
      setFeeCalculation(null);

      // Show success message with bill details
      const amount = typeof bill.amountDue === 'number' ? bill.amountDue.toFixed(2) : '0.00';
      toast.push({ message: `Checkout Successful! Transaction ID: ${bill.transactionId} Amount Paid: $${amount}`, variant: 'success' });

      // Clear navigation state to prevent check-in modal from appearing
      window.history.replaceState({}, document.title);
    },
    onError: (error: Error) => {
      toast.push({ message: `Checkout failed: ${error.message}`, variant: 'error' });
    },
  });

  // Handle navigation state for pre-selected actions
  useEffect(() => {
    if (navigationState?.action === 'checkout' && activeSession) {
      // Auto-open checkout dialog
      handleStartCheckout(activeSession);
      // Clear navigation state
      window.history.replaceState({}, document.title);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigationState, activeSession]);

  const handleStartCheckout = (session: ParkingSession) => {
    setSelectedSession(session);
    setCheckoutDialog(true);
    calculateFeeMutation.mutate(session.id);
  };

  const handleConfirmCheckout = () => {
    if (!selectedSession) return;

    checkoutMutation.mutate({
      sessionId: selectedSession.id,
      paymentMethod: paymentMethod,
    });
  };

  // Validate authentication on page load
  useAuthValidation();

  if (loadingActive || loadingSessions) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading parking sessions...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Parking Management</h1>
        <p className="text-gray-600 mt-2">Check-in and check-out of parking sessions</p>
      </div>

      {/* Check-in Section */}
      {!activeSession && (
        <CheckInSection
          preSelectedSpot={navigationState?.action === 'checkin' ? navigationState.spot : null}
          hasReservation={navigationState?.hasReservation}
        />
      )}

      {/* Active Session */}
      {activeSession ? (
        <Card className="border-blue-500 border-2">
          <CardHeader>
            <CardTitle className="flex items-center text-blue-600">
              <Clock className="h-5 w-5 mr-2 animate-pulse" />
              Active Parking Session
            </CardTitle>
            <CardDescription>You are currently parked</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Spot Number:</span>
                  <span className="font-semibold">{activeSession.spotNumber}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Location:</span>
                  <span className="font-semibold">
                    {activeSession.buildingName} - Floor {activeSession.floorNumber}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Vehicle:</span>
                  <span className="font-semibold">{activeSession.vehicleNumber}</span>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Check-in Time:</span>
                  <span className="font-semibold">
                    {new Date(activeSession.checkInTime).toLocaleString()}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Duration:</span>
                  <span className="font-semibold text-blue-600">
                    {formatDuration(activeSession.duration)}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Status:</span>
                  <Badge className="bg-green-100 text-green-800">Active</Badge>
                </div>
              </div>
            </div>
            <Button
              className="w-full"
              onClick={() => handleStartCheckout(activeSession)}
              size="lg"
            >
              <CreditCard className="h-5 w-5 mr-2" />
              Check Out & Pay
            </Button>
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <AlertCircle className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-semibold text-gray-900">No Active Session</p>
            <p className="text-sm text-gray-600 mt-2">
              You don't have any active parking sessions
            </p>
            <Button className="mt-4" onClick={() => navigateWithAnimation(navigate, '/user/spots')}>
              Find Parking Spot
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Session History */}
      <Card>
        <CardHeader>
          <CardTitle>Parking History</CardTitle>
          <CardDescription>Your past parking sessions</CardDescription>
        </CardHeader>
        <CardContent>
          {sessions && sessions.length > 0 ? (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Spot</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Vehicle</TableHead>
                    <TableHead>Check-in</TableHead>
                    <TableHead>Check-out</TableHead>
                    <TableHead>Duration</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sessions.map((session) => (
                    <TableRow key={session.id}>
                      <TableCell className="font-medium">{session.spotNumber}</TableCell>
                      <TableCell>
                        <div className="flex items-center text-sm">
                          <MapPin className="h-3 w-3 mr-1" />
                          {session.buildingName} - F{session.floorNumber}
                        </div>
                      </TableCell>
                      <TableCell>{session.vehicleNumber}</TableCell>
                      <TableCell className="text-sm">
                        {new Date(session.checkInTime).toLocaleString()}
                      </TableCell>
                      <TableCell className="text-sm">
                        {session.checkOutTime
                          ? new Date(session.checkOutTime).toLocaleString()
                          : '-'}
                      </TableCell>
                      <TableCell>
                        {formatDuration(session.duration)}
                      </TableCell>
                      <TableCell>
                        {session.status === 'ACTIVE' ? (
                          <Badge className="bg-green-100 text-green-800">Active</Badge>
                        ) : (
                          <Badge variant="outline">Completed</Badge>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              <p>No parking history available</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Checkout Dialog */}
      <Dialog open={checkoutDialog} onOpenChange={setCheckoutDialog}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Check Out & Payment</DialogTitle>
            <DialogDescription>Review your parking session and complete payment</DialogDescription>
          </DialogHeader>

          {selectedSession && (
            <div className="space-y-4">
              {/* Session Details */}
              <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                <h3 className="font-semibold text-sm text-gray-700 mb-2">Session Details</h3>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Spot:</span>
                  <span className="font-semibold">{selectedSession.spotNumber}</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Location:</span>
                  <span className="font-semibold">
                    {selectedSession.buildingName} - Floor {selectedSession.floorNumber}
                  </span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Vehicle:</span>
                  <span className="font-semibold">{selectedSession.vehicleNumber}</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-gray-600">Check-in:</span>
                  <span className="font-semibold">
                    {new Date(selectedSession.checkInTime).toLocaleString()}
                  </span>
                </div>
              </div>

              {/* Fee Calculation */}
              {calculateFeeMutation.isPending ? (
                <div className="bg-blue-50 p-4 rounded-lg text-center">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
                  <p className="text-sm text-blue-800">Calculating fee...</p>
                </div>
              ) : feeCalculation && feeCalculation.amountDue !== undefined ? (
                <div className="bg-green-50 p-4 rounded-lg space-y-2">
                  <h3 className="font-semibold text-sm text-gray-700 mb-2">Payment Summary</h3>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">Duration:</span>
                    <span className="font-semibold">{feeCalculation.durationMinutes} minutes</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">Hourly Rate:</span>
                    <span className="font-semibold">${feeCalculation.hourlyRate.toFixed(2)}/hr</span>
                  </div>
                  <div className="border-t pt-2 mt-2">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold">Total Amount:</span>
                      <span className="text-2xl font-bold text-green-600">
                        ${feeCalculation.amountDue.toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>
              ) : null}

              {/* Payment Method */}
              <div className="space-y-2">
                <Label htmlFor="paymentMethod">Payment Method</Label>
                <Select
                  value={paymentMethod}
                  onValueChange={(value) => setPaymentMethod(value as PaymentMethod)}
                >
                  <SelectTrigger id="paymentMethod">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={PaymentMethod.CREDIT_CARD}>Credit Card</SelectItem>
                    <SelectItem value={PaymentMethod.DEBIT_CARD}>Debit Card</SelectItem>
                    <SelectItem value={PaymentMethod.CASH}>Cash</SelectItem>
                    <SelectItem value={PaymentMethod.UPI}>UPI</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3 flex items-start space-x-2">
                <AlertCircle className="h-5 w-5 text-yellow-600 mt-0.5 flex-shrink-0" />
                <p className="text-xs text-yellow-800">
                  This is a simulated payment. In production, it would be redirected to a secure
                  payment gateway.
                </p>
              </div>
            </div>
          )}

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setCheckoutDialog(false);
                setSelectedSession(null);
                setFeeCalculation(null);
              }}
              disabled={checkoutMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={handleConfirmCheckout}
              disabled={checkoutMutation.isPending || !feeCalculation}
            >
              {checkoutMutation.isPending ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Processing...
                </>
              ) : (
                <>
                  <Receipt className="h-4 w-4 mr-2" />
                  Complete Payment
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ParkingManagement;

