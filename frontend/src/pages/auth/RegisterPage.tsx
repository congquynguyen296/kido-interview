import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { ROUTES } from '@/constants/routes';
import { User, Mail } from 'lucide-react';

const registerSchema = z.object({
  fullName: z.string().min(2, 'Please enter your full name'),
  email: z.string().min(1, 'Please enter your email').email('Invalid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(6, 'Password must be at least 6 characters'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords do not match",
  path: ["confirmPassword"],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export const RegisterPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormValues) => {
    try {
      setIsLoading(true);
      await api.auth.register(data);
      toast.success('Register successfully. Please verify your email to sign in!');
      navigate(ROUTES.LOGIN);
    } catch (error: any) {
      toast.error(error.message || 'Register failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Register</CardTitle>
          <p className="text-sm text-gray-500 mt-2">Create a new account to experience the system</p>
        </CardHeader>
        <CardBody className="space-y-6">
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <Input 
              label="Full Name" 
              showLabel 
              placeholder="Nguyen Van A" 
              leftIcon={<User className="w-4 h-4" />}
              {...register('fullName')}
              error={errors.fullName?.message}
            />
            <Input 
              label="Email" 
              showLabel 
              placeholder="name@example.com" 
              leftIcon={<Mail className="w-4 h-4" />}
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
            <PasswordInput 
              label="Confirm Password" 
              showLabel 
              placeholder="••••••••" 
              {...register('confirmPassword')}
              error={errors.confirmPassword?.message}
            />
            
            <div className="pt-2">
              <Button type="submit" fullWidth loading={isLoading}>
                Register
              </Button>
            </div>
          </form>
          
          <p className="text-center text-sm text-gray-600">
            Already have an account?{' '}
            <button 
              type="button"
              onClick={() => navigate(ROUTES.LOGIN)}
              className="font-medium text-indigo-600 hover:text-indigo-500"
            >
              Login
            </button>
          </p>
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
