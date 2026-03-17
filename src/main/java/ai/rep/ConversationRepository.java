package ai.rep;

import ai.model.Conversation;
import ai.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Recent first
    List<Conversation> findByUserAndDeletedFalseOrderByLastActiveAtDesc(Users user);

    // Ownership + soft delete
    Optional<Conversation> findByIdAndUserAndDeletedFalse(Long id, Users user);
}
