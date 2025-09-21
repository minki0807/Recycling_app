package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// 사용자 즐겨찾기 정보를 담는 DTO 클래스
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
    private String uid;         // 사용자 ID
    private String name;        // 즐겨찾기 이름 (품목명 또는 장소명)
    private String type;        // 즐겨찾기 종류 ("item" 또는 "place")
    private long timestamp;     // 등록 시간 (밀리초)
}
