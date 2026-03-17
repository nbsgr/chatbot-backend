package ai.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_execution")
public class AiExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC INFO
    // ===============================

    private String intent;

    private Long intentTimeMs;

    private String sourceType; // DIRECT or RAG

    private Long totalExecutionTimeMs;

    private LocalDateTime createdAt = LocalDateTime.now();


    // ===============================
    // SEARCH STAGE
    // ===============================

    @Lob
    @Column(columnDefinition = "TEXT")
    private String searchQuery;

    private Integer searchTotalResults;

    private Integer searchUniqueDomains;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String searchUrlsJson;

    private Long searchTimeMs;


    // ===============================
    // CRAWL STAGE
    // ===============================

    private Integer crawlTotalSuccess;

    private Integer crawlTotalFailed;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String crawlParentUrlsJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String crawlChildUrlsJson;

    private Long crawlTimeMs;


    // ===============================
    // SCRAPE STAGE
    // ===============================

    private Integer scrapeAccepted;

    private Integer scrapeRejected;

    private Long scrapeTimeMs;


    // ===============================
    // RAG STAGE
    // ===============================

    private Integer ragRetrievedCount;

    private Integer ragChunksIndexed;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String ragRetrievedUrlsJson;

    private Long ragTimeMs;


    // ===============================
    // GENERATION STAGE
    // ===============================

    private String modelUsed;

    private Long generationTimeMs;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String finalAnswer;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String sourcesJson;


    // ===============================
    // RELATIONS (Optional)
    // ===============================

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;


    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getId() { return id; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public Long getIntentTimeMs() { return intentTimeMs; }
    public void setIntentTimeMs(Long intentTimeMs) { this.intentTimeMs = intentTimeMs; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getTotalExecutionTimeMs() { return totalExecutionTimeMs; }
    public void setTotalExecutionTimeMs(Long totalExecutionTimeMs) { this.totalExecutionTimeMs = totalExecutionTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public Integer getSearchTotalResults() { return searchTotalResults; }
    public void setSearchTotalResults(Integer searchTotalResults) { this.searchTotalResults = searchTotalResults; }

    public Integer getSearchUniqueDomains() { return searchUniqueDomains; }
    public void setSearchUniqueDomains(Integer searchUniqueDomains) { this.searchUniqueDomains = searchUniqueDomains; }

    public String getSearchUrlsJson() { return searchUrlsJson; }
    public void setSearchUrlsJson(String searchUrlsJson) { this.searchUrlsJson = searchUrlsJson; }

    public Long getSearchTimeMs() { return searchTimeMs; }
    public void setSearchTimeMs(Long searchTimeMs) { this.searchTimeMs = searchTimeMs; }

    public Integer getCrawlTotalSuccess() { return crawlTotalSuccess; }
    public void setCrawlTotalSuccess(Integer crawlTotalSuccess) { this.crawlTotalSuccess = crawlTotalSuccess; }

    public Integer getCrawlTotalFailed() { return crawlTotalFailed; }
    public void setCrawlTotalFailed(Integer crawlTotalFailed) { this.crawlTotalFailed = crawlTotalFailed; }

    public String getCrawlParentUrlsJson() { return crawlParentUrlsJson; }
    public void setCrawlParentUrlsJson(String crawlParentUrlsJson) { this.crawlParentUrlsJson = crawlParentUrlsJson; }

    public String getCrawlChildUrlsJson() { return crawlChildUrlsJson; }
    public void setCrawlChildUrlsJson(String crawlChildUrlsJson) { this.crawlChildUrlsJson = crawlChildUrlsJson; }

    public Long getCrawlTimeMs() { return crawlTimeMs; }
    public void setCrawlTimeMs(Long crawlTimeMs) { this.crawlTimeMs = crawlTimeMs; }

    public Integer getScrapeAccepted() { return scrapeAccepted; }
    public void setScrapeAccepted(Integer scrapeAccepted) { this.scrapeAccepted = scrapeAccepted; }

    public Integer getScrapeRejected() { return scrapeRejected; }
    public void setScrapeRejected(Integer scrapeRejected) { this.scrapeRejected = scrapeRejected; }

    public Long getScrapeTimeMs() { return scrapeTimeMs; }
    public void setScrapeTimeMs(Long scrapeTimeMs) { this.scrapeTimeMs = scrapeTimeMs; }

    public Integer getRagRetrievedCount() { return ragRetrievedCount; }
    public void setRagRetrievedCount(Integer ragRetrievedCount) { this.ragRetrievedCount = ragRetrievedCount; }

    public Integer getRagChunksIndexed() { return ragChunksIndexed; }
    public void setRagChunksIndexed(Integer ragChunksIndexed) { this.ragChunksIndexed = ragChunksIndexed; }

    public String getRagRetrievedUrlsJson() { return ragRetrievedUrlsJson; }
    public void setRagRetrievedUrlsJson(String ragRetrievedUrlsJson) { this.ragRetrievedUrlsJson = ragRetrievedUrlsJson; }

    public Long getRagTimeMs() { return ragTimeMs; }
    public void setRagTimeMs(Long ragTimeMs) { this.ragTimeMs = ragTimeMs; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public Long getGenerationTimeMs() { return generationTimeMs; }
    public void setGenerationTimeMs(Long generationTimeMs) { this.generationTimeMs = generationTimeMs; }

    public String getFinalAnswer() { return finalAnswer; }
    public void setFinalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; }

    public String getSourcesJson() { return sourcesJson; }
    public void setSourcesJson(String sourcesJson) { this.sourcesJson = sourcesJson; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
}