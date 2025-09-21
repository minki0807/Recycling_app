package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// FAQ(자주 묻는 질문) 데이터를 담는 DTO 클래스
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqDTO {
    private String id;           // 문서 ID (Firestore 문서 ID와 매핑 가능)
    private String question;     // 질문 내용
    private String answer;       // 답변 내용
    private long createdAt;      // 생성 시각(타임스탬프)
    private long updatedAt;      // 수정 시각(타임스탬프)
    private String category;     // 질문 분류 (선택 사항)
}
