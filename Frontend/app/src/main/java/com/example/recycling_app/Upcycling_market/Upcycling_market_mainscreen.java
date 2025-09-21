package com.example.recycling_app.Upcycling_market;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.adapter.ProductAdapter;
import com.example.recycling_app.dto.User;
import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.service.ProductApiService;
import com.example.recycling_app.dto.market.ApiResponse;
import com.example.recycling_app.dto.market.ProductDTO;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Upcycling_market_mainscreen extends AppCompatActivity
        implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "MarketMainScreen";

    // UI 컴포넌트
    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private EditText editTextSearch;
    private ImageButton buttonBack;
    private ImageView searchIcon;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton addproductbutton;
    private ImageView imageViewSeller;

    // 어댑터와 API
    private ProductAdapter productAdapter;
    private ProductApiService apiService;
    private String currentSearchKeyword = "";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upcycling_market_mainscreen);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Edge-to-edge 설정
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // API 서비스 초기화
        apiService = RetrofitClient.getProductApiService();

        // UI 초기화
        initializeViews();
        loadUserProfile();
        setupRecyclerView();
        setupClickListeners();
        setupSwipeRefresh();
        setupSystemUI();
    }

    private void initializeViews() {
        recyclerViewProducts = findViewById(R.id.rv_products);
        progressBar = findViewById(R.id.pb_loading);
        emptyLayout = findViewById(R.id.ll_empty);
        editTextSearch = findViewById(R.id.et_search);
        buttonBack = findViewById(R.id.btn_back);
        searchIcon = findViewById(R.id.iv_search);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        addproductbutton = findViewById(R.id.add_product_button);
        imageViewSeller = findViewById(R.id.iv_seller);

        // 검색 힌트 설정
        editTextSearch.setHint("상품명을 입력하세요");
    }

    // 현재 로그인한 사용자의 프로필 정보를 Firestore에서 불러오는 메서드
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Firestore 문서를 User 객체로 변환
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                // Glide를 사용하여 프로필 이미지 설정
                                Glide.with(this)
                                        .load(user.getProfileImageUrl())
                                        .placeholder(R.drawable.basic_profile_logo)
                                        .error(R.drawable.basic_profile_logo)
                                        .circleCrop()
                                        .into(imageViewSeller);
                            }
                        } else {
                            // Firestore에 사용자 정보가 없는 경우
                            Log.w(TAG, "Firestore에 사용자 문서가 없습니다: " + uid);
                            setDefaultProfileImage();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 데이터 로드 실패 시
                        Log.e(TAG, "사용자 정보 로드 실패", e);
                        setDefaultProfileImage();
                    });
        } else {
            // 로그인한 사용자가 없는 경우
            Log.d(TAG, "로그인한 사용자가 없습니다.");
            setDefaultProfileImage();
        }
    }

    // 프로필 이미지를 기본값으로 설정하는 헬퍼 메서드
    private void setDefaultProfileImage() {
        Glide.with(this)
                .load(R.drawable.basic_profile_logo)
                .circleCrop()
                .into(imageViewSeller);
    }

    // 새로고침 설정 메서드
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "새로고침 시작");
            // 현재 검색어가 있는지 확인하여 적절한 목록을 다시 로드
            if (currentSearchKeyword.isEmpty()) {
                loadProducts();
            } else {
                searchProducts(currentSearchKeyword);
            }
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);

        // 스크롤 리스너 추가
        recyclerViewProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setupClickListeners() {
        // 뒤로가기 버튼
        buttonBack.setOnClickListener(v -> onBackPressed());

        // 상품 등록 버튼
        ImageButton addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Product_Registration.class);
            startActivity(intent);
        });

        // 검색 아이콘 클릭
        searchIcon.setOnClickListener(v -> performSearch());

        // 검색창 엔터키 처리
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        if (imageViewSeller != null) {
            imageViewSeller.setOnClickListener(v -> {
                Intent intent = new Intent(this, Seller_Profile.class);
                startActivity(intent);
            });
        }
    }

    private void performSearch() {
        String keyword = editTextSearch.getText().toString().trim();

        if (keyword.isEmpty()) {
            // 검색어가 비어있으면 전체 목록 로드
            currentSearchKeyword = "";
            loadProducts();
        } else {
            // 검색 수행
            currentSearchKeyword = keyword;
            searchProducts(keyword);
        }

        // 키보드 숨기기
        editTextSearch.clearFocus();
    }

    private void loadProducts() {
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        Call<ApiResponse<ProductDTO>> call = apiService.getAllProducts();
        call.enqueue(new Callback<ApiResponse<ProductDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDTO>> call, Response<ApiResponse<ProductDTO>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false); // 새로고침 애니메이션 종료

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductDTO> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        List<ProductDTO> products = apiResponse.getProducts();
                        updateProductList(products);
                        Log.d(TAG, "상품 로드 성공: " + (products != null ? products.size() : 0) + "개");
                    } else {
                        showError("상품을 불러오는데 실패했습니다: " + apiResponse.getMessage());
                        showEmptyState(true);
                    }
                } else {
                    showError("서버 응답 오류: " + response.code());
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductDTO>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false); // [추가] 새로고침 애니메이션 종료
                Log.e(TAG, "네트워크 오류", t);
                showError("네트워크 연결을 확인해주세요");
                showEmptyState(true);
            }
        });
    }

    private void searchProducts(String keyword) {
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        Call<ApiResponse<ProductDTO>> call = apiService.searchProducts(keyword);
        call.enqueue(new Callback<ApiResponse<ProductDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductDTO>> call, Response<ApiResponse<ProductDTO>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false); // 새로고침 애니메이션 종료

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductDTO> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        List<ProductDTO> products = apiResponse.getProducts();
                        updateProductList(products);

                        String message = "'" + keyword + "' 검색 결과: " +
                                (products != null ? products.size() : 0) + "개";

                        Log.d(TAG, "검색 완료: " + message);
                    } else {
                        showError("검색에 실패했습니다: " + apiResponse.getMessage());
                        showEmptyState(true);
                    }
                } else {
                    showError("서버 응답 오류: " + response.code());
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductDTO>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false); // 새로고침 애니메이션 종료
                Log.e(TAG, "검색 네트워크 오류", t);
                showError("네트워크 연결을 확인해주세요");
                showEmptyState(true);
            }
        });
    }

    private void updateProductList(List<ProductDTO> products) {
        if (products != null && !products.isEmpty()) {
            productAdapter.setProductList(products);
            showEmptyState(false);
            recyclerViewProducts.setVisibility(View.VISIBLE);
        } else {
            productAdapter.setProductList(null);
            showEmptyState(true);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerViewProducts.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if (show) {
            emptyLayout.removeAllViews();

            // 빈 상태 뷰 동적 생성
            LinearLayout emptyContent = new LinearLayout(this);
            emptyContent.setOrientation(LinearLayout.VERTICAL);
            emptyContent.setGravity(android.view.Gravity.CENTER);

            // 빈 상태 아이콘
            ImageView emptyIcon = new ImageView(this);
            emptyIcon.setImageResource(R.drawable.ic_empty_box);
            emptyIcon.setAlpha(0.5f);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(200, 200);
            iconParams.bottomMargin = 32;
            emptyIcon.setLayoutParams(iconParams);

            // 빈 상태 텍스트
            android.widget.TextView emptyText = new android.widget.TextView(this);
            if (currentSearchKeyword.isEmpty()) {
                emptyText.setText("등록된 상품이 없습니다");
            } else {
                emptyText.setText("'" + currentSearchKeyword + "' 검색 결과가 없습니다");
            }
            emptyText.setTextSize(16);
            emptyText.setTextColor(getResources().getColor(R.color.gray_600, getTheme()));
            emptyText.setGravity(android.view.Gravity.CENTER);

            emptyContent.addView(emptyIcon);
            emptyContent.addView(emptyText);
            emptyLayout.addView(emptyContent);

            emptyLayout.setVisibility(View.VISIBLE);
            recyclerViewProducts.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerViewProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProductClick(ProductDTO product) {
        // 상품 상세 화면으로 이동
        Intent intent = new Intent(this, Product_Detail.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 상품 등록 후 돌아왔을 때 목록 새로고침
        if (currentSearchKeyword.isEmpty()) {
            loadProducts();
        } else {
            searchProducts(currentSearchKeyword);
        }
    }

    private void setupSystemUI() {
        // 상태바 밝게 설정
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
        }

        // 시스템 바에 따른 레이아웃 조정
        // 리스너를 main_layout이 아닌 root_layout에 적용하여 전체 화면의 Inset을 받습니다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            // 스크롤되는 메인 콘텐츠(main_layout)에만 패딩을 적용합니다.
            View mainLayout = findViewById(R.id.main_layout);
            mainLayout.setPadding(
                    mainLayout.getPaddingLeft(),
                    statusBars.top, // 상단은 상태바 높이만큼
                    mainLayout.getPaddingRight(),
                    navBars.bottom  // 하단은 네비게이션 바 높이만큼
            );

            ImageButton addButton = findViewById(R.id.add_product_button);
            if (addButton != null) {
                // XML에 정의된 기본 마진(30dp)을 픽셀 값으로 변환
                int baseMargin = (int) (30 * getResources().getDisplayMetrics().density);

                // LayoutParams를 가져와서 bottomMargin 재설정
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addButton.getLayoutParams();
                params.bottomMargin = baseMargin + navBars.bottom; // 기본 마진 + 내비게이션 바 높이
                addButton.setLayoutParams(params);
            }


            return WindowInsetsCompat.CONSUMED; // Inset 처리가 완료되었음을 알립니다.
        });
    }
}