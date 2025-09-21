package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.FaqDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

// FAQ(자주 묻는 질문) 관련 비즈니스 로직을 처리하는 서비스 클래스
@Service
public class FaqService {
    private static final String COLLECTION_NAME = "faqs";  // Firestore 내 FAQ 컬렉션명

    // 전체 FAQ 목록 조회
    public List<FaqDTO> getAllFaqs() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();       // Firestore 인스턴스 획득
        var querySnapshot = db.collection(COLLECTION_NAME).get().get(); // 전체 문서 조회 (동기)

        List<FaqDTO> faqList = new ArrayList<>();
        querySnapshot.getDocuments().forEach(doc -> {
            FaqDTO faq = doc.toObject(FaqDTO.class);          // 문서를 DTO로 변환
            faq.setId(doc.getId());                            // 문서 ID를 DTO에 저장
            faqList.add(faq);                                  // 리스트에 추가
        });

        return faqList;                                       // FAQ 리스트 반환
    }

    // 단일 FAQ 조회 (문서 ID 기준)
    public FaqDTO getFaqById(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();       // Firestore 인스턴스
        var docSnapshot = db.collection(COLLECTION_NAME).document(id).get().get(); // 특정 문서 조회
        if (!docSnapshot.exists()) {                          // 문서 없으면
            return null;                                      // null 반환
        }
        FaqDTO faq = docSnapshot.toObject(FaqDTO.class);     // 문서 DTO 변환
        faq.setId(docSnapshot.getId());                       // 문서 ID 세팅
        return faq;                                           // DTO 반환
    }

    // 새 FAQ 등록
    public String addFaq(FaqDTO faqDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();       // Firestore 인스턴스
        var docRef = db.collection(COLLECTION_NAME).document(); // 신규 문서 레퍼런스 생성 (자동 ID)
        faqDTO.setId(docRef.getId());                         // 문서 ID를 DTO에 설정
        docRef.set(faqDTO).get();                             // 문서 저장 (동기)
        return faqDTO.getId();                                // 새 문서 ID 반환
    }

    // FAQ 수정 (기존 문서 덮어쓰기)
    public void updateFaq(String id, FaqDTO faqDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();       // Firestore 인스턴스
        db.collection(COLLECTION_NAME).document(id).set(faqDTO).get(); // 문서 덮어쓰기 (동기)
    }

    // FAQ 삭제 (문서 ID로)
    public void deleteFaq(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();       // Firestore 인스턴스
        db.collection(COLLECTION_NAME).document(id).delete().get(); // 문서 삭제 (동기)
    }
}
