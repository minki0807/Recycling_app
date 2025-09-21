package com.example.recycling_app.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity; // Android의 기본 액티비티 클래스
import android.content.Intent; // 다른 액티비티를 시작할 때 사용
import android.os.Bundle; // 액티비티 상태 저장/복원 시 사용
import android.util.Log; // Log 클래스 임포트
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.widget.ImageButton;
import android.widget.ImageView; // 이미지 뷰
import android.widget.TextView; // 텍스트 뷰
import android.widget.Toast; // 짧은 메시지 팝업

import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import androidx.activity.EdgeToEdge; // 화면 전체를 사용하는 EdgeToEdge 기능 활성화
import androidx.core.graphics.Insets; // 시스템 바 인셋(inset) 정보
import androidx.core.view.ViewCompat; // 뷰 호환성 유틸리티
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat; // 윈도우 인셋(inset) 호환성 유틸리티
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.StartscreenActivity;
import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.accountmanagement.AccountManagementActivity;
import com.example.recycling_app.Profile.customerservice.CustomerSupportActivity;
import com.example.recycling_app.Profile.detailsofuse.UsageHistoryActivity;
import com.example.recycling_app.Profile.mysetting.SettingsActivity;
import com.example.recycling_app.Profile.profieedit.ProfileEditActivity;
import com.example.recycling_app.R;

import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.service.ProfileApiService;
import com.example.recycling_app.dto.ProfileDTO;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.activity.result.ActivityResultLauncher; // ActivityResultLauncher 추가
import androidx.activity.result.contract.ActivityResultContracts; // ActivityResultContracts 추가


// 마이페이지 기능을 담당하는 액티비티
// 사용자에게 프로필 정보 표시 및 다양한 메뉴(프로필 수정, 계정 관리, 이용 내역, 설정, 고객 지원, 로그아웃) 제공
public class MypageActivity extends AppCompatActivity {
    // UI 요소들을 위한 변수 선언
    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
    private TextView mypageTitle; // 마이페이지 제목 텍스트
    private de.hdodenhof.circleimageview.CircleImageView profileImage; // 원형 프로필 이미지 뷰 (서드파티 라이브러리)
    private TextView profileName; // 사용자 프로필 이름 텍스트

    // 메뉴 항목들을 나타내는 View 변수들 (주로 LinearLayout이나 ConstraintLayout)
    private View itemProfileEdit; // '프로필 수정' 메뉴
    private View itemAccountManagement; // '계정 관리' 메뉴
    private View itemUsageHistory; // '이용 내역' 메뉴
    private View itemSettings; // '설정' 메뉴
    private View itemCustomerSupport; // '고객 지원' 메뉴
    private View itemLogout; // '로그아웃' 메뉴
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private String firebaseIdToken;
    private String currentUid;
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // EdgeToEdge 기능을 활성화하여 시스템 바(상단바, 하단바) 영역까지 콘텐츠를 확장
        setContentView(R.layout.activity_profile); // 이 액티비티에 연결될 레이아웃 파일 지정 (activity_profile.xml)

        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        mypageTitle = findViewById(R.id.mypage_title);
        profileImage = findViewById(R.id.profile_image); // CircleImageView 위젯과 연결
        profileName = findViewById(R.id.profile_name);

        // 각 메뉴 항목 View들을 ID로 찾아서 연결
        itemProfileEdit = findViewById(R.id.item_profile_edit);
        itemAccountManagement = findViewById(R.id.item_account_management);
        itemUsageHistory = findViewById(R.id.item_usage_history);
        itemSettings = findViewById(R.id.item_settings);
        itemCustomerSupport = findViewById(R.id.item_customer_support);
        itemLogout = findViewById(R.id.item_logout);

        // Firebase 및 Google 로그인 클라이언트 초기화
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                firebaseIdToken = task.getResult().getToken();
                                loadProfileData(); // 프로필 데이터 로드
                            } else {
                                Log.e("MypageActivity", "ID 토큰 가져오기 실패", task.getException());
                            }
                        }
                    });
        }

        // 현재 로그인된 사용자의 이름 가져와서 설정
        if (mAuth.getCurrentUser() != null) {
            String userName = mAuth.getCurrentUser().getDisplayName();
            if (userName != null && !userName.isEmpty()) {
                profileName.setText(userName);
            } else {
                // 이름이 없을 경우 이메일을 표시하거나 기본 텍스트 설정
                profileName.setText("사용자");
            }
        }

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정
        setupBottomNavigation();

        // 뒤로가기 아이콘 클릭 리스너
        backArrowIcon.setOnClickListener(v -> onBackPressed()); // 클릭 시 이전 화면으로 돌아감

        // '프로필 수정' 메뉴 클릭 리스너
        // ProfileEditActivity로 이동하는 Intent 생성 및 시작
        itemProfileEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, ProfileEditActivity.class);
            editProfileLauncher.launch(intent);
        });

        // '계정 관리' 메뉴 클릭 리스너
        // AccountManagementActivity로 이동하는 Intent 생성 및 시작
        itemAccountManagement.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, AccountManagementActivity.class);
            startActivity(intent);
        });

        // '이용 내역' 메뉴 클릭 리스너
        // UsageHistoryActivity로 이동하는 Intent 생성 및 시작
        itemUsageHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, UsageHistoryActivity.class);
            startActivity(intent);
        });

        // '설정' 메뉴 클릭 리스너
        // SettingsActivity로 이동하는 Intent 생성 및 시작
        itemSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // '설정' 메뉴 클릭 리스너
        // SettingsActivity로 이동하는 Intent 생성 및 시작
        itemCustomerSupport.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, CustomerSupportActivity.class);
            startActivity(intent);
        });

        // 로그아웃 메뉴 클릭 시 Firebase 로그아웃 처리 추가
        itemLogout.setOnClickListener(v -> {
            signOut(); // 로그아웃 메소드 호출
        });

        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정
        // 이 코드는 레이아웃이 시스템 바 아래로 확장될 때 콘텐츠가 시스템 바에 가려지지 않도록 함
        // 시스템 바의 인셋만큼 뷰의 좌, 상, 우, 하 패딩을 설정
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

        // ActivityResultLauncher 초기화
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("profileChanged", false)) {
                            // profileEditActivity에서 프로필 변경이 감지되면 프로필 데이터를 다시 로드
                            loadProfileData();
                            Toast.makeText(MypageActivity.this, "프로필이 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentUid != null && firebaseIdToken != null) {
            loadProfileData();
        }
    }

    private void loadProfileData(){
        if(currentUid == null || firebaseIdToken == null) {
            Log.e("MypageActivity", "사용자 인증 정보가 없어 프로필을 로드할 수 없습니다.");
            return;
        }

        ProfileApiService apiService = RetrofitClient.getProfileApiService();
        String authHeader = "Bearer " + firebaseIdToken;

        apiService.getProfile(currentUid, authHeader).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDTO> call, @NonNull Response<ProfileDTO> response) {
                if(response.isSuccessful() && response.body() != null) {
                    ProfileDTO profileDTO = response.body();
                    profileName.setText(profileDTO.getNickname());
                    if(profileDTO.getProfileImageUrl() != null && !profileDTO.getProfileImageUrl().isEmpty()) {
                        String imageUrl = profileDTO.getProfileImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.contains("firebasestorage.app")) {
                            imageUrl = imageUrl + "?t=" + System.currentTimeMillis();
                        }
                        Glide.with(MypageActivity.this)
                                .load(profileDTO.getProfileImageUrl())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.basic_profile_logo)
                                .error(R.drawable.basic_profile_logo)
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.basic_profile_logo);
                    }
                    Log.d("MypageActivity", "프로필 정보 로드 성공");
                } else {
                    Log.e("MypageActivity", "프로필 정보 로드 실패: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileDTO> call, @NonNull Throwable t) {
                Log.e("MypageActivity", "네트워크 오류: " + t.getMessage(), t);
            }
        });
    }

    // 로그아웃 기능을 처리하는 메소드 추가
    private void signOut() {
        // Firebase에서 현재 사용자 로그아웃 처리
        mAuth.signOut();

        // Google Sign In 클라이언트에서 로그아웃 처리
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            // 로그아웃이 완료되면 토스트 메시지를 띄우고 로그인 화면으로 이동
            Toast.makeText(MypageActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MypageActivity.this, StartscreenActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티를 종료하여 뒤로가기 시 다시 돌아오지 않도록 함
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
            Intent intent = new Intent(MypageActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}
//
//package com.example.recycling_app.Profile;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity; // Android의 기본 액티비티 클래스
//import android.content.Intent; // 다른 액티비티를 시작할 때 사용
//import android.os.Bundle; // 액티비티 상태 저장/복원 시 사용
//import android.util.Log; // Log 클래스 임포트
//import android.view.View; // UI 컴포넌트의 기본 클래스
//import android.widget.ImageButton;
//import android.widget.ImageView; // 이미지 뷰
//import android.widget.TextView; // 텍스트 뷰
//import android.widget.Toast; // 짧은 메시지 팝업
//
//import com.example.recycling_app.Community.CommunityActivity;
//import com.example.recycling_app.Howtobox.Wasteguide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//
//import androidx.activity.EdgeToEdge; // 화면 전체를 사용하는 EdgeToEdge 기능 활성화
//import androidx.core.graphics.Insets; // 시스템 바 인셋(inset) 정보
//import androidx.core.view.ViewCompat; // 뷰 호환성 유틸리티
//import androidx.core.view.WindowCompat;
//import androidx.core.view.WindowInsetsCompat; // 윈도우 인셋(inset) 호환성 유틸리티
//import androidx.core.view.WindowInsetsControllerCompat;
//
//import com.example.recycling_app.StartscreenActivity;
//import com.example.recycling_app.Camera_recognition.CameraActivity;
//import com.example.recycling_app.Location.LocationActivity;
//import com.example.recycling_app.MainscreenActivity;
//import com.example.recycling_app.Profile.accountmanagement.AccountManagementActivity;
//import com.example.recycling_app.Profile.customerservice.CustomerSupportActivity;
//import com.example.recycling_app.Profile.detailsofuse.UsageHistoryActivity;
//import com.example.recycling_app.Profile.mysetting.SettingsActivity;
//import com.example.recycling_app.Profile.profieedit.ProfileEditActivity;
//import com.example.recycling_app.R;
//
//// 마이페이지 기능을 담당하는 액티비티
//// 사용자에게 프로필 정보 표시 및 다양한 메뉴(프로필 수정, 계정 관리, 이용 내역, 설정, 고객 지원, 로그아웃) 제공
//public class MypageActivity extends AppCompatActivity {
//    // UI 요소들을 위한 변수 선언
//    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
//    private TextView mypageTitle; // 마이페이지 제목 텍스트
//    private de.hdodenhof.circleimageview.CircleImageView profileImage; // 원형 프로필 이미지 뷰 (서드파티 라이브러리)
//    private TextView profileName; // 사용자 프로필 이름 텍스트
//
//    // 메뉴 항목들을 나타내는 View 변수들 (주로 LinearLayout이나 ConstraintLayout)
//    private View itemProfileEdit; // '프로필 수정' 메뉴
//    private View itemAccountManagement; // '계정 관리' 메뉴
//    private View itemUsageHistory; // '이용 내역' 메뉴
//    private View itemSettings; // '설정' 메뉴
//    private View itemCustomerSupport; // '고객 지원' 메뉴
//    private View itemLogout; // '로그아웃' 메뉴
//    private FirebaseAuth mAuth;
//    private GoogleSignInClient mGoogleSignInClient;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this); // EdgeToEdge 기능을 활성화하여 시스템 바(상단바, 하단바) 영역까지 콘텐츠를 확장
//        setContentView(R.layout.activity_profile); // 이 액티비티에 연결될 레이아웃 파일 지정 (activity_profile.xml)
//
//        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
//        backArrowIcon = findViewById(R.id.back_arrow_icon);
//        mypageTitle = findViewById(R.id.mypage_title);
//        profileImage = findViewById(R.id.profile_image); // CircleImageView 위젯과 연결
//        profileName = findViewById(R.id.profile_name);
//
//        // 각 메뉴 항목 View들을 ID로 찾아서 연결
//        itemProfileEdit = findViewById(R.id.item_profile_edit);
//        itemAccountManagement = findViewById(R.id.item_account_management);
//        itemUsageHistory = findViewById(R.id.item_usage_history);
//        itemSettings = findViewById(R.id.item_settings);
//        itemCustomerSupport = findViewById(R.id.item_customer_support);
//        itemLogout = findViewById(R.id.item_logout);
//
//        // Firebase 및 Google 로그인 클라이언트 초기화
//        mAuth = FirebaseAuth.getInstance();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        // 현재 로그인된 사용자의 이름 가져와서 설정
//        if (mAuth.getCurrentUser() != null) {
//            String userName = mAuth.getCurrentUser().getDisplayName();
//            if (userName != null && !userName.isEmpty()) {
//                profileName.setText(userName);
//            } else {
//                // 이름이 없을 경우 이메일을 표시하거나 기본 텍스트 설정
//                profileName.setText("사용자");
//            }
//        }
//
//        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정
//        setupBottomNavigation();
//
//        // 뒤로가기 아이콘 클릭 리스너
//        backArrowIcon.setOnClickListener(v -> onBackPressed()); // 클릭 시 이전 화면으로 돌아감
//
//        // '프로필 수정' 메뉴 클릭 리스너
//        // ProfileEditActivity로 이동하는 Intent 생성 및 시작
//        itemProfileEdit.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, ProfileEditActivity.class);
//            startActivity(intent);
//        });
//
//        // '계정 관리' 메뉴 클릭 리스너
//        // AccountManagementActivity로 이동하는 Intent 생성 및 시작
//        itemAccountManagement.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, AccountManagementActivity.class);
//            startActivity(intent);
//        });
//
//        // '이용 내역' 메뉴 클릭 리스너
//        // UsageHistoryActivity로 이동하는 Intent 생성 및 시작
//        itemUsageHistory.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, UsageHistoryActivity.class);
//            startActivity(intent);
//        });
//
//        // '설정' 메뉴 클릭 리스너
//        // SettingsActivity로 이동하는 Intent 생성 및 시작
//        itemSettings.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, SettingsActivity.class);
//            startActivity(intent);
//        });
//
//        // '설정' 메뉴 클릭 리스너
//        // SettingsActivity로 이동하는 Intent 생성 및 시작
//        itemCustomerSupport.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, CustomerSupportActivity.class);
//            startActivity(intent);
//        });
//
//        // 로그아웃 메뉴 클릭 시 Firebase 로그아웃 처리 추가
//        itemLogout.setOnClickListener(v -> {
//            signOut(); // 로그아웃 메소드 호출
//        });
//
//        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정
//        // 이 코드는 레이아웃이 시스템 바 아래로 확장될 때 콘텐츠가 시스템 바에 가려지지 않도록 함
//        // 시스템 바의 인셋만큼 뷰의 좌, 상, 우, 하 패딩을 설정
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        // 상단바 아이콘과 글씨 색상을 어둡게 설정 (Light Mode)
//        WindowInsetsControllerCompat windowInsetsController =
//                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
//        if (windowInsetsController != null) {
//            windowInsetsController.setAppearanceLightStatusBars(true);
//        }
//    }
//
//    // 로그아웃 기능을 처리하는 메소드 추가
//    private void signOut() {
//        // Firebase에서 현재 사용자 로그아웃 처리
//        mAuth.signOut();
//
//        // Google Sign In 클라이언트에서 로그아웃 처리
//        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
//            // 로그아웃이 완료되면 토스트 메시지를 띄우고 로그인 화면으로 이동
//            Toast.makeText(MypageActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(MypageActivity.this, StartscreenActivity.class);
//            startActivity(intent);
//            finish(); // 현재 액티비티를 종료하여 뒤로가기 시 다시 돌아오지 않도록 함
//        });
//    }
//
//    // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
//    private void setupBottomNavigation() {
//        ImageButton homeIcon = findViewById(R.id.home_icon);
//        ImageButton mapIcon = findViewById(R.id.map_icon);
//        ImageButton cameraIcon = findViewById(R.id.camera_icon);
//        ImageButton messageIcon = findViewById(R.id.message_icon);
//        ImageButton accountIcon = findViewById(R.id.account_icon);
//
//        homeIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, MainscreenActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent);
//            finish();
//        });
//
//        mapIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, LocationActivity.class);
//            startActivity(intent);
//        });
//
//        cameraIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, CameraActivity.class);
//            startActivity(intent);
//        });
//
//        messageIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, CommunityActivity.class);
//            startActivity(intent);
//        });
//
//        accountIcon.setOnClickListener(v -> {
//            Intent intent = new Intent(MypageActivity.this, MypageActivity.class);
//            startActivity(intent);
//        });
//    }
//}