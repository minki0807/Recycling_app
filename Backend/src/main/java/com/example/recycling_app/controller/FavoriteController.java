package com.example.recycling_app.controller;

import com.example.recycling_app.dto.FavoriteDTO;
import com.example.recycling_app.service.FavoriteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 즐겨찾기 관련 HTTP 요청을 처리하는 컨트롤러 클래스
// 즐겨찾기 추가, 조회, 삭제 기능 제공
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;  // 즐겨찾기 관련 비즈니스 로직 처리 서비스

    // 즐겨찾기 추가
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteDTO favorite) {
        try {
            String result = favoriteService.addFavorite(favorite);  // 즐겨찾기 추가
            return ResponseEntity.ok(result);                      // 성공 시 200 OK + 결과 메시지
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("즐겨찾기 추가 실패"); // 예외 시 500 Internal Server Error
        }
    }

    // 즐겨찾기 조회 (정렬 옵션: timestamp 또는 name)
    @GetMapping("/{uid}")
    public ResponseEntity<?> getFavorites(
            @PathVariable String uid,                                 // 사용자 고유 UID
            @RequestParam(defaultValue = "timestamp") String sortBy   // 정렬 기준 (기본값: timestamp)
    ) {
        try {
            List<FavoriteDTO> favorites = favoriteService.getFavorites(uid, sortBy); // 즐겨찾기 목록 조회
            return ResponseEntity.ok(favorites);                                     // 성공 시 200 OK + 목록 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("즐겨찾기 조회 실패"); // 예외 발생 시 500
        }
    }

    // 즐겨찾기 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteFavorite(
            @RequestParam String uid,   // 사용자 UID
            @RequestParam String name,  // 즐겨찾기 이름
            @RequestParam String type   // 즐겨찾기 타입 (예: category, product 등)
    ) {
        try {
            String result = favoriteService.deleteFavorite(uid, name, type); // 즐겨찾기 삭제 요청
            return ResponseEntity.ok(result);                                // 성공 시 200 OK + 결과 메시지
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("즐겨찾기 삭제 실패"); // 예외 발생 시 500
        }
    }
}
