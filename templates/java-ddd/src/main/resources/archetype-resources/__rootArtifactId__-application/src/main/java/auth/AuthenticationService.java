#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user authentication operations.
 */
@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                  UserRepository userRepository,
                                  TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Authenticates a user and generates access/refresh tokens.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return authentication result with tokens
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthenticationResult authenticate(String email, String password) {
        logger.info("Authentication attempt for email: {}", email);

        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Load user from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (!user.isEnabled()) {
                throw new AccountDisabledException("Account is disabled");
            }

            // Generate tokens
            TokenService.TokenPair tokenPair = tokenService.generateTokens(user);

            logger.info("Authentication successful for user: {}", user.getId());

            return new AuthenticationResult(
                    tokenPair.accessToken(),
                    tokenPair.refreshToken(),
                    tokenService.getAccessTokenExpiration() / 1000, // Convert to seconds
                    user
            );

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email: {} - {}", email, e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new authentication result with fresh tokens
     */
    @Transactional
    public AuthenticationResult refreshToken(String refreshToken) {
        logger.info("Token refresh request");

        try {
            TokenService.TokenPair tokenPair = tokenService.refreshAccessToken(refreshToken);

            // Get user from new access token
            var userId = tokenService.getUserIdFromToken(tokenPair.accessToken());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenService.InvalidTokenException("User not found"));

            logger.info("Token refresh successful for user: {}", user.getId());

            return new AuthenticationResult(
                    tokenPair.accessToken(),
                    tokenPair.refreshToken(),
                    tokenService.getAccessTokenExpiration() / 1000,
                    user
            );

        } catch (TokenService.InvalidTokenException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     */
    @Transactional
    public void logout(String refreshToken) {
        logger.info("Logout request");
        tokenService.revokeRefreshToken(refreshToken);
        logger.info("Logout successful");
    }

    /**
     * Result of a successful authentication.
     */
    public record AuthenticationResult(
            String accessToken,
            String refreshToken,
            long expiresIn,
            User user
    ) {
    }

    /**
     * Exception thrown when account is disabled.
     */
    public static class AccountDisabledException extends RuntimeException {
        public AccountDisabledException(String message) {
            super(message);
        }
    }
}
