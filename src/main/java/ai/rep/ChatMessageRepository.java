package ai.rep;

import ai.model.ChatMessage;
import ai.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderBySentAtAsc(Conversation conversation);
    List<ChatMessage> findTop10ByConversationOrderBySentAtDesc(Conversation conversation);

}
