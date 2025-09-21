package com.example.recycling_app.controller;


import com.example.recycling_app.dto.NotificationSettingDTO;
import com.example.recycling_app.service.NotificationService;
import com.example.recycling_app.util.FirebaseTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @Autowired
    private NotificationService notificationService;

    // 전체 알림 설정 조회
    @GetMapping
    public ResponseEntity<?> getNotificationSetting(@RequestHeader("Authorization") String idToken) {
        try {
            String uid = firebaseTokenVerifier.verifyIdToken(idToken);
//            String uid = "test_user_uid_123";
            NotificationSettingDTO setting = notificationService.getNotificationSetting(uid);
            return ResponseEntity.ok(setting);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("알림 설정을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 전체 알림 설정 업데이트
    @PostMapping
    public ResponseEntity<?> updateNotificationSetting(
            @RequestHeader(value = "Authorization", required = false) String idToken,
            @RequestBody NotificationSettingDTO settingDTO) {
        try {
            String uid = firebaseTokenVerifier.verifyIdToken(idToken);
//            String uid = "test_user_uid_123";
            notificationService.updateNotificationSetting(uid, settingDTO);
            return ResponseEntity.ok("알림 설정이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("알림 설정을 업데이트하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}