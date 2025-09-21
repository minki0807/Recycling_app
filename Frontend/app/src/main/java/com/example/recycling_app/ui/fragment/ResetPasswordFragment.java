package com.example.recycling_app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recycling_app.Account.LoginActivity;
import com.example.recycling_app.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ResetPasswordFragment extends Fragment {

    private static final String TAG = "ResetPasswordFragment";

    private EditText emailEditText;
    private Button resetPasswordSubmitButton;
    private LinearLayout initialInputGroup;
    private LinearLayout resultDisplayGroup;
    private EditText resetEmailDisplayEditText;
    private Button loginFromResetButton;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        mAuth = FirebaseAuth.getInstance();

        // UI 요소들을 초기화합니다.
        emailEditText = view.findViewById(R.id.edit_text_reset_password_email);
        resetPasswordSubmitButton = view.findViewById(R.id.button_reset_password_submit);
        initialInputGroup = view.findViewById(R.id.initial_input_group_reset_password);
        resultDisplayGroup = view.findViewById(R.id.result_display_group_reset_password);
        resetEmailDisplayEditText = view.findViewById(R.id.edit_text_reset_email_display);
        loginFromResetButton = view.findViewById(R.id.button_login_from_reset);

        // 초기 상태 설정
        initialInputGroup.setVisibility(View.VISIBLE);
        resultDisplayGroup.setVisibility(View.GONE);

        // '임시 비밀번호 발송' 버튼 클릭 리스너 설정
        resetPasswordSubmitButton.setOnClickListener(v -> {
            final String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "올바른 이메일 형식을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // 가입 방식을 확인하는 로직 호출
            checkSignInMethodAndSendEmail(email);
        });

        // '로그인하기' 버튼 클릭 리스너 (결과 화면에서)
        loginFromResetButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * 이메일의 가입 방식을 확인하고, 그에 따라 적절한 조치를 취합니다.
     * @param email 사용자가 입력한 이메일 주소
     */
    private void checkSignInMethodAndSendEmail(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (getContext() == null) {
                        return; // 프래그먼트가 화면에 없을 경우 작업 중단
                    }

                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();

                        if (signInMethods == null || signInMethods.isEmpty()) {
                            // 1. 가입되지 않은 이메일
                            Toast.makeText(getContext(), "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT).show();

                        } else if (signInMethods.contains("password")) {
                            // 2. 일반 이메일/비밀번호로 가입한 경우 -> 재설정 이메일 발송
                            sendPasswordResetEmail(email);

                        } else if (signInMethods.contains("google.com")) {
                            // 3. 구글 계정으로 가입한 경우 -> 안내 메시지 표시
                            Toast.makeText(getContext(), "구글 계정으로 가입된 이메일입니다.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // 확인 과정 중 오류 발생 (네트워크 문제 등)
                        Log.e(TAG, "Error checking sign-in methods: ", task.getException());
                        Toast.makeText(getContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 실제 비밀번호 재설정 이메일을 발송하고 UI를 업데이트합니다.
     * @param email 이메일을 보낼 주소
     */
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (getContext() == null) {
                        return; // 프래그먼트가 화면에 없을 경우 작업 중단
                    }
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "비밀번호 재설정 이메일을 전송했습니다.", Toast.LENGTH_SHORT).show();
                        // 성공 시 UI 변경
                        initialInputGroup.setVisibility(View.GONE);
                        resultDisplayGroup.setVisibility(View.VISIBLE);
                        resetEmailDisplayEditText.setText(email);
                    } else {
                        Log.e(TAG, "Failed to send password reset email: ", task.getException());
                        Toast.makeText(getContext(), "이메일 전송에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}