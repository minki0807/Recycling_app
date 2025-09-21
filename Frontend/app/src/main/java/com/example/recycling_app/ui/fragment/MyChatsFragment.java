package com.example.recycling_app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recycling_app.R;
import com.example.recycling_app.Upcycling_market.ChatActivity;
import com.example.recycling_app.adapter.ChatListAdapter;
import com.example.recycling_app.dto.market.ChatRoomDTO;
import com.example.recycling_app.dto.market.ChatRoomUIModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyChatsFragment extends Fragment {

    private static final String TAG = "MyChatsFragment";

    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private TextView tvEmptyMessage;
    private FirebaseFirestore db;
    private String currentUserId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<ChatRoomUIModel> allChatRoomsUIModel; // 검색을 위한 전체 UI 모델 목록
    private ListenerRegistration chatRoomListener; // Firestore 리스너 관리를 위한 변수

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_chats, container, false);
        initViews(view);
        setupRecyclerView();
        setupSwipeRefreshLayout();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 화면이 보일 때 실시간 리스너를 등록하여 데이터 로드 시작
        loadMyChatRooms();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 화면이 사라질 때 리스너를 제거하여 리소스 낭비 방지
        if (chatRoomListener != null) {
            chatRoomListener.remove();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMyChats);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutChats);
        allChatRoomsUIModel = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    private void setupRecyclerView() {
        // 어댑터 생성 시 클릭 리스너를 구현하여 전달
        chatListAdapter = new ChatListAdapter(chatRoom -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
            intent.putExtra("otherUserId", chatRoom.getOtherUserId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatListAdapter);
    }

    private void setupSwipeRefreshLayout() {
        // 새로고침 시에도 동일한 데이터 로드 메소드 호출
        swipeRefreshLayout.setOnRefreshListener(this::loadMyChatRooms);
    }

    private void loadMyChatRooms() {
        if (currentUserId == null) {
            showEmptyMessage("로그인이 필요합니다.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        // addSnapshotListener를 사용하여 실시간으로 채팅방 목록 업데이트
        Query query = db.collection("chatRooms")
                .whereArrayContains("participants", currentUserId)
                .orderBy("updatedAt", Query.Direction.DESCENDING);

        chatRoomListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) {
                Log.e(TAG, "Error loading chat rooms", e);
                showEmptyMessage("채팅 목록을 불러올 수 없습니다.");
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            // 1. Firestore에서 가져온 ChatRoomDTO 목록
            List<ChatRoomDTO> roomDTOs = snapshots.toObjects(ChatRoomDTO.class);

            if (roomDTOs.isEmpty()) {
                showEmptyMessage("채팅 내역이 없습니다.");
                allChatRoomsUIModel.clear(); // 목록 비우기
                chatListAdapter.submitList(new ArrayList<>()); // 어댑터에도 빈 리스트 전달
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            // 2. 각 DTO를 UI 모델로 변환하는 비동기 작업 목록 생성
            List<Task<ChatRoomUIModel>> tasks = new ArrayList<>();
            for (ChatRoomDTO dto : roomDTOs) {
                tasks.add(createUIModelFromDTO(dto));
            }

            // 3. 모든 변환 작업이 완료될 때까지 기다림
            Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        List<ChatRoomUIModel> finalUiModels = new ArrayList<>();
                        for (Object result : results) {
                            finalUiModels.add((ChatRoomUIModel) result);
                        }

                        allChatRoomsUIModel.clear();
                        allChatRoomsUIModel.addAll(finalUiModels); // 검색을 위해 전체 목록 저장

                        showChatRooms(finalUiModels);
                        swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(err -> {
                        Log.e(TAG, "UI 모델 변환 중 하나 이상의 작업 실패", err);
                        showEmptyMessage("데이터를 처리하는 중 오류가 발생했습니다.");
                        swipeRefreshLayout.setRefreshing(false);
                    });
        });
    }

    // ChatRoomDTO를 ChatRoomUIModel로 변환하는 메소드
    private Task<ChatRoomUIModel> createUIModelFromDTO(ChatRoomDTO chatRoom) {
        String otherUserId = chatRoom.getParticipants().stream()
                .filter(uid -> !uid.equals(currentUserId))
                .findFirst().orElse(null);

        if (otherUserId == null) {
            // 상대방이 없는 경우 (오류 상황), 빈 모델을 반환하는 Task 생성
            return Tasks.forResult(new ChatRoomUIModel(chatRoom.getChatRoomId(), "", "알 수 없음", null, chatRoom.getLastMessage(), chatRoom.getUpdatedAt(), 0));
        }

        // 상대방 유저 정보를 Firestore에서 가져오는 Task
        return db.collection("users").document(otherUserId).get().continueWith(task -> {
            String nickname = "알 수 없음";
            String profileUrl = null;

            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot userDoc = task.getResult();
                nickname = userDoc.getString("nickname");
                profileUrl = userDoc.getString("profileImageUrl");
            }

            long unreadCount = chatRoom.getUnread() != null ? chatRoom.getUnread().getOrDefault(currentUserId, 0L) : 0L;

            // 최종적으로 화면에 표시될 UI 모델을 생성하여 반환
            return new ChatRoomUIModel(
                    chatRoom.getChatRoomId(), otherUserId, nickname, profileUrl,
                    chatRoom.getLastMessage(), chatRoom.getUpdatedAt(), unreadCount
            );
        });
    }

    private void showChatRooms(List<ChatRoomUIModel> chatRooms) {
        tvEmptyMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        chatListAdapter.submitList(chatRooms);
    }

    private void showEmptyMessage(String message) {
        recyclerView.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
    }

    // 검색 메소드 (ChatRoomUIModel 기준)
    public void search(String query) {
        if (allChatRoomsUIModel == null) return;

        if (query.isEmpty()) {
            showChatRooms(allChatRoomsUIModel);
            return;
        }

        List<ChatRoomUIModel> filteredList = allChatRoomsUIModel.stream()
                .filter(chatRoom -> {
                    // 상대방 닉네임 또는 마지막 메시지로 검색
                    boolean matchesNickname = chatRoom.getOtherUserNickname().toLowerCase().contains(query.toLowerCase());
                    boolean matchesMessage = chatRoom.getLastMessage() != null && chatRoom.getLastMessage().toLowerCase().contains(query.toLowerCase());
                    return matchesNickname || matchesMessage;
                })
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            showEmptyMessage("검색 결과가 없습니다.");
        } else {
            showChatRooms(filteredList);
        }
    }
}