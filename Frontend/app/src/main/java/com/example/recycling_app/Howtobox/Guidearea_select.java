package com.example.recycling_app.Howtobox;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Guidearea_select extends AppCompatActivity {

    private RegionViewModel viewModel;
    private RegionAdapter sidoAdapter;
    private RegionAdapter sigunguAdapter;
    private RegionAdapter dongAdapter;
    private TextView selectedRegionTextView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private String selectedSido = "";
    private String selectedSigungu = "";
    private String selectedDong = "";

    // 검색을 위한 원본 데이터 저장 리스트
    private List<String> originalSidoList = new ArrayList<>();
    private List<String> originalSigunguList = new ArrayList<>();
    private List<String> originalDongList = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidearea_select);

        // ViewModel 인스턴스 생성 및 초기화
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(RegionViewModel.class);

        // XML 레이아웃에서 UI 요소를 찾아 멤버 변수에 할당
        selectedRegionTextView = findViewById(R.id.selectedRegionTextView);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchIcon);
        AppCompatButton confirmButton = findViewById(R.id.confirmButton);
        AppCompatButton cancelButton = findViewById(R.id.cancelButton);

        // RecyclerView, ViewModel, 검색 기능 설정
        setupRecyclerViews();
        observeViewModel();
        setupSearchFunction();

        // '확인' 버튼 클릭 리스너 설정
        confirmButton.setOnClickListener(v -> {
            // 시도와 시군구가 모두 선택되었을 때만 확인 가능
            if (!selectedSido.isEmpty() && !selectedSigungu.isEmpty()) {
                // UI 표시용 텍스트 설정 (세종특별자치시의 경우 '전체'로 변환)
                if ("세종특별자치시".equals(selectedSido) && "없음".equals(selectedSigungu)) {
                    selectedRegionTextView.setText("세종특별자치시 전체");
                } else {
                    selectedRegionTextView.setText("전체");
                }

                // Intent에 선택된 지역 정보를 담아 이전 Activity(Wasteguide)로 전달
                Intent resultIntent = new Intent();
                resultIntent.putExtra("EXTRA_SIDO", selectedSido);
                resultIntent.putExtra("EXTRA_SIGUNGU", selectedSigungu);

                // 동읍면 정보가 있으면 추가하고, 없으면 빈 문자열 전달
                if (!selectedDong.isEmpty()) {
                    resultIntent.putExtra("EXTRA_DONG", selectedDong);
                } else {
                    resultIntent.putExtra("EXTRA_DONG", "");
                }

                setResult(RESULT_OK, resultIntent);
                finish(); // 현재 Activity 종료
            } else {
                Toast.makeText(this, "시도와 시군구를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // '취소' 버튼 클릭 리스너 설정
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // UI에 시스템 바(상태 바, 내비게이션 바) 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 상태 바 아이콘 색상을 밝게 설정
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    // 검색 기능 설정 메서드: 검색 버튼 클릭 시 전체 주소 검색 로직 실행
    private void setupSearchFunction() {
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                String searchText = searchEditText.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    handleFullAddressSearch(searchText);
                }
            });
        }
    }

    // 복합 주소를 파싱하고 지역을 자동으로 선택하는 메서드
    private void handleFullAddressSearch(String fullAddress) {
        String[] parts = fullAddress.split(" ");

        if (parts.length >= 2) {
            String sidoName = parts[0];

            // 시도 목록에서 첫 번째 단어를 찾습니다.
            if (originalSidoList.contains(sidoName)) {
                StringBuilder sigunguBuilder = new StringBuilder();
                // 시군구 이름이 여러 단어로 구성된 경우를 고려하여 모두 결합
                for (int i = 1; i < parts.length; i++) {
                    sigunguBuilder.append(parts[i]);
                    if (i < parts.length - 1) {
                        sigunguBuilder.append(" ");
                    }
                }
                String combinedSigungu = sigunguBuilder.toString();

                // ViewModel을 통해 시군구 목록을 가져와 검증
                viewModel.onSidoSelected(sidoName);
                viewModel.getSigungus().observe(this, sigungus -> {
                    if (sigungus != null && sigungus.contains(combinedSigungu)) {
                        selectedSido = sidoName;
                        selectedSigungu = combinedSigungu;
                        updateAndConfirmSelection();
                    } else {
                        Toast.makeText(this, "입력한 주소의 시군구를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "입력한 주소의 시도를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "정확한 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 최종 선택된 지역을 업데이트하고 결과를 반환하는 헬퍼 메서드
    private void updateAndConfirmSelection() {
        updateSelectedRegionText();

        if (!selectedSido.isEmpty() && !selectedSigungu.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("EXTRA_SIDO", selectedSido);
            resultIntent.putExtra("EXTRA_SIGUNGU", selectedSigungu);
            resultIntent.putExtra("EXTRA_DONG", selectedDong);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    // RecyclerView 및 어댑터 초기화 설정
    private void setupRecyclerViews() {
        // 시/도 RecyclerView 설정
        RecyclerView sidoRecyclerView = findViewById(R.id.sidoRecyclerView);
        sidoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sidoAdapter = new RegionAdapter();
        sidoRecyclerView.setAdapter(sidoAdapter);
        sidoAdapter.setOnItemClickListener(sidoName -> {
            // 시도 선택 시, 시군구와 동읍면 초기화 및 ViewModel 데이터 로드
            selectedSido = sidoName;
            selectedSigungu = "";
            selectedDong = "";

            if ("세종특별자치시".equals(sidoName)) {
                selectedSigungu = "없음";
                sigunguAdapter.setItems(new ArrayList<>(Collections.singletonList("전체")));
                originalSigunguList = new ArrayList<>(Collections.singletonList("전체"));
                viewModel.onSigunguSelected(selectedSido, selectedSigungu);
            } else {
                sigunguAdapter.setItems(new ArrayList<>());
                dongAdapter.setItems(new ArrayList<>());
                originalSigunguList.clear();
                originalDongList.clear();
                viewModel.onSidoSelected(sidoName);
            }

            searchEditText.setText("");
            updateSelectedRegionText();
        });

        // 시/군/구 RecyclerView 설정
        RecyclerView sigunguRecyclerView = findViewById(R.id.sigunguRecyclerView);
        sigunguRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sigunguAdapter = new RegionAdapter();
        sigunguRecyclerView.setAdapter(sigunguAdapter);
        sigunguAdapter.setOnItemClickListener(sigunguName -> {
            // 시군구 선택 시, 동읍면 초기화 및 ViewModel 데이터 로드
            selectedDong = "";

            if (!"세종특별자치시".equals(selectedSido)) {
                selectedSigungu = sigunguName;
            }
            viewModel.onSigunguSelected(selectedSido, selectedSigungu);

            searchEditText.setText("");
            updateSelectedRegionText();
        });

        // 동/읍/면 RecyclerView 설정
        RecyclerView dongRecyclerView = findViewById(R.id.dongRecyclerView);
        dongRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dongAdapter = new RegionAdapter();
        dongRecyclerView.setAdapter(dongAdapter);
        dongAdapter.setOnItemClickListener(dongName -> {
            // 동읍면 선택
            selectedDong = dongName;
            searchEditText.setText("");
            updateSelectedRegionText();
        });
    }

    // ViewModel의 LiveData를 관찰하여 UI 업데이트
    private void observeViewModel() {
        // 시/도 목록 업데이트
        viewModel.getSidos().observe(this, sidos -> {
            if (sidos != null) {
                originalSidoList = new ArrayList<>(sidos);
                sidoAdapter.setItems(sidos);
            } else {
                originalSidoList.clear();
                sidoAdapter.setItems(new ArrayList<>());
            }
        });

        // 시/군/구 목록 업데이트
        viewModel.getSigungus().observe(this, sigungus -> {
            if (sigungus != null) {
                originalSigunguList = new ArrayList<>(sigungus);
                sigunguAdapter.setItems(sigungus);
            } else {
                originalSigunguList.clear();
                sigunguAdapter.setItems(new ArrayList<>());
            }
        });

        // 동/읍/면 목록 업데이트
        viewModel.getDongs().observe(this, dongs -> {
            if (dongs != null) {
                originalDongList = new ArrayList<>(dongs);
                dongAdapter.setItems(dongs);
            } else {
                originalDongList.clear();
                dongAdapter.setItems(new ArrayList<>());
            }
        });
    }

    // 선택된 지역 정보를 화면 상단 TextView에 표시
    private void updateSelectedRegionText() {
        StringBuilder selectionText = new StringBuilder();
        if (!selectedSido.isEmpty()) {
            selectionText.append(selectedSido);
        }

        if (!selectedSigungu.isEmpty()) {
            if ("세종특별자치시".equals(selectedSido) && "없음".equals(selectedSigungu)) {
                selectionText.append(" ").append("전체");
            } else {
                selectionText.append(" ").append(selectedSigungu);
            }
        }

        if (!selectedDong.isEmpty()) {
            selectionText.append(" ").append(selectedDong);
        }

        if (selectionText.length() == 0) {
            selectedRegionTextView.setText("지역을 선택해주세요");
        } else {
            selectedRegionTextView.setText(selectionText.toString());
        }
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
                    Intent intent = new Intent(Guidearea_select.this, MainscreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            if (mapIcon != null) {
                mapIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Guidearea_select.this, LocationActivity.class);
                    startActivity(intent);
                });
            }

            if (cameraIcon != null) {
                cameraIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Guidearea_select.this, CameraActivity.class);
                    startActivity(intent);
                });
            }

            messageIcon.setOnClickListener(v -> {
                Intent intent = new Intent(Guidearea_select.this, CommunityActivity.class);
                startActivity(intent);
            });

            if (accountIcon != null) {
                accountIcon.setOnClickListener(v -> {
                    Intent intent = new Intent(Guidearea_select.this, MypageActivity.class);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "하단 내비게이션 설정 중 오류 발생: " + e.getMessage());
        }
    }
}