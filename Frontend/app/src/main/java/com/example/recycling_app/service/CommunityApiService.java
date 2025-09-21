package com.example.recycling_app.service;

import static android.content.ContentValues.TAG;

import android.util.Log;

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
            try {
                // await()는 Main Thread에서 호출하면 안 되지만, 이 경우에만 예외적으로 처리
                // 실제 앱에서는 AsyncTask나 코루틴 등을 사용하여 비동기 처리 필요
                String idToken = user.getIdToken(true).getResult().getToken();
                return "Bearer " + idToken;
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Firebase ID Token 획득 실패", e);
                return null;
            }
        }
        return null;
    }

    // 게시글 목록 조회
    public void getPosts(String category, Callback<List<Post>> callback) {
        apiService.getPosts(category, getAuthorizationHeader()).enqueue(callback);
    }

    // 게시글 상세 조회
    public void getPostById(String postId, Callback<Post> callback) {
        apiService.getPostById(postId, getAuthorizationHeader()).enqueue(callback);
    }

    public void writePost(Post post, Callback<Map<String, String>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.createPost(authHeader, post).enqueue(callback);
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

    // 내가 작성한 게시글 조회
    public void getMyPosts(Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getMyPosts(authHeader).enqueue(callback);
        } else {
            Log.e(TAG, "Authorization 헤더가 없어 API 호출 불가");
            // callback.onFailure()를 호출하여 호출자에게 실패를 알림
            callback.onFailure(null, new IllegalStateException("Authentication token is missing."));
        }
    }

    // 내가 댓글 단 게시글 조회
    public void getPostsCommentedByMe(Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getPostsCommentedByMe(authHeader).enqueue(callback);
        } else {
            Log.e(TAG, "Authorization 헤더가 없어 API 호출 불가");
            callback.onFailure(null, new IllegalStateException("Authentication token is missing."));
        }
    }

    // 내가 좋아요한 게시글 조회
    public void getMyLikedPosts(Callback<List<Post>> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.getMyLikedPosts(authHeader).enqueue(callback);
        } else {
            Log.e(TAG, "Authorization 헤더가 없어 API 호출 불가");
            callback.onFailure(null, new IllegalStateException("Authentication token is missing."));
        }
    }

    // 상대방 프로필 조회
    public void getProfile(String uid, Callback<ProfileDTO> callback) {
        // 상대방 프로필 조회는 로그인 여부와 관계없으므로 인증 헤더를 보내지 않음 (백엔드 로직에 맞춤)
        apiService.getProfile(uid, getAuthorizationHeader()).enqueue(callback);
    }

    // 상대방 게시글 조회
    public void getUserPosts(String uid, Callback<List<Post>> callback) {
        apiService.getUserPosts(uid).enqueue(callback);
    }

    public void createComment(String postId, String content, String uid, Callback<Comment> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            apiService.createComment(postId, content, authHeader).enqueue(callback);
        }
    }

    public void updateComment(String commentId, String content, String uid, Callback<Void> callback) {
        String authHeader = getAuthorizationHeader();
        if (authHeader != null) {
            Map<String, String> body = Map.of("content", content);
            apiService.updateComment(commentId, authHeader, body).enqueue(callback);
        }
    }
}
