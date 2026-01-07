#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public AuthController(UserRegistrationService registrationService,
                          AuthenticationService authenticationService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration request
     * @return the created user details
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration request received for email: {}", request.email());

        UserRegistrationService.RegisterCommand command = new UserRegistrationService.RegisterCommand(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );

        User user = registrationService.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegisterResponse.from(user));
    }

    /**
     * Authenticates a user and returns access/refresh tokens.
     *
     * @param request the login request
     * @return login response with tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.email());

        AuthenticationService.AuthenticationResult result =
                authenticationService.authenticate(request.email(), request.password());

        return ResponseEntity.ok(LoginResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.user()
        ));
    }

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        logger.info("Token refresh request received");

        AuthenticationService.AuthenticationResult result =
                authenticationService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(LoginResponse.of(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.user()
        ));
    }

    /**
     * Logs out the current user by revoking their refresh token.
     *
     * @param request the logout request containing the refresh token
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutRequest request) {
        logger.info("Logout request received");

        authenticationService.logout(request.refreshToken());

        return ResponseEntity.ok("Successfully logged out");
    }
}
