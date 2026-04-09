package com.woongjin.survey.domain.auth.service;

import com.woongjin.survey.domain.auth.infra.RedisTokenRepository;
import com.woongjin.survey.domain.auth.infra.UserPrincipal;
import com.woongjin.survey.domain.employee.domain.Employee;
import com.woongjin.survey.domain.employee.domain.enums.EmployeeRole;
import com.woongjin.survey.global.jwt.JwtAuthException;
import com.woongjin.survey.global.jwt.JwtErrorCode;
import com.woongjin.survey.global.jwt.JwtProperties;
import com.woongjin.survey.global.jwt.JwtTokenProvider;
import com.woongjin.survey.domain.employee.repository.EmployeeRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AuthService 단위 테스트 (Mock)
 * <p>
 * 검증 대상: AuthService가 의존성들을 올바르게 조립해서 쓰는가?
 * - 실제 DB 조회, BCrypt 검증, Redis 저장은 검증하지 않음
 * - 각 의존성 내부 동작은 JwtTokenProviderTest, RedisTokenRepositoryTest에서 별도 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RedisTokenRepository redisTokenRepository;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private Claims mockClaims; // reissue() 테스트에서 getClaims() 반환값으로 사용

    @InjectMocks
    private AuthService authService;

    // 테스트용 고정값
    private static final Long EMP_ID = 1L;
    private static final String EMP_NO = "testUser";
    private static final String PASSWORD = "password123";
    private static final String ROLE = "ROLE_USER";
    private static final String ACCESS_TOKEN = "mocked-access-token";
    private static final String REFRESH_TOKEN = "mocked-refresh-token";
    private static final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7;

    // =========================================================
    // login()
    // =========================================================
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("정상 로그인")
        void 정상_로그인_TokenResponse_반환() {
            // given
            UserPrincipal principal = new UserPrincipal(
                    EMP_ID, EMP_NO, "", "테스트유저", true,
                    List.of(new SimpleGrantedAuthority(ROLE))
            );
            Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(jwtTokenProvider.generateAccessToken(EMP_ID, EMP_NO, ROLE, "테스트유저")).willReturn(ACCESS_TOKEN);
            given(jwtTokenProvider.generateRefreshToken(EMP_ID)).willReturn(REFRESH_TOKEN);
            given(jwtProperties.getRefreshTokenExpiration()).willReturn(REFRESH_EXP);

            // when
            TokenResponse result = authService.login(EMP_NO, PASSWORD);

            // then
            assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }


        @Test
        @DisplayName("비밀번호 불일치")
        void 비밀번호_불일치_BadCredentialsException_전파() {
            // given
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("비밀번호 불일치"));

            // when & then
            assertThatThrownBy(() -> authService.login(EMP_NO, "wrongPassword"))
                    .isInstanceOf(BadCredentialsException.class);

            // 토큰 생성, Redis 저장이 호출되지 않았는지 검증
            then(jwtTokenProvider).shouldHaveNoInteractions();
            then(redisTokenRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("비활성화 계정 ")
        void 비활성화_계정_DisabledException_전파() {
            // given
            given(authenticationManager.authenticate(any()))
                    .willThrow(new DisabledException("비활성화 계정"));

            // when & then
            assertThatThrownBy(() -> authService.login(EMP_NO, PASSWORD))
                    .isInstanceOf(DisabledException.class);

            // 토큰 생성, Redis 저장이 호출되지 않았는지 검증
            then(jwtTokenProvider).shouldHaveNoInteractions();
            then(redisTokenRepository).shouldHaveNoInteractions();
        }
    }

    // =========================================================
    // reissue()
    // =========================================================
    @Nested
    @DisplayName("reissue()")
    class Reissue {

        @Test
        @DisplayName("정상 재발급")
        void 정상_재발급_TokenResponse_반환() {
            // given
            Employee employee = Employee.builder()
                    .empNo(EMP_NO)
                    .empName("테스트유저")
                    .role(EmployeeRole.ADMIN)
                    .build();

            given(mockClaims.getSubject()).willReturn(String.valueOf(EMP_ID));
            given(jwtTokenProvider.getClaims(REFRESH_TOKEN)).willReturn(mockClaims);
            given(redisTokenRepository.getRefreshToken(EMP_ID)).willReturn(REFRESH_TOKEN);
            given(employeeRepository.findById(EMP_ID)).willReturn(Optional.of(employee));
            given(jwtTokenProvider.generateAccessToken(EMP_ID, EMP_NO, "ADMIN", "테스트유저")).willReturn("new-access-token");
            given(jwtTokenProvider.generateRefreshToken(EMP_ID)).willReturn("new-refresh-token");
            given(jwtProperties.getRefreshTokenExpiration()).willReturn(REFRESH_EXP);

            // when
            TokenResponse result = authService.reissue(REFRESH_TOKEN);

            // then
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            then(redisTokenRepository).should(times(1))
                    .saveRefreshToken(EMP_ID, "new-refresh-token", REFRESH_EXP);
        }

        @Test
        @DisplayName("Redis에 토큰 없음")
        void Redis_토큰_없음_REFRESH_TOKEN_NOT_FOUND() {
            // given
            given(mockClaims.getSubject()).willReturn(String.valueOf(EMP_ID));
            given(jwtTokenProvider.getClaims(REFRESH_TOKEN)).willReturn(mockClaims);
            given(redisTokenRepository.getRefreshToken(EMP_ID)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND));
        }

        @Test
        @DisplayName("토큰 불일치 (탈취 의심)")
        void 토큰_불일치_REFRESH_TOKEN_MISMATCH_및_삭제() {
            // given
            given(mockClaims.getSubject()).willReturn(String.valueOf(EMP_ID));
            given(jwtTokenProvider.getClaims(REFRESH_TOKEN)).willReturn(mockClaims);
            given(redisTokenRepository.getRefreshToken(EMP_ID)).willReturn("different-token");

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.REFRESH_TOKEN_MISMATCH));

            then(redisTokenRepository).should(times(1)).deleteRefreshToken(EMP_ID);
        }

        @Test
        @DisplayName("만료/위변조 토큰")
        void 만료_위변조_토큰_JwtAuthException_전파() {
            // given
            given(jwtTokenProvider.getClaims(REFRESH_TOKEN))
                    .willThrow(new JwtAuthException(JwtErrorCode.TOKEN_EXPIRED));

            // when & then
            assertThatThrownBy(() -> authService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(JwtAuthException.class)
                    .satisfies(ex -> assertThat(((JwtAuthException) ex).getErrorCode())
                            .isEqualTo(JwtErrorCode.TOKEN_EXPIRED));

            then(redisTokenRepository).shouldHaveNoInteractions();
            then(employeeRepository).shouldHaveNoInteractions();
        }
    }
}
