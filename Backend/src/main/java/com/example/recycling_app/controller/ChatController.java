package com.example.recycling_app.controller;

import com.google.cloud.Timestamp;
import com.example.recycling_app.dto.MessageDTO;
import com.example.recycling_app.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService; // 생성자 주입
    }

    @PostMapping("/room") // 채팅방 생성 or 가져오기
    public String createOrGetRoom(@RequestParam String postId, // 게시글 ID
                                  @RequestParam String uidA,   // 참여자 A
                                  @RequestParam String uidB) throws Exception {
        System.out.println("createRoom 호출됨: postId=" + postId + ", uidA=" + uidA + ", uidB=" + uidB);
        return chatService.createChatRoom(postId, uidA, uidB);
    }

    @PostMapping("/sendText") // 텍스트 메시지 전송
    public void sendText(@RequestParam String roomId,   // 채팅방 ID
                         @RequestParam String senderUid,// 보낸 사람 UID
                         @RequestParam String text) throws Exception { // 메시지 내용
        chatService.sendText(roomId, senderUid, text);
    }

    @PostMapping("/sendFile") // 파일/이미지 전송
    public void sendFile(@RequestParam String roomId,   // 채팅방 ID
                         @RequestParam String senderUid,// 보낸 사람 UID
                         @RequestParam MultipartFile file, // 업로드된 파일
                         @RequestParam(defaultValue = "false") boolean isImage) throws Exception { // 이미지 여부
        chatService.sendFile(roomId, senderUid, file, isImage);
    }

    @GetMapping("/rooms") // 내가 참여한 채팅방 목록
    public List<Map<String, Object>> listMyRooms(@RequestParam String myUid, // 내 UID
                                                 @RequestParam(defaultValue = "20") int limit) throws Exception { // 조회 개수 제한
        return chatService.listMyRoomsWithTitle(myUid, limit);
    }

    @GetMapping("/messages") // 채팅방 메시지 조회
    public List<MessageDTO> listMessages(@RequestParam String roomId, // 채팅방 ID
                                         @RequestParam(defaultValue = "50") int limit, // 조회 개수 제한
                                         @RequestParam(required = false) Long startAfter) throws Exception { // 페이징 기준 시각
        Timestamp ts = startAfter != null ? Timestamp.ofTimeSecondsAndNanos(startAfter, 0) : null;
        return chatService.listMessages(roomId, limit, ts);
    }

    @PostMapping("/markRead") // 메시지 읽음 처리
    public void markRead(@RequestParam String roomId, // 채팅방 ID
                         @RequestParam String myUid) throws Exception { // 읽은 사용자 UID
        chatService.markRead(roomId, myUid);
    }
}
