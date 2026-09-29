import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { PasswordInput } from '@/components/ui/PasswordInput';
import { Button } from '@/components/ui/Button';
import { api } from '@/api';
import { useAuthStore } from '@/store/authStore';
import { ROUTES } from '@/constants/routes';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const setAuth = useAuthStore(state => state.setAuth);
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormValues) => {
    try {
      setIsLoading(true);
      const res = await api.auth.login({ usernameOrEmail: data.email, password: data.password });
      setAuth(res.user, res.accessToken);
      toast.success('Login successfully');
      
      const from = (location.state as any)?.from?.pathname || ROUTES.HOME;
      navigate(from, { replace: true });
    } catch (error: any) {
      toast.error(error.message || 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Login</CardTitle>
          <p className="text-sm text-gray-500 mt-2">Login to access the system</p>
        </CardHeader>
        <CardBody className="space-y-6">
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <Input 
              label="Email" 
              showLabel 
              placeholder="name@example.com" 
              {...register('email')}
              error={errors.email?.message}
            />
            <PasswordInput 
              label="Password" 
              showLabel 
              placeholder="••••••••" 
              {...register('password')}
              error={errors.password?.message}
            />
            <div className="flex justify-end">
              <button 
                type="button" 
                onClick={() => navigate(ROUTES.FORGOT_PASSWORD)}
                className="text-sm font-medium text-indigo-600 hover:text-indigo-500"
              >
                Forgot password?
              </button>
            </div>
            <Button type="submit" fullWidth loading={isLoading}>
              Login
            </Button>
          </form>
          
          <p className="text-center text-sm text-gray-600 mt-6">
            Not have an account?{'  '}
            <button 
              type="button"
              onClick={() => navigate(ROUTES.REGISTER)} 
              className="font-medium text-indigo-600 hover:text-indigo-500"
            >
              Register now
            </button>
          </p>
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
