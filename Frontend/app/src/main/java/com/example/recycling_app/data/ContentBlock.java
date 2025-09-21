package com.example.recycling_app.data;

import com.google.gson.annotations.SerializedName;

public class ContentBlock {
    @SerializedName("type")
    private String type;

    @SerializedName("text")
    private String text;

    @SerializedName("mediaUrl")
    private String mediaUrl;

    @SerializedName("order")
    private int order;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
