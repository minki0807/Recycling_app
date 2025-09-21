package com.example.recycling_app.dto;

// 재활용된 품목의 데이터를 담는 DTO
// AI 인식을 통해 기록된 재활용 데이터를 나타내는 데 사용
public class RecycleData {
    // 인식된 재활용 품목의 이름 (예: "플라스틱 병", "종이컵")
    private String itemName;
    // 품목이 인식된 시간 (날짜 및 시간 문자열 형식)
    private String recognizedAt;
    // 인식된 품목의 이미지 URL (서버에 저장된 이미지 경로)
    private String imageUrl;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 라이브러리에서 내부적으로 사용
    public RecycleData(){}

    // --- Getter 메서드 ---

    // 품목 이름 반환
    public String getItemName(){
        return itemName;
    }

    // 인식된 시간 반환
    public String getRecognizedAt(){
        return recognizedAt;
    }

    // 이미지 URL 반환
    public String getImageUrl(){
        return imageUrl;
    }

    // --- Setter 메서드 ---

    // 품목 이름 설정
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // 인식된 시간 설정
    public void setRecognizedAt(String recognizedAt) {
        this.recognizedAt = recognizedAt;
    }

    // 이미지 URL 설정
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}