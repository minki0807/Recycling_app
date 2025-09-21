package com.example.recycling_app.dto.market;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatRoomDTO {

    // [개선] @DocumentId 어노테이션 추가
    // Firestore가 문서 ID를 이 필드에 자동으로 채워줍니다.
    @DocumentId
    private String chatRoomId;

    private String postId;
    private List<String> participants;
    private String lastMessage;
    private String lastMessageType;
    private String lastSenderUid;
    private Map<String, Long> unread;

    @ServerTimestamp
    private Date updatedAt;

    public ChatRoomDTO() {
    }

    // --- Getters ---
    public String getChatRoomId() { return chatRoomId; }
    public String getPostId() { return postId; }
    public List<String> getParticipants() { return participants; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageType() { return lastMessageType; }
    public String getLastSenderUid() { return lastSenderUid; }
    public Map<String, Long> getUnread() { return unread; } // 타입 변경
    public Date getUpdatedAt() { return updatedAt; }

    // --- Setters ---
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageType(String lastMessageType) { this.lastMessageType = lastMessageType; }
    public void setLastSenderUid(String lastSenderUid) { this.lastSenderUid = lastSenderUid; }
    public void setUnread(Map<String, Long> unread) { this.unread = unread; } // 타입 변경
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}