package com.example.recycling_app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserProfileDTO {
    @JsonIgnore // 상대방 프로필 조회시 uid 안나오게 하기 위함
    private String uid;

    private String nickname;
    private String profileImageUrl;
    private int postCount;
    private int commentCount;
    private int likeCount;

    public UserProfileDTO(String uid, String nickname, String profileImageUrl,
                          int postCount, int commentCount, int likeCount) {
        this.uid = uid;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getPostCount() { return postCount; }
    public void setPostCount(int postCount) { this.postCount = postCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}