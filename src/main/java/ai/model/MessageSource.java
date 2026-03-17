package ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "message_sources")
public class MessageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each source belongs to ONE bot message
    @ManyToOne
    @JoinColumn(name = "chat_message_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(name = "source_url", length = 1000, nullable = false)
    private String sourceUrl;

    // ===== Getters & Setters =====

    public Long getId() { return id; }

    public ChatMessage getChatMessage() { return chatMessage; }
    public void setChatMessage(ChatMessage chatMessage) { this.chatMessage = chatMessage; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
