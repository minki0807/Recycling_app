package com.example.recycling_app.controller;

import com.example.recycling_app.service.FirebaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/community")
public class MediaController {
    private final FirebaseStorageService firebaseStorageService;

    public MediaController(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    @PostMapping("/media/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) throws Exception {
        String mimeType = file.getContentType();
        if (mimeType == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "파일 형식이 없습니다."));
        }

        boolean allowed = mimeType.startsWith("image/") ||
                mimeType.startsWith("video/") ||
                mimeType.contains("haansofthwp") ||
                mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                mimeType.equals("application/pdf") ||
                mimeType.equals("application/octet-stream");;
        System.out.println("Uploaded file MIME type: " + mimeType);


        if (!allowed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "지원하지 않는 파일 형식입니다."));
        }
        if (file.getSize() > 20 * 1024 * 1024) { // 20MB 제한
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "20MB 이하 파일만 업로드 가능합니다."));
        }

        String url = firebaseStorageService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}