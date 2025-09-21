package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.FavoriteDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

// 사용자 즐겨찾기 관련 비즈니스 로직을 담당하는 서비스 클래스
@Service
public class FavoriteService {

    private static final String COLLECTION_NAME = "favorites"; // Firestore 컬렉션명

    // 즐겨찾기 추가 메서드 (중복 등록 방지 기능 포함)
    public String addFavorite(FavoriteDTO favorite) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();                    // Firestore 인스턴스 획득
        CollectionReference favoritesRef = db.collection(COLLECTION_NAME); // favorites 컬렉션 참조

        // uid, name, type이 동일한 즐겨찾기 중복 체크 쿼리 생성
        Query query = favoritesRef
                .whereEqualTo("uid", favorite.getUid())
                .whereEqualTo("name", favorite.getName())
                .whereEqualTo("type", favorite.getType());

        ApiFuture<QuerySnapshot> future = query.get();                     // 쿼리 실행
        List<QueryDocumentSnapshot> documents = future.get().getDocuments(); // 결과 문서 리스트

        if (!documents.isEmpty()) {                                    // 중복 항목 존재 시
            return "이미 등록된 즐겨찾기 항목입니다.";                     // 중복 메시지 반환
        }

        favorite.setTimestamp(System.currentTimeMillis());              // 현재 시간(밀리초) 세팅
        favoritesRef.add(favorite);                                     // 즐겨찾기 데이터 추가
        return "즐겨찾기 추가 성공";                                      // 성공 메시지 반환
    }

    // 특정 사용자(uid)의 즐겨찾기 목록 조회, 정렬 옵션 지원(name or timestamp)
    public List<FavoriteDTO> getFavorites(String uid, String sortBy) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();                    // Firestore 인스턴스
        CollectionReference favoritesRef = db.collection(COLLECTION_NAME); // favorites 컬렉션 참조

        Query query = favoritesRef.whereEqualTo("uid", uid);              // uid 기준 필터링
        ApiFuture<QuerySnapshot> future = query.get();                     // 쿼리 실행
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();// 결과 문서 리스트

        List<FavoriteDTO> favorites = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            favorites.add(doc.toObject(FavoriteDTO.class));    // Document를 DTO로 변환해 리스트에 추가
        }

        // 정렬 처리: sortBy가 "name"일 경우 이름순 정렬, 그 외은 timestamp 최신순(내림차순) 정렬
        if ("name".equals(sortBy)) {
            favorites.sort(Comparator.comparing(FavoriteDTO::getName));   // 이름 오름차순 정렬
        } else {
            favorites.sort(Comparator.comparingLong(FavoriteDTO::getTimestamp).reversed()); // 최신순 정렬
        }

        return favorites;      // 정렬된 즐겨찾기 리스트 반환
    }

    // 특정 사용자(uid)의 즐겨찾기 항목 삭제 (이름과 타입으로 특정)
    public String deleteFavorite(String uid, String name, String type) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();                     // Firestore 인스턴스
        CollectionReference favoritesRef = db.collection(COLLECTION_NAME); // favorites 컬렉션 참조

        // uid, name, type으로 필터링 쿼리 생성
        Query query = favoritesRef
                .whereEqualTo("uid", uid)
                .whereEqualTo("name", name)
                .whereEqualTo("type", type);

        ApiFuture<QuerySnapshot> future = query.get();     // 쿼리 실행
        List<QueryDocumentSnapshot> documents = future.get().getDocuments(); // 결과 문서 리스트

        if (documents.isEmpty()) {                           // 삭제할 문서가 없으면
            return "해당 즐겨찾기 항목을 찾을 수 없습니다.";      // 해당 메시지 반환
        }

        // 조회된 문서들 모두 삭제 처리
        for (QueryDocumentSnapshot doc : documents) {
            doc.getReference().delete();
        }

        return "즐겨찾기 삭제 성공";       // 성공 메시지 반환
    }
}
