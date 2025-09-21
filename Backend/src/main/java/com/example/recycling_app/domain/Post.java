package com.example.recycling_app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Post {
    private String postId;
    private String uid;
    private String nickname;
    private String title;
    private String category;       // 예: 분리수거, 업사이클링, Q&A 등
    private List<ContentBlock> contents;     // 본문: 텍스트/이미지/동영상 혼합 배열
    private Date createdAt;
    private Date updatedAt;
    private boolean deleted;
    private Date deletedAt;
    private int likesCount;
    private int commentsCount;

    // 클라이언트에게 좋아요 상태를 전달하기 위한 필드. DB에 저장하지 않음.
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean LikedByCurrentUser;
}
