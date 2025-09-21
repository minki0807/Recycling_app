package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 사용자 알림 설정 정보를 담는 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationSettingDTO {
    private boolean recyclingNotificationEnabled; // 분리수거 알림 설정
    private int daysBefore;
    private String notificationTime;
    private boolean adNotificationEnabled; // 광고/프로모션 알림 설정
    private boolean infoNotificationEnabled; // 공지사항 등 정보성 알림 설정
}