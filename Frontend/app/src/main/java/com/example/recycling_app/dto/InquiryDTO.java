package com.example.recycling_app.dto;

import com.google.gson.annotations.SerializedName; // Gson 라이브러리 사용 시, JSON 필드 이름 매핑을 위한 어노테이션 (현재 코드에서는 직접 사용되지 않음)

// 1:1 문의 데이터를 담는 DTO
// 백엔드 서버의 'InquiryDTO'와 동일한 구조
// Retrofit 등을 통해 서버와 1:1 문의 데이터를 주고받을 때 사용
public class InquiryDTO {
    // 문의 작성자 고유 ID (Firebase 인증 사용자 식별자)
    private String uid;
    // 문의 제목
    private String title;
    // 문의 내용
    private String content;
    // 문의 작성 시간 (UNIX timestamp, 밀리초 단위)
    private long timestamp;
    // 관리자 또는 시스템이 작성한 답변 (선택 사항이며, 없을 수도 있음)
    private String answer;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 Retrofit 등에서 내부적으로 사용
    // 인자 없는 public 생성자 필수
    public InquiryDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public InquiryDTO(String uid, String title, String content, long timestamp, String answer) {
        this.uid = uid;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.answer = answer;
    }

    // --- Getter 메서드 ---

    // 작성자 UID 반환
    public String getUid() {
        return uid;
    }

    // 문의 제목 반환
    public String getTitle() {
        return title;
    }

    // 문의 내용 반환
    public String getContent() {
        return content;
    }

    // 문의 작성 시간(타임스탬프) 반환
    public long getTimestamp() {
        return timestamp;
    }

    // 답변 내용 반환
    public String getAnswer() {
        return answer;
    }

    // --- Setter 메서드 ---

    // 작성자 UID 설정
    public void setUid(String uid) {
        this.uid = uid;
    }

    // 문의 제목 설정
    public void setTitle(String title) {
        this.title = title;
    }

    // 문의 내용 설정
    public void setContent(String content) {
        this.content = content;
    }

    // 문의 작성 시간(타임스탬프) 설정
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // 답변 내용 설정
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}