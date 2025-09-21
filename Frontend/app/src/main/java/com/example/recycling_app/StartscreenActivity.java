package com.example.recycling_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.Account.Googleuser_AdditionalInfoActivity;
import com.example.recycling_app.Account.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.example.recycling_app.Account.FindInfoActivity;
import com.example.recycling_app.Account.SignupActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class StartscreenActivity extends AppCompatActivity {

    private static final String TAG = "StartscreenActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In - 이메일과 프로필 정보를 요청하도록 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail() // 이메일 주소 요청
                .requestProfile() // 프로필 정보 요청 (이름 등)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.btn_google_sign_in).setOnClickListener(v -> signIn());

        Button loginButton = findViewById(R.id.login_button);
        TextView signupTextView = findViewById(R.id.signup_text);
        TextView findInfoTextView = findViewById(R.id.find_info_text);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartscreenActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signupTextView.setOnClickListener(v -> {
            Intent intent = new Intent(StartscreenActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        findInfoTextView.setOnClickListener(v -> {
            Intent intent = new Intent(StartscreenActivity.this, FindInfoActivity.class);
            startActivity(intent);
        });

        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    private void signIn() {
        // 기존에 로그인되어 있는 계정 정보가 있다면 로그아웃을 먼저 수행
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // signOut()이 완료되면 로그인 인텐트 시작
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google SignIn Account - Email: " + account.getEmail() + ", Name: " + account.getDisplayName());
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount googleAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Firebase Auth에서 이메일을 가져오되, 없으면 GoogleSignInAccount에서 가져오기
                            String userEmail = user.getEmail();
                            if (userEmail == null || userEmail.isEmpty()) {
                                userEmail = googleAccount.getEmail();
                            }

                            // 이름도 마찬가지로 처리
                            String userName = user.getDisplayName();
                            if (userName == null || userName.isEmpty()) {
                                userName = googleAccount.getDisplayName();
                            }

                            Log.d(TAG, "Firebase User - UID: " + userId + ", Email: " + userEmail + ", Name: " + userName);

                            // 최종 이메일과 이름 변수 (람다 내에서 사용하기 위해 final로 선언)
                            final String finalEmail = userEmail;
                            final String finalName = userName;

                            db.collection("users").document(userId)
                                    .get()
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            DocumentSnapshot document = dbTask.getResult();
                                            if (document.exists()) {
                                                Toast.makeText(StartscreenActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(StartscreenActivity.this, MainscreenActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(StartscreenActivity.this, "회원 정보 없음. 추가 정보 입력이 필요합니다.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(StartscreenActivity.this, Googleuser_AdditionalInfoActivity.class);
                                                intent.putExtra("email", finalEmail);
                                                intent.putExtra("name", finalName);
                                                intent.putExtra("uid", userId); // UID도 전달
                                                startActivity(intent);
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting document from Firestore: ", dbTask.getException());
                                            Toast.makeText(StartscreenActivity.this, "Firestore 조회 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(StartscreenActivity.this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(StartscreenActivity.this, "Firebase 인증 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}