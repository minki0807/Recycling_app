package com.example.recycling_app.controller.gemini;

import com.example.recycling_app.dto.gemini.GeminiRequest;
import com.example.recycling_app.dto.gemini.GeminiResponse;
import com.example.recycling_app.service.gemini.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gemini 관련 API 요청을 처리하는 Spring Boot REST 컨트롤러입니다.
 * 클라이언트의 요청을 받아 GeminiService로 전달하고, 그 결과를 응답으로 반환합니다.
 */
@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiController {

    private static final Logger logger = LoggerFactory.getLogger(GeminiController.class);
    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * 분리수거 정보를 요청하는 POST 엔드포인트입니다.
     * HTTP POST 요청을 받아 분류 정보를 포함하는 GeminiRequest 객체를 처리합니다.
     * @param request 클라이언트로부터 받은 분류 정보가 담긴 요청 본문
     * @return Gemini API 호출 결과가 담긴 ResponseEntity 객체
     */
    @PostMapping("/recycling-info")
    public ResponseEntity<GeminiResponse> getRecyclingInfo(@RequestBody GeminiRequest request) {
        // 입력값 검증
        if (request == null || request.getClassification() == null || request.getClassification().trim().isEmpty()) {
            logger.warn("Request body or classification is null or empty.");
            return ResponseEntity.badRequest()
                    .body(GeminiResponse.failure("분류 정보(classification)가 필요합니다."));
        }

        try {
            logger.info("분리수거 정보 요청 받음: {}", request.getClassification());
            String recyclingInfo = geminiService.getRecyclingInfoFromGemini(request.getClassification());

            // 서비스가 성공적으로 결과를 반환하면 success 응답 생성
            return ResponseEntity.ok(GeminiResponse.success(recyclingInfo));

        } catch (Exception e) {
            // 서비스에서 예외가 발생하면 failure 응답 생성
            logger.error("분리수거 정보 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(GeminiResponse.failure("서버 내부 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/health")
    public ResponseEntity<GeminiResponse> healthCheck() {
        return ResponseEntity.ok(GeminiResponse.success("서버가 정상적으로 작동중입니다."));
    }

    // 기존 /ask 엔드포인트도 새로운 방식과 동일하게 수정하여 안정성 확보
    @PostMapping("/ask")
    public ResponseEntity<GeminiResponse> askGemini(@RequestBody GeminiRequest request) {
        // 새로운 /recycling-info 엔드포인트와 동일한 로직을 사용
        return getRecyclingInfo(request);
    }
}