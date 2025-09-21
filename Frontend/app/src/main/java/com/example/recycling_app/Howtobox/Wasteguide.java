package com.example.recycling_app.Howtobox;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Camera_recognition.Photo_Recognition;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Wasteguide extends AppCompatActivity {

    private static final String TAG = "Wasteguide";

    // Firestore 인스턴스 및 UI 요소를 위한 멤버 변수 선언
    private FirebaseFirestore db;
    private TextView guideAreaSearchBox;
    private TextView guideDetailsTextView;

    // 다른 Activity로부터 결과를 받기 위한 ActivityResultLauncher 선언
    private ActivityResultLauncher<Intent> guideAreaSelectLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wasteguide);

        // Firestore 데이터베이스 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // XML 레이아웃에서 UI 요소를 찾아 멤버 변수에 할당
        guideAreaSearchBox = findViewById(R.id.guidearea_search_box);
        guideDetailsTextView = findViewById(R.id.guide_details_textview);

        // ActivityResultLauncher 초기화: Guidearea_select Activity가 종료되면 호출될 콜백 함수 정의
        guideAreaSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 결과 코드가 성공이고 데이터가 null이 아닐 때만 로직 실행
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        // Guidearea_select에서 전달된 지역 정보(시도, 시군구, 동읍면)를 가져옴
                        String sido = data.getStringExtra("EXTRA_SIDO");
                        String sigungu = data.getStringExtra("EXTRA_SIGUNGU");
                        String dong = data.getStringExtra("EXTRA_DONG");

                        if (sido != null && sigungu != null) {
                            // 세종특별자치시의 경우, UI 표시용 시군구 이름을 '전체'로 변경
                            String displaySigungu = sigungu;
                            if ("세종특별자치시".equals(sido) && "없음".equals(sigungu)) {
                                displaySigungu = "전체";
                            }

                            // 동읍면 정보가 있으면 전체 주소 문자열을 구성
                            String fullAddress;
                            if (dong != null && !dong.isEmpty()) {
                                fullAddress = sido + " " + displaySigungu + " " + dong;
                            } else {
                                // 동읍면 정보가 없으면 시도와 시군구만으로 주소 문자열을 구성
                                fullAddress = sido + " " + displaySigungu;
                            }

                            // 구성된 주소 문자열을 검색창 TextView에 표시
                            guideAreaSearchBox.setText(fullAddress.trim());

                            // Firestore에서 해당 지역의 분리배출 가이드 데이터를 조회 (원본 시군구 이름 사용)
                            fetchWasteGuideData(sido, sigungu, dong != null ? dong : "");
                        }
                    }
                }
        );

        // UI 초기 설정, 주소 검색 및 하단 내비게이션 설정 메서드 호출
        setupUI();
        setupGuideAreaNavigation();
        setupBottomNavigation();
    }

    // UI(시스템 바) 설정을 위한 메서드
    private void setupUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 주소 검색 버튼(guideAreaSearchBox)의 클릭 이벤트 설정
    private void setupGuideAreaNavigation() {
        if (guideAreaSearchBox != null) {
            guideAreaSearchBox.setOnClickListener(v -> {
                Log.d(TAG, "주소 검색 버튼이 클릭되었습니다.");
                // Guidearea_select Activity로 이동하는 Intent 생성 및 실행
                Intent intent = new Intent(Wasteguide.this, Guidearea_select.class);
                guideAreaSelectLauncher.launch(intent);
            });
        } else {
            Log.e(TAG, "guidearea_search_box를 찾을 수 없습니다.");
        }
    }

    /**
     * Firestore에서 데이터를 조회하는 메서드 (3단계 조회 로직 적용)
     */
    private void fetchWasteGuideData(String sido, String sigungu, String dong) {
        guideDetailsTextView.setText("가이드 정보를 불러오는 중...");
        Log.d(TAG, "1차 조회 시작: " + sido + ", " + sigungu + ", " + dong);

        // 1단계: 시도, 시군구, 동읍면으로 정확히 일치하는 데이터를 조회
        db.collection("waste_guide_all")
                .whereEqualTo("sido_Name", sido)
                .whereEqualTo("sigungu_Name", sigungu)
                .whereEqualTo("dongeupmyeon_Name", dong)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d(TAG, "1차 조회 성공 (정확한 주소 일치)");
                        // 성공 시 가이드 정보 표시
                        displayGuideInfo((QueryDocumentSnapshot) task.getResult().getDocuments().get(0));
                    } else {
                        // 1차 조회 실패 시 2차/3차 조회 로직 실행
                        // 시군구 이름에 공백이 포함된 경우 (예: "부천시 소사구") 첫 번째 단어를 추출하여 재시도
                        String baseSigungu = sigungu;
                        if (sigungu.contains(" ")) {
                            baseSigungu = sigungu.split(" ")[0];
                            Log.d(TAG, "1차 조회 실패. 시/군/구 이름을 분리하여 2차 조회 시도: " + baseSigungu);
                        } else {
                            Log.d(TAG, "1차 조회 실패. 3차 조회(대표 정보)를 시작합니다.");
                        }
                        fetchFallbackData(sido, sigungu, baseSigungu);
                    }
                });
    }

    /**
     * 1차 조회가 실패했을 때 실행되는 2차, 3차 조회 메서드
     * - 2차 조회: 시도 + 분리된 시군구 이름으로 조회 (예: 경기도 부천시)
     * - 3차 조회: 시도 + 원본 시군구 이름으로 조회 (예: 경기도 부천시 소사구)
     */
    private void fetchFallbackData(String originalSido, String originalSigungu, String baseSigungu) {
        db.collection("area_waste_guide_all")
                .whereEqualTo("sido_Name", originalSido)
                .whereEqualTo("sigungu_Name", baseSigungu) // 분리된 시군구 이름으로 조회
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d(TAG, "2차/3차 조회 성공 (대표 정보)");
                        displayGuideInfo((QueryDocumentSnapshot) task.getResult().getDocuments().get(0));
                    } else {
                        Log.e(TAG, "모든 조회 실패. 최종적으로 정보 없음.");
                        guideDetailsTextView.setText("해당 지역의 분리배출 가이드 정보가 없습니다.");
                    }
                });
    }

    // Firestore 문서를 받아 UI에 분리배출 가이드 정보를 표시하는 공통 메서드
    private void displayGuideInfo(QueryDocumentSnapshot document) {
        String generalWaste = document.getString("generalWaste_Method");
        String foodWaste = document.getString("foodWaste_Method");
        String recyclableWaste = document.getString("recyclableWaste_Method");
        String bulkyWaste = document.getString("bulkyWaste_Method");

        // 데이터가 null일 경우 "정보 없음"으로 대체
        if (generalWaste == null) generalWaste = "정보 없음";
        if (foodWaste == null) foodWaste = "정보 없음";
        if (recyclableWaste == null) recyclableWaste = "정보 없음";
        if (bulkyWaste == null) bulkyWaste = "정보 없음";

        // UI에 표시할 문자열 생성
        String guideContent = "일반 쓰레기: " + generalWaste + "\n\n" +
                "음식물 쓰레기: " + foodWaste + "\n\n" +
                "재활용품: " + recyclableWaste + "\n\n" +
                "대형 폐기물: " + bulkyWaste;

        // Firestore에 "\n"으로 저장된 줄바꿈 문자를 실제 줄바꿈으로 변환하여 TextView에 설정
        guideDetailsTextView.setText(guideContent.replace("\\n", "\n"));
    }

    // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
    private void setupBottomNavigation() {
        try {
            ImageButton homeIcon = findViewById(R.id.home_icon);
            ImageButton mapIcon = findViewById(R.id.map_icon);
            ImageButton cameraIcon = findViewById(R.id.camera_icon);
            ImageButton messageIcon = findViewById(R.id.message_icon);
            ImageButton accountIcon = findViewById(R.id.account_icon);

            if (homeIcon != null) {
                homeIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Wasteguide.this, MainscreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            if (mapIcon != null) {
                mapIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Wasteguide.this, LocationActivity.class);
                    startActivity(intent);
                });
            }

            if (cameraIcon != null) {
                cameraIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Wasteguide.this, CameraActivity.class);
                    startActivity(intent);
                });
            }

            messageIcon.setOnClickListener(v -> {
                Intent intent = new Intent(Wasteguide.this, CommunityActivity.class);
                startActivity(intent);
            });

            if (accountIcon != null) {
                accountIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Wasteguide.this, MypageActivity.class);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "하단 내비게이션 설정 중 오류 발생: " + e.getMessage());
        }
    }
}