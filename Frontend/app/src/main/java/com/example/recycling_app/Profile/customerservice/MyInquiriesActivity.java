package com.example.recycling_app.Profile.customerservice;

import androidx.annotation.NonNull; // null이 아님을 명시하는 어노테이션
import androidx.appcompat.app.AppCompatActivity; // Android의 기본 액티비티 클래스
import android.content.Intent; // 다른 액티비티를 시작할 때 사용
import android.os.Bundle; // 액티비티 상태 저장/복원 시 사용
import android.util.Log; // 로그 메시지 출력
import android.view.LayoutInflater; // XML 레이아웃을 View 객체로 변환
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.view.ViewGroup; // View 그룹 (레이아웃)
import android.widget.Button; // 버튼 위젯
import android.widget.ImageButton;
import android.widget.ImageView; // 이미지 뷰
import android.widget.LinearLayout; // LinearLayout 위젯 (레이아웃 그룹)
import android.widget.TextView; // 텍스트 뷰
import android.widget.Toast; // 짧은 메시지 팝업

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager; // RecyclerView의 항목을 선형으로 배열
import androidx.recyclerview.widget.RecyclerView; // 스크롤 가능한 대량의 항목을 효율적으로 표시

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.InquiryDTO; // 문의 데이터 DTO
import com.example.recycling_app.service.ProfileApiService; // 백엔드 프로필 API 서비스 인터페이스
import com.example.recycling_app.network.RetrofitClient; // Retrofit 클라이언트 (API 서비스 인스턴스 생성) util 패키지 확인

import com.google.firebase.auth.FirebaseAuth; // Firebase 인증 import
import com.google.firebase.auth.FirebaseUser; // Firebase 사용자 import
import com.google.android.gms.tasks.OnCompleteListener; // OnCompleteListener import
import com.google.android.gms.tasks.Task; // Task import

import java.text.SimpleDateFormat; // 날짜와 시간을 특정 형식으로 포맷
import java.util.ArrayList; // 동적 배열 리스트
import java.util.Date; // 날짜 및 시간 객체
import java.util.List; // 리스트 컬렉션
import java.util.Locale; // 지역(언어 및 국가) 설정

import retrofit2.Call; // Retrofit의 비동기 네트워크 요청 객체
import retrofit2.Callback; // Retrofit의 비동기 응답 처리 콜백
import retrofit2.Response; // Retrofit의 HTTP 응답 객체

// 사용자의 1:1 문의 내역을 표시하고, 새로운 문의를 작성할 수 있도록 돕는 액티비티
public class MyInquiriesActivity extends AppCompatActivity {

    // UI 요소들을 위한 변수 선언
    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
    private TextView myInquiriesTitle; // 화면 제목 텍스트
    private RecyclerView inquiriesRecyclerView; // 문의 목록을 표시할 RecyclerView
    private TextView tvNoInquiries; // 문의 기록이 없을 때 표시될 텍스트
    private Button btnSubmitNewInquiry; // '새로운 문의하기' 버튼
    private InquiryAdapter adapter; // RecyclerView에 데이터를 연결할 어댑터
    private List<InquiryDTO> inquiryList; // 문의 데이터 리스트 (InquiryDTO 타입)

    // Firebase 인증 객체 선언
    private FirebaseAuth mAuth;
    private String currentUid; // 실제 Firebase UID로 교체 (로그인 후 받아온 실제 사용자 UID 사용)
    private String firebaseIdToken; // Firebase ID 토큰

    private static final String TAG = "MyInquiriesActivity"; // 로그 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_inquiries); // 레이아웃 파일을 이 액티비티에 연결

        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        myInquiriesTitle = findViewById(R.id.my_inquiries_title);
        inquiriesRecyclerView = findViewById(R.id.my_inquiries_recycler_view);
        tvNoInquiries = findViewById(R.id.tv_no_inquiries);
        btnSubmitNewInquiry = findViewById(R.id.btn_submit_new_inquiry_button);

        // RecyclerView 설정
        inquiriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inquiryList = new ArrayList<>();
        adapter = new InquiryAdapter(inquiryList);
        inquiriesRecyclerView.setAdapter(adapter);

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정
        setupBottomNavigation();

        // 뒤로가기 아이콘 클릭 리스너 설정
        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                onBackPressed(); // 이전 화면으로 돌아가기
            }
        });

        // '새로운 문의하기' 버튼 클릭 리스너 설정
        if (btnSubmitNewInquiry != null) {
            btnSubmitNewInquiry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MyInquiriesActivity.this, SubmitInquiryActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "문의하기 버튼을 찾을 수 없습니다. XML을 확인해주세요."); // Log.e로 에러 로깅
            Toast.makeText(this, "문의하기 버튼을 찾을 수 없습니다. XML을 확인해주세요.", Toast.LENGTH_LONG).show();
        }

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 사용자 로그인 상태 확인 및 UID, ID 토큰 가져오기
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            Log.d(TAG, "Current UID: " + currentUid); // UID 로그 출력
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<com.google.firebase.auth.GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                firebaseIdToken = task.getResult().getToken();
                                Log.d(TAG, "Firebase ID Token acquired: " + (firebaseIdToken != null ? firebaseIdToken.substring(0, 30) + "..." : "null")); // 토큰 앞부분만 로그 출력 (보안상 전체는 피함)
                                // 토큰 획득 성공. 이제 문의 내역을 로드함.
                                loadInquiries();
                            } else {
                                Log.e(TAG, "Firebase ID 토큰 가져오기 실패", task.getException());
                                Toast.makeText(MyInquiriesActivity.this, "인증 정보 가져오기 실패", Toast.LENGTH_SHORT).show();
                                finish(); // 오류 시 액티비티 종료
                            }
                        }
                    });
        } else {
            Log.w(TAG, "사용자가 로그인되지 않았습니다. 문의 내역을 로드할 수 없습니다.");
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish(); // 로그인되지 않은 경우 액티비티 종료
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

    // 액티비티가 다시 화면에 나타날 때마다 문의 목록을 새로고침
    @Override
    protected void onResume() {
        super.onResume();
        // onResume 시에도 currentUid와 firebaseIdToken이 유효한지 확인하고 로드
        if (currentUid != null && firebaseIdToken != null) {
            loadInquiries();
        } else {
            // 토큰이 없으면 다시 가져오기 시도 (onCreate에서 실패했을 경우 대비)
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                currentUser.getIdToken(true)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                firebaseIdToken = task.getResult().getToken();
                                loadInquiries();
                            } else {
                                Toast.makeText(MyInquiriesActivity.this, "인증 정보 갱신 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    // 백엔드에서 사용자 문의 데이터를 비동기로 로드하는 메서드
    private void loadInquiries() {
        // UID 또는 토큰이 없는 경우 처리 (방어적 코드)
        if (currentUid == null || firebaseIdToken == null) {
            Log.w(TAG, "loadInquiries: UID 또는 Firebase ID 토큰이 null입니다. API 호출을 건너뜜.");
            Toast.makeText(this, "인증 정보가 없어 문의 내역을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            tvNoInquiries.setVisibility(View.VISIBLE); // 문의 없음 메시지 표시
            inquiriesRecyclerView.setVisibility(View.GONE); // RecyclerView 숨기기
            return;
        }

        ProfileApiService apiService = RetrofitClient.getProfileApiService();
        String authHeader = "Bearer " + firebaseIdToken; // Authorization 헤더 생성

        // getInquiriesByUid API 호출 시 Authorization 헤더 전달
        apiService.getInquiriesByUid(currentUid, authHeader).enqueue(new Callback<List<InquiryDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<InquiryDTO>> call, @NonNull Response<List<InquiryDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    inquiryList.clear();
                    inquiryList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (inquiryList.isEmpty()) {
                        tvNoInquiries.setVisibility(View.VISIBLE);
                        inquiriesRecyclerView.setVisibility(View.GONE);
                    } else {
                        tvNoInquiries.setVisibility(View.GONE);
                        inquiriesRecyclerView.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(MyInquiriesActivity.this, "문의 내역 로드 성공", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "문의 내역 로드 성공. 개수: " + inquiryList.size());
                } else {
                    String errorMessage = "문의 내역 로드 실패";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body: " + e.getMessage());
                        }
                    }
                    Log.e(TAG, "문의 내역 로드 실패: " + response.code() + " " + errorMessage);
                    Toast.makeText(MyInquiriesActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    tvNoInquiries.setVisibility(View.VISIBLE);
                    inquiriesRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<InquiryDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage(), t);
                Toast.makeText(MyInquiriesActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                tvNoInquiries.setVisibility(View.VISIBLE);
                inquiriesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    // RecyclerView 어댑터 클래스 (내부 클래스로 정의)
    public static class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder> {
        private final List<InquiryDTO> localInquiryList;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

        public InquiryAdapter(List<InquiryDTO> inquiryList) {
            this.localInquiryList = inquiryList;
        }

        @NonNull
        @Override
        public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inquiry_record, parent, false);
            return new InquiryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InquiryViewHolder holder, int position) {
            InquiryDTO inquiry = localInquiryList.get(position);
            holder.tvInquiryTitle.setText(inquiry.getTitle());
            holder.tvInquiryContent.setText(inquiry.getContent());
            holder.tvInquiryDate.setText(dateFormat.format(new Date(inquiry.getTimestamp())));

            if (inquiry.getAnswer() != null && !inquiry.getAnswer().trim().isEmpty()) {
                holder.tvInquiryStatus.setText("답변 완료");
                holder.tvInquiryStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                holder.answerContainer.setVisibility(View.VISIBLE);
                holder.tvInquiryAnswer.setText(inquiry.getAnswer());
            } else {
                holder.tvInquiryStatus.setText("답변 대기");
                holder.tvInquiryStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                holder.answerContainer.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "문의 클릭: " + inquiry.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return localInquiryList.size();
        }

        public static class InquiryViewHolder extends RecyclerView.ViewHolder {
            TextView tvInquiryTitle;
            TextView tvInquiryContent;
            TextView tvInquiryDate;
            TextView tvInquiryStatus;
            LinearLayout answerContainer;
            TextView tvInquiryAnswer;

            public InquiryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInquiryTitle = itemView.findViewById(R.id.tv_inquiry_title);
                tvInquiryContent = itemView.findViewById(R.id.tv_inquiry_content);
                tvInquiryDate = itemView.findViewById(R.id.tv_inquiry_date);
                tvInquiryStatus = itemView.findViewById(R.id.tv_inquiry_status);
                answerContainer = itemView.findViewById(R.id.answer_container);
                tvInquiryAnswer = itemView.findViewById(R.id.tv_inquiry_answer);
            }
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
            Intent intent = new Intent(MyInquiriesActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyInquiriesActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyInquiriesActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyInquiriesActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyInquiriesActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}