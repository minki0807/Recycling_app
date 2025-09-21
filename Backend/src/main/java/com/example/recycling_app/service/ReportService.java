package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.ReportRecordDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


// 사용자 제보 기록을 저장하고 조회하는 서비스 클래스
@Service
public class ReportService {

    private static final String COLLECTION_NAME = "reportRecords"; // 서브 컬렉션 이름

    // 제보 기록 저장
    public String saveReportRecord(String uid, ReportRecordDTO reportDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        reportDTO.setTimestamp(System.currentTimeMillis()); // 현재 시간 기록

        ApiFuture<WriteResult> future = db.collection("users")
                .document(uid)
                .collection(COLLECTION_NAME)
                .document() // 자동 생성 ID
                .set(reportDTO);

        future.get(); // 저장 완료 대기
        return "제보 기록 저장 완료";
    }

    // 제보 기록 조회 (최신순)
    public List<ReportRecordDTO> getReportRecords(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection("users")
                .document(uid)
                .collection(COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ReportRecordDTO> records = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            records.add(doc.toObject(ReportRecordDTO.class)); // DTO로 변환 후 리스트에 추가
        }

        return records;
    }
}
