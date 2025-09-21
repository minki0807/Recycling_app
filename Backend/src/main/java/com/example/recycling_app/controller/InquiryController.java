package com.example.recycling_app.controller;

import com.example.recycling_app.dto.InquiryDTO;
import com.example.recycling_app.service.InquiryService;
import com.example.recycling_app.util.FirebaseTokenVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 사용자의 문의 등록 및 조회, 관리자의 답변 등록/수정/삭제 기능 제공
@RestController
@RequestMapping("/inquiry") // 기본 URL 경로: /inquiry
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;                // 문의 비즈니스 로직 처리 서비스

    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier;  // Firebase 인증 토큰 검증기

    // 문의 등록 (사용자용)
    @PostMapping
    public ResponseEntity<String> submitInquiry(
            @RequestBody InquiryDTO inquiryDTO,                     // 등록할 문의 데이터
            @RequestHeader("Authorization") String authHeader       // Firebase 인증 토큰
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");       // "Bearer " 제거
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(token); // 인증된 UID 추출

            if (!verifiedUid.equals(inquiryDTO.getUid())) {         // 요청 UID와 토큰 UID 불일치 시
                return ResponseEntity.status(403).body("권한이 없습니다.");
            }

            String result = inquiryService.saveInquiry(inquiryDTO); // 문의 저장 처리
            return ResponseEntity.ok(result);                       // 200 OK + 저장된 문서 ID

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("문의 저장 실패"); // 서버 오류
        }
    }

    // 특정 사용자(uid)의 문의 목록 조회 (사용자용)
    @GetMapping("/{uid}")
    public ResponseEntity<?> getInquiriesByUid(@PathVariable String uid) {
        try {
            List<InquiryDTO> inquiryList = inquiryService.getInquiesByUid(uid); // uid로 문의 조회
            return ResponseEntity.ok(inquiryList);                              // 200 OK + 문의 리스트
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("문의 조회 실패");
        }
    }

    // 전체 문의 목록 조회 (관리자용)
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllInquiries() {
        try {
            List<InquiryDTO> inquiryList = inquiryService.getAllInquiries(); // 모든 문의 조회
            return ResponseEntity.ok(inquiryList);                           // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("전체 문의 조회 실패");
        }
    }

    // 문의에 답변 등록 (관리자용)
    @PatchMapping("/admin/answer/{docId}")
    public ResponseEntity<String> addAnswerToInquiry(
            @PathVariable String docId,                            // 문의 문서 ID
            @RequestBody Map<String, String> request               // 요청 본문: {"answer": "..."}
    ) {
        try {
            String answer = request.get("answer");                 // 답변 내용 추출
            inquiryService.addAnswer(docId, answer);               // 답변 등록
            return ResponseEntity.ok("답변 등록 완료");        // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("답변 등록 실패");
        }
    }

    // 문의에 등록된 답변 수정 (관리자용)
    @PatchMapping("/admin/answer/edit/{docId}")
    public ResponseEntity<String> editAnswer(
            @PathVariable String docId,
            @RequestBody Map<String, String> request                // {"answer": "수정할 답변"}
    ) {
        try {
            String newAnswer = request.get("answer");
            inquiryService.editAnswer(docId, newAnswer);           // 답변 수정
            return ResponseEntity.ok("답변 수정 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("답변 수정 실패");
        }
    }

    // 문의에 등록된 답변 삭제 (관리자용)
    @PatchMapping("/admin/answer/delete/{docId}")
    public ResponseEntity<String> deleteAnswer(@PathVariable String docId) {
        try {
            inquiryService.deleteAnswer(docId);                    // 답변 삭제
            return ResponseEntity.ok("답변 삭제 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("답변 삭제 실패");
        }
    }

    // 문의 내용 수정 (사용자용)
    @PatchMapping("/edit/{docId}")
    public ResponseEntity<String> editInquiry(
            @PathVariable String docId,
            @RequestBody Map<String, String> request,               // {"title": "...", "content": "..."}
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String uid = firebaseTokenVerifier.verifyIdToken(token); // UID 추출

            String newTitle = request.get("title");                // 수정할 제목
            String newContent = request.get("content");            // 수정할 본문

            boolean success = inquiryService.editInquiry(docId, uid, newTitle, newContent);

            if (!success) {
                return ResponseEntity.status(403).body("수정 권한이 없습니다.");
            }

            return ResponseEntity.ok("문의 수정 완료");             // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("문의 수정 실패");
        }
    }

    // 문의 삭제 (사용자용)
    @DeleteMapping("/delete/{docId}")
    public ResponseEntity<String> deleteInquiry(
            @PathVariable String docId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            String uid = firebaseTokenVerifier.verifyIdToken(token); // UID 추출

            boolean success = inquiryService.deleteInquiry(docId, uid);

            if (!success) {
                return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
            }

            return ResponseEntity.ok("문의 삭제 완료");              // 200 OK
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("문의 삭제 실패");
        }
    }
}
