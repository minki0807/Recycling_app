package com.example.recycling_app.dto.market;

import java.util.Date;

public class ChatMessageDTO {
    private String senderUid;
    private String message;
    private Date createdAt;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String senderUid, String message, Date createdAt) {
        this.senderUid = senderUid;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getSenderUid() { return senderUid; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }
}

