package ai.rep;

import ai.model.ChatMessage;
import ai.model.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageSourceRepository extends JpaRepository<MessageSource, Long> {

    // Get all sources for one message
    List<MessageSource> findByChatMessage(ChatMessage chatMessage);
}
