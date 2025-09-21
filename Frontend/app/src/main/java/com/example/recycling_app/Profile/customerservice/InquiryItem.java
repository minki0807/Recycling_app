package com.example.recycling_app.Profile.customerservice;

import com.google.gson.annotations.SerializedName; // GSON 라이브러리의 어노테이션 임포트

// 1:1 문의 항목의 데이터를 담는 데이터 전송 객체 (DTO) 또는 모델 클래스
// 백엔드 API로부터 받아오는 JSON 데이터를 이 객체로 매핑
public class InquiryItem {
    // @SerializedName 어노테이션은 JSON 필드 이름과 자바(Java) 객체의 필드 이름을 연결
    @SerializedName("id") // JSON의 "id" 필드를 이 `id` 변수에 매핑
    private String id; // 문의 고유 ID
    @SerializedName("subject") // JSON의 "subject" 필드를 이 `subject` 변수에 매핑
    private String subject; // 문의 제목
    @SerializedName("date") // JSON의 "date" 필드를 이 `date` 변수에 매핑
    private String date; // 문의 날짜 (문자열 형식)
    @SerializedName("status") // JSON의 "status" 필드를 이 `status` 변수에 매핑
    private String status; // 문의 처리 상태 (예: "답변 대기", "답변 완료")

    // 레트로핏(Retrofit) (내부적으로 GSON 사용)이 JSON을 자바 객체로 변환할 때 사용할 기본 생성자
    // 파라미터가 없는 생성자는 필수
    public InquiryItem() {
    }

    // 모든 필드를 초기화하는 생성자 (객체 생성 시 편리)
    public InquiryItem(String id, String subject, String date, String status) {
        this.id = id;
        this.subject = subject;
        this.date = date;
        this.status = status;
    }

    // --- 게터(Getter) 메서드들 ---
    // 각 필드의 값을 외부에서 읽을 수 있도록 제공
    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    // --- 세터(Setter) 메서드들 (필요시 추가) ---
    // 각 필드의 값을 외부에서 설정할 수 있도록 제공
    public void setId(String id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}