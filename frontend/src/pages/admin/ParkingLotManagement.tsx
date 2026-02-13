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
  
  // Pagination states
  const [buildingPage, setBuildingPage] = useState(0);
  const [floorPage, setFloorPage] = useState(0);
  const [spotPage, setSpotPage] = useState(0);
  
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

  // Fetch paginated data
  const { data: buildingsData, isLoading: loadingBuildings } = useQuery({
    queryKey: ['adminBuildings', buildingPage],
    queryFn: () => adminService.getBuildings({ page: buildingPage, size: 10 }),
    refetchInterval: 30000,
  });

  const { data: floorsData, isLoading: loadingFloors } = useQuery({
    queryKey: ['adminFloors', floorPage],
    queryFn: () => adminService.getFloors({ page: floorPage, size: 10 }),
    refetchInterval: 30000,
  });

  const { data: spotsData, isLoading: loadingSpots } = useQuery({
    queryKey: ['adminSpots', spotPage],
    queryFn: () => adminService.getSpots({ page: spotPage, size: 10 }),
    refetchInterval: 30000,
  });

  // Also fetch all buildings for the floor/spot forms
  const { data: allBuildings } = useQuery({
    queryKey: ['adminBuildingsAll'],
    queryFn: () => adminService.getAllBuildings(),
  });

  const { data: allFloors } = useQuery({
    queryKey: ['adminFloorsAll'],
    queryFn: () => adminService.getAllFloors(),
  });

  // Building mutations
  const createBuildingMutation = useMutation({
    mutationFn: (data: BuildingForm) => adminService.createBuilding(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminFloorsAll'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminFloorsAll'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminFloorsAll'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildings'] });
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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
      queryClient.invalidateQueries({ queryKey: ['adminBuildingsAll'] });
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

  if (loadingBuildings && buildingPage === 0 && loadingFloors && floorPage === 0 && loadingSpots && spotPage === 0) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading parking lot data...</p>
        </div>
      </div>
    );
  }

  const buildings = buildingsData?.items || [];
  const totalBuildings = buildingsData?.totalElements || 0;
  const totalBuildingPages = buildingsData?.totalPages || 0;

  const floors = floorsData?.items || [];
  const totalFloors = floorsData?.totalElements || 0;
  const totalFloorPages = floorsData?.totalPages || 0;

  const spots = spotsData?.items || [];
  const totalSpots = spotsData?.totalElements || 0;
  const totalSpotPages = spotsData?.totalPages || 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Parking Lot Management</h1>
          <p className="text-sm sm:text-base text-gray-600 mt-1 sm:mt-2">Add, edit, and view Buildings, Floors, and Parking Spots</p>
        </div>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-6">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Buildings</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{totalBuildings}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Floors</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{totalFloors}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Parking Spots</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{totalSpots}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Showing Page</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {activeTab === 'buildings' ? buildingPage + 1 : activeTab === 'floors' ? floorPage + 1 : spotPage + 1}
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
              setEditMode(false);
              setSelectedId(null);
              setBuildingForm({ name: '', address: '' });
              setBuildingDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Building
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Buildings ({totalBuildings})</CardTitle>
              <CardDescription>Showing page {buildingPage + 1} of {totalBuildingPages}</CardDescription>
            </CardHeader>
            <CardContent>
              {loadingBuildings ? (
                <div className="text-center py-4">
                  <p className="text-gray-600">Loading buildings...</p>
                </div>
              ) : buildings.length > 0 ? (
                <>
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Name</TableHead>
                          <TableHead>Address</TableHead>
                          <TableHead>Floors</TableHead>
                          <TableHead>Total Spots</TableHead>
                          <TableHead>Occupied</TableHead>
                          <TableHead>Available</TableHead>
                          <TableHead>Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {buildings.map((building: any) => (
                          <TableRow key={building.id}>
                            <TableCell className="font-medium">{building.name}</TableCell>
                            <TableCell>{building.address}</TableCell>
                            <TableCell>{building.totalFloors}</TableCell>
                            <TableCell>{building.totalSpots}</TableCell>
                            <TableCell>{building.occupiedSpots}</TableCell>
                            <TableCell>{building.availableSpots}</TableCell>
                            <TableCell className="space-x-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleEditBuilding(building)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="text-red-600 hover:text-red-700"
                                onClick={() => handleDeleteBuilding(building.id, building.name)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>

                  {/* Pagination */}
                  {totalBuildingPages > 1 && (
                    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mt-4">
                      <Button
                        variant="outline"
                        onClick={() => setBuildingPage((p) => Math.max(0, p - 1))}
                        disabled={buildingPage === 0}
                        className="w-full sm:w-auto"
                      >
                        Previous
                      </Button>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm text-gray-600">Page</span>
                        <Input
                          type="number"
                          min="1"
                          max={totalBuildingPages}
                          value={buildingPage + 1}
                          onChange={(e) => {
                            const newPage = parseInt(e.target.value) - 1;
                            if (newPage >= 0 && newPage < totalBuildingPages) {
                              setBuildingPage(newPage);
                            }
                          }}
                          className="w-20 text-center"
                        />
                        <span className="text-sm text-gray-600">of {totalBuildingPages}</span>
                      </div>
                      <Button
                        variant="outline"
                        onClick={() => setBuildingPage((p) => Math.min(totalBuildingPages - 1, p + 1))}
                        disabled={buildingPage === totalBuildingPages - 1}
                        className="w-full sm:w-auto"
                      >
                        Next
                      </Button>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-8">
                  <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                  <p className="text-gray-600 font-semibold">No buildings found</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Floors Tab */}
        <TabsContent value="floors" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={() => {
              setEditMode(false);
              setSelectedId(null);
              setFloorForm({ floorNumber: 1, buildingId: '' });
              setFloorDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Floor
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Floors ({totalFloors})</CardTitle>
              <CardDescription>Showing page {floorPage + 1} of {totalFloorPages}</CardDescription>
            </CardHeader>
            <CardContent>
              {loadingFloors ? (
                <div className="text-center py-4">
                  <p className="text-gray-600">Loading floors...</p>
                </div>
              ) : floors.length > 0 ? (
                <>
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Floor Number</TableHead>
                          <TableHead>Building</TableHead>
                          <TableHead>Parking Spots</TableHead>
                          <TableHead>Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {floors.map((floor: any) => (
                          <TableRow key={floor.id}>
                            <TableCell className="font-medium">{floor.floorNumber}</TableCell>
                            <TableCell>{floor.buildingName}</TableCell>
                            <TableCell>{floor.spotCount}</TableCell>
                            <TableCell className="space-x-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleEditFloor(floor)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="text-red-600 hover:text-red-700"
                                onClick={() => handleDeleteFloor(floor.id, floor.floorNumber)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>

                  {/* Pagination */}
                  {totalFloorPages > 1 && (
                    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mt-4">
                      <Button
                        variant="outline"
                        onClick={() => setFloorPage((p) => Math.max(0, p - 1))}
                        disabled={floorPage === 0}
                        className="w-full sm:w-auto"
                      >
                        Previous
                      </Button>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm text-gray-600">Page</span>
                        <Input
                          type="number"
                          min="1"
                          max={totalFloorPages}
                          value={floorPage + 1}
                          onChange={(e) => {
                            const newPage = parseInt(e.target.value) - 1;
                            if (newPage >= 0 && newPage < totalFloorPages) {
                              setFloorPage(newPage);
                            }
                          }}
                          className="w-20 text-center"
                        />
                        <span className="text-sm text-gray-600">of {totalFloorPages}</span>
                      </div>
                      <Button
                        variant="outline"
                        onClick={() => setFloorPage((p) => Math.min(totalFloorPages - 1, p + 1))}
                        disabled={floorPage === totalFloorPages - 1}
                        className="w-full sm:w-auto"
                      >
                        Next
                      </Button>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-8">
                  <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                  <p className="text-gray-600 font-semibold">No floors found</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Parking Spots Tab */}
        <TabsContent value="spots" className="space-y-4">
          <div className="flex justify-end">
            <Button onClick={() => {
              setEditMode(false);
              setSelectedId(null);
              setSpotForm({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });
              setSpotDialog(true);
            }}>
              <Plus className="h-4 w-4 mr-2" />
              Add Parking Spot
            </Button>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Parking Spots ({totalSpots})</CardTitle>
              <CardDescription>Showing page {spotPage + 1} of {totalSpotPages}</CardDescription>
            </CardHeader>
            <CardContent>
              {loadingSpots ? (
                <div className="text-center py-4">
                  <p className="text-gray-600">Loading parking spots...</p>
                </div>
              ) : spots.length > 0 ? (
                <>
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Spot Number</TableHead>
                          <TableHead>Building</TableHead>
                          <TableHead>Floor</TableHead>
                          <TableHead>Type</TableHead>
                          <TableHead>Status</TableHead>
                          <TableHead>Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {spots.map((spot: any) => (
                          <TableRow key={spot.id}>
                            <TableCell className="font-medium">{spot.spotNumber}</TableCell>
                            <TableCell>{spot.buildingName}</TableCell>
                            <TableCell>Floor {spot.floorNumber}</TableCell>
                            <TableCell>
                              <Badge className={getTypeBadge(spot.type)}>
                                {spot.type}
                              </Badge>
                            </TableCell>
                            <TableCell>
                              <Badge className={getStatusBadge(spot.status)}>
                                {spot.status}
                              </Badge>
                            </TableCell>
                            <TableCell className="space-x-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleEditSpot(spot)}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                className="text-red-600 hover:text-red-700"
                                onClick={() => handleDeleteSpot(spot.id, spot.spotNumber)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>

                  {/* Pagination */}
                  {totalSpotPages > 1 && (
                    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 mt-4">
                      <Button
                        variant="outline"
                        onClick={() => setSpotPage((p) => Math.max(0, p - 1))}
                        disabled={spotPage === 0}
                        className="w-full sm:w-auto"
                      >
                        Previous
                      </Button>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm text-gray-600">Page</span>
                        <Input
                          type="number"
                          min="1"
                          max={totalSpotPages}
                          value={spotPage + 1}
                          onChange={(e) => {
                            const newPage = parseInt(e.target.value) - 1;
                            if (newPage >= 0 && newPage < totalSpotPages) {
                              setSpotPage(newPage);
                            }
                          }}
                          className="w-20 text-center"
                        />
                        <span className="text-sm text-gray-600">of {totalSpotPages}</span>
                      </div>
                      <Button
                        variant="outline"
                        onClick={() => setSpotPage((p) => Math.min(totalSpotPages - 1, p + 1))}
                        disabled={spotPage === totalSpotPages - 1}
                        className="w-full sm:w-auto"
                      >
                        Next
                      </Button>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-8">
                  <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                  <p className="text-gray-600 font-semibold">No parking spots found</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Building Dialog */}
      <Dialog open={buildingDialog} onOpenChange={setBuildingDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Building' : 'Create New Building'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update building details' : 'Add a new parking building to the system'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="buildingName">Building Name</Label>
              <Input
                id="buildingName"
                placeholder="Main Parking Building"
                value={buildingForm.name}
                onChange={(e) => setBuildingForm({ ...buildingForm, name: e.target.value })}
                disabled={createBuildingMutation.isPending || updateBuildingMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="buildingAddress">Address</Label>
              <Input
                id="buildingAddress"
                placeholder="123 Main St, Downtown"
                value={buildingForm.address}
                onChange={(e) => setBuildingForm({ ...buildingForm, address: e.target.value })}
                disabled={createBuildingMutation.isPending || updateBuildingMutation.isPending}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setBuildingDialog(false);
                setEditMode(false);
                setBuildingForm({ name: '', address: '' });
              }}
              disabled={createBuildingMutation.isPending || updateBuildingMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={editMode ? handleUpdateBuilding : handleCreateBuilding}
              disabled={createBuildingMutation.isPending || updateBuildingMutation.isPending}
            >
              {editMode ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Floor Dialog */}
      <Dialog open={floorDialog} onOpenChange={setFloorDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Floor' : 'Create New Floor'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update floor details' : 'Add a new floor to a building'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="floorNumber">Floor Number</Label>
              <Input
                id="floorNumber"
                type="number"
                min="1"
                value={floorForm.floorNumber}
                onChange={(e) => setFloorForm({ ...floorForm, floorNumber: parseInt(e.target.value) })}
                disabled={createFloorMutation.isPending || updateFloorMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="floorBuilding">Building</Label>
              <Select
                value={floorForm.buildingId}
                onValueChange={(value) => setFloorForm({ ...floorForm, buildingId: value })}
              >
                <SelectTrigger id="floorBuilding">
                  <SelectValue placeholder="Select building" />
                </SelectTrigger>
                <SelectContent>
                  {allBuildings?.map((building: any) => (
                    <SelectItem key={building.id} value={building.id}>
                      {building.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setFloorDialog(false);
                setEditMode(false);
                setFloorForm({ floorNumber: 1, buildingId: '' });
              }}
              disabled={createFloorMutation.isPending || updateFloorMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={editMode ? handleUpdateFloor : handleCreateFloor}
              disabled={createFloorMutation.isPending || updateFloorMutation.isPending}
            >
              {editMode ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Parking Spot Dialog */}
      <Dialog open={spotDialog} onOpenChange={setSpotDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editMode ? 'Edit Parking Spot' : 'Create New Parking Spot'}</DialogTitle>
            <DialogDescription>
              {editMode ? 'Update parking spot details' : 'Add a new parking spot to a floor'}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="spotNumber">Spot Number</Label>
              <Input
                id="spotNumber"
                placeholder="A1"
                value={spotForm.spotNumber}
                onChange={(e) => setSpotForm({ ...spotForm, spotNumber: e.target.value })}
                disabled={createSpotMutation.isPending || updateSpotMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="spotFloor">Floor</Label>
              <Select
                value={spotForm.floorId}
                onValueChange={(value) => setSpotForm({ ...spotForm, floorId: value })}
              >
                <SelectTrigger id="spotFloor">
                  <SelectValue placeholder="Select floor" />
                </SelectTrigger>
                <SelectContent>
                  {allFloors?.map((floor: any) => (
                    <SelectItem key={floor.id} value={floor.id}>
                      {floor.buildingName} - Floor {floor.floorNumber}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="spotType">Type</Label>
              <Select
                value={spotForm.type}
                onValueChange={(value) => setSpotForm({ ...spotForm, type: value })}
              >
                <SelectTrigger id="spotType">
                  <SelectValue placeholder="Select type" />
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
                <SelectTrigger id="spotStatus">
                  <SelectValue placeholder="Select status" />
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
            <Button
              variant="outline"
              onClick={() => {
                setSpotDialog(false);
                setEditMode(false);
                setSpotForm({ spotNumber: '', type: 'STANDARD', status: 'AVAILABLE', floorId: '' });
              }}
              disabled={createSpotMutation.isPending || updateSpotMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              onClick={editMode ? handleUpdateSpot : handleCreateSpot}
              disabled={createSpotMutation.isPending || updateSpotMutation.isPending}
            >
              {editMode ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ParkingLotManagement;
