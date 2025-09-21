package com.example.recycling_app.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String senderUid;   // 보낸 사용자 UID
    private String type;        // 메시지 타입 (text, image, file)
    private String text;        // 텍스트 메시지 내용
    private String mediaUrl;    // 이미지/파일 다운로드 URL
    private String storagePath; // Firebase Storage 저장 경로
    private String fileName;    // 파일 이름
    private Long fileSize;      // 파일 크기 (바이트 단위)
    private String mimeType;    // 파일 MIME 타입
    private Timestamp createdAt; // 생성 시간 (Firestore Timestamp)
}
