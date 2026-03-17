package ai.rep;

import ai.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
}
