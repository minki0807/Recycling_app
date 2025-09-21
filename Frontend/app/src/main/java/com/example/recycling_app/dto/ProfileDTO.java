package com.example.recycling_app.dto;

import com.google.gson.annotations.SerializedName;

// 사용자 프로필 데이터를 담는 DTO
// 백엔드 서버의 프로필 데이터 구조와 동일하게 정의
// Retrofit 등을 통해 서버와 사용자 프로필 정보를 주고받을 때 사용
public class ProfileDTO {
    // JSON 필드 'name'과 매핑되는 사용자 이름
    @SerializedName("name")
    private String name;
    // JSON 필드 'email'과 매핑되는 사용자 이메일 주소
    @SerializedName("email")
    private String email;
    // JSON 필드 'gender'와 매핑되는 사용자 성별
    @SerializedName("gender")
    private String gender;
    // JSON 필드 'age'와 매핑되는 사용자 나이
    @SerializedName("age")
    private int age;
    // JSON 필드 'address'와 매핑되는 사용자 주소
    @SerializedName("address")
    private String address;
    // JSON 필드 'phoneNumber'와 매핑되는 사용자 전화번호
    @SerializedName("phoneNumber")
    private String phoneNumber;
    // JSON 필드 'profileImageUrl'과 매핑되는 프로필 사진 URL
    @SerializedName("profileImageUrl")
    private String profileImageUrl;
    // JSON 필드 'nickname'과 매핑되는 사용자 닉네임
    @SerializedName("nickname")
    private String nickname;
    // JSON 필드 'isProfilePublic'과 매핑되는 프로필 공개 여부 (true: 공개, false: 비공개)
    @SerializedName("isProfilePublic")
    private boolean isProfilePublic;

    @SerializedName("uid")
    private String uid;

    @SerializedName("region")
    private String region;

    @SerializedName("isGoogleUser")
    private boolean isGoogleUser;

    @SerializedName("googleaccount")
    private boolean googleaccount;

    // 기본 생성자:
    public ProfileDTO() {
    }

    // 모든 필드를 포함하는 생성자
    public ProfileDTO(String name, String email, String gender, int age, String address, String phoneNumber, String profileImageUrl, String nickname, boolean isProfilePublic,
                      String uid, String region, boolean isGoogleUser, boolean googleaccount) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.isProfilePublic = isProfilePublic;
        this.uid = uid;
        this.region = region;
        this.isGoogleUser = isGoogleUser;
        this.googleaccount = googleaccount;
    }

    // --- Getter 메서드 ---
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getNickname() { return nickname; }
    public boolean isProfilePublic() { return isProfilePublic; }
    public String getUid() { return uid; }
    public String getRegion() { return region; }
    public boolean isGoogleUser() { return isGoogleUser; }
    public boolean isGoogleaccount() { return googleaccount; }


    // --- Setter 메서드 ---
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setGender(String gender) { this.gender = gender; }
    public void setAge(int age) { this.age = age; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setProfilePublic(boolean profilePublic) { isProfilePublic = profilePublic; }

    public void setUid(String uid) { this.uid = uid; }
    public void setRegion(String region) { this.region = region; }
    public void setGoogleUser(boolean googleUser) { isGoogleUser = googleUser; }
    public void setGoogleaccount(boolean googleaccount) { googleaccount = googleaccount; }
}