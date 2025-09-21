package com.example.recycling_app.service.gemini;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

    // CHANGED: RestTemplate을 직접 생성하지 않고 주입받음
    private final RestTemplate restTemplate;

    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRecyclingInfoFromGemini(String classification) {
        // 입력 검증 (null이나 빈 문자열인 경우)
        if (classification == null || classification.trim().isEmpty()) {
            log.warn("분류 정보가 비어있습니다.");
            // CHANGED: 오류 발생 시 예외를 던짐
            throw new IllegalArgumentException("분류 정보가 비어있습니다.");
        }

        try {
            String url = String.format("%s/%s:generateContent?key=%s", GEMINI_API_BASE_URL, geminiModel, apiKey);
            String prompt = createPrompt(classification);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            return extractTextFromResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Gemini API 클라이언트 오류 발생: status={}, body={}, error={}", e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
            // CHANGED: API 호출 실패 시 구체적인 예외를 던짐
            throw new RuntimeException("Gemini API 호출에 실패했습니다. 상태 코드: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Gemini API 호출 중 알 수 없는 오류 발생: classification={}, error={}", classification, e.getMessage(), e);
            // CHANGED: 그 외 모든 예외를 던짐
            throw new RuntimeException("분리수거 정보를 가져오는 중 오류가 발생했습니다.");
        }
    }

    private String createPrompt(String classification) {
        return String.format(
                "'%s'의 분리수거 방법을 알려주세요. 다음 조건을 반드시 지켜주세요:\n\n" +
                        "1. 단계별로 번호를 붙여서 설명해주세요\n" +
                        "2. 각 단계는 간략하게 명확한 문장 한줄로 작성해주세요\n" +
                        "3. 특수문자나 마크다운 기호는 사용하지 마세요\n" +
                        "4. 각 단계 사이에 한 줄씩 띄어주세요\n" +
                        "5. 최대 3-4개 단계로 압축해서 핵심만 알려주세요\n" +
                        "6. 한국의 분리수거 기준에 맞춰 설명해주세요",
                classification
        );
    }

    private String extractTextFromResponse(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("API로부터 빈 응답을 받았습니다.");
        }
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                // CHANGED: 응답에 'candidates'가 없는 경우를 처리
                log.warn("Gemini 응답에 'candidates' 필드가 없습니다. 응답 본문: {}", responseBody);
                return "정보를 생성하지 못했습니다. 입력 값을 확인해주세요.";
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Gemini API 응답 파싱 오류: {}", e.getMessage(), e);
            throw new RuntimeException("API 응답을 처리하는 중 오류가 발생했습니다.");
        }
    }
}