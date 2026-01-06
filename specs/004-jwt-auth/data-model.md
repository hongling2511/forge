# Data Model: JWT Authentication

**Feature**: 004-jwt-auth
**Date**: 2026-01-05
**Status**: Complete

## Entity Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        User                                  │
├─────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                               │
│ username: String (unique, not null)                         │
│ email: String (unique, not null)                            │
│ passwordHash: String (not null)                             │
│ roles: Set<Role>                                            │
│ enabled: Boolean                                            │
│ createdAt: Timestamp                                        │
│ updatedAt: Timestamp                                        │
└─────────────────────────────────────────────────────────────┘
                             │
                             │ 1:N
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     RefreshToken                             │
├─────────────────────────────────────────────────────────────┤
│ id: UUID (PK)                                               │
│ token: String (unique, not null)                            │
│ userId: UUID (FK → User.id)                                 │
│ expiresAt: Timestamp                                        │
│ revoked: Boolean                                            │
│ createdAt: Timestamp                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## Entity: User

Represents an authenticated user in the system.

### Attributes

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Unique identifier |
| username | String | Unique, Not Null, 3-50 chars | Display name |
| email | String | Unique, Not Null, valid email format | Login credential |
| passwordHash | String | Not Null | BCrypt-hashed password |
| roles | Set\<Role\> | Not Null, default [USER] | User permissions |
| enabled | Boolean | Not Null, default true | Account active status |
| createdAt | Timestamp | Not Null, auto-set | Creation timestamp |
| updatedAt | Timestamp | Auto-updated | Last modification |

### Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| username | 3-50 characters, alphanumeric + underscore | "Username must be 3-50 characters" |
| email | Valid email format | "Invalid email format" |
| email | Unique in system | "Email already registered" |
| password | 8+ characters, at least 1 letter and 1 number | "Password must be at least 8 characters with letters and numbers" |

### Relationships

| Relationship | Type | Target | Description |
|--------------|------|--------|-------------|
| refreshTokens | One-to-Many | RefreshToken | User's active refresh tokens |

---

## Entity: RefreshToken

Represents a long-lived token for obtaining new access tokens.

### Attributes

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Unique identifier |
| token | String | Unique, Not Null | Token value (opaque string) |
| userId | UUID | FK → User.id, Not Null | Owner user |
| expiresAt | Timestamp | Not Null | Expiration time |
| revoked | Boolean | Not Null, default false | Token invalidation flag |
| createdAt | Timestamp | Not Null, auto-set | Creation timestamp |

### Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| token | Not empty | "Token required" |
| expiresAt | Future date | "Expiration must be in future" |

### Relationships

| Relationship | Type | Target | Description |
|--------------|------|--------|-------------|
| user | Many-to-One | User | Token owner |

### State Transitions

```
[Created] ──(time passes)──► [Expired]
    │
    │ (logout/revoke)
    ▼
[Revoked]
```

| State | revoked | expiresAt | Valid for refresh |
|-------|---------|-----------|-------------------|
| Active | false | future | Yes |
| Revoked | true | any | No |
| Expired | any | past | No |

---

## Enum: Role

Represents permission levels in the system.

### Values

| Value | Description | Default |
|-------|-------------|---------|
| USER | Standard user access | Yes (on registration) |
| ADMIN | Administrative access | No |

### Usage

- New users receive USER role by default
- ADMIN role assigned manually (no self-registration as admin)
- Roles stored as comma-separated string in database (simple approach)

---

## Value Object: TokenPair

Represents the access/refresh token pair returned on authentication.

### Attributes

| Field | Type | Description |
|-------|------|-------------|
| accessToken | String | JWT access token |
| refreshToken | String | Opaque refresh token |
| accessTokenExpiresAt | Timestamp | Access token expiration |
| tokenType | String | Always "Bearer" |

---

## Indexes

### User Table

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| idx_user_email | email | Unique | Login lookup |
| idx_user_username | username | Unique | Username validation |

### RefreshToken Table

| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| idx_refresh_token | token | Unique | Token lookup |
| idx_refresh_user | userId | Non-unique | User's tokens lookup |

---

## Database Schema (DDL)

```sql
-- User table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- Refresh token table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
```

---

## JPA Entity Mapping Notes

### User Entity
- Use `@Entity` with `@Table(name = "users")`
- `@Id` with `@GeneratedValue(strategy = GenerationType.UUID)`
- `@Column(unique = true)` for email and username
- Roles as `@ElementCollection` with `@Enumerated(EnumType.STRING)` OR simple String with conversion

### RefreshToken Entity
- `@ManyToOne` relationship to User
- `@JoinColumn(name = "user_id")`
- Cascade delete when user is deleted
