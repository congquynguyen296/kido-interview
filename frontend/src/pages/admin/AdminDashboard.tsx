import { useQuery } from '@tanstack/react-query';
import { api } from '@/api';
import { StatCard } from '@/components/ui/StatCard';
import { SimpleBarChart } from '@/components/ui/SimpleBarChart';
import { DonutChart } from '@/components/ui/DonutChart';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { Users, User, Shield } from 'lucide-react';
import { SkeletonCard, Skeleton } from '@/components/ui/Skeleton';
import { ErrorState } from '@/components/ui/ErrorState';

export const AdminDashboard = () => {
  const { data: stats, isLoading: isLoadingStats, isError: isStatsError } = useQuery({
    queryKey: ['admin', 'users', 'statistics'],
    queryFn: () => api.adminUsers.getStatistics(),
  });

  if (isStatsError) {
    return <ErrorState message="Failed to load Dashboard data." />;
  }

  const totalUsers = stats?.totalUsers || 0;
  const activeUsers = stats?.usersByStatus?.ACTIVE || 0;
  const lockedUsers = stats?.usersByStatus?.LOCKED || 0;
  


  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 mt-1">System activity overview.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        {isLoadingStats ? (
          <>
            <Skeleton className="h-28" />
            <Skeleton className="h-28" />
            <Skeleton className="h-28" />
          </>
        ) : (
          <>
            <StatCard 
              title="Total Users" 
              value={totalUsers} 
              icon={<Users className="w-6 h-6" />} 
            />
            <StatCard 
              title="Active" 
              value={activeUsers} 
              iconTone="emerald"
              icon={<User className="w-6 h-6" />} 
            />
            <StatCard 
              title="Locked" 
              value={lockedUsers} 
              iconTone="red"
              icon={<Shield className="w-6 h-6" />} 
            />

          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {isLoadingStats ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <>
            <Card>
              <CardHeader>
                <CardTitle>New Registrations (Last 7 Days)</CardTitle>
              </CardHeader>
              <CardBody>
                <SimpleBarChart 
                  height={240}
                  data={[
                    { label: 'Mon', value: 2 }, 
                    { label: 'Tue', value: 5 },
                    { label: 'Wed', value: 3 }, 
                    { label: 'Thu', value: 8 },
                    { label: 'Fri', value: 12 }, 
                    { label: 'Sat', value: 4 },
                    { label: 'Sun', value: 6 },
                  ]} 
                />
              </CardBody>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Distribution by Status</CardTitle>
              </CardHeader>
              <CardBody className="flex justify-center items-center h-[240px]">
                <DonutChart 
                  data={[
                    { label: 'Active', value: activeUsers, color: '#10b981' },
                    { label: 'Unverified', value: Math.max(totalUsers - activeUsers - lockedUsers, 0), color: '#f59e0b' },
                    { label: 'Locked', value: lockedUsers, color: '#ef4444' },
                  ]} 
                />
              </CardBody>
            </Card>
          </>
        )}
      </div>
    </div>
  );
};
