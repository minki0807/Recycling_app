package com.example.recycling_app.repository;

import com.example.recycling_app.domain.Like;
import com.example.recycling_app.domain.Post;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class LikeRepository {
    private final Firestore firestore = FirestoreClient.getFirestore();

    public void save(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        DocumentReference likeRef = firestore.collection("likes").document(likeId);
        if (likeRef.get().get().exists())
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        Like like = Like.builder()
                .likeId(likeId)
                .postId(postId)
                .uid(uid)
                .likedAt(new Date())
                .build();
        likeRef.set(like).get();
    }

    public Optional<Like> findById(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        DocumentSnapshot doc = firestore.collection("likes").document(likeId).get().get();
        if (doc.exists()) {
            return Optional.ofNullable(doc.toObject(Like.class));
        }
        return Optional.empty();
    }

    public void deleteById(String postId, String uid) throws Exception {
        String likeId = postId + "_" + uid;
        firestore.collection("likes").document(likeId).delete().get();
    }

    // 내가 좋아요한 게시글 ID 목록 조회
    public List<String> findLikedPostIdsByUid(String uid) throws Exception {
        QuerySnapshot likeQ = firestore.collection("likes")
                .whereEqualTo("uid", uid)
                .get().get();
        return likeQ.getDocuments().stream()
                .map(doc -> doc.getString("postId"))
                .collect(Collectors.toList());
    }

    // 내가 좋아요한 게시글 목록 조회 (N+1 문제 해결)
    public List<Post> findPostsLikedByUser(String uid) throws Exception {
        // 1. 좋아요한 게시글 ID 목록을 효율적으로 가져옵니다.
        List<String> postIds = findLikedPostIdsByUid(uid);
        if (postIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 2. PostRepository의 findAllByIds 메서드를 사용하여 한 번에 모든 게시글을 가져옵니다.
        PostRepository postRepository = new PostRepository(); // 주의: Autowired가 아닌 경우 직접 생성
        return postRepository.findAllByIds(postIds);
    }
}
