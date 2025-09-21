package com.example.recycling_app.Community;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.data.ContentBlock;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.service.CommunityApiService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakePostActivity extends AppCompatActivity {

    private static final String TAG = "MakePostActivity";

    private EditText titleEditText;
    private CheckBox recycleCheckBox, marketCheckBox, eventCheckBox, qnaCheckBox;
    private CommunityApiService apiService;
    private String uid;
    private String authorName;
    private LinearLayout contentContainer;
    private List<ContentBlock> contentBlocks = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String postId; // 게시물 수정 시 사용할 postId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_makepost);

        Intent intent = getIntent();
        uid = intent.getStringExtra("USER_ID");
        authorName = intent.getStringExtra("USER_NICKNAME");
        postId = intent.getStringExtra("postId");

        if (uid == null || authorName == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        apiService = CommunityApiService.getInstance();

        titleEditText = findViewById(R.id.edit_text_title);
        recycleCheckBox = findViewById(R.id.checkbox_recycle);
        marketCheckBox = findViewById(R.id.checkbox_market);
        eventCheckBox = findViewById(R.id.checkbox_event);
        qnaCheckBox = findViewById(R.id.checkbox_qna);
        Button completeButton = findViewById(R.id.button_complete);
        Button addPhotoButton = findViewById(R.id.button_add_photo);
        contentContainer = findViewById(R.id.content_container);
        ConstraintLayout Top_body = findViewById(R.id.Top_body);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            // 시스템 바의 크기를 가져옵니다.
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (Top_body.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) Top_body.getLayoutParams();
                params.topMargin = topInset + (int) (getResources().getDisplayMetrics().density);
                Top_body.setLayoutParams(params);
            }

            // 하단 주소창의 하단 마진을 내비게이션 바 높이만큼 추가합니다.
            if (contentContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentContainer.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (getResources().getDisplayMetrics().density);
                contentContainer.setLayoutParams(params);
            }

            return WindowInsetsCompat.CONSUMED; // Insets을 소비했음을 시스템에 알립니다.
        });

        setupImagePickerLauncher();

        addPhotoButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 게시글 수정 모드일 경우 기존 데이터 불러오기
        if (postId != null) {
            loadPostData(postId);
        }

        completeButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String category = getSelectedCategory();

            // 마지막으로 입력된 텍스트가 있다면 ContentBlock에 추가
            if (contentContainer.getChildCount() > 0) {
                View lastView = contentContainer.getChildAt(contentContainer.getChildCount() - 1);
                if (lastView instanceof EditText) {
                    EditText lastEditText = (EditText) lastView;
                    String lastText = lastEditText.getText().toString().trim();
                    if (!lastText.isEmpty()) {
                        ContentBlock textBlock = new ContentBlock();
                        textBlock.setType("text");
                        textBlock.setText(lastText);
                        contentBlocks.add(textBlock);
                    }
                }
            }

            if (title.isEmpty() || category == null || contentBlocks.isEmpty()) {
                Toast.makeText(this, "제목, 카테고리, 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 모든 contentBlock에 순서(order) 부여
            for (int i = 0; i < contentBlocks.size(); i++) {
                contentBlocks.get(i).setOrder(i);
            }

            Post post = new Post();
            post.setPostId(postId); // 수정 시에는 postId 포함
            post.setTitle(title);
            post.setCategory(category);
            post.setUid(uid);
            post.setNickname(authorName);
            post.setContents(contentBlocks);

            if (postId == null) {
                writePost(post);
            } else {
                updatePost(post);
            }
        });
    }

    private void writePost(Post newPost) {
        apiService.writePost(newPost, new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MakePostActivity.this, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                handleFailureResponse(t);
            }
        });
    }

    private void updatePost(Post updatedPost) {
        apiService.updatePost(updatedPost.getPostId(), uid, updatedPost, new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MakePostActivity.this, "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                handleFailureResponse(t);
            }
        });
    }

    private void loadPostData(String postId) {
        apiService.getPostById(postId, uid, new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body();
                    titleEditText.setText(post.getTitle());
                    checkCategory(post.getCategory());
                    displayContents(post.getContents());
                } else {
                    Toast.makeText(MakePostActivity.this, "게시글 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(MakePostActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayContents(List<ContentBlock> contents) {
        contentBlocks.clear();
        contentContainer.removeAllViews();
        String currentText = "";
        for (ContentBlock block : contents) {
            if ("text".equals(block.getType())) {
                currentText = block.getText();
                addEditableText(currentText);
            } else if ("image".equals(block.getType())) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(0, 16, 0, 16);
                Glide.with(this).load(block.getMediaUrl()).into(imageView);
                contentContainer.addView(imageView);
                contentBlocks.add(block);
                addEditableText(""); // 이미지 뒤에 새 EditText 추가
            }
        }
    }
    private void addEditableText(String text) {
        EditText newEditText = new EditText(this);
        newEditText.setBackgroundResource(android.R.color.transparent);
        newEditText.setHint("내용을 입력해주세요.");
        newEditText.setText(text);
        contentContainer.addView(newEditText);
    }
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // 기존 텍스트 블록 저장
                        saveCurrentText();
                        // 이미지 업로드
                        uploadImageToFirebase(uri);
                    }
                });
    }
    private void saveCurrentText() {
        if (contentContainer.getChildCount() > 0) {
            View lastView = contentContainer.getChildAt(contentContainer.getChildCount() - 1);
            if (lastView instanceof EditText) {
                EditText lastEditText = (EditText) lastView;
                String lastText = lastEditText.getText().toString().trim();
                if (!lastText.isEmpty()) {
                    ContentBlock textBlock = new ContentBlock();
                    textBlock.setType("text");
                    textBlock.setText(lastText);
                    contentBlocks.add(textBlock);
                }
                lastEditText.setText(""); // 기존 EditText 내용 비우기
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        ContentBlock imageBlock = new ContentBlock();
                        imageBlock.setType("image");
                        imageBlock.setMediaUrl(imageUrl);
                        contentBlocks.add(imageBlock);

                        ImageView imageView = new ImageView(this);
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(0, 16, 0, 16);
                        Glide.with(this).load(imageUrl).into(imageView);
                        contentContainer.addView(imageView);

                        // 이미지 뒤에 새로운 EditText 추가
                        addEditableText("");

                        Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        Log.e("MakePostActivity", "Image upload failed", e);
                    });
        }
    }

    private String getSelectedCategory() {
        if (recycleCheckBox.isChecked()) return "분리수거";
        if (marketCheckBox.isChecked()) return "업사이클링 마켓";
        if (eventCheckBox.isChecked()) return "캠페인/이벤트";
        if (qnaCheckBox.isChecked()) return "Q&A";
        return "전체";
    }

    private void checkCategory(String category) {
        if ("분리수거".equals(category)) recycleCheckBox.setChecked(true);
        else if ("업사이클링 마켓".equals(category)) marketCheckBox.setChecked(true);
        else if ("캠페인/이벤트".equals(category)) eventCheckBox.setChecked(true);
        else if ("Q&A".equals(category)) qnaCheckBox.setChecked(true);
    }

    private void handleErrorResponse(Response<?> response) {
        String errorMessage = "게시글 처리 실패";
        try {
            if (response.errorBody() != null) {
                errorMessage += "\n" + response.errorBody().string();
                Log.e("MakePostActivity", "Error: " + response.code() + " " + errorMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(MakePostActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleFailureResponse(Throwable t) {
        Log.e("MakePostActivity", "Network Failure", t);
        Toast.makeText(MakePostActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
}