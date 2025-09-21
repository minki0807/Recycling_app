package com.example.recycling_app.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.example.recycling_app.dto.MessageDTO;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.Document;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {
    private static final String CHAT_ROOMS = "chatRooms";  // 채팅방 컬렉션 이름
    private static final String MESSAGES = "messages";     // 메시지 서브컬렉션 이름

    private Firestore db() {return FirestoreClient.getFirestore();} // Firestore DB 인스턴스 가져오기

    // chatRoomId 규칙: postId + uid 정렬
    public String buildChatRoomId(String postId, String uidA, String uidB) {
        String x = uidA.compareTo(uidB) <= 0 ? uidA + "_" + uidB : uidB + "_" + uidA; // UID 정렬해서 중복 방지
        return "chat_" + postId + "_" + x; // ex) chat_post123_uidA_uidB
    }

    // 채팅방 생성(없으면 새로 생성, 있으면 재사용)
    public String createChatRoom(String postId, String uidA, String uidB) throws Exception {
        String roomId = buildChatRoomId(postId, uidA, uidB);
        DocumentReference roomRef = db().collection(CHAT_ROOMS).document(roomId);

        db().runTransaction(tx-> {
            DocumentSnapshot snap = tx.get(roomRef).get();
            if(!snap.exists()) { // 방이 없으면 생성
                Map<String, Object> data = new HashMap<>();
                data.put("postId", postId);
                data.put("participants", Arrays.asList(uidA, uidB)); // 참여자 2명
                data.put("createdAt", Timestamp.now()); // 생성 시각
                data.put("updatedAt", Timestamp.now()); // 최근 갱신 시각
                data.put("lastMessage", "");            // 마지막 메시지
                data.put("lastMessageType", null);      // 마지막 메시지 타입
                data.put("lastSenderUid", null);        // 마지막 보낸 사람
                Map<String, Long> unread = new HashMap<>();
                unread.put(uidA, 0L); unread.put(uidB, 0L); // 안읽음 카운트 초기화
                data.put("unread", unread);
                tx.set(roomRef, data);
            }
            return null;
        }).get();

        return roomId;
    }

    // 텍스트 메시지 전송 + 채팅방 메타 갱신
    public void sendText(String roomId, String senderUid, String text) throws Exception {
        DocumentReference roomRef = db().collection(CHAT_ROOMS).document(roomId);
        CollectionReference msgs = roomRef.collection(MESSAGES);
        DocumentReference msgRef = msgs.document();

        DocumentSnapshot room = roomRef.get().get();
        List<String> participants = (List<String>) room.get("participants");
        String other = participants.get(0).equals(senderUid) ? participants.get(1) : participants.get(0); // 상대방 UID

        WriteBatch batch = db().batch();

        MessageDTO m = new MessageDTO();
        m.setSenderUid(senderUid);
        m.setType("text");           // 메시지 타입 = 텍스트
        m.setText(text);             // 실제 내용
        m.setCreatedAt(Timestamp.now());
        batch.set(msgRef, m);        // 메시지 저장

        Map<String, Object> roomUpdate = new HashMap<>();
        roomUpdate.put("lastMessage", text);         // 방의 마지막 메시지 갱신
        roomUpdate.put("lastMessageType", "text");
        roomUpdate.put("lastSenderUid", senderUid);
        roomUpdate.put("updatedAt", Timestamp.now());
        roomUpdate.put("unread." + other, FieldValue.increment(1)); // 상대방 unread +1
        batch.update(roomRef, roomUpdate);

        batch.commit().get(); // 배치 실행
    }

    // 파일/이미지 메시지 전송
    public void sendFile(String roomId, String senderUid,
                         MultipartFile file, boolean isImage) throws Exception {

        String folder = "chat/" + roomId; // Storage 폴더 경로
        String objectName = folder + "/" + UUID.randomUUID() + "_" + Objects.requireNonNull(file.getOriginalFilename()); // 파일명 랜덤화
        String token = UUID.randomUUID().toString(); // 다운로드 토큰

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        BlobInfo blobInfo = BlobInfo.newBuilder(StorageClient.getInstance().bucket().getName(), objectName)
                .setContentType(file.getContentType())
                .setMetadata(Map.of("firebaseStorageDownloadTokens", token)) // 토큰 메타 추가
                .build();
        storage.create(blobInfo, file.getBytes()); // Storage 업로드

        String mediaUrl = "https://firebasestorage.googleapis.com/v0/b/"
                + StorageClient.getInstance().bucket().getName()
                + "/o/" + URLEncoder.encode(objectName, StandardCharsets.UTF_8)
                + "?alt=media&token=" + token; // 다운로드 URL

        DocumentReference roomRef = db().collection(CHAT_ROOMS).document(roomId);
        CollectionReference msgs = roomRef.collection(MESSAGES);
        DocumentReference msgRef = msgs.document();

        DocumentSnapshot room = roomRef.get().get();
        List<String> participants = (List<String>) room.get("participants");
        String other = participants.get(0).equals(senderUid) ? participants.get(1) : participants.get(0);

        WriteBatch batch = db().batch();

        MessageDTO m = new MessageDTO();
        m.setSenderUid(senderUid);
        m.setType(isImage ? "image" : "file"); // 이미지인지 파일인지 구분
        m.setMediaUrl(mediaUrl);
        m.setStoragePath(objectName);
        m.setFileName(file.getOriginalFilename());
        m.setFileSize(file.getSize());
        m.setMimeType(file.getContentType());
        m.setCreatedAt(Timestamp.now());
        batch.set(msgRef, m);

        Map<String, Object> roomUpdate = new HashMap<>();
        roomUpdate.put("lastMessage", isImage ? "사진을 보냈습니다." : "파일을 보냈습니다."); // UI 표시용 문구
        roomUpdate.put("lastMessageType", isImage ? "image" : "file");
        roomUpdate.put("lastSenderUid", senderUid);
        roomUpdate.put("updatedAt", Timestamp.now());
        roomUpdate.put("unread." + other, FieldValue.increment(1));
        batch.update(roomRef, roomUpdate);

        batch.commit().get();
    }

    // 내가 참여한 채팅방 목록 조회 (최신순, 게시물 제목 포함)
    public List<Map<String, Object>> listMyRoomsWithTitle(String myUid, int limit) throws Exception {
        // 1. 내가 참여한 채팅방 목록 조회
        Query q = db().collection(CHAT_ROOMS)
                .whereArrayContains("participants", myUid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit);
        List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();

        List<Map<String, Object>> out = new ArrayList<>();
        Set<String> postIds = new HashSet<>();

        // 2. 각 채팅방의 postId 수집
        for (QueryDocumentSnapshot d : docs) {
            Map<String, Object> roomData = d.getData();
            out.add(roomData);
            postIds.add((String) roomData.get("postId"));
        }

        // 3. 게시물 제목 가져오기 (Batch Get)
        Map<String, String> postIdToTitle = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<DocumentReference> postRefs = new ArrayList<>();
            for (String postId : postIds) {
                postRefs.add(db().collection("products").document(postId));
            }
            List<ApiFuture<DocumentSnapshot>> futures = new ArrayList<>();
            for (DocumentReference ref : postRefs) futures.add(ref.get());

            for (ApiFuture<DocumentSnapshot> f : futures) {
                DocumentSnapshot postSnap = f.get();
                if (postSnap.exists()) {
                    postIdToTitle.put(postSnap.getId(), (String) postSnap.get("title"));
                }
            }
        }
        return out;
    }


    // 특정 채팅방 메시지 목록 조회 (페이징 지원)
    public List<MessageDTO> listMessages(String roomId, int limit, @Nullable Timestamp startAfter) throws Exception {
        CollectionReference msgs = db().collection(CHAT_ROOMS).document(roomId).collection(MESSAGES);
        Query q = msgs.orderBy("createdAt", Query.Direction.ASCENDING).limit(limit); // 오래된 순
        if (startAfter != null) q = q.startAfter(startAfter); // 페이징 처리
        List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
        List<MessageDTO> out = new ArrayList<>();
        for (QueryDocumentSnapshot d : docs) out.add(d.toObject(MessageDTO.class)); // DTO 변환
        return out;
    }

    // 읽음 처리 (내 unread를 0으로 초기화)
    public void markRead(String roomId, String myUid) throws Exception {
        DocumentReference roomRef = db().collection(CHAT_ROOMS).document(roomId);
        roomRef.update("unread." + myUid, 0).get();
    }
}
