package com.example.recycling_app.Account;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    Button button_signup;
    EditText editTextEmail, editTextPassword, editTextPasswordConfirm, editTextname, editTextPhone;
    private CheckBox checkboxLocationConsent, checkboxPrivacyConsent;
    private FirebaseAuth fAuth;
    private DatabaseReference dRef; // 실시간 데이터베이스

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        button_signup = (Button) findViewById(R.id.button_signup);

        editTextEmail = (EditText) findViewById(R.id.edit_text_id);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        editTextPasswordConfirm = (EditText) findViewById(R.id.edit_text_password_confirm);
        editTextname = (EditText) findViewById(R.id.edit_text_name);
        editTextPhone = (EditText) findViewById(R.id.edit_text_phone);

        checkboxLocationConsent = findViewById(R.id.checkbox_location_consent);
        checkboxPrivacyConsent = findViewById(R.id.checkbox_privacy_consent);

        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = editTextEmail.getText().toString().trim();
                final String password = editTextPassword.getText().toString().trim();
                final String passwordConfirm = editTextPasswordConfirm.getText().toString().trim();
                final String name = editTextname.getText().toString().trim();
                final String phoneNumber = editTextPhone.getText().toString().trim();

                final boolean isLocationConsent = checkboxLocationConsent.isChecked();
                final boolean isPrivacyConsent = checkboxPrivacyConsent.isChecked();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                        TextUtils.isEmpty(passwordConfirm) || TextUtils.isEmpty(name) || // 이름 필드 유효성 검사
                        TextUtils.isEmpty(phoneNumber)) { // 전화번호 필드 유효성 검사
                    Toast.makeText(SignupActivity.this, "모든 필수 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignupActivity.this, "유효한 이메일 주소를 아이디로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 8) {
                    Toast.makeText(SignupActivity.this, "비밀번호는 최소 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(SignupActivity.this, "비밀번호와 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!phoneNumber.matches("^(010|011|016|017|018|019)-?[0-9]{3,4}-?[0-9]{4}$")) {
                    Toast.makeText(SignupActivity.this, "유효한 전화번호 형식이 아닙니다 (예: 010-1234-5678 또는 01012345678)", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!isLocationConsent) {
                    Toast.makeText(SignupActivity.this, "위치 정보 수집 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isPrivacyConsent) {
                    Toast.makeText(SignupActivity.this, "개인정보 수집 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // AdditionalInfoActivity로 데이터 전달
                Intent intent = new Intent(SignupActivity.this, AdditionalInfoActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                intent.putExtra("name", name);
                intent.putExtra("phoneNumber", phoneNumber);

                startActivity(intent);
            }
        });

        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정
        // 이 코드는 레이아웃이 시스템 바 아래로 확장될 때 콘텐츠가 시스템 바에 가려지지 않도록 함
        // 시스템 바의 인셋만큼 뷰의 좌, 상, 우, 하 패딩을 설정
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
}