package com.example.recycling_app.Community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.adapter.PostAdapter;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.service.CommunityApiService;
import com.example.recycling_app.util.ProfileLoader; // ProfileLoader 임포트 추가

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherUserPageActivity extends AppCompatActivity {

    private static final String TAG = "OtherUserPageActivity";
    private ImageView profileImage;
    private TextView nicknameText;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private CommunityApiService apiService;
    private String authorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_otherpage);

        // Intent로부터 UID 가져오기
        Intent intent = getIntent();
        authorUid = intent.getStringExtra("authorUid");

        if (authorUid == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI 요소 초기화
        profileImage = findViewById(R.id.text_profile_initial);
        nicknameText = findViewById(R.id.other_nickname);
        recyclerView = findViewById(R.id.recycler_view_posts);

        // 어댑터 초기화 및 설정
        postAdapter = new PostAdapter(authorUid);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

        apiService = CommunityApiService.getInstance();

        // ProfileLoader를 사용하여 프로필 정보 로드
        ProfileLoader.loadProfileByUid(this, nicknameText, profileImage, authorUid); //

        // 작성 게시글 목록 로드
        fetchAuthoredPosts();

        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Intent intent = new Intent(OtherUserPageActivity.this, DetailActivity.class);
                intent.putExtra("postId", post.getPostId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Post post) {

            }

            @Override
            public void onDeleteClick(Post post) {

            }

            @Override
            public void onProfileClick(Post post) {

            }
        });
    }

    private void fetchAuthoredPosts() {
        apiService.getUserPosts(authorUid, new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postAdapter.submitList(response.body());
                } else {
                    Log.e(TAG, "작성 게시글 로드 실패: " + response.code() + " " + response.message());
                    Toast.makeText(OtherUserPageActivity.this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                Log.e(TAG, "작성 게시글 로드 통신 오류: ", t);
                Toast.makeText(OtherUserPageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}