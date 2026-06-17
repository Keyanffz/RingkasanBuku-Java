package com.ringkasanbuku.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Summary {
    private String id;
    private String title;
    private String originalText;
    private String result;
    private String method;
    private String apiProvider;
    private LocalDateTime timestamp;
    private SummaryOptions options;

    public Summary(String title, String originalText, String result,
            String method, String apiProvider, SummaryOptions options) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.originalText = originalText;
        this.result = result;
        this.method = method;
        this.apiProvider = apiProvider;
        this.timestamp = LocalDateTime.now();
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getResult() {
        return result;
    }

    public String getMethod() {
        return method;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public SummaryOptions getOptions() {
        return options;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
