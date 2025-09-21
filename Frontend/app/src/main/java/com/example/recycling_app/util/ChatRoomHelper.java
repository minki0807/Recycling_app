package com.example.recycling_app.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.recycling_app.Upcycling_market.ChatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 채팅방 생성 및 관리를 위한 헬퍼 클래스
 */
public class ChatRoomHelper {

    private FirebaseFirestore db;
    private Context context;

    public ChatRoomHelper(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * 기존 채팅방을 확인하고, 없으면 새로 생성합니다.
     * @param buyerUid 구매자 UID
     * @param sellerUid 판매자 UID
     * @param productId 제품 ID
     */
    public void createChatRoom(String buyerUid, String sellerUid, String productId) {
        // 채팅방 ID 생성 (일관성을 위해 사용자 UID를 정렬하여 생성하는 것을 고려할 수 있습니다)
        String chatRoomId = buyerUid + "_" + sellerUid + "_" + productId;

        // 이미 채팅방이 존재하는지 확인
        db.collection("chatRooms").document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 기존 채팅방이 있으면 바로 입장
                        openChatRoom(chatRoomId);
                    } else {
                        // 새 채팅방 생성
                        createNewChatRoom(chatRoomId, buyerUid, sellerUid, productId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "채팅방 확인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 새로운 채팅방 데이터를 Firestore에 생성합니다.
     * @param chatRoomId 생성할 채팅방 ID
     * @param buyerUid 구매자 UID
     * @param sellerUid 판매자 UID
     * @param productId 제품 ID
     */
    private void createNewChatRoom(String chatRoomId, String buyerUid, String sellerUid, String productId) {
        // unread 맵 초기화 (모든 참여자의 읽지 않은 메시지 수를 0으로 설정)
        Map<String, Object> unreadMap = new HashMap<>();
        unreadMap.put(buyerUid, 0);
        unreadMap.put(sellerUid, 0);

        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("participants", Arrays.asList(buyerUid, sellerUid));
        chatRoomData.put("productId", productId);
        chatRoomData.put("createdAt", new Date());
        chatRoomData.put("updatedAt", new Date());
        chatRoomData.put("lastMessage", "");
        chatRoomData.put("lastSenderUid", "");
        chatRoomData.put("lastMessageType", "text");
        chatRoomData.put("unread", unreadMap); // unread 구조 추가

        db.collection("chatRooms").document(chatRoomId)
                .set(chatRoomData)
                .addOnSuccessListener(aVoid -> {
                    // 채팅방 생성 성공 후 입장
                    openChatRoom(chatRoomId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "채팅방 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    /**
     * 채팅방에 입장합니다.
     * @param chatRoomId 채팅방 ID
     */
    private void openChatRoom(String chatRoomId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        context.startActivity(intent);
    }

    /**
     * 특정 제품에 대한 채팅방이 이미 존재하는지 확인합니다.
     * @param buyerUid 구매자 UID
     * @param sellerUid 판매자 UID
     * @param productId 제품 ID
     * @param callback 결과를 받을 콜백
     */
    public void checkChatRoomExists(String buyerUid, String sellerUid, String productId,
                                    ChatRoomExistsCallback callback) {
        String chatRoomId = buyerUid + "_" + sellerUid + "_" + productId;

        db.collection("chatRooms").document(chatRoomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onResult(documentSnapshot.exists(), chatRoomId);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    /**
     * 채팅방 존재 여부 확인 콜백 인터페이스
     */
    public interface ChatRoomExistsCallback {
        void onResult(boolean exists, String chatRoomId);
        void onError(String error);
    }
}