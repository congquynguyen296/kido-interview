import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { ROUTES } from '@/constants/routes';
import { Spinner } from '../ui/Spinner';
import { MainLayout } from './MainLayout';
import { AdminLayout } from './AdminLayout';

interface ProtectedRouteProps {
  allowedRoles?: string[];
  layout?: 'main' | 'admin' | 'none';
}

export const ProtectedRoute = ({ allowedRoles, layout = 'main' }: ProtectedRouteProps) => {
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Spinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    // Redirect to login but save the attempted url
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const hasRequiredRole = user.roles.some(role => allowedRoles.includes(role));
    if (!hasRequiredRole) {
      return <Navigate to={ROUTES.HOME} replace />;
    }
  }

  // Wrap with requested layout
  if (layout === 'admin') {
    return (
      <AdminLayout user={user}>
        <Outlet />
      </AdminLayout>
    );
  }

  if (layout === 'main') {
    return (
      <MainLayout user={user}>
        <Outlet />
      </MainLayout>
    );
  }

  return <Outlet />;
};
