package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.InquiryDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// 사용자 문의 관련 비즈니스 로직 처리 서비스 클래스
@Service
public class InquiryService {
    private static final String COLLECTION_NAME = "inquiries"; // Firestore 문의 컬렉션명

    // 문의 저장
    public String saveInquiry(InquiryDTO inquiry) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(inquiry);
        return "문의 저장 완료 (ID: " + future.get().getId() + ")";
    }

    // 특정 사용자(uid)의 문의 목록 조회
    public List<InquiryDTO> getInquiesByUid(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME).whereEqualTo("uid", uid).get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        List<InquiryDTO> inquiries = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            inquiries.add(doc.toObject(InquiryDTO.class)); // 문서 -> DTO 변환
        }
        return inquiries;
    }

    // 모든 문의 조회 (관리자용)
    public List<InquiryDTO> getAllInquiries() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        List<InquiryDTO> inquiries = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            inquiries.add(doc.toObject(InquiryDTO.class));
        }
        return inquiries;
    }

    // 문의 답변 추가 (관리자 전용)
    public void addAnswer(String docId, String answer) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> update = new HashMap<>();
        update.put("answer", answer);

        db.collection(COLLECTION_NAME)
                .document(docId)
                .update(update)
                .get(); // 업데이트 완료 대기
    }

    // 문의 답변 수정 (관리자 전용)
    public void editAnswer(String docId, String newAnswer) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> update = new HashMap<>();
        update.put("answer", newAnswer);

        db.collection(COLLECTION_NAME)
                .document(docId)
                .update(update)
                .get();
    }

    // 문의 답변 삭제 (관리자 전용)
    public void deleteAnswer(String docId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> update = new HashMap<>();
        update.put("answer", null); // 답변 필드 삭제

        db.collection(COLLECTION_NAME)
                .document(docId)
                .update(update)
                .get();
    }

    // 문의 수정 (문의 작성자만 가능)
    public boolean editInquiry(String docId, String uid, String newTitle, String newContent) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(docId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists() || !uid.equals(snapshot.getString("uid"))) {
            return false; // 문서 없거나 권한 없음
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("content", newContent);

        docRef.update(updates).get();
        return true;
    }

    // 문의 삭제 (문의 작성자만 가능)
    public boolean deleteInquiry(String docId, String uid) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(docId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists() || !uid.equals(snapshot.getString("uid"))) {
            return false; // 문서 없거나 권한 없음
        }

        docRef.delete().get();
        return true;
    }
}