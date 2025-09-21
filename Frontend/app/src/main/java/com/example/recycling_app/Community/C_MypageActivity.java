package com.example.recycling_app.Community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.adapter.PostAdapter;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.service.CommunityApiService;
import com.example.recycling_app.util.ProfileLoader;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class C_MypageActivity extends AppCompatActivity {

    private static final String TAG = "C_MypageActivity";
    private TextView mypageProfileText;
    private ImageView mypageProfileImage;
    private Button myPostButton, myCommentedPostButton, myLikedPostButton;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private CommunityApiService apiService;
    private FirebaseAuth mAuth;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_mypage);

        // Firebase 인증 및 API 서비스 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        apiService = CommunityApiService.getInstance();

        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // 사용자가 로그인되어 있을 때 프로필 로드
                ProfileLoader.loadProfile(this, mypageProfileText, mypageProfileImage, user, null);
                // 기본으로 내가 쓴 글 목록을 로드
                fetchMyPosts();
            } else {
                // 사용자가 로그아웃 상태일 때 처리
                Log.e(TAG, "사용자가 로그인되어 있지 않습니다.");
                Toast.makeText(C_MypageActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                // 모든 데이터를 지우고 로그인 화면으로 이동
                postAdapter.submitList(null);
            }
        });

        mypageProfileText = findViewById(R.id.mypage_nickname);
        mypageProfileImage = findViewById(R.id.text_profile_initial);
        myPostButton = findViewById(R.id.mypost);
        myCommentedPostButton = findViewById(R.id.mycommentpost);
        myLikedPostButton = findViewById(R.id.mylikepost);
        recyclerView = findViewById(R.id.recycler_view_posts);

        mAuth = FirebaseAuth.getInstance();
        apiService = CommunityApiService.getInstance();

        // 어댑터 초기화 및 설정
        postAdapter = new PostAdapter(mAuth.getUid()); // PostAdapter에 현재 사용자의 UID 전달
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (mypageProfileImage.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mypageProfileImage.getLayoutParams();
                params.topMargin = topInset + (int) (getResources().getDisplayMetrics().density);
                mypageProfileImage.setLayoutParams(params);
            }
            if (recyclerView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (getResources().getDisplayMetrics().density);
                recyclerView.setLayoutParams(params);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Intent intent = new Intent(C_MypageActivity.this, DetailActivity.class);
                intent.putExtra("postId", post.getPostId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Post post) {
                Intent intent = new Intent(C_MypageActivity.this, MakePostActivity.class);
                intent.putExtra("postId", post.getPostId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Post post) {
                deletePost(post);
            }

            @Override
            public void onProfileClick(Post post) {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String authorUid = post.getUid();
                if (currentUid.equals(authorUid)) {
                    Intent intent = new Intent(C_MypageActivity.this, C_MypageActivity.class);
                    intent.putExtra("authorUid", authorUid);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(C_MypageActivity.this, OtherUserPageActivity.class);
                    intent.putExtra("authorUid", authorUid);
                    startActivity(intent);
                }
            }
        });

        // 버튼 클릭 리스너 설정
        myPostButton.setOnClickListener(v -> {
            fetchMyPosts();
            setCategoryButtonColor((MaterialButton) myPostButton);
        });
        myCommentedPostButton.setOnClickListener(v -> {
            fetchMyCommentedPosts();
            setCategoryButtonColor((MaterialButton) myCommentedPostButton);
        });
        myLikedPostButton.setOnClickListener(v -> {
            fetchMyLikedPosts();
            setCategoryButtonColor((MaterialButton) myLikedPostButton);
        });
        setCategoryButtonColor((MaterialButton) myPostButton);

        ProfileLoader.loadProfile(this, mypageProfileText, mypageProfileImage, mAuth.getCurrentUser(), profile -> {
            fetchMyPosts();
        });
    }

    private void setCategoryButtonColor(MaterialButton selectedButton) {
        myCommentedPostButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
        myPostButton.setTextColor(getResources().getColor(android.R.color.darker_gray));
        myLikedPostButton.setTextColor(getResources().getColor(android.R.color.darker_gray));

        selectedButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void fetchMyPosts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "사용자 로그인 정보가 없어 게시글을 불러올 수 없습니다.");
            return;
        }
        Log.d(TAG, "내가 쓴 게시글 로드 시작");
        // CommunityApiService에서 불필요한 UID 파라미터 제거
        apiService.getMyPosts(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> myPosts = response.body();
                    Log.d(TAG, "내가 쓴 게시글 로드 성공, 총 " + myPosts.size() + "개");
                    postAdapter.submitList(myPosts);
                } else {
                    Log.e(TAG, "내가 쓴 게시글 로드 실패: " + response.code() + " " + response.message());
                    Toast.makeText(C_MypageActivity.this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                Log.e(TAG, "내가 쓴 게시글 로드 통신 오류: ", t);
                Toast.makeText(C_MypageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchMyCommentedPosts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "사용자 로그인 정보가 없어 댓글 단 게시글을 불러올 수 없습니다.");
            return;
        }
        Log.d(TAG, "내가 댓글 단 게시글 로드 시작");
        apiService.getPostsCommentedByMe(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> commentedPosts = response.body();
                    Log.d(TAG, "내가 댓글 단 게시글 로드 성공, 총 " + commentedPosts.size() + "개");
                    postAdapter.submitList(commentedPosts);
                } else {
                    Log.e(TAG, "내가 댓글 단 게시글 로드 실패: " + response.code() + " " + response.message());
                    Toast.makeText(C_MypageActivity.this, "댓글 단 게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                Log.e(TAG, "내가 댓글 단 게시글 로드 통신 오류: ", t);
                Toast.makeText(C_MypageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMyLikedPosts() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "사용자 로그인 정보가 없어 좋아요한 게시글을 불러올 수 없습니다.");
            return;
        }
        Log.d(TAG, "좋아요한 게시글 로드 시작");
        apiService.getMyLikedPosts(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> likedPosts = response.body();
                    Log.d(TAG, "좋아요한 게시글 로드 성공, 총 " + likedPosts.size() + "개");
                    postAdapter.submitList(likedPosts);
                } else {
                    Log.e(TAG, "좋아요한 게시글 로드 실패: " + response.code() + " " + response.message());
                    Toast.makeText(C_MypageActivity.this, "좋아요한 게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                Log.e(TAG, "좋아요한 게시글 로드 통신 오류: ", t);
                Toast.makeText(C_MypageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost(Post post) {
    }
}
