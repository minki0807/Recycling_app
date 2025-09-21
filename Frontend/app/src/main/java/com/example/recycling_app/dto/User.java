package com.example.recycling_app.dto;

// 다른 사용자의 공개 프로필 정보를 담는 DTO
// 주로 다른 사용자의 닉네임, 지역, 프로필 이미지 URL 같은 기본적인 공개 정보를 나타낼 때 사용
public class User {
    // 사용자의 UID
    private String uid;
    // 사용자의 닉네임
    private String nickname;
    // 사용자의 지역 정보
    private String region;
    // 사용자의 프로필 이미지 URL
    private String profileImageUrl;

    // 기본 생성자:
    // JSON 데이터를 이 객체로 자동 변환(역직렬화) 시 라이브러리에서 내부적으로 사용
    public User(){}

    // --- Getter 메서드 ---
    // UID 반환
    public String getUid() {
        return uid;
    }

    // 닉네임 반환
    public String getNickname(){
        return nickname;
    }

    // 지역 반환
    public String getRegion(){
        return region;
    }

    // 프로필 이미지 URL 반환
    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    // --- Setter 메서드 ---
    // UID 설정
    public void setUid(String uid) {
        this.uid = uid;
    }

    // 닉네임 설정
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // 지역 설정
    public void setRegion(String region) {
        this.region = region;
    }

    // 프로필 이미지 URL 설정
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}