package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.AiRecognitionRecordDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// 사용자 AI 인식 활동 기록과 관련된 비즈니스 로직 처리 서비스 클래스
@Service
public class ActivityRecordService {
    private static final String COLLECTION_NAME = "activity_Records"; // Firestore에서 활동 기록이 저장된 컬렉션명

    // 특정 사용자(uid)의 AI 인식 활동 기록 리스트를 최신순으로 조회
    public List<AiRecognitionRecordDTO> getUserActivityRecords(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        // "activity_Records" 컬렉션 하위에 사용자별 문서(uid) 안에 "records" 서브컬렉션 참조
        CollectionReference recordsRef = db.collection(COLLECTION_NAME).document(uid).collection("records");

        // timestamp 필드를 기준으로 내림차순 정렬하여 조회
        ApiFuture<QuerySnapshot> query = recordsRef.orderBy("timestamp", Query.Direction.DESCENDING).get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        List<AiRecognitionRecordDTO> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            AiRecognitionRecordDTO record = doc.toObject(AiRecognitionRecordDTO.class); // 문서 데이터를 DTO로 변환
            result.add(record);
        }

        return result;
    }
}
