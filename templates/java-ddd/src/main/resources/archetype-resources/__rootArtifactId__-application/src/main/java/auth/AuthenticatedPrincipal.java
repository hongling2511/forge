#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.util.Set;
import java.util.UUID;

/**
 * Represents the authenticated user principal.
 * This interface is implemented by the infrastructure layer's authentication filter.
 */
public interface AuthenticatedPrincipal {

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    UUID getId();

    /**
     * Gets the user email.
     *
     * @return the email
     */
    String getEmail();

    /**
     * Gets the user roles.
     *
     * @return set of role names
     */
    Set<String> getRoles();
}
