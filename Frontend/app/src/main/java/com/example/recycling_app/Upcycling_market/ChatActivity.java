package com.example.recycling_app.Upcycling_market;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.example.recycling_app.adapter.ChatAdapter;
import com.example.recycling_app.dto.market.ChatMessageDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ImageButton btnBack;

    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //setupSystemUI();

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        Intent intent = getIntent();
        if (intent != null) {
            chatRoomId = intent.getStringExtra("chatRoomId");
            otherUserId = intent.getStringExtra("otherUserId");

            if (chatRoomId == null) {
                Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initUI();

            loadOtherUserInfo(otherUserId); // 상대방 정보(닉네임) 로드
            loadMessages();                 // 메시지 로드
            markMessagesAsRead();           // 읽음 처리
        }
    }

    private void initUI() {
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        btnSend = findViewById(R.id.btnSend);
        tvOtherNickname = findViewById(R.id.chatSellerName);
        btnBack = findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(currentUserId); // 어댑터에 현재 사용자 ID 전달
        recyclerView.setAdapter(chatAdapter);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    // 상대방 정보 로드만 수행po
    private void loadOtherUserInfo(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nickname = snapshot.getString("nickname");
                tvOtherNickname.setText(nickname);
            }
        });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages");

        // 기존 리스너가 있다면 제거
        if (messageListener != null) messageListener.remove();

        messageListener = messagesRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "메시지 로드 실패", error);
                        return;
                    }
                    if (value != null) {
                        List<ChatMessageDTO> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessageDTO message = doc.toObject(ChatMessageDTO.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        // 어댑터에 새 리스트를 전달하면 DiffUtil이 알아서 변경 사항을 처리
                        chatAdapter.submitList(messages);

                        if (!messages.isEmpty()) {
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // otherUserId가 null이 아닌지 한번 더 확인 (안정성)
        if (otherUserId == null) {
            Toast.makeText(this, "상대방 정보가 없어 메시지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference messagesRef = db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages");

        // DTO 사용
        ChatMessageDTO message = new ChatMessageDTO(currentUserId, text);

        messagesRef.add(message).addOnSuccessListener(documentReference -> {
            updateChatRoomAfterSendMessage(text);
            messageInput.setText("");
        }).addOnFailureListener(e ->
                Toast.makeText(this, "메시지 전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // 안정성을 높인 updateChatRoomAfterSendMessage 메소드
    private void updateChatRoomAfterSendMessage(String message) {
        DocumentReference chatRoomRef = db.collection("chatRooms").document(chatRoomId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot chatRoomDoc = transaction.get(chatRoomRef);

            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMessage", message);
            updates.put("lastSenderUid", currentUserId);
            updates.put("updatedAt", FieldValue.serverTimestamp());

            // --- [개선] unread 맵을 더 안전하게 처리 ---
            Map<String, Object> unreadMap = new HashMap<>();
            // 1. unread 필드가 존재하고, 타입이 Map인지 먼저 확인
            if (chatRoomDoc.contains("unread") && chatRoomDoc.get("unread") instanceof Map) {
                // 안전하게 기존 맵을 가져옴
                unreadMap.putAll((Map<String, Object>) chatRoomDoc.get("unread"));
            }

            // 2. 상대방의 unread 카운트를 안전하게 가져오고 증가시킴
            Object unreadValue = unreadMap.get(otherUserId);
            long unreadCount = 0;
            // 값이 숫자인 경우에만 Long 값으로 변환
            if (unreadValue instanceof Number) {
                unreadCount = ((Number) unreadValue).longValue();
            }
            unreadMap.put(otherUserId, unreadCount + 1);

            // 3. 내 unread 카운트는 0으로 초기화
            unreadMap.put(currentUserId, 0L);
            // --- 개선된 로직 끝 ---

            updates.put("unread", unreadMap);
            transaction.update(chatRoomRef, updates);
            return null;
        }).addOnFailureListener(e ->
                Log.e(TAG, "채팅방 업데이트 실패. Firestore 데이터 타입을 확인하세요.", e));
    }

    private void markMessagesAsRead() {
        DocumentReference chatRoomRef = db.collection("chatRooms").document(chatRoomId);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot chatRoomDoc = transaction.get(chatRoomRef);
            if (chatRoomDoc.contains("unread." + currentUserId)) {
                transaction.update(chatRoomRef, "unread." + currentUserId, 0);
            }
            return null;
        }).addOnFailureListener(e ->
                Log.e(TAG, "읽음 상태 업데이트 실패", e));
    }

    /*
    private void setupSystemUI() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_screen_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

     */
}