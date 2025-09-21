package com.example.recycling_app.dto.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeminiResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private String data;

    // 기본 생성자
    public GeminiResponse() {}

    // 성공 응답을 위한 정적 메서드
    public static GeminiResponse success(String data) {
        GeminiResponse response = new GeminiResponse();
        response.success = true;
        response.message = "성공";
        response.data = data;
        return response;
    }

    // 실패 응답을 위한 정적 메서드
    public static GeminiResponse failure(String message) {
        GeminiResponse response = new GeminiResponse();
        response.success = false;
        response.message = message;
        response.data = null;
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}