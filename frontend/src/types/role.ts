export interface RoleResponse {
  id: string;
  name: string; // e.g. ADMIN, USER, MODERATOR
  description?: string;
  permissions: string[]; // List of permission codes, e.g. "users:read", "users:write"
  isSystem: boolean; // System roles cannot be deleted or modified
  userCount: number;
  createdAt: string;
}

export interface CreateRoleRequest {
  name: string;
  description?: string;
  permissions: string[];
}

export interface UpdateRoleRequest {
  description?: string;
  permissions: string[];
}
