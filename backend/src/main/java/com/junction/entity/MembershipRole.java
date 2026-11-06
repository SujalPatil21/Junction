package com.junction.entity;

/**
 * Scoped restaurant access roles.
 * Aligned with PostgreSQL enum `membership_role` and TypeScript `membershipRoleEnum`.
 */
public enum MembershipRole {
    OWNER,
    MANAGER,
    STAFF
}

