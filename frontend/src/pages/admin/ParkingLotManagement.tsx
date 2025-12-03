import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminService } from '../../services/adminService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { Badge } from '../../components/ui/badge';
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
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '../../components/ui/tabs';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import { Plus, Edit, Trash2, AlertCircle } from 'lucide-react';
import { useToast } from '../../components/ui/toast';

interface BuildingForm {
  name: string;
  address: string;
}

interface FloorForm {
  floorNumber: number;
  buildingId: string;
}

interface SpotForm {
  spotNumber: string;
  type: string;
  status: string;
  floorId: string;
}

const ParkingLotManagement = () => {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [activeTab, setActiveTab] = useState('buildings');
  
  // Dialog states
  const [buildingDialog, setBuildingDialog] = useState(false);
  const [floorDialog, setFloorDialog] = useState(false);
  const [spotDialog, setSpotDialog] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  
  // Form states
  const [buildingForm, setBuildingForm] = useState<BuildingForm>({ name: '', address: '' });
  const [floorForm, setFloorForm] = useState<FloorForm>({ floorNumber: 1, buildingId: '' });
  const [spotForm, setSpotForm] = useState<SpotForm>({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });

  // Fetch data
  const { data: buildings, isLoading: loadingBuildings } = useQuery({
    queryKey: ['adminBuildings'],
    queryFn: () => adminService.getAllBuildings(),
    refetchInterval: 30000,
  });

  const { data: floors, isLoading: loadingFloors } = useQuery({
    queryKey: ['adminFloors'],
    queryFn: () => adminService.getAllFloors(),
    refetchInterval: 30000,
  });

  const { data: spots, isLoading: loadingSpots } = useQuery({
    queryKey: ['adminSpots'],
    queryFn: () => adminService.getAllSpots(),
    refetchInterval: 30000,
  });

  // Building mutations
  const createBuildingMutation = useMutation({
    mutationFn: (data: BuildingForm) => adminService.createBuilding(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setBuildingDialog(false);
      setBuildingForm({ name: '', address: '' });
      toast.push({ message: 'Building created successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to create building: ${error.message}`, variant: 'error' });
    },
  });

  const updateBuildingMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: BuildingForm }) => adminService.updateBuilding(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setBuildingDialog(false);
      setBuildingForm({ name: '', address: '' });
      setEditMode(false);
      setSelectedId(null);
      toast.push({ message: 'Building updated successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to update building: ${error.message}`, variant: 'error' });
    },
  });

  const deleteBuildingMutation = useMutation({
    mutationFn: (id: string) => adminService.deleteBuilding(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      toast.push({ message: 'Building deleted successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to delete building: ${error.message}`, variant: 'error' });
    },
  });

  // Floor mutations
  const createFloorMutation = useMutation({
    mutationFn: (data: FloorForm) => adminService.createFloor(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminFloors'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setFloorDialog(false);
      setFloorForm({ floorNumber: 1, buildingId: '' });
      toast.push({ message: 'Floor created successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to create floor: ${error.message}`, variant: 'error' });
    },
  });

  const updateFloorMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: FloorForm }) => adminService.updateFloor(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminFloors'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setFloorDialog(false);
      setFloorForm({ floorNumber: 1, buildingId: '' });
      setEditMode(false);
      setSelectedId(null);
      toast.push({ message: 'Floor updated successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to update floor: ${error.message}`, variant: 'error' });
    },
  });

  const deleteFloorMutation = useMutation({
    mutationFn: (id: string) => adminService.deleteFloor(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminFloors'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      toast.push({ message: 'Floor deleted successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to delete floor: ${error.message}`, variant: 'error' });
    },
  });

  // Spot mutations
  const createSpotMutation = useMutation({
    mutationFn: (data: SpotForm) => adminService.createSpot(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminSpots'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setSpotDialog(false);
      setSpotForm({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });
      toast.push({ message: 'Parking spot created successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to create spot: ${error.message}`, variant: 'error' });
    },
  });

  const updateSpotMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: SpotForm }) => adminService.updateSpot(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminSpots'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      setSpotDialog(false);
      setSpotForm({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });
      setEditMode(false);
      setSelectedId(null);
      toast.push({ message: 'Parking spot updated successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to update spot: ${error.message}`, variant: 'error' });
    },
  });

  const deleteSpotMutation = useMutation({
    mutationFn: (id: string) => adminService.deleteSpot(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminSpots'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      toast.push({ message: 'Parking spot deleted successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to delete spot: ${error.message}`, variant: 'error' });
    },
  });

  // Handlers
  const handleCreateBuilding = () => {
    if (!buildingForm.name || !buildingForm.address) {
      toast.push({ message: 'Please fill in all fields', variant: 'warning' });
      return;
    }
    createBuildingMutation.mutate(buildingForm);
  };

  const handleUpdateBuilding = () => {
    if (!selectedId || !buildingForm.name || !buildingForm.address) {
      toast.push({ message: 'Please fill in all fields', variant: 'warning' });
      return;
    }
    updateBuildingMutation.mutate({ id: selectedId, data: buildingForm });
  };

  const handleEditBuilding = (building: any) => {
    setSelectedId(building.id);
    setBuildingForm({ name: building.name, address: building.address });
    setEditMode(true);
    setBuildingDialog(true);
  };

  const handleDeleteBuilding = (id: string, name: string) => {
    if (confirm(`Are you sure you want to delete building "${name}"? This will delete all associated floors and spots.`)) {
      deleteBuildingMutation.mutate(id);
    }
  };

  const handleCreateFloor = () => {
    if (!floorForm.buildingId) {
      toast.push({ message: 'Please select a building', variant: 'warning' });
      return;
    }
    createFloorMutation.mutate(floorForm);
  };

  const handleUpdateFloor = () => {
    if (!selectedId || !floorForm.buildingId) {
      toast.push({ message: 'Please fill in all fields', variant: 'warning' });
      return;
    }
    updateFloorMutation.mutate({ id: selectedId, data: floorForm });
  };

  const handleEditFloor = (floor: any) => {
    setSelectedId(floor.id);
    setFloorForm({ floorNumber: floor.floorNumber, buildingId: floor.buildingId });
    setEditMode(true);
    setFloorDialog(true);
  };

  const handleDeleteFloor = (id: string, floorNumber: number) => {
    if (confirm(`Are you sure you want to delete floor ${floorNumber}? This will delete all associated parking spots.`)) {
      deleteFloorMutation.mutate(id);
    }
  };

  const handleCreateSpot = () => {
    if (!spotForm.spotNumber || !spotForm.floorId) {
      toast.push({ message: 'Please fill in all required fields', variant: 'warning' });
      return;
    }
    createSpotMutation.mutate(spotForm);
  };

  const handleUpdateSpot = () => {
    if (!selectedId || !spotForm.spotNumber || !spotForm.floorId) {
      toast.push({ message: 'Please fill in all required fields', variant: 'warning' });
      return;
    }
    updateSpotMutation.mutate({ id: selectedId, data: spotForm });
  };

  const handleEditSpot = (spot: any) => {
    setSelectedId(spot.id);
    setSpotForm({
      spotNumber: spot.spotNumber,
      type: spot.type,
      status: spot.status,
      floorId: spot.floorId,
    });
    setEditMode(true);
    setSpotDialog(true);
  };

  const handleDeleteSpot = (id: string, spotNumber: string) => {
    if (confirm(`Are you sure you want to delete parking spot "${spotNumber}"?`)) {
      deleteSpotMutation.mutate(id);
    }
  };

  const getStatusBadge = (status: string) => {
    const colors = {
      AVAILABLE: 'bg-green-100 text-green-800',
      OCCUPIED: 'bg-red-100 text-red-800',
      RESERVED: 'bg-yellow-100 text-yellow-800',
      UNDER_MAINTENANCE: 'bg-gray-100 text-gray-800',
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  };

  const getTypeBadge = (type: string) => {
    const colors = {
      STANDARD: 'bg-teal-100 text-teal-800',
      VIP: 'bg-purple-100 text-purple-800',
      HANDICAP: 'bg-orange-100 text-orange-800',
    };
    return colors[type as keyof typeof colors] || 'bg-teal-100 text-teal-800';
  };

  if (loadingBuildings || loadingFloors || loadingSpots) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading parking lot data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Parking Lot Management</h1>
          <p className="text-gray-600 mt-2">Add, edit, and view Buildings, Floors, and Parking Spots</p>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
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
            <p className="text-2xl font-bold">{floors?.length || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Spots</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{spots?.length || 0}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Available Spots</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">
              {spots?.filter((s: any) => s.status === 'AVAILABLE').length || 0}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="buildings">Buildings</TabsTrigger>
          <TabsTrigger value="floors">Floors</TabsTrigger>
          <TabsTrigger value="spots">Parking Spots</TabsTrigger>
        </TabsList>

        {/* Buildings Tab */}
        <TabsContent value="buildings" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={() => {
              setBuildingForm({ name: '', address: '' });
              setEditMode(false);
              setSelectedId(null);
              setBuildingDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Building
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Buildings</CardTitle>
              <CardDescription>Manage parking buildings</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Address</TableHead>
                    <TableHead>Floors</TableHead>
                    <TableHead>Total Spots</TableHead>
                    <TableHead>Occupied</TableHead>
                    <TableHead>Available</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {buildings && buildings.length > 0 ? (
                    buildings.map((building: any) => (
                      <TableRow key={building.id}>
                        <TableCell className="font-medium">{building.name}</TableCell>
                        <TableCell>{building.address}</TableCell>
                        <TableCell>{building.totalFloors || 0}</TableCell>
                        <TableCell>{building.totalSpots || 0}</TableCell>
                        <TableCell>
                          <Badge className="bg-red-100 text-red-800">
                            {building.occupiedSpots || 0}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge className="bg-green-100 text-green-800">
                            {building.availableSpots || 0}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditBuilding(building)}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleDeleteBuilding(building.id, building.name)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-8">
                        <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                        <p className="text-gray-600">No buildings found</p>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Floors Tab */}
        <TabsContent value="floors" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={() => {
              setFloorForm({ floorNumber: 1, buildingId: '' });
              setEditMode(false);
              setSelectedId(null);
              setFloorDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Floor
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Floors</CardTitle>
              <CardDescription>Manage building floors</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Floor Number</TableHead>
                    <TableHead>Building</TableHead>
                    <TableHead>Total Spots</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {floors && floors.length > 0 ? (
                    floors.map((floor: any) => (
                      <TableRow key={floor.id}>
                        <TableCell className="font-medium">Floor {floor.floorNumber}</TableCell>
                        <TableCell>{floor.buildingName}</TableCell>
                        <TableCell>{floor.spotCount || 0}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditFloor(floor)}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleDeleteFloor(floor.id, floor.floorNumber)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={4} className="text-center py-8">
                        <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                        <p className="text-gray-600">No floors found</p>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Spots Tab */}
        <TabsContent value="spots" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={() => {
              setSpotForm({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });
              setEditMode(false);
              setSelectedId(null);
              setSpotDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Parking Spot
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Parking Spots</CardTitle>
              <CardDescription>Manage individual parking spots</CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Spot Number</TableHead>
                    <TableHead>Floor</TableHead>
                    <TableHead>Building</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {spots && spots.length > 0 ? (
                    spots.map((spot: any) => (
                      <TableRow key={spot.id}>
                        <TableCell className="font-medium">{spot.spotNumber}</TableCell>
                        <TableCell>Floor {spot.floorNumber}</TableCell>
                        <TableCell>{spot.buildingName}</TableCell>
                        <TableCell>
                          <Badge className={getTypeBadge(spot.type)} variant="outline">
                            {spot.type}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge className={getStatusBadge(spot.status)} variant="outline">
                            {spot.status.replace('_', ' ')}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleEditSpot(spot)}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleDeleteSpot(spot.id, spot.spotNumber)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center py-8">
                        <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                        <p className="text-gray-600">No parking spots found</p>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Building Dialog */}
      <Dialog open={buildingDialog} onOpenChange={setBuildingDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Building' : 'Create Building'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update building information' : 'Add a new parking building'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="buildingName">Building Name</Label>
              <Input
                id="buildingName"
                placeholder="Main Parking Structure"
                value={buildingForm.name}
                onChange={(e) => setBuildingForm({ ...buildingForm, name: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="buildingAddress">Address</Label>
              <Input
                id="buildingAddress"
                placeholder="123 Main St, City, State"
                value={buildingForm.address}
                onChange={(e) => setBuildingForm({ ...buildingForm, address: e.target.value })}
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setBuildingDialog(false)}>
              Cancel
            </Button>
            <Button onClick={editMode ? handleUpdateBuilding : handleCreateBuilding}>
              {editMode ? 'Update' : 'Create'} Building
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Floor Dialog */}
      <Dialog open={floorDialog} onOpenChange={setFloorDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Floor' : 'Create Floor'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update floor information' : 'Add a new floor to a building'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="floorBuilding">Building</Label>
              <Select
                value={floorForm.buildingId}
                onValueChange={(value) => setFloorForm({ ...floorForm, buildingId: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a building" />
                </SelectTrigger>
                <SelectContent>
                  {buildings?.map((building: any) => (
                    <SelectItem key={building.id} value={building.id}>
                      {building.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="floorNumber">Floor Number</Label>
              <Input
                id="floorNumber"
                type="number"
                min="1"
                value={floorForm.floorNumber}
                onChange={(e) => setFloorForm({ ...floorForm, floorNumber: parseInt(e.target.value) })}
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setFloorDialog(false)}>
              Cancel
            </Button>
            <Button onClick={editMode ? handleUpdateFloor : handleCreateFloor}>
              {editMode ? 'Update' : 'Create'} Floor
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Spot Dialog */}
      <Dialog open={spotDialog} onOpenChange={setSpotDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Parking Spot' : 'Create Parking Spot'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update parking spot details' : 'Add a new parking spot'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="spotFloor">Floor</Label>
              <Select
                value={spotForm.floorId}
                onValueChange={(value) => setSpotForm({ ...spotForm, floorId: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a floor" />
                </SelectTrigger>
                <SelectContent>
                  {floors?.map((floor: any) => (
                    <SelectItem key={floor.id} value={floor.id}>
                      {floor.buildingName} - Floor {floor.floorNumber}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="spotNumber">Spot Number</Label>
              <Input
                id="spotNumber"
                placeholder="A1, B2, etc."
                value={spotForm.spotNumber}
                onChange={(e) => setSpotForm({ ...spotForm, spotNumber: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="spotType">Type</Label>
              <Select
                value={spotForm.type}
                onValueChange={(value) => setSpotForm({ ...spotForm, type: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="STANDARD">Standard</SelectItem>
                  <SelectItem value="VIP">VIP</SelectItem>
                  <SelectItem value="HANDICAP">Handicap</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="spotStatus">Status</Label>
              <Select
                value={spotForm.status}
                onValueChange={(value) => setSpotForm({ ...spotForm, status: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="AVAILABLE">Available</SelectItem>
                  <SelectItem value="OCCUPIED">Occupied</SelectItem>
                  <SelectItem value="RESERVED">Reserved</SelectItem>
                  <SelectItem value="UNDER_MAINTENANCE">Under Maintenance</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setSpotDialog(false)}>
              Cancel
            </Button>
            <Button onClick={editMode ? handleUpdateSpot : handleCreateSpot}>
              {editMode ? 'Update' : 'Create'} Spot
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ParkingLotManagement;
