package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.LanguageDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

// 사용자 언어 설정 관련 비즈니스 로직 처리 서비스 클래스
@Service
public class LanguageService {

    private static final String COLLECTION_NAME = "users"; // Firestore 사용자 컬렉션명

    // 언어 설정 조회
    public LanguageDTO getLanguageSetting(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        var doc = db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("settings")
                .document("language")
                .get()
                .get();

        if (!doc.exists()) return new LanguageDTO("ko"); // 문서 없으면 기본값 "ko" 반환
        return doc.toObject(LanguageDTO.class);           // 문서 -> DTO 변환 후 반환
    }

    // 언어 설정 저장
    public void updateLanguageSetting(String uid, LanguageDTO languageDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("settings")
                .document("language")
                .set(languageDTO)
                .get(); // 저장 완료 대기
    }
}
