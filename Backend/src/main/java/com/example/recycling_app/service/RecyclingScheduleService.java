package com.example.recycling_app.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.example.recycling_app.dto.RecyclingScheduleDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

// 지역별 가이드 컬렉션에서 분리수거 일정을 조회하는 서비스
@Service
public class RecyclingScheduleService {

    // 팀원이 사용하는 지역별 가이드 컬렉션명
    private static final String COLLECTION_NAME = "area_waste_guide_all" +
            "";

    // 지역명(시/군/구)으로 분리수거 일정을 조회하는 메서드
    public RecyclingScheduleDTO getScheduleByRegion(String sidoName, String sigunguName) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Firestore에서 'sido_Name'과 'sigungu_Name' 필드가 일치하는 문서 조회
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME)
                .whereEqualTo("sido_Name", sidoName)
                .whereEqualTo("sigungu_Name", sigunguName)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (documents.isEmpty()) {
            return null;
        }

        // 새로운 데이터 필드명을 사용하여 RecyclingScheduleDTO에 필요한 정보를 추출
        QueryDocumentSnapshot document = documents.get(0);

        RecyclingScheduleDTO scheduleDTO = new RecyclingScheduleDTO();
        scheduleDTO.setRegion(
                document.getString("sido_Name") + " " +
                        document.getString("sigungu_Name") + " " +
                        document.getString("dongeupmyeon_Name")
        );
        scheduleDTO.setRecyclingDay(document.getString("recyclableWaste_day"));
        scheduleDTO.setStartTime(document.getString("recyclableWaste_start_time"));
        scheduleDTO.setEndTime(document.getString("recyclableWaste_end_time"));
        scheduleDTO.setDescription(document.getString("recyclableWaste_Method"));

        return scheduleDTO;
    }
}