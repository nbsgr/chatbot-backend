package ai.rep;

import ai.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByEmailAndTypeAndUsedFalse(String email, String type);

    List<UserToken> findAllByEmailAndType(String email, String type);

    // 🔥 FIXED DELETE
    @Modifying
    @Transactional
    void deleteAllByEmailAndType(String email, String type);
}
