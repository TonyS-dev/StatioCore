import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../ui/button';
import { Car, LayoutDashboard, ParkingCircle, Calendar, LogIn, LogOut } from 'lucide-react';

const UserLayout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Car className="h-8 w-8 text-teal-600" />
              <h1 className="text-2xl font-bold text-gray-900">ParkNexus</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-600">
                Welcome, <span className="font-semibold">{user?.fullName}</span>
              </span>
              <Button variant="outline" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Navigation */}
      <nav className="bg-white border-b">
        <div className="container mx-auto px-4">
          <div className="flex space-x-1">
            <Link
              to="/user/dashboard"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <LayoutDashboard className="h-4 w-4 mr-2" />
              Dashboard
            </Link>
            <Link
              to="/user/spots"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <ParkingCircle className="h-4 w-4 mr-2" />
              Available Spots
            </Link>
            <Link
              to="/user/reservations"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <Calendar className="h-4 w-4 mr-2" />
              Reservations
            </Link>
            <Link
              to="/user/parking-management"
              className="flex items-center px-4 py-3 text-sm font-medium text-gray-700 hover:text-teal-600 hover:bg-gray-50 border-b-2 border-transparent hover:border-teal-600 transition-colors"
            >
              <LogIn className="h-4 w-4 mr-2" />
              Parking Management
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
            © 2025 ParkNexus. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default UserLayout;

