package com.example.recycling_app.Upcycling_market;

import android.content.ClipData;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.market.ProductDTO;
import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.service.ApiResponse;
import com.example.recycling_app.service.ProductApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class ProductEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1001;
    private static final String TAG = "ProductEditActivity";

    private ProductDTO product;
    private EditText etTitle, etDesc, etPrice;
    private Button btnSell, btnShare, btnUpdate;
    private ImageButton btnBack;
    private FrameLayout btnCamera;
    private LinearLayout llThumbRow;
    private TextView tvPhotoCount;

    private String selectedTransactionType;
    // 이미지 URI(새로 추가)와 URL(기존)을 모두 관리하는 리스트
    private List<Object> imageSources = new ArrayList<>();

    private ProductApiService apiService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("product_data")) {
            product = (ProductDTO) intent.getSerializableExtra("product_data");
        } else {
            Toast.makeText(this, "상품 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getProductApiService();
        currentUserId = FirebaseAuth.getInstance().getUid();

        initializeViews();
        setupListeners();
        populateExistingData();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        etPrice = findViewById(R.id.et_price);
        btnSell = findViewById(R.id.btn_sell);
        btnShare = findViewById(R.id.btn_share);
        btnUpdate = findViewById(R.id.btn_update);
        btnBack = findViewById(R.id.btn_back);
        btnCamera = findViewById(R.id.btn_camera);
        llThumbRow = findViewById(R.id.ll_thumb_row);
        tvPhotoCount = findViewById(R.id.tv_photo_count);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCamera.setOnClickListener(v -> openGallery());
        btnSell.setOnClickListener(v -> selectTransactionType("판매하기"));
        btnShare.setOnClickListener(v -> selectTransactionType("나눔하기"));
        btnUpdate.setOnClickListener(v -> attemptUpdateProduct());
    }

    private void populateExistingData() {
        if (product == null) return;

        etTitle.setText(product.getProductName());
        etDesc.setText(product.getProductDescription());

        // 거래 방식 및 가격 설정
        if ("판매하기".equals(product.getTransactionType())) {
            selectTransactionType("판매하기");
            etPrice.setText(String.valueOf((int) product.getPrice()));
        } else {
            selectTransactionType("나눔하기");
        }

        // 기존 이미지 로드
        if (product.getImages() != null) {
            for (String imageUrl : product.getImages()) {
                imageSources.add(imageUrl); // 기존 URL 추가
            }
            updateImageThumbnails();
        }
    }

    private void selectTransactionType(String type) {
        selectedTransactionType = type;
        if ("판매하기".equals(type)) {
            // '판매하기' 버튼을 선택된 스타일로 변경
            btnSell.setBackgroundResource(R.drawable.bg_chip_selected);
            btnSell.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            // '나눔하기' 버튼을 기본 스타일로 변경
            btnShare.setBackgroundResource(R.drawable.bg_chip);
            btnShare.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            etPrice.setVisibility(View.VISIBLE);
        } else { // "나눔하기"
            // '판매하기' 버튼을 기본 스타일로 변경
            btnSell.setBackgroundResource(R.drawable.bg_chip);
            btnSell.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            // '나눔하기' 버튼을 선택된 스타일로 변경
            btnShare.setBackgroundResource(R.drawable.bg_chip_selected);
            btnShare.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            etPrice.setVisibility(View.GONE);
            etPrice.setText("0");
        }
    }

    private void openGallery() {
        if (imageSources.size() >= 10) {
            Toast.makeText(this, "사진은 최대 10장까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            int currentSize = imageSources.size();
            int remainingSlots = 10 - currentSize;

            if (clipData != null) {
                int count = Math.min(clipData.getItemCount(), remainingSlots);
                for (int i = 0; i < count; i++) {
                    imageSources.add(clipData.getItemAt(i).getUri());
                }
            } else if (data.getData() != null && remainingSlots > 0) {
                imageSources.add(data.getData());
            }
            updateImageThumbnails();
        }
    }

    private void updateImageThumbnails() {
        // 카메라 버튼을 제외한 모든 뷰 삭제
        for (int i = llThumbRow.getChildCount() - 1; i > 0; i--) {
            llThumbRow.removeViewAt(i);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < imageSources.size(); i++) {
            Object source = imageSources.get(i);
            View thumbView = inflater.inflate(R.layout.item_thumbnail, llThumbRow, false);
            ImageView ivThumb = thumbView.findViewById(R.id.iv_thumbnail);
            ImageButton btnDelete = thumbView.findViewById(R.id.btn_delete_thumb);

            Glide.with(this).load(source).into(ivThumb);

            final int index = i;
            btnDelete.setOnClickListener(v -> {
                imageSources.remove(index);
                updateImageThumbnails();
            });

            llThumbRow.addView(thumbView);
        }
        tvPhotoCount.setText(imageSources.size() + "/10");
    }

    private void attemptUpdateProduct() {
        String title = etTitle.getText().toString().trim();
        String description = etDesc.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || selectedTransactionType == null) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("판매하기".equals(selectedTransactionType) && priceStr.isEmpty()) {
            Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageSources.isEmpty()) {
            Toast.makeText(this, "최소 1장의 사진을 등록해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. ProductDTO 객체 생성
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setUid(currentUserId);
        updatedProduct.setProductName(title);
        updatedProduct.setProductDescription(description);
        updatedProduct.setTransactionType(selectedTransactionType);
        updatedProduct.setPrice(Integer.parseInt(priceStr));

        // 2. DTO를 JSON 문자열로 변환
        Gson gson = new Gson();
        String productJson = gson.toJson(updatedProduct);
        RequestBody productRequestBody = RequestBody.create(MediaType.parse("application/json"), productJson);

        // 3. 이미지 파일들을 MultipartBody.Part로 변환
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Object source : imageSources) {
            if (source instanceof Uri) { // 새로 추가된 이미지만 파일로 변환
                Uri imageUri = (Uri) source;
                try {
                    File file = createFileFromUri(imageUri);
                    RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(imageUri)), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                    imageParts.add(body);
                } catch (IOException e) {
                    Log.e(TAG, "File creation failed", e);
                }
            }
        }

        // 4. 서버에 수정 요청
        Call<ApiResponse<Object>> call = apiService.updateProduct(product.getProductId(), productRequestBody, imageParts);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        Toast.makeText(ProductEditActivity.this, "상품이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                        // 상세 화면으로 돌아가서 변경사항을 반영하도록 결과 전달
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ProductEditActivity.this, "수정 실패: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductEditActivity.this, "서버 응답 오류로 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(ProductEditActivity.this, "네트워크 오류로 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Uri를 임시 파일로 변환하는 헬퍼 메서드
    private File createFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return tempFile;
    }
}
