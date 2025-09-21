package com.example.recycling_app.Howtobox;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionViewModel extends AndroidViewModel {

    private static final String TAG = "RegionViewModel";

    // 계층적으로 변환된 전체 데이터를 보관할 캐시
    private final List<Sido> regionDataCache = new ArrayList<>();

    private final MutableLiveData<List<String>> sidos = new MutableLiveData<>();
    private final MutableLiveData<List<String>> sigungus = new MutableLiveData<>();
    private final MutableLiveData<List<String>> dongs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<List<String>> getSidos() { return sidos; }
    public LiveData<List<String>> getSigungus() { return sigungus; }
    public LiveData<List<String>> getDongs() { return dongs; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public RegionViewModel(@NonNull Application application) {
        super(application);
        // ViewModel이 생성될 때 JSON 데이터를 로드합니다.
        loadRegionsFromJson(application.getApplicationContext());
    }

    private void loadRegionsFromJson(Context context) {
        isLoading.setValue(true);
        try {
            // 1. assets에서 JSON 파일 읽기
            InputStream inputStream = context.getAssets().open("total_addresses.json");
            Reader reader = new InputStreamReader(inputStream, "UTF-8");

            // 2. Gson을 사용하여 FlatAddress 리스트로 파싱
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<FlatAddress>>() {}.getType();
            List<FlatAddress> flatList = gson.fromJson(reader, listType);

            // 3. Flat 데이터를 계층적 데이터로 변환 (그룹화)
            Map<String, Map<String, List<String>>> regionMap = new HashMap<>();
            for (FlatAddress addr : flatList) {
                if (addr.sido == null || addr.sigungu == null || addr.dongeupmyeon == null) continue;

                regionMap.computeIfAbsent(addr.sido, k -> new HashMap<>())
                        .computeIfAbsent(addr.sigungu, k -> new ArrayList<>())
                        .add(addr.dongeupmyeon);
            }

            // 4. 그룹화된 Map을 최종 데이터 클래스 List<Sido>로 변환
            regionDataCache.clear();
            for (Map.Entry<String, Map<String, List<String>>> sidoEntry : regionMap.entrySet()) {
                List<Sigungu> sigunguList = new ArrayList<>();
                for (Map.Entry<String, List<String>> sigunguEntry : sidoEntry.getValue().entrySet()) {
                    Collections.sort(sigunguEntry.getValue()); // 읍면동 정렬
                    sigunguList.add(new Sigungu(sigunguEntry.getKey(), sigunguEntry.getValue()));
                }
                sigunguList.sort(Comparator.comparing(s -> s.name)); // 시군구 정렬
                regionDataCache.add(new Sido(sidoEntry.getKey(), sigunguList));
            }
            regionDataCache.sort(Comparator.comparing(s -> s.name)); // 시도 정렬

            // 5. 시/도 목록을 LiveData에 설정하여 UI에 알림
            List<String> sidoNameList = new ArrayList<>();
            for (Sido sido : regionDataCache) {
                sidoNameList.add(sido.name);
            }
            sidos.postValue(sidoNameList);

        } catch (Exception e) {
            Log.e(TAG, "JSON 파일 로딩 또는 파싱 실패", e);
        } finally {
            isLoading.postValue(false);
        }
    }

    public void onSidoSelected(String sidoName) {
        for (Sido sido : regionDataCache) {
            if (sido.name.equals(sidoName)) {
                List<String> sigunguNameList = new ArrayList<>();
                for (Sigungu sigungu : sido.sigunguList) {
                    sigunguNameList.add(sigungu.name);
                }
                sigungus.setValue(sigunguNameList);
                dongs.setValue(new ArrayList<>()); // 하위 동 목록 초기화
                return;
            }
        }
    }

    public void onSigunguSelected(String sidoName, String sigunguName) {
        for (Sido sido : regionDataCache) {
            if (sido.name.equals(sidoName)) {
                for (Sigungu sigungu : sido.sigunguList) {
                    if (sigungu.name.equals(sigunguName)) {
                        dongs.setValue(sigungu.dongeupmyeonList);
                        return;
                    }
                }
            }
        }
    }
}