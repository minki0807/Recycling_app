package com.example.recycling_app.dto;

// AI 이미지 인식 결과를 담는 데이터 전송 객체(DTO)
// 백엔드 서버의 'AiRecognitionRecordDTO'와 동일한 구조
// Retrofit 등을 통해 서버와 데이터 주고받을 때 사용
public class AiRecognitionRecordDTO {
    // 인식된 항목의 이름. 백엔드 DTO에 'recognizatoinItem' 오타가 있다면 그대로 반영
    private String recognizatoinItem;
    // 인식된 이미지의 URL. 서버에 저장된 이미지 위치
    private String imageUrl;
    // AI 인식이 수행된 시간의 타임스탬프 (밀리초 단위)
    private long timestamp;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 Retrofit 등에서 내부적으로 사용
    // 인자 없는 public 생성자 필수
    public AiRecognitionRecordDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public AiRecognitionRecordDTO(String recognizatoinItem, String imageUrl, long timestamp) {
        this.recognizatoinItem = recognizatoinItem;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // --- Getter 메서드 ---

    // 인식된 항목 이름 반환
    public String getRecognizatoinItem() {
        return recognizatoinItem;
    }

    // 이미지 URL 반환
    public String getImageUrl() {
        return imageUrl;
    }

    // 타임스탬프 반환
    public long getTimestamp() {
        return timestamp;
    }

    // --- Setter 메서드 ---

    // 인식된 항목 이름 설정
    public void setRecognizatoinItem(String recognizatoinItem) {
        this.recognizatoinItem = recognizatoinItem;
    }

    // 이미지 URL 설정
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 타임스탬프 설정
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}