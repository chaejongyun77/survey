package com.woongjin.survey.domain.member.service;

import com.woongjin.survey.domain.auth.service.AuthService;
import com.woongjin.survey.domain.auth.service.TokenResponse;
import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.global.jwt.JwtProperties;
import com.woongjin.survey.global.jwt.JwtTokenProvider;
import com.woongjin.survey.domain.auth.infra.RedisTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * AuthService.login() 단위 테스트
 * <p>
 * - 실제 DB / Redis / JWT 없이 Mock으로 대체
 * - login() 안의 로직 흐름만 순수하게 검증
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // =============================================
    // Mock 선언 — 실제 구현체 대신 가짜 객체 사용
    // =============================================

    @Mock
    private AuthenticationManager authenticationManager;  // 실제 DB/BCrypt 검증 안 함

    @Mock
    private JwtTokenProvider jwtTokenProvider;            // 실제 JWT 생성 안 함

    @Mock
    private RedisTokenRepository redisTokenRepository;    // 실제 Redis 연결 안 함

    @Mock
    private JwtProperties jwtProperties;                  // 실제 .env 안 읽음

    @InjectMocks
    private AuthService authService;                      // Mock들을 주입받은 테스트 대상

    // =============================================
    // 공통 테스트 데이터
    // =============================================

    private UserPrincipal UserPrincipal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 로그인 성공 시 authenticationManager가 반환할 가짜 UserPrincipal
        UserPrincipal = new UserPrincipal(
                1L,
                "admin",
                "encoded_password",
                "관리자",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // authenticationManager.authenticate()가 반환할 가짜 Authentication 객체
        authentication = new UsernamePasswordAuthenticationToken(
                UserPrincipal, null, UserPrincipal.getAuthorities()
        );
    }

    // =============================================
    // 로그인 성공
    // =============================================

    @Test
    @DisplayName("로그인 성공 — Access/Refresh Token 반환")
    void login_성공() {
        // Arrange: Mock 동작 정의
        given(authenticationManager.authenticate(any()))
                .willReturn(authentication);                         // ID/PW 검증 통과
        given(jwtTokenProvider.generateAccessToken(anyLong(), anyString(), anyString()))
                .willReturn("mock.access.token");                   // Access Token 생성
        given(jwtTokenProvider.generateRefreshToken(anyLong()))
                .willReturn("mock.refresh.token");                  // Refresh Token 생성
        given(jwtProperties.getRefreshTokenExpiration())
                .willReturn(604800000L);                            // 7일 만료시간

        // Act: 테스트 대상 메서드 호출
        TokenResponse result = authService.login("admin", "1234");

        // Assert: 결과 검증
        assertThat(result.accessToken()).isEqualTo("mock.access.token");
        assertThat(result.refreshToken()).isEqualTo("mock.refresh.token");

        // Redis에 Refresh Token 저장이 호출됐는지 검증
        verify(redisTokenRepository).saveRefreshToken(1L, "mock.refresh.token", 604800000L);
    }

    // =============================================
    // 로그인 실패 — 아이디/비밀번호 불일치
    // =============================================

    @Test
    @DisplayName("로그인 실패 — 아이디 또는 비밀번호 불일치")
    void login_실패_비밀번호_불일치() {
        // Arrange: authenticate()가 예외를 던지도록 설정
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("비밀번호가 올바르지 않습니다."));

        // Act & Assert: 예외 발생 검증
        assertThatThrownBy(() -> authService.login("admin", "wrong_password"))
                .isInstanceOf(BadCredentialsException.class);
    }


}
