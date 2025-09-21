package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecyclingScheduleDTO {
    private String region;       // 지역명 (예: "서울시 강남구")
    private String recyclingDay; // 재활용품 배출 요일 (예: "화요일")
    private String startTime;    // 배출 시작 시간 (예: "20:00")
    private String endTime;      // 배출 종료 시간 (예: "01:00")
    private String description;  // 배출 관련 상세 설명
}