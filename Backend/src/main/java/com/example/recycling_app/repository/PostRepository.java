package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Post;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class PostRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    // 게시글 저장
    public void save(Post post) throws Exception {
        String id = post.getPostId() != null ? post.getPostId() : UUID.randomUUID().toString();
        post.setPostId(id);
        firestore.collection("posts").document(id).set(post).get();
    }

    // 게시글 삭제 (논리적 삭제)
    public void delete(String postId) throws Exception {
        firestore.collection("posts").document(postId).update("deleted", true, "deletedAt", new Date()).get();
    }

    public Optional<Post> findById(String postId) throws Exception {
        DocumentSnapshot doc = firestore.collection("posts").document(postId).get().get();
        if(doc.exists()) {
            Post post = doc.toObject(Post.class);
            if(post!= null && !post.isDeleted()){
                return Optional.of(post);
            }
        }
        return Optional.empty();
    }

    // 전체 게시글 조회 (deleted = false)
    public List<Post> findAll() throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("deleted", false)
                .get().get();
        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    // 카테고리별 조회 (deleted = false)
    public List<Post> findByCategory(String category) throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("category", category)
                .whereEqualTo("deleted", false)
                .get().get();
        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    public void incrementCommentCount(String postId) throws ExecutionException, InterruptedException {
        DocumentReference postRef = firestore.collection("posts").document(postId);
        postRef.update("commentsCount", FieldValue.increment(1)).get();
    }

    // UID 기준 작성 게시글 조회 (deleted = false)
    public List<Post> findByUidAndDeletedFalse(String uid) throws Exception {
        QuerySnapshot qs = firestore.collection("posts")
                .whereEqualTo("uid", uid)
                .whereEqualTo("deleted", false)
                .get().get();

        List<Post> result = new ArrayList<>();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            result.add(doc.toObject(Post.class));
        }
        return result;
    }

    // ID 리스트로 게시글 조회 (whereIn 사용, 최대 10개)
    public List<Post> findAllByIds(List<String> postIds) throws ExecutionException, InterruptedException {
        if (postIds == null || postIds.isEmpty()) {
            return new ArrayList<>();
        }
        // Firestore의 whereIn 쿼리 한계 때문에 10개씩 끊어서 처리
        List<List<String>> partitionedIds = com.google.common.collect.Lists.partition(postIds, 10);
        List<Post> result = new ArrayList<>();

        for (List<String> ids : partitionedIds) {
            QuerySnapshot qs = firestore.collection("posts")
                    .whereIn(com.google.cloud.firestore.FieldPath.documentId(), ids)
                    .whereEqualTo("deleted", false)
                    .get().get();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                result.add(doc.toObject(Post.class));
            }
        }
        return result;
    }
}
