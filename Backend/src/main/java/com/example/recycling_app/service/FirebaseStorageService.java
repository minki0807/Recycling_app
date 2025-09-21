
package com.example.recycling_app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {

    private static final String BUCKET_NAME = "your-project-id.appspot.com";

    @PostConstruct
    public void initialize() throws Exception {
        ClassPathResource resource = new ClassPathResource("firebase/firebase-service-account.json");
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(BUCKET_NAME)
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }
    }

    /**
     * 폴더 이름이 없는 경우, 메인 메소드에 null을 전달하여 호출
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 중복 로직을 없애고 아래의 메인 메소드를 호출하도록 변경
        return this.uploadFile(file, null);
    }

    // MultipartFile을 Firebase Storage의 지정된 폴더에 업로드하고 공개 URL을 반환합니다.
    public String uploadFile(MultipartFile file, String folderName) throws Exception {
        // 파일 이름 생성 (코드를 한 곳에서 관리)
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        String fullPath;

        // folderName 유무에 따라 경로 설정
        if (StringUtils.hasText(folderName)) {
            fullPath = folderName.trim() + "/" + fileName;
        } else {
            fullPath = fileName;
        }

        var bucket = StorageClient.getInstance().bucket();

        // 파일 업로드
        bucket.create(fullPath, file.getBytes(), file.getContentType());

        // 업로드된 파일의 참조(Blob)를 가져옵니다.
        Blob blob = bucket.get(fullPath);

        // 유효한 서명된 URL(토큰 포함)을 생성합니다.
        URL signedUrl = blob.signUrl(36500, TimeUnit.DAYS);

        // 생성된 완전한 URL을 문자열로 반환합니다.
        return signedUrl.toString();
    }

    // Firebase Storage에서 파일을 삭제합니다.
    public void deleteFile(String fileUrl) throws Exception {
        // fileUrl에서 버킷 내부 파일 경로(fullPath)를 추출합니다.
        var bucket = StorageClient.getInstance().bucket();
        String encodedFullPath = fileUrl.split("/o/")[1].split("\\?")[0];
        String fullPath = URLDecoder.decode(encodedFullPath, StandardCharsets.UTF_8);

        Blob blob = bucket.get(fullPath);

        if (blob != null && blob.exists()) {
            blob.delete();
            System.out.println("Storage 파일 삭제 성공: " + fullPath);
        } else {
            System.out.println("Storage 파일이 존재하지 않음: " + fullPath);
        }
    }
}