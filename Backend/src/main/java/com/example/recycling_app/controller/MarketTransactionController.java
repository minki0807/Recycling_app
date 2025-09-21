package com.example.recycling_app.controller;

import com.example.recycling_app.dto.MarketTransactionDTO;
import com.example.recycling_app.service.MarketTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// 거래 기록 관련 요청을 처리하는 REST 컨트롤러
// 사용자 거래 기록 저장
// 사용자 거래 기록 조회
@RestController
@RequestMapping("/market")
public class MarketTransactionController {

    @Autowired
    private MarketTransactionService transactionService; // 거래 관련 비즈니스 로직을 처리하는 서비스


    // 특정 사용자의 거래 기록 저장 요청을 처리하는 API 엔드포인트
    @PostMapping("/{uid}")
    public ResponseEntity<String> saveTransaction(@PathVariable String uid, @RequestBody MarketTransactionDTO dto) {
        try {
            String result = transactionService.saveTransaction(uid, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("거래 기록 저장 실패"); // 실패 시 500 응답
        }
    }

    // 특정 사용자의 거래 기록 전체를 조회하는 API 엔드포인트
    @GetMapping("/{uid}")
    public ResponseEntity<List<MarketTransactionDTO>> getTransactions(@PathVariable String uid) {
        try {
            List<MarketTransactionDTO> result = transactionService.getTransactions(uid);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 사용자의 판매 기록을 조회하는 API 엔드포인트
    @GetMapping("/{uid}/sales")
    public ResponseEntity<List<MarketTransactionDTO>> getSales(@PathVariable String uid) {
        try {
            List<MarketTransactionDTO> result = transactionService.getSales(uid);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 사용자의 구매 기록을 조회하는 API 엔드포인트
    @GetMapping("/{uid}/purchases")
    public ResponseEntity<List<MarketTransactionDTO>> getPurchases(@PathVariable String uid) {
        try {
            List<MarketTransactionDTO> result = transactionService.getPurchases(uid);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}