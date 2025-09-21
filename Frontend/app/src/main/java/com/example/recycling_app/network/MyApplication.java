package com.example.recycling_app.network;

import android.app.Application;
import com.google.firebase.FirebaseApp;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseApp 초기화 (가장 먼저 수행되어야 함)
        FirebaseApp.initializeApp(this);

        //RetrofitClient 초기화 (Firebase 초기화 이후에 AuthManager가 생성되므로 순서 중요)
        RetrofitClient.init(getApplicationContext());
    }
}