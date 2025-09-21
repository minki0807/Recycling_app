package com.example.recycling_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.dto.market.ChatMessageDTO;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessageDTO> messages = new ArrayList<>();
    private String currentUserId = FirebaseAuth.getInstance().getUid();

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == 0) ? R.layout.item_chat_mine : R.layout.item_chat_other;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessageDTO message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderUid().equals(currentUserId)) {
            return 0; // 내 메시지
        } else {
            return 1; // 상대 메시지
        }
    }

    public void addMessage(ChatMessageDTO message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        ChatViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.textMessage);
            tvTime = itemView.findViewById(R.id.textTime);
        }

        void bind(ChatMessageDTO message) {
            tvMessage.setText(message.getMessage());
            if (message.getCreatedAt() != null) {
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(message.getCreatedAt());
                tvTime.setText(time);
            }
        }
    }
}