package com.example.recycling_app.Upcycling_market;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.recycling_app.R;
import com.example.recycling_app.adapter.ImageSliderAdapter;
import com.example.recycling_app.dto.market.ProductDTO;
import com.example.recycling_app.dto.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Product_Detail extends AppCompatActivity {

    private ProductDTO product;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvSellerName, tvProductTitle, tvProductPrice, tvProductDesc;
    private ImageView ivSeller;
    private ImageButton btnBack;
    private Button btncontact;
    private FirebaseFirestore db;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.product_detail);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("product")) {
            product = (ProductDTO) intent.getSerializableExtra("product");
        }

        if (product == null) {
            Toast.makeText(this, "상품 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSystemUI();
        setupListeners();
        populateData();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.vp_product_images);
        tabLayout = findViewById(R.id.tl_indicator);
        tvSellerName = findViewById(R.id.tv_seller_name);
        ivSeller = findViewById(R.id.iv_seller);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductDesc = findViewById(R.id.tv_product_desc);
        btnBack = findViewById(R.id.btn_back);
        btncontact = findViewById(R.id.btn_contact);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btncontact.setOnClickListener(v -> startChatWithSeller());
    }

    // 채팅 시작 로직
    private void startChatWithSeller() {
        String sellerUid = product.getUid();

        // 판매자 UID가 없거나 자신의 상품인 경우 채팅 불가
        if (sellerUid == null || sellerUid.isEmpty()) {
            Toast.makeText(this, "판매자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sellerUid.equals(currentUserId)) {
            Toast.makeText(this, "자신과의 채팅은 시작할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 기존 채팅방 찾기
        db.collection("chatRooms")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String existingChatRoomId = null;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            java.util.List<String> participants = (java.util.List<String>) document.get("participants");
                            if (participants != null && participants.contains(sellerUid)) {
                                existingChatRoomId = document.getId();
                                break;
                            }
                        }

                        if (existingChatRoomId != null) {
                            // 기존 채팅방이 있으면 바로 이동
                            navigateToChatRoom(existingChatRoomId);
                        } else {
                            // 기존 채팅방이 없으면 새로 생성
                            createChatRoom(sellerUid);
                        }
                    } else {
                        Log.e(TAG, "채팅방 검색 실패", task.getException());
                        Toast.makeText(Product_Detail.this, "채팅방을 찾는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 채팅방 생성 로직
    private void createChatRoom(String sellerUid) {
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("participants", Arrays.asList(currentUserId, sellerUid));
        // 필요에 따라 초기 메시지나 생성 시간 등 추가 가능
        chatRoomData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("chatRooms")
                .add(chatRoomData)
                .addOnSuccessListener(documentReference -> {
                    String newChatRoomId = documentReference.getId();
                    navigateToChatRoom(newChatRoomId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "채팅방 생성 실패", e);
                    Toast.makeText(Product_Detail.this, "채팅방 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    // 채팅 화면으로 이동하는 로직
    private void navigateToChatRoom(String chatRoomId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }

    private void populateData() {
        // 이미지 슬라이더 설정
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(product.getImages());
            viewPager.setAdapter(adapter);
        }

        // UID를 사용하여 판매자 정보 로드
        if (product.getUid() != null && !product.getUid().isEmpty()) {
            loadSellerInfo(product.getUid());
        } else {
            // UID가 없는 경우 기본값 표시
            setSellerInfo(null);
        }

        // 상품 정보
        tvProductTitle.setText(product.getProductName());
        tvProductDesc.setText(product.getProductDescription());

        if ("판매하기".equals(product.getTransactionType())) {
            tvProductPrice.setText(formatPrice(product.getPrice()));
        } else {
            tvProductPrice.setText("나눔 상품");
        }
    }

    // Firestore에서 판매자 정보를 가져오는 메서드
    private void loadSellerInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User seller = documentSnapshot.toObject(User.class);
                        setSellerInfo(seller);
                    } else {
                        Log.w(TAG, "판매자 정보를 찾을 수 없습니다: " + uid);
                        setSellerInfo(null); // 사용자가 없는 경우
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "판매자 정보 로드 실패", e);
                    setSellerInfo(null); // 오류 발생 시
                });
    }

    /**
     * 가져온 판매자 정보로 UI를 업데이트하는 메서드
     * @param seller UserDto 객체 (없을 경우 null)
     */
    private void setSellerInfo(User seller) {
        if (seller != null) {
            tvSellerName.setText(seller.getNickname());
            Glide.with(this)
                    .load(seller.getProfileImageUrl()) // 실제 이미지 URL
                    .placeholder(R.drawable.basic_profile_logo) // 로딩 중에 보여줄 이미지
                    .error(R.drawable.basic_profile_logo) // 로드 실패 또는 URL이 null일 때 보여줄 이미지
                    .apply(RequestOptions.circleCropTransform()) // 원형으로 자르기
                    .into(ivSeller);
        } else {
            // 판매자 정보가 없거나 로드 실패 시 기본값 설정
            tvSellerName.setText("알 수 없는 판매자");
            Glide.with(this)
                    .load(R.drawable.basic_profile_logo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivSeller);
        }
    }

    private String formatPrice(int price) {
        return NumberFormat.getCurrencyInstance(Locale.KOREA).format(price);
    }

    private void setupSystemUI() {
        // 상태바 밝게 설정
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
        }

        // 시스템 바에 따른 레이아웃 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 네비게이션 바 높이만큼 최상위 뷰에 하단 패딩 적용
            v.setPadding(systemBars.left, v.getPaddingTop(), systemBars.right, systemBars.bottom);

            // 상태 표시줄 높이만큼 뒤로가기 버튼에 상단 패딩 적용
            btnBack.setPadding(btnBack.getPaddingLeft(), systemBars.top, btnBack.getPaddingRight(), btnBack.getPaddingBottom());

            // 인셋을 소비했음을 시스템에 알림
            return WindowInsetsCompat.CONSUMED;
        });
    }
}