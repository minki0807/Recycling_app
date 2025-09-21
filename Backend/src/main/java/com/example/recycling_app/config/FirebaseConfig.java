//package com.example.recycling_app.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//
//import javax.annotation.PostConstruct; // @PostConstruct import
//
//import java.io.IOException;
//import java.io.InputStream;
//
//// Firebase Admin SDK를 초기화하는 설정 클래스
//@Configuration
//public class FirebaseConfig {
//
//    // Firebase 서비스 계정 키 파일 경로 (application.properties에서 가져옴)
//    @Value("${firebase.sdk.path}")
//    private String firebaseSdkPath;
//
//    // application.properties에 스토리지 버킷 이름 추가
//    @Value("${firebase.storage.bucket}")
//    private String firebaseStorageBucket;
//
//    private final ResourceLoader resourceLoader;
//
//    // 생성자: ResourceLoader 주입
//    public FirebaseConfig(ResourceLoader resourceLoader) {
//        this.resourceLoader = resourceLoader;
//    }
//
//    // Spring 애플리케이션 시작 시 Firebase Admin SDK 초기화
//    @PostConstruct // 이 어노테이션을 사용하여 빈이 생성된 후 초기화 메서드 호출
//    public void init() throws IOException {
//        // Firebase 앱이 이미 초기화되었는지 확인 (중복 초기화 방지)
//        if (FirebaseApp.getApps().isEmpty()) {
//            // Classpath에서 서비스 계정 키 파일 로드
//            Resource resource = resourceLoader.getResource(firebaseSdkPath);
//            InputStream serviceAccount = resource.getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .setDatabaseUrl("https://<YOUR_DATABASE_NAME>.firebaseio.com") // 실시간 DB 사용 시 설정
//                    .setStorageBucket(firebaseStorageBucket)
//                    .build();
//
//            FirebaseApp.initializeApp(options);
//            System.out.println("Firebase Admin SDK 초기화 성공!");
//        } else {
//            System.out.println("Firebase Admin SDK는 이미 초기화되어 있습니다.");
//        }
//    }
//}