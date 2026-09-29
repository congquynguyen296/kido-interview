import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { api } from '@/api';
import { ROUTES } from '@/constants/routes';

const forgotPasswordSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email'),
});

type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export const ForgotPasswordPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFormValues) => {
    try {
      setIsLoading(true);
      await api.auth.forgotPassword(data.email);
      toast.success('OTP sent to your email');
      navigate(`${ROUTES.FORGOT_PASSWORD_VERIFY}?email=${encodeURIComponent(data.email)}`);
    } catch (error: any) {
      toast.error(error.message || 'Failed to send OTP');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Forgot Password</CardTitle>
          <p className="text-sm text-gray-500 mt-2">Enter your email to receive an OTP</p>
        </CardHeader>
        <CardBody className="space-y-6">
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <Input 
              label="Email" 
              showLabel 
              placeholder="you@example.com" 
              {...register('email')}
              error={errors.email?.message}
            />
            <Button type="submit" fullWidth loading={isLoading}>
              Send OTP
            </Button>
          </form>
          
          <p className="text-center text-sm text-gray-600">
            Remembered your password?{' '}
            <button 
              type="button"
              onClick={() => navigate(ROUTES.LOGIN)} 
              className="font-medium text-indigo-600 hover:text-indigo-500"
            >
              Login now
            </button>
          </p>
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
