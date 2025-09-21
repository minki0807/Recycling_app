package com.example.recycling_app.controller;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.dto.UserProfileDTO;
import com.example.recycling_app.exception.UnauthorizedException;
import com.example.recycling_app.service.CommunityService;
import com.example.recycling_app.service.UserService;
import com.example.recycling_app.util.FirebaseTokenVerifier;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;
    @Autowired
    private UserService userService;
    @Autowired
    private FirebaseTokenVerifier firebaseTokenVerifier;

    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Post post, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            post.setUid(verifiedUid);
            communityService.writePost(post);
            return ResponseEntity.status(201).body(Map.of("message", "게시글이 성공적으로 작성되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 전체 게시글 조회 (권한 검증 필요 없음)
    @GetMapping("/posts/all")
    public ResponseEntity<List<Post>> getAllPosts(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws Exception {
        String uid = null;
        if (authorizationHeader != null) {
            uid = firebaseTokenVerifier.verifyIdToken(authorizationHeader.substring(7));
        }
        List<Post> posts = communityService.getAllPosts(uid);
        return ResponseEntity.ok(posts);
    }

    // 카테고리별 게시글 조회 (권한 검증 필요 없음)
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getPostsByCategory(@RequestParam String category, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws Exception {
        String uid = null;
        if (authorizationHeader != null) {
            uid = firebaseTokenVerifier.verifyIdToken(authorizationHeader.substring(7));
        }
        List<Post> posts = communityService.getPosts(category, uid);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable String postId, @RequestBody Post post, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            communityService.updatePost(postId, post, verifiedUid);
            return ResponseEntity.ok(Map.of("message", "게시글이 성공적으로 수정되었습니다."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            communityService.deletePost(postId, verifiedUid);
            return ResponseEntity.ok(Map.of("message", "게시글이 성공적으로 삭제되었습니다."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 댓글 작성
    @PostMapping("/comments")
    public ResponseEntity<?> createComment(@RequestBody Comment comment, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            comment.setUid(verifiedUid);
            communityService.writeComment(comment);
            return ResponseEntity.status(201).body(Map.of("message", "댓글이 성공적으로 작성되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 특정 게시글 댓글 조회 (권한 검증 필요 없음)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String postId) throws Exception {
        List<Comment> comments = communityService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable String commentId, @RequestBody Map<String, String> updateData, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            communityService.updateComment(commentId, updateData.get("content"), verifiedUid);
            return ResponseEntity.ok(Map.of("message", "댓글이 성공적으로 수정되었습니다."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            communityService.deleteComment(commentId, verifiedUid);
            return ResponseEntity.ok(Map.of("message", "댓글이 성공적으로 삭제되었습니다."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 좋아요/취소 토글
    @PatchMapping("/posts/{postId}/like")
    public ResponseEntity<?> toggleLikes(@PathVariable String postId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = authorizationHeader.substring(7);
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
            int newLikesCount = communityService.toggleLikes(postId, verifiedUid);
            return ResponseEntity.ok(Map.of("message", "좋아요 상태가 변경되었습니다.", "likesCount", String.valueOf(newLikesCount)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증에 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }


    // 특정 게시글 상세 조회 (권한 검증 필요 없음)
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostDetail(@PathVariable String postId, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws Exception {
        String uid = null;
        if (authorizationHeader != null) {
            uid = firebaseTokenVerifier.verifyIdToken(authorizationHeader.substring(7));
        }
        Post post = communityService.getPost(postId, uid);
        return ResponseEntity.ok(post);
    }

    // 내가 작성한 게시글 조회
    @GetMapping("/me/posts")
    public ResponseEntity<List<Post>> getMyPosts(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        try {
            // "Bearer " 부분을 제외한 실제 토큰을 추출
            String idToken = authorizationHeader.substring(7);
            // Firebase 인증 토큰 검증 및 UID 추출
            String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);

            List<Post> posts = communityService.getMyPosts(verifiedUid);
            return ResponseEntity.ok(posts);

        } catch (FirebaseAuthException e) {
            // 토큰이 유효하지 않거나 만료된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            // 그 외 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 내가 댓글 단 게시글 조회 (중복 게시글 없이)
    @GetMapping("/me/commented-posts")
    public ResponseEntity<List<Post>> getPostsCommentedByMe(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        String idToken = authorizationHeader.substring(7);
        String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
        List<Post> posts = communityService.getPostsCommentedByUser(verifiedUid);
        return ResponseEntity.ok(posts);
    }

    // 내가 좋아요한 게시글 조회
    @GetMapping("/me/likes")
    public ResponseEntity<List<Post>> getMyLikedPosts(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        String idToken = authorizationHeader.substring(7);
        String verifiedUid = firebaseTokenVerifier.verifyIdToken(idToken);
        List<Post> likedPosts = communityService.getLikedPostsByUser(verifiedUid);
        return ResponseEntity.ok(likedPosts);
    }

    // 상대방 프로필 기본 정보 조회 (권한 검증 필요 없음)
    @GetMapping("/users/{uid}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String uid) throws Exception {
        UserProfileDTO profile = userService.getUserProfile(uid);
        return ResponseEntity.ok(profile);
    }

    // 상대방이 작성한 게시글 조회 (권한 검증 필요 없음)
    @GetMapping("/users/{uid}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable String uid) throws Exception {
        List<Post> posts = communityService.getUserPosts(uid);
        return ResponseEntity.ok(posts);
    }

    // 상대방이 작성한 댓글 목록 조회 (권한 검증 필요 없음)
    @GetMapping("/users/{uid}/comments")
    public ResponseEntity<List<Comment>> getUserComments(@PathVariable String uid) throws Exception {
        List<Comment> comments = communityService.getUserComments(uid);
        return ResponseEntity.ok(comments);
    }
}