import { create } from 'zustand';
import type { UserProfileResponse } from '@/types/user';
import { api } from '@/api';
import { axiosClient } from '@/api/http/axiosClient';

interface AuthState {
  user: UserProfileResponse | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setAuth: (user: UserProfileResponse, accessToken: string) => void;
  setUser: (user: UserProfileResponse) => void;
  setAccessToken: (token: string) => void;
  logout: () => Promise<void>;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isAuthenticated: false,
  isLoading: true, // Initially loading while checking auth status

  setAuth: (user, accessToken) => {
    set({ user, accessToken, isAuthenticated: true });
  },

  setUser: (user) => {
    set({ user });
  },
  
  setAccessToken: (token) => {
    set({ accessToken: token, isAuthenticated: true });
  },

  logout: async () => {
    try {
      await api.auth.logout();
    } catch (e) {
      console.error('Logout failed', e);
    }
    set({ user: null, accessToken: null, isAuthenticated: false });
  },

  initialize: async () => {
    set({ isLoading: true });
    try {
      // Silently try to refresh token on initial load
      try {
        const response = await axiosClient.post('/auth/refresh', {}, { withCredentials: true, _retry: true } as any);
        const { accessToken } = response as any; // interceptor unwrap
        set({ accessToken, isAuthenticated: true });
      } catch (err) {
        // No valid session
        set({ isAuthenticated: false, isLoading: false });
        return;
      }

      // Fetch user profile if refresh succeeded
      const user = await api.user.getProfile();
      set({ user, isLoading: false });
    } catch (error) {
      console.error('Failed to initialize auth', error);
      set({ user: null, accessToken: null, isAuthenticated: false, isLoading: false });
    }
  },
}));
