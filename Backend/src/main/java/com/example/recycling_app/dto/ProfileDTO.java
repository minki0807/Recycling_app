package com.example.recycling_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//사용자 프로필 정보를 담는 DTO (Data Transfer Object) 클래스
//클라이언트와 서버 간 데이터 전달에 사용됨
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private String name; //사용자의 이름
    private String email; //사용자의 이메일 주소
    private String gender; //사용자의 성별
    private int age; //사용자의 나이
    private String address; //사용자의 거주지 주소 또는 지역
    private String phoneNumber; //사용자의 전화번호
    private String profileImageUrl; //프로필 이미지의 URL (Firebase Storage 등 외부 저장소 경로)
    private String nickname; //사용자가 설정한 닉네임
    private boolean isProfilePublic; // 프로필 공개 여부 추가
    private String uid;
    private String region;           // 지역
    private boolean isGoogleUser;    // 구글 계정 사용자 여부
    private boolean googleaccount;    // 구글 계정 정보
}