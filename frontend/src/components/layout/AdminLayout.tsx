import { Logo } from '@/components/ui/Icons';

import { Dropdown } from '@/components/ui/Dropdown';
import { cn } from '@/utils/cn';
import { 
  LogOut, 
  User, 
  LayoutDashboard, 
  Users,
  Menu,
  X
} from 'lucide-react';
import type { ReactNode } from 'react';
import type { UserProfileResponse } from '@/types/user';
import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/authStore';

interface AdminLayoutProps {
  children: ReactNode;
  user?: UserProfileResponse;
}

export const AdminLayout = ({ children }: AdminLayoutProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore(state => state.logout);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.LOGIN);
  };

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard className="w-5 h-5" />, path: ROUTES.ADMIN_DASHBOARD },
    { id: 'users', label: 'User', icon: <Users className="w-5 h-5" />, path: ROUTES.ADMIN_USERS },
  ];

  // Close mobile menu on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  // Lock body scroll when mobile menu is open
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isMobileMenuOpen]);

  return (
    <div className="min-h-dvh bg-gray-50 flex relative">
      <div className="absolute inset-0 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none z-0"></div>
      {/* Mobile Drawer Overlay */}
      {isMobileMenuOpen && (
        <div 
          className="fixed inset-0 bg-black/40 z-40 lg:hidden"
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={cn(
        "fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-500/20 transform transition-transform duration-200 ease-in-out lg:translate-x-0 lg:static lg:block flex flex-col",
        isMobileMenuOpen ? "translate-x-0" : "-translate-x-full"
      )}>
        <div className="h-16 flex items-center justify-between px-6 border-b border-gray-500/20 shrink-0">
          <Logo />
          <button 
            className="lg:hidden text-gray-500 hover:text-gray-700"
            onClick={() => setIsMobileMenuOpen(false)}
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto py-4 px-3 flex flex-col gap-1">
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path || (item.path !== ROUTES.ADMIN_DASHBOARD && location.pathname.startsWith(item.path));
            return (
              <button
                key={item.id}
                onClick={() => navigate(item.path)}
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-full text-sm font-medium transition-colors w-full text-left",
                  isActive 
                    ? "bg-indigo-50 text-indigo-600" 
                    : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                )}
              >
                {item.icon}
                {item.label}
              </button>
            );
          })}
        </div>

        <div className="p-3 border-t border-gray-500/20">
          <button
            onClick={() => navigate(ROUTES.ADMIN_PROFILE)}
            className="flex items-center gap-3 px-3 py-2.5 rounded-full text-sm font-medium text-gray-600 hover:bg-gray-100 w-full text-left transition-colors"
          >
            <User className="w-5 h-5" />
            Profile
          </button>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-full text-sm font-medium text-red-600 hover:bg-red-50 w-full text-left mt-1 transition-colors"
          >
            <LogOut className="w-5 h-5" />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 relative z-10">
        {/* Header */}
        <header className="sticky top-0 z-30 bg-white border-b border-gray-500/20 h-16 flex items-center justify-between px-4 lg:px-8">
          <div className="flex items-center gap-4">
            <button 
              className="lg:hidden text-gray-500 hover:text-gray-700"
              onClick={() => setIsMobileMenuOpen(true)}
            >
              <Menu className="w-6 h-6" />
            </button>
            
            {/* Breadcrumb (simplified) */}
            <div className="text-sm font-bold text-gray-800">
              Admin Panel
            </div>
          </div>

          <div className="flex items-center gap-4">
            <Dropdown
              align="right"
              trigger={
                <button className="flex items-center gap-2 focus:outline-none">
                  <div className="w-8 h-8 bg-gray-50 rounded-full flex items-center justify-center overflow-hidden border border-gray-200">
                    <img src="/logo_main.svg" alt="Avatar" className="w-full h-full object-contain p-1" />
                  </div>
                </button>
              }
              items={[
                { id: 'profile', label: 'Profile', icon: <User />, onClick: () => navigate(ROUTES.ADMIN_PROFILE) },
                { id: 'divider', label: '-' },
                { id: 'logout', label: 'Logout', icon: <LogOut />, danger: true, onClick: handleLogout },
              ]}
            />
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 p-4 lg:p-8 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
};
