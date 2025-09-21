package com.example.recycling_app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseInitializer {

    @Value("${firebase.sdk.path}")
    private String firebaseSdkPath;

    @Value("${firebase.storage.bucket}")
    private String firebaseStorageBucket;


    @PostConstruct
    public void init() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/firebase/firebase-service-account.json");

//            // FileInputStream 대신 클래스패스 리소스를 InputStream으로 직접 읽어옵니다.(aws ec2 전용)
//            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseSdkPath);
//
//            // 파일을 찾지 못했을 경우를 대비한 예외 처리
//            if (serviceAccount == null) {
//                throw new IOException("Firebase 서비스 계정 키 파일을 찾을 수 없습니다: " + firebaseSdkPath);
//            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(firebaseStorageBucket)
                    .build();

            // Firebase 앱이 초기화 되어 있지 않으면 초기화 수행
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public Storage storage() {
        return StorageClient.getInstance().bucket(firebaseStorageBucket).getStorage();
    }
}