package ai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user this token belongs to
    @Column(nullable = false)
    private String email;

    // Hashed OTP or reset token
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    // SIGNUP_OTP or PASSWORD_RESET
    @Column(nullable = false)
    private String type;

    // Expiry time
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    // Has this token been used?
    @Column(nullable = false)
    private Boolean used = false;

    // Brute-force protection
    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private Boolean locked = false;

    // Resend protection
    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    // Audit
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
    
    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Boolean getUsed() {
        return used;
    }
    
    public void setUsed(Boolean used) {
        this.used = used;
    }

    public int getAttempts() {
        return attempts;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Boolean getLocked() {
        return locked;
    }
    
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public int getResendCount() {
        return resendCount;
    }
    
    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }

    public LocalDateTime getLastSentAt() {
        return lastSentAt;
    }
    
    public void setLastSentAt(LocalDateTime lastSentAt) {
        this.lastSentAt = lastSentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
