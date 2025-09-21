package com.example.recycling_app.data;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Post {

    @SerializedName("postId")
    private String postId;

    @SerializedName("uid")
    private String uid;

    @SerializedName("Nickname")
    private String nickname;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    @SerializedName("commentsCount")
    private int commentsCount;

    @SerializedName("likesCount")
    private int likesCount;

    @SerializedName("contents")
    private List<ContentBlock> contents;

    // 백엔드와 일치하도록 필드 추가 및 수정
    @SerializedName("deleted")
    private boolean deleted;

    @SerializedName("deletedAt")
    private Date deletedAt;

    @SerializedName("isLikedByCurrentUser") // JSON 필드명 일치
    private boolean likedByCurrentUser;

    // Getter와 Setter
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public List<ContentBlock> getContents() {
        return contents;
    }

    public void setContents(List<ContentBlock> contents) {
        this.contents = contents;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
