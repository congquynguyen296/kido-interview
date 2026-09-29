import type { AuthenticationResponse } from '@/types/auth';
import type { UserProfileResponse, UpdateProfileRequest } from '@/types/user';
import type { UserSummaryResponse, UserDetailResponse, UserQueryRequest, CreateUserRequest, UpdateUserRequest, AdminUserStatisticsResponse } from '@/types/admin';
import type { PageResponse } from '@/types/api';

export interface ApiContracts {
  // === Auth Module ===
  auth: {
    login(data: any): Promise<AuthenticationResponse>;
    register(data: any): Promise<void>;
    verifyEmail(data: { email: string; token: string }): Promise<void>;
    resendVerification(email: string): Promise<void>;
    forgotPassword(email: string): Promise<void>;
    verifyResetOtp(data: any): Promise<{ resetToken: string }>;
    resetPassword(data: any): Promise<void>;
    logout(): Promise<void>;
    loginWithGoogle(token: string): Promise<AuthenticationResponse>;
  };

  // === User Module (Self-Service) ===
  user: {
    getProfile(): Promise<UserProfileResponse>;
    updateProfile(data: UpdateProfileRequest): Promise<UserProfileResponse>;
    changePassword(data: any): Promise<void>;
  };

  // === Admin Users Module ===
  adminUsers: {
    getUsers(query: UserQueryRequest): Promise<PageResponse<UserSummaryResponse>>;
    getUser(id: string): Promise<UserDetailResponse>;
    createUser(data: CreateUserRequest): Promise<UserDetailResponse>;
    updateUser(id: string, data: UpdateUserRequest): Promise<UserDetailResponse>;
    deleteUser(id: string): Promise<void>;
    lockUser(id: string, reason?: string): Promise<void>;
    unlockUser(id: string): Promise<void>;
    resetUserPassword(id: string, newPassword?: string): Promise<{ newPassword?: string }>;
    getStatistics(): Promise<AdminUserStatisticsResponse>;
  };
}
