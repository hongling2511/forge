#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity (domain port).
 * Implementation provided by infrastructure layer.
 */
public interface UserRepository {

    /**
     * Save a user entity.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Find all users.
     *
     * @return list of all users
     */
    java.util.List<User> findAll();

    /**
     * Find a user by their unique identifier.
     *
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Find a user by their email address.
     *
     * @param email the email address
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by their username.
     *
     * @param username the username
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists with the given email.
     *
     * @param email the email address
     * @return true if a user exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Check if a user exists with the given username.
     *
     * @param username the username
     * @return true if a user exists with this username
     */
    boolean existsByUsername(String username);

    /**
     * Delete a user by their ID.
     *
     * @param id the user ID
     */
    void deleteById(UUID id);
}
