import { Button } from '@/components/ui/Button';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { useAuthStore } from '@/store/authStore';

export const HomePage = () => {
  const { user } = useAuthStore();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Hello, {user?.fullName || 'user'}!</h1>
        <p className="text-gray-500 mt-1">Welcome to the system.</p>
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle>Overview</CardTitle>
        </CardHeader>
        <CardBody>
          <p className="text-gray-600">This is the home page for normal users.</p>
          <div className="mt-4">
            <Button>Get Started</Button>
          </div>
        </CardBody>
      </Card>
    </div>
  );
};
