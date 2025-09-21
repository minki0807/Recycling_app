package com.example.recycling_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.market.ChatRoomUIModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

// ListAdapter와 DiffUtil을 사용하여 효율적인 목록 관리
public class ChatListAdapter extends ListAdapter<ChatRoomUIModel, ChatListAdapter.ChatRoomViewHolder> {

    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    // 아이템 클릭 이벤트를 처리하기 위한 인터페이스
    public interface OnItemClickListener {
        void onItemClick(ChatRoomUIModel chatRoom);
    }

    public ChatListAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatRoomViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomUIModel chatRoom = getItem(position);
        holder.bind(chatRoom);
    }

    // --- ViewHolder ---
    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivChatProfile;
        private final TextView tvChatNickname, tvLastMessage, tvChatTime;
        private final View viewUnreadDot;
        private final Context context;

        ChatRoomViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.context = itemView.getContext();
            ivChatProfile = itemView.findViewById(R.id.ivChatProfile);
            tvChatNickname = itemView.findViewById(R.id.tvChatNickname);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvChatTime = itemView.findViewById(R.id.tvChatTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        // bind 메소드는 오직 데이터를 View에 설정하는 역할만 수행
        void bind(ChatRoomUIModel chatRoom) {
            tvChatNickname.setText(chatRoom.getOtherUserNickname());
            tvLastMessage.setText(chatRoom.getLastMessage() != null ? chatRoom.getLastMessage() : "메시지 없음");

            if (chatRoom.getUpdatedAt() != null) {
                tvChatTime.setText(dateFormat.format(chatRoom.getUpdatedAt()));
            } else {
                tvChatTime.setText("");
            }

            Glide.with(context)
                    .load(chatRoom.getOtherUserProfileUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.basic_profile_logo)
                    .error(R.drawable.basic_profile_logo)
                    .into(ivChatProfile);

            viewUnreadDot.setVisibility(chatRoom.getUnreadCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    // --- DiffUtil Callback ---
    private static final DiffUtil.ItemCallback<ChatRoomUIModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatRoomUIModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatRoomUIModel oldItem, @NonNull ChatRoomUIModel newItem) {
                    return oldItem.getChatRoomId().equals(newItem.getChatRoomId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatRoomUIModel oldItem, @NonNull ChatRoomUIModel newItem) {
                    return oldItem.equals(newItem);
                }
            };
}