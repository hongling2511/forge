#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for token management operations.
 * Coordinates between domain entities and infrastructure services.
 */
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public TokenService(TokenProvider tokenProvider,
                        RefreshTokenRepository refreshTokenRepository,
                        UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generates access and refresh tokens for a user.
     *
     * @param user the authenticated user
     * @return token pair containing access and refresh tokens
     */
    @Transactional
    public TokenPair generateTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);

        // Create and persist refresh token
        RefreshToken refreshToken = RefreshToken.create(
                user,
                tokenProvider.generateRefreshTokenValue(),
                tokenProvider.getRefreshTokenExpiration()
        );
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, refreshToken.getToken());
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshTokenValue the refresh token string
     * @return new token pair if refresh token is valid
     * @throws InvalidTokenException if refresh token is invalid or expired
     */
    @Transactional
    public TokenPair refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        User user = refreshToken.getUser();

        // Revoke old refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // Generate new token pair
        return generateTokens(user);
    }

    /**
     * Validates an access token.
     *
     * @param token the access token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        return tokenProvider.validateToken(token);
    }

    /**
     * Extracts user ID from access token.
     *
     * @param token the access token
     * @return user ID
     */
    public UUID getUserIdFromToken(String token) {
        return tokenProvider.getUserIdFromToken(token);
    }

    /**
     * Gets the access token expiration in milliseconds.
     *
     * @return expiration in milliseconds
     */
    public long getAccessTokenExpiration() {
        return tokenProvider.getAccessTokenExpiration();
    }

    /**
     * Revokes all refresh tokens for a user.
     *
     * @param userId the user ID
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param refreshTokenValue the refresh token to revoke
     */
    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Token pair containing access and refresh tokens.
     */
    public record TokenPair(String accessToken, String refreshToken) {
    }

    /**
     * Exception thrown when a token is invalid.
     */
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
