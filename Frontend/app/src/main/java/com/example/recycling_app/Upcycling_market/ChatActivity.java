package com.example.recycling_app.Upcycling_market;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.adapter.ChatAdapter;
import com.example.recycling_app.dto.market.ChatMessageDTO;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton btnSend;

    private FirebaseFirestore db;
    private String chatRoomId;
    private String currentUserId;
    private String otherUserId;

    private TextView tvOtherNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);
        tvOtherNickname = findViewById(R.id.chatSellerName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter();
        recyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        chatRoomId = getIntent().getStringExtra("chatRoomId");

        if (chatRoomId == null) return;

        // 채팅방 정보 가져오기
        loadChatRoomInfo();

        // 메시지 불러오기
        loadMessages();

        // 전송 버튼
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadChatRoomInfo() {
        DocumentReference chatRoomRef = db.collection("chatRooms").document(chatRoomId);
        chatRoomRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 참여자 가져오기
                java.util.List<String> participants = (java.util.List<String>) documentSnapshot.get("participants");
                if (participants != null) {
                    for (String uid : participants) {
                        if (!uid.equals(currentUserId)) {
                            otherUserId = uid;
                            loadOtherUserInfo(uid);
                        }
                    }
                }
            }
        });
    }

    private void loadOtherUserInfo(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nickname = snapshot.getString("nickname");
                tvOtherNickname.setText(nickname); // 상단에 상대방 닉네임 표시
            }
        });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chatRooms").document(chatRoomId).collection("messages");
        messagesRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;

                        chatAdapter.clearMessages();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessageDTO message = doc.toObject(ChatMessageDTO.class);
                            if (message != null) {
                                chatAdapter.addMessage(message);
                            }
                        }
                        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        DocumentReference messagesRef = db.collection("chatRooms").document(chatRoomId)
                .collection("messages").document();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", currentUserId);
        messageData.put("text", text);
        messageData.put("createdAt", Timestamp.now());

        messagesRef.set(messageData).addOnSuccessListener(aVoid -> {
            // 채팅방 최신 메시지 업데이트
            db.collection("chatRooms").document(chatRoomId).update("lastMessage", text,
                    "lastSenderUid", currentUserId,
                    "lastMessageType", "text",
                    "updatedAt", Timestamp.now());
            messageInput.setText("");
        });
    }
}
