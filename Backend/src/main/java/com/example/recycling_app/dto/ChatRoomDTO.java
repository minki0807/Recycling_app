package com.example.recycling_app.dto;

import com.google.cloud.Timestamp;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private String postId;                  // 게시글 ID (채팅방 생성 기준이 되는 게시글)
    private List<String> participants;      // 참여자 UID 목록 [uidA, uidB]
    private Timestamp createdAt;            // 채팅방 생성 시각
    private Timestamp updatedAt;            // 마지막 메시지 전송 시각
    private String lastMessage;             // 마지막 메시지 내용
    private String lastMessageType;         // 마지막 메시지 타입 (text | image | file)
    private String lastSenderUid;           // 마지막 메시지를 보낸 사용자 UID
    private Map<String, Long> unread;       // 사용자별 안 읽은 메시지 수 {uidA:0, uidB:2}
}
