package com.example.recycling_app.dto.market;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.Objects;

public class ChatMessageDTO {
    private String senderUid;
    private String message;

    // Firestore 서버 시간을 기준으로 자동으로 타임스탬프가 기록됩니다.
    @ServerTimestamp
    private Date createdAt;

    public ChatMessageDTO() {}

    public ChatMessageDTO(String senderUid, String message) {
        this.senderUid = senderUid;
        this.message = message;
    }

    // Getters
    public String getSenderUid() { return senderUid; }
    public String getMessage() { return message; }
    public Date getCreatedAt() { return createdAt; }

    // Setters
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // DiffUtil이 객체 내용을 비교하기 위해 반드시 필요한 메소드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessageDTO that = (ChatMessageDTO) o;
        return Objects.equals(senderUid, that.senderUid) &&
                Objects.equals(message, that.message) &&
                Objects.equals(createdAt, that.createdAt);
    }

    // equals를 구현할 때 반드시 함께 구현해야 하는 메소드
    @Override
    public int hashCode() {
        return Objects.hash(senderUid, message, createdAt);
    }
}