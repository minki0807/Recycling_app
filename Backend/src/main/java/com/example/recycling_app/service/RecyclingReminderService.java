package com.example.recycling_app.service;

import com.example.recycling_app.dto.NotificationSettingDTO;
import com.example.recycling_app.dto.RecyclingScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class RecyclingReminderService {

    @Autowired
    private RecyclingScheduleService recyclingScheduleService;

    @Autowired
    private FcmPushService fcmPushService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProfileService profileService;

    //매일 자정(0시 0분)에 실행되는 스케줄링 메서드
    //분리수거 알림을 받을 사용자를 확인하고 푸시 알림을 전송
    @Scheduled(cron = "0 0 0 * * *")
    public void checkForRecyclingDay() {
        try {
            // 1. 모든 사용자의 UID 목록을 가져옴
            List<String> allUids = profileService.getAllUserUids();

            // 2. 각 사용자에 대해 알림 로직을 실행
            for (String uid : allUids) {
                // 개별 사용자 로직에서 발생하는 예외를 처리하여 전체 스케줄링이 멈추는 것을 방지
                try {
                    NotificationSettingDTO notificationSetting = notificationService.getNotificationSetting(uid);

                    // 알림 설정이 활성화된 경우에만 로직을 수행
                    if (notificationSetting != null && notificationSetting.isRecyclingNotificationEnabled()) {
                        // 사용자 프로필에서 주소 정보 가져오기
                        String userAddress = profileService.getProfile(uid).getAddress();
                        String[] addressParts = userAddress.split(" ");
                        String sidoName = addressParts[0];
                        String sigunguName = addressParts[1];

                        // 지역별 분리수거 일정 조회
                        RecyclingScheduleDTO schedule = recyclingScheduleService.getScheduleByRegion(sidoName, sigunguName);

                        LocalDate today = LocalDate.now();
                        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

                        // 오늘이 분리수거 요일과 일치하는지 확인
                        // `DayOfWeek`는 MONDAY, TUESDAY 등으로 반환되므로 문자열 비교를 사용
                        if (schedule != null && todayDayOfWeek.toString().equals(schedule.getRecyclingDay())) {
                            String title = "분리수거 알림";
                            String body = schedule.getDescription() + " 오늘 배출해주세요.";

                            // FCM 푸시 알림 전송 (주석 처리됨)
                            // fcmPushService.sendPushNotification(uid, title, body);
                        }
                    }
                } catch (Exception e) {
                    // 특정 사용자에 대한 처리 중 발생한 예외를 출력하고 다음 사용자로 넘어감
                    System.err.println("Failed to process recycling reminder for UID " + uid + ": " + e.getMessage());
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            // 모든 UID를 가져오는 과정에서 발생한 예외는 로그로 기록
            e.printStackTrace();
        }
    }
}