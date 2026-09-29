export type EntityStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'PENDING_VERIFICATION' | 'DELETED';
export type AuthProvider = 'LOCAL' | 'GOOGLE';

export interface UserProfileResponse {
  id: string;
  email: string;
  username?: string;
  fullName: string;
  phoneNumber?: string;
  authProvider: AuthProvider;
  emailVerified: boolean;
  roles: string[];
  permissions: string[];
  createdAt: string;
  lastLoginAt?: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  username?: string;
  phoneNumber?: string;
}
