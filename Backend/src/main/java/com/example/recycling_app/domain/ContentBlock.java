package com.example.recycling_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlock {
    private String type; // text, image, video, file
    private String text;
    private  String mediaUrl; //이미지, 동영상 url
    private int order; // 본문 내 순서
}
