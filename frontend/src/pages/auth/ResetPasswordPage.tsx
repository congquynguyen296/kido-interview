import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { AuthLayout } from '@/components/layout/AuthLayout';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { PasswordInput } from '@/components/ui/PasswordInput';
import { Button } from '@/components/ui/Button';
import { api } from '@/api';
import { ROUTES } from '@/constants/routes';

const resetPasswordSchema = z.object({
  newPassword: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords do not match",
  path: ["confirmPassword"],
});

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

export const ResetPasswordPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
  });

  if (!token) {
    navigate(ROUTES.LOGIN);
    return null;
  }

  const onSubmit = async (data: ResetPasswordFormValues) => {
    try {
      setIsLoading(true);
      await api.auth.resetPassword({ resetToken: token, newPassword: data.newPassword, confirmPassword: data.confirmPassword });
      toast.success('Password reset successfully');
      navigate(ROUTES.LOGIN);
    } catch (error: any) {
      toast.error(error.message || 'Failed to reset password');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Reset Password</CardTitle>
          <p className="text-sm text-gray-500 mt-2">Enter your new password</p>
        </CardHeader>
        <CardBody className="space-y-6">
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <PasswordInput 
              label="New Password" 
              showLabel 
              placeholder="••••••••" 
              {...register('newPassword')}
              error={errors.newPassword?.message}
            />
            <PasswordInput 
              label="Confirm Password" 
              showLabel 
              placeholder="••••••••" 
              {...register('confirmPassword')}
              error={errors.confirmPassword?.message}
            />
            <Button type="submit" fullWidth loading={isLoading}>
              Reset Password
            </Button>
          </form>
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
