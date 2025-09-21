package com.example.recycling_app.Profile.customerservice;

import androidx.appcompat.app.AppCompatActivity; // Android 기본 Activity 클래스
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent; // 다른 Activity를 시작할 때 사용
import android.os.Bundle; // Activity 상태 저장 및 복원 시 사용
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.widget.ImageButton;
import android.widget.ImageView; // 이미지 뷰
import android.widget.LinearLayout; // LinearLayout 위젯 (레이아웃 그룹)
import android.widget.TextView; // 텍스트 뷰
import android.widget.Toast; // 짧은 메시지 팝업

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.Profile.profieedit.ProfileActivity;
import com.example.recycling_app.R;

// 1:1 문의 관련 기능을 제공하는 Activity
// 사용자에게 새 문의 작성 및 자신의 문의 내역을 볼 수 있는 메뉴 제공
public class InquiryActivity extends AppCompatActivity {

    // UI 요소들을 위한 변수 선언
    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
    private TextView inquiryTitle; // 화면 제목 텍스트
    private LinearLayout itemSubmitInquiry; // '문의하기' 메뉴 아이템 (LinearLayout)
    private LinearLayout itemMyInquiries;   // '내 문의' 메뉴 아이템 (LinearLayout)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry); // 이 Activity에 연결될 레이아웃 파일 지정

        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        inquiryTitle = findViewById(R.id.inquiry_title);
        itemSubmitInquiry = findViewById(R.id.item_submit_inquiry); // '문의하기' 레이아웃 초기화
        itemMyInquiries = findViewById(R.id.item_my_inquiries);     // '내 문의' 레이아웃 초기화

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정
        setupBottomNavigation();

        // 뒤로가기 아이콘 클릭 리스너 설정
        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                onBackPressed(); // Android의 뒤로가기 동작 수행 (이전 화면으로 돌아감)
            }
        });

        // "문의하기" 항목 클릭 리스너
        itemSubmitInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SubmitInquiryActivity (새 문의 작성 화면)로 이동하는 Intent 생성 및 시작
                Intent intent = new Intent(InquiryActivity.this, SubmitInquiryActivity.class);
                startActivity(intent);
                // 화면 전환 메시지 표시
                Toast.makeText(InquiryActivity.this, "새 문의 작성 화면으로 이동", Toast.LENGTH_SHORT).show();
            }
        });

        // "내 문의" 항목 클릭 리스너
        itemMyInquiries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MyInquiriesActivity (내 문의 목록 화면)로 이동하는 Intent 생성 및 시작
                Intent intent = new Intent(InquiryActivity.this, MyInquiriesActivity.class);
                startActivity(intent);
                // 화면 전환 메시지 표시
                Toast.makeText(InquiryActivity.this, "내 문의 목록 화면으로 이동", Toast.LENGTH_SHORT).show();
            }
        });

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

    // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
    private void setupBottomNavigation() {
        ImageButton homeIcon = findViewById(R.id.home_icon);
        ImageButton mapIcon = findViewById(R.id.map_icon);
        ImageButton cameraIcon = findViewById(R.id.camera_icon);
        ImageButton messageIcon = findViewById(R.id.message_icon);
        ImageButton accountIcon = findViewById(R.id.account_icon);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(InquiryActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(InquiryActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(InquiryActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
                Intent intent = new Intent( InquiryActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(InquiryActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}