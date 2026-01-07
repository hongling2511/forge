#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

/**
 * Enumeration representing user roles in the system.
 * Used for role-based access control (RBAC).
 */
public enum Role {
    /**
     * Standard user role - default for newly registered users.
     */
    USER,

    /**
     * Administrative role - grants elevated permissions.
     */
    ADMIN
}
