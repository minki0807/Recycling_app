package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//AI 인식 기록 정보를 담는 DTO (Data Transfer Object) 클래스
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiRecognitionRecordDTO {
    private String recognizatoinItem; // AI가 인식한 분리수거 항목 이름 (예: 플라스틱, 캔 등)
    private String imageUrl; // 사용자가 업로드한 이미지의 URL
    private long timestamp; // 인식이 이루어진 시간 (UNIX timestamp, 밀리초 단위)
}