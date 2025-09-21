package com.example.recycling_app.service;

import com.example.recycling_app.BuildConfig;
import com.example.recycling_app.data.Comment;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.dto.ProfileDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunityApiService {

    private static CommunityApiService instance;
    private ApiService apiService;
    private FirebaseAuth mAuth;

    private CommunityApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.apiService = retrofit.create(ApiService.class);
        this.mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized CommunityApiService getInstance() {
        if (instance == null) {
            instance = new CommunityApiService();
        }
        return instance;
    }

    private String getAuthorizationHeader() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 비동기 호출을 동기적으로 변환
            String idToken = user.getIdToken(false).getResult().getToken();
            return "Bearer " + idToken;
        }
        return null;
    }

    // getAllPosts, getPosts, getPostDetail 메서드에도 authHeader를 전달하도록 수정
    public void getPosts(String category, Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        apiService.getPosts(category, authHeader).enqueue(callback);
    }

    public void writePost(Post post, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.createPost(authHeader, post).enqueue(callback);
        }
    }

    public void getPostById(String postId, Callback<Post> callback) {
        String authHeader = getAuthorizationHeader();
        apiService.getPostDetail(postId, authHeader).enqueue(callback);
    }

    public void createComment(Comment comment, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.createComment(authHeader, comment).enqueue(callback);
        }
    }

    public void getComments(String postId, Callback<List<Comment>> callback) {
        apiService.getComments(postId).enqueue(callback);
    }

    public void getAllPosts(Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        apiService.getAllPosts(authHeader).enqueue(callback);
    }

    public void updatePost(String postId, String uid, Post post, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.updatePost(postId, authHeader, post).enqueue(callback);
        }
    }

    public void deletePost(String postId, String uid, Callback<Void> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.deletePost(postId, authHeader).enqueue(callback);
        }
    }

    public void toggleLikes(String postId, String uid, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.toggleLikes(postId, authHeader).enqueue(callback);
        }
    }

    public void updateComment(String commentId, String uid, String content, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            Map<String, String> body = Map.of("content", content);
            apiService.updateComment(commentId, authHeader, body).enqueue(callback);
        }
    }

    public void deleteComment(String commentId, String uid, Callback<Void> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.deleteComment(commentId, authHeader).enqueue(callback);
        }
    }

    // getProfile 메서드에 authHeader 파라미터 추가
    public void getProfile(String uid, Callback<ProfileDTO> callback) {
        String authHeader = getAuthorizationHeader();
        apiService.getProfile(uid, authHeader).enqueue(callback);
    }

    // MypageActivity에서 사용되는 메서드들 추가
    public void getMyPosts(String uid, Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getMyPosts(authHeader).enqueue(callback);
        }
    }

    public void getPostsCommentedByMe(String uid, Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getPostsCommentedByMe(authHeader).enqueue(callback);
        }
    }

    public void getMyLikedPosts(String uid, Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getMyLikedPosts(authHeader).enqueue(callback);
        }
    }

    // authorUid 인자를 직접 사용하여 getUserPosts를 호출합니다.
    public void getUserPosts(String uid, Callback<List<Post>> callback) {
        // 서버 API에 권한 검증이 필요 없다고 되어 있으므로 authHeader는 불필요
        apiService.getUserPosts(uid).enqueue(callback);
    }
}
