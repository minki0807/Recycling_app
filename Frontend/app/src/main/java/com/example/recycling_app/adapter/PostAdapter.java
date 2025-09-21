package com.example.recycling_app.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.data.ContentBlock;
import com.example.recycling_app.data.Post;
import com.example.recycling_app.util.ProfileLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends ListAdapter<Post, PostAdapter.PostViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Post post);
        void onEditClick(Post post);
        void onDeleteClick(Post post);
        void onProfileClick(Post post);
    }

    private OnItemClickListener listener;
    private String currentUid;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(String currentUid) {
        super(DIFF_CALLBACK);
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = getItem(position);
        holder.bind(post);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProfile, imagePostContent;
        private TextView textNickname, textCreatedAt, textTitle, textPostBody, textLikeCount, textCommentCount;
        private ImageButton btnMoreOptions, btnLike, btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // UI 요소 초기화
            imageProfile = itemView.findViewById(R.id.text_profile_initial);
            textNickname = itemView.findViewById(R.id.text_author_nickname);
            textCreatedAt = itemView.findViewById(R.id.text_post_date);
            textTitle = itemView.findViewById(R.id.text_post_title);
            textPostBody = itemView.findViewById(R.id.text_post_body);
            textLikeCount = itemView.findViewById(R.id.text_like_count);
            imagePostContent = itemView.findViewById(R.id.image_post_content);
            textCommentCount = itemView.findViewById(R.id.text_comment_count);
            btnMoreOptions = itemView.findViewById(R.id.button_more_options);
            btnLike = itemView.findViewById(R.id.button_like);
            btnComment = itemView.findViewById(R.id.button_comment);

            // 프로필 클릭 리스너
            imageProfile.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProfileClick(getItem(position));
                }
            });

            // 게시글 클릭 리스너
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Post post = getItem(position);
                    // postId가 null이 아닌 경우에만 클릭 이벤트 처리
                    if (post != null && post.getPostId() != null) {
                        listener.onItemClick(post);
                    } else {
                        Toast.makeText(itemView.getContext(), "게시글 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // 더보기 버튼 클릭 리스너 (내 게시글만 보이도록)
            if (btnMoreOptions != null) {
                btnMoreOptions.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PostAdapter.this.showPopupMenu(v, getItem(position));
                    }
                });
            }
        }

        public void bind(Post post) {
            ProfileLoader.loadProfileByUid(itemView.getContext(), textNickname, imageProfile, post.getUid());

            textCreatedAt.setText(formatDate(post.getCreatedAt()));
            textTitle.setText(post.getTitle());
            textLikeCount.setText(String.valueOf(post.getLikesCount()));
            textCommentCount.setText(String.valueOf(post.getCommentsCount()));

            String fullText = "";
            String firstImageUrl = null;

            if (post.getContents() != null && !post.getContents().isEmpty()) {
                for (ContentBlock content : post.getContents()) {
                    if ("text".equals(content.getType())) {
                        fullText += content.getText();
                    }
                    if ("image".equals(content.getType()) && firstImageUrl == null) {
                        firstImageUrl = content.getMediaUrl();
                    }
                }
            }

            if (fullText.length() > 30) {
                textPostBody.setText(fullText.substring(0, 30) + "...");
            } else {
                textPostBody.setText(fullText);
            }
            if (firstImageUrl != null) {
                imagePostContent.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(firstImageUrl)
                        .into(imagePostContent);
            } else {
                imagePostContent.setVisibility(View.GONE);
            }

            // 좋아요 버튼 상태 업데이트 (Null-check 추가)
            if (btnLike != null) {
                if (post.isLikedByCurrentUser()) {
                    btnLike.setImageResource(R.drawable.icon_like_red);
                } else {
                    btnLike.setImageResource(R.drawable.icon_like);
                }
            }


            // 더보기 버튼 가시성 (내 게시글일 경우에만 표시)
            if (btnMoreOptions != null) {
                if (currentUid != null && currentUid.equals(post.getUid())) {
                    btnMoreOptions.setVisibility(View.VISIBLE);
                } else {
                    btnMoreOptions.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void showPopupMenu(View view, Post post) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.post_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                if (listener != null) {
                    listener.onEditClick(post);
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                if (listener != null) {
                    listener.onDeleteClick(post);
                }
                return true;
            }
            return false;
        });
        popup.show();
    }
    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.getPostId().equals(newItem.getPostId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.equals(newItem);
        }
    };

    private static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        return formatter.format(date);
    }
}
