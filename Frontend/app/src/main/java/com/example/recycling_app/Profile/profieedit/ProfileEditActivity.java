package com.example.recycling_app.Profile.profieedit;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.network.RetrofitClient;
import com.example.recycling_app.dto.ProfileDTO;
import com.example.recycling_app.service.ProfileApiService;
import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView backArrowIcon;
    private TextView editProfileTitle;
    private CircleImageView editProfileImage;
    private ImageView cameraIcon;
    private Button btnChangeToDefaultProfile;
    private EditText etNickname;
    private Switch switchPublicProfile;
    private Button btnCancel;
    private Button btnConfirm;

    private ProfileDTO currentProfileData;

    private FirebaseAuth mAuth;
    private String currentUid;
    private String firebaseIdToken; // Firebase ID 토큰 변수

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
        setupBottomNavigation();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUid = currentUser.getUid();
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                        @Override
                        public void onComplete(Task<com.google.firebase.auth.GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                firebaseIdToken = task.getResult().getToken();
                                // ID 토큰을 성공적으로 가져온 후 프로필 데이터 로드
                                loadProfileData(); // 토큰 획득 후 호출
                            } else {
                                Toast.makeText(ProfileEditActivity.this, "Firebase ID 토큰 가져오기 실패", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(this).load(selectedImageUri).into(editProfileImage);
                            uploadProfileImage(selectedImageUri);
                        }
                    }
                }
        );

        backArrowIcon = findViewById(R.id.back_arrow_icon);
        editProfileTitle = findViewById(R.id.edit_profile_title);
        editProfileImage = findViewById(R.id.edit_profile_image);
        cameraIcon = findViewById(R.id.camera_change_icon);
        btnChangeToDefaultProfile = findViewById(R.id.btn_change_to_default_profile);
        etNickname = findViewById(R.id.et_nickname);
        switchPublicProfile = findViewById(R.id.switch_public_profile);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        backArrowIcon.setOnClickListener(v -> onBackPressed());
        cameraIcon.setOnClickListener(v -> openGallery());
        editProfileImage.setOnClickListener(v -> openGallery());

        btnChangeToDefaultProfile.setOnClickListener(v -> {
            editProfileImage.setImageResource(R.drawable.basic_profile_logo);
            Toast.makeText(ProfileEditActivity.this, "기본 프로필로 변경되었습니다.", Toast.LENGTH_SHORT).show();
            Map<String, Object> updates = new HashMap<>();
            updates.put("profileImageUrl", "");
            updateProfileFields(updates);
        });

        switchPublicProfile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(ProfileEditActivity.this, "프로필이 공개됩니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileEditActivity.this, "프로필이 비공개됩니다.", Toast.LENGTH_SHORT).show();
            }
            Map<String, Object> updates = new HashMap<>();
            updates.put("isProfilePublic", isChecked);
            updateProfileFields(updates);
        });

        btnCancel.setOnClickListener(v -> {
            Toast.makeText(ProfileEditActivity.this, "프로필 수정이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnConfirm.setOnClickListener(v -> saveProfileChanges());

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

    private void loadProfileData() {
        if (currentUid == null || firebaseIdToken == null) {
            Toast.makeText(this, "사용자 인증 정보가 없어 프로필을 로드할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        ProfileApiService apiService = RetrofitClient.getProfileApiService();
//        String authHeader = "Bearer " + firebaseIdToken; // Authorization 헤더 생성

        // getProfile 호출 시 authHeader 추가
        apiService.getProfile(currentUid).enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfileData = response.body();
                    etNickname.setText(currentProfileData.getNickname());

                    if (currentProfileData.getProfileImageUrl() != null && !currentProfileData.getProfileImageUrl().isEmpty()) {
                        Glide.with(ProfileEditActivity.this)
                                .load(currentProfileData.getProfileImageUrl())
                                .placeholder(R.drawable.basic_profile_logo)
                                .error(R.drawable.basic_profile_logo)
                                .into(editProfileImage);
                    }
                    switchPublicProfile.setChecked(currentProfileData.isProfilePublic());
                    Toast.makeText(ProfileEditActivity.this, "프로필 정보 로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "프로필 정보 로드 실패: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void saveProfileChanges() {
        String nickname = etNickname.getText().toString();
        boolean isPublic = switchPublicProfile.isChecked();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nickname", nickname);
        updates.put("isProfilePublic", isPublic);

        updateProfileFields(updates);
    }

    private void updateProfileFields(Map<String, Object> updates) {
        if (currentUid == null || firebaseIdToken == null) {
            Toast.makeText(this, "사용자 인증 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        ProfileApiService apiService = RetrofitClient.getProfileApiService();
//        String authHeader = "Bearer " + firebaseIdToken;

        apiService.updateProfileFields(currentUid, updates).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this, "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "프로필 업데이트 실패: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUid == null || firebaseIdToken == null) {
            Toast.makeText(this, "사용자 인증 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File file = uriToFile(imageUri);
            if (file == null) {
                Toast.makeText(this, "이미지 파일을 준비할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(imageUri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            ProfileApiService apiService = RetrofitClient.getProfileApiService();

            // uploadImage 호출 시 authHeader 추가
            apiService.uploadImage(currentUid, body).enqueue(new Callback<String>() { // Changed here
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body();
                        Toast.makeText(ProfileEditActivity.this, "프로필 이미지 업로드 성공: " + imageUrl, Toast.LENGTH_SHORT).show();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", imageUrl);
                        updateProfileFields(updates);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "이미지 업로드 실패: " + response.message(), Toast.LENGTH_LONG).show();
                    }
                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(ProfileEditActivity.this, "이미지 업로드 네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                    if (file.exists()) {
                        file.delete();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "이미지 업로드 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(getCacheDir(), "temp_image_upload_" + System.currentTimeMillis());
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(ProfileEditActivity.this, CustomGalleryActivity.class);
        galleryLauncher.launch(intent);
    }

    // 하단 내비게이션 아이콘들의 클릭 이벤트를 설정하는 메서드
    private void setupBottomNavigation() {
        ImageButton homeIcon = findViewById(R.id.home_icon);
        ImageButton mapIcon = findViewById(R.id.map_icon);
        ImageButton cameraIcon = findViewById(R.id.camera_icon);
        ImageButton messageIcon = findViewById(R.id.message_icon);
        ImageButton accountIcon = findViewById(R.id.account_icon);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        messageIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, CameraActivity.class);
            startActivity(intent);
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileEditActivity.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}