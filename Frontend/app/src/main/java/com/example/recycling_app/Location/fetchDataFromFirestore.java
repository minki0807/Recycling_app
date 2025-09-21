package com.example.recycling_app.Location;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class fetchDataFromFirestore {

    public interface OnDataReadyCallback {
        // 데이터 로딩에 성공했을 때 호출될 메소드
        void onDataReady(List<LocationData> locations);

        // 데이터 로딩에 실패했을 때 호출될 메소드
        void onDataFetchFailed(String errorMessage);
    }

    private final FirebaseFirestore db;
    private static final String TAG = "fetchDataFromFirestore";

    public fetchDataFromFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    // 데이터를 가져오는 메인 메소드
    public void fetchAllLocations(OnDataReadyCallback callback) {
        List<LocationData> allLocations = new ArrayList<>();
        String[] collections = {"recycling_clothes_location", "recycling_recycle_location", "recycling_phone_location", "recycling_homemachine_location", "recycling_pill_location", "recycling_battery_location"};
        AtomicInteger collectionCount = new AtomicInteger(collections.length);

        for (String collectionName : collections) {
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LocationData locationData = document.toObject(LocationData.class);
                                if (collectionName.contains("clothes")) {
                                    locationData.type = "clothes";
                                } else if (collectionName.contains("recycle")) {
                                    locationData.type = "recycle";
                                } else if (collectionName.contains("phone")) {
                                    locationData.type = "phone";
                                } else if (collectionName.contains("homemachine")) {
                                    locationData.type = "homemachine";
                                } else if (collectionName.contains("pill")) {
                                    locationData.type = "pill";
                                } else if (collectionName.contains("battery")) {
                                    locationData.type = "battery";
                                }

                                allLocations.add(locationData);
                            }
                        } else {
                            Log.w(TAG, "컬렉션 가져오기 오류: ", task.getException());
                            callback.onDataFetchFailed("데이터를 가져오는 중 오류가 발생했습니다: " + task.getException().getMessage());
                            return;
                        }


                        if (collectionCount.decrementAndGet() == 0) {
                            Log.d(TAG, "모든 데이터 로드 완료. 총 " + allLocations.size() + "개.");
                            callback.onDataReady(allLocations);
                        }
                    });
        }
    }
}