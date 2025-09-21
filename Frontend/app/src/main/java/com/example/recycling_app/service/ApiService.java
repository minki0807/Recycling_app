package com.example.recycling_app.service;

import com.example.recycling_app.data.Comment;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.dto.ProfileDTO;
import com.example.recycling_app.dto.RecycleData;
import com.example.recycling_app.dto.User;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 백엔드 API와의 통신을 위한 메서드 정의 인터페이스
 * Retrofit 라이브러리가 이 인터페이스를 구현하여 실제 네트워크 요청 처리
 */
public interface ApiService {

    // --- 사용자 정보 관련 API ---
    @GET("/user/info")
    Call<User> getUserInfo(@Header("Authorization") String authorizationHeader);

    // --- 커뮤니티 관련 API ---

    /**
     * 게시글 작성
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @param post 작성할 게시글 객체
     * @return 서버 응답 메시지
     */
    @POST("/community/posts")
    Call<Map<String, String>> createPost(@Header("Authorization") String authorizationHeader, @Body Post post);

    /**
     * 전체 게시글 조회
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입, 로그인 사용자용)
     * @return 게시글 목록
     */
    @GET("/community/posts/all")
    Call<List<Post>> getAllPosts(@Header("Authorization") String authorizationHeader);

    /**
     * 카테고리별 게시글 조회
     * @param category 조회할 카테고리
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입, 로그인 사용자용)
     * @return 해당 카테고리의 게시글 목록
     */
    @GET("/community/posts")
    Call<List<Post>> getPosts(@Query("category") String category, @Header("Authorization") String authorizationHeader);

    /**
     * 특정 게시글 상세 조회
     * @param postId 조회할 게시글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입, 로그인 사용자용)
     * @return 게시글 상세 정보
     */
    @GET("/community/posts/{postId}")
    Call<Post> getPostDetail(@Path("postId") String postId, @Header("Authorization") String authorizationHeader);

    /**
     * 게시글 수정
     * @param postId 수정할 게시글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @param post 수정할 게시글 객체
     * @return 서버 응답 메시지
     */
    @PUT("/community/posts/{postId}")
    Call<Map<String, String>> updatePost(@Path("postId") String postId, @Header("Authorization") String authorizationHeader, @Body Post post);

    /**
     * 게시글 삭제 (논리적 삭제)
     * @param postId 삭제할 게시글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 성공 여부
     */
    @DELETE("/community/posts/{postId}")
    Call<Void> deletePost(@Path("postId") String postId, @Header("Authorization") String authorizationHeader);

    /**
     * 좋아요/취소 토글
     * @param postId 좋아요/취소할 게시글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 변경된 좋아요 수
     */
    @PATCH("/community/posts/{postId}/like")
    Call<Map<String, String>> toggleLikes(@Path("postId") String postId, @Header("Authorization") String authorizationHeader);

    /**
     * 댓글 작성
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @param comment 작성할 댓글 객체
     * @return 서버 응답 메시지
     */
    @POST("/community/comments")
    Call<Map<String, String>> createComment(@Header("Authorization") String authorizationHeader, @Body Comment comment);

    /**
     * 댓글 수정
     * @param commentId 수정할 댓글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @param updateData 수정할 내용
     * @return 서버 응답 메시지
     */
    @PUT("/community/comments/{commentId}")
    Call<Map<String, String>> updateComment(@Path("commentId") String commentId, @Header("Authorization") String authorizationHeader, @Body Map<String, String> updateData);

    /**
     * 댓글 삭제 (논리적 삭제)
     * @param commentId 삭제할 댓글 ID
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 성공 여부
     */
    @DELETE("/community/comments/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId, @Header("Authorization") String authorizationHeader);

    /**
     * 특정 게시글의 모든 댓글 조회
     * @param postId 댓글을 조회할 게시글 ID
     * @return 댓글 목록
     */
    @GET("/community/posts/{postId}/comments")
    Call<List<Comment>> getComments(@Path("postId") String postId);

    /**
     * 내가 작성한 게시글 조회
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 내가 작성한 게시글 목록
     */
    @GET("/community/me/posts")
    Call<List<Post>> getMyPosts(@Header("Authorization") String authorizationHeader);

    /**
     * 내가 댓글 단 게시글 조회
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 내가 댓글 단 게시글 목록
     */
    @GET("/community/me/commented-posts")
    Call<List<Post>> getPostsCommentedByMe(@Header("Authorization") String authorizationHeader);

    /**
     * 내가 좋아요한 게시글 조회
     * @param authorizationHeader Firebase ID 토큰 (Bearer 타입)
     * @return 내가 좋아요한 게시글 목록
     */
    @GET("/community/me/likes")
    Call<List<Post>> getMyLikedPosts(@Header("Authorization") String authorizationHeader);

    // --- 프로필 관련 API ---
    @GET("/profile/{uid}")
    Call<ProfileDTO> getProfile(@Path("uid") String uid, @Header("Authorization") String authorizationHeader);

    // 나머지 ProfileApiService 메서드들도 필요에 따라 여기에 추가
}
