import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/button';
import { Shield, LayoutDashboard, Building, Users, FileText, LogOut } from 'lucide-react';
import { useEffect } from 'react';

const AdminLayout = () => {
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

  // Persist last active admin tab in sessionStorage
  useEffect(() => {
    if (location.pathname === '/admin' || location.pathname === '/admin/') {
      const last = sessionStorage.getItem('lastAdminTab');
      if (last) {
        navigate(last, { replace: true });
      } else {
        // default to admin dashboard
        navigate('/admin/dashboard', { replace: true });
      }
    }
  }, [location.pathname, navigate]);

  const handleTabClick = (path: string) => {
    try {
      sessionStorage.setItem('lastAdminTab', path);
    } catch (e) {
      // ignore storage errors
    }
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
      <nav className="sticky top-0 z-40 bg-white border-b shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex space-x-1">
            <Link
              to="/admin/dashboard"
              onClick={() => handleTabClick('/admin/dashboard')}
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/admin/dashboard')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <LayoutDashboard className="h-4 w-4 mr-2" />
              Dashboard
            </Link>
            <Link
              to="/admin/buildings"
              onClick={() => handleTabClick('/admin/buildings')}
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/admin/buildings')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <Building className="h-4 w-4 mr-2" />
              Parking Lot Management
            </Link>
            <Link
              to="/admin/users"
              onClick={() => handleTabClick('/admin/users')}
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/admin/users')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
            >
              <Users className="h-4 w-4 mr-2" />
              Users
            </Link>
            <Link
              to="/admin/logs"
              onClick={() => handleTabClick('/admin/logs')}
              className={`flex items-center px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                isActivePath('/admin/logs')
                  ? 'text-teal-600 bg-teal-50 border-teal-600'
                  : 'text-gray-700 border-transparent hover:text-teal-600 hover:bg-gray-50 hover:border-teal-600'
              }`}
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

