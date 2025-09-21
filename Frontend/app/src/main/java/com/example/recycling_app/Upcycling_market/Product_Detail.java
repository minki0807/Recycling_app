package com.example.recycling_app.Upcycling_market;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.service.ApiResponse;
import com.example.recycling_app.service.ProductApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Product_Detail extends AppCompatActivity {

    private static final String TAG = "Product_Detail";

    private ProductApiService apiService;
    private ProductDTO product;
    private ViewPager2 viewPager;
    private TextView tvSellerName, tvProductTitle, tvProductPrice, tvProductDesc, tvImageCounter, tvProductStatus;
    private ImageView ivSeller;
    private ImageButton btnBack, btnOptions;
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

        apiService = RetrofitClient.getProductApiService();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("product")) {
            product = (ProductDTO) intent.getSerializableExtra("product");
        }

        if (product == null || currentUserId == null) {
            Toast.makeText(this, "상품 또는 사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSystemUI();
        setupListeners();
        populateData();

        btnBack.setVisibility(View.VISIBLE);
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.vp_product_images);
        tvImageCounter = findViewById(R.id.tv_image_counter);
        tvSellerName = findViewById(R.id.tv_seller_name);
        ivSeller = findViewById(R.id.iv_seller);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductDesc = findViewById(R.id.tv_product_desc);
        tvProductStatus = findViewById(R.id.tv_product_status);
        btnBack = findViewById(R.id.btn_back);
        btnOptions = findViewById(R.id.btn_options);
        btncontact = findViewById(R.id.btn_contact);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btncontact.setOnClickListener(v -> startChatWithSeller());
        btnOptions.setOnClickListener(this::showOptionsMenu);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateImageCounter(position);
            }
        });
    }

    // 채팅 시작 로직
    private void startChatWithSeller() {
        if (product == null || product.getUid() == null) {
            Toast.makeText(this, "판매자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerUid = product.getUid();
        String productId = product.getProductId();

        // 자기 자신과는 채팅할 수 없도록 방지
        if (currentUserId.equals(sellerUid)) {
            Toast.makeText(this, "자기 자신과는 대화할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 두 사용자의 UID를 조합하여 항상 동일한 채팅방 ID를 생성
        String chatRoomId;
        if (currentUserId.compareTo(sellerUid) > 0) {
            chatRoomId = currentUserId + "_" + sellerUid + "_" + productId;
        } else {
            chatRoomId = sellerUid + "_" + currentUserId + "_" + productId;
        }

        // Firestore에 해당 ID의 채팅방이 있는지 확인 후, 없으면 생성 (Get or Create)
        DocumentReference chatRoomRef = db.collection("chatRooms").document(chatRoomId);
        chatRoomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    Map<String, Object> chatRoomData = new HashMap<>();
                    chatRoomData.put("participants", Arrays.asList(currentUserId, sellerUid)); // 참가자는 두 사용자 ID만
                    chatRoomData.put("productId", productId); // 제품 ID를 별도 필드로 저장
                    chatRoomData.put("createdAt", FieldValue.serverTimestamp());
                    // 채팅방 생성과 동시에 채팅 액티비티로 이동
                    chatRoomRef.set(chatRoomData)
                            .addOnSuccessListener(aVoid -> navigateToChatRoom(chatRoomId))
                            .addOnFailureListener(e -> Toast.makeText(Product_Detail.this, "채팅방 생성에 실패했습니다.", Toast.LENGTH_SHORT).show());
                } else {
                    // 채팅방이 이미 존재하면 바로 이동
                    navigateToChatRoom(chatRoomId);
                }
            } else {
                Toast.makeText(Product_Detail.this, "채팅방 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToChatRoom(String chatRoomId) {
        Intent intent = new Intent(Product_Detail.this, ChatActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        intent.putExtra("otherUserId", product.getUid());
        intent.putExtra("productName", product.getProductName());

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }

    private void updateImageCounter(int position) {
        if (viewPager.getAdapter() != null) {
            int totalItems = viewPager.getAdapter().getItemCount();
            String counterText = (position + 1) + " / " + totalItems;
            tvImageCounter.setText(counterText);
            tvImageCounter.setVisibility(totalItems > 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void showOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.product_menu, popupMenu.getMenu());

        if (currentUserId.equals(product.getUid())) {
            popupMenu.show();
        } else {
            Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                Intent intent = new Intent(Product_Detail.this, ProductEditActivity.class);
                intent.putExtra("product_data", product);
                startActivity(intent);
                return true;
            } else if (id == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("상품 삭제")
                .setMessage("이 상품을 정말로 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteProduct())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteProduct() {
        if (product == null || product.getProductId() == null) {
            Toast.makeText(this, "상품 정보가 없어 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Object>> call = apiService.deleteProduct(product.getProductId(), currentUserId);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(Product_Detail.this, "상품이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(Product_Detail.this, "삭제에 실패했습니다: " + (response.body() != null ? response.body().getMessage() : "서버 응답 오류"), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "상품 삭제 API 호출 실패", t);
                Toast.makeText(Product_Detail.this, "네트워크 오류로 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData() {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(product.getImages());
            viewPager.setAdapter(adapter);
            updateImageCounter(0);
        } else {
            tvImageCounter.setVisibility(View.GONE);
        }

        if (product.getUid() != null && !product.getUid().isEmpty()) {
            loadSellerInfo(product.getUid());
        } else {
            setSellerInfo(null);
        }

        tvProductTitle.setText(product.getProductName());
        tvProductDesc.setText(product.getProductDescription());

        if ("판매하기".equals(product.getTransactionType())) {
            tvProductPrice.setText(formatPrice((int) product.getPrice()));
            tvProductStatus.setText("판매 중");
            tvProductStatus.setBackgroundResource(R.drawable.status_background_sale);
        } else {
            tvProductPrice.setText("나눔 상품");
            tvProductStatus.setText("나눔 중");
            tvProductStatus.setBackgroundResource(R.drawable.status_background_free);
        }
    }

    private void loadSellerInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setSellerInfo(documentSnapshot.toObject(User.class));
                    } else {
                        Log.w(TAG, "판매자 정보를 찾을 수 없습니다: " + uid);
                        setSellerInfo(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "판매자 정보 로드 실패", e);
                    setSellerInfo(null);
                });
    }

    private void setSellerInfo(User seller) {
        if (seller != null) {
            tvSellerName.setText(seller.getNickname());
            Glide.with(this)
                    .load(seller.getProfileImageUrl())
                    .placeholder(R.drawable.basic_profile_logo)
                    .error(R.drawable.basic_profile_logo)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivSeller);
        } else {
            tvSellerName.setText("알 수 없는 판매자");
            Glide.with(this).load(R.drawable.basic_profile_logo)
                    .apply(RequestOptions.circleCropTransform()).into(ivSeller);
        }
    }

    private String formatPrice(int price) {
        return NumberFormat.getCurrencyInstance(Locale.KOREA).format(price);
    }

    private void setupSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}