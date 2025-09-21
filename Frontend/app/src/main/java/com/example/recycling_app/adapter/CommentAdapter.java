package com.example.recycling_app.adapter;

// CommentAdapter.java
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.data.Comment;
import com.example.recycling_app.util.ProfileLoader;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.CommentViewHolder> {

    // 댓글 수정 및 삭제 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onEditClick(Comment comment);
        void onDeleteClick(Comment comment);
        void onReplyClick(Comment comment);
        void onProfileClick(Comment comment);
    }

    private OnItemClickListener listener;
    private String currentUid;

    public CommentAdapter(String currentUid) {
        super(DIFF_CALLBACK);
        this.currentUid = currentUid;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bind(comment);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProfile;
        private final TextView textNickname, textCommentBody;
        private final ImageButton buttonMoreOptions;
        private final TextView buttonReply;
        private final View replyIndent;
        private final View profileIndent;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            // findViewById로 뷰 초기화
            imageProfile = itemView.findViewById(R.id.image_comment_profile);
            textNickname = itemView.findViewById(R.id.text_comment_nickname);
            textCommentBody = itemView.findViewById(R.id.text_comment_body);
            buttonMoreOptions = itemView.findViewById(R.id.comment_more_vert);
            buttonReply = itemView.findViewById(R.id.button_reply);
            replyIndent = itemView.findViewById(R.id.reply_to_comment_container);
            profileIndent = itemView.findViewById(R.id.image_comment_profile);

            buttonMoreOptions.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, getItem(position));
                }
            });

            buttonReply.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onReplyClick(getItem(position));
                }
            });

            imageProfile.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProfileClick(getItem(position));
                }
            });

            textNickname.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProfileClick(getItem(position));
                }
            });
        }

        public void bind(Comment comment) {
            ProfileLoader.loadProfileByUid(itemView.getContext(), textNickname, imageProfile, comment.getUid());

            if (textCommentBody != null) {
                textCommentBody.setText(comment.getContent());
            }

            // 대댓글 여부에 따라 들여쓰기 적용
            if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
                if (profileIndent != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) profileIndent.getLayoutParams();
                    layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, itemView.getResources().getDisplayMetrics());
                    profileIndent.setLayoutParams(layoutParams);
                }
            } else {
                if (profileIndent != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) profileIndent.getLayoutParams();
                    layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, itemView.getResources().getDisplayMetrics());
                    profileIndent.setLayoutParams(layoutParams);
                }
            }

            // 수정/삭제 버튼 가시성 제어
            if (buttonMoreOptions != null) {
                if (currentUid != null && currentUid.equals(comment.getUid())) {
                    buttonMoreOptions.setVisibility(View.VISIBLE);
                } else {
                    buttonMoreOptions.setVisibility(View.INVISIBLE);
                }
            }

            // 답장하기 버튼 가시성 제어
            if (buttonReply != null) {
                if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
                    buttonReply.setVisibility(View.GONE);
                } else {
                    buttonReply.setVisibility(View.VISIBLE);
                }
            }
        }

        private void showPopupMenu(View view, Comment comment) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.comment_options_menu); // 새로운 메뉴 리소스 사용
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit_comment) {
                    if (listener != null) {
                        listener.onEditClick(comment);
                    }
                    return true;
                } else if (itemId == R.id.action_delete_comment) {
                    if (listener != null) {
                        listener.onDeleteClick(comment);
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getCommentId().equals(newItem.getCommentId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.equals(newItem);
        }
    };

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        return formatter.format(date);
    }
}
