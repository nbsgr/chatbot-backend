package ai.manager;

import ai.dto.ApiResponse;
import ai.model.ChatMessage;
import ai.model.Conversation;
import ai.model.Users;
import ai.rep.ChatMessageRepository;
import ai.rep.ConversationRepository;
import ai.rep.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageManager {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private JWTManager JWT;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ============================
    // REST: Send user message
    // ============================
    public ApiResponse<ChatMessage> sendUserMessage(
            Long conversationId,
            String content,
            String token) {

        String email = JWT.getEmailFromToken(token);

        if (email == null)
            return new ApiResponse<>(401,
                    "Invalid or expired token", null);

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Conversation conversation =
                conversationRepository
                        .findByIdAndUserAndDeletedFalse(
                                conversationId,
                                user
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Conversation not found"));

        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversation(conversation);
        userMessage.setSenderType("USER");
        userMessage.setContent(content);
        userMessage.setSentAt(LocalDateTime.now());

        // ✅ USER messages have no sources
        userMessage.setSources(null);

        chatMessageRepository.save(userMessage);
        conversationManager.touchConversation(conversation);

        return new ApiResponse<>(
                200,
                "Message sent",
                userMessage
        );
    }

    // ============================
    // WS: Save USER message
    // ============================
    public ChatMessage saveUserMessageInternal(
            Conversation conversation,
            String content) {

        ChatMessage message = new ChatMessage();

        message.setConversation(conversation);
        message.setSenderType("USER");
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        message.setSources(null);

        chatMessageRepository.save(message);
        conversationManager.touchConversation(conversation);

        return message;
    }

    // ============================
    // WS: Save BOT message
    // ============================
    public ChatMessage saveBotMessageInternal(
            Conversation conversation,
            String content,
            List<String> sources) {

        ChatMessage message = new ChatMessage();

        message.setConversation(conversation);
        message.setSenderType("BOT");
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        try {

            // ✅ store only if sources exist
            if (sources != null && !sources.isEmpty()) {
                message.setSources(
                        objectMapper.writeValueAsString(sources)
                );
            } else {
                message.setSources(null);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to serialize sources",
                    e
            );
        }

        chatMessageRepository.save(message);
        conversationManager.touchConversation(conversation);

        return message;
    }

    // ============================
    // REST: Get messages
    // ============================
    public ApiResponse<List<ChatMessage>> getMessages(
            Long conversationId,
            String token) {

        String email = JWT.getEmailFromToken(token);

        if (email == null)
            return new ApiResponse<>(401,
                    "Invalid or expired token", null);

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Conversation conversation =
                conversationRepository
                        .findByIdAndUserAndDeletedFalse(
                                conversationId,
                                user
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Conversation not found"));

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByConversationOrderBySentAtAsc(
                                conversation
                        );

        return new ApiResponse<>(
                200,
                "Success",
                messages
        );
    }

    // ============================
    // WS INTERNAL FETCH
    // ============================
    public List<ChatMessage> getMessagesInternal(
            Conversation conversation) {

        return chatMessageRepository
                .findByConversationOrderBySentAtAsc(
                        conversation
                );
    }
}