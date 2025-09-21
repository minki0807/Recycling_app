package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Comment;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommentRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public void save(Comment comment) throws Exception {
        String id = comment.getCommentId() != null ? comment.getCommentId() : UUID.randomUUID().toString();
        comment.setCommentId(id);
        firestore.collection("comments").document(id).set(comment).get();
    }

    // 댓글 삭제 (논리적 삭제)
    public void delete(String commentId) throws Exception {
        firestore.collection("comments").document(commentId).update("deleted", true, "deletedAt", new Date()).get();
    }

    public Optional<Comment> findById(String commentId) throws Exception {
        DocumentSnapshot doc = firestore.collection("comments").document(commentId).get().get();
        if(doc.exists()) {
            Comment comment = doc.toObject(Comment.class);
            if(comment !=null && !comment.isDeleted()) {
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    // 특정 게시글의 모든 댓글(논리적 삭제되지 않은)을 조회합니다.
    // 1차 댓글과 대댓글을 모두 포함합니다.
    public List<Comment> findByPostIdAndDeletedFalse(String postId) throws Exception {
        Query query = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .whereEqualTo("deleted", false);

        QuerySnapshot qs = query.get().get();
        List<Comment> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Comment.class));
        }
        return result;
    }

    // 내 작성 댓글 모두 조회 (deleted=false)
    public List<Comment> findByUidAndDeletedFalse(String uid) throws Exception {
        QuerySnapshot qs = firestore.collection("comments")
                .whereEqualTo("uid", uid)
                .whereEqualTo("deleted", false)
                .get().get();

        List<Comment> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Comment.class));
        }
        return result;
    }

    // 내가 댓글 단 게시글 ID 목록 (distinct postId) 조회
    public List<String> findDistinctPostIdsByUidAndDeletedFalse(String uid) throws Exception {
        QuerySnapshot qs = firestore.collection("comments")
                .whereEqualTo("uid", uid)
                .whereEqualTo("deleted", false)
                .select("postId")
                .get().get();

        List<String> postIds = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            String postId = doc.getString("postId");
            if (postId != null && !postIds.contains(postId)) {
                postIds.add(postId);
            }
        }
        return postIds;
    }
}
