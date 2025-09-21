package com.example.recycling_app.Account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.StartscreenActivity;
import com.example.recycling_app.ui.dialog.SelectionDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Googleuser_AdditionalInfoActivity extends AppCompatActivity implements SelectionDialogFragment.OnItemSelectedListener {

    private static final String TAG = "GoogleAdditionalInfo";
    private EditText editTextNickname;
    private EditText editTextPhone;
    private TextView textViewAge, textViewGender, textViewRegion;
    private Button buttonSignup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userEmail, userName, userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googleuser_additional_info);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userEmail = getIntent().getStringExtra("email");
        userName = getIntent().getStringExtra("name");
        userUid = getIntent().getStringExtra("uid");

        Log.d(TAG, "Received data - Email: " + userEmail + ", Name: " + userName + ", UID: " + userUid);

        // 현재 사용자 정보 가져오는 로직
        if (userUid == null || userUid.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userUid = currentUser.getUid();
                Log.d(TAG, "UID from current user: " + userUid);
            }
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (userEmail == null || userEmail.isEmpty()) userEmail = currentUser.getEmail();
            if (userName == null || userName.isEmpty()) userName = currentUser.getDisplayName();
        }

        editTextNickname = findViewById(R.id.edit_text_nickname);
        editTextPhone = findViewById(R.id.google_edit_text_phone);
        textViewAge = findViewById(R.id.text_view_age_selection);
        textViewGender = findViewById(R.id.text_view_gender_selection);
        textViewRegion = findViewById(R.id.text_view_region_selection);
        buttonSignup = findViewById(R.id.button_signup_complete);

        // 클릭 리스너들
        textViewAge.setOnClickListener(v -> {
            ArrayList<String> ages = new ArrayList<>(generateAgeList());
            SelectionDialogFragment dialogFragment = SelectionDialogFragment.newInstance("나이 선택", "age", ages);
            dialogFragment.setOnItemSelectedListener(Googleuser_AdditionalInfoActivity.this);
            dialogFragment.show(getSupportFragmentManager(), "age_selection_dialog");
        });
        textViewGender.setOnClickListener(v -> {
            ArrayList<String> genders = new ArrayList<>(Arrays.asList("남성", "여성", "선택 안 함"));
            SelectionDialogFragment dialogFragment = SelectionDialogFragment.newInstance("성별 선택", "gender", genders);
            dialogFragment.setOnItemSelectedListener(Googleuser_AdditionalInfoActivity.this);
            dialogFragment.show(getSupportFragmentManager(), "gender_selection_dialog");
        });
        textViewRegion.setOnClickListener(v -> {
            ArrayList<String> regions = new ArrayList<>(Arrays.asList("서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도", "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"));
            SelectionDialogFragment dialogFragment = SelectionDialogFragment.newInstance("지역 선택", "region", regions);
            dialogFragment.setOnItemSelectedListener(Googleuser_AdditionalInfoActivity.this);
            dialogFragment.show(getSupportFragmentManager(), "region_selection_dialog");
        });

        buttonSignup.setOnClickListener(v -> attemptSignup());

        // 시스템 UI 관련 코드
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    private List<String> generateAgeList() {
        List<String> ages = new ArrayList<>();
        for (int i = 1; i <= 100; i++) { ages.add(String.valueOf(i)); }
        return ages;
    }

    @Override
    public void onItemSelected(String key, String selectedItem) {
        switch (key) {
            case "age": textViewAge.setText(selectedItem); break;
            case "gender": textViewGender.setText(selectedItem); break;
            case "region": textViewRegion.setText(selectedItem); break;
        }
    }

    private void attemptSignup() {
        String nickname = editTextNickname.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();
        String ageStr = textViewAge.getText().toString().trim();
        String gender = textViewGender.getText().toString().trim();
        String region = textViewRegion.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phoneNumber.matches("^(010|011|016|017|018|019)-?[0-9]{3,4}-?[0-9]{4}$")) {
            Toast.makeText(this, "유효한 전화번호 형식이 아닙니다.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(ageStr) || ageStr.equals("나이를 선택하세요") ||
                TextUtils.isEmpty(gender) || gender.equals("성별을 선택하세요") ||
                TextUtils.isEmpty(region) || region.equals("지역을 선택하세요")) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Firestore에 정보를 저장할 때 닉네임 전달
            saveAdditionalUserInfoToFirestore(currentUser, phoneNumber, age, gender, region, nickname);
        } else {
            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            Intent backToLoginIntent = new Intent(Googleuser_AdditionalInfoActivity.this, StartscreenActivity.class);
            startActivity(backToLoginIntent);
            finish();
        }
    }

    private void saveAdditionalUserInfoToFirestore(FirebaseUser user, String phoneNumber, int age, String gender, String region, String nickname) {
        String userId = user.getUid();

        String currentEmail = user.getEmail();
        if (currentEmail == null || currentEmail.isEmpty()) currentEmail = userEmail;
        String currentName = user.getDisplayName();
        if (currentName == null || currentName.isEmpty()) currentName = userName;
        final String finalEmail = currentEmail;
        final String finalName = currentName;
        Log.d(TAG, "Saving to Firestore - UID: " + userId + ", Email: " + finalEmail + ", Name: " + finalName);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", userId);
        userMap.put("name", finalName);
        userMap.put("email", finalEmail);
        userMap.put("nickname", nickname); // 닉네임 추가
        userMap.put("googleaccount", true);
        userMap.put("isGoogleUser", true);
        userMap.put("age", age);
        userMap.put("gender", gender);
        userMap.put("phoneNumber", phoneNumber);
        userMap.put("region", region);

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Googleuser_AdditionalInfoActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Firestore에 회원 정보 저장 성공: " + finalEmail);
                    Intent nextIntent = new Intent(Googleuser_AdditionalInfoActivity.this, MainscreenActivity.class);
                    startActivity(nextIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document", e);
                    Toast.makeText(Googleuser_AdditionalInfoActivity.this, "회원 정보 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}