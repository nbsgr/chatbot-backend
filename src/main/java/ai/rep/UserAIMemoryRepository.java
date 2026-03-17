package ai.rep;

import ai.model.UserAIMemory;
import ai.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAIMemoryRepository extends JpaRepository<UserAIMemory, Long> {

    // All memories for a user
    List<UserAIMemory> findByUser(Users user);

    // Specific memory like nickname
    Optional<UserAIMemory> findByUserAndMemoryKey(Users user, String memoryKey);

    // Check if memory key exists
    boolean existsByUserAndMemoryKey(Users user, String memoryKey);

    // Delete one memory (e.g. "don't call me that")
    @Transactional
    void deleteByUserAndMemoryKey(Users user, String memoryKey);

    // Delete all memory (account delete)
    @Transactional
    void deleteByUser(Users user);
}
