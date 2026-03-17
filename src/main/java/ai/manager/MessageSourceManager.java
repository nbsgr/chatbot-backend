package ai.manager;

import ai.model.ChatMessage;
import ai.model.MessageSource;
import ai.rep.MessageSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageSourceManager {

    @Autowired private MessageSourceRepository repo;

    public void saveSources(ChatMessage message, List<String> urls) {
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;

            MessageSource src = new MessageSource();
            src.setChatMessage(message);
            src.setSourceUrl(url.trim());
            repo.save(src);
        }
    }
}
