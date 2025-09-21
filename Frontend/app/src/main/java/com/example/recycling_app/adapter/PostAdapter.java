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
        private final TextView postTitle, postNickname, postBody, postLikesCount, postCommentsCount;
        private final ImageView postProfileImage, postContentImage;
        private final ImageButton postMoreVert;
        private final ImageView postLikeIcon, postCommentIcon;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            // UI 요소 초기화
            postTitle = itemView.findViewById(R.id.text_post_title);
            postNickname = itemView.findViewById(R.id.text_author_nickname);
            postBody = itemView.findViewById(R.id.text_post_body);
            postLikesCount = itemView.findViewById(R.id.text_like_count);
            postCommentsCount = itemView.findViewById(R.id.text_comment_count);
            postProfileImage = itemView.findViewById(R.id.text_profile_initial);
            postContentImage = itemView.findViewById(R.id.image_post_content);
            postMoreVert = itemView.findViewById(R.id.button_more_options);
            postLikeIcon = itemView.findViewById(R.id.icon_like);
            postCommentIcon = itemView.findViewById(R.id.icon_chat);

            // 프로필 클릭 리스너
            postProfileImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProfileClick(getItem(position));
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });

            postMoreVert.setOnClickListener(showPopupMenu);
            postProfileImage.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onProfileClick(getItem(position));
                    }
                }
            });
        }

        public void bind(Post post) {
            postTitle.setText(post.getTitle());
            postNickname.setText(post.getNickname());
            postLikesCount.setText(String.valueOf(post.getLikesCount()));
            postCommentsCount.setText(String.valueOf(post.getCommentsCount()));

            // 프로필 정보 로딩
            ProfileLoader.loadProfileByUid(itemView.getContext(), null, postProfileImage, post.getUid());

            // --- 게시글 본문 및 이미지 표시 로직 수정 ---
            boolean textFound = false;
            boolean imageFound = false;

            if (post.getContents() != null) {
                for (ContentBlock contentBlock : post.getContents()) {
                    if ("text".equals(contentBlock.getType()) && !textFound) {
                        postBody.setText(contentBlock.getText());
                        postBody.setVisibility(View.VISIBLE);
                        textFound = true;
                    }
                    if ("image".equals(contentBlock.getType()) && !imageFound) {
                        String imageUrl = contentBlock.getMediaUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(itemView.getContext()).load(imageUrl).into(postContentImage);
                            postContentImage.setVisibility(View.VISIBLE);
                            imageFound = true;
                        }
                    }
                    // 첫 번째 텍스트와 첫 번째 이미지를 찾았으면 루프 종료
                    if (textFound && imageFound) {
                        break;
                    }
                }
            }

            // 텍스트나 이미지가 없는 경우 View를 숨김
            if (!textFound) {
                postBody.setVisibility(View.GONE);
            }
            if (!imageFound) {
                postContentImage.setVisibility(View.GONE);
            }

            // 수정 및 삭제 메뉴 버튼 표시 여부
            if (post.getUid() != null && post.getUid().equals(currentUid)) {
                postMoreVert.setVisibility(View.VISIBLE);
            } else {
                postMoreVert.setVisibility(View.GONE);
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
            // isLikedByCurrentUser 필드는 UI 상태이므로 비교에서 제외
            return oldItem.getNickname().equals(newItem.getNickname()) &&
                    oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getLikesCount() == newItem.getLikesCount() &&
                    oldItem.getCommentsCount() == newItem.getCommentsCount() &&
                    oldItem.getContents().equals(newItem.getContents());
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
