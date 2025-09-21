package com.example.recycling_app.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// Firebase ID 토큰을 검증하는 필터
@Slf4j
public class FirebaseTokenFilter extends OncePerRequestFilter {

//  테스트용 임시 UID 정의
//  private static final String TEST_UID = "test_user_uid_123";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        // 토큰이 없거나 "Bearer "로 시작하지 않으면 다음 필터로 넘김
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authorizationHeader.substring(7); // "Bearer " 제외한 실제 토큰
        log.info("수신된 ID Token: [{}]", idToken);

//        // --- 여기에 테스트 모드 로직 추가 시작 ---
//        if ("test_token".equals(idToken)) {
//            String uid = "test_user_uid_123";
//
//            UserDetails userDetails = User.builder()
//                    .username(uid)
//                    .password("")
//                    .authorities(Collections.singletonList(() -> "ROLE_USER"))
//                    .build();
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            request.setAttribute("uid", uid);
//
//            log.info("테스트 모드 활성화: Firebase ID Token 검증 건너뛰고 UID: {} 사용", uid);
//            filterChain.doFilter(request, response); // 여기서 다음 필터로 바로 진행
//            return; // 이 요청에 대한 필터링을 여기서 종료
//        }
//// --- 테스트 모드 로직 추가 끝 ---

        try {
            // 2. Firebase Admin SDK를 사용하여 ID 토큰 검증
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // 3. 검증된 토큰에서 사용자 정보(UID) 추출
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail(); // 이메일도 필요시 추출

            // 4. Spring Security 컨텍스트에 사용자 정보 저장
            // 여기서는 간단하게 UserDetails 객체를 생성하여 저장
            UserDetails userDetails = User.builder()
                    .username(uid) // UID를 username으로 사용
                    .password("") // 비밀번호는 토큰 기반 인증이므로 필요 없음
                    .authorities(Collections.singletonList(() -> "ROLE_USER")) // 기본 역할 부여
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 현재 요청의 SecurityContext에 Authentication 객체 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. 요청 애트리뷰트에 UID 저장 (컨트롤러/서비스에서 쉽게 접근 가능)
            request.setAttribute("uid", uid);
            log.info("Firebase ID Token 검증 성공. UID: {}", uid);

        } catch (FirebaseAuthException e) {
            log.error("Firebase ID Token 검증 실패: {}", e.getMessage());
            // 토큰이 유효하지 않으면 401 Unauthorized 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Firebase ID token.");
            return; // 필터 체인 중단
        } catch (Exception e) {
            log.error("Firebase ID Token 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An unexpected error occurred during token verification.");
            return; // 필터 체인 중단
        }

        // 다음 필터 또는 서블릿으로 요청 전달
        filterChain.doFilter(request, response);
    }
}
