package com.example.recycling_app.controller;

import com.example.recycling_app.dto.AiRecognitionRecordDTO;
import com.example.recycling_app.service.ActivityRecordService;
import com.example.recycling_app.util.FirebaseTokenVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;


// AI 인식 활동 기록을 조회하는 컨트롤러 클래스
// 사용자의 Firebase 인증 토큰을 검증하여 본인만 조회 가능하도록 처리
@RestController
@RequestMapping("/activity")
public class ActivityRecordController {

    @Autowired
    private ActivityRecordService activityRecordService;           // 활동 기록 조회 서비스

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier;           // Firebase 토큰 검증 객체

    @GetMapping("/{uid}") // GET /activity/{uid}
    public ResponseEntity<?> getActivityRecords(
            @PathVariable String uid,                              // 경로에서 전달된 사용자 UID
            @RequestHeader("Authorization") String authHeader      // HTTP 헤더에 담긴 Firebase 인증 토큰
    ) {
        try {
            String token = authHeader.replace("Bearer ", ""); // "Bearer " 접두어 제거
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token); // 토큰 검증 및 UID 추출

            if (!uid.equals(verifiedUid)) {                        // UID가 일치하지 않으면 권한 없음
                return ResponseEntity.status(401).body("권한이 없습니다.");
            }

            List<AiRecognitionRecordDTO> records =
                    activityRecordService.getUserActivityRecords(uid); // 사용자 활동 기록 조회
            return ResponseEntity.ok(records);                          // 기록 반환

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("기록 불러오기 실패"); // DB 접근 실패 등
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("토큰 검증 실패");     // 인증 실패 처리
        }
    }
}
