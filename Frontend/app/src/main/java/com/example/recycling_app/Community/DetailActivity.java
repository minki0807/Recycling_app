package com.example.recycling_app.Community;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private String postId;
    private String authorUid;
    private CommunityApiService apiService;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration postListener;
    private ListenerRegistration commentsListener;

    private TextView textAuthorNickname, textPostDate, textPostTitle, textCommentCount, textLikeCount;
    private LinearLayout contentDisplayContainer;
    private RecyclerView recyclerViewComments;
    private NestedScrollView scrollViewContent;
    private EditText edit_comment;
    private ImageButton edit_upload, buttonLike;
    private Post currentPost;
    private String currentUid;
    private String currentUserNickname;
    private String replyToCommentId;
    private CommentAdapter commentAdapter;
    private String editingCommentId = null;
    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_detail);

        // Intent에서 postId 가져오기. null 체크를 onCreate 초기에 수행합니다.
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 뷰를 먼저 초기화
        ImageView text_profile_initial = findViewById(R.id.text_profile_initial);
        LinearLayout layout_comment_input = findViewById(R.id.layout_comment_input);
        textAuthorNickname = findViewById(R.id.text_author_nickname);
        textPostDate = findViewById(R.id.text_post_date);
        textPostTitle = findViewById(R.id.text_post_title);
        contentDisplayContainer = findViewById(R.id.content_display_container);
        recyclerViewComments = findViewById(R.id.recycler_view_comments);
        edit_comment = findViewById(R.id.edit_comment);
        edit_upload = findViewById(R.id.edit_upload);
        scrollViewContent = findViewById(R.id.scroll_view_content);
        textLikeCount = findViewById(R.id.text_like_count);
        buttonLike = findViewById(R.id.button_like);
        textCommentCount = findViewById(R.id.text_comment_count);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            // 시스템 바의 크기를 가져옵니다.
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (text_profile_initial.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) text_profile_initial.getLayoutParams();
                params.topMargin = topInset + (int) (getResources().getDisplayMetrics().density);
                text_profile_initial.setLayoutParams(params);
            }

            // 하단 주소창의 하단 마진을 내비게이션 바 높이만큼 추가합니다.
            if (layout_comment_input.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout_comment_input.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (getResources().getDisplayMetrics().density);
                layout_comment_input.setLayoutParams(params);
            }

            return WindowInsetsCompat.CONSUMED; // Insets을 소비했음을 시스템에 알립니다.
        });

        setupWindowInsets();

        apiService = CommunityApiService.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // swipeRefreshLayout이 초기화된 후 리스너를 설정
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                fetchPostDetails();
            });
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUid = currentUser.getUid();
            // ProfileLoader를 사용하여 현재 사용자 닉네임 로드
            ProfileLoader.loadProfile(this, null, null, currentUser, profile -> {
                if (profile != null) {
                    currentUserNickname = profile.getNickname();
                } else {
                    currentUserNickname = "익명";
                }
                initUiAfterNicknameLoad();
            });
        } else {
            currentUid = null;
            currentUserNickname = "익명";
            initUiAfterNicknameLoad();
        }
    }

    private void setupRealtimeListeners() {
        // 게시글 정보 실시간 리스너
        DocumentReference postRef = db.collection("posts").document(postId);
        postListener = postRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "게시글 실시간 업데이트 오류.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Post post = documentSnapshot.toObject(Post.class);
                if (post != null) {
                    updatePostUI(post);
                }
            } else {
                Toast.makeText(DetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void sortRepliesRecursively(List<Comment> comments) {
        Collections.sort(comments, Comparator.comparing(Comment::getCreatedAt));
        for (Comment comment : comments) {
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                sortRepliesRecursively(comment.getReplies());
            }
        }
    }
    private List<Comment> flattenCommentTreeWithDepth(List<Comment> rootComments) {
        List<Comment> flattenedList = new ArrayList<>();
        for (Comment rootComment : rootComments) {
            flattenedList.add(rootComment);
            if (rootComment.getReplies() != null && !rootComment.getReplies().isEmpty()) {
                flattenedList.addAll(flattenReplies(rootComment.getReplies(), 1));
            }
        }
        return flattenedList;
    }

    private List<Comment> flattenReplies(List<Comment> replies, int depth) {
        List<Comment> flattenedReplies = new ArrayList<>();
        for (Comment reply : replies) {
            reply.setDepth(depth); // 댓글 객체에 depth 정보 추가
            flattenedReplies.add(reply);
            if (reply.getReplies() != null && !reply.getReplies().isEmpty()) {
                flattenedReplies.addAll(flattenReplies(reply.getReplies(), depth + 1));
            }
        }
        return flattenedReplies;
    }
    public List<Comment> buildCommentTree(List<Comment> flatComments) {
        Map<String, Comment> commentMap = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();

        for (Comment comment : flatComments) {
            commentMap.put(comment.getCommentId(), comment);
        }

        for (Comment comment : flatComments) {
            String parentId = comment.getParentId();
            if (parentId == null || parentId.isEmpty()) {
                comment.setDepth(0); // 1차 댓글의 깊이는 0
                rootComments.add(comment);
            } else {
                Comment parentComment = commentMap.get(parentId);
                if (parentComment != null) {
                    if (parentComment.getReplies() == null) {
                        parentComment.setReplies(new ArrayList<>());
                    }
                    comment.setDepth(parentComment.getDepth() + 1); // 부모 댓글의 깊이 + 1
                    parentComment.getReplies().add(comment);
                }
            }
        }

        return flattenCommentTreeWithDepth(rootComments);
    }

    private void updatePostUI(Post post) {
        // 게시글 UI 업데이트
        textPostTitle.setText(post.getTitle());
        textAuthorNickname.setText(post.getNickname());
        textPostDate.setText(formatDate(post.getCreatedAt()));
        textLikeCount.setText(String.valueOf(post.getLikesCount()));
        textCommentCount.setText(String.valueOf(post.getCommentsCount()));
        authorUid = post.getUid();

        // 좋아요 버튼 상태 업데이트
        boolean isLiked = post.isLikedByCurrentUser();
        buttonLike.setImageResource(isLiked ? R.drawable.icon_like_red : R.drawable.icon_like);
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(date);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (postListener != null) {
            postListener.remove();
        }
        if (commentsListener != null) {
            commentsListener.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postListener != null) {
            postListener.remove(); // 메모리 누수 방지
        }
    }


    private void initUiAfterNicknameLoad() {
        commentAdapter = new CommentAdapter(currentUid);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
        setupCommentAdapterListener();
        setupRealtimeCommentsListener(); // 댓글 리스너를 여기서 호출

        ImageButton moreOptionsButton = findViewById(R.id.button_more_options);
        if (moreOptionsButton != null) {
            moreOptionsButton.setOnClickListener(v -> {
                if (currentPost != null) {
                    showPopupMenu(v, currentPost);
                } else {
                    Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setupCommentUploadButton();
        fetchPostDetails();
        setupLikeButton();
    }

    private void setupRealtimeCommentsListener() {
        // 댓글 목록 실시간 리스너
        Query commentsQuery = db.collection("comments")
                .whereEqualTo("postId", postId)
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.ASCENDING);

        commentsListener = commentsQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "댓글 실시간 업데이트 오류.", e);
                // 오류 발생 시 사용자에게 토스트 메시지 표시
                Toast.makeText(DetailActivity.this, "댓글을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                // Firestore 문서들을 Comment 객체 리스트로 변환
                List<Comment> flatComments = queryDocumentSnapshots.toObjects(Comment.class);

                // 가져온 모든 댓글을 계층 구조로 재구성 (1차 댓글, 대댓글 등)
                List<Comment> structuredComments = buildCommentTree(flatComments);

                // 어댑터에 데이터 업데이트
                if (commentAdapter != null) {
                    commentAdapter.submitList(structuredComments);
                }

                Log.d(TAG, "댓글 목록 실시간 업데이트 완료: " + structuredComments.size() + "개");
            }
        });
    }

    private void setupCommentAdapterListener() {
        commentAdapter.setOnItemClickListener(new CommentAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Comment comment) {
                // 댓글 수정 로직 구현
                editingCommentId = comment.getCommentId();
                replyToCommentId = null; // 수정 모드에서는 대댓글 모드 비활성화
                edit_comment.setText(comment.getContent());
                edit_comment.setHint("댓글 수정 중...");
                edit_comment.requestFocus();
                // 키보드 보이기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (edit_comment.getWindowToken() != null) {
                    imm.showSoftInput(edit_comment, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void onDeleteClick(Comment comment) {
                new AlertDialog.Builder(DetailActivity.this)
                        .setTitle("댓글 삭제")
                        .setMessage("정말로 이 댓글을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> deleteComment(comment.getCommentId()))
                        .setNegativeButton("취소", null)
                        .show();
            }

            @Override
            public void onReplyClick(Comment comment) {
                replyToCommentId = comment.getCommentId();
                editingCommentId = null; // 대댓글 모드에서는 수정 모드 비활성화
                edit_comment.setHint(comment.getNickname() + "님에게 답장 중...");
                edit_comment.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (edit_comment.getWindowToken() != null) {
                    imm.showSoftInput(edit_comment, InputMethodManager.SHOW_IMPLICIT);
                }
            }


            @Override
            public void onProfileClick(Comment comment) {

            }
        });
    }

    private void deleteComment(String commentId) {
        apiService.deleteComment(commentId, currentUid, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "댓글 삭제 실패";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "Comment delete failed: " + errorMessage);
                    Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Comment delete network failure", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View view, Post post) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                Intent intent = new Intent(DetailActivity.this, MakePostActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("USER_ID", currentUid);
                intent.putExtra("USER_NICKNAME", currentUserNickname);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete) {
                new AlertDialog.Builder(this)
                        .setTitle("게시글 삭제")
                        .setMessage("정말로 이 게시글을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> deletePost(post))
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }


    private void setupLikeButton() {
        buttonLike.setOnClickListener(v -> {
            if (currentPost != null && currentUid != null) {
                toggleLikes(currentPost.getPostId(), currentUid);
            } else {
                Toast.makeText(DetailActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLikes(String postId, String uid) {
        apiService.toggleLikes(postId, uid, new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    runOnUiThread(() -> {
                        int likesCount = Integer.parseInt(response.body().get("likesCount"));
                        textLikeCount.setText(String.valueOf(likesCount));
                        boolean newIsLiked = !currentPost.isLikedByCurrentUser();
                        currentPost.setLikedByCurrentUser(newIsLiked);
                        updateLikeButtonUI(newIsLiked);
                        Toast.makeText(DetailActivity.this, "좋아요 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(DetailActivity.this, "좋아요 상태 변경 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLikeButtonUI(boolean isLiked) {
        if (isLiked) {
            buttonLike.setImageResource(R.drawable.icon_like_red);
        } else {
            buttonLike.setImageResource(R.drawable.icon_like);
        }
    }

    private void setupWindowInsets() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(true);

        NestedScrollView scrollView = findViewById(R.id.scroll_view_content);

        LinearLayout layout_comment_input = findViewById(R.id.layout_comment_input);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int bottomInset = Math.max(imeInset, navBarInset);

            scrollView.setPadding(
                    scrollView.getPaddingLeft(),
                    topInset,
                    scrollView.getPaddingRight(),
                    scrollView.getPaddingBottom()
            );

            if (layout_comment_input.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout_comment_input.getLayoutParams();
                params.bottomMargin = bottomInset;
                layout_comment_input.setLayoutParams(params);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupCommentUploadButton() {
        edit_upload.setOnClickListener(v -> {
            // 댓글 수정 또는 작성 로직 분기
            if (editingCommentId != null) {
                updateComment(edit_comment.getText().toString());
            } else {
                createComment();
            }
        });
    }

    private void createComment() {
        String commentContent = edit_comment.getText().toString();
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "댓글 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newComment = new Comment();
        newComment.setPostId(postId);
        newComment.setUid(user.getUid());
        newComment.setContent(commentContent);
        newComment.setNickname(currentUserNickname);
        newComment.setParentId(replyToCommentId);

        apiService.createComment(newComment, new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    resetCommentInput();
                } else {
                    Toast.makeText(DetailActivity.this, "댓글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "댓글 등록 실패: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "댓글 등록 네트워크 오류: ", t);
            }
        });
    }


    private void updateComment(String commentText) {
        apiService.updateComment(editingCommentId, currentUid, commentText, new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "댓글 수정 성공", Toast.LENGTH_SHORT).show();
                    resetCommentInput();
                    // fetchComments(); // Firestore 리스너가 자동 업데이트하므로 이 호출은 필요 없음
                } else {
                    Log.e(TAG, "댓글 수정 실패: " + response.code() + " " + response.message());
                    Toast.makeText(DetailActivity.this, "댓글 수정 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "댓글 수정 네트워크 오류: ", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetCommentInput() {
        edit_comment.setText("");
        edit_comment.setHint("댓글을 입력하세요...");
        editingCommentId = null;
        replyToCommentId = null;
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit_comment.getWindowToken(), 0);
    }

    private void deletePost(Post post) {
        apiService.deletePost(post.getPostId(), currentUid, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMessage = "삭제 실패";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        errorMessage += ": 서버에서 오류 메시지를 받지 못했습니다.";
                    }
                    Log.e("DetailActivity", "Delete failed: " + errorMessage);
                    Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPostDetails() {
        apiService.getPostById(postId, new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPost = response.body();
                    Post post = currentPost;

                    textAuthorNickname.setText(post.getNickname() != null ? post.getNickname() : "작성자 정보 없음");
                    textPostTitle.setText(post.getTitle() != null ? post.getTitle() : "제목 없음");
                    textPostDate.setText(formatDate(post.getCreatedAt()));
                    textCommentCount.setText(String.valueOf(post.getCommentsCount()));
                    textLikeCount.setText(String.valueOf(post.getLikesCount()));
                    updateLikeButtonUI(post.isLikedByCurrentUser());

                    ImageButton moreOptionsButton = findViewById(R.id.button_more_options);
                    if (currentUid != null && currentUid.equals(post.getUid())) {
                        moreOptionsButton.setVisibility(View.VISIBLE);
                    } else {
                        moreOptionsButton.setVisibility(View.GONE);
                    }

                    // ProfileLoader를 사용하여 작성자 프로필 로드
                    ImageView authorProfileImage = findViewById(R.id.text_profile_initial);
                    TextView authorNicknameText = findViewById(R.id.text_author_nickname);

                    ProfileLoader.loadProfileByUid(DetailActivity.this,
                            authorNicknameText,
                            authorProfileImage,
                            post.getUid());

                    displayPostContents(post.getContents());

                } else {
                    int errorCode = response.code();
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("DetailActivity", "게시글 로딩 실패: HTTP " + errorCode + " - " + errorBody);
                        Toast.makeText(DetailActivity.this, "게시글을 불러오는 데 실패했습니다: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("DetailActivity", "Error parsing error body", e);
                        Toast.makeText(DetailActivity.this, "게시글을 불러오는 데 실패했습니다. 오류 본문 파싱 오류.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e("DetailActivity", "네트워크 오류 발생", t);
                Toast.makeText(DetailActivity.this, "네트워크 오류가 발생했습니다: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void displayPostContents(List<ContentBlock> contents) {
        contentDisplayContainer.removeAllViews();
        Collections.sort(contents, Comparator.comparingInt(ContentBlock::getOrder));

        for (ContentBlock block : contents) {
            if ("text".equals(block.getType())) {
                TextView textView = new TextView(this);
                textView.setText(block.getText());
                textView.setTextSize(16);
                textView.setPadding(0, 8, 0, 8);
                textView.setTextColor(getResources().getColor(android.R.color.black));
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
}
