import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../ui/button';
import { Shield, LayoutDashboard, Building, Users, FileText, LogOut } from 'lucide-react';

const AdminLayout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-gradient-to-r from-teal-600 to-teal-800 text-white">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Shield className="h-8 w-8" />
              <div>
                <h1 className="text-2xl font-bold">ParkNexus Admin</h1>
                <p className="text-xs text-teal-200">Management Portal</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm">
                <span className="text-teal-200">Admin:</span>{' '}
                <span className="font-semibold">{user?.fullName}</span>
              </span>
              <Button variant="secondary" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white border-b shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex space-x-1">
            <Link
              to="/admin/dashboard"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <LayoutDashboard className="h-4 w-4 mr-2" />
              Dashboard
            </Link>
            <Link
              to="/admin/buildings"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <Building className="h-4 w-4 mr-2" />
              Parking Lot Management
            </Link>
            <Link
              to="/admin/users"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <Users className="h-4 w-4 mr-2" />
              Users
            </Link>
            <Link
              to="/admin/logs"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <FileText className="h-4 w-4 mr-2" />
              Activity Logs
            </Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-auto">
        <div className="container mx-auto px-4 py-6">
          <p className="text-center text-sm text-gray-600">
            © 2025 ParkNexus Admin Panel. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default AdminLayout;

