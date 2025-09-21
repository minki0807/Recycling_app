package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



// 사용자 문의 데이터를 담는 DTO 클래스
// 사용자가 작성한 문의 제목, 내용, 작성 시간 및 답변 내용을 포함
// 사용자 문의 데이터를 담는 DTO 클래스
// 사용자가 작성한 문의 제목, 내용, 작성 시간 및 답변 내용을 포함

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InquiryDTO {
    private String uid;         // 문의 작성자 UID (Firebase 인증 사용자 식별자)
    private String title;       // 문의 제목
    private String content;     // 문의 내용
    private long timestamp;     // 문의 작성 시간 (UNIX timestamp, 밀리초)
    private String answer;      // 관리자 또는 시스템이 작성한 답변
}
