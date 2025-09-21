package com.example.recycling_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks; // Tasks import 추가 (동기적 대기용)
import android.util.Log;
import java.util.concurrent.ExecutionException; // ExecutionException import 추가

// Firebase 인증 및 사용자 토큰 관리를 위한 유틸리티 클래스
public class AuthManager {

    private static final String TAG = "AuthManager"; // 로그 태그
    private FirebaseAuth mAuth;
    private Context context; // SharedPreferences를 위해 Context 필요

    // SharedPreferences 키 (필요하다면 사용자 정보 저장에 사용)
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_UID = "user_uid";
    private static final String KEY_ID_TOKEN = "id_token"; // 사용자가 수동으로 저장할 경우

    // 생성자: FirebaseAuth 인스턴스와 Context를 초기화
    public AuthManager(Context context) {
        this.context = context.getApplicationContext(); // 메모리 누수 방지를 위해 Application Context 사용
        mAuth = FirebaseAuth.getInstance();
    }

    // 1. AuthDataCallback 인터페이스 정의
    // Firebase ID 토큰 획득 결과를 비동기적으로 전달하기 위한 콜백 인터페이스
    public interface AuthDataCallback {
        void onAuthDataReceived(String uid, String idToken); // 인증 데이터 성공적으로 획득 시 호출
        void onAuthDataError(String errorMessage); // 인증 데이터 획득 실패 시 호출
    }

    // 2. getAuthData 메서드 정의 (비동기 방식: UI 업데이트 등에 사용)
    // 현재 로그인된 사용자의 UID와 Firebase ID 토큰을 비동기적으로 가져오는 메서드
    public void getAuthData(AuthDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Firebase ID 토큰을 강제로 새로 고침 (true)
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.GetTokenResult>() {
                        @Override
                        public void onComplete(Task<com.google.firebase.auth.GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                String uid = currentUser.getUid();
                                Log.d(TAG, "ID Token refreshed successfully. UID: " + uid);
                                // 성공적으로 데이터를 획득하면 콜백 호출
                                callback.onAuthDataReceived(uid, idToken);
                            } else {
                                String errorMessage = "Firebase ID 토큰 가져오기 실패: " + task.getException().getMessage();
                                Log.e(TAG, errorMessage, task.getException());
                                // 오류 발생 시 콜백 호출
                                callback.onAuthDataError(errorMessage);
                            }
                        }
                    });
        } else {
            String errorMessage = "사용자가 로그인되지 않았습니다.";
            Log.w(TAG, errorMessage);
            // 로그인된 사용자가 없으면 콜백 호출
            callback.onAuthDataError(errorMessage);
        }
    }

    // 3. getIdTokenSync 메서드 정의 (동기 방식: Retrofit Interceptor 등에 사용)
    // 현재 로그인된 사용자의 Firebase ID 토큰을 동기적으로 가져오는 메서드
    // 이 메서드는 UI 스레드에서 호출되면 ANR (Application Not Responding)을 유발할 수 있으므로,
    // 반드시 백그라운드 스레드 (예: OkHttp Interceptor)에서 호출해야함.
    public String getIdTokenSync() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "getIdTokenSync: No current Firebase user.");
            return null;
        }
        try {
            // Tasks.await()를 사용하여 비동기 Task를 동기적으로 기다림.
            // getIdToken(false)는 캐시된 토큰을 반환하거나, 만료되었을 경우 네트워크를 통해 새로 고침.
            return Tasks.await(currentUser.getIdToken(false)).getToken();
        } catch (ExecutionException e) {
            Log.e(TAG, "getIdTokenSync: ExecutionException - " + e.getMessage(), e);
            return null;
        } catch (InterruptedException e) {
            Log.e(TAG, "getIdTokenSync: InterruptedException - " + e.getMessage(), e);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            return null;
        }
    }

    // 현재 로그인된 사용자의 UID를 동기적으로 반환 (null일 수 있음)
    public String getUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    // 다른 클래스(RetrofitClient 등)와의 호환성을 위해 getUid() 메서드를 추가합니다.
    public String getUid() {
        return getUserId();
    }

    // 사용자 인증 정보를 Firebase 및 SharedPreferences에서 삭제 (로그아웃 또는 탈퇴 시)
    public void clearAuthData() {
        // Firebase 로그아웃
        mAuth.signOut();
        // SharedPreferences에서 저장된 데이터 삭제 (만약 저장했다면)
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_UID);
        editor.remove(KEY_ID_TOKEN);
        editor.apply();
        Log.d(TAG, "인증 데이터 및 Firebase 세션이 지워졌습니다.");
    }

    // Firebase ID 토큰을 SharedPreferences에 저장하는 메서드
    // 재시작 시 토큰을 빠르게 가져오거나, 오프라인 시나리오에 대비할 때 유용할 수 있지만,
    // Firebase SDK가 토큰 캐싱을 잘 처리하므로 필수는 아님.
    public void saveIdToken(String uid, String idToken) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_ID_TOKEN, idToken);
        editor.apply();
        Log.d(TAG, "UID 및 ID Token이 SharedPreferences에 저장되었습니다.");
    }

    // (선택 사항) SharedPreferences에서 ID 토큰을 가져오는 메서드
    public String getSavedIdToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ID_TOKEN, null);
    }
}