import type { UserProfileResponse } from './user';

export interface AuthenticationResponse {
  accessToken: string;
  expiresIn: number;
  user: UserProfileResponse;
}
