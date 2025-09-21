package com.example.recycling_app.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

// FCM(Firebase Cloud Messaging) 푸시 알림 전송을 처리하는 서비스 클래스
@Service
public class FcmPushService {

    // 특정 디바이스 토큰으로 푸시 알림 메시지 전송
    // targetToken: 수신 디바이스의 FCM 토큰
    // title: 푸시 알림 제목
    // body: 푸시 알림 내용
    public void sendPushMessage(String targetToken, String title, String body) throws Exception {
        // 메시지 객체 생성: 수신 토큰과 알림(제목, 본문) 설정
        Message message = Message.builder()
                .setToken(targetToken)                        // 타겟 디바이스 토큰 지정
                .setNotification(Notification.builder()      // 알림 내용 빌더
                        .setTitle(title)                      // 알림 제목 설정
                        .setBody(body)                        // 알림 본문 설정
                        .build())
                .build();

        // FirebaseMessaging 인스턴스를 통해 메시지 전송
        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Sent message: " + response);   // 전송 결과 로그 출력
    }
}
