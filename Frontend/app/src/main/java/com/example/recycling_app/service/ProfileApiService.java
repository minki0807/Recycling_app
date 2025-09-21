package com.example.recycling_app.service;

import com.example.recycling_app.dto.AiRecognitionRecordDTO;
import com.example.recycling_app.dto.FaqDTO;
import com.example.recycling_app.dto.InquiryDTO;
import com.example.recycling_app.dto.LanguageDTO;
import com.example.recycling_app.dto.ProfileDTO;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
// import retrofit2.http.Header; // Interceptor 사용 시 필요 없음
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ProfileApiService {

    // GET 요청: "profile/{uid}" 경로로 특정 사용자의 프로필 정보 조회
    @GET("profile/{uid}")
    Call<ProfileDTO> getProfile(@Path("uid") String uid);

    // PUT 요청: "profile/{uid}" 경로로 사용자 프로필 전체 저장 또는 업데이트
    @PUT("profile/{uid}")
    Call<String> saveProfile(
            @Path("uid") String uid,
            @Body ProfileDTO profileDTO
    );

    // DELETE 요청: "profile/{uid}" 경로로 특정 사용자 계정 삭제
    @DELETE("profile/{uid}")
    Call<String> deleteUserAccount(@Path("uid") String uid);

    // POST 요청 (Multipart): "profile/{uid}/upload-image" 경로로 프로필 이미지 업로드
    @Multipart
    @POST("profile/{uid}/upload-image")
    Call<String> uploadImage(
            @Path("uid") String uid,
            @Part MultipartBody.Part file
    );

    // PATCH 요청: "profile/{uid}" 경로로 프로필 정보 부분 수정
    @PATCH("profile/{uid}")
    Call<String> updateProfileFields(
            @Path("uid") String uid,
            @Body Map<String, Object> updates
    );

    // PATCH 요청: "profile/{uid}/password" 경로로 사용자 비밀번호 변경
    @PATCH("profile/{uid}/password")
    Call<String> changePassword(
            @Path("uid") String uid,
            @Body Map<String, String> passwordUpdate
    );

    // GET 요청: "activity/{uid}" 경로로 특정 사용자의 AI 인식 활동 기록 조회
    @GET("activity/{uid}")
    Call<List<AiRecognitionRecordDTO>> getActivityRecords(@Path("uid") String uid);
    // GET 요청: "language/{uid}" 경로로 특정 사용자의 언어 설정 조회
    @GET("language/{uid}")
    Call<LanguageDTO> getLanguageSetting(@Path("uid") String uid);

    // POST 요청: "language/{uid}" 경로로 사용자 언어 설정 저장 또는 수정
    @POST("language/{uid}")
    Call<String> updateLanguageSetting(
            @Path("uid") String uid,
            @Body LanguageDTO languageDTO
    );

    // POST 요청: "inquiry" 경로로 1:1 문의 등록
    @POST("inquiry")
    Call<String> submitInquiry(@Body InquiryDTO inquiryDTO);

    // GET 요청: "inquiry/{uid}" 경로로 특정 사용자가 작성한 문의 목록 조회
    @GET("inquiry/{uid}")
    Call<List<InquiryDTO>> getInquiriesByUid(@Path("uid") String uid);

    // GET 요청: "faq" 경로로 전체 FAQ 목록 조회.
    @GET("faq")
    Call<List<FaqDTO>> getAllFaqs();
}