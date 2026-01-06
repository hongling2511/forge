#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID>, UserRepository {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findByUsername(String username);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByUsername(String username);
}
