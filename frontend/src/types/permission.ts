export interface PermissionResponse {
  id: string;
  code: string; // e.g. "users:read"
  resource: string; // e.g. "users", "roles", "reports"
  action: string; // e.g. "read", "write", "delete", "approve"
  description?: string;
  module: string; // Used to group permissions, e.g. "User Management", "System Settings"
}
