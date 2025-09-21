package com.example.recycling_app.Profile.detailsofuse;

import androidx.appcompat.app.AppCompatActivity; // 안드로이드의 기본 액티비티 클래스

import android.os.Bundle; // 액티비티 상태 저장/복원 시 사용
import android.view.LayoutInflater; // XML 레이아웃을 View 객체로 변환
import android.view.View; // UI 컴포넌트의 기본 클래스
import android.view.ViewGroup; // View 그룹 (레이아웃)
import android.widget.ImageView; // 이미지 뷰
import android.widget.TextView; // 텍스트 뷰
import android.widget.Toast; // 짧은 메시지 팝업

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager; // RecyclerView의 항목을 선형으로 배열
import androidx.recyclerview.widget.RecyclerView; // 스크롤 가능한 대량의 항목을 효율적으로 표시

import com.bumptech.glide.Glide; // 이미지 로딩 라이브러리 Glide
import com.example.recycling_app.R;
import com.example.recycling_app.dto.AiRecognitionRecordDTO; // AI 인식 기록 데이터 DTO
import com.example.recycling_app.service.ProfileApiService; // 백엔드 프로필 API 서비스 인터페이스
import com.example.recycling_app.network.RetrofitClient; // Retrofit 클라이언트 (API 서비스 인스턴스 생성)

import java.text.SimpleDateFormat; // 날짜와 시간을 특정 형식으로 포맷
import java.util.ArrayList; // 동적 배열 리스트
import java.util.Date; // 날짜 및 시간 객체
import java.util.List; // 리스트 컬렉션
import java.util.Locale; // 지역(언어 및 국가) 설정

import retrofit2.Call; // Retrofit의 비동기 네트워크 요청 객체
import retrofit2.Callback; // Retrofit의 비동기 응답 처리 콜백
import retrofit2.Response; // Retrofit의 HTTP 응답 객체

// AI 인식 기록을 보여주는 액티비티
public class AiRecognitionHistoryActivity extends AppCompatActivity {

    // UI 요소들을 위한 변수 선언
    private ImageView backArrowIcon; // 뒤로가기 화살표 아이콘
    private TextView aiRecognitionHistoryTitle; // 화면 제목 텍스트
    private RecyclerView aiRecognitionRecyclerView; // AI 인식 기록 목록을 표시할 RecyclerView
    private TextView tvNoAiRecords; // 인식 기록이 없을 때 표시될 텍스트 뷰
    private AiRecognitionRecordAdapter adapter; // RecyclerView에 데이터를 연결할 어댑터
    private List<AiRecognitionRecordDTO> recognitionRecords; // AI 인식 기록 데이터 리스트 (DTO 타입)

    // TODO: 실제 Firebase UID와 ID 토큰으로 교체해야합. (로그인 후 받아온 실제 값 사용)
    // 예시 코드:
    // FirebaseAuth.getInstance().getCurrentUser().getUid();
    // FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).getResult().getToken();
    private String currentUid = "test_user_uid"; // 테스트용 UID. 실제 UID로 교체 필수
    private String firebaseIdToken = "dummy_firebase_id_token"; // 테스트용 ID 토큰. 실제 ID 토큰으로 교체 필수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recognition_history); // 레이아웃 파일을 이 액티비티에 연결

        // UI 요소 초기화 (XML 레이아웃의 ID와 연결)
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        aiRecognitionHistoryTitle = findViewById(R.id.ai_recognition_history_title);
        aiRecognitionRecyclerView = findViewById(R.id.ai_recognition_recycler_view);
        tvNoAiRecords = findViewById(R.id.tv_no_ai_records);

        // RecyclerView 설정
        aiRecognitionRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // 세로 스크롤 가능한 선형 레이아웃 설정
        recognitionRecords = new ArrayList<>(); // 기록 데이터를 담을 빈 리스트 초기화
        adapter = new AiRecognitionRecordAdapter(recognitionRecords); // 어댑터에 데이터 리스트 연결
        aiRecognitionRecyclerView.setAdapter(adapter); // RecyclerView에 어댑터 설정

        // 뒤로가기 아이콘 클릭 리스너 설정
        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                onBackPressed(); // 안드로이드의 뒤로가기 동작 수행 (이전 화면으로 돌아감)
            }
        });

        // AI 인식 기록 로드 메서드 호출
        loadAiRecognitionRecords();

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

    // 백엔드에서 AI 인식 기록 데이터를 비동기로 로드하는 메서드
    private void loadAiRecognitionRecords() {
        // RetrofitClient를 통해 ProfileApiService 인터페이스의 구현체(API 서비스 객체)를 가져옴
        ProfileApiService apiService = RetrofitClient.getProfileApiService();
        // Authorization 헤더 생성: "Bearer " 접두사 뒤에 Firebase ID 토큰을 붙임 (JWT 인증 방식)
        String authHeader = "Bearer " + firebaseIdToken;

        // API 서비스의 `getActivityRecords` 메서드 호출하여 네트워크 요청 전송
        // `enqueue` 메서드는 비동기적으로 요청을 실행하고, 응답이 오면 `Callback` 인터페이스의 메서드를 호출
        apiService.getActivityRecords(currentUid).enqueue(new Callback<List<AiRecognitionRecordDTO>>() {
            @Override
            public void onResponse(Call<List<AiRecognitionRecordDTO>> call, Response<List<AiRecognitionRecordDTO>> response) {
                // HTTP 응답이 성공적일 때 (2xx 코드)
                if (response.isSuccessful() && response.body() != null) {
                    recognitionRecords.clear(); // 기존 데이터 모두 지우기
                    recognitionRecords.addAll(response.body()); // 새로 받아온 데이터 추가
                    adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알려 RecyclerView 업데이트

                    // 로드된 기록이 있는지에 따라 "기록 없음" 메시지 또는 RecyclerView 표시
                    if (recognitionRecords.isEmpty()) {
                        tvNoAiRecords.setVisibility(View.VISIBLE); // "기록 없음" 메시지 보이기
                        aiRecognitionRecyclerView.setVisibility(View.GONE); // RecyclerView 숨기기
                    } else {
                        tvNoAiRecords.setVisibility(View.GONE); // "기록 없음" 메시지 숨기기
                        aiRecognitionRecyclerView.setVisibility(View.VISIBLE); // RecyclerView 보이기
                    }
                    Toast.makeText(AiRecognitionHistoryActivity.this, "AI 인식 기록 로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    // HTTP 응답이 실패했을 때 (4xx, 5xx 코드 등)
                    String errorMessage = "AI 인식 기록 로드 실패";
                    try {
                        // 에러 본문이 있다면 메시지에 추가
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력 (디버깅용)
                    }
                    Toast.makeText(AiRecognitionHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    tvNoAiRecords.setVisibility(View.VISIBLE); // 실패 시에도 "기록 없음" 메시지 표시
                    aiRecognitionRecyclerView.setVisibility(View.GONE); // RecyclerView 숨기기
                }
            }

            @Override
            public void onFailure(Call<List<AiRecognitionRecordDTO>> call, Throwable t) {
                // 네트워크 요청 자체가 실패했을 때 (예: 인터넷 연결 없음, 서버 응답 없음)
                Toast.makeText(AiRecognitionHistoryActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace(); // 오류 스택 트레이스 출력 (디버깅용)
                tvNoAiRecords.setVisibility(View.VISIBLE); // 네트워크 오류 시에도 "기록 없음" 메시지 표시
                aiRecognitionRecyclerView.setVisibility(View.GONE); // RecyclerView 숨기기
            }
        });
    }

    // --- RecyclerView 어댑터 클래스 (내부 클래스로 정의) ---
    // AiRecognitionRecordDTO 객체 리스트를 RecyclerView 항목으로 변환하고 표시
    public static class AiRecognitionRecordAdapter extends RecyclerView.Adapter<AiRecognitionRecordAdapter.ViewHolder> {
        private List<AiRecognitionRecordDTO> localRecognitionRecords; // 어댑터가 사용할 AI 인식 기록 데이터 리스트
        // 날짜 포맷터: "YYYY.MM.DD HH:MM" 형식으로 날짜를 표시 (기본 로케일 사용)
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

        // 어댑터 생성자: 데이터 리스트를 받아 초기화
        public AiRecognitionRecordAdapter(List<AiRecognitionRecordDTO> recognitionRecords) {
            this.localRecognitionRecords = recognitionRecords;
        }

        @Override
        // ViewHolder 생성: RecyclerView의 각 항목에 해당하는 View를 생성하고 ViewHolder에 담아 반환
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // item_ai_recognition_record.xml 레이아웃 파일을 인플레이트하여 View 객체 생성
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai_recognition_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        // ViewHolder에 데이터 바인딩: 특정 위치(position)의 데이터를 ViewHolder의 View에 설정
        public void onBindViewHolder(ViewHolder holder, int position) {
            AiRecognitionRecordDTO record = localRecognitionRecords.get(position); // 현재 위치의 기록 객체 가져오기

            // AI 인식 항목 이름 설정 (백엔드 DTO의 오타 'recognizatoinItem'을 그대로 반영)
            holder.tvItemName.setText(record.getRecognizatoinItem());

            // 타임스탬프(long)를 Date 객체로 변환 후, 지정된 형식의 날짜 문자열로 변환하여 설정
            String formattedDate = dateFormat.format(new Date(record.getTimestamp()));
            holder.tvRecognitionDate.setText(formattedDate);

            // 인식 결과 설정 (임시 로직):
            // 백엔드 DTO에 직접적인 '결과' 필드가 없으므로, 항목 이름을 기반으로 클라이언트에서 판단
            // 실제 앱에서는 백엔드에서 이 정보를 명확히 받아오거나, 클라이언트에서 더 정교한 규칙을 정의해야 함
            String recognitionResult = getResultBasedOnItem(record.getRecognizatoinItem());
            holder.tvRecognitionResult.setText(recognitionResult);

            // 인식 결과에 따라 텍스트 색상 변경
            if (recognitionResult.equals("재활용 가능")) {
                // Android 기본 초록색으로 설정
                holder.tvRecognitionResult.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                // Android 기본 빨간색으로 설정 (재활용 불가능, 분류 불가능 등)
                holder.tvRecognitionResult.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            // 이미지 URL을 사용하여 Glide로 이미지 로드
            if (record.getImageUrl() != null && !record.getImageUrl().isEmpty()) {
                // Glide를 사용하여 이미지를 ivItemIcon에 로드
                Glide.with(holder.itemView.getContext())
                        .load(record.getImageUrl()) // 로드할 이미지 URL
                        .placeholder(R.drawable.basic_profile_logo) // 이미지를 로딩 중일 때 표시될 이미지
                        .error(R.drawable.basic_profile_logo) // 이미지 로드 실패 시 표시될 이미지
                        .into(holder.ivItemIcon); // 이미지를 표시할 ImageView
            } else {
                // 이미지 URL이 없으면 기본 프로필 로고 이미지 설정
                holder.ivItemIcon.setImageResource(R.drawable.basic_profile_logo);
            }

            // 개별 항목 클릭 리스너 설정 (필요시 상세 화면으로 이동 등)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 클릭된 항목의 정보로 토스트 메시지 표시
                    Toast.makeText(v.getContext(), "클릭: " + record.getRecognizatoinItem() + " (" + recognitionResult + ")", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        // 전체 항목 개수 반환
        public int getItemCount() {
            return localRecognitionRecords.size();
        }

        // 인식 항목 이름에 따른 재활용 결과를 반환하는 헬퍼 메서드 (현재 임시 로직)
        // 실제로는 백엔드에서 받은 재활용 가능/불가능 여부를 사용해야 함
        private String getResultBasedOnItem(String itemName) {
            if (itemName != null) {
                // 항목 이름에 특정 키워드가 포함되어 있는지 확인하여 결과 반환
                if (itemName.contains("플라스틱") || itemName.contains("종이") || itemName.contains("유리")) {
                    return "재활용 가능";
                } else if (itemName.contains("비닐") || itemName.contains("음식물")) {
                    return "일반 쓰레기";
                }
            }
            return "분류 불가능"; // 매칭되는 키워드가 없으면 기본값 반환
        }

        // --- ViewHolder 클래스 ---
        // RecyclerView의 각 항목에 대한 뷰를 보유
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tvItemName; // 항목 이름 텍스트 뷰
            public TextView tvRecognitionDate; // 인식 날짜 텍스트 뷰
            public TextView tvRecognitionResult; // 인식 결과 텍스트 뷰
            public ImageView ivItemIcon; // 항목 아이콘 이미지 뷰

            // ViewHolder 생성자: item_ai_recognition_record.xml의 뷰 ID와 연결
            public ViewHolder(View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
                tvRecognitionDate = itemView.findViewById(R.id.tv_recognition_date);
                tvRecognitionResult = itemView.findViewById(R.id.tv_recognition_result);
                ivItemIcon = itemView.findViewById(R.id.iv_item_icon); // 아이콘 ImageView 초기화
            }
        }
    }
}