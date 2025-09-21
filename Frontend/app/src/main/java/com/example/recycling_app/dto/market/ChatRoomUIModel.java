// ChatRoomUIModel.java

package com.example.recycling_app.dto.market;

import java.util.Date;
import java.util.Objects;

// 화면 표시 전용 데이터 모델
public class ChatRoomUIModel {
    private final String chatRoomId;
    private final String otherUserId;
    private final String otherUserNickname;
    private final String otherUserProfileUrl;
    private final String lastMessage;
    private final Date updatedAt;
    private final long unreadCount;

    public ChatRoomUIModel(String chatRoomId, String otherUserId, String otherUserNickname, String otherUserProfileUrl, String lastMessage, Date updatedAt, long unreadCount) {
        this.chatRoomId = chatRoomId;
        this.otherUserId = otherUserId;
        this.otherUserNickname = otherUserNickname;
        this.otherUserProfileUrl = otherUserProfileUrl;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
        this.unreadCount = unreadCount;
    }

    // --- Getters ---
    public String getChatRoomId() { return chatRoomId; }
    public String getOtherUserId() { return otherUserId; }
    public String getOtherUserNickname() { return otherUserNickname; }
    public String getOtherUserProfileUrl() { return otherUserProfileUrl; }
    public String getLastMessage() { return lastMessage; }
    public Date getUpdatedAt() { return updatedAt; }
    public long getUnreadCount() { return unreadCount; }

    // DiffUtil을 위한 equals와 hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomUIModel that = (ChatRoomUIModel) o;
        return unreadCount == that.unreadCount &&
                chatRoomId.equals(that.chatRoomId) &&
                Objects.equals(otherUserNickname, that.otherUserNickname) &&
                Objects.equals(otherUserProfileUrl, that.otherUserProfileUrl) &&
                Objects.equals(lastMessage, that.lastMessage) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, otherUserNickname, otherUserProfileUrl, lastMessage, updatedAt, unreadCount);
    }
}