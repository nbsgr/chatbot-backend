package ai.rep;

import ai.model.AiExecution;
import ai.model.Conversation;
import ai.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiExecutionRepository extends JpaRepository<AiExecution, Long> {

    // Get executions for a conversation (latest first)
    Page<AiExecution> findByConversationOrderByCreatedAtDesc(
            Conversation conversation,
            Pageable pageable
    );

    // Get executions for a user (latest first)
    Page<AiExecution> findByUserOrderByCreatedAtDesc(
            Users user,
            Pageable pageable
    );
}