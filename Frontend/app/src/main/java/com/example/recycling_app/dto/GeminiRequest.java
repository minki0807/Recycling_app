package com.example.recycling_app.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

/**
 * Gemini API 요청을 위한 데이터 전송 객체 (DTO)
 * 백엔드 서버로 분류된 쓰레기 정보를 전송하기 위해 사용
 */
public class GeminiRequest {

    /**
     * 분류된 쓰레기의 종류
     * 예: "플라스틱 병", "종이", "캔", "유리병" 등
     */
    @SerializedName("classification")
    private String classification;

    /**
     * 기본 생성자
     * Gson 직렬화/역직렬화를 위해 필요
     */
    public GeminiRequest() {
    }

    /**
     * 분류 정보를 받는 생성자
     *
     * @param classification 분류된 쓰레기 종류
     */
    public GeminiRequest(@NonNull String classification) {
        this.classification = classification;
    }

    /**
     * 분류 정보를 반환하는 getter 메서드
     *
     * @return 분류된 쓰레기 종류
     */
    @Nullable
    public String getClassification() {
        return classification;
    }

    /**
     * 분류 정보를 설정하는 setter 메서드
     *
     * @param classification 분류된 쓰레기 종류
     */
    public void setClassification(@Nullable String classification) {
        this.classification = classification;
    }

    /**
     * 요청 데이터의 유효성을 검사하는 메서드
     *
     * @return 유효한 데이터인지 여부
     */
    public boolean isValid() {
        return classification != null && !classification.trim().isEmpty();
    }

    /**
     * 분류 정보를 정리하여 설정하는 유틸리티 메서드
     * 앞뒤 공백 제거 및 null 체크 포함
     *
     * @param classification 설정할 분류 정보
     */
    public void setClassificationSafe(@Nullable String classification) {
        if (classification != null) {
            this.classification = classification.trim();
        } else {
            this.classification = null;
        }
    }

    /**
     * 객체의 문자열 표현을 반환
     * 디버깅 및 로깅 목적으로 사용
     *
     * @return 객체의 문자열 표현
     */
    @NonNull
    @Override
    public String toString() {
        return "GeminiRequest{" +
                "classification='" + classification + '\'' +
                '}';
    }

    /**
     * 객체 동등성 비교
     *
     * @param obj 비교할 객체
     * @return 동등한지 여부
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        GeminiRequest that = (GeminiRequest) obj;
        return classification != null ?
                classification.equals(that.classification) :
                that.classification == null;
    }

    /**
     * 객체의 해시코드 반환
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return classification != null ? classification.hashCode() : 0;
    }
}
