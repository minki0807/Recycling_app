package com.example.recycling_app.Profile.customerservice;

import androidx.appcompat.app.AppCompatActivity; // Android 기본 Activity 클래스
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent; // 다른 Activity를 시작할 때 사용
import android.os.Bundle; // Activity 상태 저장 및 복원 시 사용
import android.os.CountDownTimer;
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.widget.ImageButton;
import android.widget.ImageView; // 이미지 뷰
import android.widget.LinearLayout; // LinearLayout 위젯 (레이아웃 그룹)
import android.widget.TextView; // 텍스트 뷰

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.Profile.profieedit.ProfileActivity;
import com.example.recycling_app.R;

// 고객 지원 기능을 담당하는 Activity
// 사용자에게 문의하기, 내 문의 내역 보기, FAQ 등의 메뉴를 제공
public class CustomerSupportActivity extends AppCompatActivity {

    // UI 요소들을 위한 변수 선언
    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
    private TextView customerSupportTitle; // 화면 제목 텍스트
    private LinearLayout itemSubmitInquiry; // '문의하기' 메뉴 아이템 (LinearLayout)
    private LinearLayout itemMyInquiries;   // '내 문의 내역' 메뉴 아이템 (LinearLayout)
    private LinearLayout itemFaq;           // 'FAQ' 메뉴 아이템 (LinearLayout)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support); // 이 Activity에 연결될 레이아웃 파일 지정

        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        customerSupportTitle = findViewById(R.id.customer_support_title);

        itemSubmitInquiry = findViewById(R.id.menu_items_container);
        itemMyInquiries = findViewById(R.id.item_inquiry);
        itemFaq = findViewById(R.id.item_faq);

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정
        setupBottomNavigation();

        // 뒤로가기 아이콘 클릭 리스너 설정
        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                onBackPressed(); // Android의 뒤로가기 동작 수행 (이전 화면으로 돌아감)
            }
        });

        // "문의하기" 메뉴 아이템 클릭 리스너
        itemSubmitInquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SubmitInquiryActivity로 이동하는 Intent 생성 및 시작
                Intent intent = new Intent(CustomerSupportActivity.this, SubmitInquiryActivity.class);
                startActivity(intent);
            }
        });

        // "내 문의 내역" 메뉴 아이템 클릭 리스너
        itemMyInquiries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MyInquiriesActivity로 이동하는 Intent 생성 및 시작
                Intent intent = new Intent(CustomerSupportActivity.this, MyInquiriesActivity.class);
                startActivity(intent);
            }
        });

        // "FAQ" 메뉴 아이템 클릭 리스너
        itemFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FaqActivity로 이동하는 Intent 생성 및 시작
                Intent intent = new Intent(CustomerSupportActivity.this, FaqActivity.class);
                startActivity(intent);
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
            Intent intent = new Intent(CustomerSupportActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}