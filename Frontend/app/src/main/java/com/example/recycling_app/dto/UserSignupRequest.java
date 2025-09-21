package com.example.recycling_app.dto;

public class UserSignupRequest {
    public String email;
    public String name;
    public String phoneNumber;
    public int age;
    public String gender;
    public String region;
    public boolean isGoogleUser;

    public String uid;
    public String nick_name;

    public UserSignupRequest(String email, String name, String phoneNumber, int age, String gender, String region, boolean isGoogleUser, String uid, String nickname) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.gender = gender;
        this.region = region;
        this.isGoogleUser = isGoogleUser;
        this.uid = uid;
        this.nick_name = nickname;
    }
}