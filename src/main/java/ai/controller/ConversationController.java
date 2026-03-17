package ai.controller;

import ai.dto.ApiResponse;
import ai.manager.ConversationManager;
import ai.model.Conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationManager CM;

    // =========================
    // Create new conversation
    // =========================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Conversation>> createConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> data
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        String title = (data != null) ? data.get("title") : null;

        ApiResponse<Conversation> response = CM.createConversation(token, title);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // Get all conversations
    // =========================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Conversation>>> getUserConversations(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        ApiResponse<List<Conversation>> response = CM.getUserConversations(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // Get single conversation
    // =========================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Conversation>> getConversation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        ApiResponse<Conversation> response = CM.getConversation(id, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // Rename
    // =========================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PutMapping("/rename/{id}")
    public ResponseEntity<ApiResponse<Void>> renameConversation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> data
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        String title = data.get("title");

        ApiResponse<Void> response = CM.renameConversation(id, title, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // Soft delete
    // =========================
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "").trim();
        ApiResponse<Void> response = CM.deleteConversation(id, token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
