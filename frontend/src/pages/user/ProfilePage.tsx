import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/store/authStore';
import { api } from '@/api';
import { Card, CardBody, CardHeader, CardTitle } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { PasswordInput } from '@/components/ui/PasswordInput';
import { Button } from '@/components/ui/Button';
import { Tabs } from '@/components/ui/Tabs';
import { User, Lock, Mail, Phone } from 'lucide-react';

const profileSchema = z.object({
  fullName: z.string().min(2, 'Please enter your full name'),
  phoneNumber: z.string().optional(),
});

const passwordSchema = z.object({
  oldPassword: z.string().min(1, 'Please enter your old password'),
  newPassword: z.string().min(6, 'Please enter your new password'),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords do not match",
  path: ["confirmPassword"],
});

type ProfileFormValues = z.infer<typeof profileSchema>;
type PasswordFormValues = z.infer<typeof passwordSchema>;

export const ProfilePage = () => {
  const { user, setUser } = useAuthStore();
  const [activeTab, setActiveTab] = useState('info');
  const [isSaving, setIsSaving] = useState(false);

  const { register: registerProfile, handleSubmit: handleProfileSubmit, formState: { errors: profileErrors } } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      fullName: user?.fullName || '',
      phoneNumber: user?.phoneNumber || '',
    },
  });

  const { register: registerPassword, handleSubmit: handlePasswordSubmit, reset: resetPassword, formState: { errors: passwordErrors } } = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordSchema),
  });

  const onUpdateProfile = async (data: ProfileFormValues) => {
    try {
      setIsSaving(true);
      const updatedUser = await api.user.updateProfile(data);
      setUser(updatedUser);
      toast.success('Update profile successfully!');
    } catch (error: any) {
      toast.error(error.message || 'Update profile failed!');
    } finally {
      setIsSaving(false);
    }
  };

  const onChangePassword = async (data: PasswordFormValues) => {
    try {
      setIsSaving(true);
      await api.user.changePassword(data);
      toast.success('Change password successfully!');
      resetPassword();
    } catch (error: any) {
      toast.error(error.message || 'Change password failed!');
    } finally {
      setIsSaving(false);
    }
  };

  if (!user) return null;

  return (
    <div className="space-y-6 w-full">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">User Profile</h1>
        <p className="text-gray-500 mt-1">Manage your account information and security.</p>
      </div>

      <div className="flex flex-col md:flex-row gap-6">
        {/* Sidebar */}
        <div className="w-full md:w-64 shrink-0 space-y-6">
          <Card>
            <CardBody className="flex flex-col items-center text-center p-6 space-y-4">

              <div>
                <h3 className="font-semibold text-gray-900">{user.fullName}</h3>
                <p className="text-sm text-gray-500">{user.email}</p>
              </div>
            </CardBody>
          </Card>

          <Card>
            <CardBody className="p-2">
              <Tabs
                className="[&>div]:flex-col [&>div>button]:justify-start"
                tabs={[
                  { id: 'info', label: 'Profile', icon: <User className="w-4 h-4" /> },
                  { id: 'security', label: 'Security', icon: <Lock className="w-4 h-4" /> },
                ]}
                activeId={activeTab}
                onChange={setActiveTab}
              />
            </CardBody>
          </Card>
        </div>

        {/* Main Content */}
        <div className="flex-1">
          {activeTab === 'info' && (
            <Card className="animate-in fade-in slide-in-from-bottom-2 duration-300">
              <CardHeader>
                <CardTitle>Personal information</CardTitle>
                <p className="text-sm text-gray-500 mt-1">Update your basic information</p>
              </CardHeader>
              <CardBody>
                <form onSubmit={handleProfileSubmit(onUpdateProfile)} className="space-y-5">
                  <Input
                    label="Email"
                    showLabel
                    disabled
                    value={user.email}
                    leftIcon={<Mail className="w-4 h-4" />}
                    hint="Email can't be changed."
                  />
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                    <Input
                      label="Full name"
                      showLabel
                      {...registerProfile('fullName')}
                      error={profileErrors.fullName?.message}
                      leftIcon={<User className="w-4 h-4" />}
                    />
                    <Input
                      label="Phone number"
                      showLabel
                      {...registerProfile('phoneNumber')}
                      error={profileErrors.phoneNumber?.message}
                      leftIcon={<Phone className="w-4 h-4" />}
                    />
                  </div>
                  <div className="flex justify-end pt-4">
                    <Button type="submit" loading={isSaving}>
                      Save changes
                    </Button>
                  </div>
                </form>
              </CardBody>
            </Card>
          )}

          {activeTab === 'security' && (
            <Card className="animate-in fade-in slide-in-from-bottom-2 duration-300">
              <CardHeader>
                <CardTitle>Security</CardTitle>
                <p className="text-sm text-gray-500 mt-1">Change your password</p>
              </CardHeader>
              <CardBody>
                <form onSubmit={handlePasswordSubmit(onChangePassword)} className="space-y-5">
                  <PasswordInput
                    label="Old password"
                    showLabel
                    {...registerPassword('oldPassword')}
                    error={passwordErrors.oldPassword?.message}
                  />
                  <PasswordInput
                    label="New password"
                    showLabel
                    {...registerPassword('newPassword')}
                    error={passwordErrors.newPassword?.message}
                  />
                  <PasswordInput
                    label="Confirm new password"
                    showLabel
                    {...registerPassword('confirmPassword')}
                    error={passwordErrors.confirmPassword?.message}
                  />
                  <div className="flex justify-end pt-4">
                    <Button type="submit" loading={isSaving}>
                      Update password
                    </Button>
                  </div>
                </form>
              </CardBody>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};
