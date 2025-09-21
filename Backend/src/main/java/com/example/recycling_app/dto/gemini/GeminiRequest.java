package com.example.recycling_app.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeminiRequest {
    @JsonProperty("classification")
    private String classification;

    // 기본 생성자
    public GeminiRequest() {}

    // 생성자
    public GeminiRequest(String classification) {
        this.classification = classification;
    }

    // Getter, Setter
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
}