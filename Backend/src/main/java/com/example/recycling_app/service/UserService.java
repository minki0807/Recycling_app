package com.example.recycling_app.service;

import com.example.recycling_app.dto.UserProfileDTO;
import com.example.recycling_app.exception.NotFoundException;
import com.example.recycling_app.repository.CommentRepository;
import com.example.recycling_app.repository.LikeRepository;
import com.example.recycling_app.repository.PostRepository;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;


// 사용자 정보를 Firestore에서 조회하고 권한(역할)을 판별하는 서비스 클래스
@Service
public class UserService {

    private static final String COLLECTION_NAME = "users"; // 사용자 정보를 저장한 Firestore 컬렉션 이름
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    // 생성자 주입 추가
    public UserService(PostRepository postRepository, CommentRepository commentRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    // UID에 해당하는 사용자의 역할(role) 정보를 Firestore에서 조회
    public String getUserRole(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore(); // Firestore 인스턴스 가져오기

        // 해당 UID의 문서를 비동기적으로 조회하고 결과를 동기적으로 대기
        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get();

        // 문서가 존재하지 않으면 null 반환
        if (!doc.exists()) {
            return null;
        }

        // 문서의 'role' 필드 값을 문자열로 반환 (예: "admin", "user")
        return doc.getString("role");
    }

    // UID 사용자가 관리자(admin)인지 확인하는 메서드
    public boolean isAdmin(String uid) throws ExecutionException, InterruptedException {
        String role = getUserRole(uid);    // 사용자의 역할 가져오기
        return "admin".equals(role);       // 역할이 "admin"인지 비교 후 결과 반환
    }

    // 사용자 프로필 조회 (게시글, 댓글, 좋아요 수)
    public UserProfileDTO getUserProfile(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        DocumentSnapshot doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .get();

        if (!doc.exists()) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        String nickname = doc.getString("nickname");
        String profileImageUrl = doc.getString("profileImageUrl");

        int postCount = 0, commentCount = 0, likeCount = 0;

        try {
            postCount = postRepository.findByUidAndDeletedFalse(uid).size();
            commentCount = commentRepository.findByUidAndDeletedFalse(uid).size();
            likeCount = likeRepository.findPostsLikedByUser(uid).size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new UserProfileDTO(uid, nickname, profileImageUrl, postCount, commentCount, likeCount);
    }
}
