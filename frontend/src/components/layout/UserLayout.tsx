import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/button';
import { Car, LayoutDashboard, ParkingCircle, Calendar, LogIn, LogOut } from 'lucide-react';

const UserLayout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  // Helper function to check if a path is active
  const isActivePath = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
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
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/user/dashboard')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <LayoutDashboard className="h-4 w-4 mr-2" />
              Dashboard
            </Link>
            <Link
              to="/user/spots"
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/user/spots')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <ParkingCircle className="h-4 w-4 mr-2" />
              Available Spots
            </Link>
            <Link
              to="/user/reservations"
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/user/reservations')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <Calendar className="h-4 w-4 mr-2" />
              Reservations
            </Link>
            <Link
              to="/user/parking-management"
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/user/parking-management') || isActivePath('/user/parking')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <LogIn className="h-4 w-4 mr-2" />
              Parking Management
            </Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8 flex-1">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t">
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
