package ai.manager;

import ai.dto.ApiResponse;
import ai.model.Conversation;
import ai.model.Users;
import ai.rep.ConversationRepository;
import ai.rep.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationManager {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JWTManager JWT;

    // =========================
    // Create new conversation
    // =========================
    public ApiResponse<Conversation> createConversation(String token, String title) {

        String email = JWT.getEmailFromToken(token);
        if (email == null) return new ApiResponse<>(401, "Invalid token", null);

        Users user = usersRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return new ApiResponse<>(404, "User not found", null);

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(title == null || title.isBlank() ? "New chat" : title);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastActiveAt(LocalDateTime.now());
        conversation.setDeleted(false);

        conversationRepository.save(conversation);

        return new ApiResponse<>(200, "Conversation created", conversation);
    }

    // =========================
    // Get all conversations
    // =========================
    public ApiResponse<List<Conversation>> getUserConversations(String token) {

        String email = JWT.getEmailFromToken(token);
        if (email == null) return new ApiResponse<>(401, "Invalid token", null);

        Users user = usersRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return new ApiResponse<>(404, "User not found", null);

        List<Conversation> conversations =
                conversationRepository.findByUserAndDeletedFalseOrderByLastActiveAtDesc(user);

        return new ApiResponse<>(200, "Success", conversations);
    }

    // =========================
    // Get single conversation
    // =========================
    public ApiResponse<Conversation> getConversation(Long id, String token) {

        String email = JWT.getEmailFromToken(token);
        if (email == null) return new ApiResponse<>(401, "Invalid token", null);

        Users user = usersRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return new ApiResponse<>(404, "User not found", null);

        Conversation conversation =
                conversationRepository.findByIdAndUserAndDeletedFalse(id, user)
                        .orElse(null);

        if (conversation == null)
            return new ApiResponse<>(404, "Conversation not found", null);

        return new ApiResponse<>(200, "Success", conversation);
    }

    // =========================
    // Rename
    // =========================
    public ApiResponse<Void> renameConversation(Long id, String newTitle, String token) {

        String email = JWT.getEmailFromToken(token);
        if (email == null) return new ApiResponse<>(401, "Invalid token", null);

        Users user = usersRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return new ApiResponse<>(404, "User not found", null);

        Conversation conversation =
                conversationRepository.findByIdAndUserAndDeletedFalse(id, user)
                        .orElse(null);

        if (conversation == null)
            return new ApiResponse<>(404, "Conversation not found", null);

        conversation.setTitle(newTitle == null || newTitle.isBlank() ? "Untitled" : newTitle);
        conversation.setLastActiveAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        return new ApiResponse<>(200, "Renamed", null);
    }

    // =========================
    // Soft delete
    // =========================
    public ApiResponse<Void> deleteConversation(Long id, String token) {

        String email = JWT.getEmailFromToken(token);
        if (email == null) return new ApiResponse<>(401, "Invalid token", null);

        Users user = usersRepository.findByEmail(email)
                .orElse(null);

        if (user == null) return new ApiResponse<>(404, "User not found", null);

        Conversation conversation =
                conversationRepository.findByIdAndUserAndDeletedFalse(id, user)
                        .orElse(null);

        if (conversation == null)
            return new ApiResponse<>(404, "Conversation not found", null);

        conversation.setDeleted(true);
        conversation.setDeletedAt(LocalDateTime.now());

        conversationRepository.save(conversation);

        return new ApiResponse<>(200, "Deleted", null);
    }

    // =========================
    // Touch (on message)
    // =========================
    public void touchConversation(Conversation conversation) {
        conversation.setLastActiveAt(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
}
