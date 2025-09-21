package com.example.recycling_app.controller;

import com.example.recycling_app.dto.LanguageDTO;
import com.example.recycling_app.service.LanguageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 사용자의 언어 설정 조회 및 저장/수정을 처리하는 컨트롤러
@RestController
@RequestMapping("/language") // 기본 URL 경로: /language
public class LanguageController {

    @Autowired
    private LanguageService languageService; // 언어 설정 관련 비즈니스 로직 처리 서비스

    // 언어 설정 조회 (사용자용)
    @GetMapping("/{uid}")
    public ResponseEntity<LanguageDTO> getLanguage(
            @PathVariable String uid // 조회할 사용자의 UID
    ) {
        try {
            LanguageDTO dto = languageService.getLanguageSetting(uid); // 서비스 호출하여 언어 설정 조회
            return ResponseEntity.ok(dto);                             // 200 OK + 언어 설정 정보 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();       // 500 Internal Server Error
        }
    }

    // 언어 설정 저장 또는 수정 (사용자용)
    @PostMapping("/{uid}")
    public ResponseEntity<String> updateLanguage(
            @PathVariable String uid,      // 언어 설정을 저장할 사용자 UID
            @RequestBody LanguageDTO dto   // 저장할 언어 설정 정보
    ) {
        try {
            languageService.updateLanguageSetting(uid, dto);          // 언어 설정 저장 또는 수정 처리
            return ResponseEntity.ok("언어 설정 저장 성공");            // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("언어 설정 저장 실패"); // 500 오류 + 메시지
        }
    }
}
