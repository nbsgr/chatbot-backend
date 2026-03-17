package ai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_ai_memory",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_email", "memory_key"})
)
public class UserAIMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Memory belongs to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    private Users user;

    // Type of memory (nickname, full_name, preference_topic, etc.)
    @Column(name = "memory_key", nullable = false, length = 100)
    private String memoryKey;

    // Actual stored value
    @Column(name = "memory_value", nullable = false, length = 1000)
    private String memoryValue;

    // When this memory was last updated
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===== Lifecycle Hooks =====
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getMemoryKey() {
        return memoryKey;
    }

    public void setMemoryKey(String memoryKey) {
        this.memoryKey = memoryKey;
    }

    public String getMemoryValue() {
        return memoryValue;
    }

    public void setMemoryValue(String memoryValue) {
        this.memoryValue = memoryValue;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
