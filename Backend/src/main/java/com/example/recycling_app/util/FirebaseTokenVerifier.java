package com.example.recycling_app.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

// Firebase ID 토큰을 검증하고 UID를 추출하는 유틸리티 클래스
@Component
public class FirebaseTokenVerifier {

    // 전달받은 Firebase ID 토큰을 검증하고 사용자 UID 반환
    public String verifyIdToken(String idToken) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken.getUid();
    }
}