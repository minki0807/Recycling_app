package com.example.recycling_app.Profile.mysetting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Log import 추가
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.Profile.accountmanagement.AccountDeletionActivity;
import com.example.recycling_app.Profile.accountmanagement.PersonalInfoEditActivity;
import com.example.recycling_app.Profile.profieedit.ProfileActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.LanguageDTO;
import com.example.recycling_app.service.ProfileApiService;
import com.example.recycling_app.network.RetrofitClient; // RetrofitClient import 확인
import com.example.recycling_app.util.AuthManager; // AuthManager import 확인

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 설정 화면을 담당하는 액티비티
public class SettingsActivity extends AppCompatActivity {

    private ImageView backArrowIcon;
    private TextView settingsTitle;
    private Spinner spinnerLanguage;
    private ImageView ivLanguageDropdownArrow;
    private Button btnSaveLanguage; // 언어 저장 버튼
    private Button btnPersonalInfoEdit; // 개인 정보 수정 버튼 (XML에 있다면)
    private Button btnAccountDeletion; // 회원 탈퇴 버튼 (XML에 있다면)
    private AuthManager authManager;
    private String currentUid; // 사용자 UID
    private String firebaseIdToken; // Firebase ID 토큰 (AuthManager로부터 획득)

    private static final String TAG = "SettingsActivity"; // 로그 태그
    // 언어 목록 정의
    private static final List<String> LANGUAGES = Arrays.asList("한국어", "영어", "중국어", "일본어");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
        setupBottomNavigation();

        // AuthManager 초기화
        authManager = RetrofitClient.getAuthManager(); // RetrofitClient에서 AuthManager 인스턴스 가져오기

        // UI 요소 초기화
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        settingsTitle = findViewById(R.id.settings_title);
        spinnerLanguage = findViewById(R.id.spinner_language);
        ivLanguageDropdownArrow = findViewById(R.id.iv_language_dropdown_arrow);
        btnSaveLanguage = findViewById(R.id.btn_save_language);

        // XML에 이 버튼들이 있다면 주석 해제 및 ID 연결
        // btnPersonalInfoEdit = findViewById(R.id.btn_personal_info_edit);
        // btnAccountDeletion = findViewById(R.id.btn_account_deletion);

        // 뒤로가기 아이콘 클릭 리스너
        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                onBackPressed();
            }
        });

        // 언어 스피너 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_custom, LANGUAGES); // 여기를 수정
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom); // 여기를 수정
        spinnerLanguage.setAdapter(adapter);

        // 스피너 드롭다운 화살표 클릭 리스너
        ivLanguageDropdownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerLanguage.performClick();
            }
        });

        // 언어 저장 버튼 클릭 리스너
        btnSaveLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLanguageSetting();
            }
        });

        // AuthManager를 통해 UID 및 ID 토큰 획득
        authManager.getAuthData(new AuthManager.AuthDataCallback() {
            @Override
            public void onAuthDataReceived(String uid, String idToken) {
                if (uid != null && idToken != null) {
                    currentUid = uid;
                    firebaseIdToken = idToken;
                    Log.d(TAG, "AuthData received - UID: " + currentUid + ", Token length: " + idToken.length());
                    loadLanguageSetting(); // 인증 데이터 획득 후 언어 설정 로드
                } else {
                    Log.w(TAG, "인증 데이터(UID 또는 ID Token) 획득 실패.");
                    Toast.makeText(SettingsActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 로그인 정보가 없으면 액티비티 종료
                }
            }

            @Override
            public void onAuthDataError(String errorMessage) {
                Log.e(TAG, "AuthManager 오류: " + errorMessage);
                Toast.makeText(SettingsActivity.this, "인증 정보 로드 중 오류 발생: " + errorMessage, Toast.LENGTH_LONG).show();
                finish(); // 오류 발생 시 액티비티 종료
            }
        });

        // 개인 정보 수정 버튼 클릭 리스너 (주석 해제 시)
        if (btnPersonalInfoEdit != null) {
            btnPersonalInfoEdit.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, PersonalInfoEditActivity.class);
                startActivity(intent);
            });
        }

        // 회원 탈퇴 버튼 클릭 리스너 (주석 해제 시)

        if (btnAccountDeletion != null) {
            btnAccountDeletion.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, AccountDeletionActivity.class);
                startActivity(intent);
            });
        }

        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정
        // 이 코드는 레이아웃 콘텐츠가 시스템 바 아래로 확장될 때, 콘텐츠가 시스템 바에 가려지지 않도록 패딩을 추가
        // `main_layout`은 해당 액티비티의 최상위 레이아웃 ID여야 함
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 상단바 아이콘과 글씨 색상을 어둡게 설정 (Light Mode)
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    // 언어 설정을 백엔드로부터 로드하는 메서드
    private void loadLanguageSetting() {
        // UID와 토큰이 모두 있는지 확인
        if (currentUid == null || firebaseIdToken == null) {
            Log.w(TAG, "loadLanguageSetting: UID 또는 Firebase ID 토큰이 null입니다. API 호출을 건너뜜.");
            Toast.makeText(this, "인증 정보가 없어 언어 설정을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            setSpinnerSelection("ko"); // 기본값으로 설정
            return;
        }

        ProfileApiService apiService = RetrofitClient.getProfileApiService();
        String authHeader = "Bearer " + firebaseIdToken; // Authorization 헤더 생성

        // getLanguageSetting API 호출 시 Authorization 헤더 전달
        apiService.getLanguageSetting(currentUid, authHeader).enqueue(new Callback<LanguageDTO>() {
            @Override
            public void onResponse(Call<LanguageDTO> call, Response<LanguageDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String currentLanguageCode = response.body().getLanguage();
                    setSpinnerSelection(currentLanguageCode);
                    Log.d(TAG, "언어 설정 로드 성공: " + currentLanguageCode);
                    Toast.makeText(SettingsActivity.this, "언어 설정 로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "언어 설정 로드 실패: " + response.message();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " (" + response.errorBody().string() + ")";
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body: " + e.getMessage());
                    }
                    Log.e(TAG, errorMessage + " (Code: " + response.code() + ")");

                    if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(SettingsActivity.this, "인증 실패: 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                        authManager.clearAuthData(); // 인증 정보 삭제
                        finish(); // 액티비티 종료
                    } else {
                        Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        setSpinnerSelection("ko"); // 실패 시 기본값으로 설정
                    }
                }
            }

            @Override
            public void onFailure(Call<LanguageDTO> call, Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage(), t);
                Toast.makeText(SettingsActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
                setSpinnerSelection("ko"); // 오류 시 기본값으로 설정
            }
        });
    }

    // 스피너 선택 설정 메서드
    private void setSpinnerSelection(String languageCode) {
        String language;
        switch (languageCode) {
            case "en":
                language = "영어";
                break;
            case "zh":
                language = "중국어";
                break;
            case "ja":
                language = "일본어";
                break;
            case "ko":
            default:
                language = "한국어";
                break;
        }

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerLanguage.getAdapter();
        int position = adapter.getPosition(language);
        if (position >= 0) {
            spinnerLanguage.setSelection(position);
            Log.d(TAG, "스피너 선택 설정: " + language);
        } else {
            Log.w(TAG, "스피너에서 언어를 찾을 수 없음: " + language);
            spinnerLanguage.setSelection(0); // 기본값(한국어)으로 설정
        }
    }


    // 언어 설정을 백엔드에 저장하는 메서드
    private void saveLanguageSetting() {
        // UID와 토큰이 모두 있는지 확인
        if (currentUid == null || firebaseIdToken == null) {
            Log.w(TAG, "saveLanguageSetting: UID 또는 Firebase ID 토큰이 null입니다. API 호출을 건너뜜.");
            Toast.makeText(this, "인증 정보가 없어 언어 설정을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedLanguageText = (String) spinnerLanguage.getSelectedItem();
        String languageCode;

        switch (selectedLanguageText) {
            case "영어":
                languageCode = "en";
                break;
            case "중국어":
                languageCode = "zh";
                break;
            case "일본어":
                languageCode = "ja";
                break;
            case "한국어":
            default:
                languageCode = "ko";
                break;
        }
        Log.d(TAG, "선택된 언어 코드: " + languageCode);

        LanguageDTO languageDTO = new LanguageDTO(languageCode);
        ProfileApiService apiService = RetrofitClient.getProfileApiService();
        String authHeader = "Bearer " + firebaseIdToken; // Authorization 헤더 생성

        // updateLanguageSetting API 호출 시 Authorization 헤더 전달
        apiService.updateLanguageSetting(currentUid, authHeader ,languageDTO).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "언어 설정 저장 성공!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "언어 설정 저장 성공.");
                } else {
                    String errorMessage = "언어 설정 저장 실패";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body: " + e.getMessage());
                    }
                    Log.e(TAG, errorMessage + " (Code: " + response.code() + ")");

                    if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(SettingsActivity.this, "인증 실패: 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                        authManager.clearAuthData(); // 인증 정보 삭제
                        finish(); // 액티비티 종료
                    } else {
                        Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage(), t);
                Toast.makeText(SettingsActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
    private void setupBottomNavigation() {
        ImageButton homeIcon = findViewById(R.id.home_icon);
        ImageButton mapIcon = findViewById(R.id.map_icon);
        ImageButton cameraIcon = findViewById(R.id.camera_icon);
        ImageButton messageIcon = findViewById(R.id.message_icon);
        ImageButton accountIcon = findViewById(R.id.account_icon);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}