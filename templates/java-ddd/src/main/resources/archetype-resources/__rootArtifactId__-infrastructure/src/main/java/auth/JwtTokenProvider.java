#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT token provider for creating and validating JWT tokens.
 * Implements the TokenProvider interface from application layer.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${symbol_dollar}{jwt.secret}") String secret,
            @Value("${symbol_dollar}{jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${symbol_dollar}{jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${symbol_dollar}{jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    /**
     * Generate an access token for a user.
     *
     * @param userId the user ID
     * @param email the user's email
     * @param roles the user's roles
     * @return the generated JWT access token
     */
    public String generateAccessToken(UUID userId, String email, Set<Role> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpiration);

        String rolesString = roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", rolesString)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generate an access token for a User entity.
     *
     * @param user the user entity
     * @return the generated JWT access token
     */
    public String generateAccessToken(User user) {
        return generateAccessToken(user.getId(), user.getEmail(), user.getRoles());
    }

    /**
     * Generate a refresh token string value.
     *
     * @return a random refresh token string
     */
    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get the refresh token expiration time.
     *
     * @return instant when refresh token expires
     */
    public Instant getRefreshTokenExpiry() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }

    /**
     * Get the refresh token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /**
     * Get the access token expiration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get the access token expiration in seconds.
     *
     * @return expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract the email from a JWT token.
     *
     * @param token the JWT token
     * @return the email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract roles from a JWT token.
     *
     * @param token the JWT token
     * @return set of roles
     */
    public Set<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        String rolesString = claims.get("roles", String.class);
        if (rolesString == null || rolesString.isEmpty()) {
            return Set.of();
        }
        return Set.of(rolesString.split(","));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
