package com.example.recycling_app.network;

import android.app.Application;
import com.example.recycling_app.network.RetrofitClient;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 앱이 시작될 때 RetrofitClient를 초기화합니다.
        RetrofitClient.init(this);
    }
}
