package com.example.recycling_app.dto;

// 사용자 언어 설정을 담는 DTO
// 백엔드 서버의 'LanguageDTO'와 동일한 구조
// Retrofit 등을 통해 서버와 언어 설정 데이터를 주고받을 때 사용
public class LanguageDTO {
    // 사용자가 설정한 언어 코드 (예: 한국어는 "ko", 영어는 "en")
    private String language;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 Retrofit 등에서 내부적으로 사용
    // 인자 없는 public 생성자 필수
    public LanguageDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public LanguageDTO(String language) {
        this.language = language;
    }

    // --- Getter 메서드 ---

    // 언어 코드 반환
    public String getLanguage() {
        return language;
    }

    // --- Setter 메서드 ---

    // 언어 코드 설정
    public void setLanguage(String language) {
        this.language = language;
    }
}