package com.example.recycling_app.dto;

import com.google.gson.annotations.SerializedName;

public class GeminiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private String data;

    @SerializedName("message")
    private String message;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}