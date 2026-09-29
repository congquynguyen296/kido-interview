import type { ApiContracts } from './contracts';
import { httpApi } from './http';

export const api: ApiContracts = httpApi;

export * from './ApiError';
export * from './contracts';
