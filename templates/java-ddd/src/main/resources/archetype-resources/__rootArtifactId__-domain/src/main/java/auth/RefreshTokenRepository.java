#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity (domain port).
 * Implementation provided by infrastructure layer.
 */
public interface RefreshTokenRepository {

    /**
     * Save a refresh token entity.
     *
     * @param refreshToken the refresh token to save
     * @return the saved refresh token
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Find a refresh token by its token string.
     *
     * @param token the token string
     * @return an Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user.
     *
     * @param userId the user ID
     * @return list of refresh tokens for the user
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * Find all valid (not revoked and not expired) refresh tokens for a user.
     *
     * @param userId the user ID
     * @return list of valid refresh tokens for the user
     */
    List<RefreshToken> findValidTokensByUserId(UUID userId);

    /**
     * Delete a refresh token by its ID.
     *
     * @param id the refresh token ID
     */
    void deleteById(UUID id);

    /**
     * Delete all refresh tokens for a user.
     *
     * @param userId the user ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Revoke all refresh tokens for a user.
     *
     * @param userId the user ID
     */
    void revokeAllByUserId(UUID userId);
}
