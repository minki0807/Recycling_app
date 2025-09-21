package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.MarketTransactionDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

// 사용자 거래(market transaction) 기록을 저장하고 조회하는 서비스 클래스
@Service
public class MarketTransactionService {
    private static final String COLLECTION_NAME= "market_transactions"; // Firestore 거래 기록 컬렉션명

    // 거래 기록 저장
    public String saveTransaction(String uid, MarketTransactionDTO transactionDTO) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        if (transactionDTO.getTransactionId() == null || transactionDTO.getTransactionId().isEmpty()) {
            transactionDTO.setTransactionId(UUID.randomUUID().toString()); // 고유 ID 생성
        }

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("transactions")
                .document(transactionDTO.getTransactionId())
                .set(transactionDTO);

        future.get();
        return "거래 기록 저장 완료";
    }

    // 특정 사용자 UID의 모든 거래 기록 조회
    public List<MarketTransactionDTO> getTransactions(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("transactions")
                .get();

        List<MarketTransactionDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            list.add(doc.toObject(MarketTransactionDTO.class));
        }
        return list;
    }

    // 특정 사용자의 판매 기록만 조회
    public List<MarketTransactionDTO> getSales(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("transactions")
                .whereEqualTo("transactionType", "SALE")
                .get();

        List<MarketTransactionDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            list.add(doc.toObject(MarketTransactionDTO.class));
        }
        return list;
    }

    // 특정 사용자의 구매 기록만 조회
    public List<MarketTransactionDTO> getPurchases(String uid) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .document(uid)
                .collection("transactions")
                .whereEqualTo("transactionType", "PURCHASE")
                .get();

        List<MarketTransactionDTO> list = new ArrayList<>();
        for (DocumentSnapshot doc : future.get().getDocuments()) {
            list.add(doc.toObject(MarketTransactionDTO.class));
        }
        return list;
    }
}