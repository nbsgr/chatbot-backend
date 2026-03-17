package ai.dto;

import java.util.List;

public class StreamResponse {

    private String type; // TOKEN | SOURCES | COMPLETE | ERROR
    private String content;
    private List<String> sources;

    public StreamResponse() {}

    public StreamResponse(String type, String content, List<String> sources) {
        this.type = type;
        this.content = content;
        this.sources = sources;
    }

    public String getType() { return type; }
    public String getContent() { return content; }
    public List<String> getSources() { return sources; }

    public void setType(String type) { this.type = type; }
    public void setContent(String content) { this.content = content; }
    public void setSources(List<String> sources) { this.sources = sources; }
}