package com.example.recycling_app.dto.market;

import lombok.Data;
import java.util.List;

@Data
public class ProductDto {
    private String productId; // Firestore 문서의 고유 ID
    private String uid; // 판매자 ID
    private String productName; // 제품 이름
    private String productDescription; // 제품 설명
    private int price; // 제품 가격
    private List<String> images; // 이미지 URL 목록
    private long createdAt; // 생성 시간
    private String transactionType; // 거래 방식 (판매하기, 나눔하기)
}