import type { ApiContracts } from '../contracts';
import { axiosClient } from './axiosClient';

export const httpApi: ApiContracts = {
  auth: {
    login: (data) => axiosClient.post('/auth/login', data),
    register: (data) => axiosClient.post('/auth/register', data),
    verifyEmail: (data) => axiosClient.post('/auth/verify-email', data),
    resendVerification: (email) => axiosClient.post('/auth/resend-verification', { email }),
    forgotPassword: (email) => axiosClient.post('/auth/forgot-password', { email }),
    verifyResetOtp: (data) => axiosClient.post('/auth/verify-reset-otp', data),
    resetPassword: (data) => axiosClient.post('/auth/reset-password', data),
    logout: () => axiosClient.post('/auth/logout'),
    loginWithGoogle: (token) => axiosClient.post('/auth/google', { token }),
  },
  
  user: {
    getProfile: () => axiosClient.get('/users/me'),
    updateProfile: (data) => axiosClient.put('/users/me', data),
    changePassword: (data) => axiosClient.patch('/users/me/password', data),
  },

  adminUsers: {
    getUsers: (query) => axiosClient.get('/admin/users', { params: query }),
    getUser: (id) => axiosClient.get(`/admin/users/${id}`),
    createUser: (data) => axiosClient.post('/admin/users', data),
    updateUser: (id, data) => axiosClient.put(`/admin/users/${id}`, data),
    deleteUser: (id) => axiosClient.delete(`/admin/users/${id}`),
    lockUser: (id, reason) => axiosClient.patch(`/admin/users/${id}/status`, { status: 'LOCKED', reason }),
    unlockUser: (id) => axiosClient.patch(`/admin/users/${id}/status`, { status: 'ACTIVE' }),
    resetUserPassword: (id, newPassword) => axiosClient.post(`/admin/users/${id}/reset-password`, { newPassword }),
    getStatistics: () => axiosClient.get('/admin/users/statistics'),
  }
};
