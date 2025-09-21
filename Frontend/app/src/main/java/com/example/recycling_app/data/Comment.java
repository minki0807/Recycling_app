package com.example.recycling_app.data;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class Comment {

    @SerializedName("commentId")
    private String commentId;

    @SerializedName("postId")
    private String postId;

    @SerializedName("uid")
    private String uid;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("content")
    private String content;

    @SerializedName("parentId")
    private String parentId;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    @SerializedName("isDeleted")
    private boolean isDeleted;

    @SerializedName("deletedAt")
    private Date deletedAt;
    private List<Comment> replies;
    private int depth;

    // Getter와 Setter
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
}
