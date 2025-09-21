package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 마켓 거래 정보를 담는 DTO 클래스
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketTransactionDTO {
    private String transactionId;       // 거래 ID
    private String title;               // 거래 제목
    private String description;         // 거래 상세 설명
    private String imageUrl;            // 상품 또는 거래 관련 이미지 URL
    private String transactionDate;     // 거래 일자 (예: "2025-07-21" 형식)
    private String status;              // 거래 상태 (예: "거래중", "완료" 등)
    private String transactionType;     // "SALE" 또는 "PURCHASE"로 거래 유형을 구분
    private String partnerUid;          // 상대방(판매자 또는 구매자)의 UID
}