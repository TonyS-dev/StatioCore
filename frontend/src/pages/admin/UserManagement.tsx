import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminService } from '../../services/adminService';
import { useAuth } from '../../hooks/useAuth';
import { Role } from '../../types';
import type { RegisterRequest, User } from '../../types';
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../../components/ui/table';
import { UserPlus, Filter, Shield, User as UserIcon, AlertCircle } from 'lucide-react';
import { useToast } from '../../components/ui/toast';

const UserManagement = () => {
  const queryClient = useQueryClient();
  const toast = useToast();
  const { user: currentUser } = useAuth();
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [activeFilter, setActiveFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const [createUserDialog, setCreateUserDialog] = useState(false);
  const [editUserDialog, setEditUserDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newUser, setNewUser] = useState<RegisterRequest & { role?: Role }>({
    fullName: '',
    email: '',
    password: '',
    role: Role.USER,
  });

  // Fetch users with filters
  const { data: usersData, isLoading, error } = useQuery({
    queryKey: ['adminUsers', roleFilter, activeFilter, page],
    queryFn: () =>
      adminService.getUsers(
        {
          role: roleFilter as Role | undefined,
          active: activeFilter ? activeFilter === 'true' : undefined,
        },
        { page, size: 10 }
      ),
  });

  // Create user mutation
  const createUserMutation = useMutation({
    mutationFn: (data: RegisterRequest & { role?: Role }) => adminService.createUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
      setCreateUserDialog(false);
      setNewUser({ fullName: '', email: '', password: '', role: Role.USER });
      toast.push({ message: 'User created successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to create user: ${error.message}`, variant: 'error' });
    },
  });

  // Update user status mutation
  const updateStatusMutation = useMutation({
    mutationFn: ({ userId, isActive }: { userId: string; isActive: boolean }) =>
      adminService.updateUserStatus(userId, isActive),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
      toast.push({ message: 'User status updated successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to update user status: ${error.message}`, variant: 'error' });
    },
  });

  // Update user mutation
  const updateUserMutation = useMutation({
    mutationFn: ({ userId, data }: { userId: string; data: Partial<User> }) =>
      adminService.updateUser(userId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
      setEditUserDialog(false);
      setSelectedUser(null);
      toast.push({ message: 'User updated successfully!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to update user: ${error.message}`, variant: 'error' });
    },
  });

  // Delete user mutation
  const deleteUserMutation = useMutation({
    mutationFn: (userId: string) => adminService.deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
      toast.push({ message: 'User deleted successfully (soft delete)!', variant: 'success' });
    },
    onError: (error: Error) => {
      toast.push({ message: `Failed to delete user: ${error.message}`, variant: 'error' });
    },
  });

  const handleCreateUser = () => {
    if (!newUser.fullName || !newUser.email || !newUser.password) {
      toast.push({ message: 'Please fill in all fields', variant: 'warning' });
      return;
    }

    if (newUser.password.length < 6) {
      toast.push({ message: 'Password must be at least 6 characters long', variant: 'warning' });
      return;
    }

    createUserMutation.mutate(newUser);
  };

  const handleToggleStatus = (user: User) => {
    // Prevent admin from deactivating themselves
    if (currentUser && currentUser.id === user.id && user.isActive && currentUser.role === Role.ADMIN) {
      toast.push({
        message: "Admins cannot deactivate their own account.",
        variant: "error",
      });
      return;
    }

    if (confirm(`Are you sure you want to ${user.isActive ? 'deactivate' : 'activate'} ${user.email}?`)) {
      updateStatusMutation.mutate({ userId: user.id, isActive: !user.isActive });
    }
  };

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setEditUserDialog(true);
  };

  const handleUpdateUser = () => {
    if (!selectedUser) return;

    updateUserMutation.mutate({
      userId: selectedUser.id,
      data: {
        fullName: selectedUser.fullName,
        email: selectedUser.email,
        role: selectedUser.role,
        isActive: selectedUser.isActive,
      },
    });
  };

  const handleDeleteUser = (user: User) => {
    // Prevent admin from deleting themselves
    if (currentUser && currentUser.email === user.email && currentUser.role === Role.ADMIN) {
      toast.push({ 
        message: 'Admin users cannot delete themselves', 
        variant: 'error' 
      });
      return;
    }

    if (
      confirm(
        `Are you sure you want to delete ${user.email}? This will soft delete the user.`
      )
    ) {
      deleteUserMutation.mutate(user.id);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-teal-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading users...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-md">
        <p className="font-semibold">Error loading users</p>
        <p className="text-sm">{(error as Error).message}</p>
      </div>
    );
  }

  const users = usersData?.items || [];
  const totalUsers = usersData?.totalElements || 0;
  const totalPages = usersData?.totalPages || 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600 mt-2">Manage system users and administrators</p>
        </div>
        <Button onClick={() => setCreateUserDialog(true)}>
          <UserPlus className="h-4 w-4 mr-2" />
          Create User
        </Button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Total Users</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{totalUsers}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Active Users</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-green-600">
              {users.filter((u) => u.isActive).length}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Admins</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-orange-600">
              {users.filter((u) => u.role === Role.ADMIN).length}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-gray-600">Regular Users</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold text-teal-600">
              {users.filter((u) => u.role === Role.USER).length}
            </p>
          </CardContent>
        </Card>
      </div>

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
              <Label>Role</Label>
              <Select value={roleFilter || 'all'} onValueChange={(value) => setRoleFilter(value === 'all' ? '' : value)}>
                <SelectTrigger>
                  <SelectValue placeholder="All Roles" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Roles</SelectItem>
                  <SelectItem value={Role.ADMIN}>Admin</SelectItem>
                  <SelectItem value={Role.USER}>User</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label>Status</Label>
              <Select value={activeFilter || 'all'} onValueChange={(value) => setActiveFilter(value === 'all' ? '' : value)}>
                <SelectTrigger>
                  <SelectValue placeholder="All Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="true">Active</SelectItem>
                  <SelectItem value="false">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex items-end">
              <Button
                variant="outline"
                onClick={() => {
                  setRoleFilter('');
                  setActiveFilter('');
                  setPage(0);
                }}
                className="w-full"
              >
                Clear Filters
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Users Table */}
      <Card>
        <CardHeader>
          <CardTitle>Users ({totalUsers})</CardTitle>
          <CardDescription>Showing page {page + 1} of {totalPages}</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users.length > 0 ? (
                  users.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>
                        <div className="flex items-center space-x-3">
                          <div className="bg-gray-100 p-2 rounded-full">
                            {user.role === Role.ADMIN ? (
                              <Shield className="h-4 w-4 text-orange-600" />
                            ) : (
                              <UserIcon className="h-4 w-4 text-teal-600" />
                            )}
                          </div>
                          <div>
                            <p className="font-medium">{user.fullName}</p>
                            <p className="text-xs text-gray-500">ID: {user.id.slice(0, 8)}...</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>
                        <Badge
                          className={
                            user.role === Role.ADMIN
                              ? 'bg-orange-100 text-orange-800'
                              : 'bg-teal-100 text-teal-800'
                          }
                          variant="outline"
                        >
                          {user.role}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge
                          className={
                            user.isActive
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }
                          variant="outline"
                        >
                          {user.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-sm">
                        {new Date(user.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end space-x-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEditUser(user)}
                          >
                            Edit
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleToggleStatus(user)}
                            disabled={updateStatusMutation.isPending}
                          >
                            {user.isActive ? 'Deactivate' : 'Activate'}
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDeleteUser(user)}
                            disabled={Boolean(deleteUserMutation.isPending) || (currentUser?.email === user.email && currentUser?.role === Role.ADMIN)}
                            title={currentUser?.email === user.email && currentUser?.role === Role.ADMIN ? "Admin users cannot delete themselves" : "Delete user"}
                          >
                            Delete
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8">
                      <AlertCircle className="h-8 w-8 text-gray-400 mx-auto mb-2" />
                      <p className="text-gray-600">No users found</p>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4">
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create User Dialog */}
      <Dialog open={createUserDialog} onOpenChange={setCreateUserDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New User</DialogTitle>
            <DialogDescription>Add a new user or administrator to the system</DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="fullName">Full Name</Label>
              <Input
                id="fullName"
                placeholder="John Doe"
                value={newUser.fullName}
                onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
                disabled={createUserMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="user@statiocore.com"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                disabled={createUserMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={newUser.password}
                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                disabled={createUserMutation.isPending}
              />
              <p className="text-xs text-gray-500">Minimum 6 characters</p>
            </div>

            <div className="space-y-2">
              <Label>Role</Label>
              <Select
                value={newUser.role}
                onValueChange={(value) => setNewUser({ ...newUser, role: value as Role })}
                disabled={createUserMutation.isPending}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={Role.USER}>User</SelectItem>
                  <SelectItem value={Role.ADMIN}>Admin</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="bg-teal-50 border border-blue-200 rounded-md p-3 flex items-start space-x-2">
              <Shield className="h-5 w-5 text-blue-600 mt-0.5 flex-shrink-0" />
              <p className="text-xs text-blue-800">
                {newUser.role === Role.ADMIN
                  ? 'Admin users have full privileges including user management and system configuration.'
                  : 'Regular users can access parking features, make reservations, and manage their account.'}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setCreateUserDialog(false);
                setNewUser({ fullName: '', email: '', password: '', role: Role.USER });
              }}
              disabled={createUserMutation.isPending}
            >
              Cancel
            </Button>
            <Button onClick={handleCreateUser} disabled={createUserMutation.isPending}>
              {createUserMutation.isPending ? 'Creating...' : 'Create User'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit User Dialog */}
      <Dialog open={editUserDialog} onOpenChange={setEditUserDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit User</DialogTitle>
            <DialogDescription>Update user information and permissions</DialogDescription>
          </DialogHeader>

          {selectedUser && (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="editFullName">Full Name</Label>
                <Input
                  id="editFullName"
                  placeholder="John Doe"
                  value={selectedUser.fullName}
                  onChange={(e) =>
                    setSelectedUser({ ...selectedUser, fullName: e.target.value })
                  }
                  disabled={updateUserMutation.isPending}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="editEmail">Email</Label>
                <Input
                  id="editEmail"
                  type="email"
                  placeholder="user@statiocore.com"
                  value={selectedUser.email}
                  onChange={(e) =>
                    setSelectedUser({ ...selectedUser, email: e.target.value })
                  }
                  disabled={updateUserMutation.isPending}
                />
              </div>

              <div className="space-y-2">
                <Label>Role</Label>
                <Select
                  value={selectedUser.role}
                  onValueChange={(value) =>
                    setSelectedUser({ ...selectedUser, role: value as Role })
                  }
                  disabled={updateUserMutation.isPending}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={Role.USER}>User</SelectItem>
                    <SelectItem value={Role.ADMIN}>Admin</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>Status</Label>
                <Select
                  value={selectedUser.isActive ? 'true' : 'false'}
                  onValueChange={(value) =>
                    setSelectedUser({ ...selectedUser, isActive: value === 'true' })
                  }
                  disabled={updateUserMutation.isPending}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="true">Active</SelectItem>
                    <SelectItem value="false">Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3 flex items-start space-x-2">
                <AlertCircle className="h-5 w-5 text-yellow-600 mt-0.5 flex-shrink-0" />
                <p className="text-xs text-yellow-800">
                  Changing the role or status will affect the user's access to the system.
                </p>
              </div>
            </div>
          )}

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setEditUserDialog(false);
                setSelectedUser(null);
              }}
              disabled={updateUserMutation.isPending}
            >
              Cancel
            </Button>
            <Button onClick={handleUpdateUser} disabled={updateUserMutation.isPending}>
              {updateUserMutation.isPending ? 'Updating...' : 'Update User'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default UserManagement;

