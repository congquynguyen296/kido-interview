CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    created_by VARCHAR(255),
    modified_at TIMESTAMP(6) WITH TIME ZONE,
    modified_by VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255),
    phone_number VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(255),
    auth_provider VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP(6) WITH TIME ZONE,
    password_changed_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    created_by VARCHAR(255),
    modified_at TIMESTAMP(6) WITH TIME ZONE,
    modified_by VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    system_role BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    created_by VARCHAR(255),
    modified_at TIMESTAMP(6) WITH TIME ZONE,
    modified_by VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(255),
    action VARCHAR(255)
);

CREATE TABLE token_validation (
    jti UUID PRIMARY KEY,
    token_type VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    reason VARCHAR(255)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_auth_provider_id ON users (auth_provider, provider_id);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_token_validation_user_id ON token_validation (user_id);
CREATE INDEX idx_token_validation_expires_at ON token_validation (expires_at);
