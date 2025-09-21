package com.example.recycling_app.controller;

import com.example.recycling_app.dto.FaqDTO;
import com.example.recycling_app.service.FaqService;
import com.example.recycling_app.service.UserService;
import com.example.recycling_app.util.FirebaseTokenVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// FAQ(자주 묻는 질문) 데이터를 처리하는 컨트롤러 클래스
// 전체 FAQ 조회, 단일 FAQ 조회는 누구나 가능
// 등록/수정/삭제는 관리자 권한 필요
@RestController
@RequestMapping("/faq")
public class FaqController {

    @Autowired
    private FaqService faqService;  // FAQ 서비스 객체 주입

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier; // Firebase 인증 토큰 검증기 주입

    @Autowired
    private UserService userService;  // 사용자 정보 및 권한 처리 서비스 주입

    // 관리자 권한을 체크하는 내부 메서드
    // Firebase 인증 토큰에서 uid를 추출하고
    // 해당 uid가 관리자인지 확인
    private boolean checkAdmin(String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", ""); // "Bearer " 접두어 제거
        String uid = firebaseTokenVerifier.verifyIdToken(token);    // 토큰 검증 및 uid 추출
        return userService.isAdmin(uid);                            // uid에 대한 관리자 여부 확인
    }

    // 전체 FAQ 목록 조회 (권한 불필요)
    // 모든 사용자 접근 가능
    @GetMapping
    public ResponseEntity<List<FaqDTO>> getAllFaqs() {
        try {
            List<FaqDTO> faqs = faqService.getAllFaqs();  // 전체 FAQ 데이터 조회
            return ResponseEntity.ok(faqs);               // 성공 시 200 OK 응답
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // 서버 오류 시 500
        }
    }

    // 특정 FAQ 조회 (권한 불필요)
    @GetMapping("/{id}")
    public ResponseEntity<FaqDTO> getFaq(@PathVariable String id) {
        try {
            FaqDTO faq = faqService.getFaqById(id);  // 해당 ID FAQ 조회
            if (faq == null)
                return ResponseEntity.notFound().build(); // 존재하지 않을 경우 404
            return ResponseEntity.ok(faq);               // 성공 시 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // 예외 발생 시 500
        }
    }

    // FAQ 등록 (관리자만 가능)
    @PostMapping
    public ResponseEntity<String> addFaq(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FaqDTO faqDTO
    ) {
        try {
            if (!checkAdmin(authHeader)) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다."); // 권한 없음
            }

            String id = faqService.addFaq(faqDTO);   // FAQ 등록
            return ResponseEntity.ok(id);            // 등록된 FAQ ID 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // 서버 오류
        }
    }

    // FAQ 수정 (관리자만 가능)
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFaq(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody FaqDTO faqDTO
    ) {
        try {
            if (!checkAdmin(authHeader)) {
                return ResponseEntity.status(403).build(); // 관리자 아님
            }

            faqService.updateFaq(id, faqDTO);        // FAQ 수정 요청
            return ResponseEntity.ok().build();       // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // 예외 처리
        }
    }

    // FAQ 삭제 (관리자만 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id
    ) {
        try {
            if (!checkAdmin(authHeader)) {
                return ResponseEntity.status(403).build(); // 권한 없음
            }

            faqService.deleteFaq(id);               // FAQ 삭제 처리
            return ResponseEntity.ok().build();     // 성공 응답
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build(); // 서버 에러
        }
    }
}
