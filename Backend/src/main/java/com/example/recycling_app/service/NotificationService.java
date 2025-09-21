package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.NotificationSettingDTO;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private static final String COLLECTION_NAME = "users";

    // 사용자 알림 설정 조회
    // Firestore 'users/{uid}/settings/notifications' 문서에서 알림 설정 정보를 가져옴
    public NotificationSettingDTO getNotificationSetting(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION_NAME) // 'users' 컬렉션에 접근
                .document(uid) // 특정 UID의 문서에 접근
                .collection("settings") // 'settings' 하위 컬렉션에 접근
                .document("notifications") // 'notifications' 문서에 접근
                .get() // 문서 데이터를 비동기적으로 가져옴
                .get() // 비동기 작업 결과를 동기적으로 기다림
                .toObject(NotificationSettingDTO.class); // 결과를 DTO 객체로 변환
    }

    // 사용자 알림 설정 업데이트
    // Firestore 'users/{uid}/settings/notifications' 문서에 알림 설정 정보를 저장 또는 덮어씀
    public void updateNotificationSetting(String uid, NotificationSettingDTO settingDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME) // 'users' 컬렉션에 접근
                .document(uid) // 특정 UID의 문서에 접근
                .collection("settings") // 'settings' 하위 컬렉션에 접근
                .document("notifications") // 'notifications' 문서에 접근
                .set(settingDTO) // DTO 객체를 문서 데이터로 설정 (덮어쓰기)
                .get(); // 비동기 작업 결과를 동기적으로 기다림
    }
}