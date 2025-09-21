package com.example.recycling_app.Upcycling_market;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.recycling_app.R;
import com.example.recycling_app.ui.fragment.MyChatsFragment;
import com.example.recycling_app.ui.fragment.MyProductsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Seller_Profile extends AppCompatActivity {

    private static final String TAG = "Seller_Profile";

    private Button btnProducts, btnChats;
    private ImageView ivBack;
    private TextView tvNickname;
    private ImageView ivProfile;
    private View underlineProducts, underlineChats;
    private EditText etsearch;
    private Fragment currentFragment; // 현재 프래그먼트를 추적하기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        btnProducts = findViewById(R.id.btnProducts);
        btnChats = findViewById(R.id.btnChats);
        ivBack = findViewById(R.id.seller_profile_back);
        tvNickname = findViewById(R.id.tvNickname);
        ivProfile = findViewById(R.id.ivProfile);
        underlineProducts = findViewById(R.id.underline_products);
        underlineChats = findViewById(R.id.underline_chats);
        etsearch = findViewById(R.id.et_search);

        // 뒤로가기 버튼 클릭 리스너 설정
        ivBack.setOnClickListener(v -> onBackPressed());

        // 기본적으로 내 제품 Fragment를 표시
        if (savedInstanceState == null) {
            replaceFragment(new MyProductsFragment());
            setButtonSelected(btnProducts, btnChats);
        }

        // 제품 버튼 클릭 리스너
        btnProducts.setOnClickListener(v -> {
            replaceFragment(new MyProductsFragment());
            setButtonSelected(btnProducts, btnChats);
        });

        // 채팅 버튼 클릭 리스너
        btnChats.setOnClickListener(v -> {
            replaceFragment(new MyChatsFragment());
            setButtonSelected(btnChats, btnProducts);
        });

        // 사용자 이름 및 프로필 설정
        loadUserProfile();

        // 검색 기능 설정
        setupSearch();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // 닉네임 설정
                            String nickname = documentSnapshot.getString("nickname");
                            tvNickname.setText(nickname != null ? nickname : "사용자");

                            // 프로필 사진 설정 (Glide 사용)
                            String profileUrl = documentSnapshot.getString("profileImageUrl");
                            Glide.with(this)
                                    .load(profileUrl)
                                    .placeholder(R.drawable.basic_profile_logo)
                                    .error(R.drawable.basic_profile_logo)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(ivProfile);
                        }
                    });
        }
    }

    private void replaceFragment(Fragment fragment) {
        currentFragment = fragment; // 현재 프래그먼트 업데이트
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }

    private void setButtonSelected(Button selectedButton, Button unselectedButton) {
        // 밑줄 가시성 설정
        if (selectedButton.getId() == R.id.btnProducts) {
            underlineProducts.setVisibility(View.VISIBLE);
            underlineChats.setVisibility(View.GONE);
        } else {
            underlineProducts.setVisibility(View.GONE);
            underlineChats.setVisibility(View.VISIBLE);
        }
    }

    // 검색창 설정 메소드
    private void setupSearch() {
        // 1. 텍스트가 변경될 때마다 실시간으로 검색 (기존 기능)
        etsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                if (currentFragment instanceof MyProductsFragment) {
                    ((MyProductsFragment) currentFragment).search(query);
                } else if (currentFragment instanceof MyChatsFragment) {
                    ((MyChatsFragment) currentFragment).search(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. 키보드의 '검색' 버튼(엔터키)을 눌렀을 때 검색 실행 (추가된 기능)
        etsearch.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 검색 동작을 명시적으로 호출할 필요는 없습니다.
                // TextWatcher가 이미 텍스트 변경을 감지하고 있기 때문입니다.
                // 여기서는 키보드를 숨기는 역할만 수행합니다.
                hideKeyboard();
                return true; // 이벤트 처리를 완료했음을 알림
            }
            return false; // 기본 동작 수행
        });
    }

    // 키보드를 숨기는 메소드
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}