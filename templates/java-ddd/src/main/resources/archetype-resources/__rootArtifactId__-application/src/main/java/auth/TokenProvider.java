#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.util.Set;
import java.util.UUID;

/**
 * Port interface for JWT token operations.
 * Implementation provided by the infrastructure layer.
 */
public interface TokenProvider {

    /**
     * Generates an access token for a user.
     *
     * @param user the user
     * @return the access token
     */
    String generateAccessToken(User user);

    /**
     * Generates a refresh token string value.
     *
     * @return the refresh token value
     */
    String generateRefreshTokenValue();

    /**
     * Gets the refresh token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    long getRefreshTokenExpiration();

    /**
     * Gets the access token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    long getAccessTokenExpiration();

    /**
     * Validates an access token.
     *
     * @param token the token to validate
     * @return true if valid
     */
    boolean validateToken(String token);

    /**
     * Extracts user ID from token.
     *
     * @param token the token
     * @return the user ID
     */
    UUID getUserIdFromToken(String token);

    /**
     * Extracts email from token.
     *
     * @param token the token
     * @return the email
     */
    String getEmailFromToken(String token);

    /**
     * Extracts roles from token.
     *
     * @param token the token
     * @return set of role names
     */
    Set<String> getRolesFromToken(String token);
}
