package ai.controller;

import ai.dto.ApiResponse;
import ai.manager.ChatMessageManager;
import ai.manager.ChatStreamManager;
import ai.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageManager CMM;

    @Autowired
    private ChatStreamManager chatStreamManager;

    // ============================
    // REST: Send USER message
    // ============================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessage>> sendUserMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> data
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long conversationId = Long.parseLong(data.get("conversationId"));
        String content = data.get("content");

        ApiResponse<ChatMessage> response =
                CMM.sendUserMessage(conversationId, content, token);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ============================
    // REST: Load messages
    // ============================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> data
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        Long conversationId = Long.parseLong(data.get("conversationId"));

        ApiResponse<List<ChatMessage>> response =
                CMM.getMessages(conversationId, token);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // ============================
    // WEBSOCKET: Stream bot reply
    // ============================
    @MessageMapping("/chat.send")
    public void streamChat(
            Principal principal,
            Map<String, String> data) {

        if (principal == null) {
            System.out.println("WebSocket Principal is NULL");
            return;
        }

        String email = principal.getName();

        Long conversationId =
                Long.parseLong(data.get("conversationId"));

        String content = data.get("content");

        chatStreamManager.streamBotReply(
                conversationId,
                content,
                email
        );
    }
}