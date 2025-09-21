package com.example.recycling_app.Community;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.adapter.CommentAdapter;
import com.example.recycling_app.data.ContentBlock;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.data.Comment;
import com.example.recycling_app.service.CommunityApiService;
import com.example.recycling_app.util.ProfileLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private TextView postTitle, postNickname, postCreatedAt, postLikesCount, postCommentsCount;
    private ImageView postProfileImage, postLikesIcon;
    private LinearLayout contentDisplayContainer; // 게시글 본문 동적 추가를 위한 컨테이너
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageButton commentSendButton;
    private String postId;
    private CommunityApiService apiService;
    private CommentAdapter commentAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_detail);

        mAuth = FirebaseAuth.getInstance();
        apiService = CommunityApiService.getInstance();

        // UI 요소 초기화
        postTitle = findViewById(R.id.text_post_title);
        postNickname = findViewById(R.id.text_author_nickname);
        postLikesCount = findViewById(R.id.text_like_count);
        postCommentsCount = findViewById(R.id.text_comment_count);
        postProfileImage = findViewById(R.id.text_profile_initial);
        postLikesIcon = findViewById(R.id.button_like);
        contentDisplayContainer = findViewById(R.id.content_display_container);
        commentsRecyclerView = findViewById(R.id.recycler_view_comments);
        commentInput = findViewById(R.id.edit_comment);
        commentSendButton = findViewById(R.id.edit_upload);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // postId 가져오기
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "게시글 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 댓글 어댑터 초기화 및 리스너 설정
        String currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        commentAdapter = new CommentAdapter(currentUid);

        // CommentAdapter에 리스너 설정
        commentAdapter.setOnItemClickListener(new CommentAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Comment comment) {
                // 댓글 수정 다이얼로그 표시
                showEditCommentDialog(comment);
            }

            @Override
            public void onDeleteClick(Comment comment) {
                // 댓글 삭제 확인 다이얼로그 표시
                showDeleteCommentDialog(comment);
            }

            @Override
            public void onReplyClick(Comment comment) {
                // 댓글 입력창에 답장할 닉네임 자동 추가
                String replyPrefix = "@" + comment.getNickname() + " ";
                commentInput.setText(replyPrefix);
                commentInput.setSelection(replyPrefix.length()); // 커서 위치를 텍스트 끝으로 이동
                commentInput.requestFocus();
                // 키보드 자동으로 올리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentInput, InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onProfileClick(Comment comment) {
                // 댓글 작성자 프로필 보기
                Intent intent = new Intent(DetailActivity.this, OtherUserPageActivity.class);
                intent.putExtra("uid", comment.getUid());
                startActivity(intent);
            }
        });

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        // 스와이프 새로고침 설정
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchPostDetails();
            fetchComments();
        });

        // 댓글 작성 버튼 클릭 리스너
        commentSendButton.setOnClickListener(v -> {
            String commentContent = commentInput.getText().toString().trim();
            if (!commentContent.isEmpty()) {
                createComment(commentContent);
            } else {
                Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 초기 데이터 로드
        fetchPostDetails();
        fetchComments();
    }

    private void showEditCommentDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("댓글 수정");

        final EditText input = new EditText(this);
        input.setText(comment.getContent());
        builder.setView(input);

        builder.setPositiveButton("수정", (dialog, which) -> {
            String newContent = input.getText().toString().trim();
            if (!newContent.isEmpty()) {
                updateComment(comment.getCommentId(), newContent);
            } else {
                Toast.makeText(DetailActivity.this, "수정할 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateComment(String commentId, String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.updateComment(commentId, content, user.getUid(), new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    fetchComments(); // 댓글 목록 새로고침
                } else {
                    Log.e(TAG, "댓글 수정 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "댓글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "댓글 수정 통신 오류: ", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteCommentDialog(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("댓글 삭제")
                .setMessage("정말로 이 댓글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteComment(comment.getCommentId()))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteComment(String commentId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteComment(commentId, user.getUid(), new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    fetchComments(); // 댓글 목록 새로고침
                } else {
                    Log.e(TAG, "댓글 삭제 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "댓글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "댓글 삭제 통신 오류: ", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPostDetails() {
        Log.d(TAG, "게시글 상세 정보 로드 시작, postId: " + postId);
        apiService.getPostById(postId, new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body();
                    Log.d(TAG, "게시글 상세 정보 로드 성공: " + post.getTitle());
                    Log.d(TAG, "게시글 내용: " + (post.getContents() != null ? post.getContents().size() : 0) + "개");
                    displayPost(post);
                    // 좋아요 상태에 따라 아이콘 변경
                    updateLikeIcon(post.isLikedByCurrentUser());
                } else {
                    Log.e(TAG, "게시글 상세 정보 로드 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "게시글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Log.e(TAG, "네트워크 오류 발생", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPost(Post post) {
        postTitle.setText(post.getTitle());
        postNickname.setText(post.getNickname());
        postCreatedAt.setText(formatDate(post.getCreatedAt()));
        postLikesCount.setText(String.valueOf(post.getLikesCount()));
        postCommentsCount.setText(String.valueOf(post.getCommentsCount()));
        ProfileLoader.loadProfileByUid(this, null, postProfileImage, post.getUid());

        // 게시글 본문 동적 뷰 로직
        displayPostContents(post.getContents());

        // 좋아요 버튼 클릭 리스너
        postLikesIcon.setOnClickListener(v -> toggleLike(post.getPostId()));
    }

    private void displayPostContents(List<ContentBlock> contents) {
        // 기존 뷰 제거
        contentDisplayContainer.removeAllViews();

        if (contents == null || contents.isEmpty()) {
            Log.d(TAG, "게시글 본문 내용이 없습니다.");
            return;
        }

        // 순서에 따라 정렬 (백엔드에서 이미 정렬되어 온다고 가정, 안정성을 위해 추가)
        Collections.sort(contents, Comparator.comparingInt(ContentBlock::getOrder));

        for (ContentBlock block : contents) {
            if ("text".equals(block.getType())) {
                TextView textView = new TextView(this);
                textView.setText(block.getText());
                textView.setTextSize(16);
                textView.setPadding(0, 8, 0, 8);
                contentDisplayContainer.addView(textView);
            } else if ("image".equals(block.getType())) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(0, 16, 0, 16);
                Glide.with(this).load(block.getMediaUrl()).into(imageView);
                contentDisplayContainer.addView(imageView);
            }
        }
    }

    private void fetchComments() {
        Log.d(TAG, "댓글 목록 로드 시작, postId: " + postId);
        apiService.getComments(postId, new Callback<List<Comment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    Log.d(TAG, "댓글 로드 성공, 총 " + comments.size() + "개");
                    commentAdapter.submitList(comments);
                } else {
                    Log.e(TAG, "댓글 로드 실패: " + response.code() + " " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                Log.e(TAG, "댓글 로드 통신 오류: ", t);
            }
        });
    }

    private void createComment(String content) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.createComment(postId, content, user.getUid(), new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    hideKeyboard();
                    fetchComments(); // 댓글 목록 새로고침
                } else {
                    Log.e(TAG, "댓글 작성 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "댓글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Log.e(TAG, "댓글 작성 통신 오류: ", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike(String postId) {
        apiService.toggleLikes(postId, new Call<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchPostDetails(); // 좋아요 상태 변경 후 게시글 정보 갱신
                } else {
                    Log.e(TAG, "좋아요 토글 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "좋아요 상태 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "좋아요 토글 통신 오류: ", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeIcon(boolean isLiked) {
        if (isLiked) {
            postLikesIcon.setImageResource(R.drawable.icon_like_red); // 채워진 하트 이미지로 변경
        } else {
            postLikesIcon.setImageResource(R.drawable.icon_like); // 빈 하트 이미지로 변경
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        return formatter.format(date);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
