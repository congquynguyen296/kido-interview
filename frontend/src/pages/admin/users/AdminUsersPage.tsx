import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/api';
import { Card, CardHeader } from '@/components/ui/Card';
import { DataTable } from '@/components/ui/DataTable';
import { Pagination } from '@/components/ui/Pagination';
import { Button } from '@/components/ui/Button';
import { SearchInput } from '@/components/ui/SearchInput';
import { Badge } from '@/components/ui/Badge';
import { Dropdown } from '@/components/ui/Dropdown';

import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { Plus, Edit2, Lock, Unlock, Trash2, KeyRound, User } from 'lucide-react';
import toast from 'react-hot-toast';
import type { UserSummaryResponse, UserDetailResponse } from '@/types/admin';
import type { EntityStatus } from '@/types/user';
import { UserFormModal } from './UserFormModal';

export const AdminUsersPage = () => {
  const queryClient = useQueryClient();
  
  // State
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  
  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserDetailResponse | null>(null);
  
  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserSummaryResponse | null>(null);

  // Queries
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'users', { page, size, search }],
    queryFn: () => api.adminUsers.getUsers({ page, size, search }),
    placeholderData: (previousData) => previousData,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: api.adminUsers.createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('Add user successfully');
      setIsFormOpen(false);
    },
    onError: (error: any) => toast.error(error.message),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string, data: any }) => api.adminUsers.updateUser(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('Update user successfully');
      setIsFormOpen(false);
    },
    onError: (error: any) => toast.error(error.message),
  });

  const deleteMutation = useMutation({
    mutationFn: api.adminUsers.deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('Delete user successfully');
      setIsDeleteOpen(false);
    },
    onError: (error: any) => toast.error(error.message),
  });

  const toggleLockMutation = useMutation({
    mutationFn: ({ id, status }: { id: string, status: EntityStatus }) => 
      status === 'LOCKED' ? api.adminUsers.unlockUser(id) : api.adminUsers.lockUser(id),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success(variables.status === 'LOCKED' ? 'Unlock user successfully' : 'Lock user successfully');
    },
    onError: (error: any) => toast.error(error.message),
  });

  // Handlers
  const handleEdit = async (user: UserSummaryResponse) => {
    try {
      const detail = await api.adminUsers.getUser(user.id);
      setEditingUser(detail);
      setIsFormOpen(true);
    } catch (e: any) {
      toast.error('Failed to get user details');
    }
  };

  const handleDelete = (user: UserSummaryResponse) => {
    setSelectedUser(user);
    setIsDeleteOpen(true);
  };

  const handleToggleLock = (user: UserSummaryResponse) => {
    toggleLockMutation.mutate({ id: user.id, status: user.status });
  };

  const handleFormSubmit = async (formData: any) => {
    if (editingUser) {
      await updateMutation.mutateAsync({ id: editingUser.id, data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
  };

  // Columns
  const columns = [
    {
      key: 'user',
      header: 'User',
      render: (u: UserSummaryResponse) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center shrink-0">
            <User className="w-4 h-4 text-gray-500" />
          </div>
          <div>
            <div className="font-medium text-gray-900">{u.fullName}</div>
            <div className="text-xs text-gray-500">{u.email}</div>
          </div>
        </div>
      )
    },
    {
      key: 'roles',
      header: 'Roles',
      render: (u: UserSummaryResponse) => (
        <div className="flex flex-wrap gap-1">
          {u.roles?.map(r => (
            <Badge key={r} tone={r === 'ADMIN' ? 'indigo' : 'gray'}>{r}</Badge>
          ))}
        </div>
      )
    },
    {
      key: 'status',
      header: 'Status',
      render: (u: UserSummaryResponse) => {
        let tone: any = 'gray';
        let label = 'N/A';
        if (u.status === 'ACTIVE') { tone = 'emerald'; label = 'Active'; }
        if (u.status === 'LOCKED') { tone = 'red'; label = 'Locked'; }
        if (u.status === 'INACTIVE') { tone = 'gray'; label = 'Inactive'; }
        return <Badge tone={tone}>{label}</Badge>;
      }
    },
    {
      key: 'actions',
      header: '',
      render: (u: UserSummaryResponse) => (
        <div className="flex justify-end">
          <Dropdown
            align="right"
            items={[
              { id: 'edit', label: 'Edit', icon: <Edit2 />, onClick: () => handleEdit(u) },
              { 
                id: 'lock', 
                label: u.status === 'LOCKED' ? 'Unlock' : 'Lock', 
                icon: u.status === 'LOCKED' ? <Unlock /> : <Lock />, 
                onClick: () => handleToggleLock(u) 
              },
              { 
                id: 'reset', 
                label: 'Reset password', 
                icon: <KeyRound />, 
                onClick: async () => {
                  try {
                    await api.adminUsers.resetUserPassword(u.id);
                    toast.success('Reset password email sent successfully');
                  } catch (e: any) {
                    toast.error(e.message || 'Reset password email sent failed');
                  }
                } 
              },
              { id: 'div', label: '-' },
              { id: 'del', label: 'Delete user', icon: <Trash2 />, danger: true, onClick: () => handleDelete(u) },
            ]}
          />
        </div>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Manage users</h1>
          <p className="text-gray-500 mt-1">Manage the list of users in the system.</p>
        </div>
        <Button 
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => {
            setEditingUser(null);
            setIsFormOpen(true);
          }}
        >
          Add user
        </Button>
      </div>

      <Card>
        <CardHeader className="flex-col sm:flex-row gap-4 items-start sm:items-center py-4">
          <div className="w-full sm:max-w-sm">
            <SearchInput 
              placeholder="Search by email, name..."
              value={search}
              onSearch={(v) => { setSearch(v); setPage(1); }}
            />
          </div>
        </CardHeader>
        <div className="p-4 pt-0">
          <DataTable
            columns={columns}
            data={data?.content || []}
            keyExtractor={u => u.id}
            isLoading={isLoading}
          />
          <div className="mt-4 pt-4 border-t border-gray-500/10">
            <Pagination
              currentPage={page}
              totalPages={data?.totalPages || 1}
              onPageChange={setPage}
              pageSize={size}
              onPageSizeChange={(s) => { setSize(s); setPage(1); }}
            />
          </div>
        </div>
      </Card>

      <UserFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleFormSubmit}
        initialData={editingUser}
        isSaving={createMutation.isPending || updateMutation.isPending}
      />

      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        title="Delete user"
        description={`Are you sure you want to delete user "${selectedUser?.fullName}"? This action cannot be undone.`}
        danger
        confirmKeyword={selectedUser?.email}
        confirmText="Delete user"
        onConfirm={async () => {
          if (selectedUser) {
            await deleteMutation.mutateAsync(selectedUser.id);
          }
        }}
      />
    </div>
  );
};
