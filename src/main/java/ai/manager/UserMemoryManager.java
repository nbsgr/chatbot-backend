package ai.manager;

import ai.model.UserAIMemory;
import ai.model.Users;
import ai.rep.UserAIMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class UserMemoryManager {

    private static final Logger log = LoggerFactory.getLogger(UserMemoryManager.class);

    @Autowired
    private UserAIMemoryRepository repo;

    // =====================================================
    // Save or Update Memory
    // =====================================================
    @Transactional
    public void saveOrUpdateMemory(Users user, String key, String value) {

        if (user == null || key == null || key.isBlank() ||
            value == null || value.isBlank()) {
            return;
        }

        String normalizedKey = key.trim().toLowerCase();
        String normalizedValue = value.trim();

        try {

            Optional<UserAIMemory> existing =
                    repo.findByUserAndMemoryKey(user, normalizedKey);

            UserAIMemory memory;

            if (existing.isPresent()) {
                memory = existing.get();
                memory.setMemoryValue(normalizedValue);
                log.info("🧠 Updating memory [{}] for user {}", normalizedKey, user.getEmail());
            } else {
                memory = new UserAIMemory();
                memory.setUser(user);
                memory.setMemoryKey(normalizedKey);
                memory.setMemoryValue(normalizedValue);
                log.info("🧠 Creating memory [{}] for user {}", normalizedKey, user.getEmail());
            }

            repo.save(memory);

        } catch (Exception e) {
            log.error("Memory save failed for user {} key {}",
                    user.getEmail(), normalizedKey, e);
        }
    }

    // =====================================================
    // Get All Memories
    // =====================================================
    public Map<String, String> getUserMemoryMap(Users user) {

        Map<String, String> memoryMap = new HashMap<>();

        if (user == null) return memoryMap;

        List<UserAIMemory> memories = repo.findByUser(user);

        for (UserAIMemory mem : memories) {
            memoryMap.put(mem.getMemoryKey(), mem.getMemoryValue());
        }

        return memoryMap;
    }

    // =====================================================
    // Get Single Memory
    // =====================================================
    public String getMemoryValue(Users user, String key) {

        if (user == null || key == null) return null;

        return repo.findByUserAndMemoryKey(user, key.trim().toLowerCase())
                .map(UserAIMemory::getMemoryValue)
                .orElse(null);
    }

    // =====================================================
    // Delete Specific Memory
    // =====================================================
    @Transactional
    public void deleteMemory(Users user, String key) {

        if (user == null || key == null) return;

        repo.deleteByUserAndMemoryKey(user, key.trim().toLowerCase());

        log.info("🗑 Memory [{}] deleted for user {}", key, user.getEmail());
    }

    // =====================================================
    // Clear All Memory
    // =====================================================
    @Transactional
    public void clearAllMemory(Users user) {

        if (user == null) return;

        repo.deleteByUser(user);

        log.info("🗑 All memories cleared for user {}", user.getEmail());
    }
}