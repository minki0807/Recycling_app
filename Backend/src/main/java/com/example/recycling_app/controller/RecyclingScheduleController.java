package com.example.recycling_app.controller;

import com.example.recycling_app.dto.RecyclingScheduleDTO;
import com.example.recycling_app.service.ProfileService;
import com.example.recycling_app.service.RecyclingScheduleService;
import com.example.recycling_app.util.FirebaseTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recycling-schedule")
public class RecyclingScheduleController {

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private RecyclingScheduleService recyclingScheduleService;

    // 사용자 지역의 분리수거 일정 조회
    @GetMapping
    public ResponseEntity<?> getRecyclingScheduleByRegion(@RequestHeader(value = "Authorization", required = false) String idToken) {
        try {
            String uid = firebaseTokenVerifier.verifyIdToken(idToken);
//            String uid = "test_user_uid_123"; // 필터를 건너뛰고 임시 UID를 사용
            String userAddress = profileService.getProfile(uid).getAddress();

            // 주소에서 시도와 시군구 정보 추출 (예: "대구광역시 군위군 부계면...")
            String[] addressParts = userAddress.split(" ");
            String sidoName = addressParts[0];
            String sigunguName = addressParts[1];

            // 다른 팀원의 컬렉션에서 분리수거 일정 정보 조회
            RecyclingScheduleDTO schedule = recyclingScheduleService.getScheduleByRegion(sidoName, sigunguName);

            if (schedule == null) {
                return ResponseEntity.status(404).body("해당 지역의 분리수거 정보를 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("분리수거 일정을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}