import { useEffect, useState, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { api } from '@/api';
import { ROUTES } from '@/constants/routes';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { AuthLayout } from '@/components/layout/AuthLayout';

export const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('Verifying your email...');
  const calledRef = useRef(false);

  const email = searchParams.get('email');
  const token = searchParams.get('token');

  useEffect(() => {
    if (!email || !token) {
      setStatus('error');
      setMessage('Invalid verification link.');
      return;
    }

    if (calledRef.current) return;
    calledRef.current = true;

    api.auth.verifyEmail({ email, token })
      .then(() => {
        setStatus('success');
        setMessage('Your email has been successfully verified!');
      })
      .catch((err) => {
        setStatus('error');
        setMessage(err.message || 'Verification failed or token expired.');
      });
  }, [email, token]);

  return (
    <AuthLayout>
      <Card className="w-full">
        <CardHeader className="text-center pb-2">
          <CardTitle>Email Verification</CardTitle>
        </CardHeader>
        <CardBody className="space-y-6 flex flex-col items-center">
          {status === 'loading' && (
            <div className="flex flex-col items-center space-y-4">
              <Loader2 className="w-12 h-12 text-indigo-500 animate-spin" />
              <p className="text-gray-600">{message}</p>
            </div>
          )}
          
          {status === 'success' && (
            <div className="flex flex-col items-center space-y-4">
              <CheckCircle className="w-12 h-12 text-green-500" />
              <p className="text-gray-600 text-center">{message}</p>
              <Button onClick={() => navigate(ROUTES.LOGIN)} className="mt-4">
                Go to Login
              </Button>
            </div>
          )}

          {status === 'error' && (
            <div className="flex flex-col items-center space-y-4">
              <XCircle className="w-12 h-12 text-red-500" />
              <p className="text-gray-600 text-center">{message}</p>
              <Button onClick={() => navigate(ROUTES.LOGIN)} variant="secondary" className="mt-4">
                Back to Login
              </Button>
            </div>
          )}
        </CardBody>
      </Card>
    </AuthLayout>
  );
};
