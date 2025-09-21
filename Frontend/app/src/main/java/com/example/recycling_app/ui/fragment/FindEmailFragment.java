package com.example.recycling_app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recycling_app.Account.LoginActivity;
import com.example.recycling_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FindEmailFragment extends Fragment {
    private static final String TAG = "FindEmailFragment";

    private EditText nameEditText, numberEditText;
    private Button findIdBtn;
    private LinearLayout initialInputGroup;
    private LinearLayout resultDisplayGroup;
    private TextView foundEmailTextView;
    private Button resetPasswordBtn;
    private TextView loginLinkTextView;

    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_email, container, false);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();
        realtimeDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        Log.d(TAG, "=== FindEmailFragment 초기화 ===");
        Log.d(TAG, "Firestore 인스턴스: " + (firestore != null ? "OK" : "NULL"));
        Log.d(TAG, "Realtime DB 인스턴스: " + (realtimeDatabase != null ? "OK" : "NULL"));

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        nameEditText = view.findViewById(R.id.edit_text_find_email_name);
        numberEditText = view.findViewById(R.id.edit_text_find_email_phone_number);
        findIdBtn = view.findViewById(R.id.button_find_email_submit);
        initialInputGroup = view.findViewById(R.id.initial_input_group);
        resultDisplayGroup = view.findViewById(R.id.result_display_group);
        foundEmailTextView = view.findViewById(R.id.edit_text_found_email);
        resetPasswordBtn = view.findViewById(R.id.button_reset_password_from_find_email);
        loginLinkTextView = view.findViewById(R.id.text_view_login_link);

        initialInputGroup.setVisibility(View.VISIBLE);
        resultDisplayGroup.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        findIdBtn.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String number = numberEditText.getText().toString().trim();

            Log.d(TAG, "=== 아이디 찾기 버튼 클릭 ===");
            Log.d(TAG, "입력된 이름: '" + name + "'");
            Log.d(TAG, "입력된 전화번호: '" + number + "'");

            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(getContext(), "이름과 전화번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 디버깅을 위해 먼저 전체 데이터 구조 확인
            debugDatabaseStructure();

            // 실제 검색 수행
            findUserId(name, number);
        });

        resetPasswordBtn.setOnClickListener(v -> {
            ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, resetPasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loginLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void debugDatabaseStructure() {
        Log.d(TAG, "=== 데이터베이스 구조 디버깅 ===");

        // Realtime Database 전체 구조 확인
        realtimeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "--- Realtime Database 구조 ---");
                Log.d(TAG, "users 노드 존재 여부: " + dataSnapshot.exists());
                Log.d(TAG, "자식 개수: " + dataSnapshot.getChildrenCount());

                int count = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    count++;
                    if (count <= 3) { // 처음 3개만 로그 출력
                        Log.d(TAG, "사용자 " + count + " - Key: " + child.getKey());
                        Log.d(TAG, "  - name: " + child.child("name").getValue());
                        Log.d(TAG, "  - phoneNumber: " + child.child("phoneNumber").getValue());
                        Log.d(TAG, "  - email: " + child.child("email").getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Realtime DB 디버깅 실패: " + databaseError.getMessage());
            }
        });

        // Firestore 구조 확인
        firestore.collection("users").limit(3).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "--- Firestore 구조 ---");
                    Log.d(TAG, "문서 개수: " + queryDocumentSnapshots.size());

                    int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        count++;
                        Log.d(TAG, "문서 " + count + " - ID: " + doc.getId());
                        Log.d(TAG, "  - name: " + doc.getString("name"));
                        Log.d(TAG, "  - phoneNumber: " + doc.getString("phoneNumber"));
                        Log.d(TAG, "  - email: " + doc.getString("email"));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore 디버깅 실패", e));
    }

    private void findUserId(String name, String number) {
        Log.d(TAG, "=== 사용자 검색 시작 ===");

        // 1단계: Firestore에서 검색
        searchInFirestore(name, number);
    }

    private void searchInFirestore(String name, String number) {
        Log.d(TAG, "--- Firestore 검색 시작 ---");

        firestore.collection("users")
                .get() // 모든 문서 가져와서 로컬에서 필터링 (디버깅용)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Firestore 쿼리 성공, 문서 개수: " + queryDocumentSnapshots.size());

                    boolean found = false;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String docName = doc.getString("name");
                        String docPhone = doc.getString("phoneNumber");
                        String docEmail = doc.getString("email");

                        Log.d(TAG, "검사 중 - 문서 ID: " + doc.getId());
                        Log.d(TAG, "  DB 이름: '" + docName + "' vs 입력 이름: '" + name + "'");
                        Log.d(TAG, "  DB 전화번호: '" + docPhone + "' vs 입력 전화번호: '" + number + "'");
                        Log.d(TAG, "  이름 일치: " + (docName != null && docName.equals(name)));
                        Log.d(TAG, "  전화번호 일치: " + (docPhone != null && docPhone.equals(number)));

                        if (docName != null && docName.equals(name) &&
                                docPhone != null && docPhone.equals(number)) {
                            Log.d(TAG, "=== Firestore에서 일치하는 사용자 발견! ===");
                            Log.d(TAG, "이메일: " + docEmail);

                            if (docEmail != null && !docEmail.isEmpty()) {
                                foundEmailTextView.setText(docEmail);
                                showResultView(true);
                                found = true;
                                Toast.makeText(getContext(), "이메일을 찾았습니다!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    if (!found) {
                        Log.d(TAG, "Firestore에서 찾지 못함. Realtime Database 검색으로 이동");
                        searchInRealtimeDatabase(name, number);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore 검색 실패", e);
                    Toast.makeText(getContext(), "Firestore 검색 중 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    searchInRealtimeDatabase(name, number);
                });
    }

    private void searchInRealtimeDatabase(String name, String number) {
        Log.d(TAG, "--- Realtime Database 검색 시작 ---");

        realtimeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Realtime DB 검색 - 데이터 존재: " + dataSnapshot.exists());
                Log.d(TAG, "자식 개수: " + dataSnapshot.getChildrenCount());

                boolean found = false;

                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Realtime Database에 users 노드가 존재하지 않음");
                    showNotFoundResult("데이터베이스에 사용자 정보가 없습니다.");
                    return;
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue(String.class);
                    String userNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                    String userId = userSnapshot.child("email").getValue(String.class);

                    Log.d(TAG, "검사 중 - 사용자 Key: " + userSnapshot.getKey());
                    Log.d(TAG, "  DB 이름: '" + userName + "' vs 입력 이름: '" + name + "'");
                    Log.d(TAG, "  DB 전화번호: '" + userNumber + "' vs 입력 전화번호: '" + number + "'");
                    Log.d(TAG, "  이름 일치: " + (userName != null && userName.equals(name)));
                    Log.d(TAG, "  전화번호 일치: " + (userNumber != null && userNumber.equals(number)));

                    if (userName != null && userName.equals(name) &&
                            userNumber != null && userNumber.equals(number)) {
                        Log.d(TAG, "=== Realtime DB에서 일치하는 사용자 발견! ===");
                        Log.d(TAG, "이메일: " + userId);

                        if (userId != null && !userId.isEmpty()) {
                            foundEmailTextView.setText(userId);
                            showResultView(true);
                            found = true;
                            Toast.makeText(getContext(), "이메일을 찾았습니다!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }

                if (!found) {
                    Log.d(TAG, "=== 두 데이터베이스 모두에서 사용자를 찾지 못함 ===");
                    showNotFoundResult("해당하는 사용자를 찾을 수 없습니다.\n이름과 전화번호를 다시 확인해주세요.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Realtime DB 검색 취소됨: " + databaseError.getMessage());
                showNotFoundResult("데이터베이스 연결 오류가 발생했습니다.");
            }
        });
    }

    private void showNotFoundResult(String message) {
        showResultView(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void showResultView(boolean isSuccess) {
        if (isSuccess) {
            initialInputGroup.setVisibility(View.GONE);
            resultDisplayGroup.setVisibility(View.VISIBLE);
        } else {
            initialInputGroup.setVisibility(View.VISIBLE);
            resultDisplayGroup.setVisibility(View.GONE);
        }
    }
}