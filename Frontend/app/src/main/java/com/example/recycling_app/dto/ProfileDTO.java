package com.example.recycling_app.dto;

import com.google.gson.annotations.SerializedName;

public class ProfileDTO {
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("gender")
    private String gender;
    @SerializedName("age")
    private int age;
    @SerializedName("address")
    private String address;
    @SerializedName("phoneNumber")
    private String phoneNumber;
    @SerializedName("profileImageUrl")
    private String profileImageUrl;
    @SerializedName("nickname")
    private String nickname;
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

    public ProfileDTO() {
    }

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
    public void setUid(String uid) { this.uid = uid; }
    public void setRegion(String region) { this.region = region; }
    public void setGoogleaccount(boolean googleaccount) { this.googleaccount = googleaccount; } // 버그 수정
    public void setGoogleUser(boolean isGoogleUser) { this.isGoogleUser = isGoogleUser; } // 이름 변경
    public void setProfilePublic(boolean isProfilePublic) { this.isProfilePublic = isProfilePublic; } // 이름 변경
}