package com.example.recycling_app.network;

import static com.example.recycling_app.network.ApiClient.getClient;

import android.content.Context;

import com.example.recycling_app.BuildConfig;
import com.example.recycling_app.service.ProductApiService;
import com.example.recycling_app.service.ProfileApiService;
import com.example.recycling_app.util.AuthManager;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Retrofit 클라이언트를 관리하는 싱글톤 클래스.
 * 반드시 Application 클래스에서 init()을 먼저 호출해야 합니다.
 */
public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.BASE_URL;

    // volatile 키워드로 멀티스레드 환경에서 변수 변경 사항의 가시성을 보장합니다.
    private static volatile Retrofit retrofit = null;
//    private static volatile ApiService apiService = null;
    private static volatile ProfileApiService profileApiService = null;
    private static volatile AuthManager authManager = null;
    private static volatile ProductApiService productApiService = null;

    // 외부에서 new RetrofitClient()를 호출하는 것을 막습니다.
    private RetrofitClient() {}

    /**
     * Retrofit 클라이언트를 초기화하는 유일한 메서드.
     * Application의 onCreate()에서 반드시 한 번 호출되어야 합니다.
     * @param context 애플리케이션 컨텍스트
     */
    public static void init(Context context) {
        // Double-checked locking 패턴으로 스레드에 안전한 초기화를 보장합니다.
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    // 1. AuthManager 초기화
                    authManager = new AuthManager(context.getApplicationContext());

                    // 2. Http 로깅 인터셉터 설정
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 3. 인증 토큰을 추가하는 인터셉터 설정
                    Interceptor authInterceptor = chain -> {
                        Request originalRequest = chain.request();
                        Request.Builder requestBuilder = originalRequest.newBuilder();

                        String idToken = authManager.getIdTokenSync();
                        if (idToken != null && !idToken.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + idToken);
                        }

                        Request newRequest = requestBuilder.build();
                        return chain.proceed(newRequest);
                    };

                    // 4. OkHttpClient 설정 (로깅 및 인증 인터셉터 모두 추가)
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(loggingInterceptor)
                            .addInterceptor(authInterceptor)
                            .connectTimeout(60, TimeUnit.SECONDS) // 서버 연결 대기 시간: 60초
                            .readTimeout(60, TimeUnit.SECONDS)    // 서버 응답 대기 시간: 60초
                            .writeTimeout(60, TimeUnit.SECONDS)   // 서버에 데이터 쓰는 시간: 60초
                            .build();

                    // 5. Retrofit 인스턴스 생성
                    // ScalarsConverterFactory를 Gson보다 먼저 추가하여 String 응답을 우선 처리
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
    }

    /**
     * 클라이언트가 초기화되었는지 확인하는 내부 메서드.
     * 초기화되지 않았다면 IllegalStateException을 발생시킵니다.
     */
    private static void checkInitialized() {
        if (retrofit == null) {
            throw new IllegalStateException("RetrofitClient.init(Context) must be called first!");
        }
    }

    public static ProductApiService getProductApiService() {
        checkInitialized();
        if (productApiService == null) {
            synchronized (RetrofitClient.class) {
                if (productApiService == null) {
                    productApiService = retrofit.create(ProductApiService.class);
                }
            }
        }
        return productApiService;
    }

    // ProfileApiService의 싱글톤 인스턴스를 반환
    public static ProfileApiService getProfileApiService() {
        checkInitialized();
        if (profileApiService == null) {
            synchronized (RetrofitClient.class) {
                if (profileApiService == null) {
                    profileApiService = retrofit.create(ProfileApiService.class);
                }
            }
        }
        return profileApiService;
    }

    // AuthManager의 싱글톤 인스턴스를 반환
    public static AuthManager getAuthManager() {
        checkInitialized();
        return authManager;
    }

    // Retrofit 인스턴스 자체를 반환 (필요시 사용)
    public static Retrofit getRetrofitInstance() {
        checkInitialized();
        return retrofit;
    }
}
