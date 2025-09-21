package com.example.recycling_app.controller;

import com.example.recycling_app.dto.ReportRecordDTO;
import com.example.recycling_app.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 사용자 제보 기록과 관련된 HTTP 요청을 처리하는 컨트롤러 클래스
// 주요 기능: 제보 기록 저장 및 특정 사용자 제보 기록 조회
@RestController
@RequestMapping("/reports") // 기본 URL 경로: /reports
public class ReportController {

    @Autowired
    private ReportService reportService; // 제보 기록 서비스 의존성 주입

    // 특정 사용자(uid)의 제보 기록 저장 API
    @PostMapping("/{uid}")
    public ResponseEntity<String> saveReport(
            @PathVariable String uid,                 // 경로 변수로 전달되는 사용자 UID
            @RequestBody ReportRecordDTO reportDTO   // 저장할 제보 기록 데이터
    ) {
        try {
            String result = reportService.saveReportRecord(uid, reportDTO); // 제보 기록 저장 처리
            return ResponseEntity.ok(result);                               // 200 OK + 저장 결과 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("제보 기록 저장 실패"); // 500 서버 오류
        }
    }

    // 특정 사용자(uid)의 제보 기록 리스트 조회 API
    @GetMapping("/{uid}")
    public ResponseEntity<List<ReportRecordDTO>> getReports(
            @PathVariable String uid                  // 조회할 사용자 UID
    ) {
        try {
            List<ReportRecordDTO> records = reportService.getReportRecords(uid); // 사용자 제보 기록 조회
            return ResponseEntity.ok(records);                                  // 200 OK + 제보 리스트 반환
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();               // 500 서버 오류
        }
    }
}
