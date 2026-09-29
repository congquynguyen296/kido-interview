import { Logo } from '@/components/ui/Icons';

import { Dropdown } from '@/components/ui/Dropdown';
import { LogOut, User, LayoutDashboard } from 'lucide-react';
import type { ReactNode } from 'react';
import type { UserProfileResponse } from '@/types/user';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/authStore';

interface MainLayoutProps {
  children: ReactNode;
  user?: UserProfileResponse;
}

export const MainLayout = ({ children, user }: MainLayoutProps) => {
  const navigate = useNavigate();
  const logout = useAuthStore(state => state.logout);

  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.LOGIN);
  };

  return (
    <div className="min-h-dvh bg-gray-50 flex flex-col relative">
      <div className="absolute inset-0 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none z-0"></div>
      {/* Header */}
      <header className="sticky top-0 z-30 bg-white border-b border-gray-500/20 h-16">
        <div className="max-w-5xl mx-auto px-4 h-full flex items-center justify-between">
          <div className="cursor-pointer" onClick={() => navigate(ROUTES.HOME)}>
            <Logo />
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
                { id: 'profile', label: 'Profile', icon: <User />, onClick: () => navigate(ROUTES.PROFILE) },
                ...(user?.roles?.includes('ADMIN') ? [{ id: 'admin', label: 'Admin dashboard', icon: <LayoutDashboard />, onClick: () => navigate(ROUTES.ADMIN_DASHBOARD) }] : []),
                { id: 'divider', label: '-' },
                { id: 'logout', label: 'Logout', icon: <LogOut />, danger: true, onClick: handleLogout },
              ]}
            />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 w-full max-w-5xl mx-auto px-4 py-6 md:py-10 relative z-10">
        {children}
      </main>
    </div>
  );
};
