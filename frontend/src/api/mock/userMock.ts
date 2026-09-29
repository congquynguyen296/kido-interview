import type { ApiContracts } from '../contracts';
import { MockUsers, delay, getUserPermissions } from './db';
import { ApiError } from '../ApiError';
import { HTTP_CODE } from '@/constants/httpCode';

// A simple mock way to determine current user
const getCurrentUser = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) throw new ApiError(HTTP_CODE.UNAUTHENTICATED, 'Chưa xác thực');

  // mock-token-user_1 -> user_1
  const id = token.replace('mock-token-', '');
  const user = MockUsers.find(u => u.id === id);
  if (!user) {
    // Fallback for demo
    const fallback = MockUsers[0];
    return {
      ...fallback,
      permissions: getUserPermissions(fallback.roles)
    };
  }

  return {
    ...user,
    permissions: getUserPermissions(user.roles)
  };
};

export const userMock: ApiContracts['user'] = {
  getProfile: async () => {
    await delay(500);
    return getCurrentUser();
  },

  updateProfile: async (data) => {
    await delay(800);
    const currentUser = getCurrentUser();
    return {
      ...currentUser,
      ...data
    };
  },

  updateAvatar: async (data) => {
    await delay(800);
  },

  changePassword: async (data) => {
    await delay(800);
    if (data.oldPassword !== '123456') {
      throw new ApiError(HTTP_CODE.OLD_PASSWORD_INCORRECT, 'Mật khẩu cũ không chính xác');
    }
  }
};
