// ChatAdapter.java

package com.example.recycling_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.dto.market.ChatMessageDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// [개선] ListAdapter와 DiffUtil을 사용하여 효율적인 목록 관리
public class ChatAdapter extends ListAdapter<ChatMessageDTO, RecyclerView.ViewHolder> {

    private final String currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // [설명] 뷰 타입을 명확하게 상수로 정의
    private static final int VIEW_TYPE_MINE = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    // [개선] 생성자에서 현재 사용자 ID를 직접 전달받음 (의존성 주입)
    public ChatAdapter(String currentUserId) {
        super(DIFF_CALLBACK);
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        // [개선] 메시지 발신자와 현재 사용자 ID를 비교하여 뷰 타입 결정
        ChatMessageDTO message = getItem(position);
        if (message != null && message.getSenderUid().equals(currentUserId)) {
            return VIEW_TYPE_MINE;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // [개선] 뷰 타입에 따라 명확하게 다른 ViewHolder를 생성
        if (viewType == VIEW_TYPE_MINE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_mine, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageDTO message = getItem(position);
        // [개선] ViewHolder 타입에 따라 bind 메소드 호출
        if (holder.getItemViewType() == VIEW_TYPE_MINE) {
            ((MyMessageViewHolder) holder).bind(message);
        } else {
            ((OtherMessageViewHolder) holder).bind(message);
        }
    }

    // [개선] 내 메시지를 위한 ViewHolder
    private class MyMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTime;

        MyMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.textMessage);
            tvTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessageDTO message) {
            tvMessage.setText(message.getMessage());
            if (message.getCreatedAt() != null) {
                tvTime.setText(timeFormat.format(message.getCreatedAt()));
            }
        }
    }

    // [개선] 상대방 메시지를 위한 ViewHolder
    private class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTime;

        OtherMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.textMessage);
            tvTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessageDTO message) {
            tvMessage.setText(message.getMessage());
            if (message.getCreatedAt() != null) {
                tvTime.setText(timeFormat.format(message.getCreatedAt()));
            }
        }
    }

    // DiffUtil.ItemCallback 구현: 리스트 변경 사항을 효율적으로 계산
    private static final DiffUtil.ItemCallback<ChatMessageDTO> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatMessageDTO>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatMessageDTO oldItem, @NonNull ChatMessageDTO newItem) {
                    // DTO에서 생성 날짜 가져오기
                    Date oldDate = oldItem.getCreatedAt();
                    Date newDate = newItem.getCreatedAt();

                    // 수정된 로직:
                    // 1. 둘 다 null이면 동일한 것으로 간주합니다.
                    // 2. 하나만 null이면 동일하지 않습니다.
                    // 3. 둘 다 null이 아니면 equals()로 비교합니다.
                    if (oldDate == null && newDate == null) {
                        return true;
                    }
                    if (oldDate != null && newDate != null) {
                        // 이제 둘 다 null이 아님을 확인했으니 안전하게 equals()를 호출합니다.
                        // createdAt이 고유 식별자라고 가정합니다.
                        return oldDate.equals(newDate);
                    }
                    return false; // 하나만 null이므로 동일하지 않습니다.
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatMessageDTO oldItem, @NonNull ChatMessageDTO newItem) {
                    return oldItem.equals(newItem);
                }
            };
}