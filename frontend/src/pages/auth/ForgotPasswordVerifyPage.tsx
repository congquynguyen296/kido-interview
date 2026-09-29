import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
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

const verifyOtpSchema = z.object({
  otp: z.string().length(6, 'OTP must be exactly 6 characters'),
});

type VerifyOtpFormValues = z.infer<typeof verifyOtpSchema>;

export const ForgotPasswordVerifyPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const email = searchParams.get('email');
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<VerifyOtpFormValues>({
    resolver: zodResolver(verifyOtpSchema),
  });

  if (!email) {
    navigate(ROUTES.FORGOT_PASSWORD);
    return null;
  }

  const onSubmit = async (data: VerifyOtpFormValues) => {
    try {
      setIsLoading(true);
      const res = await api.auth.verifyResetOtp({ email, otp: data.otp });
      toast.success('OTP verified');
      navigate(`${ROUTES.RESET_PASSWORD}?token=${encodeURIComponent(res.resetToken)}`);
    } catch (error: any) {
      toast.error(error.message || 'Failed to verify OTP');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Verify OTP</CardTitle>
          <p className="text-sm text-gray-500 mt-2">Enter the 6-digit OTP sent to your email</p>
        </CardHeader>
        <CardBody className="space-y-6">
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            <Input 
              label="OTP" 
              showLabel 
              placeholder="123456" 
              maxLength={6}
              {...register('otp')}
              error={errors.otp?.message}
            />
            <Button type="submit" fullWidth loading={isLoading}>
              Verify
            </Button>
          </form>
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
