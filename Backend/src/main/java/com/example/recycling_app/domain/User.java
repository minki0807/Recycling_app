package com.example.recycling_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;            // 회원 식별자 (Firebase UID)
    private String email;          // 이메일(로그인 아이디)
    private String password;       // 암호화
    private String name;
    private String nickname;
    private String phoneNumber;
    private String region;
    private int age;
    private String gender;
    private Date createdAt;
    private Date updatedAt;
}