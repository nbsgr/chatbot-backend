package ai.manager;

import ai.model.*;
import ai.rep.AiExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BotBrainManager {

    @Autowired
    private AiExecutionRepository aiExecutionRepository;

    @Autowired
    private UserMemoryManager userMemoryManager;

    private static final String FLASK_URL =
            "http://localhost:5000/api/process";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public BotResult processUserMessage(
            String userMessage,
            Users user,
            Conversation conversation
    ) throws Exception {

        // =====================================
        // 1️⃣ Call Flask
        // =====================================
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("question", userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        FLASK_URL,
                        request,
                        Map.class
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Flask API failed");
        }

        Map<String, Object> body = response.getBody();

        // ✅ SAFE SUCCESS CHECK
        if (body == null ||
                !Boolean.TRUE.equals(body.get("success"))) {
            throw new RuntimeException("Invalid Flask response");
        }

        // =====================================
        // 2️⃣ Extract Core Fields
        // =====================================
        String answer =
                Optional.ofNullable((String) body.get("finalAnswer"))
                        .orElse("⚠️ Unable to generate response.");

        List<String> sources =
                body.get("sources") instanceof List
                        ? (List<String>) body.get("sources")
                        : new ArrayList<>();

        Map<String, String> memoryUpdate =
                body.get("memoryUpdate") instanceof Map
                        ? (Map<String, String>) body.get("memoryUpdate")
                        : null;

        // =====================================
        // 3️⃣ Memory Updates
        // =====================================
        String nickname = null;
        String fullname = null;

        if (memoryUpdate != null) {

            nickname = memoryUpdate.get("nickname");
            fullname = memoryUpdate.get("fullname");

            if (nickname != null && !nickname.isBlank()) {
                userMemoryManager
                        .saveOrUpdateMemory(user, "nickname", nickname);
            }

            if (fullname != null && !fullname.isBlank()) {
                userMemoryManager
                        .saveOrUpdateMemory(user, "fullname", fullname);
            }
        }

        // =====================================
        // 4️⃣ AI EXECUTION LOG
        // =====================================
        AiExecution exec = new AiExecution();

        exec.setIntent((String) body.get("intent"));
        exec.setIntentTimeMs(getLong(body.get("intentTimeMs")));
        exec.setSourceType((String) body.get("sourceType"));
        exec.setTotalExecutionTimeMs(
                getLong(body.get("totalExecutionTimeMs"))
        );

        exec.setFinalAnswer(answer);
        exec.setSourcesJson(
                objectMapper.writeValueAsString(sources)
        );

        // =====================================
        // 5️⃣ SEARCH STAGE
        // =====================================
        parseSearch(exec, body);

        // =====================================
        // 6️⃣ CRAWL STAGE
        // =====================================
        parseCrawl(exec, body);

        // =====================================
        // 7️⃣ SCRAPE STAGE
        // =====================================
        parseScrape(exec, body);

        // =====================================
        // 8️⃣ RAG STAGE
        // =====================================
        parseRag(exec, body);

        // =====================================
        // 9️⃣ GENERATION STAGE
        // =====================================
        parseGeneration(exec, body);

        exec.setConversation(conversation);
        exec.setUser(user);

        aiExecutionRepository.save(exec);

        // =====================================
        // 🔟 Return Result
        // =====================================
        return new BotResult(answer, sources, nickname, fullname);
    }

    // =====================================================
    // STAGE PARSERS
    // =====================================================

    private void parseSearch(AiExecution exec, Map<String,Object> body) throws Exception {

        Map<String,Object> stage =
                (Map<String,Object>) body.get("searchStage");

        if(stage == null) return;

        exec.setSearchQuery((String) stage.get("query"));
        exec.setSearchTotalResults(getInt(stage.get("totalResults")));
        exec.setSearchUniqueDomains(getInt(stage.get("uniqueDomains")));
        exec.setSearchUrlsJson(
                objectMapper.writeValueAsString(stage.get("urls"))
        );
        exec.setSearchTimeMs(getLong(stage.get("timeMs")));
    }

    private void parseCrawl(AiExecution exec, Map<String,Object> body) throws Exception {

        Map<String,Object> stage =
                (Map<String,Object>) body.get("crawlStage");

        if(stage == null) return;

        exec.setCrawlTotalSuccess(getInt(stage.get("totalSuccess")));
        exec.setCrawlTotalFailed(getInt(stage.get("totalFailed")));
        exec.setCrawlParentUrlsJson(
                objectMapper.writeValueAsString(stage.get("parentUrls"))
        );
        exec.setCrawlChildUrlsJson(
                objectMapper.writeValueAsString(stage.get("childUrls"))
        );
        exec.setCrawlTimeMs(getLong(stage.get("timeMs")));
    }

    private void parseScrape(AiExecution exec, Map<String,Object> body) {

        Map<String,Object> stage =
                (Map<String,Object>) body.get("scrapeStage");

        if(stage == null) return;

        exec.setScrapeAccepted(getInt(stage.get("accepted")));
        exec.setScrapeRejected(getInt(stage.get("rejected")));
        exec.setScrapeTimeMs(getLong(stage.get("timeMs")));
    }

    private void parseRag(AiExecution exec, Map<String,Object> body) throws Exception {

        Map<String,Object> stage =
                (Map<String,Object>) body.get("ragStage");

        if(stage == null) return;

        exec.setRagRetrievedCount(getInt(stage.get("retrievedCount")));
        exec.setRagChunksIndexed(getInt(stage.get("chunksIndexed")));
        exec.setRagRetrievedUrlsJson(
                objectMapper.writeValueAsString(stage.get("retrievedUrls"))
        );
        exec.setRagTimeMs(getLong(stage.get("timeMs")));
    }

    private void parseGeneration(AiExecution exec, Map<String,Object> body) {

        Map<String,Object> stage =
                (Map<String,Object>) body.get("generationStage");

        if(stage == null) return;

        exec.setModelUsed((String) stage.get("modelUsed"));
        exec.setGenerationTimeMs(
                getLong(stage.get("generationTimeMs"))
        );
    }

    // =====================================================
    // SAFE PARSERS
    // =====================================================

    private Long getLong(Object value) {
        return value instanceof Number
                ? ((Number) value).longValue()
                : null;
    }

    private Integer getInt(Object value) {
        return value instanceof Number
                ? ((Number) value).intValue()
                : null;
    }

    // =====================================================
    // DTO
    // =====================================================

    public static class BotResult {

        public final String answer;
        public final List<String> sources;
        public final String nickname;
        public final String fullname;

        public BotResult(
                String answer,
                List<String> sources,
                String nickname,
                String fullname
        ) {
            this.answer = answer;
            this.sources = sources;
            this.nickname = nickname;
            this.fullname = fullname;
        }
    }
}