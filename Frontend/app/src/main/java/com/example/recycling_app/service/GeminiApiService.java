package com.example.recycling_app.service;

import android.util.Log;
import androidx.annotation.NonNull;

import com.example.recycling_app.BuildConfig;
import com.example.recycling_app.dto.GeminiRequest;
import com.example.recycling_app.dto.GeminiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 안드로이드 앱에서 백엔드 서버의 API와 통신하기 위한 서비스 클래스
 */
public class GeminiApiService {

    private static final String TAG = "GeminiApiService";
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static final int TIMEOUT_SECONDS = 30;

    private static GeminiApiService instance;
    private final GeminiApiInterface apiInterface;

    public static synchronized GeminiApiService getInstance() {
        if (instance == null) {
            instance = new GeminiApiService();
        }
        return instance;
    }

    private GeminiApiService() {
        Gson gson = new GsonBuilder().serializeNulls().create();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiInterface = retrofit.create(GeminiApiInterface.class);
    }

    /**
     * 분리수거 정보를 요청하는 메서드 (비동기)
     *
     * @param classification 분류된 쓰레기 종류
     * @param callback       결과를 받을 콜백
     */
    public void getRecyclingInfo(@NonNull String classification, @NonNull ApiCallback callback) {
        if (classification.trim().isEmpty()) {
            callback.onError("분류 정보가 비어있습니다.");
            return;
        }

        Log.d(TAG, "분리수거 정보 요청 시작: " + classification);
        GeminiRequest request = new GeminiRequest(classification);

        Call<GeminiResponse> call = apiInterface.getRecyclingInfo(request);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeminiResponse> call, @NonNull Response<GeminiResponse> response) {
                if (response.isSuccessful()) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse != null && geminiResponse.isSuccess()) {
                        Log.d(TAG, "API 호출 성공. \n데이터\n" + geminiResponse.getData());
                        callback.onSuccess(geminiResponse.getData());
                    } else {
                        String errorMessage = (geminiResponse != null) ? geminiResponse.getMessage() : "서버 응답이 비어있습니다.";
                        Log.e(TAG, "서버 로직 오류: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                } else {
                    String errorMsg = "서버 응답 오류: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "에러 바디 파싱 실패", e);
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeminiResponse> call, @NonNull Throwable t) {
                String errorMsg = "네트워크 오류: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Retrofit 인터페이스 정의
     */
    private interface GeminiApiInterface {
        @POST("api/gemini/recycling-info")
        Call<GeminiResponse> getRecyclingInfo(@Body GeminiRequest request);
    }

    /**
     * API 호출 결과를 받는 콜백 인터페이스
     */
    public interface ApiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    /**
     * HTTP 요청/응답을 로깅하는 인터셉터
     */
    private static class LoggingInterceptor implements Interceptor {
        @NonNull
        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Log.d(TAG, "--> " + request.method() + " " + request.url());
            long startTime = System.nanoTime();
            okhttp3.Response response = chain.proceed(request);
            long endTime = System.nanoTime();
            Log.d(TAG, "<-- " + response.code() + " " + response.request().url() + " (" + (endTime - startTime) / 1e6d + "ms)");
            return response;
        }
    }
}
