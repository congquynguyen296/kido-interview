import { useForm } from 'react-hook-form';
import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Modal } from '@/components/ui/Modal';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Select } from '@/components/ui/Select';

import type { UserDetailResponse } from '@/types/admin';

const userSchema = z.object({
  email: z.string().email('Invalid email'),
  fullName: z.string().min(2, 'Full name is too short'),
  status: z.enum(['ACTIVE', 'INACTIVE', 'LOCKED']),
  roles: z.array(z.string()).min(1, 'Select at least 1 role'),
});

type UserFormValues = z.infer<typeof userSchema>;

interface UserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: any) => Promise<void>;
  initialData?: UserDetailResponse | null;
  isSaving: boolean;
}

export const UserFormModal = ({ isOpen, onClose, onSubmit, initialData, isSaving }: UserFormModalProps) => {
  const roles = [
    { id: 'role_admin', name: 'ADMIN' },
    { id: 'role_user', name: 'USER' }
  ];

  const { register, handleSubmit, reset, formState: { errors }, setValue, watch } = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      email: '',
      fullName: '',
      status: 'ACTIVE',
      roles: [],
    },
  });

  const selectedRoles = watch('roles');

  useEffect(() => {
    if (isOpen) {
      if (initialData) {
        reset({
          email: initialData.email,
          fullName: initialData.fullName,
          status: initialData.status as any,
          roles: initialData.roles,
        });
      } else {
        reset({
          email: '',
          fullName: '',
          status: 'ACTIVE',
          roles: [],
        });
      }
    }
  }, [isOpen, initialData, reset]);

  const toggleRole = (roleName: string) => {
    const currentRoles = [...selectedRoles];
    const index = currentRoles.indexOf(roleName);
    if (index > -1) {
      currentRoles.splice(index, 1);
    } else {
      currentRoles.push(roleName);
    }
    setValue('roles', currentRoles, { shouldValidate: true });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={initialData ? 'Update User' : 'Add New User'}
      className="sm:max-w-md"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Email"
          showLabel
          {...register('email')}
          error={errors.email?.message}
          disabled={!!initialData} // Email cannot be changed
        />
        <Input
          label="Full Name"
          showLabel
          {...register('fullName')}
          error={errors.fullName?.message}
        />
        {!initialData && (
          <Select
            label="Status"
            showLabel
            {...register('status')}
            error={errors.status?.message}
          >
            <option value="ACTIVE">Active</option>
            <option value="LOCKED">Locked</option>
            <option value="INACTIVE">Inactive</option>
          </Select>
        )}

        <div>
          <label className="block text-sm font-medium text-gray-800 mb-2">Roles</label>
          <div className="space-y-2 max-h-40 overflow-y-auto border border-gray-500/30 rounded-xl p-3">
            {roles?.map(role => (
              <label key={role.id} className="flex items-center gap-3 p-1 cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedRoles.includes(role.name)}
                  onChange={() => toggleRole(role.name)}
                  className="h-4 w-4 rounded border-gray-500/30 text-indigo-500 focus:ring-indigo-500 cursor-pointer"
                />
                <span className="text-sm font-medium text-gray-700">{role.name}</span>
              </label>
            ))}
          </div>
          {errors.roles?.message && (
            <p className="text-red-500 text-xs mt-1">{errors.roles.message}</p>
          )}
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-gray-500/10 mt-6">
          <Button type="button" variant="secondary" onClick={onClose} disabled={isSaving}>
            Cancel
          </Button>
          <Button type="submit" loading={isSaving}>
            {initialData ? 'Update' : 'Add'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};
