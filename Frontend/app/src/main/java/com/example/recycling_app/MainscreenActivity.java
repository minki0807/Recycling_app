package com.example.recycling_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Howtobox.Wasteguide;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.Upcycling_market.Upcycling_market_mainscreen;

public class MainscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        ImageButton camerabox = findViewById(R.id.camera_box);
        ImageButton mapbox = findViewById(R.id.map_box);
        ImageButton howtobox = findViewById(R.id.howto_box);
        ImageButton upcyclingmarketbox = findViewById(R.id.upcycling_market_box);

        ImageButton mapicon = findViewById(R.id.map_icon);
        ImageButton cameraicon = findViewById(R.id.camera_icon);
        ImageButton messageicon = findViewById(R.id.message_icon);
        ImageButton accounticon = findViewById(R.id.account_icon);

        ImageView main_header = findViewById(R.id.main_header_top);
        ImageView underbar = findViewById(R.id.underbar);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            // 시스템 바의 크기를 가져옵니다.
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (main_header.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) main_header.getLayoutParams();
                params.topMargin = topInset + (int) (getResources().getDisplayMetrics().density);
                main_header.setLayoutParams(params);
            }

            // 하단 주소창의 하단 마진을 내비게이션 바 높이만큼 추가합니다.
            if (underbar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) underbar.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (getResources().getDisplayMetrics().density);
                underbar.setLayoutParams(params);
            }

            return WindowInsetsCompat.CONSUMED; // Insets을 소비했음을 시스템에 알립니다.
        });

        // 상단바 아이콘과 글씨 색상을 어둡게 설정 (Light Mode)
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        // 카메라 이동
        camerabox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        cameraicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });


        // 지도 이동
        mapbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        mapicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });

        // 재활용 방법 이동
        howtobox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, Wasteguide.class);
                startActivity(intent);
            }
        });

        // 업사이클링 마켓 이동
        upcyclingmarketbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, Upcycling_market_mainscreen.class);
                startActivity(intent);
            }
        });
        
        // 커뮤니티 이동
        messageicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, CommunityActivity.class);
                startActivity(intent);
            }
        });


        // 마이페이지 이동
        accounticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainscreenActivity.this, MypageActivity.class);
                startActivity(intent);
            }
        });

    }
}