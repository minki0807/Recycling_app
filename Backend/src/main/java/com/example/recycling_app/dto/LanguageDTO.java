package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 언어 설정 정보를 담는 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDTO {
    private String language; // "ko", "en" 등
}
