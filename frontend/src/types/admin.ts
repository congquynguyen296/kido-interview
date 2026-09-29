import type { EntityStatus, AuthProvider } from './user';

export interface UserSummaryResponse {
  id: string;
  email: string;
  username?: string;
  fullName: string;
  authProvider: AuthProvider;
  status: EntityStatus;
  roles: string[];
  createdAt: string;
  lastLoginAt?: string;
}

export interface UserDetailResponse extends UserSummaryResponse {
  phoneNumber?: string;
  emailVerified: boolean;
}

export interface UserQueryRequest {
  search?: string;
  status?: EntityStatus;
  role?: string;
  page: number;
  size: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface CreateUserRequest {
  email: string;
  fullName: string;
  password?: string; // Optional if created via admin
  roles: string[];
  status: EntityStatus;
}

export interface UpdateUserRequest {
  fullName?: string;
  roles?: string[];
  status?: EntityStatus;
}

export interface AdminUserStatisticsResponse {
  totalUsers: number;
  usersByStatus: Record<string, number>;
  usersByProvider: Record<string, number>;
  newUsersLast7Days: number;
  newUsersLast30Days: number;
}
