package com.example.recycling_app.dto;

import com.google.gson.annotations.SerializedName; // Gson 라이브러리 사용 시, JSON 필드 이름 매핑을 위한 어노테이션

// FAQ(자주 묻는 질문) 데이터를 담는 DTO
// 백엔드 서버의 'FaqDTO'와 동일한 구조
// Retrofit 등을 통해 서버와 FAQ 데이터를 주고받을 때 사용
public class FaqDTO {
    // FAQ 항목의 고유 ID. Firestore 문서 ID와 매핑
    // 백엔드에서 ID를 설정하므로 클라이언트에서도 이 필드 필요
    private String id;
    // FAQ 질문 내용
    private String question;
    // FAQ 답변 내용
    private String answer;
    // FAQ 생성 시간 (타임스탬프)
    private long createdAt;
    // FAQ 최종 업데이트 시간 (타임스탬프)
    private long updatedAt;
    // FAQ 카테고리 (예: '일반', '분리수거', '앱 사용' 등)
    private String category;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 Retrofit 등에서 내부적으로 사용
    // 인자 없는 public 생성자 필수
    public FaqDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public FaqDTO(String id, String question, String answer, long createdAt, long updatedAt, String category) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.category = category;
    }

    // --- Getter 메서드 ---

    // ID 반환
    public String getId() {
        return id;
    }

    // 질문 반환
    public String getQuestion() {
        return question;
    }

    // 답변 반환
    public String getAnswer() {
        return answer;
    }

    // 생성 시간 반환
    public long getCreatedAt() {
        return createdAt;
    }

    // 업데이트 시간 반환
    public long getUpdatedAt() {
        return updatedAt;
    }

    // 카테고리 반환
    public String getCategory() {
        return category;
    }

    // --- Setter 메서드 ---

    // ID 설정
    public void setId(String id) {
        this.id = id;
    }

    // 질문 설정
    public void setQuestion(String question) {
        this.question = question;
    }

    // 답변 설정
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    // 생성 시간 설정
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // 업데이트 시간 설정
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 카테고리 설정
    public void setCategory(String category) {
        this.category = category;
    }
}