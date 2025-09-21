package com.example.recycling_app.service;

import com.example.recycling_app.Profile.customerservice.InquiryItem; // 문의 항목 데이터 모델 DTO
import java.util.List;                       // List 컬렉션
import retrofit2.Call;                       // Retrofit 비동기 통신 객체
import retrofit2.http.GET;                   // HTTP GET 요청 정의 어노테이션

// 1:1 문의 관련 백엔드 API와의 통신을 위한 인터페이스
// Retrofit 라이브러리가 이 인터페이스를 구현해서 실제 네트워크 요청 처리
public interface InquiryApiService {
    // GET 요청: "api/inquiries" 엔드포인트로 모든 1:1 문의 목록 가져오기
    // 백엔드에서 InquiryItem 객체 리스트 형식으로 응답 예상
    @GET("api/inquiries")

    Call<List<InquiryItem>> getAllInquiries(); // InquiryItem 리스트를 담은 Call 객체 반환

}