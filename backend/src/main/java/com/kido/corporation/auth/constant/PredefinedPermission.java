package com.kido.corporation.auth.constant;

import java.util.List;
import java.util.Map;

public class PredefinedPermission {
    public static final String USER_READ = "USER_READ";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_MANAGE_STATUS = "USER_MANAGE_STATUS";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String PERMISSION_CREATE = "PERMISSION_CREATE";
    public static final String PERMISSION_UPDATE = "PERMISSION_UPDATE";
    public static final String PERMISSION_DELETE = "PERMISSION_DELETE";
    public static final String SESSION_REVOKE = "SESSION_REVOKE";

    public static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
            PredefinedRole.ADMIN, List.of(
                    USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE, USER_MANAGE_STATUS, USER_ASSIGN_ROLE,
                    ROLE_READ, ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE,
                    PERMISSION_READ, PERMISSION_CREATE, PERMISSION_UPDATE, PERMISSION_DELETE,
                    SESSION_REVOKE
            ),
            PredefinedRole.USER, List.of(),
            PredefinedRole.MODERATOR, List.of(USER_READ, USER_MANAGE_STATUS)
    );
}
