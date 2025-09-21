package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


 // 사용자의 제보 기록을 담는 DTO 클래스
 // 제보 제목, 내용 및 제보된 시간을 포함

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRecordDTO {
    private String title;        // 제보 제목
    private String content;      // 제보 상세 내용
    private long timestamp;      // 제보한 시간 (UNIX timestamp, 밀리초 단위)
}
