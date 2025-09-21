package com.example.recycling_app.Community;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recycling_app.Camera_recognition.CameraActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.adapter.PostAdapter;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.service.CommunityApiService;
import com.example.recycling_app.util.ProfileLoader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityActivity extends AppCompatActivity {

    private static final String TAG = "CommunityActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CommunityApiService repository;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private String currentUid, currentUserNickname, currentCategory = "전체";
    private ImageView profileImageView, underbar;
    private ImageButton camera_icon, map_icon, home_icon, account_icon;
    private MaterialButton textAll, textrecycle, textMarket, textEvent, textQnA;
    private FloatingActionButton fab_write;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListenerRegistration postsListener;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.communtiy_main);

        recyclerView = findViewById(R.id.recycler_view_posts);
        repository = CommunityApiService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adapter = new PostAdapter(mAuth.getCurrentUser().getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        textAll = findViewById(R.id.textAll);
        textrecycle = findViewById(R.id.textrecycle);
        textMarket = findViewById(R.id.textMarket);
        textEvent = findViewById(R.id.textEvent);
        textQnA = findViewById(R.id.textQnA);
        fab_write = findViewById(R.id.fab_write);
        underbar = findViewById(R.id.underbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        camera_icon = findViewById(R.id.camera_icon);
        map_icon = findViewById(R.id.map_icon);
        home_icon = findViewById(R.id.home_icon);
        account_icon = findViewById(R.id.account_icon);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
        } else {
            currentUid = null;
        }

            WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            if (underbar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) underbar.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (getResources().getDisplayMetrics().density);
                underbar.setLayoutParams(params);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        setupListeners();
        fetchPostsWithRealtimeListener(currentCategory);
    }

    private void setupListeners() {

        adapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Intent intent = new Intent(CommunityActivity.this, DetailActivity.class);
                intent.putExtra("postId", post.getPostId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Post post) {
                if (currentUid != null && currentUid.equals(post.getUid())) {
                    Intent intent = new Intent(CommunityActivity.this, MakePostActivity.class);
                    intent.putExtra("postId", post.getPostId());
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(Post post) {
                if (currentUid != null && currentUid.equals(post.getUid())) {
                    new AlertDialog.Builder(CommunityActivity.this)
                            .setTitle("게시글 삭제")
                            .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                            .setPositiveButton("삭제", (dialog, which) -> deletePost(post))
                            .setNegativeButton("취소", null)
                            .show();
                }
            }

            @Override
            public void onProfileClick(Post post) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String currentUid = user.getUid();
                    String authorUid = post.getUid();
                    if (currentUid.equals(authorUid)) {
                        Intent intent = new Intent(CommunityActivity.this, C_MypageActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(CommunityActivity.this, OtherUserPageActivity.class);
                        intent.putExtra("authorUid", authorUid);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(CommunityActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fab_write.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(CommunityActivity.this, MakePostActivity.class);
                intent.putExtra("USER_ID", currentUid);
                intent.putExtra("USER_NICKNAME", currentUserNickname);
                startActivity(intent);
            } else {
                Toast.makeText(CommunityActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchPostsWithRealtimeListener(currentCategory); // 현재 카테고리로 게시글 새로고침
        });

        textAll.setOnClickListener(v -> {
            currentCategory = "전체";
            fetchPostsWithRealtimeListener(currentCategory);
            setCategoryButtonColor(textAll);
        });
        textrecycle.setOnClickListener(v -> {
            currentCategory = "분리수거";
            fetchPostsWithRealtimeListener(currentCategory);
            setCategoryButtonColor(textrecycle);
        });
        textMarket.setOnClickListener(v -> {
            currentCategory = "업사이클링 마켓";
            fetchPostsWithRealtimeListener(currentCategory);
            setCategoryButtonColor(textMarket);
        });
        textEvent.setOnClickListener(v -> {
            currentCategory = "캠페인/이벤트";
            fetchPostsWithRealtimeListener(currentCategory);
            setCategoryButtonColor(textEvent);
        });
        textQnA.setOnClickListener(v -> {
            currentCategory = "Q&A";
            fetchPostsWithRealtimeListener(currentCategory);
            setCategoryButtonColor(textQnA);
        });

        home_icon.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, MainscreenActivity.class);
            startActivity(intent);
        });
        map_icon.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, LocationActivity.class);
            startActivity(intent);
        });
        camera_icon.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, CameraActivity.class);
            startActivity(intent);
        });
        account_icon.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, MypageActivity.class);
            startActivity(intent);
        });

        setCategoryButtonColor(textAll);

        profileImageView = findViewById(R.id.text_profile_initial);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // ProfileLoader에 콜백 함수를 추가하여 닉네임을 받아옵니다.
            ProfileLoader.loadProfile(this, null, profileImageView, user, profile -> {
                if (profile != null) {
                    this.currentUserNickname = profile.getNickname();
                } else {
                    this.currentUserNickname = "익명";
                }
            });

            profileImageView.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityActivity.this, C_MypageActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setCategoryButtonColor(MaterialButton selectedButton) {
        // 모든 버튼의 색상을 기본값으로 초기화
        textAll.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textrecycle.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textMarket.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textEvent.setTextColor(getResources().getColor(android.R.color.darker_gray));
        textQnA.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // 선택된 버튼의 색상만 변경
        selectedButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void deletePost(Post post) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || !user.getUid().equals(post.getUid())) {
            Toast.makeText(CommunityActivity.this, "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게시글 삭제").setMessage("정말로 이 게시글을 삭제하시겠습니까?").setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String uid = mAuth.getCurrentUser().getUid();
                        repository.deletePost(post.getPostId(), currentUid, new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(CommunityActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    fetchPostsWithRealtimeListener("전체");
                                } else {
                                    String errorMessage = "삭제 실패";
                                    if (response.errorBody() != null) {
                                        try {
                                            errorMessage += ": " + response.errorBody().string();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    Log.e("CommunityActivity", "Delete failed: " + errorMessage);
                                    Toast.makeText(CommunityActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(CommunityActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchPostsWithRealtimeListener(String category) {
        if (postsListener != null) {
            postsListener.remove();
        }

        Query query;
        if ("전체".equals(category)) {
            query = db.collection("posts")
                    .whereEqualTo("deleted", false)
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        } else {
            query = db.collection("posts")
                    .whereEqualTo("category", category)
                    .whereEqualTo("deleted", false)
                    .orderBy("createdAt", Query.Direction.DESCENDING);
        }

        postsListener = (query).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "게시글 실시간 업데이트 오류", e);
                Toast.makeText(CommunityActivity.this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                return;
            }

            if (queryDocumentSnapshots != null) {
                List<Post> posts = queryDocumentSnapshots.toObjects(Post.class);
                adapter.submitList(posts);
                Log.d(TAG, "게시글 목록 실시간 업데이트 완료: " + posts.size() + "개");
            }

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 메모리 누수 방지를 위해 리스너 해제
        if (postsListener != null) {
            postsListener.remove();
        }
    }
}