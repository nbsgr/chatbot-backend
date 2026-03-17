package ai.manager;

import ai.dto.StreamResponse;
import ai.model.AiExecution;
import ai.model.Conversation;
import ai.model.Users;
import ai.rep.AiExecutionRepository;
import ai.rep.ConversationRepository;
import ai.rep.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class ChatStreamManager {

    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private ConversationRepository conversationRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private ChatMessageManager chatMessageManager;
    @Autowired private AiExecutionRepository aiExecutionRepository;
    @Autowired private WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void streamBotReply(Long conversationId, String userMessage, String email) {

        Users user = usersRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        Conversation conversation =
                conversationRepository
                        .findByIdAndUserAndDeletedFalse(conversationId, user)
                        .orElse(null);

        if (conversation == null) return;

        StringBuilder fullAnswer = new StringBuilder();
        List<String> sources = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        Flux<String> stream = webClient.post()
                .uri("/api/process-stream")
                .bodyValue(Map.of("question", userMessage))
                .retrieve()
                .bodyToFlux(String.class);

        stream.subscribe(

                // =============================
                // ON NEXT
                // =============================
                line -> {
                    try {
                        Map<String, Object> data =
                                objectMapper.readValue(line, Map.class);

                        String type = (String) data.get("type");

                        switch (type) {

                            case "TOKEN":
                                String token = (String) data.get("content");
                                if (token != null) {
                                    fullAnswer.append(token);

                                    messagingTemplate.convertAndSendToUser(
                                            email,
                                            "/queue/conversation/" + conversationId,
                                            new StreamResponse("TOKEN", token, null)
                                    );
                                }
                                break;

                            case "SOURCES":
                                List<String> receivedSources =
                                        (List<String>) data.get("sources");

                                if (receivedSources != null) {
                                    sources.clear();
                                    sources.addAll(receivedSources);

                                    messagingTemplate.convertAndSendToUser(
                                            email,
                                            "/queue/conversation/" + conversationId,
                                            new StreamResponse("SOURCES", null, sources)
                                    );
                                }
                                break;

                            case "FINAL_METADATA":
                                Map<String, Object> finalMeta =
                                        (Map<String, Object>) data.get("data");
                                if (finalMeta != null) {
                                    metadata.putAll(finalMeta);
                                }
                                break;

                            case "INTENT":
                                metadata.put("intent", data.get("intent"));
                                metadata.put("intentTimeMs", data.get("intentTimeMs"));
                                break;

                            case "COMPLETE":
                                metadata.put("totalExecutionTimeMs",
                                        data.get("totalExecutionTimeMs"));
                                metadata.put("sourceType",
                                        data.get("sourceType"));
                                break;
                        }

                    } catch (Exception ignored) {}
                },

                // =============================
                // ON ERROR
                // =============================
                error -> {
                    messagingTemplate.convertAndSendToUser(
                            email,
                            "/queue/conversation/" + conversationId,
                            new StreamResponse("ERROR", "Streaming failed.", null)
                    );

                    messagingTemplate.convertAndSendToUser(
                            email,
                            "/queue/conversation/" + conversationId,
                            new StreamResponse("COMPLETE", null, null)
                    );
                },

                // =============================
                // ON COMPLETE
                // =============================
                () -> {

                    try {

                        // Save BOT message
                        chatMessageManager.saveBotMessageInternal(
                                conversation,
                                fullAnswer.toString(),
                                sources
                        );

                        // Save AI execution
                        AiExecution exec = new AiExecution();
                        exec.setConversation(conversation);
                        exec.setUser(user);

                        exec.setFinalAnswer(fullAnswer.toString());
                        exec.setSourcesJson(
                                objectMapper.writeValueAsString(sources)
                        );

                        exec.setIntent((String) metadata.get("intent"));
                        exec.setIntentTimeMs(getLong(metadata.get("intentTimeMs")));
                        exec.setSourceType((String) metadata.get("sourceType"));
                        exec.setTotalExecutionTimeMs(
                                getLong(metadata.get("totalExecutionTimeMs"))
                        );
                        exec.setModelUsed((String) metadata.get("modelUsed"));
                        exec.setGenerationTimeMs(
                                getLong(metadata.get("generationTimeMs"))
                        );

                        aiExecutionRepository.save(exec);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    messagingTemplate.convertAndSendToUser(
                            email,
                            "/queue/conversation/" + conversationId,
                            new StreamResponse("COMPLETE", null, null)
                    );
                }
        );
    }

    private Long getLong(Object value) {
        return value instanceof Number
                ? ((Number) value).longValue()
                : null;
    }
}