package com.example.recycling_app.Account;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.recycling_app.R;
import com.example.recycling_app.ui.fragment.FindEmailFragment;
import com.example.recycling_app.ui.fragment.ResetPasswordFragment;

public class FindInfoActivity extends AppCompatActivity {

    private Button buttonFindEmail;
    private Button buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_info);

        buttonFindEmail = findViewById(R.id.button_find_email_tab);
        buttonResetPassword = findViewById(R.id.button_reset_password_tab);

        // 초기 화면 설정: 아이디 찾기 프래그먼트 로드 및 탭 상태 설정
        if (savedInstanceState == null) {
            loadFragment(new FindEmailFragment());
            setTabSelected(buttonFindEmail, true);
            setTabSelected(buttonResetPassword, false);
        }

        buttonFindEmail.setOnClickListener(v -> {
            loadFragment(new FindEmailFragment());
            setTabSelected(buttonFindEmail, true);
            setTabSelected(buttonResetPassword, false);
        });

        buttonResetPassword.setOnClickListener(v -> {
            loadFragment(new ResetPasswordFragment());
            setTabSelected(buttonResetPassword, true);
            setTabSelected(buttonFindEmail, false);
        });

        // EdgeToEdge 관련 코드: 시스템 바(상단바, 하단바)의 인셋을 고려하여 뷰의 패딩을 조정합니다.
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

    /**
     * 지정된 프래그먼트를 프래그먼트 컨테이너에 로드합니다.
     * @param fragment 로드할 Fragment 객체
     */
    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // 탭 선택 상태에 따라 배경 틴트와 텍스트 색상을 설정하는 헬퍼 메서드
    private void setTabSelected(Button button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.selected_tab_color)));
            button.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            button.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }
}
