package com.example.recycling_app.Upcycling_market;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.service.ApiResponse;
import com.example.recycling_app.dto.market.ProductDTO;
import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.service.ProductApiService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Product_Registration extends AppCompatActivity {

    private static final String TAG = "ProductRegistration";
    private static final int MAX_IMAGES = 10;

    // UI 요소
    private EditText etTitle, etDesc, etPrice;
    private Button btnSell, btnShare, btnUpload;
    private FrameLayout btnCamera;
    private LinearLayout llThumbRow;
    private TextView tvPhotoCount;
    private ImageButton btnBack;

    // 데이터
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private String transactionType = "판매하기"; // 기본값
    private ProductApiService apiService;

    // 갤러리 결과 처리를 위한 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // 여러 이미지 선택 처리
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        int remainingSlots = MAX_IMAGES - selectedImageUris.size();
                        int count = Math.min(clipData.getItemCount(), remainingSlots);

                        for (int i = 0; i < count; i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            selectedImageUris.add(imageUri);
                        }
                        if (clipData.getItemCount() > remainingSlots) {
                            Toast.makeText(this, "사진은 최대 10장까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // 한 이미지 선택 처리
                    else if (data.getData() != null) {
                        if (selectedImageUris.size() < MAX_IMAGES) {
                            Uri imageUri = data.getData();
                            selectedImageUris.add(imageUri);
                        }
                    }
                    updateThumbnails();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.product_registration);

        // RetrofitClient 초기화
        RetrofitClient.init(getApplicationContext());
        apiService = RetrofitClient.getProductApiService();

        initializeViews();
        setupSystemUI();
        setupListeners();
        updateTradeButtonUI();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        etPrice = findViewById(R.id.et_price);
        btnSell = findViewById(R.id.btn_sell);
        btnShare = findViewById(R.id.btn_share);
        btnUpload = findViewById(R.id.btn_upload);
        btnCamera = findViewById(R.id.btn_camera);
        llThumbRow = findViewById(R.id.ll_thumb_row);
        tvPhotoCount = findViewById(R.id.tv_photo_count);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v -> openGallery());

        btnSell.setOnClickListener(v -> {
            transactionType = "판매하기";
            updateTradeButtonUI();
        });

        btnShare.setOnClickListener(v -> {
            transactionType = "나눔하기";
            updateTradeButtonUI();
        });

        btnUpload.setOnClickListener(v -> registerProduct());
    }

    private void openGallery() {
        if (selectedImageUris.size() >= MAX_IMAGES) {
            Toast.makeText(this, "사진은 최대 10장까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 다중 선택 허용
        galleryLauncher.launch(intent);
    }

    private void updateThumbnails() {
        // 카메라 버튼을 제외한 모든 썸네일 뷰 제거
        llThumbRow.removeAllViews();
        llThumbRow.addView(btnCamera);

        // 선택된 이미지 URI로 썸네일 다시 추가
        for (Uri uri : selectedImageUris) {
            View thumbnailView = LayoutInflater.from(this).inflate(R.layout.item_thumbnail, llThumbRow, false);
            ImageView ivThumbnail = thumbnailView.findViewById(R.id.iv_thumbnail);
            ImageView ivRemove = thumbnailView.findViewById(R.id.btn_delete_thumb);

            Glide.with(this).load(uri).into(ivThumbnail);

            ivRemove.setOnClickListener(v -> {
                selectedImageUris.remove(uri);
                updateThumbnails(); // 썸네일 목록 갱신
            });

            llThumbRow.addView(thumbnailView);
        }

        // 사진 카운트 업데이트
        tvPhotoCount.setText(selectedImageUris.size() + "/" + MAX_IMAGES);
    }

    private void updateTradeButtonUI() {
        if ("판매하기".equals(transactionType)) {
            btnSell.setBackgroundResource(R.drawable.bg_chip_selected);
            btnSell.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnShare.setBackgroundResource(R.drawable.bg_chip);
            btnShare.setTextColor(ContextCompat.getColor(this, R.color.gray_700)); // 색상 리소스 필요
            etPrice.setVisibility(View.VISIBLE);
        } else { // 나눔하기
            btnShare.setBackgroundResource(R.drawable.bg_chip_selected);
            btnShare.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnSell.setBackgroundResource(R.drawable.bg_chip);
            btnSell.setTextColor(ContextCompat.getColor(this, R.color.gray_700));
            etPrice.setVisibility(View.GONE);
            etPrice.setText(""); // 가격 필드 초기화
        }
    }

    private void registerProduct() {
        // 1. 입력값 검증
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "최소 1장의 사진을 등록해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = etTitle.getText().toString().trim();
        String description = etDesc.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "작품명과 설명을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("판매하기".equals(transactionType) && priceStr.isEmpty()) {
            Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. ProductDto 객체 생성
        ProductDTO productDto = new ProductDTO();
        productDto.setUid(RetrofitClient.getAuthManager().getUid()); // AuthManager에서 현재 사용자 UID 가져오기
        productDto.setProductName(title);
        productDto.setProductDescription(description);
        productDto.setTransactionType(transactionType);
        productDto.setPrice("판매하기".equals(transactionType) ? Integer.parseInt(priceStr) : 0);

        // 3. RequestBody 생성 (JSON)
        Gson gson = new Gson();
        String productJson = gson.toJson(productDto);
        RequestBody productRequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), productJson);

        // 4. MultipartBody.Part 리스트 생성 (이미지 파일)
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                // Uri를 실제 파일 경로로 변환
                File file = createFileFromUri(uri, "image" + imageParts.size());
                RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                imageParts.add(body);
            } catch (Exception e) {
                Log.e(TAG, "File creation failed", e);
                Toast.makeText(this, "이미지 파일을 준비하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 5. API 호출
        btnUpload.setEnabled(false); // 중복 클릭 방지
        Toast.makeText(this, "업로드 중", Toast.LENGTH_SHORT).show();

        Call<ApiResponse<Object>> call = apiService.registerProduct(productRequestBody, imageParts);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    Toast.makeText(Product_Registration.this, "상품이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 등록 성공 시 화면 종료
                } else {
                    Toast.makeText(Product_Registration.this, "상품 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    btnUpload.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(Product_Registration.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnUpload.setEnabled(true);
            }
        });
    }

    // Uri를 임시 파일로 복사하여 File 객체를 생성하는 헬퍼 메서드
    private File createFileFromUri(Uri uri, String fileName) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), fileName);
        tempFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4 * 1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return tempFile;
    }

    private void setupSystemUI() {
        // 상태바 아이콘/글자 색상을 어둡게 설정 (배경이 밝을 경우)
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
        }

        // 시스템 바에 의해 가려지는 영역(Inset)을 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_post_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 최상위 뷰에 시스템 바 영역만큼 패딩을 적용하여 모든 콘텐츠가 가려지지 않게 함
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // 뒤로가기 버튼에 별도로 패딩을 주던 코드는 삭제합니다.

            // 인셋 처리를 완료했음을 시스템에 알림
            return WindowInsetsCompat.CONSUMED;
        });
    }
}